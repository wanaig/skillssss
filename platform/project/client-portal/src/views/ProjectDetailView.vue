<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getProjectStatus, getProjectLog, getAcceptanceReport, deleteProject } from '@/api'
import { useRouter } from 'vue-router'
import ReportView from '@/components/ReportView.vue'

const props = defineProps<{ id: string }>()
const router = useRouter()

interface ProjectStatus {
  projectId: string
  name: string
  status: string
  phases: Record<string, string>
  createdAt: string
  updatedAt: string
}

const status = ref<ProjectStatus | null>(null)
const logLines = ref<string[]>([])
const reportContent = ref('')
const loading = ref(true)
const error = ref('')
let pollTimer: ReturnType<typeof setInterval> | null = null

const phaseLabel: Record<string, string> = {
  intake: '需求采集',
  architecture: '架构设计',
  backend: '后端开发',
  frontend: '前端开发',
  flutter: '移动端开发',
  blockchain: '区块链开发',
  fullstack: '前后端集成',
  deploy: '部署上线',
  delivery: '交付验收',
}

const statusLabel: Record<string, string> = {
  intake: '需求采集中',
  pending: '等待中',
  in_progress: '进行中',
  completed: '已完成',
  failed: '失败',
  skipped: '已跳过',
  delivered: '已交付',
}

const statusColor: Record<string, string> = {
  intake: '#e94560',
  pending: '#999',
  in_progress: '#42a5f5',
  completed: '#66bb6a',
  failed: '#ef5350',
  skipped: '#bdbdbd',
  delivered: '#7b1fa2',
}

const progressPercent = computed(() => {
  if (!status.value) return 0
  const phases = Object.entries(status.value.phases)
  const done = phases.filter(([, s]) => s === 'completed' || s === 'failed').length
  const total = phases.filter(([, s]) => s !== 'skipped').length
  return total > 0 ? Math.round((done / total) * 100) : 0
})

const delivered = computed(() => status.value?.status === 'delivered')
const isIntake = computed(() => status.value?.status === 'intake')

async function handleDelete() {
  if (!confirm('确认删除此项目？所有相关文件将被永久删除。')) return
  try {
    await deleteProject(props.id)
    router.push('/projects')
  } catch {
    alert('删除失败')
  }
}

