# Sprint 1: 用户注册登录与JWT认证 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现用户注册、登录和JWT认证功能，为后续功能提供身份验证基础

**Architecture:** 采用Spring Boot 3单体模块化架构，服务端包含网关模块和核心服务模块。使用MySQL存储用户数据，Redis缓存Token，JWT实现无状态认证。

**Tech Stack:** Spring Boot 3, Spring Security, JWT (jjwt), MySQL 8, Redis, MyBatis-Plus, Maven

---

## 文件结构

```
LinkX/
├── pom.xml                          # 父POM
├── linkx-server/
│   ├── pom.xml                      # 服务端POM
│   └── src/
│       ├── main/
│       │   ├── java/com/linkx/server/
│       │   │   ├── LinkXServerApplication.java
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── RedisConfig.java
│       │   │   │   └── MyBatisPlusConfig.java
│       │   │   ├── common/
│       │   │   │   ├── Result.java
│       │   │   │   ├── ErrorCode.java
│       │   │   │   └── GlobalExceptionHandler.java
│       │   │   ├── security/
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   └── UserDetailsServiceImpl.java
│       │   │   ├── module/
│       │   │   │   └── auth/
│       │   │   │       ├── controller/AuthController.java
│       │   │   │       ├── service/AuthService.java
│       │   │   │       ├── service/impl/AuthServiceImpl.java
│       │   │   │       ├── dto/RegisterRequest.java
│       │   │   │       ├── dto/LoginRequest.java
│       │   │   │       └── dto/AuthResponse.java
│       │   │   └── entity/
│       │   │       └── SysUser.java
│       │   └── resources/
│       │       ├── application.yml
│       │       └── mapper/
│       │           └── SysUserMapper.xml
│       └── test/java/com/linkx/server/
│           └── module/auth/
│               └── AuthTest.java
```

---

### Task 1: Maven项目结构初始化

**Covers:** 项目基础架构

**Files:**
- Create: `pom.xml` (父POM)
- Create: `linkx-server/pom.xml` (服务端POM)
- Create: `linkx-server/src/main/java/com/linkx/server/LinkXServerApplication.java`

- [ ] **Step 1: 创建父POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.linkx</groupId>
    <artifactId>linkx</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>LinkX IM</name>

    <modules>
        <module>linkx-server</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>3.2.5</spring-boot.version>
        <mybatis-plus.version>3.5.6</mybatis-plus.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 2: 创建服务端POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.linkx</groupId>
        <artifactId>linkx</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>linkx-server</artifactId>
    <name>LinkX Server</name>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 创建启动类**

```java
package com.linkx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LinkXServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LinkXServerApplication.class, args);
    }
}
```

- [ ] **Step 4: 创建application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/linkx_im?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

linkx:
  jwt:
    secret: LinkXIM2026SecretKeyForJwtTokenGenerationMustBeLongEnough
    expiration: 86400000  # 24小时
    refresh-expiration: 604800000  # 7天
```

- [ ] **Step 5: 验证项目结构**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add pom.xml linkx-server/
git commit -m "feat: 初始化Maven项目结构"
```

---

### Task 2: 数据库表创建

**Covers:** 用户数据存储

**Files:**
- Create: `linkx-server/src/main/resources/schema.sql`

- [ ] **Step 1: 创建数据库初始化脚本**

```sql
CREATE DATABASE IF NOT EXISTS linkx_im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE linkx_im;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    signature VARCHAR(255) COMMENT '个性签名',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    region VARCHAR(100) COMMENT '地区',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_phone (phone),
    UNIQUE INDEX uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    login_ip VARCHAR(100) COMMENT '登录IP',
    device VARCHAR(255) COMMENT '设备',
    os VARCHAR(100) COMMENT '操作系统',
    browser VARCHAR(100) COMMENT '浏览器',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- Token黑名单表
CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    token VARCHAR(1000) COMMENT 'Token',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_token (token(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';
```

