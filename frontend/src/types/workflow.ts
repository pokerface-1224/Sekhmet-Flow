import type { Edge, Node } from '@vue-flow/core'

/**
 * 工作流接口定义
 * @property id 工作流 ID
 * @property name 工作流名称
 * @property nodes 节点列表
 * @property edges 边列表 (连接关系)
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
export interface Workflow {
  id: string
  name: string
  nodes: Node[]
  edges: Edge[]
  createdAt: string
  updatedAt: string
}
