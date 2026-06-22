# LinkX IM 功能缺口与 P0-P2 路线图

## 1. 文档目的

本文档用于沉淀当前 `LinkX IM` 项目的能力现状、功能缺口、P0/P1/P2 迭代路线，以及可直接执行的 P0 详细任务清单。

目标不是继续无序堆功能，而是先把项目从“功能能跑”提升到“可持续开发、可控上线、可回归验证”的状态。

## 2. 当前项目现状

### 2.1 已具备能力

- 账号注册、登录、JWT + RefreshToken
- 用户资料、好友、黑名单
- 单聊、群聊、消息撤回、已读
- 群公告、群成员管理、管理员、群主转让、禁言
- 文件上传、文件消息、文件访问票据
- WebSocket 实时聊天
- Vue 3 + Electron 桌面端

### 2.2 当前主要问题

- 安全、配置、日志、部署等底座能力仍不完整
- 前端聊天页职责过重，维护成本高
- 后端核心 Service 已较大，继续演进会越来越难维护
- 自动化测试覆盖不足，核心链路回归风险高
- 仍存在部分硬编码和环境耦合问题

## 3. 功能缺口清单

### 3.1 认证与安全

- 缺少 RBAC 权限体系
- 缺少验证码机制
- 缺少登录限流与异常锁定
- 缺少设备维度会话管理
- 缺少强制下线能力
- 缺少敏感操作审计日志
- 缺少统一的输入内容安全治理

### 3.2 聊天可靠性

- 缺少消息送达确认闭环
- 缺少客户端幂等去重闭环
- 缺少断线重连后的增量补偿
- 缺少多端同步策略
- 缺少会话游标和未读状态同步机制

### 3.3 群聊能力

- 缺少群邀请链接或二维码
- 缺少全员禁言
- 缺少群置顶消息
- 缺少群消息收藏或精华
- 缺少群文件空间能力
- 缺少可配置的群审批规则

### 3.4 搜索能力

- 缺少全局搜索入口
- 缺少按联系人、群、文件、日期的筛选
- 缺少搜索结果高亮
- 缺少消息上下文定位能力

### 3.5 文件能力

- 缺少大文件上传能力
- 缺少断点续传
- 缺少文件类型白名单
- 缺少容量限制与清理策略
- 缺少对象存储抽象
- 缺少下载失败重试和下载记录

### 3.6 用户设置

- 缺少完整设置中心
- 缺少通知偏好设置
- 缺少声音与免打扰策略配置
- 缺少下载目录配置
- 缺少快捷键配置
- 缺少隐私设置

### 3.7 运营与管理

- 缺少后台管理能力
- 缺少用户封禁与解封
- 缺少群治理工具
- 缺少系统公告管理
- 缺少在线统计与审计看板

### 3.8 运维与部署

- 缺少 Docker 化能力
- 缺少标准化部署文档
- 缺少环境变量模板
- 缺少日志采集和监控告警说明
- 缺少数据库迁移执行规范

## 4. 优先级路线图

### 4.1 P0：稳定上线底座

P0 的核心目标：

- 安全兜底
- 配置治理
- 前端解耦
- 测试补齐
- 日志与观测
- 部署基础

P0 不以新增大量业务功能为主，而是优先解决长期维护和上线风险。

### 4.2 P1：核心体验增强

- 消息可靠性增强
- 群能力增强
- 全局搜索
- 文件能力升级
- 桌面端设置中心

### 4.3 P2：产品与平台化能力

- 运营后台
- 多实例与架构演进
- AI 能力接入
- 更完整的消息体验能力

## 5. P0 详细任务清单

## 5.1 P0-1 配置治理

### 目标

消除硬编码地址，完成前后端环境区分，收口关键运行参数。

### 当前问题

- 前端接口地址直接写死
- 运行环境切换依赖改源码
- 关键配置分散，生产启动依赖经验

### 任务项

- 将前端 API 地址改为基于 `import.meta.env` 的环境配置
- 增加前端 `.env.development`、`.env.production`、`.env.example`
- 新增统一前端环境配置文件
- 梳理后端环境变量，包括数据库、Redis、JWT、上传路径、允许源
- 明确开发、测试、生产环境参数差异

### 需要修改的文件

- `linkx-web/src/api/client.ts`
- `linkx-web/vite.config.ts`
- `linkx-server/src/main/resources/application.yml`

### 建议新增文件

- `linkx-web/src/config/env.ts`
- `linkx-web/.env.development`
- `linkx-web/.env.production`
- `linkx-web/.env.example`

### 验收标准

- 前端在不改源码的情况下切换环境
- 后端通过环境变量即可完成主要配置
- 不再存在前端直写 `http://localhost:8080`

## 5.2 P0-2 安全兜底

### 目标

先补最基础的安全底线，避免项目进入“功能越多、风险越大”的状态。

