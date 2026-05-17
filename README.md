# Skill: harness_overview

# Harness Engineering — 多智能体协同开发系统

基于 AI Agent 的**多智能体协同开发框架**，通过 **1 个平台门户 + 1 个架构设计模块 + 4 个领域开发模块 + 1 个集成模块 + 1 个部署模块**共 **36 个子智能体**协作，实现从客户端需求提交到生产部署的全流程自动化研发。

> 🆕 **外包开发平台已上线** — 非技术客户可通过 Web 界面提交需求、实时追踪 AI 开发进度、查看验收报告。详见下方「外包平台使用」。

| 平台模块 | 说明 | 状态 |
|---------|------|------|
| **platform/** | 客户端门户（Vue 3）+ 项目API（Spring Boot）+ 需求采集/交付验收智能体 | ✅ 已实现 |
| **client-portal/** | 客户 Web 界面：注册登录、提交需求、进度追踪、验收报告 | ✅ 已实现 |
| **platform-api/** | REST API：项目 CRUD、JWT 认证、文件状态轮询、owner 权限隔离 | ✅ 已实现 |

## When to Use This Skill

- 需要快速了解项目概览、目录结构和核心设计理念
- 需要了解标准使用流程的顶层视图
- 需要查阅各领域模块的职责和文档索引
- 需要了解目标技术栈和项目定位

## Core Content

## 快速概览

```
platform/ → architecture/ → backend/ + frontend/ + flutter/ + blockchain/ → fullstack/ → deploy/ → platform/
 (Phase -1)   (Phase 0)              (Phase 1, 可并行)                      (Phase 2)   (Phase 3)  (Phase 4)
 需求采集      架构设计                 并行开发                               集成联调     部署上线    交付验收
```

每个领域内部遵循统一的 **计划 → 批量开发-测试循环 → 收尾** 三阶段流水线。

> **Phase -1 和 Phase 4** 由 `platform/` 领域负责：客户端通过 Web 界面提交需求 → AI 生成 PRD → 开发 → 部署 → 自动生成验收报告。

## 目录结构

```
├── platform/           # Phase -1 + Phase 4: 外包平台门户（2 个Agent + Web 应用）
│   ├── agents/         # 子智能体提示词（pf_intake / pf_delivery）
│   ├── main_agent_prompt_platform.md  # 平台编排者
│   ├── project/
│   │   ├── client-portal/    # Vue 3 客户端前端
│   │   └── platform-api/     # Spring Boot 项目 API
│   └── outputs/        # 项目产出（按 projectId 分目录）
│
├── architecture/       # Phase 0: 技术架构设计（6 个Agent）
│   ├── agents/         # 子智能体提示词
│   └── outputs/        # 各 Agent 产出文档（按 agent 名分目录）
│
├── backend/            # Phase 1b: 后端 API 开发（5 个Agent）
│   ├── agents/         # 子智能体提示词
│   ├── outputs/        # 各 Agent 产出（计划/测试报告/经验）
│   └── project/        # Spring Boot 项目代码
│
├── frontend/           # Phase 1a: Vue 3 前端开发（5 个Agent）
│   ├── agents/         # 子智能体提示词
│   ├── outputs/        # 各 Agent 产出
│   └── project/        # Vue 项目代码
│
├── fullstack/          # Phase 2: 前后端联调（5 个Agent）
│   ├── agents/         # 子智能体提示词
│   ├── outputs/        # 各 Agent 产出
│   └── project/        # 集成项目代码
│
├── deploy/             # Phase 3: 生产部署上线（3 个Agent）
│   ├── agents/         # 子智能体提示词
│   ├── outputs/        # 各 Agent 产出
│   └── project/        # 部署配置文件
│
├── flutter/            # Phase 1c: Flutter 跨端开发（5 个Agent）
│   ├── agents/         # 子智能体提示词
│   ├── outputs/        # 各 Agent 产出
│   └── project/        # Flutter 项目代码
│
├── blockchain/         # Phase 1d: FISCO BCOS 智能合约（5 个Agent）
│   ├── agents/         # 子智能体提示词
│   ├── outputs/        # 各 Agent 产出
│   └── project/        # 合约项目代码
│
├── docs/               # 详细文档
└── .github/workflows/  # CI 验证配置
```

## 外包平台使用（客户端 + 运营者）

### 启动服务

```bash
# 终端 1：后端 API（端口 8080，H2 内存数据库，无需安装 PostgreSQL）
cd platform/project/platform-api
mvn spring-boot:run

# 终端 2：前端界面（端口 5173，自动代理 API 请求到 8080）
cd platform/project/client-portal
npm install
npm run dev
```

打开浏览器访问 `http://localhost:5173/`

### 客户端操作（非技术用户）

| 步骤 | 操作 | 说明 |
|------|------|------|
| 1. 注册 | 点击右上角「注册」→ 填邮箱 + 密码 | JWT 自动登录 |
| 2. 提交需求 | 点击「提交需求」→ 用自然语言描述项目 | 例如："做一个个人博客，支持 Markdown 写文章" |
| 3. 确认 PRD | 项目详情页 → 点击「📋 确认需求」 | 查看 AI 生成的结构化需求文档 |
| 4. 追踪进度 | 项目详情页自动轮询（每 5 秒） | 9 个阶段实时进度条 + 时间线日志 |
| 5. 查看交付 | 全部完成后 → 验收报告 | 功能完成表、质量报告、交付物清单 |

### 运营者操作（启动 AI 流水线）

客户提交需求后，项目进入 `intake` 状态。在 Reasonix 中加载平台编排器：

```text
/skill platform/main_agent_prompt_platform.md

PROJECT_ID = <从浏览器地址栏复制的项目 ID>
PLATFORM_ROOT = skillssss/platform
```

编排器自动执行：需求采集 → 架构设计 → 前后端开发 → 联调 → 部署 → 交付验收，全程更新 `project-status.json`，客户端实时可见。

### 权限模型

| 操作 | 认证要求 |
|------|---------|
| 注册 / 登录 | 公开 |
| 查看项目列表 | JWT — 只看自己创建的项目 |
| 提交新需求 | JWT — 自动关联 ownerId |
| 查看他人项目 | 403 Forbidden |

## 使用方式（完整流程）

### 前置条件

1. 支持 Agent/Task/Skill 调用的 AI 编程环境
2. 一份 PRD 文档（`REQUIREMENT_FILE`）
3. 准备好各项目目录（前端、后端、可选 Flutter、可选区块链、部署输出目录）

### 建议先准备一组路径变量

```text
REQUIREMENT_FILE={D:\path\to\prd.md}
ARCH_ROOT={skillssss\architecture\outputs}
FRONTEND_ROOT={skillssss\frontend}
BACKEND_ROOT={skillssss\backend}
FLUTTER_ROOT={skillssss\flutter}              # 可选
BLOCKCHAIN_ROOT={skillssss\blockchain}        # 可选
DEPLOY_ROOT={skillssss\deploy}
```

### Phase 0：架构设计（必须先执行）

使用 `architecture/main_agent_prompt_fs_architect.md`，输入：

```text
REQUIREMENT_FILE
PROJECT_ROOT={ARCH_ROOT}
```

产出（位于 `ARCH_ROOT`）：

```text
architecture-design.md
tech-stack.md
data-architecture.md
infra-architecture.md
security-architecture.md
api-contract.md
ui-ux-architecture.md
implementation-roadmap.md
```

### Phase 1：并行开发（可同时启动）

#### 1) 前端

使用 `frontend/main_agent_prompt_vue.md`：

```text
PROJECT_ROOT={FRONTEND_ROOT}
REQUIREMENT_FILE
TECH_STACK_FILE={ARCH_ROOT}\tech-stack.md
CONTRACT_FILE={ARCH_ROOT}\api-contract.md
SECURITY_FILE={ARCH_ROOT}\security-architecture.md
UI_UX_FILE={ARCH_ROOT}\ui-ux-architecture.md
IMPLEMENTATION_ROADMAP_FILE={ARCH_ROOT}\implementation-roadmap.md
BATCH_SIZE=1
```

#### 2) 后端

使用 `backend/main_agent_prompt.md`：

```text
PROJECT_ROOT={BACKEND_ROOT}
REQUIREMENT_FILE
TECH_STACK_FILE={ARCH_ROOT}\tech-stack.md
DATA_ARCHITECTURE_FILE={ARCH_ROOT}\data-architecture.md
CONTRACT_FILE={ARCH_ROOT}\api-contract.md
SECURITY_FILE={ARCH_ROOT}\security-architecture.md
IMPLEMENTATION_ROADMAP_FILE={ARCH_ROOT}\implementation-roadmap.md
BATCH_SIZE=1
```

#### 3) Flutter（可选）

使用 `flutter/main_agent_prompt_flutter.md`：

```text
PROJECT_ROOT={FLUTTER_ROOT}
REQUIREMENT_FILE
TECH_STACK_FILE={ARCH_ROOT}\tech-stack.md
CONTRACT_FILE={ARCH_ROOT}\api-contract.md
SECURITY_FILE={ARCH_ROOT}\security-architecture.md
UI_UX_FILE={ARCH_ROOT}\ui-ux-architecture.md
IMPLEMENTATION_ROADMAP_FILE={ARCH_ROOT}\implementation-roadmap.md
BATCH_SIZE=1
```

#### 4) 区块链（可选）

使用 `blockchain/main_agent_prompt_blockchain.md`：

```text
PROJECT_ROOT={BLOCKCHAIN_ROOT}
REQUIREMENT_FILE
TECH_STACK_FILE={ARCH_ROOT}\tech-stack.md
DATA_ARCHITECTURE_FILE={ARCH_ROOT}\data-architecture.md
CONTRACT_FILE={ARCH_ROOT}\api-contract.md
SECURITY_FILE={ARCH_ROOT}\security-architecture.md
IMPLEMENTATION_ROADMAP_FILE={ARCH_ROOT}\implementation-roadmap.md
BATCH_SIZE=1
```

### Phase 2：前后端联调（串行）

⚠️ 必须在 `frontend/` 与 `backend/` 完成后执行。

使用 `fullstack/main_agent_prompt_fullstack.md`：

```text
FRONTEND_ROOT
BACKEND_ROOT
FLUTTER_ROOT={可选}
BLOCKCHAIN_ROOT={可选}
UI_UX_FILE={ARCH_ROOT}\ui-ux-architecture.md
CONTRACT_FILE={ARCH_ROOT}\api-contract.md
TECH_STACK_FILE={ARCH_ROOT}\tech-stack.md
DATA_ARCHITECTURE_FILE={ARCH_ROOT}\data-architecture.md
INFRA_FILE={ARCH_ROOT}\infra-architecture.md
SECURITY_FILE={ARCH_ROOT}\security-architecture.md
IMPLEMENTATION_ROADMAP_FILE={ARCH_ROOT}\implementation-roadmap.md
BATCH_SIZE=1
```

### Phase 3：部署上线（最后执行）

使用 `deploy/main_agent_prompt_deploy.md`：

```text
TECH_STACK_FILE={ARCH_ROOT}\tech-stack.md
INFRA_FILE={ARCH_ROOT}\infra-architecture.md
SECURITY_FILE={ARCH_ROOT}\security-architecture.md
IMPLEMENTATION_ROADMAP_FILE={ARCH_ROOT}\implementation-roadmap.md
FRONTEND_ROOT
BACKEND_ROOT
FLUTTER_ROOT={可选}
BLOCKCHAIN_ROOT={可选}
DEPLOY_ROOT
```

### 验证与收尾

1. 查看各域 `main-log.md`，确认任务状态为 ✅
2. 查看 `test-reports/` 与 `lessons-learned.md`，确认测试结论与经验沉淀
3. 本仓库文档维护执行：

```bash
npm ci
npm run lint
```

## 核心设计理念

| 理念 | 说明 |
|------|------|
| **主-从协同** | 主智能体调度，子智能体专注编码/测试，主Agent 不直接修改代码 |
| **文件即记忆** | 所有输出持久化到文件，解决长时上下文丢失 |
| **隔离即规范** | 子Agent 仅接收分发的路径和参数，避免信息污染 |
| **纠错闭环** | 开发 → 三维测试 → 不通过则 resume 修复 → 重测，最多 3 轮 |
| **测试只读** | 测试Agent 只读代码，仅输出 PASS/FAIL 判定报告 |

## 文档索引

- [系统架构详情](docs/architecture.md) — Agent 角色总览、执行顺序、数据流
- [使用手册](docs/workflow.md) — 详细使用流程和参数说明
- [设计原理](docs/design_principles.md) — 方法论、上下文策略、流程设计

## 目标技术栈

| 领域 | 主要技术 |
|------|---------|
| 平台前端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router + Marked（Markdown 渲染） |
| 平台后端 | Java 17 + Spring Boot 3.2 + Spring Security + JWT + H2 |
| 前端（生成） | Vue 3 + TypeScript + Vite + Pinia + Vue Router |
| 后端（生成） | Java / Spring Boot |
| 数据库 | PostgreSQL + Prisma/TypeORM + Redis |
| API 风格 | RESTful |
| 鉴权 | JWT (Access + Refresh Token) |
| 跨平台 | Flutter (iOS/Android/Web/Desktop) |
| 区块链 | FISCO BCOS v3.x + Solidity + Hardhat |
| 部署 | Docker + K8s / Cloud |

## 许可与参考

设计理念参考 B站 @费曼学徒冬瓜 的《Ralph+多智能体协同》视频。

本项目本身是一套 Prompt 模板集合，不是一个可运行的软件。它需要在支持 Agent 调用的 AI 编程环境中作为"技能/提示词"加载使用。

## Tags

- domain: software-engineering
- type: overview
- version: 2.0.0
