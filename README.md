# Harness Engineering — 多智能体协同开发系统

基于 Claude Code 的**多智能体协同开发框架**，通过 **1 个架构设计模块 + 4 个领域开发模块** 共 **24 个子智能体** 协作，实现从 PRD 需求文档到全栈代码的全流程自动化研发。

---

## 核心设计理念

| 理念 | 说明 |
|------|------|
| **主-从协同架构** | 主智能体负责任务分解与调度，子智能体专注单一领域的编码/测试，主智能体绝不直接修改代码 |
| **文件即记忆** | 所有智能体输出持久化到 markdown 文件（dev-plan.md / design-guide.md / lessons-learned.md），解决长时工作上下文丢失问题 |
| **隔离即规范** | 子智能体仅接收主智能体分发的路径和参数，不读取完整上下文，避免信息污染 |
| **日志即保障** | 主智能体记录 main-log.md，时间精确到分钟（yymmdd hhmm），实现全链路可追溯 |
| **纠错闭环** | 开发 → 三维测试 → 不通过则 resume 同一开发Agent修复 → 重测，最多 3 轮 |
| **测试只读** | 所有测试Agent设置为只读模式，仅输出 PASS/FAIL 判定报告，无权修改任何源代码 |

---

## 系统全局架构

```
                        ┌──────────────────────────────────────┐
                        │         Phase 0: architecture/       │
                        │         技术架构设计（必须最先执行）    │
                        │                                       │
                        │  5 个子Agent 并行分析 5 个维度：        │
                        │  techstack · data · infra             │
                        │  security · api-design                │
                        │                                       │
                        │  产出：architecture-design.md          │
                        │       + 5 份维度分析文档               │
                        │       + implementation-roadmap.md     │
                        └──────────────┬───────────────────────┘
                                       │
          ┌────────────────────────────┼────────────────────────────┐
          │                            │                            │
          ▼                            ▼                            ▼
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Phase 1a        │     │  Phase 1b        │     │  Phase 1c        │
│  frontend/       │     │  backend/        │     │  flutter/        │
│  Vue 3 前端开发   │     │  后端 API 开发    │     │  Flutter 跨端    │
│                  │     │                  │     │                  │
│  5 个Agent:       │     │  5 个Agent:       │     │  5 个Agent:       │
│  planner · dev   │     │  planner · dev   │     │  planner · dev   │
│  + 3 testers     │     │  + 3 testers     │     │  + 3 testers     │
└────────┬─────────┘     └────────┬─────────┘     └──────────────────┘
         │                        │
         │    前端页面 + 组件      │    后端 API 接口
         └───────────┬────────────┘
                     │ 两端代码均已产出
                     ▼
          ┌──────────────────┐
          │  Phase 2         │
          │  fullstack/      │
          │  前后端联调       │  ← ⚠️ 必须在 frontend/ 和 backend/ 之后
          │                  │
          │  5 个Agent:       │
          │  planner · dev   │
          │  + 3 testers     │
          │  (contract       │
          │   dataflow       │
          │   integration)   │
          └──────────────────┘
```

---

## 目录结构

