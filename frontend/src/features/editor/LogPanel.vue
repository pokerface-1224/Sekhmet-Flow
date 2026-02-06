<script setup lang="ts">
import { useLogStore } from '../../stores/logStore'
import { computed } from 'vue'

const logStore = useLogStore()

const logs = computed(() => logStore.logs)



/** 根据日志类型获取对应的 CSS 类名 */
function getLogClass(type: string) {
  switch (type) {
    case 'error': return 'text-red-500'
    case 'success': return 'text-green-500'
    case 'warning': return 'text-yellow-500'
    case 'info':
    default: return 'text-blue-400'
  }
}
</script>

<template>
  <div 
    v-if="logStore.isOpen" 
    class="absolute bottom-0 left-0 right-0 h-64 bg-gray-900 border-t border-gray-700 flex flex-col shadow-lg z-50 transition-all duration-300"
  >
    <div class="flex items-center justify-between px-4 py-2 bg-gray-800 border-b border-gray-700">
      <h3 class="text-sm font-semibold text-gray-200">Execution Logs</h3>
      <div class="flex gap-2">
        <button 
          @click="logStore.clearLogs()"
          class="text-xs px-2 py-1 bg-gray-700 hover:bg-gray-600 rounded text-gray-300"
        >
          Clear
        </button>
        <button 
          @click="logStore.togglePanel()"
          class="text-xs px-2 py-1 bg-gray-700 hover:bg-gray-600 rounded text-gray-300"
        >
          Close
        </button>
      </div>
    </div>
    
    <div class="flex-1 overflow-auto p-4 font-mono text-sm">
      <div v-if="logs.length === 0" class="text-gray-500 italic">No logs yet.</div>
      <div v-for="log in logs" :key="log.id" class="mb-2 border-b border-gray-800 pb-2 last:border-0">
        <div class="flex gap-2">
          <span class="text-gray-500 text-xs">[{{ log.timestamp }}]</span>
          <span :class="getLogClass(log.type)" class="uppercase text-xs font-bold w-16">{{ log.type }}</span>
          <span class="text-gray-300">{{ log.message }}</span>
        </div>
        <pre v-if="log.details" class="mt-1 ml-24 text-xs text-gray-400 bg-gray-800 p-2 rounded overflow-x-auto">{{ JSON.stringify(log.details, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>
