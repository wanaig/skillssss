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

- 支持 Agent / Task 功能的 AI 编程环境
- 一份 PRD 需求文档（功能需求/产品文档）

## 第 1 步：架构设计（必须最先执行）

加载 architecture 主智能体：

```
使用 fs-architect 主智能体
PRD 路径: {你的需求文档路径}
输出目录: {架构文档输出目录}
```

架构主智能体会：
1. 收集项目约束信息（团队技能、规模、合规等）
2. 并行启动 5 个维度分析Agent（techstack / data / infra / security / api-design）
3. 一致性检查和需求覆盖度检查
4. 产出 `architecture-design.md` + 5 份维度文档 + `implementation-roadmap.md`

## 第 2 步：前后端并行开发

架构设计完成后，可同时启动前端和后端开发。

### 启动前端

```
使用 /frontend/main-agent-prompt-vue.md
PROJECT_ROOT: {前端项目路径}
REQUIREMENT_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
CONTRACT_FILE: {api-contract.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
BATCH_SIZE: 1 (或指定 N)
```

### 启动后端

```
使用 /backend/main-agent-prompt.md
PROJECT_ROOT: {后端项目路径}
REQUIREMENT_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
DATA_ARCHITECTURE_FILE: {data-architecture.md 路径}
CONTRACT_FILE: {api-contract.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
BATCH_SIZE: 1 (或指定 N)
```

### 如需跨平台应用（Flutter）

使用 /flutter/main-agent-prompt-flutter.md
PROJECT_ROOT: {Flutter 项目路径}
REQUIREMENT_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
CONTRACT_FILE: {api-contract.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}

### 如需区块链智能合约（FISCO BCOS）

使用 /blockchain/main-agent-prompt-blockchain.md
PROJECT_ROOT: {区块链项目路径}
REQUIREMENT_FILE: {PRD 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
DATA_ARCHITECTURE_FILE: {data-architecture.md 路径}
CONTRACT_FILE: {api-contract.md 路径}
SECURITY_FILE: {security-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}

## 第 3 步：前后端联调

⚠️ **必须等 frontend/ 和 backend/ 全部完成后再执行。**

```
使用 /fullstack/main-agent-prompt-fullstack.md
FRONTEND_ROOT: {前端项目路径}
BACKEND_ROOT: {后端项目路径}
FLUTTER_ROOT: {Flutter 项目路径（如有）}
BLOCKCHAIN_ROOT: {区块链项目路径（如有）}
CONTRACT_FILE: {api-contract.md 路径}
TECH_STACK_FILE: {tech-stack.md 路径}
DATA_ARCHITECTURE_FILE: {data-architecture.md 路径}
IMPLEMENTATION_ROADMAP_FILE: {implementation-roadmap.md 路径}
BATCH_SIZE: 1 (或指定 N)

联调主智能体会：
1. 读取 API 契约文档 + 扫描各端代码现状
2. 创建前端 API 调用层（`src/api/`、`src/types/api.ts`、`vite.config.ts` 代理）
3. 如有 BLOCKCHAIN_ROOT，额外创建 `src/api/blockchain.ts` 区块链调用层
4. 逐模块对接接口：前端类型定义 ↔ 后端响应格式
5. 三维测试：契约一致性 / 数据流完整性 / 端到端集成
6. 修正循环确保各端完全对齐

## 第 4 步：验证收尾

所有领域完成后，检查各领域的 `main-log.md` 确认：
- 全部模块标记 ✅
- 迭代统计汇总
- `lessons-learned.md` 记录的经验可复用到下个项目

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
