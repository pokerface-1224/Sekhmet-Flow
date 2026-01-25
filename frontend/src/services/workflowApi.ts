import api from './api';
import type { Node, Edge } from '@vue-flow/core';

export interface WorkflowData {
    id?: string;
    name?: string;
    nodes: Node[];
    edges: Edge[];
}

export const workflowApi = {
    getAll: () => api.get<WorkflowData[]>('/workflows'),
    save: (data: WorkflowData) => api.post<WorkflowData>('/workflows', data),
    get: (id: string) => api.get<WorkflowData>(`/workflows/${id}`),
    run: (id: string, inputs: Record<string, any> = {}) => api.post<Record<string, any>>(`/workflows/${id}/run`, inputs)
};
