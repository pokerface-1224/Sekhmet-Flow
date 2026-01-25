import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Node, Edge } from '@vue-flow/core'
import { workflowApi, type WorkflowData } from '../services/workflowApi'
import { useLogStore } from './logStore'

export const useWorkflowStore = defineStore('workflow', () => {
  const nodes = ref<Node[]>([])
  const edges = ref<Edge[]>([])
  const currentWorkflowId = ref<string | null>(null)

  // Initialize or load workflow
  function loadWorkflow(workflowNodes: Node[], workflowEdges: Edge[], id?: string) {
    nodes.value = workflowNodes
    edges.value = workflowEdges
    if (id) currentWorkflowId.value = id
  }

  function addNode(node: Node) {
    nodes.value.push(node)
  }

  function addEdge(edge: Edge) {
    edges.value.push(edge)
  }

  function removeNode(nodeId: string) {
    const index = nodes.value.findIndex(n => n.id === nodeId)
    if (index !== -1) {
      nodes.value.splice(index, 1)
    }

    // Remove connected edges
    for (let i = edges.value.length - 1; i >= 0; i--) {
      const edge = edges.value[i]
      if (edge && (edge.source === nodeId || edge.target === nodeId)) {
        edges.value.splice(i, 1)
      }
    }
  }

  function removeEdge(edgeId: string) {
    const index = edges.value.findIndex(e => e.id === edgeId)
    if (index !== -1) {
      edges.value.splice(index, 1)
    }
  }

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

  async function fetchWorkflows() {
    try {
      const response = await workflowApi.getAll()
      return response.data
    } catch (error) {
      console.error('Failed to fetch workflows', error)
      return []
    }
  }

  async function runWorkflow() {
    const logStore = useLogStore()

    // Always save current state before running to ensure backend has latest graph
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

          // Log Input
          if (result.inputs && Object.keys(result.inputs).length > 0) {
            logStore.addLog('info', `Node Input: ${nodeLabel}`, result.inputs)
          }

          // Update Node Data with Outputs
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
