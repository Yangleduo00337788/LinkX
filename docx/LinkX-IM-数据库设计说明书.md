\# LinkX IM 数据库设计说明书



Version：V1.0



Project：LinkX IM



Database：MySQL 8.0+



Character Set：utf8mb4



Engine：InnoDB



Primary Key Strategy：Snowflake ID



\---



\# 1. 数据库设计原则



\## 1.1 设计目标



满足：



\* 高并发

\* 高可用

\* 易扩展

\* 易维护



支持：



\* 单聊

\* 群聊

\* 文件传输

\* AI助手

\* 音视频

\* 企业协同



\---



\# 2. 数据库规划



数据库名称：



```sql

linkx\_im

```



模块划分：



```text

sys\_\*

用户模块



im\_\*

即时通讯模块



file\_\*

文件模块



ai\_\*

AI模块



admin\_\*

后台管理模块

```



\---



\# 3. 用户模块



\## 3.1 用户表



表名：



```sql

sys\_user

```



```sql

CREATE TABLE sys\_user

(

&#x20;   id BIGINT PRIMARY KEY COMMENT '用户ID',



&#x20;   username VARCHAR(50) NOT NULL COMMENT '用户名',



&#x20;   nickname VARCHAR(50) NOT NULL COMMENT '昵称',



&#x20;   password VARCHAR(255) NOT NULL COMMENT '密码',



&#x20;   phone VARCHAR(20),



&#x20;   email VARCHAR(100),



&#x20;   avatar VARCHAR(500),



&#x20;   signature VARCHAR(255),



&#x20;   gender TINYINT DEFAULT 0,



&#x20;   region VARCHAR(100),



&#x20;   status TINYINT DEFAULT 1,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP,



&#x20;   update\_time DATETIME DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP,



&#x20;   deleted TINYINT DEFAULT 0

);

```



索引：



```sql

CREATE UNIQUE INDEX uk\_username ON sys\_user(username);



CREATE UNIQUE INDEX uk\_phone ON sys\_user(phone);



CREATE UNIQUE INDEX uk\_email ON sys\_user(email);

```



\---



\## 3.2 登录日志表



```sql

CREATE TABLE sys\_login\_log

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   user\_id BIGINT,



&#x20;   login\_ip VARCHAR(100),



&#x20;   device VARCHAR(255),



&#x20;   os VARCHAR(100),



&#x20;   browser VARCHAR(100),



&#x20;   login\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\## 3.3 Token黑名单



```sql

CREATE TABLE sys\_token\_blacklist

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   token VARCHAR(1000),



&#x20;   expire\_time DATETIME,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\# 4. 好友系统



\## 4.1 好友关系表



```sql

CREATE TABLE sys\_friend

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   user\_id BIGINT NOT NULL,



&#x20;   friend\_id BIGINT NOT NULL,



&#x20;   remark VARCHAR(100),



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\## 4.2 好友申请表



```sql

CREATE TABLE sys\_friend\_request

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   from\_user\_id BIGINT,



&#x20;   to\_user\_id BIGINT,



&#x20;   message VARCHAR(255),



&#x20;   status TINYINT DEFAULT 0,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



状态：



```text

0 待处理

1 同意

2 拒绝

```



\---



\## 4.3 黑名单表



```sql

CREATE TABLE sys\_blacklist

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   user\_id BIGINT,



&#x20;   blacklist\_user\_id BIGINT,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\# 5. 会话系统



\## 5.1 会话表



```sql

CREATE TABLE im\_session

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   session\_type TINYINT,



&#x20;   owner\_id BIGINT,



&#x20;   target\_id BIGINT,



&#x20;   last\_message VARCHAR(500),



&#x20;   last\_message\_time DATETIME,



&#x20;   unread\_count INT DEFAULT 0,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



类型：



```text

1 单聊

2 群聊

```



\---



\# 6. 消息系统



\## 6.1 消息表



```sql

CREATE TABLE im\_message

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   session\_id BIGINT,



&#x20;   sender\_id BIGINT,



&#x20;   receiver\_id BIGINT,



&#x20;   message\_type TINYINT,



&#x20;   content TEXT,



&#x20;   file\_url VARCHAR(1000),



&#x20;   status TINYINT DEFAULT 0,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



消息类型：



```text

