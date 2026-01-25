# 项目结构规划：节点式 LLM 工作流平台

本设计方案基于 **Spring Boot (后端)** 和 **Vue 3 + Vue Flow (前端)**，旨在构建一个类似 ComfyUI 的节点式 LLM 工作流编排系统。

## 1. 顶层目录结构

采用 Monorepo 风格或分离式结构，将前后端代码分开管理。不使用 Docker 部署，直接在本地环境运行。

```text
sekhmet-llm-flow/
├── backend/                # Spring Boot 后端项目
├── frontend/               # Vue 3 前端项目
├── data/                   # 数据存储目录
│   └── workflows           # 工作流数据存储文件 (JSONL 格式)
├── docs/                   # 项目文档
└── README.md               # 项目说明
```

---

## 2. 后端结构 (Spring Boot + LangChain4j)

后端主要负责工作流的存储、解析、以及通过 LangChain4j 调用 LLM 模型。数据持久化层采用本地 JSONL 文件存储。

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/sekhmet/llmflow/
│   │   │   ├── config/                 # 配置类
│   │   │   │   ├── LangChainConfig.java    # LangChain4j 模型配置 (OpenAI, Ollama等)
│   │   │   │   ├── CorsConfig.java         # 跨域配置
│   │   │   │   └── AsyncConfig.java        # 异步执行配置
│   │   │   │
│   │   │   ├── controller/             # 控制器层
│   │   │   │   ├── WorkflowController.java # 工作流增删改查
│   │   │   │   ├── ExecutionController.java# 工作流运行/停止
│   │   │   │   └── NodeController.java     # 节点定义/模版获取
│   │   │   │
│   │   │   ├── model/                  # 数据模型
│   │   │   │   ├── entity/                 # 实体对象
│   │   │   │   │   ├── Workflow.java       # 工作流定义
│   │   │   │   │   └── ExecutionLog.java   # 运行日志
│   │   │   │   ├── dto/                    # 数据传输对象
│   │   │   │   │   ├── WorkflowRequest.java
│   │   │   │   │   └── NodeExecutionResult.java
│   │   │   │   └── graph/                  # 图结构定义 (用于解析前端 JSON)
│   │   │   │       ├── GraphDefinition.java
│   │   │   │       ├── NodeDefinition.java
│   │   │   │       └── EdgeDefinition.java
│   │   │   │
│   │   │   ├── service/                # 业务逻辑层
│   │   │   │   ├── WorkflowService.java    # 工作流管理 (调用 Repository 读写 JSONL)
│   │   │   │   ├── engine/                 # 核心执行引擎
│   │   │   │   │   ├── WorkflowEngine.java     # 图遍历与执行调度
│   │   │   │   │   ├── NodeExecutor.java       # 节点执行接口
│   │   │   │   │   └── nodes/                  # 具体节点实现
│   │   │   │   │       ├── LlmNodeExecutor.java    # 调用 LangChain4j
│   │   │   │   │       ├── PromptNodeExecutor.java # Prompt 模版处理
│   │   │   │   │       └── OutputNodeExecutor.java # 结果输出
│   │   │   │   └── LlmService.java         # LangChain4j 统一封装
│   │   │   │
│   │   │   └── repository/             # 数据访问层
│   │   │       └── JsonlWorkflowRepository.java # 实现 JSONL 文件的读写操作
│   │   │
│   │   └── resources/
│   │       ├── application.yml         # 配置文件 (API Keys, 文件路径配置)
│   └── test/                           # 单元测试
└── pom.xml                             # Maven 依赖 (spring-boot-starter-web, langchain4j, jackson, etc.)
```

### 关键技术栈
-   **Framework**: Spring Boot 3.x
-   **AI Integration**: LangChain4j (支持 OpenAI, HuggingFace, LocalAI 等)
-   **Data Storage**: 本地文件系统 (JSONL) - `workflows.jsonl`
    -   使用 Jackson 或 Gson 逐行读写 JSON 对象。
-   **JSON Processing**: Jackson - 解析前端传来的复杂图数据

---

## 3. 前端结构 (Vue 3 + Vue Flow)

前端负责提供可视化的节点编辑器，通过 Vue Flow 实现拖拽连线。

```text
frontend/
├── public/
├── src/
│   ├── assets/                 # 静态资源
│   ├── components/             # 通用组件
│   │   ├── common/                 # 按钮、输入框等基础组件
│   │   └── layout/                 # 布局组件 (Header, Sidebar)
│   │
│   ├── features/               # 核心功能模块
│   │   ├── editor/                 # 编辑器核心
│   │   │   ├── EditorCanvas.vue        # Vue Flow 画布
│   │   │   ├── Sidebar.vue             # 节点拖拽侧边栏
│   │   │   ├── Controls.vue            # 缩放/控制条
│   │   │   └── PropertyPanel.vue       # 选中节点的属性配置面板
│   │   │
│   │   └── nodes/                  # 自定义节点组件 (Custom Nodes)
│   │       ├── BaseNode.vue            # 节点基础样式封装
│   │       ├── LlmNode.vue             # LLM 模型节点 (选择模型、参数)
│   │       ├── PromptNode.vue          # 提示词节点 (输入文本)
│   │       └── ChatOutputNode.vue      # 聊天输出节点
│   │
│   ├── hooks/                  # 组合式函数 (Composables)
│   │   ├── useWorkflow.ts          # 工作流状态逻辑
│   │   └── useSocket.ts            # WebSocket 实时日志 (可选)
│   │
│   ├── stores/                 # 状态管理 (Pinia)
│   │   ├── workflowStore.ts        # 存储当前图的 nodes 和 edges
│   │   └── uiStore.ts              # UI 状态 (侧边栏开关等)
│   │
│   ├── services/               # API 服务
│   │   ├── api.ts                  # Axios 实例
│   │   └── workflowApi.ts          # 后端接口调用
│   │
│   ├── types/                  # TypeScript 类型定义
│   │   ├── node.ts                 # 节点类型定义
│   │   └── workflow.ts             # 工作流数据结构
│   │
│   ├── App.vue
│   └── main.ts
├── package.json                # 依赖 (vue, @vue-flow/core, axios, pinia)
├── vite.config.ts              # Vite 配置
└── tsconfig.json               # TypeScript 配置
```

### 关键技术栈
-   **Framework**: Vue 3 (Composition API)
-   **Graph Library**: Vue Flow (@vue-flow/core, @vue-flow/background, @vue-flow/controls)
-   **State Management**: Pinia
-   **UI Framework**: TailwindCSS
-   **HTTP Client**: Axios

---

## 4. 数据交互流程

1.  **搭建**: 用户在前端拖拽 `LlmNode`, `PromptNode` 等，连接 Edge。
2.  **保存**: 前端将 `nodes` 和 `edges` 序列化为 JSON，发送给后端 `WorkflowController`。
3.  **存储**: 后端 `JsonlWorkflowRepository` 将新的工作流对象追加写入 `data/workflows.jsonl` 文件的一行。
4.  **运行**:
    *   用户点击“运行”。
    *   后端 `WorkflowEngine` 接收图数据（或从 JSONL 读取）。
    *   引擎进行拓扑排序，确定执行顺序。
    *   `LlmNodeExecutor` 调用 `LangChain4j` 接口与大模型交互。
    *   执行结果通过 API 返回前端，或通过 WebSocket 实时推送到前端节点上显示。
