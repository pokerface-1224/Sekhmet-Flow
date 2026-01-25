# 开发环境搭建指南 (Development Setup)

本指南将帮助你配置本地开发环境并启动 Sekhmet 项目。

## 1. 前置要求 (Prerequisites)

请确保你的系统已安装以下软件：

*   **Java Development Kit (JDK)**: 版本 17 或更高。
    *   验证: `java -version`
*   **Maven**: 3.6 或更高 (通常 IDE 会内置)。
    *   验证: `mvn -v`
*   **Node.js**: 18.0 或更高 (LTS 版本推荐)。
    *   验证: `node -v`
    *   含 `npm`: `npm -v`

## 2. 后端启动 (Backend)

后端是一个标准的 Spring Boot 项目。

1.  进入后端目录：
    ```bash
    cd backend
    ```

2.  (可选) 配置 OpenAI API Key：
    打开 `src/main/resources/application.yml`，修改 `sekhmet.openai.api-key`，或者设置环境变量：
    ```bash
    # Windows PowerShell
    $env:OPENAI_API_KEY="sk-..."
    ```

3.  启动服务：
    ```bash
    # 使用 Maven 插件运行
    mvn spring-boot:run
    ```
    或者在 IntelliJ IDEA / Eclipse 中直接运行 `LlmflowApplication.java` 的 `main` 方法。

4.  验证：
    浏览器访问 `http://localhost:8080/api/workflows`，应返回 `[]` (如果数据为空)。

## 3. 前端启动 (Frontend)

前端基于 Vue 3 和 Vite。

1.  进入前端目录：
    ```bash
    cd frontend
    ```

2.  安装依赖：
    ```bash
    npm install
    ```

3.  启动开发服务器：
    ```bash
    npm run dev
    ```

4.  访问：
    打开浏览器访问终端显示的地址 (通常是 `http://localhost:5173`)。

## 4. 常见问题 (FAQ)

### 端口冲突
*   后端默认使用 **8080**。如需修改，编辑 `application.yml`: `server.port: 9090`。
*   前端 Vite 默认使用 **5173**。

### 跨域问题 (CORS)
后端已配置 `CorsConfig` 允许来自前端开发端口的请求。如果修改了端口，请同步检查后端 CORS 配置。
