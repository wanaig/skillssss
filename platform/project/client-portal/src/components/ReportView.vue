<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps<{
  content: string
}>()

const html = computed(() => {
  if (!props.content) return '<p class="empty">暂无验收报告</p>'
  return marked(props.content, {
    breaks: true,
    gfm: true,
  })
})
</script>

<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<style>
/* ── Markdown content styling ─────────────────────────────── */
.markdown-body {
  font-size: 15px;
  line-height: 1.8;
  color: #2c3e50;
}

.markdown-body h1 {
  font-size: 22px;
  margin: 0 0 8px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e94560;
  color: #1a1a2e;
}

.markdown-body h2 {
  font-size: 18px;
  margin: 28px 0 12px;
  color: #1a1a2e;
  border-left: 3px solid #e94560;
  padding-left: 10px;
}

.markdown-body h3 {
  font-size: 16px;
  margin: 20px 0 8px;
  color: #444;
}

.markdown-body p {
  margin: 8px 0;
}

.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 14px;
}

.markdown-body th {
  background: #f0f2f5;
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  border-bottom: 2px solid #ddd;
  white-space: nowrap;
}

.markdown-body td {
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
  vertical-align: top;
}

.markdown-body tr:hover td {
  background: #fafbfc;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 24px;
  margin: 8px 0;
}

.markdown-body li {
  margin: 4px 0;
}

.markdown-body input[type="checkbox"] {
  margin-right: 8px;
  accent-color: #e94560;
}

.markdown-body code {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Courier New', monospace;
}

.markdown-body pre {
  background: #1a1a2e;
  color: #e0e0e0;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.6;
  margin: 12px 0;
}

.markdown-body pre code {
  background: none;
  padding: 0;
  font-size: 13px;
}

.markdown-body blockquote {
  border-left: 3px solid #e94560;
  margin: 12px 0;
  padding: 8px 16px;
  background: #fef5f7;
  border-radius: 0 8px 8px 0;
}

.markdown-body hr {
  border: none;
  border-top: 1px solid #eee;
  margin: 24px 0;
}

.markdown-body .empty {
  color: #999;
  text-align: center;
  padding: 40px 0;
}
</style>
