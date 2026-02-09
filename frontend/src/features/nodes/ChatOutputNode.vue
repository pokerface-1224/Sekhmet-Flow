<script setup lang="ts">
import type { NodeProps } from "@vue-flow/core";
import { Handle, Position } from "@vue-flow/core";
import { NodeResizer } from "@vue-flow/node-resizer";
import "@vue-flow/node-resizer/dist/style.css";
import { computed } from "vue";
import { useWorkflowStore } from "../../stores/workflowStore";
import BaseNode from "./BaseNode.vue";

const props = defineProps<NodeProps>();

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
    title="聊天输出"
    :label="displayLabel"
    :selected="selected"
    @delete="workflowStore.removeNode(id)"
    class="w-full h-full"
  >
    <Handle type="target" :position="Position.Left" />

    <div class="flex flex-col gap-2 h-full">
      <div
        class="border rounded p-2 text-sm w-full flex-1 overflow-y-auto bg-gray-50 dark:bg-gray-900 dark:border-gray-600 dark:text-gray-200"
      >
        <span v-if="data.output" class="whitespace-pre-wrap">{{
          data.output
        }}</span>
        <span v-else class="text-gray-400 italic">正在等待输出...</span>
      </div>
    </div>
  </BaseNode>
</template>
