# Skill: bc_planner

# FISCO BCOS 区块链项目计划与基础设施工程师

Reads requirements documents and architecture design documents, formulates smart contract development plans and design guides, and sets up Solidity project infrastructure (Hardhat + FISCO BCOS configuration) so that downstream bc_solidity_dev sub-agents can start development immediately.

## When to Use This Skill

- Formulating a blockchain development plan
- Setting up a FISCO BCOS contract project
- Creating development plans and engineering foundations for blockchain requirements

## Core Workflow

### 1. Read Input

Confirm the following information (provided by master agent):
- Requirements doc path (REQUIREMENT_FILE)
- Tech stack doc path (TECH_STACK_FILE)
- Data architecture doc path (DATA_ARCHITECTURE_FILE)
- API contract doc path (CONTRACT_FILE)
- Security architecture doc path (SECURITY_FILE)
- Implementation roadmap path (IMPLEMENTATION_ROADMAP_FILE)
- Output directory (PROJECT_ROOT)

### 2. FISCO BCOS On-Chain Decision Analysis

Blockchain is not a silver bullet — not all data should go on-chain. Analyze requirements and make explicit on-chain decisions.

**Scenarios suitable for on-chain**:
- Data shared between multiple organizations with mutual distrust (evidence, traceability)
- Business records requiring public verifiability (audit logs, asset flows)
- Multi-party collaborative workflows (approval, reconciliation, settlement)
- Digital asset ownership and transfer (points, credentials, NFTs)

**Scenarios NOT suitable for on-chain**:
- Single-party pure business data (user profiles, preferences)
- High-frequency, large-volume data (real-time messages, image files)
- Data requiring frequent modification without audit needs (drafts, caches)
- Privacy-sensitive data without trusted execution environments (plaintext on-chain)

**On-chain decision template** (write at the beginning of contract-design-guide.md):

| Business Module | On-Chain Decision | On-Chain Storage | Off-Chain Storage | Rationale |
|---------|---------|---------|---------|---------|
| {Module 1} | On-chain/Off-chain/Hybrid | {field list} | {field list} | {reason} |

### 3. Produce dev-plan.md

dev-plan.md content structure:

```markdown
# FISCO BCOS 智能合约开发计划

## 项目技术基线
- 区块链平台：FISCO BCOS v3.x
- 合约语言：Solidity ^0.8.0
- 开发框架：Hardhat
- SDK 语言：Java（Spring Boot 后端集成）/ Node.js（Hardhat 部署脚本）
- 共识机制：PBFT
- 账户模型：国密 SM2 / ECDSA

## 合约清单

| # | 合约名 | 所属模块 | 描述 | 预估行数 | 优先级 | 依赖 | 状态 |
|---|--------|---------|------|---------|--------|------|------|
| 1 | {合约名} | {模块名} | {一句话描述} | ~{N}行 | P0 | {依赖其他合约} | ⏳ |
| 2 | {合约名} | {模块名} | {一句话描述} | ~{N}行 | P0 | {依赖} | ⏳ |
| ... | ... | ... | ... | ... | ... | ... | ⏳ |

## 依赖关系
- {合约B} 依赖 {合约A} 的接口
- {合约C} 需要部署 {合约A} 的地址作为构造参数

## 建议开发顺序
1. 基础库合约（Ownable, AccessControl 等）
2. 核心业务合约（按依赖顺序）
3. 工厂/代理合约

## 批量分组（BATCH_SIZE={N}）
Batch 1: {合约1, 合约2}
Batch 2: ...
```

### 4. Produce contract-design-guide.md

contract-design-guide.md content structure:

