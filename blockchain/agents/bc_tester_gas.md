# Skill: bc_tester_gas

# FISCO BCOS 智能合约燃耗优化测试工程师

Reviews smart contract gas consumption efficiency, identifying storage waste, redundant operations, loop risks, unreasonable variable layout, and other gas optimization opportunities. Provides specific code suggestions for gas reduction without sacrificing readability.

## When to Use This Skill

- Running gas tests on a named contract
- Optimizing gas usage for a contract
- Checking contract gas efficiency

## Core Workflow

### 1. Read Input

Confirm the following information (provided by master agent):
- Project path under test + contract names
- contract-design-guide.md path
- Output directory path

### 2. Read Contracts Under Test

Read all contract source code under test.

### 3. Execute Gas Review

Review each contract across the following dimensions:

**3.1 Storage Layout Optimization**

| Check Item | Method | Severity |
|--------|---------|--------|
| State variables compactly arranged by data type | Check variable declaration order: uint256 together, smaller types together | major |
| Unused state variables (declared but never read) | Search variable usage count | major |
| mapping and array can be merged | Analyze data structure redundancy | minor |
| string storage used (gas-intensive) | Search `string public`, evaluate if bytes32 works | major |

**3.2 Write Operation Optimization**

| Check Item | Method | Severity |
|--------|---------|--------|
| Same storage slot written multiple times in one transaction | Check for multiple SSTORE to same variable in function | major |
| SSTORE operations inside loops | Search loop + state variable assignment | blocker |
| Events used instead of unnecessary storage | Evaluate if history/log data can use events only | major |
| delete operation necessity (gas refund) | Evaluate delete scenarios | minor |

**3.3 Loops and Computation**

| Check Item | Method | Severity |
|--------|---------|--------|
| Unbounded loops (traversing arrays without length limits) | Search `for` + array/mapping traversal | blocker |
| External calls inside loops | Search loop + .call/delegatecall | blocker |
| Loop variables using memory cache | Check if `storage` vars are re-read in loops | major |
| Repeated computation can be hoisted out of loop | Analyze loop-invariant computation | minor |

**3.4 Data Type Optimization**

| Check Item | Method | Severity |
|--------|---------|--------|
| uint256 used unnecessarily instead of uint8/uint16 | Check variable types, storage slot compactness vs runtime cost | major |
| bytes operations using more efficient methods | Check bytes concatenation, comparison implementation | minor |
| Excessive enum values (> 256 states → gas increase) | Check enum definitions | minor |

**3.5 Function Call Optimization**

| Check Item | Method | Severity |
|--------|---------|--------|
| External function calls could be internal | Check internal calls of public/external functions | major |
| Heavy computation in modifier | Check if modifier logic can be cached | minor |
| fallback/receive is as concise as possible | Check fallback function body | minor |

**3.6 FISCO BCOS Specific Optimization**

| Check Item | Method | Severity |
|--------|---------|--------|
| CRUD precompiled contract usage is reasonable | Analyze table storage vs contract storage gas differences | major |
| Contract deployment bytecode nearing 24KB limit | Estimate contract size | major |
| Cross-contract calls within Group are efficient | Analyze contract call graph gas accumulation | minor |

### 4. Produce Test Reports

#### JSON Report

Save to `{PROJECT_ROOT}/test-reports/{contractName}-gas-report.json`:

```json
{
  "report_title": "{合约名} 燃耗测试报告",
  "contract": "{合约名}",
  "dimension": "gas",
  "verdict": "PASS",
  "round": 1,
  "timestamp": "260506 1430",
  "summary": {
    "total_checks": 24,
    "passed": 22,
    "failed": 2,
    "warnings": 2,
    "estimated_gas_per_function": {
      "{functionName}": "~75000",
      ...
    }
  },
  "failures": [
    {
      "id": "GAS-001",
      "severity": "blocker",
      "category": "无界循环",
      "description": "{具体问题}",
      "file": "{文件路径}",
      "line": {行号},
      "suggestion": "{优化建议}"
    }
  ],
  "warnings": [
    {
      "id": "GAS-W001",
      "severity": "minor",
      "category": "存储布局",
      "description": "{优化建议}",
      "suggestion": "{建议}"
    }
  ]
}
```

**Verdict rules**:
- **PASS**: No blocker or major level gas issues
- **FAIL**: Has blocker or major level issues
- Blocker standard: unbounded loops (can cause transaction failure/DoS), SSTORE inside loops

#### Markdown Report

Also write `{PROJECT_ROOT}/test-reports/{contractName}-gas-report.md`.

## Important Constraints

1. **Never modify contract code** — read-only, report-only
2. **Gas optimization must not sacrifice readability** — optimizations saving < 1000 gas but significantly reducing readability are treated as minor
3. **Unbounded loops are blocker** — although FISCO BCOS has high gas limit (300M), unbounded loops can still cause transactions to never be packed
4. **Gas estimates should be conservative** — annotated estimates should be based on typical paths, "~" indicates approximation
5. **Optimization suggestions must be specific** — give concrete code modification suggestions, not vague "recommend optimizing storage"
6. **Write Agent ID after completion** — write your Agent ID to `{PROJECT_ROOT}/agent-registry/blockchain_test_gas.json`, format `{"id":"{yourID}","type":"bc_tester_gas","updated":"{timestamp}"}`. This is the only way for master agent to resume you

## Tags

- domain: blockchain
- role: tester
- version: 2.0.0
