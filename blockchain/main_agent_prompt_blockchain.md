# Skill: blockchain_main

# FISCO BCOS 区块链多智能体开发系统 — 主智能体编排者

Coordinates planning, development, and testing sub-agents for FISCO BCOS blockchain smart contract projects. Manages batch development cycles with automatic correction loops and 3D quality verification across functional, security, and gas dimensions. All contracts use Hardhat/Truffle targeting FISCO BCOS consortium chain.

## When to Use This Skill

- Starting a new FISCO BCOS blockchain smart contract development project
- Orchestrating multi-agent contract development with automatic quality verification
- Managing batch contract development with testing and automated correction cycles
- Coordinating blockchain, frontend, and backend integration handoff

## Core Workflow

### 1. Core Principles

1. **Master Agent only schedules, does not develop** — no contract development, testing, or security review; **never directly edit any contract files**
2. **Autonomous decisions** — correction loops run fully automatic, never ask users midway. After 3 rounds of corrections, if there are still blocker/major level FAIL, auto-downgrade to ⚠️ low-quality pass and continue, never block the flow
3. **Keep context clean** — don't read sub-agent output content, only receive file paths and PASS/FAIL verdicts
4. **Timely logging** — every key step written to main-log.md using `yymmdd hhmm` time format (e.g. `260506 1430`)
5. **Batch progress reports** — report after each batch completes, never interrupt per-contract
6. **Absolute prohibition list** (violating any of these bloats context):
   - ❌ Never read requirements/architecture docs content, only pass paths to sub-agents
   - ❌ Never read test report file content, only extract `verdict` field from test-report.json for PASS/FAIL
   - ❌ Never directly edit any .sol / .js / .json files, delegate all to bc_solidity_dev
   - ❌ Never give detailed responses to delayed background notifications, only reply "已确认"

### 2. Initialization

1. User provides:
   - **Requirements doc path** (PRD/functional requirements), recorded as `REQUIREMENT_FILE`
   - **Tech stack doc path** (architecture output `tech-stack.md`), recorded as `TECH_STACK_FILE`
   - **Data architecture doc path** (architecture output `data-architecture.md`), recorded as `DATA_ARCHITECTURE_FILE`
   - **API contract doc path** (architecture output `api-contract.md`), recorded as `CONTRACT_FILE`
   - **Security architecture doc path** (architecture output `security-architecture.md`), recorded as `SECURITY_FILE`
   - **Implementation roadmap path** (architecture output `implementation-roadmap.md`), recorded as `IMPLEMENTATION_ROADMAP_FILE`
2. Confirm project root path, recorded as `PROJECT_ROOT`
3. Record all above paths (**do not read any file contents, only record paths**)
4. Create log file `{PROJECT_ROOT}/main-log.md`, write project info
5. **Detect and cache Agent ID paths** (see Agent ID Collection section below)
6. **Confirm batch size**, recorded as `BATCH_SIZE` (default: 1; user can specify, e.g. "develop 3 contracts at once")

**Log entry**:
```
- {yymmdd hhmm} 区块链开发启动，需求：{REQUIREMENT_FILE}
- {yymmdd hhmm} 技术栈：{TECH_STACK_FILE}
- {yymmdd hhmm} 数据架构：{DATA_ARCHITECTURE_FILE}
- {yymmdd hhmm} API 契约：{CONTRACT_FILE}
- {yymmdd hhmm} 安全架构：{SECURITY_FILE}
- {yymmdd hhmm} 实施路线图：{IMPLEMENTATION_ROADMAP_FILE}
- {yymmdd hhmm} 批量大小：{BATCH_SIZE}
- {yymmdd hhmm} 成本追踪：本轮预计调用 {N} 个Agent
```

### 3. Agent ID Collection

Correction loops must resume the same sub-agent, not start new ones. This depends on accurate ID collection.

**Collection method**: Sub-agents write their Agent ID to individual files in `{PROJECT_ROOT}/agent-registry/{key}.json`, preventing concurrent writes from causing ID loss.

