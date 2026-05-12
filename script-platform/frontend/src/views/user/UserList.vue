<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input v-model="query.username" placeholder="用户名" clearable style="width: 180px" @keyup.enter="load" />
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="-"
        start-placeholder="注册起"
        end-placeholder="注册止"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 360px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-button type="success" style="margin-left: auto" @click="onAdd">+ 新增用户</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column label="角色" width="140">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            :disabled="row.role === 'super_admin'"
            @change="(v) => toggleStatus(row, v)" />
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="onEdit(row)">编辑</el-button>
          <el-popconfirm
            :title="`确认删除用户 ${row.username}？此操作不可撤销。`"
            @confirm="onDelete(row)">
            <template #reference>
              <el-button link type="danger" :disabled="row.role === 'super_admin'">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      background
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="load"
      @size-change="load" />

    <user-form-dialog v-model="dialog.visible" :model="dialog.model" @saved="load" />
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageUsers, deleteUser, updateUserStatus } from '../../api/user'
import UserFormDialog from './UserFormDialog.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])
const query = reactive({
  pageNum: 1, pageSize: 20, username: '', status: undefined, startTime: '', endTime: ''
})
const dialog = reactive({ visible: false, model: null })

watch(dateRange, (v) => {
  query.startTime = v?.[0] || ''
  query.endTime = v?.[1] || ''
})

async function load() {
  loading.value = true
  try {
    const data = await pageUsers(query)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.username = ''
  query.status = undefined
  query.startTime = ''
  query.endTime = ''
  dateRange.value = []
  query.pageNum = 1
  load()
}

function onAdd() {
  dialog.model = null
  dialog.visible = true
}

function onEdit(row) {
  dialog.model = { ...row }
  dialog.visible = true
}

async function onDelete(row) {
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  load()
}

async function toggleStatus(row, v) {
  const status = v ? 1 : 0
  await updateUserStatus(row.id, status)
  row.status = status
  ElMessage.success('状态已更新')
}

function roleLabel(r) {
  return { super_admin: '超级管理员', admin: '普通管理员', readonly: '只读用户' }[r] || r
}
function roleTagType(r) {
  return { super_admin: 'danger', admin: 'warning', readonly: 'info' }[r] || ''
}

onMounted(load)
</script>