1 文本

2 图片

3 文件

4 视频

5 语音

6 表情

7 系统消息

```



\---



\## 6.2 已读表



```sql

CREATE TABLE im\_message\_read

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   message\_id BIGINT,



&#x20;   user\_id BIGINT,



&#x20;   read\_time DATETIME

);

```



\---



\# 7. 群聊系统



\## 7.1 群组表



```sql

CREATE TABLE im\_group

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   group\_name VARCHAR(100),



&#x20;   group\_avatar VARCHAR(500),



&#x20;   owner\_id BIGINT,



&#x20;   notice TEXT,



&#x20;   status TINYINT DEFAULT 1,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\## 7.2 群成员表



```sql

CREATE TABLE im\_group\_member

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   group\_id BIGINT,



&#x20;   user\_id BIGINT,



&#x20;   role\_type TINYINT DEFAULT 0,



&#x20;   join\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



角色：



```text

0 普通成员

1 管理员

2 群主

```



\---



\## 7.3 群公告表



```sql

CREATE TABLE im\_group\_notice

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   group\_id BIGINT,



&#x20;   content TEXT,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\# 8. 文件系统



\## 8.1 文件表



```sql

CREATE TABLE im\_file

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   user\_id BIGINT,



&#x20;   file\_name VARCHAR(255),



&#x20;   file\_size BIGINT,



&#x20;   file\_type VARCHAR(50),



&#x20;   file\_url VARCHAR(1000),



&#x20;   md5 VARCHAR(64),



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\## 8.2 分片表



```sql

CREATE TABLE im\_file\_chunk

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   file\_id BIGINT,



&#x20;   chunk\_index INT,



&#x20;   chunk\_size BIGINT,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\# 9. AI模块



\## 9.1 AI会话表



```sql

CREATE TABLE ai\_conversation

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   user\_id BIGINT,



&#x20;   title VARCHAR(255),



&#x20;   model\_name VARCHAR(100),



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\## 9.2 AI消息表



```sql

CREATE TABLE ai\_message

(

&#x20;   id BIGINT PRIMARY KEY,



&#x20;   conversation\_id BIGINT,



&#x20;   role\_name VARCHAR(50),



&#x20;   content LONGTEXT,



&#x20;   token\_count INT,



&#x20;   create\_time DATETIME DEFAULT CURRENT\_TIMESTAMP

);

```



\---



\# 10. Redis设计



\## 用户缓存



```text

user:{userId}

```



示例：



```text

user:10001

```



\---



\## 登录Token



```text

token:{userId}

```



\---



\## 在线状态



```text

online:{userId}

```



\---



\## 好友列表缓存



```text

friend:{userId}

```



\---



\## 会话缓存



```text

session:{userId}

```



\---



\## 消息缓存



```text

msg:{userId}

```



\---



\# 11. Kafka设计



\## Topic规划



```text

linkx-message

单聊消息



linkx-group-message

群聊消息



linkx-notification

通知消息



linkx-file

文件消息



linkx-ai

AI任务

```



\---



\# 12. MinIO规划



Bucket：



```text

avatar



image



video



file



group



ai

```



目录：



```text

/avatar



/image



/video



/file



/group



/ai

```



\---



\# 13. 索引规范



所有表必须包含：



```sql

PRIMARY KEY(id)

```



时间字段：



```sql

create\_time



update\_time

```



查询频繁字段：



```sql

user\_id



group\_id



session\_id

```



必须建立索引。



\---



\# 14. 数据库演进规划



V0.1



\* 用户系统

\* 登录认证



V0.2



\* 好友系统



V0.3



\* 单聊系统



V0.4



\* 群聊系统



V0.5



\* 文件系统



V0.6



\* AI系统



V0.7



\* 音视频系统



V0.8



\* 企业协同



V1.0



\* 集群化部署

\* 百万级消息优化



\---



\# 15. 最终目标



支撑：



\* 10万+在线用户

\* 5万+单机连接

\* 百万级消息存储

\* 企业级IM部署



为 LinkX IM 提供稳定、高性能、可扩展的数据基础设施。



