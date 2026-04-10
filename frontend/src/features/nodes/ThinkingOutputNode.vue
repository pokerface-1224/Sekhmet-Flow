<script setup lang="ts">
import type { NodeProps } from "@vue-flow/core";
import { Handle, Position, useVueFlow } from "@vue-flow/core";
import { NodeResizer } from "@vue-flow/node-resizer";
import "@vue-flow/node-resizer/dist/style.css";
import { computed } from "vue";
import { useWorkflowStore } from "../../stores/workflowStore";
import BaseNode from "./BaseNode.vue";

const props = defineProps<NodeProps>();
const { updateNodeData } = useVueFlow();

const displayLabel = computed(() => {
  if (typeof props.label === "string" || typeof props.label === "number") {
    return props.label;
  }
  return undefined;
});

const workflowStore = useWorkflowStore();
</script>

<template>
  <NodeResizer :min-width="200" :min-height="150" :line-style="{ borderWidth: 0 }" :handle-style="{ opacity: 0 }" />

  <BaseNode
    title="思维链输出"
    :label="displayLabel"
    :selected="selected"
    @delete="workflowStore.removeNode(id)"
    themeColor="purple"
    class="w-full h-full"
  >
    <Handle type="target" :position="Position.Left" />

    <div class="flex flex-col gap-2 h-full">
      <div
        class="border rounded p-2 text-sm w-full flex-1 flex flex-col bg-purple-50 dark:bg-purple-950 border-purple-200 dark:border-purple-700 dark:text-purple-200"
      >
        <div class="flex items-center gap-1 mb-1 text-xs text-purple-500 dark:text-purple-400 font-medium">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
          <span>推理过程</span>
        </div>
        <textarea
          :value="data.thinkingContent || ''"
          @input="(e) => updateNodeData(id, { thinkingContent: (e.target as HTMLTextAreaElement).value })"
          placeholder="暂无思维链输出（仅推理模型会产生）"
          class="w-full flex-1 bg-transparent border-none resize-none focus:outline-none whitespace-pre-wrap nodrag text-inherit"
        ></textarea>
      </div>
    </div>
  </BaseNode>
</template>
