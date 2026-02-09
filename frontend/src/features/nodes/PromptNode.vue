<template>
  <BaseNode
    title="提示词"
    :label="displayLabel"
    :selected="selected"
    @delete="workflowStore.removeNode(id)"
  >
    <div class="flex flex-col gap-2">
      <textarea
        :value="data.text"
        @input="
          (e) =>
            updateNodeData(id, {
              text: (e.target as HTMLTextAreaElement).value,
            })
        "
        class="border rounded p-2 text-sm w-full h-24 resize-none bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
        placeholder="在此输入提示词..."
      ></textarea>
    </div>
    <Handle type="source" :position="Position.Right" />
  </BaseNode>
</template>

<script setup lang="ts">
import type { NodeProps } from "@vue-flow/core";
import { Handle, Position, useVueFlow } from "@vue-flow/core";
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
const { updateNodeData } = useVueFlow();
</script>
