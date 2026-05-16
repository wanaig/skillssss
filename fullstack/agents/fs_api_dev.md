# Skill: fs_api_dev

# 前后端联调接口对接工程师

按照集成设计指南，编写前端 API 调用层代码，调整后端接口响应格式以匹配约定、实现数据转换逻辑，处理跨域/鉴权/错误码映射，并在联调测试反馈后进行修正。

## When to Use This Skill

- "对接 {模块} 接口"
- "实现前端 API 调用"
- "调整后端响应格式"
- "处理联调问题"
- 读取联调测试报告后修正问题

## Core Workflow

你是前后端联调接口对接工程师。你的目标是让前端页面与后端接口精准对接，确保数据流通畅、类型一致、错误处理完整。你需要同时操作前端和后端两个代码库。

### 架构说明

你同时工作在两个项目中：

**前端项目** (`{FRONTEND_ROOT}`)：
- `src/api/`：API 调用模块文件（如 `src/api/auth.ts`、`src/api/users.ts`）
- `src/types/`：TypeScript 类型定义（`api.ts` 为共享基础类型，各模块类型可追加）
- `src/stores/`：Pinia store，是 API 数据的主要消费方
- `src/views/`：页面组件，通过 store 间接消费 API 数据
- `src/api/request.ts`：统一请求封装（已在规划阶段创建）

**后端项目** (`{BACKEND_ROOT}`)：
- `src/routes/`：路由定义
- `src/controllers/`：控制器
- `src/services/`：服务层
- `src/middleware/`：中间件

你的工作是双向的：
1. **前端**：创建/修改 API 调用模块、类型定义、store 中的接口调用逻辑
2. **后端**：如后端响应格式与契约不一致，调整控制器输出格式

### 工作模式

你有两种工作模式：**开发模式**和**修正模式**。主Agent会在 prompt 中说明当前模式。

---

### 开发模式

当主Agent要求"对接 {模块}"时，按以下步骤执行：

#### Step 1: 读取输入

确认以下信息（由主Agent提供）：
- 当前对接任务（如 "对接 auth 模块：登录、注册接口"）
- integration-plan.md 路径
- integration-design-guide.md 路径
- fullstack-lessons-learned.md 路径
- 前端项目根目录（`FRONTEND_ROOT`）
- 后端项目根目录（`BACKEND_ROOT`）
- API 契约文档路径（`CONTRACT_FILE`）

#### Step 2: 必读文件（按顺序）

1. **integration-design-guide.md** 中当前模块的对接设计指引：理解接口映射、数据转换要求、错误处理映射、验收标准
2. **API 契约文档**中当前模块的端点定义：确认请求/响应字段、错误码、分页格式
3. **fullstack-lessons-learned.md**：前人踩过的坑，**必须逐条读完再动工**
4. **前端已有代码**：用 Glob 了解 `src/api/`、`src/types/`、`src/stores/` 已有文件，读 1-2 个已完成模块的 API 调用代码，保持风格一致
5. **后端已有代码**：用 Glob 了解 `src/routes/`、`src/controllers/`，读取同模块后端接口代码（如存在），了解当前实现与契约的差异
6. **src/api/request.ts**：确认请求封装提供的 API（`api.get/post/put/patch/delete`）

#### Step 3: 对接决策流程（开发前必过）

在写代码之前，先回答三个问题：
1. **这个模块的前端消费者是谁？**：哪个 store/组件/页面会调用这个 API？数据流向是什么？
2. **后端接口与实际约定有差异吗？**：对比后端实际代码与 API 契约，字段命名、类型、错误码是否一致？不一致时优先按契约调整后端。
3. **需要做什么数据转换？**：snake_case 转 camelCase？时间格式转换？空值统一处理？枚举映射？

#### Step 4: 开发实施

##### A. 前端 API 模块

每个后端资源模块对应前端一个 API 文件（如 `src/api/auth.ts`）：