### 当前问题

- 生产 CORS 需要收紧
- 缺少登录限流
- 缺少验证码预留
- 文件上传安全需要增强
- 敏感操作审计不足

### 任务项

- 收紧生产环境 CORS 为白名单模式
- 为登录、注册、刷新 Token 增加基础限流
- 为验证码接入预留接口和字段
- 增加统一文本输入清洗和长度治理
- 增加上传文件扩展名与 MIME 校验
- 为文件访问票据和敏感操作补日志

### 需要修改的文件

- `linkx-server/src/main/java/com/linkx/server/config/SecurityConfig.java`
- `linkx-server/src/main/java/com/linkx/server/module/auth/controller/AuthController.java`
- `linkx-server/src/main/java/com/linkx/server/module/auth/service/impl/AuthServiceImpl.java`
- `linkx-server/src/main/java/com/linkx/server/module/file/controller/FileController.java`
- `linkx-server/src/main/java/com/linkx/server/module/file/service/impl/FileServiceImpl.java`
- `linkx-server/src/main/java/com/linkx/server/module/chat/service/impl/ChatServiceImpl.java`
- `linkx-server/src/main/java/com/linkx/server/module/group/service/impl/GroupServiceImpl.java`

### 建议新增文件

- `linkx-server/src/main/java/com/linkx/server/config/RateLimitConfig.java`
- `linkx-server/src/main/java/com/linkx/server/common/TextNormalizer.java`
- `linkx-server/src/main/java/com/linkx/server/common/AuditLogService.java`

### 验收标准

- 生产环境不允许任意来源跨域
- 登录接口具备基础频控
- 危险文件无法上传
- 关键敏感行为具备审计日志

## 5.3 P0-3 前端解耦

### 目标

降低聊天主页面复杂度，建立可维护的前端结构。

### 当前问题

- `Chat.vue` 承担过多职责
- 会话、消息、群详情、上传、弹窗、实时事件处理全部堆在页面里
- 页面局部状态过多，不利于持续迭代

### 拆分建议

- `SessionSidebar.vue`：会话列表、会话搜索、未读展示
- `MessagePane.vue`：消息列表、滚动加载、消息定位
- `MessageComposer.vue`：输入区、发送、上传、@ 功能
- `GroupDetailPanel.vue`：群详情、成员、公告、管理动作
- `ChatDialogs.vue`：建群、邀请成员、禁言、转让群主、媒体预览等弹窗

### 状态建议

- 新增 `chat.ts`：会话列表、当前会话、消息缓存、未读状态、发送状态
- 新增 `group.ts`：群详情、成员列表、群管理动作
- 新增 `notification.ts`：消息提醒与桌面通知策略

### 任务项

- 将 `Chat.vue` 拆成组件组合页
- 抽离聊天状态到 Pinia
- 抽离群状态到 Pinia
- 在 WebSocket Hook 之上增加实时事件服务层
- 补齐类型定义，减少 `any`

### 需要修改的文件

- `linkx-web/src/views/Chat.vue`
- `linkx-web/src/hooks/useChatSocket.ts`
- `linkx-web/src/api/client.ts`

### 建议新增文件

- `linkx-web/src/components/chat/SessionSidebar.vue`
- `linkx-web/src/components/chat/MessagePane.vue`
- `linkx-web/src/components/chat/MessageComposer.vue`
- `linkx-web/src/components/chat/GroupDetailPanel.vue`
- `linkx-web/src/components/chat/ChatDialogs.vue`
- `linkx-web/src/stores/chat.ts`
- `linkx-web/src/stores/group.ts`
- `linkx-web/src/stores/notification.ts`
- `linkx-web/src/services/chatRealtimeService.ts`
- `linkx-web/src/types/chat.ts`
- `linkx-web/src/types/group.ts`

### 验收标准

- `Chat.vue` 只保留页面编排职责
- 状态管理不再散落于页面局部变量
- 新功能能在不修改核心页面主体结构的前提下迭代

## 5.4 P0-4 测试补齐

### 目标

为核心链路建立最小可依赖的自动化回归保障。

### 当前问题

- 认证、聊天、群聊、文件能力的测试覆盖不足
- 核心逻辑改动容易引发回归

### 建议优先测试模块

#### 认证

- 注册成功
- 重复用户名失败
- 重复邮箱失败
- 登录成功
- 密码错误失败
- 用户禁用失败
- RefreshToken 成功
- RefreshToken 无效或已失效失败

#### 聊天

- 好友单聊发送成功
- 非好友不能单聊
- 被拉黑不能发消息
- 已读正确回写
- 撤回超时失败

#### 群聊

- 群成员发群消息成功
- 非群成员不能发消息
- 被禁言用户不能发群消息
- 管理员和群主权限边界正确

#### 文件