- [ ] **Step 2: 执行SQL脚本**

Run: `mysql -u root -p < D:/yangleduo/Code/Java/LinkX/linkx-server/src/main/resources/schema.sql`
Expected: 执行成功，创建3张表

- [ ] **Step 3: 提交**

```bash
git add linkx-server/src/main/resources/schema.sql
git commit -m "feat: 添加数据库初始化脚本"
```

---

### Task 3: 通用响应类和异常处理

**Covers:** API响应规范

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/common/Result.java`
- Create: `linkx-server/src/main/java/com/linkx/server/common/ErrorCode.java`
- Create: `linkx-server/src/main/java/com/linkx/server/common/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建Result响应类**

```java
package com.linkx.server.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
}
```

- [ ] **Step 2: 创建ErrorCode枚举**

```java
package com.linkx.server.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PHONE_EXISTS(1003, "手机号已注册"),
    EMAIL_EXISTS(1004, "邮箱已注册"),
    PASSWORD_ERROR(1005, "密码错误"),
    USER_DISABLED(1006, "用户已被禁用"),

    // 认证相关 2001-2999
    TOKEN_INVALID(2001, "Token无效"),
    TOKEN_EXPIRED(2002, "Token已过期"),
    TOKEN_BLACKLISTED(2003, "Token已被注销");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

- [ ] **Step 3: 创建全局异常处理器**

```java
package com.linkx.server.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getErrorCode());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public Result<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return Result.error(ErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        return Result.error(ErrorCode.PASSWORD_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }
}
```

- [ ] **Step 4: 创建BusinessException**

```java
package com.linkx.server.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

- [ ] **Step 5: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/common/
git commit -m "feat: 添加通用响应类和异常处理"
```

---

### Task 4: 用户实体和Mapper

**Covers:** 用户数据访问层

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/entity/SysUser.java`
- Create: `linkx-server/src/main/java/com/linkx/server/mapper/SysUserMapper.java`

- [ ] **Step 1: 创建用户实体类**

```java
package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String nickname;

    private String password;

    private String phone;

    private String email;

    private String avatar;

    private String signature;

    private Integer gender;

    private String region;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 2: 创建Mapper接口**

```java
package com.linkx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkx.server.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
```

- [ ] **Step 3: 创建MyBatis-Plus配置**

```java
package com.linkx.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
```

- [ ] **Step 4: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/entity/
git add linkx-server/src/main/java/com/linkx/server/mapper/
git add linkx-server/src/main/java/com/linkx/server/config/MyBatisPlusConfig.java
git commit -m "feat: 添加用户实体和Mapper"
```

---

### Task 5: JWT Token工具类

**Covers:** JWT认证核心

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/security/JwtTokenProvider.java`

- [ ] **Step 1: 创建JWT工具类**

```java
package com.linkx.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${linkx.jwt.secret}")
    private String jwtSecret;

    @Value("${linkx.jwt.expiration}")
    private long jwtExpiration;

    @Value("${linkx.jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("username", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed", e);
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/security/JwtTokenProvider.java
git commit -m "feat: 添加JWT Token工具类"
```

---

### Task 6: 注册登录DTO

**Covers:** API请求响应模型

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/module/auth/dto/RegisterRequest.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/auth/dto/LoginRequest.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/auth/dto/AuthResponse.java`

- [ ] **Step 1: 创建RegisterRequest**

```java
package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 50, message = "昵称长度1-50位")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50位")
    private String password;

    private String phone;

    private String email;
}
```

- [ ] **Step 2: 创建LoginRequest**

```java
package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 3: 创建AuthResponse**

```java
package com.linkx.server.module.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
}
```

- [ ] **Step 4: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/module/auth/dto/
git commit -m "feat: 添加注册登录DTO"
```

---

### Task 7: AuthService认证服务

