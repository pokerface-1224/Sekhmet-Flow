<script setup lang="ts">
import { Background } from "@vue-flow/background";
import {
  VueFlow,
  useVueFlow,
  type EdgeChange,
  type Node,
  type NodeChange,
  type NodeMouseEvent,
} from "@vue-flow/core";
import "@vue-flow/core/dist/style.css";
import "@vue-flow/core/dist/theme-default.css";
import { watch } from "vue";
import { useUiStore } from "../../stores/uiStore";
import { useWorkflowStore } from "../../stores/workflowStore";
import ChatOutputNode from "../nodes/ChatOutputNode.vue";
import LlmNode from "../nodes/LlmNode.vue";
import PromptNode from "../nodes/PromptNode.vue";
import SystemPromptNode from "../nodes/SystemPromptNode.vue";
import ThinkingOutputNode from "../nodes/ThinkingOutputNode.vue";
import Controls from "./Controls.vue";

const workflowStore = useWorkflowStore();
const uiStore = useUiStore();
// onConnect: 处理连线事件
// ... (omitted lines)

const nodeTypes = {
  "llm-node": LlmNode,
  "prompt-node": PromptNode,
  "chat-output-node": ChatOutputNode,
  "system-prompt-node": SystemPromptNode,
  "thinking-output-node": ThinkingOutputNode,
};
// project: 坐标转换
// nodesDraggable: 节点拖拽状态
const { onConnect, project, nodesDraggable } = useVueFlow();

watch(nodesDraggable, (draggable) => {
  uiStore.setLocked(!draggable);
});

const onNodesChangeHandler = (changes: NodeChange[]) => {
  changes.forEach((change) => {
    if (change.type === "remove") {
      if (!nodesDraggable.value) return;
      workflowStore.removeNode(change.id);
    }
  });
};

const onEdgesChangeHandler = (changes: EdgeChange[]) => {
  changes.forEach((change) => {
    if (change.type === "remove") {
      if (!nodesDraggable.value) return;
      workflowStore.removeEdge(change.id);
    }
  });
};


// Initial state sync (optional, usually start empty or load from store)
// For now, we rely on the store being the source of truth if we were to load it,
// but VueFlow maintains its own state. We can sync back to store on changes.

// 处理连接事件
onConnect((params) => {
  const newEdge = {
    ...params,
    id: `edge_${Date.now()}`,
  };
  workflowStore.addEdge(newEdge);
});

const onDragOver = (event: DragEvent) => {
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = "move";
  }
};

const onDrop = (event: DragEvent) => {
  const type = event.dataTransfer?.getData("application/vueflow");
  if (!type) return;

  const { left, top } = (event.target as Element).getBoundingClientRect();

  const position = project({
    x: event.clientX - left,
    y: event.clientY - top,
  });

  const newNode: Node = {
    id: `node_${Date.now()}`,
    type,
    position,
    label:
      type === "llm-node"
        ? "LLM 模型"
        : type === "prompt-node"
          ? "提示词"
          : type === "system-prompt-node"
            ? "系统提示词"
            : type === "thinking-output-node"
              ? "思维链输出"
              : "聊天输出",
    data: {
      label: type,
    },
  };

  workflowStore.addNode(newNode);
};

const onNodeClick = (event: NodeMouseEvent) => {
  uiStore.selectNode(event.node);
};

const onPaneClick = () => {
  uiStore.selectNode(null);
};
</script>

<template>
  <div class="h-full w-full" @drop="onDrop" @dragover="onDragOver">
    <VueFlow
      v-model:nodes="workflowStore.nodes"
      v-model:edges="workflowStore.edges"
      :node-types="nodeTypes"
      @nodes-change="onNodesChangeHandler"
      @edges-change="onEdgesChangeHandler"
      @node-click="onNodeClick"
      @pane-click="onPaneClick"
      class="bg-gray-50 dark:bg-gray-900"
    >
      <Background />
      <Controls />
    </VueFlow>
  </div>
</template>
