<script setup lang="ts">
import { computed } from "vue";
import { useUiStore } from "../../stores/uiStore";

const props = defineProps<{
  title: string;
  label?: string | number;
  selected?: boolean;
  themeColor?: string;
}>();

defineEmits<{
  (e: "delete"): void;
}>();

const uiStore = useUiStore();

/**
 * 颜色映射表：themeColor => Tailwind 类名
 * 支持的主题色：blue, emerald, purple, amber, rose
 */
const colorMap: Record<string, { border: string; headerBg: string; headerText: string; accent: string; labelBg: string; labelText: string }> = {
  blue: {
    border: 'border-blue-500',
    headerBg: 'bg-blue-50 dark:bg-blue-950',
    headerText: 'text-blue-900 dark:text-blue-100',
    accent: 'bg-blue-500',
    labelBg: 'bg-blue-100 dark:bg-blue-900',
    labelText: 'text-blue-600 dark:text-blue-300',
  },
  emerald: {
    border: 'border-emerald-500',
    headerBg: 'bg-emerald-50 dark:bg-emerald-950',
    headerText: 'text-emerald-900 dark:text-emerald-100',
    accent: 'bg-emerald-500',
    labelBg: 'bg-emerald-100 dark:bg-emerald-900',
    labelText: 'text-emerald-600 dark:text-emerald-300',
  },
  purple: {
    border: 'border-purple-500',
    headerBg: 'bg-purple-50 dark:bg-purple-950',
    headerText: 'text-purple-900 dark:text-purple-100',
    accent: 'bg-purple-500',
    labelBg: 'bg-purple-100 dark:bg-purple-900',
    labelText: 'text-purple-600 dark:text-purple-300',
  },
  amber: {
    border: 'border-amber-500',
    headerBg: 'bg-amber-50 dark:bg-amber-950',
    headerText: 'text-amber-900 dark:text-amber-100',
    accent: 'bg-amber-500',
    labelBg: 'bg-amber-100 dark:bg-amber-900',
    labelText: 'text-amber-600 dark:text-amber-300',
  },
  rose: {
    border: 'border-rose-500',
    headerBg: 'bg-rose-50 dark:bg-rose-950',
    headerText: 'text-rose-900 dark:text-rose-100',
    accent: 'bg-rose-500',
    labelBg: 'bg-rose-100 dark:bg-rose-900',
    labelText: 'text-rose-600 dark:text-rose-300',
  },
};

const defaultColors = {
  border: 'border-blue-500',
  headerBg: 'bg-gray-50 dark:bg-gray-900',
  headerText: 'text-gray-900 dark:text-gray-100',
  accent: 'bg-gray-400',
  labelBg: 'bg-gray-200 dark:bg-gray-800',
  labelText: 'text-gray-500 dark:text-gray-400',
};

const theme = computed(() => {
  if (props.themeColor && colorMap[props.themeColor]) {
    return colorMap[props.themeColor];
  }
  return defaultColors;
});
</script>

<template>
  <div
    class="bg-white dark:bg-gray-800 border-2 rounded-lg shadow-md min-w-[200px] flex flex-col h-full transition-all duration-200"
    :class="
      selected ? theme.border : 'border-gray-200 dark:border-gray-700'
    "
  >
    <div
      class="px-4 py-2 border-b border-gray-200 dark:border-gray-700 rounded-t-lg font-medium flex justify-between items-center shrink-0 relative overflow-hidden"
      :class="[theme.headerBg, theme.headerText]"
    >
      <!-- 左侧色条 -->
      <div class="absolute left-0 top-0 bottom-0 w-1 rounded-tl-lg" :class="theme.accent"></div>
      <div class="flex items-center gap-2 ml-1">
        <span>{{ title }}</span>
        <span
          v-if="label"
          class="text-xs font-normal px-1.5 py-0.5 rounded"
          :class="[theme.labelBg, theme.labelText]"
        >
          {{ label }}
        </span>
      </div>
      <button
        v-if="!uiStore.isLocked"
        @click.stop="$emit('delete')"
        class="text-gray-400 hover:text-red-500 dark:text-gray-500 dark:hover:text-red-400 transition-colors"
        title="删除节点"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M6 18L18 6M6 6l12 12"
          />
        </svg>
      </button>
    </div>
    <div class="p-4 flex-1 overflow-hidden flex flex-col">
      <slot />
    </div>
  </div>
</template>
