<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { useLogStore } from "../../stores/logStore";
import { useWorkflowStore } from "../../stores/workflowStore";
import type { WorkflowData } from "../../services/workflowApi";

const workflowStore = useWorkflowStore();
const logStore = useLogStore();

/** 打开独立日志窗口 */
function openLogsWindow() {
  window.open('/logs', '_blank');
}
const isRunning = ref(false);
const isSaving = ref(false);
const showWorkflowList = ref(false);
const workflows = ref<WorkflowData[]>([]);
const listContainer = ref<HTMLElement | null>(null);

async function handleRun() {
  isRunning.value = true;

  try {
    await workflowStore.runWorkflow();
  } catch (error: any) {
    // Error is already logged in store
  } finally {
    isRunning.value = false;
  }
}

async function handleSave() {
  isSaving.value = true;
  try {
    await workflowStore.saveWorkflow();
    logStore.addLog("success", "工作流保存成功");
  } catch (error) {
    logStore.addLog("error", "保存失败", String(error));
  } finally {
    isSaving.value = false;
  }
}

async function handleFetchWorkflows() {
  showWorkflowList.value = !showWorkflowList.value;
  if (showWorkflowList.value) {
    workflows.value = await workflowStore.fetchWorkflows();
  }
}

function handleLoadWorkflow(workflow: WorkflowData) {
  workflowStore.loadWorkflow(
    workflow.nodes || [],
    workflow.edges || [],
    workflow.id,
    workflow.name
  );
  showWorkflowList.value = false;
  logStore.addLog("info", `已加载工作流: ${workflow.name || "未命名"}`);
}

// Close dropdown when clicking outside
function handleClickOutside(event: MouseEvent) {
  if (
    listContainer.value &&
    !listContainer.value.contains(event.target as Node)
  ) {
    showWorkflowList.value = false;
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<template>
  <header
    class="h-16 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 flex items-center px-6 justify-between"
  >
    <div class="flex items-center gap-2">
      <div
        class="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center text-white font-bold text-lg"
      >
        S
      </div>
      <h1 class="text-xl font-bold text-gray-800 dark:text-gray-100">
        Sekhmet
      </h1>
    </div>

    <div class="flex items-center gap-4" ref="listContainer">
      <!-- Workflow Name Input -->
      <input
        v-model="workflowStore.currentWorkflowName"
        type="text"
        placeholder="工作流名称"
        class="bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200 px-3 py-1.5 rounded-md text-sm border-none focus:ring-2 focus:ring-blue-500 outline-none w-48"
      />

      <div class="relative">
        <button
          @click.stop="handleFetchWorkflows"
          class="bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-2"
        >
          读取
          <span class="text-xs">▼</span>
        </button>

        <!-- Workflow List Dropdown -->
        <div
          v-if="showWorkflowList"
          class="absolute top-full right-0 mt-2 w-64 bg-white dark:bg-gray-800 rounded-md shadow-lg border border-gray-200 dark:border-gray-700 py-1 z-50 max-h-96 overflow-y-auto"
        >
          <div
            v-if="workflows.length === 0"
            class="px-4 py-3 text-sm text-gray-500 dark:text-gray-400 text-center"
          >
            暂无保存的工作流
          </div>
          <button
            v-else
            v-for="wf in workflows"
            :key="wf.id"
            @click="handleLoadWorkflow(wf)"
            class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-700 flex flex-col gap-0.5"
          >
            <span class="font-medium truncate">{{ wf.name || "未命名" }}</span>
            <span class="text-xs text-gray-500 dark:text-gray-400 truncate">{{
              wf.id
            }}</span>
          </button>
        </div>
      </div>

      <button
        @click="openLogsWindow"
        class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 px-3 py-2 text-sm transition-colors flex items-center gap-1"
        title="在新窗口中打开执行日志"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
        </svg>
        日志
      </button>

      <button
        @click="handleRun"
        :disabled="isRunning"
        class="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-2"
      >
        <span
          v-if="isRunning"
          class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"
        ></span>
        {{ isRunning ? "运行中..." : "运行" }}
      </button>

      <button
        @click="handleSave"
        :disabled="isSaving"
        class="bg-green-600 hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-2"
      >
        <span
          v-if="isSaving"
          class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"
        ></span>
        {{ isSaving ? "保存中..." : "保存" }}
      </button>
    </div>
  </header>
</template>
