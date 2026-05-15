# Skill: harness_overview

# Harness Engineering — 多智能体协同开发系统

基于 AI Agent 的**多智能体协同开发框架**，通过 **1 个架构设计模块 + 4 个领域开发模块 + 1 个集成模块 + 1 个部署模块**共 **34 个子智能体**协作，实现从 PRD 到生产部署的全流程自动化研发。

## When to Use This Skill

- 需要快速了解项目概览、目录结构和核心设计理念
- 需要了解标准使用流程的顶层视图
- 需要查阅各领域模块的职责和文档索引
- 需要了解目标技术栈和项目定位

## Core Content

## 快速概览

```
architecture/ → backend/ + frontend/ + flutter/ + blockchain/ → fullstack/ → deploy/
   (Phase 0)                  (Phase 1, 可并行)                 (Phase 2)   (Phase 3)
```

每个领域内部遵循统一的 **计划 → 批量开发-测试循环 → 收尾** 三阶段流水线。

## 目录结构

```
├── architecture/       # Phase 0: 技术架构设计（6 个Agent）
├── backend/            # Phase 1b: 后端 API 开发（5 个Agent）
├── frontend/           # Phase 1a: Vue 3 前端开发（5 个Agent）
├── fullstack/          # Phase 2: 前后端联调（5 个Agent）
├── deploy/             # Phase 3: 生产部署上线（3 个Agent）
├── flutter/            # Phase 1c: Flutter 跨端开发（5 个Agent）
├── blockchain/         # Phase 1d: FISCO BCOS 智能合约（5 个Agent）
├── docs/               # 详细文档
└── .github/workflows/  # CI 验证配置
```

## 使用方式（完整流程）

### 前置条件

1. 支持 Agent/Task/Skill 调用的 AI 编程环境
2. 一份 PRD 文档（`REQUIREMENT_FILE`）
3. 准备好各项目目录（前端、后端、可选 Flutter、可选区块链、部署输出目录）

### 建议先准备一组路径变量

```text
REQUIREMENT_FILE={D:\path\to\prd.md}
ARCH_ROOT={D:\path\to\architecture-output}
FRONTEND_ROOT={D:\path\to\frontend}
BACKEND_ROOT={D:\path\to\backend}
FLUTTER_ROOT={D:\path\to\flutter}            # 可选
BLOCKCHAIN_ROOT={D:\path\to\blockchain}      # 可选
DEPLOY_ROOT={D:\path\to\deploy-output}
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
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Vue Router |
| 后端 | Java / Spring Boot |
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
