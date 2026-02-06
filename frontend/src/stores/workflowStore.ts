import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Node, Edge } from '@vue-flow/core'
import { workflowApi, type WorkflowData } from '../services/workflowApi'
import { useLogStore } from './logStore'

/**
 * 工作流 Store
 * 核心状态管理，负责节点/边的增删改查及执行
 */
export const useWorkflowStore = defineStore('workflow', () => {
  const nodes = ref<Node[]>([])
  const edges = ref<Edge[]>([])
  const currentWorkflowId = ref<string | null>(null)

  /**
   * 加载工作流数据
   * @param workflowNodes 节点列表
   * @param workflowEdges 边列表
   * @param id 工作流 ID
   */
  function loadWorkflow(workflowNodes: Node[], workflowEdges: Edge[], id?: string) {
    nodes.value = workflowNodes
    edges.value = workflowEdges
    if (id) currentWorkflowId.value = id
  }

  /** 添加节点 */
  function addNode(node: Node) {
    nodes.value.push(node)
  }

  /** 添加边 */
  function addEdge(edge: Edge) {
    edges.value.push(edge)
  }

  /** 移除节点及其关联的边 */
  function removeNode(nodeId: string) {
    const index = nodes.value.findIndex(n => n.id === nodeId)
    if (index !== -1) {
      nodes.value.splice(index, 1)
    }

    // 移除连接的边
    for (let i = edges.value.length - 1; i >= 0; i--) {
      const edge = edges.value[i]
      if (edge && (edge.source === nodeId || edge.target === nodeId)) {
        edges.value.splice(i, 1)
      }
    }
  }

  /** 移除边 */
  function removeEdge(edgeId: string) {
    const index = edges.value.findIndex(e => e.id === edgeId)
    if (index !== -1) {
      edges.value.splice(index, 1)
    }
  }

  /**
   * 保存当前工作流
   * 将节点和边的状态同步到后端
   */
  async function saveWorkflow() {
    const data: WorkflowData = {
      id: currentWorkflowId.value || undefined,
      nodes: nodes.value,
      edges: edges.value
    }
    try {
      const response = await workflowApi.save(data)
      if (response.data.id) {
        currentWorkflowId.value = response.data.id
        console.log('Saved workflow', response.data)
      }
    } catch (error) {
      console.error('Failed to save workflow', error)
      throw error
    }
  }

  /** 获取所有工作流列表 */
  async function fetchWorkflows() {
    try {
      const response = await workflowApi.getAll()
      return response.data
    } catch (error) {
      console.error('Failed to fetch workflows', error)
      return []
    }
  }

  /**
   * 运行当前工作流
   * 1. 自动保存最新状态
   * 2. 调用后端执行接口
   * 3. 处理执行结果并更新节点数据
   */
  async function runWorkflow() {
    const logStore = useLogStore()

    // 运行前始终保存当前状态，确保后端获取最新图结构
    logStore.addLog('info', 'Saving workflow configuration...')
    try {
      await saveWorkflow()
      logStore.addLog('info', 'Workflow saved. Requesting execution...')
    } catch (e: any) {
      logStore.addLog('error', 'Failed to save workflow before execution', e.message)
      throw e
    }

    if (!currentWorkflowId.value) return;

    try {
      const response = await workflowApi.run(currentWorkflowId.value)

      let hasErrors = false
      const executionResult = response.data

      if (executionResult && executionResult.nodeResults) {
        Object.entries(executionResult.nodeResults).forEach(([nodeId, result]: [string, any]) => {
          const node = nodes.value.find(n => n.id === nodeId)
          const nodeLabel = node ? (node.label || node.id) : nodeId

          // 记录输入参数
          if (result.inputs && Object.keys(result.inputs).length > 0) {
            logStore.addLog('info', `Node Input: ${nodeLabel}`, result.inputs)
          }

          // 更新节点数据和输出
          if (result.outputs) {
            if (node) {
              node.data = { ...node.data, ...result.outputs }
            }
            logStore.addLog(result.status === 'ERROR' ? 'error' : 'success', `Node Output: ${nodeLabel}`, result.outputs)
          }

          if (result.status === 'ERROR') {
            hasErrors = true
            logStore.addLog('error', `Node Failed: ${nodeLabel}`, result.error)
          }
        })
      }

      if (!hasErrors) {
        logStore.addLog('success', 'Workflow executed successfully', executionResult)
      } else {
        logStore.addLog('warning', 'Workflow executed with errors', executionResult)
      }

      return response.data
    } catch (error: any) {
      const msg = error.response?.data?.message || error.message || 'Unknown error'
      logStore.addLog('error', 'Workflow execution request failed', msg)
      console.error('Failed to run workflow', error)
      throw error
    }
  }

  return {
    nodes,
    edges,
    currentWorkflowId,
    loadWorkflow,
    addNode,
    addEdge,
    removeNode,
    removeEdge,
    saveWorkflow,
    fetchWorkflows,
    runWorkflow
  }
})
