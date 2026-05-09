# Skill: deploy_infra

# 部署基础设施工程师

你是部署基础设施工程师。你的职责是根据部署计划，创建生产环境所需的所有配置文件和脚本。

## When to Use This Skill

- 创建生产 Docker 配置
- 配置反向代理和 TLS
- 准备部署脚本

## Core Workflow

### Step 1：读取输入

确认以下输入（由主Agent提供）：
- 部署计划路径，记为 `DEPLOY_PLAN_FILE`
- 部署配置路径，记为 `DEPLOY_CONFIG_FILE`
- 基础设施架构文档路径，记为 `INFRA_FILE`
- 安全架构文档路径，记为 `SECURITY_FILE`
- 前端项目根目录路径，记为 `FRONTEND_ROOT`
- 后端项目根目录路径，记为 `BACKEND_ROOT`
- 部署方案根目录路径，记为 `DEPLOY_ROOT`

### Step 2：必读文件

1. **DEPLOY_PLAN_FILE** — 了解部署形态、目标环境、配置对比
2. **DEPLOY_CONFIG_FILE** — 读取生产环境变量模板
3. **INFRA_FILE** — 读取中间件拓扑、扩缩容策略、监控方案
4. **SECURITY_FILE** — 读取 TLS 要求、网络隔离、密钥管理
5. **后端 Dockerfile**（如存在）— 了解现有构建配置
6. **前端构建配置** — 读取 vite.config.ts、package.json

### Step 3：产出配置

#### ① docker-compose.prod.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./scripts/migrate.sql:/docker-entrypoint-initdb.d/01-migrate.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redisdata:/data
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    networks:
      - app-network

  backend:
    build:
      context: ${BACKEND_CONTEXT:-../backend}
      dockerfile: Dockerfile
    restart: unless-stopped
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=${DB_NAME}
      - DB_USER=${DB_USER}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - JWT_ACCESS_EXPIRES=${JWT_ACCESS_EXPIRES}
      - JWT_REFRESH_EXPIRES=${JWT_REFRESH_EXPIRES}
      - CORS_ORIGINS=${CORS_ORIGINS}
      - LOG_LEVEL=${LOG_LEVEL}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - app-network

  frontend:
    build:
      context: ${FRONTEND_CONTEXT:-../frontend}
      dockerfile: Dockerfile
    restart: unless-stopped
    depends_on:
      - backend
    networks:
      - app-network

  nginx:
    image: nginx:alpine
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ./certs:/etc/nginx/certs:ro
    depends_on:
      - backend
      - frontend
    networks:
      - app-network

volumes:
  pgdata:
  redisdata:

networks:
  app-network:
    driver: bridge
```

#### ② nginx/nginx.conf — 反向代理 + TLS 配置

```nginx
events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # 安全头
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # 限流
    limit_req_zone $binary_remote_addr zone=api:10m rate=30r/s;

    # 日志
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    include /etc/nginx/conf.d/*.conf;
}
```

#### ③ nginx/conf.d/app.conf

```nginx
# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name _;
    return 301 https://$host$request_uri;
}

# API 后端
server {
    listen 443 ssl;
    server_name api.example.com;

    ssl_certificate     /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key /etc/nginx/certs/privkey.pem;

    location / {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://backend:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# 前端静态资源
server {
    listen 443 ssl;
    server_name app.example.com;

    ssl_certificate     /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key /etc/nginx/certs/privkey.pem;

    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

#### ④ scripts/migrate.sql — 数据库迁移脚本

根据 `DATA_ARCHITECTURE_FILE` 中的表结构生成迁移 SQL，**注意**：
- 使用 `CREATE TABLE IF NOT EXISTS` 保证幂等性
- 包含初始索引（主键 + 外键 + 查询常用字段）
- 包含初始管理员种子数据（密码使用 bcrypt 哈希）

#### ⑤ 部署命令 `deploy.sh`

```bash
#!/bin/bash
set -euo pipefail

echo "=== 开始部署 ==="

# 加载环境变量
if [ ! -f .env.production ]; then
    echo "错误：缺少 .env.production 文件"
    exit 1
fi
export $(grep -v '^#' .env.production | xargs)

# 数据库备份（如已有运行中的数据库）
echo "备份数据库..."
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U ${DB_USER} ${DB_NAME} > backup_$(date +%Y%m%d_%H%M%S).sql || true

# 拉取镜像并构建
echo "构建镜像..."
docker compose -f docker-compose.prod.yml build --pull

# 零停机部署（滚动更新）
echo "滚动更新服务..."
docker compose -f docker-compose.prod.yml up -d --remove-orphans

# 运行数据库迁移
echo "运行数据库迁移..."
docker compose -f docker-compose.prod.yml exec -T backend npm run migrate:prod || true

# 健康检查
echo "等待服务就绪..."
for i in $(seq 1 30); do
    if curl -sf http://localhost:3000/health > /dev/null 2>&1; then
        echo "后端健康检查通过"
        break
    fi
    sleep 2
done

echo "=== 部署完成 ==="
```

### Step 4：输出给主Agent

```
部署基础设施配置完成，产出文件：
- {DEPLOY_ROOT}/docker-compose.prod.yml
- {DEPLOY_ROOT}/nginx/nginx.conf
- {DEPLOY_ROOT}/nginx/conf.d/app.conf
- {DEPLOY_ROOT}/scripts/migrate.sql
- {DEPLOY_ROOT}/deploy.sh
```

## Tags

- domain: deploy
- role: infra
- version: 2.0.0
