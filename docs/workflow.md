# Skill: harness_workflow

# 使用手册

Harness Engineering 系统的详细使用流程，从架构设计到前后端并行开发、联调、部署上线的分步操作指南，包含参数说明和日志体系。

## When to Use This Skill

- 需要按步骤执行多智能体协同开发任务时
- 需要了解每个阶段的输入参数和输出文件
- 需要调整 BATCH_SIZE 或查阅日志文件结构
- 需要启动 Flutter 跨端或区块链智能合约模块

## Core Content

## 前置条件

1. 支持 Agent/Task/Skill 调用的 AI 编程环境
2. 一份 PRD 文档（`REQUIREMENT_FILE`）
3. 准备好各项目目录（前端、后端、可选 Flutter、可选区块链、部署输出目录）

## 路径变量模板（推荐先准备）

```text
REQUIREMENT_FILE={D:\path\to\prd.md}
ARCH_ROOT={D:\path\to\architecture-output}
FRONTEND_ROOT={D:\path\to\frontend}
BACKEND_ROOT={D:\path\to\backend}
FLUTTER_ROOT={D:\path\to\flutter}            # 可选
BLOCKCHAIN_ROOT={D:\path\to\blockchain}      # 可选
DEPLOY_ROOT={D:\path\to\deploy-output}
```

## Phase -1：需求采集（外包平台入口）

使用 `platform/main_agent_prompt_platform.md` 的 Phase 0 部分：

```text
CLIENT_INPUT={客户需求描述}
PLATFORM_ROOT={平台项目根目录}
```

输出：
- `{PLATFORM_ROOT}/outputs/pf_intake/prd.md` — 结构化 PRD 文档
- `{PLATFORM_ROOT}/outputs/project-status.json` — 项目状态跟踪

此阶段完成后，将产出 `prd.md` 的路径记为 `REQUIREMENT_FILE`，传递给后续所有阶段。

## Phase 0：架构设计（必须先执行）

使用 `architecture/main_agent_prompt_fs_architect.md`：

```text
REQUIREMENT_FILE
PROJECT_ROOT={ARCH_ROOT}
```

输出（位于 `ARCH_ROOT`）：

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

## Phase 1：并行开发（可同时启动）

### 1) 前端

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

### 2) 后端

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

### 3) Flutter（可选）

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

### 4) 区块链（可选）

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

## Phase 2：前后端联调（串行）

⚠️ **必须等 frontend/ 和 backend/ 完成后再执行。**

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

## Phase 3：生产部署（最后执行）

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

## Phase 4：交付验收（外包平台出口）

使用 `platform/main_agent_prompt_platform.md` 的 Phase 4 部分：

```text
REQUIREMENT_FILE
PLATFORM_ROOT
```

输出：
- `{PLATFORM_ROOT}/outputs/pf_delivery/acceptance-report.md` — 客户验收报告

## 验证收尾

所有阶段完成后，检查：

1. 各领域 `main-log.md` 是否全部模块 ✅
2. `test-reports/` 的测试结论是否通过
3. `lessons-learned.md` 是否形成经验沉淀

## BATCH_SIZE 说明

默认 `BATCH_SIZE=1`，表示每次开发 1 个模块，然后立即测试。用户可指定更大的值（如 3），此时一次开发 3 个模块后统一测试。开发批量与测试批量始终一致。

## 日志体系

| 日志文件 | 位置 | 内容 |
|---------|------|------|
| `main-log.md` | 各领域根目录 | 所有关键事件，时间戳精确到分钟 |
| `dev-plan.md` / `integration-plan.md` | 各领域根目录 | 任务清单，带 ⏳/✅/⚠️ 状态 |
| `test-report.json` | `test-reports/` | 测试Agent 产出的结构化判定 |
| `agent-registry/{key}.json` | 各项目根目录 | Agent ID 键值索引 |
| `lessons-learned.md` | 各项目根目录 | 跨批次累积的经验 |

## Tags

- domain: software-engineering
- type: guide
- version: 2.0.0
