import api from './api';
import type { Node, Edge } from '@vue-flow/core';

/**
 * 工作流数据传输对象接口
 */
export interface WorkflowData {
    id?: string;
    name?: string;
    nodes: Node[];
    edges: Edge[];
}

/**
 * 工作流 API 服务对象
 * 封装与后端工作流相关的 HTTP 请求
 */
export const workflowApi = {
    /** 获取所有工作流 */
    getAll: () => api.get<WorkflowData[]>('/workflows'),
    /** 保存工作流 */
    save: (data: WorkflowData) => api.post<WorkflowData>('/workflows', data),
    /** 根据 ID 获取工作流详情 */
    get: (id: string) => api.get<WorkflowData>(`/workflows/${id}`),
    /** 运行工作流 */
    run: (id: string, inputs: Record<string, any> = {}) => api.post<Record<string, any>>(`/workflows/${id}/run`, inputs)
};