```
skillssss/
│
├── architecture/                    # Phase 0: 技术架构设计（最先执行）
│   ├── 主智能体提示词-fs-architect.md     # 架构设计主编排者
│   └── agents/
│       ├── fa-techstack.md               # 技术栈评估（框架/语言/中间件）
│       ├── fa-data.md                    # 数据架构设计（数据库/缓存/存储）
│       ├── fa-infra.md                   # 基础设施架构（部署/CI-CD/网关）
│       ├── fa-security.md                # 安全架构设计（认证/鉴权/加密）
│       └── fa-api-design.md             # API 契约设计（端点/数据结构/错误码）
│
├── backend/                         # Phase 1b: 后端 API 开发
│   ├── 主智能体提示词.md                  # 后端主编排者
│   └── agents/
│       ├── be-planner.md                 # 基础设施与规划
│       ├── be-api-dev.md                 # API 接口开发
│       ├── be-tester-functional.md       # 功能测试
│       ├── be-tester-performance.md      # 性能测试
│       └── be-tester-security.md         # 安全测试
│
├── frontend/                        # Phase 1a: Vue 3 前端开发
│   ├── 主智能体提示词-Vue.md              # 前端主编排者
│   └── agents/
│       ├── dg-vue-planner.md             # 规划与脚手架搭建
│       ├── dg-frontend-vue-dev.md        # Vue 组件/Store/路由开发
│       ├── dg-vue-tester-component.md    # 组件结构测试
│       ├── dg-vue-tester-logic.md        # 业务逻辑与状态测试
│       └── dg-vue-tester-style.md        # 样式与 UX 测试
│
├── fullstack/                       # Phase 2: 前后端联调（⚠️ 在 frontend+backend 之后）
│   ├── 主智能体提示词-前后端联调.md         # 联调主编排者
│   └── agents/
│       ├── fs-planner.md                 # 集成规划与基础设施
│       ├── fs-api-dev.md                 # 接口对接开发（双向操作前后端代码）
│       ├── fs-tester-contract.md         # 契约一致性测试
│       ├── fs-tester-dataflow.md         # 数据流完整性测试
│       └── fs-tester-integration.md      # 端到端集成测试
│
├── flutter/                          # Phase 1c: Flutter 跨平台开发
│   ├── 主智能体提示词-Flutter.md            # Flutter 主编排者
│   └── agents/
│       ├── dg-flutter-planner.md           # 规划与工程化配置
│       ├── dg-flutter-dev.md               # 跨平台 Widget 开发
│       ├── dg-flutter-tester-crossplatform.md  # 跨端兼容测试
│       ├── dg-flutter-tester-logic.md      # 逻辑测试
│       └── dg-flutter-tester-style.md      # 样式测试
│
├── README.md                        # 本文件
└── 笔记-非最新仅参考-多智能体协同-长时工作设计.md
```

---

## 执行顺序与依赖关系

```
Phase 0           Phase 1 (并行)        Phase 2 (串行)
───────────      ──────────────        ─────────────

                   ┌─ frontend/ ─┐
                   │  Vue 前端    │────┐
                   └─────────────┘    │
                                      ├──▶ fullstack/
architecture/ ──▶  ┌─ backend/  ─┐    │    前后端联调
  架构设计          │  后端 API   │────┘
                   └─────────────┘
                   ┌─ flutter/ ──┘
                    │  Flutter 跨端  (独立并行)
                    └─────────────┘
```

| 阶段 | 模块 | 依赖 | 可并行 | 说明 |
|------|------|------|--------|------|
| **Phase 0** | `architecture/` | 仅需 PRD 文档 | — | 必须最先执行，产出设计基线 |
| **Phase 1a** | `frontend/` | `architecture/` 产出 | 可与 1b, 1c 并行 | Vue 3 页面和组件开发 |
| **Phase 1b** | `backend/` | `architecture/` 产出 | 可与 1a, 1c 并行 | REST API 接口开发 |
| **Phase 1c** | `flutter/` | `architecture/` 产出 | 可与 1a, 1b 并行 | Flutter 跨平台应用开发（独立） |
| **Phase 2** | `fullstack/` | `frontend/` **且** `backend/` 均完成 | **不可并行** | 需要两端代码均已存在 |

### 为什么 fullstack/ 必须在之后？

`fs-api-dev` 的职责是创建桥接层（前端 API 调用模块、共享类型定义、数据转换），以及修复两端独立开发产生的不一致——而非从零开发新功能。没有前后端代码，联调无从谈起。

---

## 智能体角色总览（24 个）

### architecture/ — 5 个 Agent