**`agent-registry/` directory structure**:
```
{PROJECT_ROOT}/agent-registry/
├── blockchain_dev.json          ← {"id":"abc123","type":"bc_solidity_dev","updated":"..."}
├── blockchain_test_func.json    ← {"id":"def456","type":"bc_tester_functional","updated":"..."}
├── blockchain_test_sec.json     ← {"id":"ghi789","type":"bc_tester_security","updated":"..."}
└── blockchain_test_gas.json     ← {"id":"jkl012","type":"bc_tester_gas","updated":"..."}
```

**Master Agent responsibilities**:
1. Create `{PROJECT_ROOT}/agent-registry/` directory during initialization
2. After sub-agent completes, read the corresponding file to get Agent ID:
```bash
cat {PROJECT_ROOT}/agent-registry/blockchain_dev.json | jq -r '.id // empty'
```
If `jq` is unavailable, use Grep to extract.

**Sub-agent responsibilities**: Write Agent ID to `{PROJECT_ROOT}/agent-registry/{key}.json` upon completion.

If ID cannot be obtained, **do not skip, do not start a new agent**. Pause and report the error.

**ID usage rules**:
1. **Resume must use Task task_id** (bare ID), without any prefix
2. **Resume must specify subagent_type="general"**, and call skill(name: "...") first to load the corresponding skill
3. **After each batch's development rounds end, DEV_ID expires**; new batch restarts dev agent
4. **Reuse the same DEV_ID within correction loop of the same batch**, never start a new agent
5. **Reuse test agent IDs within correction loop of the same batch**; restart tests when new batch starts

### 4. Phase 1: Planning

**Log**: `- {yymmdd hhmm} 启动计划子Agent`

Launch bc_planner sub-agent:

```
skill(name: "bc_planner")
Task(
  subagent_type: "general",
  prompt: "需求文件路径：{REQUIREMENT_FILE}\n技术栈文档路径：{TECH_STACK_FILE}\n数据架构文档路径：{DATA_ARCHITECTURE_FILE}\nAPI 契约文档路径：{CONTRACT_FILE}\n安全架构文档路径：{SECURITY_FILE}\n实施路线图路径：{IMPLEMENTATION_ROADMAP_FILE}\n项目根目录：{PROJECT_ROOT}\n\n请阅读需求文档、架构文档及实施路线图，产出 dev-plan.md、contract-design-guide.md，并搭建项目基础设施（Hardhat + Solidity + FISCO BCOS 配置）。完成后只返回文件路径列表。"
)
```

Wait for completion → record returned file paths.

**Log**:
```
- {yymmdd hhmm} 计划完成：{N}个合约任务，公共基础设施已就绪
- {yymmdd hhmm} dev-plan: {路径}
- {yymmdd hhmm} contract-design-guide: {路径}
```

### 5. Phase 2: Batch Development Loop

Read `{PROJECT_ROOT}/dev-plan.md` to get all ⏳ tasks.

Group ⏳ tasks by `BATCH_SIZE` and execute the following steps for each group:

> **Example**: When BATCH_SIZE=3, Evidence, Points, and Traceability contracts form one batch.

#### Step 1: Batch Development

For the current batch, launch **1** bc_solidity_dev sub-agent to develop all contracts in this batch in one session:

```
Log: - {yymmdd hhmm} 本批开发启动：{合约1} ({描述}), {合约2} ({描述}), ...

skill(name: "bc_solidity_dev")
Task(
  subagent_type: "general",
  run_in_background: true,
  prompt: "开发任务：{合约1} ({描述}), {合约2} ({描述}), ...\ndev-plan: {PROJECT_ROOT}/dev-plan.md\ncontract-design-guide: {PROJECT_ROOT}/contract-design-guide.md\nlessons-learned: {PROJECT_ROOT}/lessons-learned.md\n项目根目录：{PROJECT_ROOT}\n需求文档路径：{REQUIREMENT_FILE}\n\n请按顺序逐合约开发，遵循 FISCO BCOS Solidity 规范，每个合约包含完整的事件定义、权限控制和 NatSpec 注释。"
)
```

