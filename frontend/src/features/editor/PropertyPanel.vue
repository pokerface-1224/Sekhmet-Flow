<script setup lang="ts">
import { useUiStore } from '../../stores/uiStore'
import { useWorkflowStore } from '../../stores/workflowStore'
import { computed } from 'vue'

const uiStore = useUiStore()
const workflowStore = useWorkflowStore()

// 获取当前选中的节点
const selectedNode = computed(() => uiStore.selectedNode)

/** 删除当前选中的节点 */
const deleteNode = () => {
  if (selectedNode.value) {
    workflowStore.removeNode(selectedNode.value.id)
    uiStore.selectNode(null)
  }
}
</script>

<template>
  <aside 
    v-if="selectedNode"
    class="w-80 h-full border-l border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 flex flex-col"
  >
    <div class="p-4 border-b border-gray-200 dark:border-gray-700 font-bold text-gray-700 dark:text-gray-200">
      Properties
    </div>
    
    <div class="p-4 flex flex-col gap-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">ID</label>
        <div class="mt-1 text-sm text-gray-500 dark:text-gray-400 font-mono">{{ selectedNode.id }}</div>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Type</label>
        <div class="mt-1 text-sm text-gray-500 dark:text-gray-400 capitalize">{{ selectedNode.type?.replace('-node', '') }}</div>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">Label</label>
        <input 
          v-model="selectedNode.label" 
          type="text" 
          class="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm bg-white dark:bg-gray-700 dark:text-white p-2 border"
        />
      </div>

      <!-- Specific Node Settings could go here -->
      <div v-if="selectedNode.type === 'llm-node'">
         <div class="text-xs text-gray-400 mt-2">LLM Specific Settings (also available on node)</div>
      </div>

    </div>

    <div class="mt-auto p-4 border-t border-gray-200 dark:border-gray-700">
      <button
        @click="deleteNode"
        :disabled="uiStore.isLocked"
        class="w-full px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-md transition-colors text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Delete Node
      </button>
    </div>
  </aside>
  <aside v-else class="w-80 h-full border-l border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 flex items-center justify-center text-gray-400 text-sm">
    Select a node to view properties
  </aside>
</template>
