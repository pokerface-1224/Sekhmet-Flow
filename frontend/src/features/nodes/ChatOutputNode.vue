<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import BaseNode from './BaseNode.vue'
import type { NodeProps } from '@vue-flow/core'
import { useWorkflowStore } from '../../stores/workflowStore'

defineProps<NodeProps>()

const workflowStore = useWorkflowStore()
</script>

<template>
  <BaseNode title="Chat Output" :selected="selected" @delete="workflowStore.removeNode(id)">
    <Handle type="target" :position="Position.Left" />
    
    <div class="flex flex-col gap-2">
      <div 
        class="border rounded p-2 text-sm w-full h-32 overflow-y-auto bg-gray-50 dark:bg-gray-900 dark:border-gray-600 dark:text-gray-200"
      >
        <span v-if="data.output" class="whitespace-pre-wrap">{{ data.output }}</span>
        <span v-else class="text-gray-400 italic">Waiting for output...</span>
      </div>
    </div>
  </BaseNode>
</template>