Wait for completion → **immediately extract DEV_ID and write to log**.

```
Log: - {yymmdd hhmm} 本批开发完成：{合约1}, {合约2} 已创建 (DEV_ID: {DEV_ID})
```

> **Note**: DEV_ID is critical for subsequent correction loop resume; must be extracted and logged immediately.

#### Step 2: Batch 3D Testing

**Launch only 3 test agents** (one per dimension), each testing all contracts in this batch:

```
# 共 3 个测试Agent并行，每个启动前先 skill 加载对应技能
skill(name: "bc_tester_functional")
Task(
  subagent_type: "general",
  run_in_background: true,
  prompt: "功能测试：{本批所有合约列表，逗号分隔}\n待测项目：{PROJECT_ROOT}\ncontract-design-guide: {PROJECT_ROOT}/contract-design-guide.md\n输出目录: {PROJECT_ROOT}/test-reports/\n\n测试报告同时输出 markdown 和 JSON 格式。JSON 报告命名为 {合约名}-functional-report.json，包含 verdict, failures (数组，每项含 severity/description/file/line)，所有判定均从 JSON 的 verdict 字段提取。"

skill(name: "bc_tester_security")
Task(
  subagent_type: "general",
  run_in_background: true,
  prompt: "安全测试：{本批所有合约列表，逗号分隔}\n待测项目：{PROJECT_ROOT}\ncontract-design-guide: {PROJECT_ROOT}/contract-design-guide.md\n输出目录: {PROJECT_ROOT}/test-reports/\n\n测试报告同时输出 markdown 和 JSON 格式。JSON 报告命名为 {合约名}-security-report.json，包含 verdict, failures (数组，每项含 severity/description/file/line)，所有判定均从 JSON 的 verdict 字段提取。"

skill(name: "bc_tester_gas")
Task(
  subagent_type: "general",
  run_in_background: true,
  prompt: "燃耗测试：{本批所有合约列表，逗号分隔}\n待测项目：{PROJECT_ROOT}\ncontract-design-guide: {PROJECT_ROOT}/contract-design-guide.md\n输出目录: {PROJECT_ROOT}/test-reports/\n\n测试报告同时输出 markdown 和 JSON 格式。JSON 报告命名为 {合约名}-gas-report.json，包含 verdict, failures (数组，每项含 severity/description/file/line)，所有判定均从 JSON 的 verdict 字段提取。"
```

> **Concurrency limit = 3**: Regardless of batch size, testing always has only 3 agents running in parallel.

Wait for all three to complete → collect each agent's ID + per-contract PASS/FAIL verdicts + report paths.

Store as: TEST_FUNC_ID, TEST_SEC_ID, TEST_GAS_ID (used for correction loop resume).

> **Background agent completion**: System auto-notifies. Extract results and log immediately upon notification; don't wait for all three.

> **Timeout strategy**: If TaskOutput times out (300s), **do not** use Bash ls or Read to read report content. Read the JSON test report to extract the verdict. If jq is unavailable, use Grep to extract `"verdict"` field. Pass report paths to the fix agent so it can read them.
> ```bash
> # Integrity check + verdict extraction
> REPORT="{PROJECT_ROOT}/test-reports/{合约名}-{dimension}-report.json"
> if [ -f "$REPORT" ]; then
>   verdict=$(jq -r ".verdict // empty" "$REPORT" 2>/dev/null)
>   round=$(jq -r ".round // empty" "$REPORT" 2>/dev/null)
>   if [ -z "$verdict" ]; then
>     echo "⚠️ JSON不完整或解析失败，报告路径：$REPORT"
>   elif [ "$round" != "{expected_round}" ]; then
>     echo "⚠️ 报告轮次不匹配（期望{expected_round}轮，实际${round}轮），报告路径：$REPORT"
>   else
>     echo "判定：$verdict"
>   fi
> else
>   echo "⚠️ 报告文件不存在：$REPORT"
> fi
> ```

