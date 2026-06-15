# LinkX IM — Agent Guide

## Project layout

```
linkx-server/   Java 21 + Spring Boot 3.2.5 backend (port 8080)
linkx-web/      Vue 3 + TypeScript 6 + Vite 8 + Electron 42
```

Monorepo with Maven parent POM at root. Only one server module (`linkx-server`). Frontend is a single-page app with Electron desktop packaging.

## Build commands

```bash
# Backend
mvn clean compile                          # from root or linkx-server/
mvn test                                   # runs AuthTest.java (needs MySQL+Redis)

# Frontend (run in linkx-web/)
npm run build                              # vue-tsc -b && vite build
npm run dev                                # Vite dev server (localhost:5173)
npm run electron:dev                       # Vite build then Electron launch
npm run electron:build                     # Windows NSIS installer
npm run electron:build:mac                 # macOS DMG
```

No lint, formatter, or CI pipeline is configured.

## Must-know gotchas

1. **Naive UI providers are NOT auto-registered.** `app.use(naive)` does not add `NMessageProvider` etc. Components calling `useMessage()` must be inside `<n-message-provider>` added manually in `App.vue`.

2. **File upload endpoints are POST-only.** Both `/api/file/upload/avatar` and `/api/file/upload/image` — GET causes `HttpRequestMethodNotSupportedException`.

3. **CORS blocks Electron production.** `SecurityConfig` allows only `http://localhost:5173` and `http://localhost:3000`. Electron production loads from `file://` (origin `null`), which is blocked. Dev mode works because it proxies through Vite at `localhost:5173`.

4. **Global `:focus-visible` adds a green box-shadow** on all focusable elements (`wechat.css` line 139-142). Must override with `box-shadow: none` on `.message-input:focus` and `:focus-visible` to prevent unwanted borders.

5. **Vite base is relative** (`base: './'` in `vite.config.ts`). Required for Electron's `file://` protocol loading in production.

6. **Template expression errors cascade.** A single `ReferenceError` in a v-for template expression (e.g., calling an undefined function) breaks the entire component render tree — ALL items fail to display. This already happened once with missing `formatSize()`.

7. **File message content stores URL, not fileId.** `sendFileMessage` queries `SysFileMapper` to get `fileUrl` before storing in `im_message.content`.

8. **Jackson LocalDateTime serialization is critical.** `JacksonConfig.java` registers `JavaTimeModule` with `yyyy-MM-dd HH:mm:ss` pattern. Without it, all DTOs with `LocalDateTime` fields cause 500 `HttpMessageConversionException` errors.

9. **Long > JavaScript MAX_SAFE_INTEGER serialized as strings.** `JacksonConfig.SafeLongSerializer` outputs numbers > 2^53 as strings to prevent frontend precision loss.

## Backend architecture

```
com.linkx.server/
  common/          Result, ErrorCode, BusinessException, GlobalExceptionHandler
  config/          SecurityConfig, JacksonConfig, MyBatisPlusConfig, RedisConfig, WebConfig
  entity/          SysUser, ImMessage, ImSession, SysFriend, SysFriendRequest, SysBlacklist, SysFile
  mapper/          MyBatis-Plus mappers for each entity
  security/        JwtAuthenticationFilter
  module/
    auth/          register/login/JWT (access 24h + refresh 7d)
    user/          profile, search
    friend/        request/accept/reject/delete
    blacklist/     add/remove/list/check
    chat/          sessions, messages, file messages, recall
    file/          upload avatar/image, list, delete
```

- Auth: JWT with `@AuthenticationPrincipal UserDetails`. Token stored in `Authorization: Bearer` header.
- MyBatis-Plus with Lombok `@Data` entities. IDs use `IdType.ASSIGN_ID` (snowflake).
- File storage: disk at `D:/yangleduo/Code/Java/LinkX/linkx-server/uploads/`, served at `http://localhost:8080/uploads/`.

## Frontend architecture

```
src/
  api/client.ts       Axios instance, all API endpoints
  views/              Login, Register, Layout, Chat, Friends, Files, Blacklist, Profile
  components/         TitleBar.vue (Electron frameless window)
  stores/user.ts      Single Pinia store for auth state
  utils/              theme.ts, electron.ts, fileDrop.ts, notify.ts
  styles/wechat.css   Global styles with CSS custom properties + theme support
```

- No dedicated chat or friend stores — `Chat.vue` manages state locally.
- API interceptor unwraps `res.data` — callers access `res.data` for payload.
- Router uses `createWebHashHistory` for Electron compatibility.
- Chat uses HTTP polling (3s interval), no WebSocket.

## Field mapping traps

- DB column `avatar_url` maps to Java field `avatar` via `@TableField("avatar_url")` in `SysUser`.
- DB column `password_hash` maps to Java field `password` via `@TableField("password_hash")`.
- `MessageDTO` fields: `fromUserId, fromNickname, fromAvatar, toUserId, content, msgType, status, createTime`.
- `ChatSessionDTO` fields: `id, userId, targetId, targetNickname, targetUsername, targetAvatar, lastMessage, lastMessageTime, unreadCount`.

## Database

MySQL `linkx_im` with 10 tables. Schema at `linkx-server/src/main/resources/schema.sql`. Key tables: `sys_user`, `im_session`, `im_message`, `sys_friend`, `sys_blacklist`, `sys_file`. Group tables (`im_group_info`, `im_group_member`) exist in schema but module not yet implemented.

## Theme system

`data-theme` attribute on `<html>`. CSS variables under `[data-theme="dark"]` and `[data-theme="light"]`. Managed by `theme.ts` composable. Default is dark. Persisted via `localStorage` key `linkx-theme`.

## Testing

Only `AuthTest.java` exists using `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc`. No frontend tests configured.
