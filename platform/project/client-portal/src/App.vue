<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="app-container">
    <header class="app-header">
      <router-link to="/" class="app-logo">Harness Engineering</router-link>
      <nav class="app-nav">
        <template v-if="auth.isLoggedIn">
          <router-link to="/projects">项目列表</router-link>
          <router-link to="/new">提交需求</router-link>
        </template>
        <template v-else>
          <router-link to="/">首页</router-link>
        </template>
      </nav>
      <div class="app-user">
        <template v-if="auth.isLoggedIn">
          <span class="user-name">{{ auth.name || auth.email }}</span>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-login">登录</router-link>
          <router-link to="/register" class="btn-register">注册</router-link>
        </template>
      </div>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f5f7fa;
  color: #2c3e50;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #1a1a2e;
  color: #fff;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.app-logo {
  color: #e94560;
  font-weight: 700;
  font-size: 18px;
  text-decoration: none;
}

.app-nav {
  display: flex;
  gap: 16px;
  flex: 1;
}

.app-nav a {
  color: #a0a0b0;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.app-nav a:hover,
.app-nav a.router-link-active {
  color: #fff;
}

.app-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 13px;
  color: #ccc;
}

.btn-logout {
  background: none;
  border: 1px solid #555;
  color: #aaa;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  border-color: #e94560;
  color: #e94560;
}

.btn-login {
  color: #ccc;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.btn-login:hover {
  color: #fff;
}

.btn-register {
  background: #e94560;
  color: #fff;
  text-decoration: none;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  transition: background 0.2s;
}

.btn-register:hover {
  background: #d63850;
}

.app-main {
  flex: 1;
  padding: 24px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}
</style>
