import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Node } from '@vue-flow/core'

export const useUiStore = defineStore('ui', () => {
  const isSidebarOpen = ref(true)
  const selectedNode = ref<Node | null>(null)
  const isLocked = ref(false)

  function toggleSidebar() {
    isSidebarOpen.value = !isSidebarOpen.value
  }

  function setLocked(locked: boolean) {
    isLocked.value = locked
  }

  function selectNode(node: Node | null) {
    selectedNode.value = node
  }

  return {
    isSidebarOpen,
    selectedNode,
    isLocked,
    toggleSidebar,
    setLocked,
    selectNode
  }
})
