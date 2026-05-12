<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="520px"
    :close-on-click-modal="false"
    @update:model-value="v => emit('update:modelValue', v)"
    @closed="onClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" :disabled="isEdit" placeholder="3~32 位字母数字下划线" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          :placeholder="isEdit ? '不填则保持原密码' : '必填'" />
      </el-form-item>
      <el-form-item label="确认密码" prop="password2">
        <el-input
          v-model="form.password2"
          type="password"
          show-password
          :placeholder="isEdit ? '不填则保持原密码' : '必填'" />
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" style="width: 100%">
          <el-option label="超级管理员" value="super_admin" />
          <el-option label="普通管理员" value="admin" />
          <el-option label="只读用户" value="readonly" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-switch
          :model-value="form.status === 1"
          @change="v => form.status = v ? 1 : 0" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="256" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="onSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createUser, updateUser } from '../../api/user'

const props = defineProps({
  modelValue: Boolean,
  model: Object
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref(null)
const loading = ref(false)
const isEdit = computed(() => !!props.model?.id)

const form = reactive({
  id: null, username: '', password: '', password2: '',
  role: 'admin', status: 1, remark: ''
})

watch(() => props.modelValue, (v) => {
  if (!v) return
  Object.assign(form, {
    id: null, username: '', password: '', password2: '',
    role: 'admin', status: 1, remark: ''
  })
  if (props.model) {
    Object.assign(form, props.model)
    form.password = ''
    form.password2 = ''
  }
  formRef.value?.clearValidate()
})

const pwdPattern = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).{8,32}$/

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{3,32}$/, message: '3~32 位字母数字下划线', trigger: 'blur' }
  ],
  password: [{
    validator(rule, value, cb) {
      if (!isEdit.value && !value) return cb(new Error('请输入密码'))
      if (value && !pwdPattern.test(value))
        return cb(new Error('密码须 8~32 位，且包含大写字母、小写字母、数字'))
      cb()
    },
    trigger: 'blur'
  }],
  password2: [{
    validator(rule, value, cb) {
      if (form.password && value !== form.password)
        return cb(new Error('两次密码不一致'))
      cb()
    },
    trigger: 'blur'
  }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = { ...form }
    delete payload.password2
    if (isEdit.value) {
      await updateUser(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createUser(payload)
      ElMessage.success('新增成功')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    loading.value = false
  }
}

function onClosed() {
  formRef.value?.resetFields()
}
</script>
