<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPrd, getProjectStatus } from '@/api'
import ReportView from '@/components/ReportView.vue'

const props = defineProps<{ id: string }>()
const router = useRouter()

const prdContent = ref('')
const projectName = ref('')
const loading = ref(true)
const confirmed = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    const [prd, status] = await Promise.all([
      getPrd(props.id),
      getProjectStatus(props.id),
    ])
    prdContent.value = prd?.content || ''
    projectName.value = status?.name || ''
    if (!prdContent.value) {
      error.value = 'PRD 尚未生成，请等待需求采集完成'
    }
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})

function handleConfirm() {
  confirmed.value = true
}

function handleBack() {
  router.push(`/projects/${props.id}`)
}
</script>

<template>
  <div class="confirm-page">
    <div class="confirm-header">
      <button class="btn-back" @click="handleBack">← 返回</button>
      <h2>需求确认 — {{ projectName }}</h2>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <button class="btn-back" @click="handleBack">返回项目详情</button>
    </div>

    <template v-else>
      <!-- PRD content -->
      <div class="prd-section">
        <ReportView :content="prdContent" />
      </div>

      <!-- Confirm actions -->
      <div v-if="!confirmed" class="confirm-actions">
        <button class="btn-confirm" @click="handleConfirm">
          ✅ 确认需求，启动开发
        </button>
        <button class="btn-revise" @click="handleBack">
          📝 需要修改需求
        </button>
      </div>

      <div v-else class="confirmed-msg">
        <span class="check-icon">✅</span>
        <p>需求已确认！AI 智能体将开始架构设计和代码开发。</p>
        <p class="confirmed-hint">返回项目详情页查看实时进度。</p>
        <button class="btn-back" @click="handleBack">查看项目进度</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.confirm-page {
  max-width: 850px;
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.confirm-header h2 {
  font-size: 20px;
  color: #1a1a2e;
}

.btn-back {
  background: none;
  border: 1px solid #ddd;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #e94560;
  color: #e94560;
}

.loading, .error-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.prd-section {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 24px;
}

.confirm-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding: 20px 0;
}

.btn-confirm {
  background: #66bb6a;
  color: #fff;
  border: none;
  padding: 14px 32px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-confirm:hover {
  background: #4caf50;
}

.btn-revise {
  background: #fff;
  color: #e94560;
  border: 2px solid #e94560;
  padding: 14px 32px;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-revise:hover {
  background: #fef0f2;
}

.confirmed-msg {
  text-align: center;
  padding: 40px 0;
}

.check-icon {
  font-size: 56px;
  display: block;
  margin-bottom: 16px;
}

.confirmed-msg p {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.confirmed-hint {
  font-size: 14px !important;
  color: #999 !important;
  margin-bottom: 24px !important;
}
</style>
