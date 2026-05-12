<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <div>
        <h3 style="margin: 0">{{ model.scriptName }}</h3>
        <div class="meta-line">
          <el-tag size="small">v{{ model.version }}</el-tag>
          <span>最后更新：{{ model.updatedAt }}</span>
          <span>负责人：{{ model.owner }}</span>
        </div>
      </div>
      <div>
        <el-button @click="$router.back()">返回列表</el-button>
        <el-button type="primary" @click="copyCode">复制代码</el-button>
        <el-button v-if="userStore.isAdmin" type="warning" @click="$router.push(`/scripts/edit/${model.id}`)">编辑</el-button>
      </div>
    </div>

    <el-descriptions :column="3" border size="small" class="meta-desc">
      <el-descriptions-item label="脚本路径">{{ model.scriptPath }}</el-descriptions-item>
      <el-descriptions-item label="归属模块">{{ model.module }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="model.status === 1 ? 'success' : 'info'">
          {{ model.status === 1 ? '启用' : '禁用' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="预警钉钉">{{ model.alertDingtalk || '—' }}</el-descriptions-item>
      <el-descriptions-item label="预警电话">{{ model.alertPhone || '—' }}</el-descriptions-item>
      <el-descriptions-item label="调度频率">{{ model.scheduleDesc || '—' }}</el-descriptions-item>
      <el-descriptions-item label="功能说明" :span="3">{{ model.description }}</el-descriptions-item>
    </el-descriptions>

    <code-editor v-model="model.scriptContent" readonly height="540px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScript } from '../../api/script'
import CodeEditor from '../../components/CodeEditor.vue'
import { useUserStore } from '../../store/user'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const model = ref({ scriptContent: '' })

async function load() {
  loading.value = true
  try {
    model.value = await getScript(route.params.id)
  } finally { loading.value = false }
}

async function copyCode() {
  try {
    await navigator.clipboard.writeText(model.value.scriptContent || '')
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

onMounted(load)
</script>

<style scoped>
.meta-line {
  color: #606266;
  display: flex;
  gap: 16px;
  font-size: 13px;
  margin-top: 6px;
}
.meta-desc { margin-bottom: 16px; }
</style>
