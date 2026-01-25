# 系统架构设计 (System Architecture)

## 1. 概览 (Overview)

Sekhmet 是一个节点式 LLM 工作流编排平台。核心设计目标是提供一个轻量级、本地化、可视化的 LLM 应用构建环境。

系统采用 **前后端分离 (Decoupled)** 架构，数据存储采用 **本地文件系统 (Local Filesystem)** 以保持轻便。

## 2. 架构图 (Architecture Diagram)

```mermaid
graph TD
    User[用户 (Browser)] -->|HTTP/WebSocket| Frontend[前端 (Vue 3 + Vue Flow)]
    
    subgraph Frontend Scope
        Store[Pinia State]
        Canvas[Flow Canvas]
    end
    
    Frontend -->|REST API| Backend[后端 (Spring Boot)]
    
    subgraph Backend Scope
        Controller[Web Layer]
        Service[Service Layer]
        Engine[Execution Engine]
        LlmClient[LangChain4j]
    end
    
    Backend -->|Read/Write| DB[(JSONL Data)]
    Backend -->|API Call| ExternalLLM[外部大模型 (OpenAI/LocalAI)]
    
    Controller --> Service
    Service --> Engine
    Service --> DB
    Engine --> LlmClient
```

## 3. 核心模块 (Core Modules)

### 3.1 前端 (Frontend)
*   **技术栈**: Vue 3, TypeScript, TailwindCSS, Vue Flow.
*   **职责**:
    *   **可视化编辑**: 提供拖拽式的节点编辑器。
    *   **状态管理**: 使用 Pinia 管理当前工作流图产生的 Nodes 和 Edges 状态。
    *   **实时交互**: 展示运行结果和调试信息。

### 3.2 后端 (Backend)
*   **技术栈**: Java 17, Spring Boot 3.2, LangChain4j.
*   **职责**:
    *   **API 服务**: 提供工作流的 CRUD 接口。
    *   **执行引擎**: 解析有向无环图 (DAG)，按拓扑顺序调度节点执行。
    *   **LLM 集成**: 通过 LangChain4j 统一封装不同模型厂商的接口 (OpenAI, HuggingFace, etc.)。

### 3.3 数据存储 (Storage)
*   **格式**: JSONL (JSON Lines)。
*   **位置**: `data/workflows.jsonl`
*   **设计理由**: 
    *   **零外部依赖**: 无需安装 MySQL/PostgreSQL，开箱即用。
    *   **Git 友好**: 文本格式方便版本控制。
    *   **原子性**: 单行写入，降低文件损坏风险。

## 4. 关键流程 (Key Processes)

### 工作流执行流程
1.  **提交**: 前端将当前图结构 (Nodes/Edges) 序列化为 JSON 提交给后端 `execution` 接口。
2.  **解析**: 后端解析 JSON，构建内部图模型。
3.  **排序**: 使用拓扑排序算法 (Topological Sort) 确定节点执行顺序。
4.  **调度**: 依次执行节点。
    *   如果是 **PromptNode**: 进行字符串模版替换。
    *   如果是 **LLMNode**: 调用 LangChain4j 向 LLM 发送请求。
5.  **返回**: 最终结果返回前端。
