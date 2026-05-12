<template>
  <el-container class="app-layout">
    <el-aside :width="collapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!collapse">Python 脚本管理</span>
        <span v-else>SP</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="collapse"
        router
        background-color="#001529"
        text-color="#cfd8dc"
        active-text-color="#ffd04b">
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item v-if="userStore.isSuperAdmin" index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/scripts">
          <el-icon><Document /></el-icon>
          <template #title>脚本管理</template>
        </el-menu-item>
        <el-menu-item index="/audit-logs">
          <el-icon><Tickets /></el-icon>
          <template #title>审计日志</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="collapse = !collapse">
            <Fold v-if="!collapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item to="/">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta?.title || '' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag :type="roleTag.type">{{ roleTag.label }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span style="margin-left: 6px">{{ userStore.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { logout as apiLogout } from '../api/auth'
import {
  House, User, Document, Tickets, UserFilled, Fold, Expand
} from '@element-plus/icons-vue'

const collapse = ref(false)
const router = useRouter()
const userStore = useUserStore()

const roleTag = computed(() => {
  const map = {
    super_admin: { type: 'danger', label: '超级管理员' },
    admin: { type: 'warning', label: '普通管理员' },
    readonly: { type: 'info', label: '只读用户' }
  }
  return map[userStore.role] || { type: '', label: userStore.role || '-' }
})

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    try { await apiLogout() } catch (e) { /* ignore */ }
    userStore.clear()
    router.replace('/login')
  }
}
</script>

<style scoped lang="scss">
.app-layout {
  height: 100%;
}
.sidebar {
  background: #001529;
  overflow-x: hidden;
  transition: width 0.2s;

  .logo {
    color: #fff;
    height: 56px;
    line-height: 56px;
    text-align: center;
    font-size: 16px;
    font-weight: bold;
    border-bottom: 1px solid #002140;
  }
  :deep(.el-menu) { border-right: 0; }
}
.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  .collapse-icon {
    font-size: 18px;
    cursor: pointer;
  }
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  .user-info {
    cursor: pointer;
    display: inline-flex;
    align-items: center;
  }
}
</style>
