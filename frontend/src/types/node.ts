import type { Node } from '@vue-flow/core'

export interface NodeData {
  label?: string
  [key: string]: any
}

export interface LlmNodeData extends NodeData {
  model: string
  temperature: number
  systemPrompt?: string
}

export interface PromptNodeData extends NodeData {
  prompt: string
}

export interface ChatOutputNodeData extends NodeData {
  output?: string
}

export type AppNode = Node<NodeData>
