# Harness Engineering — 多智能体协同开发系统

基于 AI Agent 的**多智能体协同开发框架**，通过 **1 个架构设计模块 + 4 个领域开发模块**共 **24 个子智能体**协作，实现从 PRD 到全栈代码的全流程自动化研发。

## 快速概览

```
architecture/ → backend/ + frontend/ + flutter/ → fullstack/
   (Phase 0)          (Phase 1, 可并行)            (Phase 2, 串行)
```

每个领域内部遵循统一的 **计划 → 批量开发-测试循环 → 收尾** 三阶段流水线。

## 目录结构

```
├── architecture/       # Phase 0: 技术架构设计（5 个Agent）
├── backend/            # Phase 1b: 后端 API 开发（5 个Agent）
├── frontend/           # Phase 1a: Vue 3 前端开发（5 个Agent）
├── fullstack/          # Phase 2: 前后端联调（5 个Agent）
├── flutter/            # Phase 1c: Flutter 跨端开发（5 个Agent）
├── docs/               # 详细文档
└── .github/workflows/  # CI 验证配置
```

## 使用方式

### 前置条件

- 支持 Agent/Task 功能的 AI 编程环境
- 一份 PRD 需求文档

### 标准流程

**第 1 步：架构设计（必须最先执行）**

加载 `architecture/main-agent-prompt-fs-architect.md`，提供 PRD 路径。产出 7 份架构文档。

**第 2 步：前后端并行开发**

架构完成后，可同时启动前端和后端（以及 Flutter）：

```
# 前端
/frontend/main-agent-prompt-vue.md
PROJECT_ROOT + REQUIREMENT_FILE + 架构文档路径

# 后端
/backend/main-agent-prompt.md
OUTPUT_DIR + REQUIREMENTS_FILE + 架构文档路径
```

**第 3 步：前后端联调**

⚠️ 必须等 frontend/ 和 backend/ 完成后执行。

```
/fullstack/main-agent-prompt-fullstack.md
FRONTEND_ROOT + BACKEND_ROOT + CONTRACT_FILE + 架构文档路径
```

**第 4 步：验证收尾**

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
| 后端 | Node.js / Express |
| 数据库 | PostgreSQL + Prisma/TypeORM + Redis |
| API 风格 | RESTful |
| 鉴权 | JWT (Access + Refresh Token) |
| 跨平台 | Flutter (iOS/Android/Web/Desktop) |
| 部署 | Docker + K8s / Cloud |

## 许可与参考

设计理念参考 B站 @费曼学徒冬瓜 的《Ralph+多智能体协同》视频。

本项目本身是一套 Prompt 模板集合，不是一个可运行的软件。它需要在支持 Agent 调用的 AI 编程环境中作为"技能/提示词"加载使用。
