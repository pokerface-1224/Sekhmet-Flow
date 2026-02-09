<script setup lang="ts">
import { computed } from "vue";
import { useLogStore } from "../stores/logStore";

const logStore = useLogStore();

const logs = computed(() => logStore.logs);

/** 根据日志类型获取对应的 CSS 类名 */
function getLogClass(type: string) {
  switch (type) {
    case "error":
      return "text-red-500";
    case "success":
      return "text-green-500";
    case "warning":
      return "text-yellow-500";
    case "info":
    default:
      return "text-blue-400";
  }
}
</script>

<template>
  <div
    class="h-screen flex flex-col overflow-hidden bg-gray-900 text-gray-100"
  >
    <!-- Header -->
    <div
      class="flex items-center justify-between px-4 py-3 bg-gray-800 border-b border-gray-700"
    >
      <div class="flex items-center gap-3">
        <div
          class="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-lg"
        >
          S
        </div>
        <h1 class="text-lg font-semibold text-gray-200">执行日志</h1>
      </div>
      <button
        @click="logStore.clearLogs()"
        class="text-xs px-3 py-1.5 bg-gray-700 hover:bg-gray-600 rounded text-gray-300 transition-colors"
      >
        清空日志
      </button>
    </div>

    <!-- Log Content -->
    <div class="flex-1 overflow-auto p-4 font-mono text-sm">
      <div v-if="logs.length === 0" class="text-gray-500 italic text-center py-8">
        暂无日志，运行工作流后将在此显示执行日志。
      </div>
      <div
        v-for="log in logs"
        :key="log.id"
        class="mb-2 border-b border-gray-800 pb-2 last:border-0"
      >
        <div class="flex gap-2">
          <span class="text-gray-500 text-xs">[{{ log.timestamp }}]</span>
          <span
            :class="getLogClass(log.type)"
            class="uppercase text-xs font-bold w-16"
            >{{ log.type }}</span
          >
          <span class="text-gray-300">{{ log.message }}</span>
        </div>
        <pre
          v-if="log.details"
          class="mt-1 ml-24 text-xs text-gray-400 bg-gray-800 p-2 rounded overflow-x-auto"
          >{{ JSON.stringify(log.details, null, 2) }}</pre
        >
      </div>
    </div>
  </div>
</template>