- 文件上传成功
- 非法文件上传失败
- 文件票据访问成功
- 无效票据访问失败

### 需要修改或新增的测试文件

- `linkx-server/src/test/java/com/linkx/server/module/auth/AuthTest.java`
- `linkx-server/src/test/java/com/linkx/server/module/auth/AuthRefreshTest.java`
- `linkx-server/src/test/java/com/linkx/server/module/chat/ChatServiceTest.java`
- `linkx-server/src/test/java/com/linkx/server/module/group/GroupServiceTest.java`
- `linkx-server/src/test/java/com/linkx/server/module/file/FileControllerTest.java`

### 验收标准

- 至少覆盖认证、聊天、群聊、文件四条主链路
- 核心改动能通过自动化测试快速发现回归问题

## 5.5 P0-5 日志与观测

### 目标

让线上问题可定位、可追踪，而不是仅靠人工猜测。

### 任务项

- 为认证链路增加结构化日志
- 为聊天发送、撤回、已读、群管理动作增加关键日志
- 为 WebSocket 建连、断连、鉴权失败、频控触发增加日志
- 为文件上传和文件访问增加日志
- 统一日志关键字段，如 `userId`、`groupId`、`messageId`、`requestId`

### 建议重点修改文件

- `linkx-server/src/main/java/com/linkx/server/module/auth/service/impl/AuthServiceImpl.java`
- `linkx-server/src/main/java/com/linkx/server/module/chat/service/impl/ChatServiceImpl.java`
- `linkx-server/src/main/java/com/linkx/server/module/group/service/impl/GroupServiceImpl.java`
- `linkx-server/src/main/java/com/linkx/server/module/chat/ws/ChatWebSocketHandler.java`
- `linkx-server/src/main/java/com/linkx/server/module/file/controller/FileController.java`

### 验收标准

- 关键异常能快速定位到用户、群、消息、文件
- WebSocket 异常可追踪

## 5.6 P0-6 部署基础

### 目标

确保新环境能被可靠拉起，降低部署依赖人肉经验的程度。

### 任务项

- 增加后端 `Dockerfile`
- 增加 `docker-compose.yml`
- 增加项目级 `.env.example`
- 增加部署说明文档
- 增加数据库初始化与迁移执行说明
- 明确 MySQL、Redis、Server 的启动顺序

### 建议新增文件

- `linkx-server/Dockerfile`
- `docker-compose.yml`
- `.env.example`
- `docs/deploy/README.md`

### 验收标准

- 新成员可以按文档完成本地或测试环境启动
- 服务启动不依赖口头说明
- 数据库初始化步骤清晰

## 6. P1 任务建议

### 6.1 消息可靠性增强

- 增加 ACK 机制
- 增加断线补偿
- 增加多端同步
- 增加发送失败重试策略

### 6.2 群能力增强

- 群邀请链接或二维码
- 全员禁言
- 群置顶消息
- 群收藏或精华消息
- 群文件入口优化

### 6.3 搜索升级

- 增加全局搜索页
- 支持联系人、群、消息、文件统一搜索
- 增加筛选和高亮

### 6.4 文件能力升级

- 抽象文件存储接口
- 支持本地存储与 MinIO 可切换
- 增加下载重试和进度展示

### 6.5 设置中心

- 通知设置
- 声音设置
- 下载目录
- 快捷键
- 隐私设置

## 7. P2 任务建议

### 7.1 后台运营能力

- 用户管理
- 群治理
- 系统公告
- 审计与统计看板

### 7.2 架构演进

- 多实例下 WebSocket 会话共享
- Redis 或 MQ 协同事件推送
- 热点数据缓存

### 7.3 AI 能力

- 设计 `AIProvider` 抽象
- 会话摘要
- 群公告生成
- 搜索增强

### 7.4 产品增强

- 消息回复
- 消息转发
- 消息收藏
- 记录导出

## 8. 推荐执行顺序

### 第 1 周

- 完成前后端配置治理
- 完成生产安全兜底第一版

### 第 2 周

- 拆分 `Chat.vue` 第一阶段
- 抽 `chat store` 与 `group store`

### 第 3 周

- 补认证、聊天、群聊、文件测试
- 增加关键链路日志

### 第 4 周

- 完成 Docker 化和部署文档
- 完成数据库迁移说明

## 9. 当前最值得马上开始的 8 件事

- 改造前端 API 地址环境化
- 收紧生产 CORS
- 增加登录限流与验证码预留
- 拆分 `Chat.vue`
- 增加 `chat store` 和 `group store`
- 补齐核心业务自动化测试
- 增加关键链路日志
- 完成 Docker 和部署文档

## 10. 结论

当前项目最需要的不是继续堆功能，而是优先补齐工程底座。

先完成 P0，项目才能进入稳定可迭代状态；在此基础上推进 P1 和 P2，成本更低、风险更可控、长期收益更高。
