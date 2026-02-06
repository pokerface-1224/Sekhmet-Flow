import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Node } from '@vue-flow/core'

/**
 * UI 状态 Store
 * 管理界面交互状态，如侧边栏开关、节点选中等
 */
export const useUiStore = defineStore('ui', () => {
  const isSidebarOpen = ref(true)
  const selectedNode = ref<Node | null>(null)
  /** 画布锁定状态 */
  const isLocked = ref(false)

  /** 切换侧边栏显示 */
  function toggleSidebar() {
    isSidebarOpen.value = !isSidebarOpen.value
  }

  /** 设置画布锁定状态 */
  function setLocked(locked: boolean) {
    isLocked.value = locked
  }

  /**
   * 设置当前选中的节点
   * @param node 选中的节点对象，null 表示取消选中
   */
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
