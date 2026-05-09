# Skill: harness_design_notes

# 设计笔记 — 多智能体协同开发

补充参考文档，包含 v2 版本改进项对照、历史方法记录、模型选择原则和与其他方法的对比分析。设计原理正篇已迁移至 docs/design-principles.md。

## When to Use This Skill

- 需要了解 v1 到 v2 的版本演进和改进项
- 需要查阅历史子智能体 ID 获取方法
- 需要参考模型选择的推荐策略
- 需要与其他多智能体方法进行对比评估

## Core Content

> ⚠️ 本文档的设计原理内容已迁移至 [`docs/design-principles.md`](docs/design-principles.md)。
> 本文件保留作为补充参考，包含一些未纳入正式文档的思考片段。

---

## 与正式文档的关系

正式的架构文档、使用手册和设计原理请参见：

| 文档 | 路径 |
|------|------|
| 系统架构详情 | [`docs/architecture.md`](docs/architecture.md) |
| 使用手册 | [`docs/workflow.md`](docs/workflow.md) |
| 设计原理 | [`docs/design-principles.md`](docs/design-principles.md) |
| 项目概述 | [`README.md`](README.md) |

---

## v2 改进项对照

以下改进基于 v1 版本的已知局限，在 v2 中实现：

| v1 问题 | v2 解决方案 |
|---------|-----------|
| Agent ID 收集依赖文件系统时间戳，并发Agent可能拿错ID | 子Agent写入 `agent-registry.json`（键值索引），主Agent按 key 精确提取 |
| PASS/FAIL 判定依赖 Grep 文本匹配，格式漂移导致误判 | 测试Agent 输出结构化 `test-report.json`，主Agent 解析 `verdict` 字段 |
| 3轮重试无差异对待，严重问题被静默降级 | 引入 severity 三级（blocker/major/minor），blocker/major 禁止降级通过 |
| 错误修改无回滚路径 | 第2轮修正后提供 git revert + 重启Agent 选项 |
| 无成本追踪 | 每批/每Phase 记录 Agent 调用次数和 token 消耗 |
| 仅支持新项目，无法做增量开发 | Planner Agent 自动分析现有代码结构，产出 `existing-architecture-analysis.md` |
| 性能/安全测试无真实数据环境 | be-planner 自动搭建 Docker Compose 测试环境 + 种子数据 |

---

## 补充参考

### 子智能体 ID 获取（历史方法）

```bash
ls -lt ~/.claude/projects/*/*/subagents/agent-*.meta.json | head -3
```

从文件名提取：`agent-a95e84cd0b54c85ad.meta.json` → 裸ID = `a95e84cd0b54c85ad`

### 模型选择原则

| 场景 | 推荐模型 | 理由 |
|------|----------|------|
| 简单、重复性任务 | haiku | 快速、便宜、够用 |
| 代码开发 | inherit（跟随主智能体） | 开发需要最强推理 |
| 测试/审查 | inherit 或 sonnet | 需要足够的判断力 |

### 与其他方法的对比

| 维度 | 单智能体长对话 | 双窗口手动切换 | 本方法论（主智能体编排） |
|------|---------------|---------------|----------------------|
| 上下文管理 | 差（窗口爆满） | 中（手动管理） | 好（自动隔离+传递） |
| 迭代效率 | 低 | 中 | 高（自动循环） |
| 可追溯性 | 差 | 中 | 好（三层日志） |
| 经验积累 | 差 | 差 | 好（自动写入经验库） |
| 实施复杂度 | 低 | 中 | 高（需要精心设计模板） |

---

*参考理论：Meta-Harness（Khattab & Finn, 2026）*

## Tags

- domain: software-engineering
- type: notes
- version: 2.0.0
