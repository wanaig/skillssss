# Harness Engineering — 多智能体协同开发系统

基于 Claude Code 的**多智能体协同开发框架**，通过 **主智能体编排 + 专业化子智能体协作**，实现从需求到代码的全流程自动化研发。

## 核心设计理念

- **主-从协同架构**：主智能体负责任务分解与调度，子智能体专注单一领域的编码/测试
- **文件即记忆**：所有智能体输出持久化到文件，解决长时工作上下文丢失问题
- **隔离即规范**：子智能体仅接收主智能体分发的信息，避免上下文污染
- **日志即保障**：主-从智能体均记录完整日志，实现全链路可追溯
- **纠错循环**：测试不通过 → 恢复开发智能体修复 → 重新测试（最多 5 轮）

## 系统架构

```
                    ┌─────────────────────────┐
                    │   architecture/         │
                    │   技术架构设计 (第一阶段) │
                    └───────────┬─────────────┘
                                │ 产出 architecture-design.md
          ┌─────────┬───────────┼───────────┬─────────┐
          ▼         ▼           ▼           ▼         ▼
  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
  │ backend/ │ │ frontend/│ │fullstack/│ │ uniapp/  │
  │ 后端开发 │ │ Vue前端  │ │ 前后联调  │ │ 跨平台   │
  └──────────┘ └──────────┘ └──────────┘ └──────────┘
```

## 目录结构

```
skillssss/
├── architecture/            # 技术架构设计
│   ├── 主智能体提示词-fs-architect.md
│   └── agents/
│       ├── fa-techstack.md          # 技术栈评估
│       ├── fa-data.md               # 数据架构设计
│       ├── fa-infra.md              # 基础设施架构
│       ├── fa-security.md           # 安全架构设计
│       └── fa-api-design.md         # API 契约设计
├── backend/                 # 后端 API 开发
│   ├── 主智能体提示词.md
│   └── agents/
│       ├── be-planner.md            # 基础设施与规划
│       ├── be-api-dev.md            # API 开发
│       ├── be-tester-functional.md  # 功能测试
│       ├── be-tester-performance.md # 性能测试
│       └── be-tester-security.md    # 安全测试
├── frontend/                # Vue 3 前端开发
│   ├── 主智能体提示词-Vue.md
│   └── agents/
│       ├── dg-vue-planner.md                # 规划与脚手架
│       ├── dg-frontend-vue-dev.md           # 组件开发
│       ├── dg-vue-tester-component.md       # 组件结构测试
│       ├── dg-vue-tester-logic.md           # 逻辑与状态测试
│       └── dg-vue-tester-style.md           # 样式与视觉测试
├── fullstack/               # 前后端联调
│   ├── 主智能体提示词-前后端联调.md
│   └── agents/
│       ├── fs-planner.md            # 集成规划
│       ├── fs-api-dev.md            # 接口对接开发
│       ├── fs-tester-contract.md    # 契约测试
│       ├── fs-tester-dataflow.md    # 数据流测试
│       └── fs-tester-integration.md # 集成测试
├── uniapp/                  # uni-app 跨平台开发
│   ├── 主智能体提示词-uni-app.md
│   └── agents/
│       ├── dg-uni-app-planner.md            # 规划与工程化
│       ├── dg-uni-app-dev.md                # 跨平台组件开发
│       ├── dg-uni-app-tester-crossplatform.md # 跨平台兼容测试
│       ├── dg-uni-app-tester-logic.md       # 逻辑测试
│       └── dg-uni-app-tester-style.md       # 样式测试
└── 笔记-非最新仅参考-多智能体协同-长时工作设计.md  # 核心设计笔记
```

## 目标技术栈

| 领域 | 主要技术 | 备选方案 |
|------|---------|---------|
| **前端** | Vue 3 + TypeScript + Vite + Pinia + Vue Router | React, Next.js |
| **后端** | Node.js / Express (模块化单体) | NestJS, FastAPI, Spring Boot, Go |
| **数据库** | PostgreSQL + Prisma/TypeORM | MySQL, MongoDB |
| **缓存** | Redis | — |
| **API 风格** | RESTful | GraphQL, tRPC |
| **鉴权** | JWT (Access + Refresh Token) | OAuth2, Session |
| **部署** | Docker + K8s / Cloud | — |
| **CI/CD** | GitHub Actions / GitLab CI | — |
| **跨平台** | uni-app (H5/微信小程序/iOS/Android) | — |

## 工作流程

每个领域遵循统一的三阶段流水线：

### 阶段一：规划 (Planning)
规划智能体读取需求文档，产出 `dev-plan.md`、`design-guide.md`，并搭建项目脚手架。

### 阶段二：批量开发-测试循环
1. **开发** — 开发智能体按批次实现模块
2. **并行测试** — 3 个测试智能体从不同维度同时测试
3. **纠错** — 测试不通过时恢复开发智能体修复（最多 3 轮）
4. **更新计划** — 记录进度和问题总结

### 阶段三：收尾
- 汇总统计（总任务数、通过率、修复轮次）
- 输出 `lessons-learned.md`（跨智能体知识积累）

## 使用方式

### 前置条件
- Claude Code 环境（支持 Agent 功能）
- `~/.claude/` 目录结构就绪

### 开始一个项目

1. **架构设计（必须最先执行）**
   ```
   使用 fs-architect 主智能体，提供 PRD 文档路径
   ```

2. **领域开发（可并行）**
   根据架构设计文档，按需启动各领域主智能体：
   - `/backend/主智能体提示词.md` — 启动后端开发
   - `/frontend/主智能体提示词-Vue.md` — 启动前端开发
   - `/fullstack/主智能体提示词-前后端联调.md` — 启动前后端联调
   - `/uniapp/主智能体提示词-uni-app.md` — 启动跨平台开发

3. **确认参数**
   与主智能体确认项目目录、批次大小 (`BATCH_SIZE`) 等配置后，主智能体将自动编排全流程。

### 智能体 Recovery 机制

子智能体会将 ID 持久化到 `~/.claude/projects/.../agent-*.meta.json`，纠错循环时主智能体通过 `resume` 复用同一智能体会话，保持上下文连续性。

## 设计说明

- **测试智能体只读**：所有测试智能体设置为只读模式，仅输出 PASS/FAIL 判定，无权修改代码。
- **语义化文件命名**：全项目使用固定规则命名的 markdown 文件（如 `dev-plan.md`）作为沟通载体，主智能体通过文件名了解执行状态。
- **精简上下文**：主智能体从不读取子智能体输出全文，仅通过 `Grep` 提取关键判定结果。
