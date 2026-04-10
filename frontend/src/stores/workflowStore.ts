import type { Edge, Node } from "@vue-flow/core";
import { defineStore } from "pinia";
import { ref } from "vue";
import { workflowApi, type WorkflowData } from "../services/workflowApi";
import { useLogStore } from "./logStore";

/**
 * 工作流 Store
 * 核心状态管理，负责节点/边的增删改查及执行
 */
export const useWorkflowStore = defineStore("workflow", () => {
  const nodes = ref<Node[]>([]);
  const edges = ref<Edge[]>([]);
  const currentWorkflowId = ref<string | null>(null);
  const currentWorkflowName = ref<string>("未命名工作流");

  /**
   * 加载工作流数据
   * @param workflowNodes 节点列表
   * @param workflowEdges 边列表
   * @param id 工作流 ID
   * @param name 工作流名称
   */
  function loadWorkflow(
    workflowNodes: Node[],
    workflowEdges: Edge[],
    id?: string,
    name?: string,
  ) {
    nodes.value = workflowNodes;
    edges.value = workflowEdges;
    if (id) currentWorkflowId.value = id;
    if (name) currentWorkflowName.value = name;
  }

  /** 添加节点 */
  function addNode(node: Node) {
    nodes.value.push(node);
  }

  /** 添加边 */
  function addEdge(edge: Edge) {
    edges.value.push(edge);
  }

  /** 移除节点及其关联的边 */
  function removeNode(nodeId: string) {
    const index = nodes.value.findIndex((n) => n.id === nodeId);
    if (index !== -1) {
      nodes.value.splice(index, 1);
    }

    // 移除连接的边
    for (let i = edges.value.length - 1; i >= 0; i--) {
      const edge = edges.value[i];
      if (edge && (edge.source === nodeId || edge.target === nodeId)) {
        edges.value.splice(i, 1);
      }
    }
  }

  /** 移除边 */
  function removeEdge(edgeId: string) {
    const index = edges.value.findIndex((e) => e.id === edgeId);
    if (index !== -1) {
      edges.value.splice(index, 1);
    }
  }

  /**
   * 保存当前工作流
   * 将节点和边的状态同步到后端
   */
  async function saveWorkflow() {
    const data: WorkflowData = {
      id: currentWorkflowId.value || undefined,
      name: currentWorkflowName.value,
      nodes: nodes.value,
      edges: edges.value,
    };
    try {
      const response = await workflowApi.save(data);
      if (response.data.id) {
        currentWorkflowId.value = response.data.id;
        console.log("工作流已保存", response.data);
      }
    } catch (error) {
      console.error("保存工作流失败", error);
      throw error;
    }
  }

  /** 获取所有工作流列表 */
  async function fetchWorkflows() {
    try {
      const response = await workflowApi.getAll();
      return response.data;
    } catch (error) {
      console.error("获取工作流失败", error);
      return [];
    }
  }

  /**
   * 运行当前工作流
   * 1. 自动保存最新状态
   * 2. 调用后端执行接口
   * 3. 处理执行结果并更新节点数据
   */
  async function runWorkflow() {
    const logStore = useLogStore();

    // 运行前始终保存当前状态，确保后端获取最新图结构
    logStore.addLog("info", "正在保存工作流配置...");
    try {
      await saveWorkflow();
      logStore.addLog("info", "工作流已保存。正在请求执行...");
    } catch (e: any) {
      logStore.addLog("error", "执行前保存工作流失败", e.message);
      throw e;
    }

    if (!currentWorkflowId.value) return;

    try {
      const response = await workflowApi.run(currentWorkflowId.value);

      let hasErrors = false;
      const executionResult = response.data;

      if (executionResult && executionResult.nodeResults) {
        Object.entries(executionResult.nodeResults).forEach(
          ([nodeId, result]: [string, any]) => {
            const node = nodes.value.find((n) => n.id === nodeId);
            const nodeLabel = node ? node.label || node.id : nodeId;

            // Update node data
            if (result.outputs && node) {
              // Separate _execution_config from actual outputs
              const { _execution_config, ...actualOutputs } = result.outputs;
              node.data = { ...node.data, ...actualOutputs };
            }

            // Build structured log entry
            const logDetails: any = {};

            // Config section (parameters)
            if (result.outputs?._execution_config) {
              const config = result.outputs._execution_config;
              logDetails.配置 = {
                模型: config.model,
                供应商: config.provider,
                温度: config.temperature,
                topK: config.topK,
                topP: config.topP,
              };
              if (config.tokenUsage) {
                const input = config.tokenUsage.input || 0;
                const output = config.tokenUsage.output || 0;
                logDetails.Token消耗 = `Input: ${input} | Output: ${output}`;
              }
              if (config.finalPromptLayout) {
                logDetails.最终提示词 = config.finalPromptLayout;
              }
            }

            // Response preview (LLM node)
            if (result.outputs?.response) {
              const resp = result.outputs.response;
              logDetails.响应 = resp.length > 200 ? resp.substring(0, 200) + '...' : resp;
            }

            // Prompt text (Prompt node)
            if (result.outputs?.text) {
              const text = result.outputs.text;
              logDetails.提示词内容 = text.length > 200 ? text.substring(0, 200) + '...' : text;
            }

            // System Prompt
            const sysPrompt = result.inputs?.systemPrompt || result.outputs?.systemPrompt;
            if (sysPrompt) {
              logDetails.系统提示词 = sysPrompt.length > 200 ? sysPrompt.substring(0, 200) + '...' : sysPrompt;
            }

            // Chat output (ChatOutput node)
            if (result.outputs?.output) {
              const output = result.outputs.output;
              logDetails.输出内容 = output.length > 200 ? output.substring(0, 200) + '...' : output;
            }

            // Duration
            if (result.durationMs) {
              logDetails.耗时 = `${result.durationMs}ms`;
            }

            // Error
            if (result.status === "ERROR") {
              hasErrors = true;
              logDetails.错误 = result.error;
              logStore.addLog("error", `❌ ${nodeLabel}`, logDetails);
            } else {
              logStore.addLog("success", `✅ ${nodeLabel}`, logDetails);
            }
          },
        );
      }

      if (!hasErrors) {
        logStore.addLog("success", "工作流执行完成");
      } else {
        logStore.addLog(
          "warning",
          "工作流执行完成，但存在错误",
        );
      }

      return response.data;
    } catch (error: any) {
      const msg = error.response?.data?.message || error.message || "未知错误";
      logStore.addLog("error", "工作流执行请求失败", msg);
      console.error("运行工作流失败", error);
      throw error;
    }
  }

  return {
    nodes,
    edges,
    currentWorkflowId,
    currentWorkflowName,
    loadWorkflow,
    addNode,
    addEdge,
    removeNode,
    removeEdge,
    saveWorkflow,
    fetchWorkflows,
    runWorkflow,
  };
});
