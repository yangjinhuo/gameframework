<template>
  <div>
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">用户总数</div>
          <div class="stat-value">{{ stats.userCount ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">脚本总数</div>
          <div class="stat-value">{{ stats.scriptCount ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">审计日志</div>
          <div class="stat-value">{{ stats.auditCount ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome" shadow="never">
      <h3>欢迎，{{ userStore.username }}</h3>
      <p>这里是 Python 脚本管理平台。您可以在左侧菜单中管理脚本、用户及查看审计日志。</p>
      <ul>
        <li>脚本管理 — 维护脚本元信息并在线查看/编辑脚本内容</li>
        <li>用户管理 — 仅超级管理员可见</li>
        <li>审计日志 — 全部写操作自动记录，支持导出</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '../../api/audit'
import { useUserStore } from '../../store/user'

const stats = ref({})
const userStore = useUserStore()

onMounted(async () => {
  try {
    stats.value = await getDashboardStats()
  } catch (e) { /* handled */ }
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 8px 0;
}
.stat-label { color: #909399; font-size: 14px; }
.stat-value { font-size: 32px; font-weight: bold; color: #1e3c72; margin-top: 8px; }
.welcome { margin-top: 16px; }
.welcome h3 { margin: 0 0 8px; }
.welcome p { color: #606266; margin: 0 0 8px; }
.welcome ul { color: #606266; line-height: 1.8; padding-left: 20px; }
</style>
