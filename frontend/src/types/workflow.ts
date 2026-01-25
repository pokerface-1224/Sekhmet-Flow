import type { Edge, Node } from '@vue-flow/core'

export interface Workflow {
  id: string
  name: string
  nodes: Node[]
  edges: Edge[]
  createdAt: string
  updatedAt: string
}