```markdown
# FISCO BCOS 合约设计指南

## 1. 编码规范
- Solidity 版本：^0.8.0
- 命名规范：合约名 PascalCase，函数名 camelCase，常量 UPPER_CASE
- 必须使用 NatSpec 注释格式（@notice, @param, @return, @dev）
- 每个函数必须定义事件并在关键状态变更后触发
- 使用自定义 error 替代 revert string（节省 Gas）

## 2. 安全规范
- 必须使用 OpenZeppelin 库（如非定制，优先继承而非重复实现）
- 权限检查使用 modifier，避免在函数体内手写 require
- 转账/支付类函数必须防重入（ReentrancyGuard）
- 禁止使用 tx.origin 做身份验证
- 整型运算使用 SafeMath 或 Solidity 0.8+ 内置溢出检查
- 地址参数必须校验非零地址

## 3. 存储规范
- 状态变量按数据类型紧凑排列（节约存储槽）
- 大数组/映射必须设计分页查询
- 历史数据使用事件而非状态存储
- 合理使用 mapping 替代 array

## 4. FISCO BCOS 特约规范
- 支持国密算法（SM2/SM3），地址类型兼容 address
- 使用 FISCO BCOS 的预编译合约（如表存储 CRUD）
- 正确设置 gas limit，FISCO BCOS 区块 gas 上限默认 300M
- 合约部署时考虑 FISCO BCOS 的权限模型（部署者即管理员）

## 5. 接口规范
- 所有公开函数必须在合约头部声明接口（interface）
- 返回数据采用结构体或基础类型
- 禁止返回动态数组作为外部调用返回值（会导致 ABI 编码问题）

## 6. 事件规范
- 每个状态变更操作必须触发对应事件
- 事件参数包含操作者地址（msg.sender）
- 关键操作的事件必须 indexed 关键字段

## 7. 燃耗优化
- 减少存储写入次数（SSTORE 是最贵的操作码）
- 循环中使用 memory 变量缓存 storage 引用
- 使用 uint256 作为默认整数类型（EVM 原生处理宽度）
- 避免在链上做复杂计算——计算逻辑放链下，链上只存储结果
```

### 5. Set Up Project Infrastructure

Use Bash to perform the following initialization:

```bash
# Create Hardhat project structure
mkdir -p {PROJECT_ROOT}/contracts
mkdir -p {PROJECT_ROOT}/test
mkdir -p {PROJECT_ROOT}/scripts
mkdir -p {PROJECT_ROOT}/artifacts

# Initialize npm project
cd {PROJECT_ROOT} && npm init -y

# Install dependencies
npm install --save-dev hardhat @nomiclabs/hardhat-waffle @nomiclabs/hardhat-ethers ethers @openzeppelin/contracts @fisco-bcos/api

# Create hardhat.config.js (FISCO BCOS adapted)
```

**hardhat.config.js template**:

```javascript
require("@nomiclabs/hardhat-waffle");
require("@nomiclabs/hardhat-ethers");

module.exports = {
  solidity: {
    version: "0.8.19",
    settings: {
      optimizer: { enabled: true, runs: 200 },
    },
  },
  networks: {
    fisco: {
      url: process.env.FISCO_NODE_URL || "http://127.0.0.1:8545",
      chainId: 1,
      gas: 300000000,
      gasPrice: 1,
      groupId: 1,
      accounts: process.env.PRIVATE_KEY ? [process.env.PRIVATE_KEY] : [],
    },
  },
  paths: {
    sources: "./contracts",
    tests: "./test",
    cache: "./cache",
    artifacts: "./artifacts",
  },
};
```

### 6. Return File Paths

After all outputs are complete, **only return the file path list**, not file contents:

```
- dev-plan: {PROJECT_ROOT}/dev-plan.md
- contract-design-guide: {PROJECT_ROOT}/contract-design-guide.md
- 项目基础设施：{PROJECT_ROOT}/（含 contracts/, test/, scripts/ 目录 + hardhat.config.js + package.json）
```

## Core Principles

**Progressive write, save as you go**: Never write large files in one shot. All output files must be completed step by step, writing and saving each file immediately. This:
- Avoids single-output-too-large hanging
- Provides clear checkpoints after each step
- Ensures saved files aren't lost even if mid-process failure

**Execution order**: 1. Read input → 2. Generate dev-plan.md → 3. Generate contract-design-guide.md → 4. Build project scaffolding → 5. Return file path list

## Important Constraints

1. **Step-by-step execution**: Don't generate all files at once, create them individually in order and save immediately
2. **Return only paths**: Don't return file contents to master agent (master agent doesn't read content)
3. **Contract granularity**: Each contract 80-200 lines recommended, complex contracts can be appropriately relaxed
4. **FISCO BCOS compatibility**: Don't use EVM features unsupported by FISCO BCOS (e.g. Shanghai fork PUSH0)
5. **IDE compatibility**: hardhat.config.js path configuration ensures normal IDE code completion

## Tags

- domain: blockchain
- role: planner
- version: 2.0.0
