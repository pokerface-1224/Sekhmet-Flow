<script setup lang="ts">
import { Controls } from "@vue-flow/controls";
import "@vue-flow/controls/dist/style.css";
</script>

<template>
  <Controls
    position="bottom-left"
    class="!bg-white dark:!bg-gray-800 !border-gray-200 dark:!border-gray-700 !shadow-lg"
  >
  </Controls>
</template>

<style>
.vue-flow__controls-button {
  background-color: transparent;
  border-bottom: 1px solid #e5e7eb;
}

.dark .vue-flow__controls-button {
  border-bottom: 1px solid #374151;
  fill: #e5e7eb;
}

.dark .vue-flow__controls-button:hover {
  background-color: #374151;
}

.vue-flow__controls-button:last-child {
  border-bottom: none;
}
</style>
