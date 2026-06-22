# LinkX IM 部署说明

## 1. 目标

本文档用于提供 `LinkX IM` 的标准化本地/测试环境启动说明，覆盖以下内容：

- Docker Compose 启动顺序
- MySQL 初始化方式
- Redis 与后端服务依赖关系
- 环境变量准备方式
- 常见启动与验证步骤

当前提供的是后端服务、MySQL、Redis 的容器化启动方案。前端 `linkx-web` 仍建议按现有开发方式单独运行或单独打包。

## 2. 文件说明

- `docker-compose.yml`：项目级容器编排文件
- `.env.example`：项目级环境变量模板
- `linkx-server/Dockerfile`：后端镜像构建文件
- `linkx-server/src/main/resources/schema.sql`：数据库初始化脚本

## 3. 启动顺序

推荐且已经在 `docker-compose.yml` 中固化的启动顺序如下：

1. 启动 `mysql`
2. 启动 `redis`
3. 等待 `mysql` 与 `redis` 健康检查通过
4. 启动 `server`

`server` 容器通过 `depends_on + service_healthy` 等待依赖服务就绪，避免后端在数据库和 Redis 尚未可用时提前启动。

## 4. 环境变量准备

### 4.1 复制模板

在项目根目录执行：

```bash
cp .env.example .env
```

Windows PowerShell 可执行：

```powershell
Copy-Item .env.example .env
```

### 4.2 必改项

至少需要修改以下变量：

- `LINKX_DB_PASSWORD`
- `LINKX_DB_ROOT_PASSWORD`
- `LINKX_REDIS_PASSWORD`
- `LINKX_JWT_SECRET`
- `LINKX_SECURITY_ALLOWED_ORIGIN_PATTERNS`

### 4.3 CORS 配置建议

生产环境必须配置 `LINKX_SECURITY_ALLOWED_ORIGIN_PATTERNS`，多个值使用英文逗号分隔，例如：

```env
LINKX_SECURITY_ALLOWED_ORIGIN_PATTERNS=https://im.example.com,https://admin.example.com
```

如果你仍需要本地前端联调，可以临时加入：

```env
LINKX_SECURITY_ALLOWED_ORIGIN_PATTERNS=http://localhost:5173
```

## 5. 数据库初始化

### 5.1 初始化方式

`docker-compose.yml` 已将以下脚本挂载到 MySQL 初始化目录：

- `linkx-server/src/main/resources/schema.sql`

首次创建数据库数据卷时，MySQL 会自动执行该脚本初始化表结构。

### 5.2 注意事项

- 只有在 **首次创建 MySQL 数据卷** 时才会自动执行初始化脚本
- 如果你已经启动过并持久化了 `mysql-data` 卷，修改 `schema.sql` 后不会自动再次执行
- 需要重置数据库时，可删除相关数据卷后重新启动

## 6. 启动方式

### 6.1 构建并启动

在项目根目录执行：

```bash
docker compose up -d --build
```

### 6.2 查看状态

```bash
docker compose ps
```

### 6.3 查看后端日志

```bash
docker compose logs -f server
```

### 6.4 停止服务

```bash
docker compose down
```

### 6.5 停止并清理数据卷

```bash
docker compose down -v
```

仅在你明确需要重建数据库和 Redis 数据时再执行 `-v`。

## 7. 服务映射

默认端口如下：

- MySQL：`3306`
- Redis：`6379`
- LinkX Server：`8080`

访问地址：

- 后端接口：`http://localhost:8080`
- 上传资源：`http://localhost:8080/uploads/`

## 8. 上传目录说明

后端容器内上传目录固定为：

```text
/data/linkx/uploads
```

该目录通过 `server-uploads` Docker Volume 持久化，容器重建后文件不会因容器销毁而丢失。

## 9. 前端联调建议

### 9.1 Web 前端开发联调

后端容器启动后，在 `linkx-web` 中使用：

```env
VITE_API_BASE_URL=http://localhost:8080
```

然后执行：

```bash
npm run dev
```

### 9.2 Electron 生产构建注意

如果你使用 Electron 生产构建，后端必须允许 `file://` 场景对应的受控策略；当前项目主要通过浏览器开发地址联调，正式上线前建议再结合发布方式复核 CORS 与资源访问策略。

## 10. 常见问题

### 10.1 后端启动失败，提示数据库连接失败

排查顺序：

1. 确认 `mysql` 容器已健康
2. 确认 `.env` 中数据库用户名和密码一致
3. 确认本机端口未被其他 MySQL 占用

### 10.2 修改了 `schema.sql` 但没有生效

这是 MySQL 初始化目录的正常行为。若需重新初始化：

```bash
docker compose down -v
docker compose up -d --build
```

### 10.3 JWT Secret 启动后仍不安全

请确保 `LINKX_JWT_SECRET` 已替换为长度足够、不可预测的生产密钥，不要保留默认占位值。

## 11. 建议的部署基线

测试环境至少满足以下要求：

- 使用 `.env` 管理实际敏感配置
- 不将真实口令提交到仓库
- 通过 `docker compose up -d --build` 完成统一启动
- 通过 `docker compose logs -f server` 排查启动问题
- 通过数据卷持久化数据库、Redis 与上传文件
