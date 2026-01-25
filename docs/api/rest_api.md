# REST API 接口文档

## 基础信息
*   Base URL: `/api`
*   ContentType: `application/json`

## 工作流 (Workflows)

### 1. 获取所有工作流
获取系统中存储的所有工作流列表。

- **URL**: `/workflows`
- **Method**: `GET`
- **Response**: `200 OK`

```json
[
  {
    "id": "uuid-1",
    "name": "Translation Workflow",
    "nodes": [...],
    "edges": [...]
  },
  ...
]
```

### 2. 获取特定工作流
根据 ID 获取单个工作流的详细信息。

- **URL**: `/workflows/{id}`
- **Method**: `GET`
- **Path Parameters**:
    - `id`: 工作流的唯一标识符
- **Response**: `200 OK`

```json
{
  "id": "uuid-1",
  ...
}
```

### 3. 保存/更新工作流
创建新的工作流或更新现有工作流。

- **URL**: `/workflows`
- **Method**: `POST`
- **Body**:

```json
{
  "id": "可选，不传则新建",
  "name": "My Workflow",
  "nodes": [],
  "edges": []
}
```

- **Response**: `200 OK` (返回保存后的完整对象)

## 执行 (Execution) (待实现)
*预计接口*

- **URL**: `/execution/run`
- **Method**: `POST`
- **Body**: `{ "workflowId": "..." }` 或直接传输图数据。
