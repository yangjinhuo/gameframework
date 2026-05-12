<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-title">
        <h2>Python 脚本管理平台</h2>
        <p>Script Management Platform</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onSubmit">
            登&nbsp;&nbsp;录
          </el-button>
        </el-form-item>
      </el-form>
      <p class="tip">默认账号：admin / Admin@123</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const router = useRouter()
const userStore = useUserStore()

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const data = await login(form)
    userStore.setLogin(data)
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  background: linear-gradient(135deg, #1e3c72, #2a5298);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 380px;
  padding: 36px 32px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}
.login-title {
  text-align: center;
  margin-bottom: 24px;
}
.login-title h2 { margin: 0 0 6px; color: #1e3c72; }
.login-title p { margin: 0; color: #909399; font-size: 12px; }
.tip {
  color: #909399;
  font-size: 12px;
  text-align: center;
  margin-top: 8px;
}
</style>
