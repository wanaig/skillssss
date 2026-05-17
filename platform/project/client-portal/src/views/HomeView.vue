<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="home">
    <section class="hero">
      <h1>AI 驱动的项目开发平台</h1>
      <p class="hero-subtitle">
        提交您的项目需求，34 个 AI 智能体协同工作，从需求分析到生产部署全流程自动化。
      </p>
      <div class="hero-actions">
        <button class="btn-primary" @click="router.push('/new')">提交新项目</button>
        <button class="btn-secondary" @click="router.push('/projects')">查看已有项目</button>
      </div>
    </section>

    <section class="features">
      <div class="feature-card">
        <h3>📋 需求引导</h3>
        <p>AI 智能体通过对话式引导，帮您将模糊的想法转化为结构化的产品需求文档。</p>
      </div>
      <div class="feature-card">
        <h3>🏗️ 架构设计</h3>
        <p>6 个架构分析智能体并行评估技术栈、数据模型、安全策略等维度。</p>
      </div>
      <div class="feature-card">
        <h3>💻 自动开发</h3>
        <p>前端（Vue 3）/ 后端（Spring Boot）/ 移动端 / 区块链，多领域并行开发。</p>
      </div>
      <div class="feature-card">
        <h3>✅ 三维测试</h3>
        <p>功能测试 + 性能测试 + 安全测试，每批代码自动验证，最多 3 轮修正。</p>
      </div>
      <div class="feature-card">
        <h3>🚀 一键部署</h3>
        <p>Docker 容器化部署，自动生成运维文档和配置。</p>
      </div>
      <div class="feature-card">
        <h3>📊 实时追踪</h3>
        <p>项目进度实时可见，每个阶段的产出和测试结果一目了然。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.hero {
  text-align: center;
  padding: 60px 0 40px;
}

.hero h1 {
  font-size: 36px;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  max-width: 600px;
  margin: 0 auto 32px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-primary {
  background: #e94560;
  color: #fff;
  border: none;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #d63850;
}

.btn-secondary {
  background: #fff;
  color: #e94560;
  border: 2px solid #e94560;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-secondary:hover {
  background: #fef0f2;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 40px 0;
}

.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.feature-card h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #1a1a2e;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .features {
    grid-template-columns: 1fr;
  }
}
</style>
