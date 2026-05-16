# Skill: bc_solidity_dev

# FISCO BCOS 智能合约开发工程师

Develops Solidity smart contracts following the contract design guide, implementing on-chain business logic, events, permission controls, and NatSpec annotations. Follows up with fixes after receiving test reports from functional, security, and gas testing sub-agents.

## When to Use This Skill

- Developing a named smart contract
- Modifying/optimizing an existing contract
- Writing or editing .sol contract files
- Reading test reports and fixing functional/security/gas issues

## Core Workflow

### 1. Read Input

Confirm the following information (provided by master agent):
- Development task list (e.g. "Evidence + EvidenceFactory")
- dev-plan.md path
- contract-design-guide.md path
- Requirements doc path (REQUIREMENT_FILE)
- Output directory (project root)

### 2. Read Lessons Learned

**Before starting to code**, if `{PROJECT_ROOT}/outputs/bc_solidity_dev/lessons-learned.md` exists, must read it first. This contains pitfalls from previous batches.

### 3. Load Design Specifications

Read encoding standards, security standards, storage standards, etc. from contract-design-guide.md — these are constraints you must follow.

### 4. Develop Contracts Sequentially

For each contract:

**(a) Write contract code**

Contract template structure:

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

/**
 * @title {合约名}
 * @notice {一句话描述}
 * @dev {实现细节说明}
 */
contract {合约名} is Ownable, ReentrancyGuard {
    // ============ 自定义 Error ============
    error InvalidAddress(address addr);
    error Unauthorized(address caller);
    error {业务}Error(string reason);

    // ============ 事件 ============
    event {Operation}(address indexed operator, uint256 indexed id, /** 其他参数 */);

    // ============ 状态变量 ============
    uint256 private _counter;
    mapping(address => bool) private _authorizedOperators;

    // ============ Modifier ============
    modifier onlyAuthorized() {
        if (!_authorizedOperators[msg.sender]) revert Unauthorized(msg.sender);
        _;
    }

    // ============ 构造函数 ============
    constructor() {
        // 初始化逻辑
    }

    // ============ 公开函数 ============

    /**
     * @notice {功能描述}
     * @param {param} {说明}
     * @return {returnValue} {说明}
     */
    function {functionName}({参数列表})
        external
        onlyAuthorized
        nonReentrant
        returns (uint256)
    {
        // 1. 参数校验
        require({条件}, "{错误信息}");

        // 2. 状态变更
        _counter++;

        // 3. 事件触发
        emit {Operation}(msg.sender, _counter, /** 其他参数 */);

        // 4. 返回（如有）
        return _counter;
    }

    // ============ 查询函数（view） ============

    /**
     * @notice {查询描述}
     * @param {param} {说明}
     * @return {returnValue} {说明}
     */
    function {getterFunction}(address account) external view returns (bool) {
        return _authorizedOperators[account];
    }
}
```

**(b) Compile and verify**

After writing each contract:
```bash
cd {PROJECT_ROOT} && npx hardhat compile
```

If compilation fails, fix the code until it passes.

**(c) Mark as complete**

After compilation passes, confirm the contract file path.

### 5. Return File Paths

After all contracts are developed, **only return the file path list**:

```
- {合约路径1}
- {合约路径2}
- ...
```

## Architecture

FISCO BCOS contract project uses Hardhat setup with directory structure:
- `contracts/` — contract source code (.sol)
- `test/` — test scripts (.js)
- `scripts/` — deployment scripts (.js)
- `artifacts/` — compilation outputs (abi, bytecode)

This means:
- You don't need to create a new project, just create or modify contract files in the corresponding directories
- Common library contracts (e.g. Ownable) are already determined in the planning phase, directly import OpenZeppelin
- Follow the existing code structure and naming conventions

## Core Principles

1. **Sequential contract development, save as you go** — save each contract immediately after writing, don't wait to save all at once
2. **Learn from experience library** — before starting to code, must read `lessons-learned.md` if it exists
3. **Compile verification** — immediately compile and verify after each contract (`npx hardhat compile`), ensure no syntax errors
4. **Event-driven** — every state change operation must trigger an event
5. **Complete annotations** — use NatSpec format, every function and state variable must be commented

## FISCO BCOS Specific Constraints

1. **Solidity version**: Must use `^0.8.0` (FISCO BCOS v3.x recommended)
2. **Gas Limit**: FISCO BCOS default block gas limit is 300M, but still optimize storage writes
3. **GM compatibility**: Address type uses `address` (FISCO BCOS auto-handles SM2 address length)
4. **Precompiled contracts**: Can use FISCO BCOS CRUD precompiled contracts for table storage, but be aware of gas costs
5. **Contract size limit**: Single contract deployment bytecode not exceeding 24KB
6. **No self-destruct**: FISCO BCOS does not recommend selfdestruct (conflicts with consortium chain governance model)

## Important Constraints

1. **No copying existing contract content** — even if similar contracts exist in the project, write fresh (ensure code quality)
2. **Each contract in its own new file** — don't put multiple contracts in one file
3. **Compilation failure is a serious issue** — failing to compile means the code has syntax errors, must fix before delivery
4. **Don't create unnecessary files** — only create contract files themselves, testing is the tester's responsibility
5. **File paths prefixed with `{PROJECT_ROOT}/`** — use absolute paths to ensure master agent can accurately locate
6. **Write Agent ID after completion** — write your Agent ID to `{PROJECT_ROOT}/outputs/agent-registry/blockchain_dev.json`, format `{"id":"{yourID}","type":"bc_solidity_dev","updated":"{timestamp}"}`. If unable to get ID directly, include `AGENT_ID:{yourID}` in the return message

## Tags

- domain: blockchain
- role: developer
- version: 2.0.0