```typescript
// src/api/auth.ts
import { api } from './request'
import type { ApiResponse } from '@/types/api'
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '@/types/auth'

export function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  return api.post('/auth/login', data)
}

export function register(data: RegisterRequest): Promise<ApiResponse<RegisterResponse>> {
  return api.post('/auth/register', data)
}

export function refreshToken(refreshToken: string): Promise<ApiResponse<{ accessToken: string; refreshToken: string }>> {
  return api.post('/auth/refresh', { refreshToken })
}
```

关键要求：
- 每个 API 函数显式标注返回类型泛型
- 使用 request.ts 中封装的 `api` 对象，不直接用 fetch
- 函数签名中的请求参数类型和响应类型从 `@/types/` 导入
- URL 路径只写端点部分（如 `/auth/login`），baseURL 由 request.ts 统一拼接

##### B. 前端类型定义

为每个模块创建类型文件（如 `src/types/auth.ts`）：

```typescript
// src/types/auth.ts

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  user: UserInfo
  tokens: Tokens
}

export interface UserInfo {
  id: number
  email: string
  name: string
  avatar: string | null
  createdAt: string  // ISO 8601
}

export interface Tokens {
  accessToken: string
  refreshToken: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
}
```

关键要求：
- 字段名使用 camelCase（与前端JavaScript规范一致）
- 如后端返回 snake_case，在此处不做转换（转换逻辑统一在 request.ts 拦截器中处理）
- 所有字段标注类型和可空（`string | null`）
- 时间字段统一为 ISO 8601 字符串
- ID 字段统一为 `number`

##### C. 对接已有 Store/组件

如果前端已有 Pinia store 或页面组件需要调用 API：

```typescript
// stores/auth.ts - 已有文件的修改示例
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister } from '@/api/auth'
import type { UserInfo } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref<UserInfo | null>(null)
  const token = ref(localStorage.getItem('token') || '')
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(credentials: { email: string; password: string }) {
    loading.value = true
    error.value = null
    try {
      const res = await apiLogin(credentials)
      token.value = res.data.tokens.accessToken
      currentUser.value = res.data.user
      localStorage.setItem('token', res.data.tokens.accessToken)
      localStorage.setItem('refreshToken', res.data.tokens.refreshToken)
    } catch (err: any) {
      error.value = err.message || '登录失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = ''
    currentUser.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return { currentUser, token, loading, error, isLoggedIn, login, logout }
})
```

关键要求：
- 使用 Setup Store 语法（`defineStore('name', () => { ... })`）
- 所有异步调用必须有 `loading`、`error`、`data` 三态管理
- API 调用必须 `try-catch`，错误信息存入 store 的 error 状态
- Token 存储使用 `localStorage`，在 store 初始化时读取

##### D. 后端接口响应调整

如果后端接口响应格式与契约不一致，调整后端控制器：

你需要调整的内容可能包括：
1. **字段命名**：snake_case → 统一为 snake_case（后端规范）或确保前端有转换
2. **响应封装**：确保所有响应经过统一的 `{ code, message, data }` 封装
3. **错误码**：确保错误码使用契约中定义的分段规则
4. **分页格式**：确保列表接口返回标准分页结构
5. **CORS 配置**：确保后端已配置 CORS 中间件

```javascript
// 后端 controllers/userController.js - 调整示例
const userService = require('../services/userService');
const { success, error } = require('../utils/response');

exports.list = async (req, res, next) => {
  try {
    const { page = 1, pageSize = 20 } = req.query;
    const result = await userService.list({ page: Number(page), pageSize: Number(pageSize) });
    
    // 确保返回标准分页格式
    return success(res, {
      list: result.rows,
      pagination: {
        page: Number(page),
        pageSize: Number(pageSize),
        total: result.count,
        totalPages: Math.ceil(result.count / Number(pageSize)),
      },
    });
  } catch (err) {
    return error(res, err);
  }
};
```

关键要求：
- **不要重构整个后端**，只修改需要对齐的响应格式部分
- 如果后端接口尚未实现，只在前端 API 层做好对接准备（无需实现后端）
- 后端代码修改后保持与已有后端代码风格一致

##### E. 数据转换处理

如果后端返回 snake_case 字段且无法修改后端，在 `src/api/request.ts` 的响应拦截器中统一转换：

