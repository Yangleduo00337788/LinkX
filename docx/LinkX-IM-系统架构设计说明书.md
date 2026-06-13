\# LinkX IM 系统架构设计说明书



\*\*版本：V1.0\*\*



\*\*项目名称：LinkX IM\*\*



\*\*编写日期：2026\*\*



\---



\# 1. 文档说明



\## 1.1 文档目的



本文档用于描述 LinkX IM 系统整体技术架构设计方案，为研发、测试、运维及后期扩展提供统一技术规范。



系统目标：



\* 微信级聊天体验

\* Telegram级消息架构

\* Discord级扩展能力

\* 飞书级协同能力

\* AI Native能力



\---



\# 2. 系统总体架构



\## 2.1 整体架构



```text

┌─────────────────────────┐

│      JavaFX客户端       │

└──────────┬──────────────┘

&#x20;          │

&#x20;          │ HTTPS

&#x20;          ▼

┌─────────────────────────┐

│       API Gateway       │

│      Spring Boot 3      │

└──────────┬──────────────┘

&#x20;          │

&#x20;┌─────────┼─────────┐

&#x20;▼         ▼         ▼



用户服务  好友服务  群组服务



&#x20;▼         ▼         ▼



┌─────────────────────────┐

│      Netty IM服务       │

│    长连接消息中心       │

└──────────┬──────────────┘

&#x20;          │

&#x20;          ▼

┌─────────────────────────┐

│        Kafka集群        │

└──────────┬──────────────┘

&#x20;          │

&#x20;          ▼

┌─────────────────────────┐

│      Message服务        │

└──────────┬──────────────┘

&#x20;          │

&#x20;          ▼

┌─────────────────────────┐

│        MySQL8           │

└─────────────────────────┘



&#x20;          │

&#x20;          ▼



┌─────────────────────────┐

│         Redis           │

└─────────────────────────┘



&#x20;          │

&#x20;          ▼



┌─────────────────────────┐

│         MinIO           │

└─────────────────────────┘

```



\---



\# 3. 技术架构选型



\## 3.1 客户端架构



\### 技术栈



\* JDK21

\* JavaFX

\* Maven

\* ControlsFX

\* SQLite

\* Netty Client

\* WebRTC



\### 职责



\* UI展示

\* 消息收发

\* 本地缓存

\* 文件上传下载

\* 音视频入口

\* AI助手入口



\---



\## 3.2 服务端架构



\### 技术栈



\* Spring Boot 3

\* Spring Security

\* Netty

\* Redis

\* Kafka

\* MySQL8

\* MinIO



\### 职责



\* 用户管理

\* 权限控制

\* 消息转发

\* 文件管理

\* AI能力接入



\---



\## 3.3 管理后台



\### 技术栈



\* Vue3

\* TypeScript

\* NaiveUI

\* Vite



\### 功能



\* 用户管理

\* 群组管理

\* 敏感词管理

\* 内容审核

\* 系统配置



\---



\# 4. 客户端架构设计



\## 4.1 分层架构



```text

UI层



↓

Controller层



↓

Service层



↓

Netty通信层



↓

本地缓存层(SQLite)

```



\---



\## 4.2 模块划分



```text

linkx-client



├─ login

├─ register

├─ contacts

├─ chat

├─ group

├─ file

├─ ai

├─ settings

├─ tray

├─ notification

└─ common

```



\---



\# 5. 服务端架构设计



\## 5.1 服务划分



```text

linkx-server



├─ gateway

├─ auth-service

├─ user-service

├─ friend-service

├─ group-service

├─ message-service

├─ file-service

├─ ai-service

└─ admin-service

```



\### V1阶段



采用单体模块化架构。



\### 后期



逐步拆分微服务。



\---



\# 6. Netty即时通讯架构



\## 6.1 长连接架构



```text

客户端



↓ TCP



Netty Server



↓

ChannelManager



↓

MessageDispatcher



↓

Kafka



↓

消息消费



↓

消息持久化

```



\---



