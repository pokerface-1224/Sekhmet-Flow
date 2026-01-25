<template>
  <BaseNode title="LLM Model" :selected="selected" @delete="workflowStore.removeNode(id)">
    <Handle type="target" :position="Position.Left" />
    
    <div class="flex flex-col gap-2">
      <label class="text-sm text-gray-500 dark:text-gray-400">Provider</label>
      <select 
        :value="data.provider || 'openai'" 
        @change="(e) => updateNodeData(id, { provider: (e.target as HTMLSelectElement).value })"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag">
        <option value="openai">OpenAI</option>
        <option value="gemini">Google Gemini</option>
        <option value="deepseek">Deepseek</option>
      </select>

      <label class="text-sm text-gray-500 dark:text-gray-400">API Key</label>
      <input 
        type="password" 
        :value="data.apiKey || ''"
        @input="(e) => updateNodeData(id, { apiKey: (e.target as HTMLInputElement).value })" 
        placeholder="Optional (overrides default)"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag" 
      />

      <label class="text-sm text-gray-500 dark:text-gray-400">Model</label>
      <input 
        type="text" 
        :value="data.modelName || 'gpt-3.5-turbo'"
        @input="(e) => updateNodeData(id, { modelName: (e.target as HTMLInputElement).value })" 
        placeholder="e.g. gpt-4"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag" 
      />
      
      <label class="text-sm text-gray-500 dark:text-gray-400">Temperature</label>
      <input 
        type="number" 
        step="0.1" 
        min="0" 
        max="2" 
        :value="typeof data.temperature === 'number' ? data.temperature : 0.7"
        @input="(e) => updateNodeData(id, { temperature: parseFloat((e.target as HTMLInputElement).value) })"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag" 
      />
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
