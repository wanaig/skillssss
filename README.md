# Skill: harness_overview

# Harness Engineering — 多智能体协同开发系统

基于 AI Agent 的**多智能体协同开发框架**，通过 **1 个架构设计模块 + 4 个领域开发模块 + 1 个部署模块**共 **27 个子智能体**协作，实现从 PRD 到生产部署的全流程自动化研发。

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
├── architecture/       # Phase 0: 技术架构设计（5 个Agent）
├── backend/            # Phase 1b: 后端 API 开发（5 个Agent）
├── frontend/           # Phase 1a: Vue 3 前端开发（5 个Agent）
├── fullstack/          # Phase 2: 前后端联调（5 个Agent）
├── deploy/             # Phase 3: 生产部署上线（7 个Agent）
├── flutter/            # Phase 1c: Flutter 跨端开发（5 个Agent）
├── blockchain/         # Phase 1d: FISCO BCOS 智能合约（5 个Agent）
├── docs/               # 详细文档
└── .github/workflows/  # CI 验证配置
```

## 使用方式

### 前置条件

- 支持 Agent/Task 功能的 AI 编程环境
- 一份 PRD 需求文档

### 标准流程

**第 1 步：架构设计（必须最先执行）**
加载 `architecture/main-agent-prompt-fs_architect.md`，提供 PRD 路径。产出 7 份架构文档。

**第 2 步：各端并行开发**
架构完成后，可同时启动前端、后端、Flutter 和区块链：

```
# 前端
/frontend/main-agent-prompt-vue.md
PROJECT_ROOT + REQUIREMENT_FILE + 架构文档路径

# 后端
/backend/main-agent-prompt.md
PROJECT_ROOT + REQUIREMENT_FILE + 架构文档路径

# Flutter（可选）
/flutter/main-agent-prompt-flutter.md
PROJECT_ROOT + REQUIREMENT_FILE + 架构文档路径

# 区块链（可选）
/blockchain/main-agent-prompt-blockchain.md
PROJECT_ROOT + REQUIREMENT_FILE + 架构文档路径
```

**第 3 步：前后端联调**

⚠️ 必须在 frontend/ 和 backend/ 完成后执行。

```
/fullstack/main-agent-prompt-fullstack.md
FRONTEND_ROOT + BACKEND_ROOT + CONTRACT_FILE + 架构文档路径
```

**第 4 步：生产部署上线**

联调全部通过后，生成可部署的生产包：

```
/deploy/main-agent-prompt-deploy.md
TECH_STACK_FILE + INFRA_FILE + SECURITY_FILE + 项目路径
```

**第 5 步：验证收尾**

检查各领域的 `main-log.md`，确认全部模块 ✅。

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
- [设计原理](docs/design-principles.md) — 方法论、上下文策略、流程设计

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
