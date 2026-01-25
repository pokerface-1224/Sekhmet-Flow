<script setup lang="ts">
import Header from './components/layout/Header.vue'
import Sidebar from './features/editor/Sidebar.vue'
import EditorCanvas from './features/editor/EditorCanvas.vue'
import PropertyPanel from './features/editor/PropertyPanel.vue'
import LogPanel from './features/editor/LogPanel.vue'
</script>

<template>
  <div class="h-screen flex flex-col overflow-hidden bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100">
    <Header />
    <main class="flex-1 flex overflow-hidden">
      <Sidebar />
      <div class="flex-1 relative border-r border-gray-200 dark:border-gray-700">
        <EditorCanvas />
      </div>
      <PropertyPanel />
    </main>
    <LogPanel />
  </div>
</template>

<style>
#app {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  max-width: none;
  text-align: left;
}
</style>
