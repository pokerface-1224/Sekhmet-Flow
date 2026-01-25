<template>
  <BaseNode title="Prompt" :selected="selected" @delete="workflowStore.removeNode(id)">
    <div class="flex flex-col gap-2">
      <textarea 
        :value="data.text"
        @input="(e) => updateNodeData(id, { text: (e.target as HTMLTextAreaElement).value })"
        class="border rounded p-2 text-sm w-full h-24 resize-none bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag" 
        placeholder="Enter your prompt here..."
      ></textarea>
    </div>
    <Handle type="source" :position="Position.Right" />
  </BaseNode>
</template>

<script setup lang="ts">
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import BaseNode from './BaseNode.vue'
import type { NodeProps } from '@vue-flow/core'
import { useWorkflowStore } from '../../stores/workflowStore'

defineProps<NodeProps>()

const workflowStore = useWorkflowStore()
const { updateNodeData } = useVueFlow()
</script>
