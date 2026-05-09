# Skill: bc_tester_security

# FISCO BCOS 智能合约安全测试工程师

Reviews smart contract security implementation, identifying common vulnerabilities including reentrancy attacks, integer overflow, permission flaws, tx.origin abuse, and randomness risks. Applies SWC Registry standards with FISCO BCOS-specific considerations for consortium chain security models.

## When to Use This Skill

- Running security tests on a named contract
- Checking contract security posture

## Core Workflow

### 1. Read Input

Confirm the following information (provided by master agent):
- Project path under test + contract names
- contract-design-guide.md path
- Output directory path

### 2. Read Contracts Under Test

Read all contract source code under test.

### 3. Execute Security Review

Review each contract across the following dimensions:

**3.1 Reentrancy Attack Prevention**

| Check Item | Method | Severity |
|--------|---------|--------|
| Transfer/payment functions use ReentrancyGuard | Search for `nonReentrant` modifier | blocker |
| State changes before external calls | Check logic order: change state first, then interact | blocker |
| Use of transfer/send (2300 gas limit) | Search `.transfer(` and `.send(` | major |

**3.2 Permission Control**

| Check Item | Method | Severity |
|--------|---------|--------|
| Critical functions have permission modifiers | Check onlyOwner/onlyAuthorized etc. | blocker |
| tx.origin used for identity verification | Search for `tx.origin` | blocker |
| Permission management functions have multi-sig or timelock protection | Check owner transfer protection | major |
| Setter functions missing onlyOwner | Search "set" functions for permission control | major |

**3.3 Integer Safety**

| Check Item | Method | Severity |
|--------|---------|--------|
| Use of unchecked blocks | Search `unchecked`, evaluate necessity | blocker |
| Counter overflow risk | Check counter operations in large loops | major |
| SafeCast/OpenZeppelin type conversions used | Check type conversion safety | minor |

**3.4 Randomness and Timing**

| Check Item | Method | Severity |
|--------|---------|--------|
| Block hash used for randomness | Search `blockhash`, `block.timestamp` for randomness | blocker |
| Reliance on block.timestamp for precise timing | Search `block.timestamp`, evaluate tolerance | major |
| Front-running risk | Analyze if transaction ordering affects outcome fairness | blocker |

**3.5 Signature and Verification**

| Check Item | Method | Severity |
|--------|---------|--------|
| Signature verification prevents replay attacks | Check nonce/heavy mechanism | blocker |
| ecrecover correctly recovers signer | Search `ecrecover`, check zero address branch | major |
| Address parameters validated for non-zero | Search `require.*!=.*address(0)` exists | major |

**3.6 Call Injection Risks**

| Check Item | Method | Severity |
|--------|---------|--------|
| Low-level call has return value check | Search `.call(`, check success branch | blocker |
| delegatecall target address is controlled | Search `delegatecall`, evaluate risk | blocker |
| Arbitrary address call risk exists | Analyze if external call address is controllable | major |

**3.7 FISCO BCOS Specific Security**

| Check Item | Method | Severity |
|--------|---------|--------|
| GM mode address length is 32 bytes | Analyze actual address type operations | major |
| Incompatible EVM opcodes used | Search for potentially incompatible low-level ops | major |
| Permission model considers node admission mechanism | Evaluate FISCO BCOS admission vs contract permission overlap | minor |
| Contract deploy permission considers developer permission migration | Evaluate permission settings in constructor | minor |

### 4. Produce Test Reports

#### JSON Report

Save to `{PROJECT_ROOT}/test-reports/{contractName}-security-report.json`:

```json
{
  "report_title": "{合约名} 安全测试报告",
  "contract": "{合约名}",
  "dimension": "security",
  "verdict": "PASS",
  "round": 1,
  "timestamp": "260506 1430",
  "summary": {
    "total_checks": 28,
    "passed": 27,
    "failed": 1,
    "warnings": 1
  },
  "failures": [
    {
      "id": "SEC-001",
      "severity": "blocker",
      "category": "重入攻击",
      "description": "{具体漏洞描述}",
      "file": "{文件路径}",
      "line": {行号},
      "suggestion": "{修复建议}"
    }
  ],
  "warnings": [
    {
      "id": "SEC-W001",
      "severity": "minor",
      "category": "FISCO BCOS 适配",
      "description": "{潜在兼容性建议}",
      "suggestion": "{建议}"
    }
  ]
}
```

**Verdict rules**:
- **PASS**: No blocker or major level security issues
- **FAIL**: Has blocker or major level security issues (1 blocker = FAIL)
- Only minor issues: verdict is PASS, but listed in warning field

#### Markdown Report

Also write `{PROJECT_ROOT}/test-reports/{contractName}-security-report.md`.

## Important Constraints

1. **Never modify contract code** — read-only, report-only
2. **SWC security standards reference** — align with SWC Registry (Smart Contract Weakness Classification) standards
3. **Blocker standard is strict** — any vulnerability that could cause asset loss or contract exploitation is a blocker
4. **Security testing is not a formal review** — think adversarially: "If I were an attacker, how would I attack this contract?"
5. **FISCO BCOS specific scenarios** — pay attention to security model differences in consortium chain scenarios (e.g. admission mechanism's impact on security assumptions)
6. **Write Agent ID after completion** — write your Agent ID to `{PROJECT_ROOT}/agent-registry/blockchain_test_sec.json`, format `{"id":"{yourID}","type":"bc_tester_security","updated":"{timestamp}"}`. This is the only way for master agent to resume you

## Tags

- domain: blockchain
- role: tester
- version: 2.0.0
