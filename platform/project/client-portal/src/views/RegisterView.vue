<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  if (!email.value || !password.value) {
    error.value = '请填写邮箱和密码'
    return
  }
  if (password.value.length < 6) {
    error.value = '密码至少6位'
    return
  }
  loading.value = true
  try {
    await auth.register(email.value, password.value, name.value || email.value.split('@')[0])
    router.push('/projects')
  } catch (e: any) {
    error.value = e.response?.data?.error || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>注册</h2>
      <p class="auth-sub">创建账号以提交和管理项目</p>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>昵称</label>
          <input v-model="name" type="text" placeholder="可选，默认使用邮箱前缀" :disabled="loading" />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="email" type="email" placeholder="your@email.com" :disabled="loading" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="至少6位" :disabled="loading" />
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <p class="auth-switch">
        已有账号？
        <router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.auth-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 400px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.auth-card h2 {
  font-size: 22px;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.auth-sub {
  font-size: 14px;
  color: #999;
  margin-bottom: 28px;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #e94560;
}

.error-msg {
  color: #d32f2f;
  font-size: 14px;
  margin-bottom: 12px;
}

.btn-primary {
  width: 100%;
  background: #e94560;
  color: #fff;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:disabled {
  background: #f0a0b0;
  cursor: not-allowed;
}

.btn-primary:not(:disabled):hover {
  background: #d63850;
}

.auth-switch {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #999;
}

.auth-switch a {
  color: #e94560;
  text-decoration: none;
  font-weight: 600;
}
</style>
