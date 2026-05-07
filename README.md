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
| **测试只读** | 测试Agent 只读代码，仅输出 PASS/FAIL 判定报告，无权修改任何源代码。支持 auto-fix 级别的小修改（import 补全、格式修正）可由测试Agent 直接提交 |
| **分级阻断** | 测试Agent 对每个 FAIL 标注严重级别（blocker/major/minor），blocker/major 级别不回退为低质量通过 |
| **可回滚** | 第2轮修正后仍存在 blocker 级问题时，支持 git revert 重置该批次，重启 Agent 从零开始 |
| **结构化判定** | 测试Agent 输出 JSON 格式报告（test-report.json），主Agent 解析 verdict 字段替代 Grep 文本匹配，消除格式漂移导致的误判 |
| **Agent 注册表** | 子Agent 完成后将 ID 写入 agent-registry.json（键值索引），替代文件系统时间戳扫描，避免并发Agent ID 混淆 |
| **成本可观测** | 每批/每Phase 完成后记录 Agent 调用次数和 token 消耗，修正轮次越高说明 prompt 或 PRD 质量存在问题 |
| **增量开发** | Planner Agent 自动检测已有代码结构并产出 existing-architecture-analysis.md，支持在现有项目上进行功能迭代 |
| **真实测试环境** | be-planner 自动搭建 Docker Compose 测试环境（PostgreSQL + Redis + 种子数据），性能/安全Agent 连接真实数据库验证 |

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
│   ├── main-agent-prompt-fs-architect.md     # 架构设计主编排者
│   └── agents/
│       ├── fa-techstack.md               # 技术栈评估（框架/语言/中间件）
│       ├── fa-data.md                    # 数据架构设计（数据库/缓存/存储）
│       ├── fa-infra.md                   # 基础设施架构（部署/CI-CD/网关）
│       ├── fa-security.md                # 安全架构设计（认证/鉴权/加密）
│       └── fa-api-design.md             # API 契约设计（端点/数据结构/错误码）
│
├── backend/                         # Phase 1b: 后端 API 开发
│   ├── main-agent-prompt.md                  # 后端主编排者
│   └── agents/
│       ├── be-planner.md                 # 基础设施与规划
│       ├── be-api-dev.md                 # API 接口开发
│       ├── be-tester-functional.md       # 功能测试
│       ├── be-tester-performance.md      # 性能测试
│       └── be-tester-security.md         # 安全测试
│
├── frontend/                        # Phase 1a: Vue 3 前端开发
│   ├── main-agent-prompt-vue.md              # 前端主编排者
│   └── agents/
│       ├── dg-vue-planner.md             # 规划与脚手架搭建
│       ├── dg-frontend-vue-dev.md        # Vue 组件/Store/路由开发
│       ├── dg-vue-tester-component.md    # 组件结构测试
│       ├── dg-vue-tester-logic.md        # 业务逻辑与状态测试
│       └── dg-vue-tester-style.md        # 样式与 UX 测试
│
├── fullstack/                       # Phase 2: 前后端联调（⚠️ 在 frontend+backend 之后）
│   ├── main-agent-prompt-fullstack.md         # 联调主编排者
│   └── agents/
│       ├── fs-planner.md                 # 集成规划与基础设施
│       ├── fs-api-dev.md                 # 接口对接开发（双向操作前后端代码）
│       ├── fs-tester-contract.md         # 契约一致性测试
│       ├── fs-tester-dataflow.md         # 数据流完整性测试
│       └── fs-tester-integration.md      # 端到端集成测试
│
├── flutter/                          # Phase 1c: Flutter 跨平台开发
│   ├── main-agent-prompt-flutter.md            # Flutter 主编排者
│   └── agents/
│       ├── dg-flutter-planner.md           # 规划与工程化配置
│       ├── dg-flutter-dev.md               # 跨平台 Widget 开发
│       ├── dg-flutter-tester-crossplatform.md  # 跨端兼容测试
│       ├── dg-flutter-tester-logic.md      # 逻辑测试
│       └── dg-flutter-tester-style.md      # 样式测试
│
├── README.md                        # 本文件
└── notes-multi-agent-collaboration-design.md
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
使用 /frontend/main-agent-prompt-vue.md
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
使用 /backend/main-agent-prompt.md
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
使用 /flutter/main-agent-prompt-flutter.md
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
使用 /fullstack/main-agent-prompt-fullstack.md
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

## 版本控制与协作策略

多 Agent 并行读写同一代码库，必须建立 Git 协作规范以避免代码覆盖和冲突。

### 分支策略

| 规则 | 说明 |
|------|------|
| **Agent 独立分支** | 每个开发 Agent 启动时从主分支切出专用分支，命名规则：`agent/{agent-name}/{模块名}`（如 `agent/be-api-dev/user-auth`） |
| **主智能体维护发布分支** | 阶段开始时创建 `phase/{N}-integration` 分支，Agent 分支完成测试后合并到该分支 |
| **按模块隔离** | BATCH_SIZE > 1 时，不同模块分配到不同 Agent 分支，减少并发写入同一文件的概率 |
| **提交粒度约束** | 每个 Agent 完成一个函数/方法后立即 `git commit`，commit message 包含模块名和 Agent ID |

### 冲突解决流程

```
Agent 开发完成
     ↓
向发布分支提 PR（阶段控制器自动检测）
     ↓
┌─ 无冲突 → 自动合并，继续下一步
│
└─ 有冲突 → 主智能体启动 fs-planner 分析冲突范围
                ↓
         判断冲突类型：
         ├─ 同一文件不同函数 → 合并双方修改，保留完整文件
         ├─ 同一函数被修改 → 保留后完成的版本，记录冲突点到 conflict-log.md
         └─ 结构性冲突（如路由注册/import 顺序）→ fs-api-dev 手动整合
                ↓
         冲突解决后重新运行受影响的测试 Agent
```

