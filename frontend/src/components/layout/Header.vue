<script setup lang="ts">
import { useWorkflowStore } from '../../stores/workflowStore'
import { useLogStore } from '../../stores/logStore'
import { ref } from 'vue'

const workflowStore = useWorkflowStore()
const logStore = useLogStore()
const isRunning = ref(false)

async function handleRun() {
  isRunning.value = true
  
  try {
    await workflowStore.runWorkflow()
  } catch (error: any) {
    // Error is already logged in store, but we can keep a UI notification if needed
    // or just let the store handle the logs.
    // For now, minimal handling here since store logs "Execution request failed"
  } finally {
    isRunning.value = false
  }
}
</script>

<template>
  <header class="h-16 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 flex items-center px-6 justify-between">
    <div class="flex items-center gap-2">
      <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-lg">
        S
      </div>
      <h1 class="text-xl font-bold text-gray-800 dark:text-gray-100">Sekhmet Flow</h1>
    </div>
    
    <div class="flex items-center gap-4">
      <button 
        @click="logStore.togglePanel()"
        class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 px-3 py-2 text-sm transition-colors"
      >
        {{ logStore.isOpen ? 'Hide Logs' : 'Show Logs' }}
      </button>

      <button 
        @click="handleRun"
        :disabled="isRunning"
        class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-2"
      >
        <span v-if="isRunning" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
        {{ isRunning ? 'Running...' : 'Run Workflow' }}
      </button>
      <button class="bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 px-4 py-2 rounded-md text-sm font-medium transition-colors">
        Save
      </button>
    </div>
  </header>
</template>
