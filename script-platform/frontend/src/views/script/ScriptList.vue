<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input v-model="query.scriptName" placeholder="脚本名称" clearable style="width: 180px" @keyup.enter="load" />
      <el-select v-model="query.module" placeholder="归属模块" clearable filterable style="width: 160px">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <el-input v-model="query.owner" placeholder="负责人" clearable style="width: 160px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-button v-if="userStore.isAdmin" type="success" style="margin-left: auto" @click="onAdd">+ 新增脚本</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="scriptName" label="脚本名称" min-width="140" />
      <el-table-column prop="scriptPath" label="脚本路径" show-overflow-tooltip min-width="180" />
      <el-table-column prop="description" label="功能说明" show-overflow-tooltip min-width="180" />
      <el-table-column label="归属模块" width="120">
        <template #default="{ row }"><el-tag>{{ row.module }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="owner" label="负责人" width="120" />
      <el-table-column label="预警钉钉" width="160">
        <template #default="{ row }">{{ row.alertDingtalk || '—' }}</template>
      </el-table-column>
      <el-table-column label="预警电话" width="140">
        <template #default="{ row }">{{ row.alertPhone || '—' }}</template>
      </el-table-column>
      <el-table-column label="调度频率" width="160">
        <template #default="{ row }">{{ row.scheduleDesc || '—' }}</template>
      </el-table-column>
      <el-table-column label="版本" width="80">
        <template #default="{ row }">v{{ row.version }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="onView(row)">查看</el-button>
          <el-button v-if="userStore.isAdmin" link type="warning" @click="onEdit(row)">编辑</el-button>
          <el-popconfirm
            v-if="userStore.isSuperAdmin"
            :title="`确认删除脚本 ${row.scriptName}？`"
            @confirm="onDelete(row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageScripts, deleteScript, listModules } from '../../api/script'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const modules = ref([])
const query = reactive({
  pageNum: 1, pageSize: 20, scriptName: '', module: '', owner: '', status: undefined
})

async function load() {
  loading.value = true
  try {
    const data = await pageScripts(query)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
}

async function loadModules() {
  modules.value = await listModules()
}

function onReset() {
  Object.assign(query, { scriptName: '', module: '', owner: '', status: undefined, pageNum: 1 })
  load()
}
function onAdd() { router.push('/scripts/new') }
function onView(row) { router.push(`/scripts/view/${row.id}`) }
function onEdit(row) { router.push(`/scripts/edit/${row.id}`) }

async function onDelete(row) {
  await deleteScript(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => { load(); loadModules() })
</script>