### 串行修改的安全性

即使是串行修改（一个文件先被 Agent A 改，再被 Agent B 改），Agent B 并不知道之前的 Git 历史。为此：

| 保护措施 | 说明 |
|---------|------|
| **修改前 `git pull`** | 每个 Agent 开始修改文件前必须先拉取最新代码 |
| **逐函数提交** | 不允许批量修改后一次性提交，每个函数独立 commit |
| **文件锁定声明** | Agent 在 `main-log.md` 中声明"正在修改 {文件路径}"，后续 Agent 读取日志发现冲突时应暂停并报告主智能体 |
| **Git diff 审查** | 主智能体在 Agent 完成后执行 `git diff` 检查变更范围，确认未覆盖其他 Agent 的工作 |

---

## 设计改进（v2）

以下改进基于 v1 版本的已知局限：

### 改进项对照

| v1 问题 | v2 解决方案 | 影响文件 |
|---------|-----------|---------|
| Agent ID 收集依赖文件系统时间戳，并发Agent可能拿错ID | 子Agent写入 `agent-registry.json`（键值索引），主Agent按 key 精确提取 | 5 个主Agent提示词 |
| PASS/FAIL 判定依赖 Grep 文本匹配，格式漂移导致误判 | 测试Agent 输出结构化 `test-report.json`，主Agent 解析 `verdict` 字段 | 5 个主Agent + 12 个测试Agent |
| 3轮重试无差异对待，严重问题被静默降级 | 引入 severity 三级（blocker/major/minor），blocker/major 禁止降级通过 | 12 个测试Agent + 5 个主Agent |
| 错误修改无回滚路径，Agent 可能在错误基础上叠加错误 | 第2轮修正后提供 git revert + 重启Agent 选项 | 5 个主Agent |
| 无成本追踪，用户不知道跑了多少Agent/花了多少 token | 每批/每Phase 记录 Agent 调用次数，修正轮次高 → 提示 prompt/PRD 质量问题 | 5 个主Agent |
| 仅支持新项目，无法在现有代码库上做增量开发 | Planner Agent 自动分析现有代码结构，产出 `existing-architecture-analysis.md` | 4 个 Planner Agent |
| 性能/安全测试无真实数据环境，纸上推理不可信 | be-planner 自动搭建 Docker Compose 测试环境 + 种子数据 | be-planner.md |

---

## CI/CD 流水线集成

项目中的测试 Agent（功能测试、性能测试、安全测试等）侧重**语义层面**的验证（API 契约一致性、数据流完整性、业务逻辑正确性），不能替代 CI 流水线中的**自动化检查**（lint、类型检查、单元测试、构建验证）。两者互补。

### Agent 测试 vs CI 流水线

| 维度 | Agent 测试 | CI 流水线 |
|------|-----------|----------|
| **执行者** | AI Agent（大模型推理） | 自动化脚本（确定性执行） |
| **覆盖范围** | 跨文件/跨模块语义验证、契约检查、安全审计 | 代码规范、类型安全、单元测试、构建产物 |
| **触发时机** | Agent 开发完成，测试 Agent 被调度后 | Git push / PR 创建时自动触发 |
| **失败处理** | 输出报告 → resume 开发 Agent 修复 | 阻断合并，要求修复后重新提交 |

### 推荐 CI 配置

每个开发模块的项目根目录应包含 CI 配置文件，Agent 提交代码后 CI 自动运行：

**前端 (`frontend/`) — GitHub Actions 示例**：
```yaml
# .github/workflows/frontend-ci.yml
name: Frontend CI
on:
  pull_request:
    branches: [phase/*]
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci
      - run: npm run lint          # ESLint
      - run: npm run typecheck     # vue-tsc
      - run: npm run test:unit     # Vitest 单元测试
      - run: npm run build         # Vite 构建验证
```

**后端 (`backend/`) — GitHub Actions 示例**：
```yaml
# .github/workflows/backend-ci.yml
name: Backend CI
on:
  pull_request:
    branches: [phase/*]
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci
      - run: npm run lint          # ESLint
      - run: npm run typecheck     # tsc --noEmit
      - run: npm run test          # Jest/Vitest 单元测试
      - run: npm run build         # 编译验证
```

### 工作流整合

```
Agent 提交代码到 agent/{name}/{module} 分支
    ↓
向 phase/{N}-integration 提 PR
    ↓
┌────────────────────────────────────────┐
│  CI 自动运行（lint → typecheck →       │
│  unit test → build）                    │
│                                          │
│  ✅ 通过 → PR 可合并                     │
│  ❌ 失败 → 阻断合并，Agent 必须修复      │
└────────────────────────────────────────┘
    ↓
CI 通过后，主智能体调度测试 Agent（功能/性能/安全）
    ↓
Agent 测试通过 → 合并到发布分支
```

### 开发 Agent 的责任

- 提交代码后等待 CI 结果，CI 失败时读取失败日志定位问题并修复
- 编写代码时需确保能通过 `npm run lint`（无语法/风格错误）和 `npm run typecheck`（类型安全）
- CI 通过的前提是代码可以成功构建，因此开发 Agent 必须在提交前确保 `npm run build` 通过

---

## 💖 特别感谢

这个项目的核心设计理念，完全是跟着B站大佬 @[费曼学徒冬瓜] 的视频一步步学来的！
视频《Ralph+多智能体协同，让AI长时高品质工作，从原理到实践》帮我打通了全流程，强烈推荐！
👉 传送门：https://www.bilibili.com/video/BV1t9oZBDENp/?share_source=copy_web&vd_source=9f3feb8a6c288c6171bc73f3a8a833b1
