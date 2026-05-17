<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProjects, deleteProject } from '@/api'

interface Project {
  projectId: string
  name: string
  status: string
  createdAt: string
  phases: Record<string, string>
}

const router = useRouter()
const projects = ref<Project[]>([])
const loading = ref(true)
const error = ref('')

const statusLabel: Record<string, string> = {
  intake: '需求采集中',
  pending: '等待中',
  in_progress: '进行中',
  completed: '已完成',
  failed: '失败',
  delivered: '已交付',
}

const statusColor: Record<string, string> = {
  intake: '#e94560',
  pending: '#999',
  in_progress: '#42a5f5',
  completed: '#66bb6a',
  failed: '#ef5350',
  delivered: '#7b1fa2',
}

function progressPercent(phases: Record<string, string>): number {
  const entries = Object.entries(phases)
  const done = entries.filter(([, s]) => s === 'completed' || s === 'failed').length
  const total = entries.filter(([, s]) => s !== 'skipped').length
  return total > 0 ? Math.round((done / total) * 100) : 0
}

async function handleDelete(projectId: string, event: Event) {
  event.stopPropagation()
  if (!confirm('确认删除此项目？所有相关文件将被永久删除。')) return
  try {
    await deleteProject(projectId)
    projects.value = projects.value.filter(p => p.projectId !== projectId)
  } catch {
    alert('删除失败，请重试')
  }
}

const phaseLabel: Record<string, string> = {
  intake: '需求',
  architecture: '架构',
  backend: '后端',
  frontend: '前端',
  flutter: '移动',
  blockchain: '区块',
  fullstack: '集成',
  deploy: '部署',
  delivery: '交付',
}

onMounted(async () => {
  try {
    projects.value = await getProjects()
  } catch {
    error.value = '加载失败，请确认 API 服务已启动'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="project-list">
    <div class="list-header">
      <h2>项目列表</h2>
      <button class="btn-primary" @click="router.push('/new')">+ 新建项目</button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="sk-card" v-for="n in 3" :key="n">
        <div class="sk-line sk-title"></div>
        <div class="sk-line sk-sub"></div>
        <div class="sk-line sk-bar"></div>
      </div>
    </div>

    <div v-else-if="error" class="empty-state">
      <span class="empty-icon">⚠️</span>
      <p>{{ error }}</p>
    </div>

    <div v-else-if="projects.length === 0" class="empty-state">
      <span class="empty-icon">📋</span>
      <p>暂无项目</p>
      <p class="empty-hint">点击"新建项目"提交您的第一个需求</p>
    </div>

    <div v-else class="project-cards">
      <div
        v-for="project in projects"
        :key="project.projectId"
        class="project-card"
        @click="router.push(`/projects/${project.projectId}`)"
      >
        <div class="card-top">
          <div class="card-info">
            <h3>{{ project.name }}</h3>
            <p class="card-date">
              创建于 {{ new Date(project.createdAt).toLocaleDateString('zh-CN') }}
              · {{ progressPercent(project.phases) }}% 完成
            </p>
          </div>
          <span
            class="status-badge"
            :style="{ background: statusColor[project.status] || '#999' }"
          >
            {{ statusLabel[project.status] || project.status }}
          </span>
          <button
            class="btn-delete"
            @click="(e) => handleDelete(project.projectId, e)"
            title="删除项目"
          >✕</button>
        </div>

        <!-- Mini progress bar -->
        <div class="mini-progress-track">
          <div
            class="mini-progress-fill"
            :style="{ width: progressPercent(project.phases) + '%' }"
          ></div>
        </div>

        <!-- Compact phase dots -->
        <div class="phase-strip">
          <span
            v-for="(phaseStatus, phase) in project.phases"
            :key="phase"
            class="phase-tag"
            :class="`pt-${phaseStatus}`"
          >
            {{ phaseLabel[phase] || phase }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.list-header h2 {
  font-size: 24px;
  color: #1a1a2e;
}

.btn-primary {
  background: #e94560;
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #d63850;
}

/* Skeleton */
.loading-state { display: flex; flex-direction: column; gap: 12px; }
.sk-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
}
.sk-line {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 10px;
}
.sk-title { width: 60%; height: 18px; }
.sk-sub { width: 40%; height: 14px; }
.sk-bar { width: 100%; height: 6px; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Empty / error */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}
.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-hint { font-size: 14px; margin-top: 8px; color: #bbb; }

/* Cards */
.project-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.15s;
}

.project-card:hover {
  box-shadow: 0 6px 16px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.card-info h3 {
  font-size: 16px;
  margin-bottom: 4px;
  color: #1a1a2e;
}

.card-date {
  font-size: 13px;
  color: #999;
}

.status-badge {
  padding: 5px 14px;
  border-radius: 14px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.btn-delete {
  background: none;
  border: none;
  color: #ccc;
  font-size: 16px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
  margin-left: 4px;
}

.btn-delete:hover {
  color: #e94560;
  background: #fef0f2;
}

/* Mini progress */
.mini-progress-track {
  height: 4px;
  background: #eee;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 10px;
}

.mini-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #e94560, #f0738a);
  border-radius: 2px;
  transition: width 0.5s ease;
}

/* Phase tags */
.phase-strip {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.phase-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  background: #f5f5f5;
  color: #bbb;
}

.pt-pending { background: #f5f5f5; color: #bbb; }
.pt-in_progress { background: #e3f2fd; color: #1976d2; }
.pt-completed { background: #e8f5e9; color: #388e3c; }
.pt-failed { background: #fbe9e7; color: #d32f2f; }
.pt-skipped { background: transparent; color: #ccc; border: 1px dashed #ddd; }
</style>