**Covers:** 用户注册登录核心逻辑

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/module/auth/service/AuthService.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/auth/service/impl/AuthServiceImpl.java`

- [ ] **Step 1: 创建AuthService接口**

```java
package com.linkx.server.module.auth.service;

import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}
```

- [ ] **Step 2: 创建AuthServiceImpl实现**

```java
package com.linkx.server.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthService;
import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 检查手机号是否已注册
        if (request.getPhone() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getPhone, request.getPhone());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        }

        // 检查邮箱是否已注册
        if (request.getEmail() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getEmail, request.getEmail());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        // 生成Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 生成Token
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 验证RefreshToken
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        // 获取用户ID
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 生成新的Token
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/module/auth/service/
git commit -m "feat: 添加认证服务实现"
```

---

### Task 8: Security配置

**Covers:** Spring Security配置

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/config/SecurityConfig.java`
- Create: `linkx-server/src/main/java/com/linkx/server/security/JwtAuthenticationFilter.java`

- [ ] **Step 1: 创建JWT过滤器**

```java
package com.linkx.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 2: 创建SecurityConfig**

```java
package com.linkx.server.config;

import com.linkx.server.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 3: 创建UserDetailsService实现**

```java
package com.linkx.server.security;

import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        SysUser user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }

        return new User(
                String.valueOf(user.getId()),
                user.getPassword(),
                user.getStatus() == 1,
                true, true, true,
                Collections.emptyList()
        );
    }
}
```

- [ ] **Step 4: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/config/SecurityConfig.java
git add linkx-server/src/main/java/com/linkx/server/security/JwtAuthenticationFilter.java
git add linkx-server/src/main/java/com/linkx/server/security/UserDetailsServiceImpl.java
git commit -m "feat: 添加Security配置和JWT过滤器"
```

---

### Task 9: AuthController认证接口

**Covers:** 注册登录API

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/module/auth/controller/AuthController.java`

- [ ] **Step 1: 创建AuthController**

```java
package com.linkx.server.module.auth.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/module/auth/controller/AuthController.java
git commit -m "feat: 添加认证控制器"
```

---

### Task 10: 集成测试

**Covers:** 验证注册登录功能

**Files:**
- Create: `linkx-server/src/test/java/com/linkx/server/module/auth/AuthTest.java`

- [ ] **Step 1: 创建认证测试类**

```java
package com.linkx.server.module.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_register_successfully() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setNickname("Test User");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void should_login_successfully() throws Exception {
        // 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setNickname("Login User");
        registerRequest.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void should_fail_with_wrong_password() throws Exception {
        // 先注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("wrongpwduser");
        registerRequest.setNickname("Wrong Pwd User");
        registerRequest.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 错误密码登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpwduser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005));
    }
}
```

- [ ] **Step 2: 运行测试**

Run: `mvn test -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml -Dtest=AuthTest`
Expected: Tests pass

- [ ] **Step 3: 提交**

```bash
git add linkx-server/src/test/java/com/linkx/server/module/auth/AuthTest.java
git commit -m "feat: 添加认证集成测试"
```

---

### Task 11: Redis配置

**Covers:** Token缓存

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/config/RedisConfig.java`

- [ ] **Step 1: 创建Redis配置**

```java
package com.linkx.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn clean compile -f D:/yangleduo/Code/Java/LinkX/linkx-server/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/config/RedisConfig.java
git commit -m "feat: 添加Redis配置"
```

---

## 自检清单

1. **Spec覆盖:** ✓ 用户注册、登录、JWT认证均已覆盖
2. **占位符扫描:** ✓ 所有步骤包含完整代码
3. **类型一致性:** ✓ DTO、Entity、Service接口一致

---

## 执行方式

Plan saved. How would you like to execute it?

- **Subagent, always**: Fresh subagent per task — remember for future sessions
- **Subagent, this time**: Fresh subagent per task — just this once
- **Inline, always**: Execute in this session — remember for future sessions
- **Inline, this time**: Execute in this session — just this once
