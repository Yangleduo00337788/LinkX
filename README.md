# LinkX IM

即时通讯全栈项目：`linkx-server`（Spring Boot）、`linkx-web`（Vue + Electron 桌面）、`linkx-admin`（管理后台）。

## 环境要求

- JDK **21**
- Node.js **18+**
- MySQL、Redis、MinIO（见 `linkx-server/src/main/resources/application.yml`）

## 后端

```bash
cd linkx-server
mvn spring-boot:run
```

默认 `http://localhost:8080`。常用环境变量：

| 变量 | 说明 |
|------|------|
| `LINKX_SERVER_PORT` | HTTP 端口 |
| `LINKX_PROFILE` | `dev` / `prod` |
| `LINKX_JWT_SECRET` | JWT 密钥（≥32 字符） |
| `LINKX_DATASOURCE_*` | MySQL |
| `LINKX_REDIS_*` | Redis |
| `LINKX_MINIO_*` | 对象存储 |

## 用户端 linkx-web

```bash
cd linkx-web
npm install
npm run dev
```

### 环境变量（Vite）

| 变量 | 说明 |
|------|------|
| `VITE_API_BASE` | API 根地址，开发可留空（走 Vite 代理） |
| `VITE_WS_BASE` | WebSocket 根地址（见 `src/config/env.ts`） |

**Electron 打包**：构建前设置与线上一致的 `VITE_API_BASE` / `VITE_WS_BASE`，再执行 `npm run electron:build`。桌面端通过 `releaseApi.latest(platform)` 与后台「发布管理」对齐；`electron/main.js` 负责检查更新与安装包下载。

### 聊天发送策略

- 主路径：WebSocket 命令 `SEND_MESSAGE` / `SEND_FILE_MESSAGE`
- 断线/超时：自动降级 `chatApi.send` / `send-file`（见 `src/utils/chatSendFallback.ts`）
- 会话置顶/免打扰/备注：统一 `PUT /api/chat/session/settings`（`sessionApi.updatePreferences` 已转发）

## 管理端 linkx-admin

```bash
cd linkx-admin
npm install
npm run dev
```

`VITE_API_BASE` 指向同一后端。

## Electron 发布前检查（摘要）

- [ ] 生产 `VITE_API_BASE` / `VITE_WS_BASE` 已配置
- [ ] `contextIsolation` / 禁用页面 `nodeIntegration`（见 `electron/main.js`）
- [ ] 后台已发布对应 `platform` 的安装包与 `downloadUrl`
- [ ] CSP / `webSecurity` 按发行渠道复核

## 测试

当前以手工联调为主；建议后续补充：登录 + refresh、发消息（WS + REST 降级）关键路径单测/E2E。

## 文档

- 群管理 API ↔ UI 对照：`docs/GROUP_API_UI_CHECKLIST.md`