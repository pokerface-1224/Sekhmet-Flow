import type { Node } from '@vue-flow/core'

/**
 * 基础节点数据接口
 */
export interface NodeData {
  label?: string
  [key: string]: any
}

/**
 * LLM 节点数据接口
 * @property model 模型名称
 * @property temperature 温度参数 (0-2)
 * @property systemPrompt 系统提示词
 */
export interface LlmNodeData extends NodeData {
  model: string
  temperature: number
  systemPrompt?: string
}

/**
 * 提示词节点数据接口
 * @property prompt 提示词文本
 */
export interface PromptNodeData extends NodeData {
  prompt: string
}

/**
 * 聊天输出节点数据接口
 * @property output 输出内容
 */
export interface ChatOutputNodeData extends NodeData {
  output?: string
}

/** 应用节点类型定义 */
export type AppNode = Node<NodeData>
