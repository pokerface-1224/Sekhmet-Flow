<script setup lang="ts">
import { useUiStore } from '../../stores/uiStore'

defineProps<{
  title: string
  selected?: boolean
}>()

defineEmits<{
  (e: 'delete'): void
}>()

const uiStore = useUiStore()
</script>

<template>
  <div
    class="bg-white dark:bg-gray-800 border-2 rounded-lg shadow-md min-w-[200px]"
    :class="selected ? 'border-blue-500' : 'border-gray-200 dark:border-gray-700'"
  >
    <div class="px-4 py-2 border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-900 rounded-t-lg font-medium text-gray-900 dark:text-gray-100 flex justify-between items-center">
      <span>{{ title }}</span>
      <button
        v-if="!uiStore.isLocked"
        @click.stop="$emit('delete')"
        class="text-gray-400 hover:text-red-500 dark:text-gray-500 dark:hover:text-red-400 transition-colors"
        title="Delete Node"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
    <div class="p-4">
      <slot />
    </div>
  </div>
</template>
