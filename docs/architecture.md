# Skill: harness_architecture

# 系统架构详情

Harness Engineering 系统的全局架构、执行顺序、依赖关系、34 个 Agent 角色总览、各领域内部工作流程、以及模块间传递的关键接口约定。

## When to Use This Skill

- 需要了解各 Agent 的角色、职责和产出
- 需要理解模块间执行顺序和依赖关系
- 需要查询模块间传递的关键变量和接口约定
- 需要了解各领域内部的三阶段流水线机制

## Core Content

## 全局架构图

                        ┌──────────────────────────────────────┐
                        │         Phase 0: architecture/       │
                        │         技术架构设计（必须最先执行）    │
                        │                                       │
                        │  6 个子Agent 并行分析 6 个维度：        │
                        │  techstack · data · infra             │
                        │  security · api-design                │
                        │                                       │
                        │  产出：architecture-design.md          │
                        │       + 6 份维度分析文档               │
                        │       + implementation-roadmap.md     │
                        └──────────────┬───────────────────────┘
                                       │
    ┌──────────────────┬───────────────┼───────────────┬──────────────────┐
    │                  │               │               │                  │
    ▼                  ▼               ▼               ▼                  ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐
│Phase 1a  │  │Phase 1b  │  │Phase 1c  │  │Phase 1d  │  │Phase 1e      │
│frontend/ │  │backend/  │  │flutter/  │  │blockchain│  │(可并行)       │
│Vue 3前端  │  │后端API   │  │Flutter   │  │FISCO BCOS│  │              │
│          │  │          │  │跨端      │  │智能合约   │  │              │
│5个Agent  │  │5个Agent  │  │5个Agent  │  │5个Agent  │  │              │
│planner   │  │planner   │  │planner   │  │planner   │  │              │
│+ dev     │  │+ dev     │  │+ dev     │  │+ dev     │  │              │
│+ 3testers│  │+ 3testers│  │+ 3testers│  │+ 3testers│  │              │
└────┬─────┘  └────┬─────┘  └──────────┘  └────┬─────┘  └──────────────┘
     │              │                           │
     │  前端页面     │  后端 API 接口             │  合约 ABI
     └──────┬───────┘                           │
            │                                   │
            ▼                                   │
  ┌──────────────────┐                          │
  │  Phase 2         │◄─────────────────────────┘
  │  fullstack/      │
  │  前后端联调       │  ← 必须在 frontend/ 和 backend/ 之后
  │                  │
  │  5 个Agent:       │
  │  planner · dev   │
  │  + 3 testers     │
  │  (contract       │
  │   dataflow       │
  │   integration)   │
  └────────┬─────────┘
           │
           ▼
  ┌──────────────────┐
  │  Phase 3         │
  │  deploy/         │
  │  生产部署上线     │  ← 必须在 fullstack/ 之后
  │                  │
  │  3 个Agent:       │
  │  planner · infra │
  │  · verifier      │
  └──────────────────┘