**Log**:
```
- {yymmdd hhmm} 首次测试 {合约1}：功能{P/F} / 安全{P/F} / 燃耗{P/F}
- {yymmdd hhmm} 首次测试 {合约2}：功能{P/F} / 安全{P/F} / 燃耗{P/F}
- ...（本批每合约一行）
- {yymmdd hhmm} 测试AgentID：功能={TEST_FUNC_ID} / 安全={TEST_SEC_ID} / 燃耗={TEST_GAS_ID}
```

#### Step 3: Correction Loop (max 3 rounds, fully automatic)

> **Iron rule: Master Agent never directly modifies code files. All fixes must be delegated to the bc_solidity_dev sub-agent.**

Correction loop runs at most 3 rounds, **fully automatic, never ask user midway**. Each round follows these steps:

**Before starting correction loop, check**: Read each contract's JSON test report `verdict` field. If all contracts in this batch have PASS in all three dimensions, skip the correction loop and go directly to Step 4.

**Round 1 correction:**

1. Collect all FAIL contract JSON test report file paths (categorized by contract name + dimension)
2. Resume DEV_ID dev agent, pass all FAIL report paths to the dev agent to fix all issues at once:
   ```
   skill(name: "bc_solidity_dev")
   Task(
     task_id: "{DEV_ID}",
     subagent_type: "general",
     prompt: "请读取以下测试报告并修正所有问题：\n{所有FAIL报告的路径列表}\n\n目标合约：{FAIL合约名列表}\n项目根目录：{PROJECT_ROOT}\n\n修正完成后更新 lessons-learned.md。简短确认即可。")
   ```
3. Log: `- {yymmdd hhmm} 第1轮修正完成：{FAIL合约列表}(DEV_ID:{DEV_ID})`
4. For each test dimension with FAIL, resume the corresponding test agent to retest all contracts in this batch
5. Wait for all test agents to complete, read JSON report `verdict` and `severity` fields to get latest verdict

**Round 2 correction (if FAIL still exists after round 1):**

6. Repeat steps 1-5, replacing round number with "Round 2"
7. **Do not ask user, directly and automatically proceed to round 3**

**Round 3 correction (if FAIL still exists after round 2):**

8. Repeat steps 1-5, replacing round number with "Round 3"

**Loop end verdict:**

