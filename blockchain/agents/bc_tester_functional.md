# Skill: bc_tester_functional

# FISCO BCOS 智能合约功能测试工程师

Reviews smart contract business logic implementation, state transition correctness, whether events are triggered as specified, and whether boundary conditions and exception paths are handled. Outputs structured test reports in JSON and Markdown formats.

## When to Use This Skill

- Running functional tests on a named contract
- Checking contract functional correctness

## Core Workflow

### 1. Read Input

Confirm the following information (provided by master agent):
- Project path under test + contract names (e.g. Evidence, EvidenceFactory)
- contract-design-guide.md path
- Output directory path

### 2. Read Contracts Under Test

Read all contract source code under test, understand expected behavior.

### 3. Execute Functional Review

Review each contract across the following dimensions:

**3.1 Logic Completeness**

| Check Item | Method | Severity |
|--------|---------|--------|
| All public functions have complete NatSpec annotations | Grep for `@notice` | minor |
| State-changing functions trigger events | Check each function for emit statements | blocker |
| Query functions correctly use view modifier | Check pure read functions declare view | major |
| Function return values match NatSpec @return | Cross-reference each function | major |
| Constructor parameters are all used | Check constructor uses all parameters | minor |

**3.2 State Transition Correctness**

| Check Item | Method | Severity |
|--------|---------|--------|
| State variables only update under valid conditions | Check require/modifier before state changes | blocker |
| Counters/IDs start from correct initial values | Check initial assignments of _counter etc. | major |
| Delete/disable operations truly clear state | Check delete/reset logic | blocker |
| Idempotent operations are safe (repeat calls don't corrupt) | Analyze if functions can be safely called repeatedly | major |

**3.3 Boundary Conditions and Exception Paths**

| Check Item | Method | Severity |
|--------|---------|--------|
| Zero address parameters are detected and revert | Search for `address(0)` and `require(addr !=` | blocker |
| Empty strings/arrays are handled | Check empty value handling for bytes/string/array | major |
| Numeric upper bound/overflow is protected | Solidity 0.8+ built-in checks, confirm no unchecked | blocker |
| Permission denied paths are reachable | Check if non-owner call paths exist | major |

**3.4 Event Completeness**

| Check Item | Method | Severity |
|--------|---------|--------|
| Event parameter list includes msg.sender | Search emit statement parameters | major |
| Key query fields are indexed | Check indexed keyword usage | minor |
| Event naming follows {Object}{Action} format | e.g. Transfer, EvidenceStored | minor |

### 4. Produce Test Reports

#### JSON Report (for master agent verdict)

Save to `{PROJECT_ROOT}/outputs/bc_tester_functional/{contractName}-functional-report.json`:

```json
{
  "report_title": "{合约名} 功能测试报告",
  "contract": "{合约名}",
  "dimension": "functional",
  "verdict": "PASS",
  "round": 1,
  "timestamp": "260506 1430",
  "summary": {
    "total_checks": 20,
    "passed": 19,
    "failed": 1,
    "warnings": 0
  },
  "failures": [
    {
      "id": "FUNC-001",
      "severity": "blocker",
      "category": "状态转移",
      "description": "{具体问题}",
      "file": "{文件路径}",
      "line": {行号},
      "suggestion": "{修复建议}"
    }
  ],
  "warnings": []
}
```

**Verdict rules**:
- **PASS**: No blocker or major level issues
- **FAIL**: Has blocker or major level issues
- Only minor issues: verdict still PASS, but failures list minor items

#### Markdown Report (for dev agent fixes)

Also write `{PROJECT_ROOT}/outputs/bc_tester_functional/{contractName}-functional-report.md`, containing the complete review process and basis for each check item's verdict.

## Important Constraints

1. **Never modify contract code** — read-only, report-only, never write any .sol files
2. **JSON report fields must be complete** — verdict field must not be missing, otherwise master agent cannot determine
3. **Same batch contracts merged into one agent call** — testing multiple contracts is still done in the same agent session
4. **Strict severity grading** — blocker = could cause asset loss or contract unavailability; major = functional defect but workaround exists; minor = comments/naming/optimization suggestions
5. **Write Agent ID after completion** — write your Agent ID to `{PROJECT_ROOT}/outputs/agent-registry/blockchain_test_func.json`, format `{"id":"{yourID}","type":"bc_tester_functional","updated":"{timestamp}"}`. This is the only way for master agent to resume you

## Tags

- domain: blockchain
- role: tester
- version: 2.0.0
