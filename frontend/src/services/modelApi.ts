import api from './api';

export interface ModelConfig {
  id: string;
  provider: string; // 'openai兼容' | 'gemini' | 'deepseek'
  apiKey?: string;
  baseUrl?: string;
  modelName?: string;
  temperature?: number;
}

export interface DiscoverParams {
  provider: string;
  apiKey: string;
  baseUrl?: string;
}

export const fetchModels = async (): Promise<ModelConfig[]> => {
  const response = await api.get<ModelConfig[]>('/models');
  return response.data;
};

export const createOrUpdateModel = async (config: ModelConfig): Promise<void> => {
  await api.post('/models', config);
};

export const deleteModel = async (id: string): Promise<void> => {
  await api.delete(`/models/${id}`);
};

/**
 * 从提供商 API 发现可用模型
 */
export const discoverModels = async (params: DiscoverParams): Promise<string[]> => {
  const response = await api.get<string[]>('/models/discover', { params });
  return response.data;
};