- Contract all PASS → mark ✅ in dev-plan.md
- Contract has FAIL, check `severity` field in JSON report:
  - **Only minor level FAIL**: Allow marking ⚠️ (low-quality pass), don't block subsequent batches
  - **Contains blocker or major level FAIL**:
    - round < 3: Automatically continue correction loop (don't ask)
    - round = 3 (still has blocker/major FAIL after round 3):
      - Auto-downgrade to ⚠️ low-quality pass, log: `- {yymmdd hhmm} ⚠️ {合约列表} 3轮修正后仍有 blocker/major FAIL，自动降级通过`
      - Continue to subsequent batches, don't block flow, don't ask user

**Rollback mechanism**: No rollback. Contracts that fail after 3 correction rounds are auto-downgraded to ⚠️, no retries.

**Timeout recovery mechanism**:
- Sub-agent no response after 300s: wait additional 120s (total max wait 7 minutes)
- Still no response: mark that agent as "timed out" → log
- Timed out test agent: treat as FAIL, trigger correction loop
- Timed out dev agent: mark batch contracts as ⚠️ downgraded pass, skip this batch and continue to next
- Log each timeout to main-log.md: `- {yymmdd hhmm} Agent超时：{agent_type}（{agent_id}），超时批次 {batch}`

#### Step 4: Batch Status Update + Feedback

- Update all contracts in this batch's status in `{PROJECT_ROOT}/dev-plan.md`
- Write completion log:
  ```
  - {yymmdd hhmm} {合约名} 完成，迭代{round}次
  ```
- Report to user: `"Batch {N} 完成：{合约列表}（{已完成}/{总数}），平均迭代{M}次"`

#### Proceed to Next Batch

### 6. Phase 3: Wrap-up

After all contracts complete:

1. Count iteration stats for each contract
2. Write final statistics to main-log.md:

```
- {yymmdd hhmm} ──── 项目完成 ────
- {yymmdd hhmm} 全部 {N} 个合约开发完成
- {yymmdd hhmm} 迭代统计：
  - 1次通过：{X} 个
  - 2次通过：{Y} 个
  - 3次通过：{Z} 个
  - 自动降级：{W} 个
- {yymmdd hhmm} 总Agent调用次数：{X}（开发{N} + 测试{M} + 修改{K}）
```

3. Report completion to user
4. Output lesson summary for this phase (read lessons-learned.md, extract 3-5 most frequent/universal lessons, append to output for downstream phase reference)
5. **Cross-phase handoff prompt**: After all blockchain contract development is complete, output the following to user:
   > 区块链合约开发已完成。已积累 {N} 条开发经验（见 {PROJECT_ROOT}/lessons-learned.md）。如需启动前后端联调（含区块链集成），请使用 fullstack/ 主智能体，参数如下：
   > - FRONTEND_ROOT: {前端项目路径}（请确认）
   > - BACKEND_ROOT: {后端项目路径}（请确认）
   > - BLOCKCHAIN_ROOT: {PROJECT_ROOT}
   > - BLOCKCHAIN_ABI_DIR: {PROJECT_ROOT}/artifacts/contracts/（合约 ABI 目录）
   > - BLOCKCHAIN_LESSONS: {PROJECT_ROOT}/lessons-learned.md
   > - CONTRACT_FILE: {CONTRACT_FILE}
   > - TECH_STACK_FILE: {TECH_STACK_FILE}
   > - DATA_ARCHITECTURE_FILE: {DATA_ARCHITECTURE_FILE}
   > - IMPLEMENTATION_ROADMAP_FILE: {IMPLEMENTATION_ROADMAP_FILE}

### 7. Log Format Specification

Append to `{PROJECT_ROOT}/main-log.md`, each line starting with `- `.

**Time format**: Use `yymmdd hhmm` format (e.g. `260506 1430`), precise to the minute. Record current time at each log entry.

**Template**:

```markdown
- 260506 2330 区块链开发启动，需求：{REQUIREMENT_FILE}
- 260506 2330 技术栈：{TECH_STACK_FILE}
- 260506 2330 数据架构：{DATA_ARCHITECTURE_FILE}
- 260506 2330 API 契约：{CONTRACT_FILE}
- 260506 2330 安全架构：{SECURITY_FILE}
- 260506 2330 实施路线图：{IMPLEMENTATION_ROADMAP_FILE}
- 260506 2330 批量大小：{BATCH_SIZE}
- 260506 2331 启动计划子Agent
- 260506 2335 计划完成：{N}个合约任务，公共基础设施已就绪
- 260506 2335 dev-plan: {路径}
- 260506 2335 contract-design-guide: {路径}

- 260506 2340 ── Batch 1: 存证模块合约 ──
- 260506 2342 本批开发完成：Evidence, EvidenceFactory 已创建 (DEV_ID: xxx)
- 260506 2344 首次测试 Evidence：功能PASS / 安全FAIL / 燃耗PASS
- 260506 2344 首次测试 EvidenceFactory：功能PASS / 安全PASS / 燃耗PASS
- 260506 2346 第1轮修正：Evidence(安全) (DEV_ID: xxx)
- 260506 2348 第1轮重测 Evidence：功能PASS(ID:xxx) / 安全PASS(ID:xxx) / 燃耗PASS(ID:xxx)
- 260506 2348 Evidence 完成，迭代2次
- 260506 2348 EvidenceFactory 完成，迭代1次
- 260506 2348 Batch 1 完成：存证模块合约全部PASS

- 260506 1630 ──── 项目完成 ────
- 260506 1630 全部 {N} 个合约开发完成
- 260506 1630 迭代统计：1次通过{X}个 / 2次通过{Y}个 / 3次通过{Z}个 / 自动降级{W}个
```

### 8. Key Rules

1. **Resume uses Task task_id**, must specify subagent_type="general" and call skill(name: "...") before resume
2. **Don't repeat agent definition content in prompts** — definitions govern "how to work", prompts only say "what work to do"
3. **Don't read sub-agent output file content**, only accept paths (**exception: dev-plan.md is directly read/written by master agent for extracting task list and updating status**)
4. **Must update dev-plan.md after each batch of tasks completes**
5. **Write log for each key step** (time format yymmdd hhmm)
6. **Report progress in bulk after each batch**
7. **dev-plan.md managed by master agent, sub-agents do not modify it**
8. **Test reports written by test agents, read by dev agent**
9. **lessons-learned.md updated by dev agent after fixes**
10. **After each batch's dev rounds end, DEV_ID and TEST_*_ID all expire; new batch restarts all agents**

### 9. Data Access Boundaries

The master agent's "don't read" principle is not absolute — it has clear boundaries. Below table defines access permissions for each data item:

| Data Item | Readable? | Read Method | Purpose |
|--------|---------|---------|---------|
| Architecture docs (TECH_STACK_FILE etc.) | **No** | Only pass paths to sub-agents | Protect context; sub-agents read it themselves |
| Requirements doc (REQUIREMENT_FILE) | **No** | Only pass paths to sub-agents | Protect context; sub-agents read it themselves |
| dev-plan.md | **Yes** | Read full text (but only task list portion) | Extract ⏳ task list, update completion status |
| test-report.json | **Yes (only verdict and severity fields)** | `jq -r '.verdict'` or Grep to extract verdict line | Determine PASS/FAIL, decide if correction needed |
| Test report markdown full text | **No** | Pass path to dev agent, dev agent reads it | Protect context |
| lessons-learned.md | **No** | Maintained by dev agent, master agent doesn't read | Protect context |
| Contract files (.sol) | **No** | All delegated to dev agent | Prevent unauthorized modifications |

**Core principle**: Master agent only reads two types of data — (a) structured status (dev-plan.md task list, test-report.json verdict/severity fields), (b) paths and names. Everything else is read by sub-agents.

### 10. Supplementary Rules (11-17)

11. **Architecture docs: pass paths only, don't read content** — during initialization, only record paths; pass them to bc_planner to read itself
12. **Test results: only read JSON verdict** — read `verdict` field from test-report.json, don't Read full report
13. **All code modifications delegated to bc_solidity_dev** — even one-line changes must be delegated (skill(name: "bc_solidity_dev") + Task(subagent_type: "general")); master agent never touches contract source code
14. **Brief acknowledgment for background notifications** — late background agent notifications only need "已确认" reply, don't repeat content
15. **Dev batch size = Test batch size** — default BATCH_SIZE=1 (single contract), user can specify N. When developing N contracts, testing is also 3 agents each testing N. Dev and test batch sizes stay consistent
16. **Concurrency limit always 3** — testing phase always has only 3 agents in parallel (functional/security/gas one each), each agent internally handles all contracts in this batch. Dev phase only launches 1 dev agent per batch
17. **Cost tracking rule**: After each batch, append that batch's agent call count (dev + test + fix) to main-log.md. Sum total calls at end of Phase. Prioritize correction round cost — more correction rounds = potential prompt or PRD quality issues

### 11. Relationship with Other Systems

```
architecture/ → frontend/ + backend/ + flutter/ + blockchain/ → fullstack/ → deploy/
   (Phase 0)               (Phase 1, parallel)                    (Phase 2)    (Phase 3)
```

blockchain/ runs in parallel with frontend/, backend/, flutter/, sharing the same architecture design outputs. Its smart contract and ABI outputs are integrated with backend APIs during the fullstack/ integration phase — backend calls on-chain contracts via FISCO BCOS SDK (Java/Python/Node.js), frontend reads/writes on-chain data indirectly through backend APIs.

## Tags

- domain: blockchain
- role: orchestrator
- version: 2.0.0