async function fetchStatus() {
  try {
    status.value = await getProjectStatus(props.id)
    error.value = ''

    if (delivered.value) {
      // Load log
      try {
        const logData = await getProjectLog(props.id)
        logLines.value = logData.lines || []
      } catch { /* log unavailable */ }

      // Load report
      try {
        const report = await getAcceptanceReport(props.id)
        reportContent.value = report?.content || ''
      } catch { /* report unavailable */ }

      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
      }
    }
  } catch {
    error.value = '无法加载项目信息，请确认项目 ID 正确'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatus()
  pollTimer = setInterval(fetchStatus, 5000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="project-detail">
    <!-- Loading skeleton -->
    <template v-if="loading">
      <div class="skeleton skeleton-title"></div>
      <div class="skeleton skeleton-bar"></div>
      <div class="skeleton skeleton-card" style="height:160px"></div>
      <div class="skeleton skeleton-card" style="height:200px"></div>
    </template>

    <!-- Error -->
    <div v-else-if="error" class="error-state">
      <span class="error-icon">⚠️</span>
      <p>{{ error }}</p>
    </div>

    <!-- Content -->
    <template v-else-if="status">
      <!-- Header -->
      <div class="detail-header">
        <div>
          <h2>{{ status.name }}</h2>
          <p class="project-meta">
            ID: {{ status.projectId.slice(0, 8) }}
            · 创建于 {{ new Date(status.createdAt).toLocaleDateString('zh-CN') }}
          </p>
        </div>
        <div class="header-actions">
          <button v-if="isIntake" class="btn-intake" @click="router.push(`/projects/${props.id}/confirm`)">
            📋 确认需求
          </button>
          <span class="status-badge" :style="{ background: statusColor[status.status] || '#999' }">
            {{ statusLabel[status.status] || status.status }}
          </span>
          <button class="btn-delete-detail" @click="handleDelete" title="删除项目">🗑</button>
        </div>
      </div>

      <!-- Progress bar -->
      <section class="section progress-section">
        <div class="progress-header">
          <span>整体进度</span>
          <span class="progress-pct">{{ progressPercent }}%</span>
        </div>
        <div class="progress-track">
          <div
            class="progress-fill"
            :style="{ width: progressPercent + '%' }"
          ></div>
        </div>
        <div class="phase-grid">
          <div
            v-for="(phaseStatus, phase) in status.phases"
            :key="phase"
            class="phase-cell"
            :class="`ph-${phaseStatus}`"
          >
            <span class="phase-indicator" :style="{ background: statusColor[phaseStatus] || '#e0e0e0' }"></span>
            <span class="phase-name">{{ phaseLabel[phase] || phase }}</span>
          </div>
        </div>
      </section>

      <!-- Timeline -->
      <section v-if="logLines.length" class="section">
        <h3 class="section-title">📋 开发时间线</h3>
        <div class="timeline">
          <div v-for="(line, idx) in logLines" :key="idx" class="timeline-line">
            <span class="tl-dot"></span>
            {{ line }}
          </div>
        </div>
      </section>

      <!-- Acceptance Report -->
      <section v-if="delivered && reportContent" class="section report-section">
        <h3 class="section-title">📄 验收报告</h3>
        <ReportView :content="reportContent" />
      </section>

      <!-- Not yet delivered -->
      <section v-else-if="!delivered && !loading" class="section tip-section">
        <p>⏳ 项目开发进行中，每 5 秒自动刷新进度。开发完成后将展示验收报告。</p>
      </section>
    </template>
  </div>
</template>

<style scoped>
.project-detail {
  max-width: 900px;
}

/* ── Skeleton loading ─────────────────────────────────────── */
.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8px;
  margin-bottom: 16px;
}

.skeleton-title { height: 32px; width: 60%; }
.skeleton-bar { height: 48px; }
.skeleton-card { height: 160px; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ── Header ───────────────────────────────────────────────── */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.detail-header h2 {
  font-size: 24px;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.project-meta {
  font-size: 13px;
  color: #999;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge {
  padding: 8px 20px;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.btn-intake {
  background: #fff3e0;
  color: #e65100;
  border: 1px solid #ffcc80;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-intake:hover {
  background: #ffe0b2;
}

.btn-delete-detail {
  background: none;
  border: 1px solid #eee;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  color: #ccc;
  transition: all 0.2s;
}

.btn-delete-detail:hover {
  color: #e94560;
  border-color: #e94560;
  background: #fef0f2;
}

/* ── Sections ─────────────────────────────────────────────── */
.section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.section-title {
  font-size: 17px;
  margin-bottom: 16px;
  color: #1a1a2e;
}

/* ── Progress ─────────────────────────────────────────────── */
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.progress-pct {
  font-weight: 700;
  color: #e94560;
  font-size: 18px;
}

.progress-track {
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #e94560, #f0738a);
  border-radius: 4px;
  transition: width 0.6s ease;
}

.phase-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 8px;
}

.phase-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  background: #f9f9f9;
}

.ph-in_progress {
  background: #e3f2fd;
}

.ph-completed {
  background: #e8f5e9;
}

.ph-failed {
  background: #fbe9e7;
}

.phase-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.ph-in_progress .phase-indicator {
  animation: pulse 1.5s infinite;
}

.ph-skipped .phase-indicator {
  border: 2px dashed #bdbdbd;
  background: transparent !important;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.5; }
}

.phase-name {
  color: #555;
}

/* ── Timeline ─────────────────────────────────────────────── */
.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: #eee;
}

.timeline-line {
  position: relative;
  padding: 6px 0;
  font-size: 13px;
  font-family: 'Courier New', monospace;
  color: #555;
  line-height: 1.6;
}

.tl-dot {
  position: absolute;
  left: -18px;
  top: 11px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
}

/* ── Report ───────────────────────────────────────────────── */
.report-section {
  max-height: 80vh;
  overflow-y: auto;
}

/* ── Tip ──────────────────────────────────────────────────── */
.tip-section {
  text-align: center;
  color: #999;
  font-size: 15px;
  padding: 40px 24px;
}

/* ── Error ────────────────────────────────────────────────── */
.error-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.error-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}
</style>
