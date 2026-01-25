# Sekhmet - 节点式 LLM 工作流平台

Sekhmet 是一个基于 Spring Boot 和 Vue Flow 构建的节点式工作流编排系统。它允许用户通过拖拽节点的方式，可视化的构建、管理和执行 LLM (大语言模型) 工作流。

![Vue Flow](https://vueflow.dev/logo.svg)

## 🏗️ 核心架构

本项目采用前后端分离架构：

*   **后端**: Spring Boot 3.2 + LangChain4j
    *   提供工作流的存储、解析与执行引擎。
    *   支持通过 LangChain4j 集成 OpenAI 等大模型。
    *   使用本地 JSONL 文件进行轻量级数据持久化。
*   **前端**: Vue 3 + Vue Flow + TailwindCSS
    *   提供基于节点的所见即所得编辑器。
    *   支持节点的拖拽、连线、属性配置。

## 🚀 快速开始

### 前置要求
*   Java 17+
*   Maven 3.6+
*   Node.js 18+

### 1. 启动后端服务

进入 `backend` 目录并运行 Spring Boot 应用：

```bash
cd backend
# 编译并运行
mvn spring-boot:run
```

服务默认启动在端口 `8080`。

> **注意**: 默认配置下，LLM 服务处于 Mock 模式。如需调用真实 OpenAI 接口，请设置环境变量 `OPENAI_API_KEY` 或修改配置文件。

### 2. 启动前端开发服务器

进入 `frontend` 目录安装依赖并启动：

```bash
cd frontend
# 安装依赖
npm install
# 启动开发服务器
npm run dev
```

访问浏览器 `http://localhost:5173` 即可开始使用。

## ⚙️ 配置说明

后端配置文件位于 `backend/src/main/resources/application.yml`。

```yaml
sekhmet:
  data-dir: ../data           # 工作流数据存储路径
  openai:
    api-key: ${OPENAI_API_KEY:demo} # OpenAI API Key (默认 demo 模式)
```

## 📝 数据存储

所有的工作流数据默认存储在项目根目录下的 `data/workflows.jsonl` 文件中，每行为一个完整的 JSON 工作流对象。

## 🛠️ 主要功能

- [x] **工作流编排**: 拖拽 Prompt 节点、LLM 节点、输出节点并进行连线。
- [x] **数据持久化**: 支持工作流的保存与读取。
- [x] **执行引擎**: 后端解析图结构，按拓扑顺序执行节点。
- [x] **LLM 集成**: 节点可实际调用大模型生成内容。
