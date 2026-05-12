<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <h3 style="margin: 0">{{ isEdit ? '编辑脚本' : '新增脚本' }}</h3>
      <div>
        <el-button @click="onCancel">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="脚本名称" prop="scriptName">
            <el-input v-model="form.scriptName" maxlength="128" />
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="脚本路径" prop="scriptPath">
            <el-input v-model="form.scriptPath" maxlength="512" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="归属模块" prop="module">
            <el-input v-model="form.module" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="负责人" prop="owner">
            <el-input v-model="form.owner" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="状态" prop="status">
            <el-switch
              :model-value="form.status === 1"
              @change="v => form.status = v ? 1 : 0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="预警钉钉">
            <el-input v-model="form.alertDingtalk" placeholder="Webhook URL 或群名称" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="预警电话">
            <el-input v-model="form.alertPhone" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="调度 Cron">
            <el-input v-model="form.scheduleCron" placeholder="如 0 0 8 * * ?" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="调度描述">
            <el-input v-model="form.scheduleDesc" placeholder="如 每天 08:00 执行" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="功能说明" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="3" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="脚本内容" prop="scriptContent">
            <code-editor v-model="form.scriptContent" height="500px" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScript, createScript, updateScript } from '../../api/script'
import CodeEditor from '../../components/CodeEditor.vue'

const route = useRoute()
const router = useRouter()

const isEdit = ref(!!route.params.id)
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const dirty = ref(false)

const form = reactive({
  id: null,
  scriptName: '',
  scriptPath: '',
  module: '',
  description: '',
  owner: '',
  alertDingtalk: '',
  alertPhone: '',
  scheduleCron: '',
  scheduleDesc: '',
  scriptContent: '# -*- coding: utf-8 -*-\n',
  version: 1,
  status: 1
})

const rules = {
  scriptName: [{ required: true, message: '请输入脚本名称' }],
  scriptPath: [{ required: true, message: '请输入脚本路径' }],
  module: [{ required: true, message: '请输入归属模块' }],
  owner: [{ required: true, message: '请输入负责人' }],
  description: [{ required: true, message: '请输入功能说明' }],
  scriptContent: [{ required: true, message: '请输入脚本内容' }]
}

async function load() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await getScript(route.params.id)
    Object.assign(form, data)
  } finally { loading.value = false }
}

// Mark form dirty on any field change after initial load
let initialized = false
onMounted(async () => {
  await load()
  initialized = true
  // mark dirty when user edits
  const watchers = ['scriptName','scriptPath','module','description','owner',
    'alertDingtalk','alertPhone','scheduleCron','scheduleDesc','scriptContent','status']
  watchers.forEach(() => {})
})

watch(form, () => { if (initialized) dirty.value = true }, { deep: true })

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateScript(form.id, form)
      ElMessage.success('保存成功，版本已 +1')
    } else {
      const res = await createScript(form)
      form.id = res?.id
      isEdit.value = true
      ElMessage.success('新增成功')
    }
    dirty.value = false
    router.replace('/scripts')
  } finally { saving.value = false }
}

function onCancel() {
  router.back()
}

onBeforeRouteLeave(async (to, from, next) => {
  if (!dirty.value) return next()
  try {
    await ElMessageBox.confirm('您有未保存的更改，确定离开？', '提示', {
      confirmButtonText: '离开', cancelButtonText: '继续编辑', type: 'warning'
    })
    next()
  } catch (e) {
    next(false)
  }
})

function beforeUnload(e) {
  if (dirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}
window.addEventListener('beforeunload', beforeUnload)
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))
</script>