| Agent | 角色 | 产出 |
|-------|------|------|
| `fa-techstack` | 技术栈评估 | `tech-stack.md` |
| `fa-data` | 数据架构设计 | `data-architecture.md` |
| `fa-infra` | 基础设施架构 | `infra-architecture.md` |
| `fa-security` | 安全架构设计 | `security-architecture.md` |
| `fa-api-design` | API 契约设计 | `api-contract-outline.md` |
| *(主Agent)* | 编排+一致性检查+整合 | `architecture-design.md` + `implementation-roadmap.md` |

### backend/ — 5 个 Agent

| Agent | 角色 | 测试维度 |
|-------|------|---------|
| `be-planner` | 项目规划与脚手架 | — |
| `be-api-dev` | 接口开发 | — |
| `be-tester-functional` | 功能测试 | 业务逻辑正确性 |
| `be-tester-performance` | 性能测试 | 响应时间/并发/索引 |
| `be-tester-security` | 安全测试 | 注入/鉴权/数据保护 |

### frontend/ — 5 个 Agent

| Agent | 角色 | 测试维度 |
|-------|------|---------|
| `dg-vue-planner` | 项目规划与脚手架 | — |
| `dg-frontend-vue-dev` | 组件开发 | — |
| `dg-vue-tester-component` | 组件结构测试 | Props/Emits/生命周期/组件树 |
| `dg-vue-tester-logic` | 逻辑测试 | 响应式/Store/异步/类型安全 |
| `dg-vue-tester-style` | 样式测试 | CSS作用域/响应式/无障碍/交互态 |

### fullstack/ — 5 个 Agent

| Agent | 角色 | 测试维度 |
|-------|------|---------|
| `fs-planner` | 集成规划与基础设施 | — |
| `fs-api-dev` | 接口对接开发 | — |
| `fs-tester-contract` | 契约测试 | 类型定义/字段名/请求响应结构一致性 |
| `fs-tester-dataflow` | 数据流测试 | loading/error/data三态/状态链路 |
| `fs-tester-integration` | 集成测试 | CORS/鉴权流通/路由挂载/错误穿透 |

### flutter/ — 5 个 Agent

| Agent | 角色 | 测试维度 |
|-------|------|---------|
| `dg-flutter-planner` | 项目规划与工程化 | — |
| `dg-flutter-dev` | 跨平台 Widget 开发 | — |
| `dg-flutter-tester-crossplatform` | 跨端兼容测试 | Platform API / 自适应 / Web / 桌面 |
| `dg-flutter-tester-logic` | 逻辑测试 | Riverpod / 异步 / 数据流 |
| `dg-flutter-tester-style` | 样式测试 | Material 3 / 响应式 / 无障碍 |

---

## 各领域内部工作流程

每个领域遵循统一的 **3 阶段流水线**：

```
Phase 1: 规划        Phase 2: 批量开发-测试循环          Phase 3: 收尾
─────────────      ─────────────────────────────      ────────────

Planner读取PRD     ┌─── 开发Agent实现模块 ───┐        汇总统计
  ↓                │                          │        输出 lessons-learned.md
产出 dev-plan.md   ├─── 3个测试Agent并行 ────┤
  ↓                │   (各测不同维度)          │
产出 design-guide  │          ↓               │
  ↓                ├─── 任一FAIL? ──── YES ──▶ resume开发Agent修复
搭建项目脚手架      │          ↓ NO            │         ↓
                    │   标记PASS，继续下批      │    resume测试Agent重测
                    └──────────────────────────┘         ↓
                                                   最多3轮纠错

BATCH_SIZE 默认=1，用户可指定一次开发N个模块（开发批量=测试批量）
测试始终3个Agent并行，开发每批只启动1个Agent
```

### 关键机制

