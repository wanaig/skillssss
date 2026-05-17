<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { submitProject } from '@/api'

const router = useRouter()
const description = ref('')
const submitting = ref(false)
const error = ref('')

async function handleSubmit() {
  if (!description.value.trim()) {
    error.value = '请输入项目需求描述'
    return
  }
  error.value = ''
  submitting.value = true
  try {
    const result = await submitProject({
      description: description.value,
      constraints: {},
    })
    router.push(`/projects/${result.projectId}`)
  } catch {
    error.value = '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="new-project">
    <h2>提交新项目需求</h2>
    <p class="subtitle">
      用自然语言描述您的项目想法，AI 智能体会引导您完成需求细化，然后自动完成架构设计、开发、测试和部署。
    </p>

    <form class="submit-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="description">项目需求描述</label>
        <textarea
          id="description"
          v-model="description"
          placeholder="例如：我想做一个企业内部的任务管理系统，员工可以创建和分配任务、追踪进度，管理者可以查看团队的工作负载和完成率统计..."
          rows="8"
          :disabled="submitting"
        ></textarea>
      </div>

      <div v-if="error" class="error-msg">{{ error }}</div>

      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? '提交中...' : '提交需求，启动 AI 开发' }}
        </button>
        <button type="button" class="btn-cancel" @click="router.back()">取消</button>
      </div>
    </form>

    <div class="tips">
      <h3>💡 写好需求描述的技巧</h3>
      <ul>
        <li>说明这个系统的<strong>目标用户</strong>是谁</li>
        <li>列出 2-3 个<strong>核心功能</strong>（不需要很详细）</li>
        <li>如果有参考产品，可以提一下</li>
        <li>不需要写技术细节，AI 会帮您决定</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.subtitle {
  color: #666;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 32px;
  max-width: 680px;
}

.submit-form {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1a1a2e;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s;
}

.form-group textarea:focus {
  outline: none;
  border-color: #e94560;
  box-shadow: 0 0 0 3px rgba(233, 69, 96, 0.1);
}

.error-msg {
  color: #d32f2f;
  font-size: 14px;
  margin-bottom: 12px;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.btn-primary {
  background: #e94560;
  color: #fff;
  border: none;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 15px;
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

.btn-cancel {
  background: #fff;
  color: #666;
  border: 1px solid #ddd;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
}

.tips {
  background: #fffbe6;
  border-radius: 10px;
  padding: 20px 24px;
  border: 1px solid #ffe58f;
}

.tips h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.tips ul {
  padding-left: 20px;
  color: #666;
  font-size: 14px;
  line-height: 2;
}
</style>