```

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

| 阶段 | 模块 | 依赖 | 可并行 |
|------|------|------|--------|
| **Phase 0** | `architecture/` | 仅需 PRD | — |
| **Phase 1a** | `frontend/` | architecture 产出 | 可与 1b, 1c, 1d 并行 |
| **Phase 1b** | `backend/` | architecture 产出 | 可与 1a, 1c, 1d 并行 |
| **Phase 1c** | `flutter/` | architecture 产出 | 可与 1a, 1b, 1d 并行 |
| **Phase 1d** | `blockchain/` | architecture 产出 | 可与 1a, 1b, 1c 并行 |
| **Phase 2** | `fullstack/` | frontend **且** backend 完成 | 不可并行 |
| **Phase 3** | `deploy/` | fullstack 完成 | 不可并行 |

## 智能体角色总览（34 个）

### architecture/ — 6 个 Agent

| Agent | 角色 | 产出 |
|-------|------|------|
| `fa-techstack` | 技术栈评估 | `tech-stack.md` |
| `fa-data` | 数据架构设计 | `data-architecture.md` |
| `fa-infra` | 基础设施架构 | `infra-architecture.md` |
| `fa-security` | 安全架构设计 | `security-architecture.md` |
| `fa-api-design` | API 契约设计 | `api-contract.md` |

### backend/ — 5 个 Agent

| Agent | 角色 | 测试维度 |
|-------|------|---------|
| `be-planner` | 项目规划与脚手架 | — |
| `be-api-dev` | 接口开发 | — |
| `be-tester-functional` | 功能测试 | 业务逻辑正确性 |
| `be-tester-performance` | 性能测试 | N+1查询/索引/缓存/超时 |
| `be-tester-security` | 安全测试 | 注入/鉴权/加密/信息泄露 |

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

### blockchain/ — 5 个 Agent

| Agent | 角色 | 测试维度 |
|-------|------|---------|
| `bc-planner` | 合约项目规划与工程化 | — |
| `bc-solidity-dev` | Solidity 智能合约开发 | — |
| `bc-tester-functional` | 功能测试 | 业务逻辑 / 状态转移 / 事件完整性 |
| `bc-tester-security` | 安全测试 | 重入 / 溢出 / 权限 / tx.origin / 签名 |
| `bc-tester-gas` | 燃耗测试 | 存储布局 / 循环优化 / 数据类型 / Gas 估算 |

### deploy/ — 3 个 Agent

## 各领域内部工作流程

每个领域遵循统一的 **3 阶段流水线**：

```
Phase 1: 规划        Phase 2: 批量开发-测试循环          Phase 3: 收尾
─────────────      ─────────────────────────────      ────────────

Planner读取PRD     ┌─── 开发Agent实现模块 ───┐        汇总统计
  ↓                │                          │        输出 lessons-learned.md
 产出 dev-plan.md  ├─── 3个测试Agent并行 ────┤
  ↓                │   (各测不同维度)          │
 产出 design-guide │          ↓               │
  ↓                ├─── 任一FAIL? ──── YES ──▶ resume开发Agent修复
 搭建项目脚手架      │          ↓ NO            │         ↓
                    │   标记PASS，继续下批      │    resume测试Agent重测
                    └──────────────────────────┘         ↓
                                                   最多3轮纠错
```

### 关键机制

| 机制 | 说明 |
|------|------|
| **Agent Resume** | 修正时 resume 同一 Agent（复用 ID），保持上下文连续 |
| **Agent ID 收集** | 子Agent 完成后 ID 写入 `agent-registry/{key}.json` |
| **纠错循环** | 最多 3 轮，第 3 轮仍 FAIL → 标记 ⚠️ |
| **文件即状态** | `dev-plan.md` 中的 ⏳/✅/⚠️ 标记为进度来源 |
| **经验积累** | `lessons-learned.md` 由开发Agent 修正后更新 |

## 数据流与接口约定

### 模块间传递的关键变量

| 变量名 | 含义 | 来源 | 消费者 |
|--------|------|------|--------|
| `REQUIREMENT_FILE` | PRD 路径 | 用户 | 全部模块 |
| `TECH_STACK_FILE` | 技术栈文档 | architecture | 全部模块 |
| `DATA_ARCHITECTURE_FILE` | 数据架构文档 | architecture | backend, fullstack |
| `CONTRACT_FILE` | API 契约文档 | architecture | backend, frontend, fullstack, flutter |
| `IMPLEMENTATION_ROADMAP_FILE` | 实施路线图 | architecture | 全部模块 |
| `SECURITY_FILE` | 安全架构文档 | architecture | backend, frontend, flutter |
| `FRONTEND_ROOT` | 前端项目路径 | 用户/architecture | frontend, fullstack |
| `BACKEND_ROOT` | 后端项目路径 | 用户/architecture | backend, fullstack |

## Tags

- domain: software-engineering
- type: architecture
- version: 2.0.0
