<template>
  <div ref="container" class="code-editor" :style="{ height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as monaco from 'monaco-editor'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
  language: { type: String, default: 'python' },
  height: { type: String, default: '520px' }
})
const emit = defineEmits(['update:modelValue'])

const container = ref(null)
let editor = null

onMounted(() => {
  editor = monaco.editor.create(container.value, {
    value: props.modelValue || '',
    language: props.language,
    theme: 'vs-dark',
    readOnly: props.readonly,
    automaticLayout: true,
    tabSize: 4,
    insertSpaces: true,
    fontSize: 13,
    fontFamily: 'JetBrains Mono, Fira Code, Menlo, monospace',
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    wordWrap: 'on'
  })
  editor.onDidChangeModelContent(() => {
    emit('update:modelValue', editor.getValue())
  })
})

watch(() => props.modelValue, (v) => {
  if (editor && v !== editor.getValue()) editor.setValue(v || '')
})
watch(() => props.readonly, (v) => {
  editor?.updateOptions({ readOnly: v })
})

onBeforeUnmount(() => {
  editor?.dispose()
  editor = null
})
</script>

<style scoped>
.code-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
</style>
