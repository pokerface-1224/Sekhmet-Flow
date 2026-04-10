<template>
  <BaseNode
    title="LLM 模型"
    :label="displayLabel"
    :selected="selected"
    @delete="workflowStore.removeNode(id)"
    themeColor="emerald"
    class="min-w-[320px]"
  >
    <Handle type="target" :position="Position.Left" />

    <div class="flex flex-col gap-2">
      
      <!-- 1. Provider -->
      <label class="text-sm text-gray-500 dark:text-gray-400">供应商</label>
      <select
        v-model="providerInput"
        @change="updateProvider"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
      >
        <option value="openai兼容">OpenAI兼容</option>
        <option value="gemini">Google Gemini</option>
        <option value="deepseek">Deepseek</option>
      </select>

      <!-- 2. Base URL -->
      <label class="text-sm text-gray-500 dark:text-gray-400">Base URL (仅openai兼容需填写)</label>
      <input
        type="text"
        v-model="baseUrlInput"
        @input="updateBaseUrl"
        placeholder="自定义 Base URL"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
      />

      <!-- 3. API Key -->
      <label class="text-sm text-gray-500 dark:text-gray-400">API 密钥</label>
      <input
        type="password"
        v-model="apiKeyInput"
        @input="updateApiKey"
        placeholder="临时输入 (不会保存到工作流文件)"
        class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
      />
      <p class="text-xs text-gray-400 dark:text-gray-500 -mt-1">Key 通过模型注册持久化到本地数据库，不写入工作流文件</p>

      <!-- 4. Model Input & Selection -->
      <div class="flex justify-between items-center">
        <label class="text-sm text-gray-500 dark:text-gray-400">模型名称 (或选择)</label>
        <button
            @click="handleDiscover"
            :disabled="isDiscovering"
            class="px-2 py-0.5 text-xs rounded bg-blue-500 text-white hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed nodrag"
        >
            {{ isDiscovering ? '发现中...' : '发现' }}
        </button>
      </div>
      <div class="relative group">
        <input
          type="text"
          v-model="modelNameInput"
          @input="handleModelNameInput"
          @focus="showDropdown = true"
          @blur="handleBlur"
          placeholder="输入或选择模型..."
          class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag w-full"
        />
        <!-- Dropdown List -->
        <div 
            v-if="showDropdown && (availableModels.length > 0 || discoveredModels.length > 0)"
            class="absolute z-10 w-full mt-1 bg-white dark:bg-gray-800 border dark:border-gray-600 rounded shadow-lg max-h-48 overflow-y-auto"
            @mousedown.prevent 
        >
            <!-- Registered Models -->
            <div v-if="availableModels.length > 0">
                <div class="px-2 py-1 text-xs font-bold text-gray-500 bg-gray-100 dark:bg-gray-700">已注册模型</div>
                <div 
                    v-for="model in availableModels" 
                    :key="model.id"
                    @click="selectModel(model.id, model.modelName || '', true)"
                    class="px-2 py-1 text-sm hover:bg-blue-100 dark:hover:bg-gray-700 cursor-pointer text-gray-800 dark:text-gray-200"
                >
                    {{ model.modelName || model.id }} <span class="text-xs text-gray-400">({{ model.id }})</span>
                </div>
            </div>

            <!-- Discovered Models -->
            <div v-if="discoveredModels.length > 0">
                <div class="px-2 py-1 text-xs font-bold text-gray-500 bg-gray-100 dark:bg-gray-700">发现的新模型</div>
                <div 
                    v-for="modelName in discoveredModels" 
                    :key="modelName"
                    @click="selectModel(getDiscoveredId(modelName), modelName, false)"
                    class="px-2 py-1 text-sm hover:bg-blue-100 dark:hover:bg-gray-700 cursor-pointer text-gray-800 dark:text-gray-200"
                >
                    {{ modelName }}
                </div>
            </div>
        </div>
      </div>

      <!-- 5. Configuration Area (Temperature) -->
      <div class="flex flex-col gap-2 border-t pt-2 mt-2 dark:border-gray-700">
        <label class="text-sm text-gray-500 dark:text-gray-400">温度</label>
        <input
          type="number"
          step="0.1"
          min="0"
          max="2"
          :value="typeof data.temperature === 'number' ? data.temperature : 0.7"
          @input="
            (e) =>
              updateNodeData(id, {
                temperature: parseFloat((e.target as HTMLInputElement).value),
              })
          "
          class="border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
        />
      </div>

      <!-- Advanced Settings Toggle -->
      <div class="flex items-center gap-2 mt-2">
        <input 
          type="checkbox" 
          id="advanced-mode" 
          v-model="isAdvanced" 
          class="rounded text-blue-500 focus:ring-blue-500 bg-gray-100 dark:bg-gray-700 border-gray-300 dark:border-gray-600"
        />
        <label for="advanced-mode" class="text-xs text-gray-500 dark:text-gray-400 cursor-pointer select-none">
          高级设置 (Top K / Top P)
        </label>
      </div>

      <!-- Advanced Inputs -->
      <div v-if="isAdvanced" class="flex flex-col gap-2 mt-2 pt-2 border-t dark:border-gray-700 animate-in fade-in slide-in-from-top-1">
        <div class="grid grid-cols-2 gap-2">
            <div>
                <label class="text-xs text-gray-500 dark:text-gray-400">Top K</label>
                <input
                type="number"
                min="0"
                step="1"
                :value="data.topK"
                @input="(e) => updateNodeData(id, { topK: parseInt((e.target as HTMLInputElement).value) || undefined })"
                placeholder="默认"
                class="w-full border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
                />
            </div>
            <div>
                <label class="text-xs text-gray-500 dark:text-gray-400">Top P</label>
                <input
                type="number"
                min="0"
                max="1"
                step="0.05"
                :value="data.topP"
                @input="(e) => updateNodeData(id, { topP: parseFloat((e.target as HTMLInputElement).value) || undefined })"
                placeholder="默认"
                class="w-full border rounded p-1 text-sm bg-transparent dark:border-gray-600 dark:text-gray-200 nodrag"
                />
            </div>
        </div>
      </div>
    </div>

    <Handle type="source" :position="Position.Right" />
  </BaseNode>