| 机制 | 说明 |
|------|------|
| **Agent Resume** | 修正时 resume 同一 Agent（复用 ID），保持上下文连续，不另起新 Agent |
| **Agent ID 收集** | 子Agent 完成后 ID 写入 `~/.claude/projects/.../agent-*.meta.json`，主Agent 探测提取 |
| **纠错循环** | 最多 3 轮，第 3 轮仍 FAIL → 标记 ⚠️ "低质量通过"，不阻塞整体进度 |
| **上下文保护** | 主Agent 不读子Agent 产出内容，仅用 Grep 提取 `### 判定：PASS/FAIL` |
| **文件即状态** | `dev-plan.md` / `integration-plan.md` 中的 ⏳/✅/⚠️ 标记是整个系统的进度来源 |
| **经验积累** | `lessons-learned.md` 由开发Agent 在每次修正后更新，遵循"原则性 > 数值性、模式级 > 页面级、可迁移 > 可复制" |

---

## 使用方式

### 前置条件

- Claude Code 环境（支持 Agent / Task 功能）
- `~/.claude/` 目录结构就绪

### 标准流程

#### 第 1 步：架构设计（必须最先执行）

将以下提示词发送给 Claude Code，加载 architecture 主智能体：

```
使用 fs-architect 主智能体，PRD 路径: {你的需求文档路径}
输出目录: {架构文档输出目录}
```

架构主智能体会：
1. 收集项目约束信息（团队技能、规模、合规等）
2. 并行启动 5 个维度分析Agent
3. 进行一致性检查和需求覆盖度检查
4. 产出 `architecture-design.md` + 5 份维度文档 + `implementation-roadmap.md`

#### 第 2 步：前后端并行开发

架构设计完成后，可同时启动前端和后端开发：

**启动前端：**
```
使用 /frontend/主智能体提示词-Vue.md
PROJECT_ROOT: {前端项目路径}
REQUIREMENT_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
CONTRACT_FILE: {api-contract-outline.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
BATCH_SIZE: 1 (或指定 N)
```

**启动后端：**
```
使用 /backend/主智能体提示词.md
OUTPUT_DIR: {后端项目路径}
REQUIREMENTS_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
DATA_ARCHITECTURE_FILE: {data-architecture.md 路径}
CONTRACT_FILE: {api-contract-outline.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
BATCH_SIZE: 1 (或指定 N)
```

**如需跨平台应用（Flutter）：**
```
使用 /flutter/主智能体提示词-Flutter.md
PROJECT_ROOT: {Flutter 项目路径}
REQUIREMENT_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
CONTRACT_FILE: {api-contract-outline.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
```

#### 第 3 步：前后端联调

⚠️ **必须等 frontend/ 和 backend/ 全部完成后再执行此步骤。**

```
使用 /fullstack/主智能体提示词-前后端联调.md
FRONTEND_ROOT: {前端项目路径}
BACKEND_ROOT: {后端项目路径}
CONTRACT_FILE: {architecture 产出的 api-contract-outline.md 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
DATA_ARCHITECTURE_FILE: {data-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
BATCH_SIZE: 1 (或指定 N)
```

联调主智能体会：
1. 读取 API 契约文档 + 扫描两端代码现状
2. 创建前端 API 调用层（`src/api/`、`src/types/api.ts`、`vite.config.ts` 代理）
3. 逐模块对接接口：前端类型定义 ↔ 后端响应格式
4. 三维测试：契约一致性 / 数据流完整性 / 端到端集成
5. 修正循环确保两端完全对齐

#### 第 4 步：验证收尾

所有领域完成后，检查各领域的 `main-log.md` 确认：
- 全部模块标记 ✅
- 迭代统计汇总
- `lessons-learned.md` 记录的经验可复用到下个项目

---

## 数据流与接口约定

### 模块间传递的关键变量

