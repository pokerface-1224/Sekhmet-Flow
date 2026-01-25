# 部署指南 (Deployment)

本文档介绍如何将系统打包并部署到生产环境 (或类生产环境)。

## 1. 后端打包

使用 Maven 将 Spring Boot 应用打包为可执行 JAR 文件。

```bash
cd backend
mvn clean package -DskipTests
```

构建成功后，在 `backend/target/` 目录下会生成 `llmflow-0.0.1-SNAPSHOT.jar`。

**运行 JAR:**
```bash
java -jar target/llmflow-0.0.1-SNAPSHOT.jar --sekhmet.openai.api-key=sk-your-key
```

## 2. 前端打包

使用 Vite 构建生产环境静态资源。

```bash
cd frontend
npm run build
```

构建产物位于 `frontend/dist/` 目录。

## 3. 部署策略

### 方案 A: 前后端一体 (简单)
Spring Boot 可以直接托管前端静态资源。
1.  将 `frontend/dist/` 下的所有文件复制到 `backend/src/main/resources/static/` 目录。
2.  重新打包后端 JAR。
3.  运行 JAR 即可同时提供 API 和 页面服务。

### 方案 B: Nginx 反向代理 (推荐)
使用 Nginx 托管静态资源，并反向代理 API 请求。

**Nginx 配置示例:**

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /path/to/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html; # 支持 Vue Router History 模式
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```
