<script setup lang="ts">
import { Controls } from '@vue-flow/controls'
import { Panel } from '@vue-flow/core'
import { useWorkflowStore } from '../../stores/workflowStore'
import '@vue-flow/controls/dist/style.css'

const workflowStore = useWorkflowStore()

/** 保存工作流 */
async function onSave() {
  try {
    await workflowStore.saveWorkflow()
    // 简单反馈，后续可优化为 Toast 提示
    console.log('Workflow saved successfully')
  } catch (e) {
    console.error(e)
  }
}

/** 运行工作流 */
async function onRun() {
  console.log('Run triggered')
  // workflowStore.runWorkflow() // 待实现: 在 Store 中调用
}
</script>

<template>
  <Controls position="bottom-left" class="!bg-white dark:!bg-gray-800 !border-gray-200 dark:!border-gray-700 !shadow-lg">
  </Controls>
  
  <Panel position="top-right" class="flex gap-2">
    <button class="px-3 py-1.5 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm font-medium transition-colors" @click="onSave">
      Save Workflow
    </button>
    <button class="px-3 py-1.5 bg-green-600 text-white rounded-md hover:bg-green-700 text-sm font-medium transition-colors" @click="onRun">
      Run
    </button>
  </Panel>
</template>

<style>
.vue-flow__controls-button {
  background-color: transparent;
  border-bottom: 1px solid #e5e7eb;
}

.dark .vue-flow__controls-button {
  border-bottom: 1px solid #374151;
  fill: #e5e7eb;
}

.dark .vue-flow__controls-button:hover {
  background-color: #374151;
}

.vue-flow__controls-button:last-child {
  border-bottom: none;
}
</style>