</template>

<script setup lang="ts">
import type { NodeProps } from "@vue-flow/core";
import { Handle, Position, useVueFlow } from "@vue-flow/core";
import { computed, onMounted, ref, watch } from "vue";
import { useWorkflowStore } from "../../stores/workflowStore";
import BaseNode from "./BaseNode.vue";
import {
  fetchModels,
  discoverModels,
  createOrUpdateModel,
  type ModelConfig,
} from "../../services/modelApi";

const props = defineProps<NodeProps>();

const displayLabel = computed(() => {
  if (typeof props.label === "string" || typeof props.label === "number") {
    return props.label;
  }
  return undefined;
});

const workflowStore = useWorkflowStore();
const { updateNodeData } = useVueFlow();

// Model pool state
const availableModels = ref<ModelConfig[]>([]);

// Discovery state
const isDiscovering = ref(false);
const discoveredModels = ref<string[]>([]);
const showDropdown = ref(false);
const isAdvanced = ref(false);

// Local inputs synced with node data
const providerInput = ref(props.data.provider || 'openai');
const apiKeyInput = ref(props.data.apiKey || '');
const baseUrlInput = ref(props.data.baseUrl || '');
const modelNameInput = ref(props.data.modelName || '');

// Sync local inputs when node data changes externally
watch(() => props.data, (newData) => {
    if (newData.provider !== undefined) providerInput.value = newData.provider;
    if (newData.apiKey !== undefined) apiKeyInput.value = newData.apiKey;
    if (newData.baseUrl !== undefined) baseUrlInput.value = newData.baseUrl;
    if (newData.modelName !== undefined) modelNameInput.value = newData.modelName;
}, { deep: true });

onMounted(async () => {
  try {
    availableModels.value = await fetchModels();
  } catch (e) {
    console.error("Failed to fetch models:", e);
  }
});

function getDiscoveredId(modelName: string) {
    return `${providerInput.value}-${modelName}`;
}

// Close dropdown on blur with delay to allow clicking
function handleBlur() {
    setTimeout(() => {
        showDropdown.value = false;
    }, 200);
}

// When manual typing occurs, we clear the modelId (switching to custom mode)
// unless the typed name EXACTLY matches a registered model... but it's safer to clear id.
function handleModelNameInput() {
    // Check if input matches an existing model name?
    // User might want to type "gpt-4" to use the pool model "openai-gpt-4".
    // For now, assume manual entry implies custom config overrides.
    updateNodeData(props.id, { 
        modelName: modelNameInput.value,
        modelId: '' // Clear pooled ID to use manual config
    });
}

async function handleDiscover() {

  isDiscovering.value = true;
  discoveredModels.value = [];
  showDropdown.value = true; // Show results immediately

  try {
    const models = await discoverModels({
      provider: providerInput.value,
      apiKey: apiKeyInput.value,
      baseUrl: baseUrlInput.value || undefined,
    });
    discoveredModels.value = models;
  } catch (e) {
    console.error("Failed to discover models:", e);
    // Silent fail
  } finally {
    isDiscovering.value = false;
  }
}

async function selectModel(modelId: string, modelName: string, isRegistered: boolean) {
    modelNameInput.value = modelName;
    updateNodeData(props.id, { modelName: modelName });
    
    // Register if needed
    if (!isRegistered) {
        const config: ModelConfig = {
            id: modelId,
            provider: providerInput.value,
            apiKey: apiKeyInput.value,
            baseUrl: baseUrlInput.value || undefined,
            modelName: modelName,
            temperature: 0.7,
        };
        try {
            await createOrUpdateModel(config);
            availableModels.value = await fetchModels(); 
        } catch (err) {
            console.error("Silent registration failed:", err);
        }
    }

    // Set modelId to use pooled instance
    updateNodeData(props.id, { modelId: modelId });
    
    // If registered, pull its config to UI
    if (isRegistered || !isRegistered) { // Logic holds for both since we just registered it
       // Find config (refresh might be needed for newly registered)
       // We can just use current inputs if discovered
       // But if user selected an existing registered model, we should update inputs
       const registered = availableModels.value.find(m => m.id === modelId);
       if (registered) {
           providerInput.value = registered.provider;
           updateProvider();
           // apiKeyInput.value = registered.apiKey; // Optional: show masked?
       }
    }
    
    showDropdown.value = false;
}

// Update helpers
const updateProvider = () => updateNodeData(props.id, { provider: providerInput.value });
const updateApiKey = () => updateNodeData(props.id, { apiKey: apiKeyInput.value });
const updateBaseUrl = () => updateNodeData(props.id, { baseUrl: baseUrlInput.value });

</script>
