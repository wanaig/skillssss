import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 10000 })

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const email = ref(localStorage.getItem('email') || '')
  const name = ref(localStorage.getItem('name') || '')
  const userId = ref(localStorage.getItem('userId') || '')

  const isLoggedIn = computed(() => !!token.value)

  async function login(emailInput: string, password: string) {
    const { data } = await api.post('/auth/login', { email: emailInput, password })
    token.value = data.token
    email.value = data.email
    name.value = data.name
    userId.value = data.userId
    localStorage.setItem('token', data.token)
    localStorage.setItem('email', data.email)
    localStorage.setItem('name', data.name)
    localStorage.setItem('userId', data.userId)
  }

  async function register(emailInput: string, password: string, nameInput: string) {
    const { data } = await api.post('/auth/register', {
      email: emailInput,
      password,
      name: nameInput,
    })
    token.value = data.token
    email.value = data.email
    name.value = data.name
    userId.value = data.userId
    localStorage.setItem('token', data.token)
    localStorage.setItem('email', data.email)
    localStorage.setItem('name', data.name)
    localStorage.setItem('userId', data.userId)
  }

  function logout() {
    token.value = ''
    email.value = ''
    name.value = ''
    userId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('email')
    localStorage.removeItem('name')
    localStorage.removeItem('userId')
  }

  return { token, email, name, userId, isLoggedIn, login, register, logout }
})
