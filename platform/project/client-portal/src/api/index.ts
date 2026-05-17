import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// Attach JWT token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Redirect to login on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth ────────────────────────────────────────────────────

export async function authLogin(email: string, password: string) {
  const { data } = await api.post('/auth/login', { email, password })
  return data
}

export async function authRegister(email: string, password: string, name: string) {
  const { data } = await api.post('/auth/register', { email, password, name })
  return data
}

// ── Projects ────────────────────────────────────────────────

export async function getProjectStatus(projectId: string) {
  const { data } = await api.get(`/projects/${projectId}/status`)
  return data
}

export async function submitProject(input: { description: string; constraints?: Record<string, string> }) {
  const { data } = await api.post('/projects', input)
  return data
}

export async function getProjects() {
  const { data } = await api.get('/projects')
  return data
}

export async function getProjectLog(projectId: string) {
  const { data } = await api.get(`/projects/${projectId}/log`)
  return data
}

export async function getPrd(projectId: string) {
  const { data } = await api.get(`/projects/${projectId}/prd`)
  return data
}

export async function deleteProject(projectId: string) {
  await api.delete(`/projects/${projectId}`)
}

export async function getAcceptanceReport(projectId: string) {
  const { data } = await api.get(`/projects/${projectId}/report`)
  return data
}
