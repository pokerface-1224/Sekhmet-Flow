# Sekhmet - 节点式 LLM 工作流平台

Sekhmet 是一个基于 Spring Boot 和 Vue Flow 构建的节点式工作流编排系统。它允许用户通过拖拽节点的方式，可视化地构建、管理和执行 LLM (大语言模型) 工作流。

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-green.svg)

## ✨ 功能特性

### 🔗 多 LLM 提供商支持
- **OpenAI** 
- **Google Gemini** 
- **DeepSeek** 
- **兼容 OpenAI 格式的 API**

### 🎨 可视化节点编排
- 拖拽式节点添加
- 自由连线定义数据流
- 实时属性配置面板

### 📦 节点类型
| 节点 | 说明 |
|------|------|
| **用户提示词** | 定义用户输入的提示词模板 |
| **系统提示词** | 设定 AI 角色和行为规则 |
| **LLM 模型** | 调用大语言模型生成内容 |
| **聊天输出** | 展示最终对话结果 |

### 🔄 模型管理
- 运行时**热切换**模型配置
- **动态发现**提供商可用模型列表
- 模型池管理，按需创建实例

### 📊 执行日志
- 结构化日志输出
- Token 消耗统计
- 支持独立窗口查看 (多显示器友好)

---

## 🏗️ 核心架构

```
┌──────────────────────────────────────────────────────────────┐
│                         Frontend                             │
│  Vue 3 + Vue Flow + Pinia + TailwindCSS                      │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐             │
│  │ Prompt  │→│  LLM    │→│ Output  │ │  Logs   │             │
│  │  Node   │ │  Node   │ │  Node   │ │  Panel  │             │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘             │
└──────────────────────────┬───────────────────────────────────┘
                           │ REST API
┌──────────────────────────▼───────────────────────────────────┐
│                         Backend                              │
│  Spring Boot 3.2 + LangChain4j                               │
│  ┌─────────────────┐ ┌───────────────┐ ┌─────────────────┐   │
│  │ WorkflowEngine  │ │ ModelService  │ │ JSONL Storage   │   │
│  │  (拓扑排序执行)  │ │ (多Provider)  │ │ (持久化)         │  │
│  └─────────────────┘ └───────────────┘ └─────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 前置要求
- Java 17+
- Maven 3.6+
- Node.js 18+

### 1. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

服务默认启动在端口 `8080`。

### 2. 启动前端开发服务器

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173` 即可开始使用。

---

## ⚙️ 配置说明

后端配置文件位于 `backend/src/main/resources/application.yml`：

```yaml
sekhmet:
  data-dir: ../data           # 工作流数据存储路径
  openai:
    api-key: ${OPENAI_API_KEY:demo}  # 默认 API Key
```

> **提示**: 你可以在 LLM 节点中直接配置 API Key，无需在配置文件中设置。

---

## 📝 数据存储

所有工作流数据存储在 `data/workflows.jsonl` 文件中，每行一个完整的 JSON 对象。

---

## 🛠️ 主要功能

- [x] **工作流编排** - 拖拽式节点操作，自由连线
- [x] **多 Provider 支持** - OpenAI, Gemini, DeepSeek
- [x] **动态模型发现** - 自动获取可用模型列表
- [x] **模型热切换** - 运行时更换模型配置
- [x] **系统提示词** - 支持设定 AI 角色
- [x] **执行引擎** - 拓扑排序，按依赖执行
- [x] **结构化日志** - Token 消耗、耗时统计
- [x] **独立日志窗口** - 支持多显示器

---

## 📂 项目结构

详见 [project_structure.md](./project_structure.md)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

MIT License