\## 6.2 Channel管理



```text

userId

&#x20;   ↓

Channel



10001 -> ChannelA



10002 -> ChannelB



10003 -> ChannelC

```



采用：



```java

ConcurrentHashMap<Long, Channel>

```



维护在线连接。



\---



\## 6.3 消息流程



```text

客户端A



↓

Netty



↓

消息路由



↓

Kafka



↓

消息存储



↓

推送客户端B

```



\---



\# 7. Redis架构



\## 用户登录状态



```text

token:userId

```



\## 在线状态



```text

online:userId

```



\## 消息缓存



```text

msg:userId

```



\## 热点群缓存



```text

group:10001

```



\---



\# 8. Kafka架构



\## Topic规划



```text

linkx-message



linkx-group-message



linkx-notification



linkx-ai

```



\### 作用



\* 削峰填谷

\* 异步存储

\* 消息解耦

\* 提高吞吐量



\---



\# 9. MySQL架构



\## 核心表



```text

sys\_user



sys\_friend



sys\_friend\_request



sys\_blacklist



im\_session



im\_message



im\_message\_read



im\_group



im\_group\_member



im\_group\_notice



im\_file



im\_file\_chunk



ai\_conversation



ai\_message



sys\_login\_log



sys\_token\_blacklist

```



\### 数据库规范



\* utf8mb4

\* InnoDB

\* Snowflake ID

\* 逻辑删除



\---



\# 10. 文件存储架构



采用：



```text

MinIO

```



\### 存储目录



```text

/avatar



/image



/video



/file



/group

```



\### 支持能力



\* 分片上传

\* 秒传

\* 断点续传



\---



\# 11. WebRTC音视频架构



```text

客户端A



↕ STUN/TURN



客户端B

```



\### 功能



\* 语音通话

\* 视频通话

\* 屏幕共享

\* 多人会议



\---



\# 12. AI架构设计



```text

AI Gateway



↓

OpenAI



↓

DeepSeek



↓

Qwen



↓

Claude



↓

Gemini

```



统一接口：



```java

public interface AiProvider {



&#x20;   String chat(String prompt);



}

```



\### 支持能力



\* AI聊天

\* AI总结

\* AI翻译

\* AI会议纪要

\* AI写作



\---



\# 13. 安全架构



\### 认证



\* JWT



\### 传输



\* HTTPS



\### 消息



\* AES256加密



\### 密码



\* BCrypt



\### 风控



\* IP风控

\* 设备识别

\* 敏感词过滤



\---



\# 14. 部署架构



```text

Nginx



↓

SpringBoot集群



↓

Redis集群



↓

Kafka集群



↓

MySQL主从



↓

MinIO集群

```



\---



\# 15. 敏捷开发路线图



\## Sprint 1（V0.1）



\* 注册

\* 登录

\* JWT认证



\## Sprint 2（V0.2）



\* 用户资料

\* 搜索用户



\## Sprint 3（V0.3）



\* 好友系统



\## Sprint 4（V0.4）



\* 单聊系统



\## Sprint 5（V0.5）



\* 群聊系统



\## Sprint 6（V0.6）



\* 文件系统



\## Sprint 7（V0.7）



\* 音视频系统



\## Sprint 8（V0.8）



\* AI助手系统



\## Sprint 9（V0.9）



\* 管理后台



\## Sprint 10（V1.0）



\* Redis优化

\* Kafka优化

\* 集群部署

\* 压力测试



\---



\# 16. 架构目标



\## 性能指标



\* 在线用户 ≥ 100,000

\* 单机连接 ≥ 50,000

\* 消息延迟 ≤ 100ms

\* 可用性 ≥ 99.99%

\* 支持水平扩展

\* 支持企业级部署



\---



\# 最终目标



打造国内领先的新一代 AI Native 即时通讯平台。



实现：



\* 微信级用户体验

\* Telegram级消息架构

\* Discord级扩展能力

\* 飞书级协同办公

\* AI Native智能能力



形成完整的企业级即时通讯生态系统。