| 变量名 | 含义 | 来源 | 消费者 |
|--------|------|------|--------|
| `REQUIREMENT_FILE` / `REQUIREMENTS_FILE` | PRD 需求文档路径 | 用户提供 | architecture/ → frontend/ / backend/ / flutter/ |
| `TECH_STACK_FILE` | 技术栈文档路径 | architecture/ 产出 `tech-stack.md` | frontend/ / backend/ / fullstack/ / flutter/ |
| `DATA_ARCHITECTURE_FILE` | 数据架构文档路径 | architecture/ 产出 `data-architecture.md` | backend/ / fullstack/ |
| `CONTRACT_FILE` | API 契约文档路径 | architecture/ 产出 `api-contract-outline.md` | backend/ / frontend/ / fullstack/ / flutter/ |
| `IMPLEMENTATION_ROADMAP_FILE` | 实施路线图路径 | architecture/ 产出 `implementation-roadmap.md` | backend/ / frontend/ / fullstack/ / flutter/ |
| `SECURITY_FILE` | 安全架构文档路径 | architecture/ 产出 `security-architecture.md` | backend/ / frontend/ / flutter/ |
| `FRONTEND_ROOT` | 前端项目根目录 | 用户提供/architecture 指定 | frontend/ / fullstack/ |
| `BACKEND_ROOT` | 后端项目根目录 | 用户提供/architecture 指定 | backend/ / fullstack/ |
| `OUTPUT_DIR` | 输出目录 | 架构设计阶段 | architecture/ / backend/ |
| `BATCH_SIZE` | 批量开发大小 | 用户指定（默认 1） | 全部 4 个领域 |

### architecture/ → 各领域的交接

| 下游模块 | 需要的输入 | 依赖的参考文件 |
|---------|-----------|---------------|
| **frontend/** | `REQUIREMENT_FILE`, `PROJECT_ROOT` | `tech-stack.md`, `api-contract-outline.md`, `security-architecture.md`, `implementation-roadmap.md` |
| **backend/** | `REQUIREMENTS_FILE`, `OUTPUT_DIR` | `tech-stack.md`, `data-architecture.md`, `api-contract-outline.md`, `security-architecture.md`, `implementation-roadmap.md` |
| **fullstack/** | `FRONTEND_ROOT`, `BACKEND_ROOT`, `CONTRACT_FILE` | `tech-stack.md`, `data-architecture.md`, `implementation-roadmap.md` |
| **flutter/** | `REQUIREMENT_FILE`, `PROJECT_ROOT` | `tech-stack.md`, `api-contract-outline.md`, `security-architecture.md`, `implementation-roadmap.md` |

---

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
| **跨平台** | Flutter (iOS/Android/Web/Desktop) | React Native |

---

## 设计说明

- **测试智能体只读**：全部 12 个测试Agent 设定为只读模式，仅输出测试报告到 `test-reports/` 或 `fullstack-test-reports/`，无权修改任何源代码。
- **语义化文件命名**：全系统统一使用 `dev-plan.md`、`design-guide.md`、`integration-plan.md`、`lessons-learned.md` 等固定规则命名，主智能体通过文件名判断执行状态。
- **精简上下文**：主智能体从不读取子智能体输出全文，仅通过 `Grep` 提取 `### 判定：PASS/FAIL` 判定结果。
- **架构优先**：`implementation-roadmap.md` 定义了 Phased 实施顺序，各领域启动前应参考其依赖约束。
- **fullstack 定位**：fullstack/ 负责创建**桥接层**（类型定义、请求封装、数据转换、代理配置、CORS 验证），修复前后端独立开发产生的不一致——不创建新的业务功能。

---

## 💖 特别感谢

这个项目的核心设计理念，完全是跟着B站大佬 @[费曼学徒冬瓜] 的视频一步步学来的！
视频《Ralph+多智能体协同，让AI长时高品质工作，从原理到实践》帮我打通了全流程，强烈推荐！
👉 传送门：https://www.bilibili.com/video/BV1t9oZBDENp/?share_source=copy_web&vd_source=9f3feb8a6c288c6171bc73f3a8a833b1
