<script setup lang="ts">
import { ref } from "vue";

const ghostRef = ref<HTMLElement | null>(null);

/**
 * 拖拽开始事件处理
 * 设置拖拽数据和自定义拖拽图像
 */
const onDragStart = (event: DragEvent, nodeType: string, label: string) => {
  if (event.dataTransfer) {
    if (ghostRef.value) {
      ghostRef.value.textContent = label;
      event.dataTransfer.setDragImage(ghostRef.value, 0, 0);
    }
    event.dataTransfer.setData("application/vueflow", nodeType);
    event.dataTransfer.effectAllowed = "move";
  }
};
</script>

<template>
  <aside
    class="w-64 h-full border-r border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 flex flex-col"
  >
    <div
      class="p-4 border-b border-gray-200 dark:border-gray-700 font-bold text-gray-700 dark:text-gray-200"
    >
      节点
    </div>

    <div class="p-4 flex flex-col gap-3">
      <!-- LLM 模型 - 绿色 -->
      <div
        class="border-2 border-gray-300 dark:border-gray-600 rounded-lg p-3 cursor-move bg-white dark:bg-gray-900 hover:border-emerald-500 transition-all duration-200 relative overflow-hidden group"
        draggable="true"
        @dragstart="onDragStart($event, 'llm-node', 'LLM 模型')"
      >
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-emerald-500 rounded-l-lg"></div>
        <div class="ml-2">
          <div class="font-medium text-gray-800 dark:text-gray-100 group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition-colors">LLM 模型</div>
          <div class="text-xs text-gray-500 dark:text-gray-400">
            大语言模型节点
          </div>
        </div>
      </div>

      <!-- 提示词 - 蓝色 -->
      <div
        class="border-2 border-gray-300 dark:border-gray-600 rounded-lg p-3 cursor-move bg-white dark:bg-gray-900 hover:border-blue-500 transition-all duration-200 relative overflow-hidden group"
        draggable="true"
        @dragstart="onDragStart($event, 'prompt-node', '提示词')"
      >
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-blue-500 rounded-l-lg"></div>
        <div class="ml-2">
          <div class="font-medium text-gray-800 dark:text-gray-100 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">提示词</div>
          <div class="text-xs text-gray-500 dark:text-gray-400">文本输入节点</div>
        </div>
      </div>

      <!-- 系统提示词 - 紫色 -->
      <div
        class="border-2 border-gray-300 dark:border-gray-600 rounded-lg p-3 cursor-move bg-white dark:bg-gray-900 hover:border-purple-500 transition-all duration-200 relative overflow-hidden group"
        draggable="true"
        @dragstart="onDragStart($event, 'system-prompt-node', '系统提示词')"
      >
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-purple-500 rounded-l-lg"></div>
        <div class="ml-2">
          <div class="font-medium text-gray-800 dark:text-gray-100 group-hover:text-purple-600 dark:group-hover:text-purple-400 transition-colors">系统提示词</div>
          <div class="text-xs text-gray-500 dark:text-gray-400">系统级指令</div>
        </div>
      </div>

      <!-- 聊天输出 - 琥珀色 -->
      <div
        class="border-2 border-gray-300 dark:border-gray-600 rounded-lg p-3 cursor-move bg-white dark:bg-gray-900 hover:border-amber-500 transition-all duration-200 relative overflow-hidden group"
        draggable="true"
        @dragstart="onDragStart($event, 'chat-output-node', '聊天输出')"
      >
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-amber-500 rounded-l-lg"></div>
        <div class="ml-2">
          <div class="font-medium text-gray-800 dark:text-gray-100 group-hover:text-amber-600 dark:group-hover:text-amber-400 transition-colors">聊天输出</div>
          <div class="text-xs text-gray-500 dark:text-gray-400">显示结果</div>
        </div>
      </div>

      <!-- 思维链输出 - 紫色 -->
      <div
        class="border-2 border-gray-300 dark:border-gray-600 rounded-lg p-3 cursor-move bg-white dark:bg-gray-900 hover:border-purple-500 transition-all duration-200 relative overflow-hidden group"
        draggable="true"
        @dragstart="onDragStart($event, 'thinking-output-node', '思维链输出')"
      >
        <div class="absolute left-0 top-0 bottom-0 w-1 bg-purple-500 rounded-l-lg"></div>
        <div class="ml-2">
          <div class="font-medium text-gray-800 dark:text-gray-100 group-hover:text-purple-600 dark:group-hover:text-purple-400 transition-colors">思维链输出</div>
          <div class="text-xs text-gray-500 dark:text-gray-400">显示推理过程</div>
        </div>
      </div>
    </div>

    <!-- Ghost Image Element -->
    <div
      ref="ghostRef"
      class="fixed top-[-1000px] left-[-1000px] bg-blue-600 text-white px-4 py-2 rounded-lg shadow-xl font-bold border-2 border-white z-50 pointer-events-none"
    ></div>
  </aside>
</template>