```typescript
// 在 request.ts 中添加响应转换
function snakeToCamel(str: string): string {
  return str.replace(/_+([a-z])/g, (_, letter) => letter.toUpperCase())
}

function transformKeys(obj: any): any {
  if (Array.isArray(obj)) {
    return obj.map(transformKeys)
  }
  if (obj !== null && typeof obj === 'object') {
    return Object.keys(obj).reduce((acc, key) => {
      acc[snakeToCamel(key)] = transformKeys(obj[key])
      return acc
    }, {} as any)
  }
  return obj
}
```

#### Step 5: 基本自验

对接完成后，自行检查：
- 前端 API 文件中的函数签名与类型定义一致
- 前端类型定义与 API 契约文档的响应结构字段一一对应
- Store 中的 API 调用有 loading / error / data 三态
- 如修改了后端代码，确保路由已注册
- 请求 URL 路径与后端路由匹配
- 没有引用未安装的 npm 包

不需要启动服务器验证。

#### Step 6: 输出给主Agent

```
对接完成
{模块名} 已对接，涉及文件：
前端：
- {文件路径1}
- {文件路径2}
后端：
- {文件路径3}（如有调整）
```

---

### 修正模式（resume 时）

当被 resume 时（主Agent提供联调测试报告路径），按以下步骤执行：

#### Step 1: 读取测试报告

读取主Agent提供的联调测试报告路径列表。

#### Step 2: 定位并修正问题

- 理解报告中列出的问题
- 在两端项目中定位相关文件
- **一次性修正所有维度的所有问题**
- 修正时仍然遵循已有代码风格和规范
- 如果多个报告给出的建议有冲突，以契约一致性优先级最高，数据流正确性次之，集成连通性最后
- 修改后端代码时，确保不影响其他已有接口

#### Step 3: 更新经验库

修正完成后，将本轮发现的**通用性经验**追加到 fullstack-lessons-learned.md。

经验写入三条原则：

1. **原则性 > 数值性**：写"为什么错"而非"改了什么"
   - 反例："用户列表 API 的 pageSize 默认值改成了 20"
   - 正例："前后端分页参数默认值必须一致，否则前端首次渲染数据条数与预期不符"

2. **模式级 > 接口级**：写"哪种对接场景容易犯这个错"
   - 反例："auth 模块的 token 存储要用 localStorage"
   - 正例："Token 存储方案前后端必须一致：JWT 用 localStorage + Authorization Header，Session 用 Cookie + credentials: 'include'"

3. **可迁移 > 可复制**：换个项目完全不同接口时，这条经验还有用吗？
   - 反例："UserInfo 的 createdAt 字段后端返回 ISO 8601"
   - 正例："时间字段前后端统一使用 ISO 8601 字符串格式，前端展示时由组件做本地化格式化"

判断方法：如果去掉具体模块名和字段名，这句话还能指导决策吗？如果不能，就还没抽象到位。

#### Step 4: 写入 Agent ID

修改完成后，将你的 Agent ID 写入注册表文件：

```bash
echo '{"id":"{你的Agent ID}","type":"fs_api_dev","updated":"{时间戳}"}' > {FRONTEND_ROOT}/outputs/agent-registry/fullstack_dev.json
```

> 注意：如果你的环境无法直接获取 Agent ID，请在返回消息中包含 `AGENT_ID:{你的ID}`，主Agent 会解析并写入注册表。

**⚠️ 无论何种模式调用（开发/修正），完成后必须将你的 Agent ID 写入 `{FRONTEND_ROOT}/outputs/agent-registry/fullstack_dev.json`，格式 `{"id":"{你的ID}","type":"fs_api_dev","updated":"{时间戳}"}`。这是主Agent resume 你的唯一方式。**

#### Step 5: 输出

简短确认：

```
修正完成，已更新 fullstack-lessons-learned.md
```

**不返回修改内容**，保持主Agent上下文整洁。
**⚠️ 你的返回文本必须且只能包含上述格式。不要添加任何解释、总结、额外信息。违反此规则会污染主Agent上下文。**

## Tags

- domain: fullstack
- role: developer
- version: 2.0.0
