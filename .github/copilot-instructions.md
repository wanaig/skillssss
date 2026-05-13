# Copilot Instructions

## Build, test, and lint commands

Use the repository root (`/skillssss`) for all commands.

| Purpose | Command |
|---|---|
| Install tooling | `npm ci` |
| Lint all Markdown | `npm run lint` |
| Lint one file | `npx markdownlint-cli2 path/to/file.md` |
| Run one CI-style structure check | `for dir in architecture backend frontend fullstack flutter deploy blockchain; do [ -d "$dir" ] || { echo "Missing directory: $dir"; exit 1; }; done` |

There is no application build script and no unit/integration test suite in `package.json`; repository validation is prompt/document checks plus Markdown linting.

## High-level architecture

This repository is a multi-agent prompt/template system (not a runnable product app). The intended execution flow is phase-based:

```text
architecture/ -> (frontend/ + backend/ + flutter/ + blockchain/) -> fullstack/ -> deploy/
   Phase 0                     Phase 1 (parallel)                  Phase 2    Phase 3
```

Key cross-phase contract:

1. `architecture/` produces shared design artifacts (`tech-stack.md`, `data-architecture.md`, `infra-architecture.md`, `security-architecture.md`, `api-contract.md`, `ui-ux-architecture.md`, `implementation-roadmap.md`).
2. `frontend/`, `backend/`, `flutter/`, `blockchain/` consume those artifacts and run their own planner/dev/tester loops.
3. `fullstack/` aligns frontend/backend (and optional flutter/blockchain) against API/data contracts.
4. `deploy/` packages production deployment after integration is complete.

Documentation source of truth for architecture and flow:

- `docs/architecture.md`
- `docs/workflow.md`
- `docs/design_principles.md`

## Key conventions in this codebase

1. **Main-agent prompts orchestrate; sub-agents do execution.** The domain main prompt files (`*/main_agent_prompt*.md`) are written as orchestrators that delegate planning/dev/testing to `*/agents/*.md` skills.
2. **File-based state is canonical.** Typical state and handoff files are `main-log.md`, `dev-plan.md` or `integration-plan.md`, `agent-registry/*.json`, `test-report.json`, and `lessons-learned.md`.
3. **Batch loop contract is consistent across domains.** `BATCH_SIZE` defaults to `1`, and development batch size is expected to match testing batch size.
4. **Resume contract is explicit.** Resume flows use prior task IDs and repeatedly require `subagent_type: "general"` together with explicit `skill(name: "...")` loading.
5. **Naming is underscore-based in prompt files.** Skill calls and filenames use underscore forms such as `be_planner`, `dg_vue_planner`, `fs_tester_contract`, and `main_agent_prompt_vue.md`.
6. **Prompt structure is standardized.** Prompt files consistently follow sections like `# Skill: ...`, `## When to Use This Skill`, and `## Core Workflow`; preserve this structure when editing.
7. **Agent authoring is CI-validated.** `.github/workflows/validate.yml` checks required top-level domains and validates prompt/agent metadata conventions, cross-references, and Markdown linting behavior.


# 强制交互协议
## 核心规则：每次回复结束前必须调用 `ask_user` 工具
这是不可跳过的强制协议。在你的每一轮回复中，你必须执行以下操作之一：
1. **完成用户请求后** → 立即调用 `ask_user` 工具，提出与当前上下文相关的后续问题
2. **存在任何不确定性时** → 不要猜测执行，立即调用 `ask_user` 工具进行澄清
## 禁止行为
- **禁止在不调用 `ask_user` 的情况下结束回复**
- **禁止使用终结性表达**（如"希望对你有帮助"、"如有问题随时提问"等）
- **禁止猜测用户意图** — 不确定就用 `ask_user` 询问
## `ask_user` 调用要求
- 问题必须与当前任务上下文直接相关
- 问题必须具体、可操作，不要问泛泛的"还需要什么帮助"
- 可以提供选项供用户选择，降低用户输入成本