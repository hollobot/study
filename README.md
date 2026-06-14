# study-demo

个人学习用的 Spring Boot 多模块示例集合，每个子模块对应一个独立的技术点，可单独启动运行。

接口文档（启动对应模块后访问）：

```text
Swagger UI : http://127.0.0.1:8080/swagger-ui/index.html
Knife4j    : http://127.0.0.1:8080/doc.html
```

## 环境要求

- JDK 17
- Maven 3.6+
- Spring Boot 3.x
- 部分模块依赖外部中间件（见下表），地址在各模块的 `application.yaml` 中配置

## 模块一览

| 模块 | 技术点 | 默认端口 | 依赖的中间件 |
| --- | --- | --- | --- |
| `Spring` | Spring 事务失效场景演示 | — | 无 |
| `RabbitMQ` | RabbitMQ 各类消息模型 | 8080 | RabbitMQ |
| `Elasticsearch` | ES 商品全文检索 | 8081 | Elasticsearch |
| `minio` | MinIO 对象存储文件管理 | 8080 | MinIO |
| `sa-token` | Sa-Token 登录鉴权 | 8080 | Redis |
| `LangChain4j` | LangChain4j 大模型对话 + RAG 知识库 | 8080 | Elasticsearch、（可选）Ollama |

> 多个模块默认端口均为 8080，需要同时运行时请修改各自的 `server.port`。

## 模块说明

### Spring

演示 Spring 事务在自调用（`this.test2()`）场景下的失效问题：同类方法内部调用不会经过代理对象，导致 `@Transactional` 失效。纯代码演示，无 Web 接口。

### RabbitMQ

覆盖 RabbitMQ 常见消息模型，统一通过 `/rabbitmq/**` 接口触发生产者发送消息：

- 简单队列（Simple）、工作队列（Work）
- 发布订阅（Fanout）、路由（Direct）、主题（Topic）
- 死信队列（DLX）、仲裁队列（Quorum）

消费端开启手动 ACK、消费重试，配置见 `application.yaml`。

### Elasticsearch

基于 Spring Data Elasticsearch 的商品搜索 Demo，接口前缀 `/api/products`：

- 单条/批量新增、按 ID 查询、查询全部、删除
- 关键词全文检索（多字段）、按名称模糊搜索、按分类查询、按价格区间查询

### minio

基于 MinIO 的文件管理，接口前缀 `/api/file`：

- 文件上传（返回 objectName 与 7 天有效期的预签名 URL）
- 文件下载、删除
- 生成指定有效期的预签名访问 URL

### sa-token

基于 Sa-Token 的登录鉴权 Demo，接口前缀 `/api/user/`，会话数据存储于 Redis：

- 登录 / 查询登录状态 / 查询登录信息
- 踢人下线、注销下线、账号封禁
- 多端会话查询

### LangChain4j

基于 LangChain4j 的大模型应用，包含对话与 RAG 两部分：

- `/chat/**`：OpenAI、智谱 GLM（含流式）、Ollama 本地模型对话
- `/rag/**`：上传 Markdown 导入向量知识库、知识库流式问答、分页查询向量数据

向量存储使用 Elasticsearch，模型 API Key 等配置见 `application.yaml`。

## 运行方式

每个模块均为独立的 Spring Boot 应用，进入对应目录单独启动即可：

```bash
cd <模块目录>
mvn spring-boot:run
```

或在 IDE 中直接运行各模块的 `*Application` 启动类。运行前请确认对应中间件已就绪，并按需修改 `application.yaml` 中的连接地址。
