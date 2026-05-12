<template>
  <div class="page-card">
    <div class="search-bar">
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="-"
        start-placeholder="起始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 380px" />
      <el-select
        v-model="query.eventTypes"
        multiple
        collapse-tags
        placeholder="事件类型"
        clearable
        style="width: 260px">
        <el-option v-for="t in eventTypes" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-input v-model="query.operator" placeholder="操作人" clearable style="width: 160px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="操作结果" clearable style="width: 120px">
        <el-option label="成功" :value="1" />
        <el-option label="失败" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-button v-if="userStore.isAdmin" type="success" style="margin-left: auto" @click="onExport" :loading="exporting">导出 Excel</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="eventTime" label="操作时间" width="170" />
      <el-table-column label="事件类型" width="140">
        <template #default="{ row }">
          <el-tag :type="eventTagType(row.eventType)">{{ eventTypeLabel(row.eventType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="operatorIp" label="IP" width="140" />
      <el-table-column prop="targetName" label="操作对象" show-overflow-tooltip />
      <el-table-column label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="请求参数" width="90" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="showJson('请求参数', row.requestParams)">详情</el-button>
        </template>
      </el-table-column>
      <el-table-column label="响应结果" width="90" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="showJson('响应结果', row.responseResult)">详情</el-button>
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

    <el-dialog v-model="jsonDialog.visible" :title="jsonDialog.title" width="720px">
      <pre class="json-view">{{ jsonDialog.content }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageAuditLogs, exportAuditLogs } from '../../api/audit'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false)
const exporting = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref([])

const eventTypes = [
  { value: 'USER_CREATE', label: '新增用户', type: 'success' },
  { value: 'USER_UPDATE', label: '修改用户', type: 'warning' },
  { value: 'USER_DELETE', label: '删除用户', type: 'danger' },
  { value: 'USER_LOGIN',  label: '用户登录', type: 'info' },
  { value: 'USER_LOGOUT', label: '用户登出', type: 'info' },
  { value: 'SCRIPT_CREATE', label: '新增脚本', type: 'success' },
  { value: 'SCRIPT_EDIT',   label: '编辑脚本', type: 'warning' },
  { value: 'SCRIPT_DELETE', label: '删除脚本', type: 'danger' },
  { value: 'SCRIPT_VIEW',   label: '查看脚本', type: '' }
]

const query = reactive({
  pageNum: 1, pageSize: 20,
  startTime: '', endTime: '', eventTypes: [], operator: '', status: undefined
})
const jsonDialog = reactive({ visible: false, title: '', content: '' })

watch(dateRange, (v) => {
  query.startTime = v?.[0] || ''
  query.endTime = v?.[1] || ''
})

async function load() {
  loading.value = true
  try {
    const data = await pageAuditLogs(query)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}

function onReset() {
  dateRange.value = []
  Object.assign(query, {
    startTime: '', endTime: '', eventTypes: [], operator: '', status: undefined, pageNum: 1
  })
  load()
}

async function onExport() {
  exporting.value = true
  try {
    const res = await exportAuditLogs(query)
    const blob = new Blob([res.data], { type: res.headers['content-type'] })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `audit_logs_${Date.now()}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } finally { exporting.value = false }
}

function showJson(title, content) {
  jsonDialog.title = title
  try {
    jsonDialog.content = content ? JSON.stringify(JSON.parse(content), null, 2) : '(空)'
  } catch (e) {
    jsonDialog.content = content || '(空)'
  }
  jsonDialog.visible = true
}

function eventTypeLabel(t) {
  return eventTypes.find(e => e.value === t)?.label || t
}
function eventTagType(t) {
  return eventTypes.find(e => e.value === t)?.type || ''
}

onMounted(load)
</script>

<style scoped>
.json-view {
  background: #282c34;
  color: #abb2bf;
  padding: 12px;
  border-radius: 4px;
  max-height: 500px;
  overflow: auto;
  font-family: 'JetBrains Mono', 'Fira Code', 'Menlo', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>
