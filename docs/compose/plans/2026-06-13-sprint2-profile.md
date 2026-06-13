# Sprint 2: 用户资料与搜索用户 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or use this plan task-by-task.

**Goal:** 实现用户资料查看/修改和搜索用户功能

**Architecture:** 在已有Spring Boot架构上扩展，新增User模块处理用户资料相关业务。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Spring Security

---

### Task 1: UserController用户资料接口

**Files:**
- Create: `linkx-server/src/main/java/com/linkx/server/module/user/controller/UserController.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/user/service/UserService.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/user/service/impl/UserServiceImpl.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/user/dto/UserProfileDTO.java`
- Create: `linkx-server/src/main/java/com/linkx/server/module/user/dto/UpdateProfileRequest.java`

- [ ] **Step 1: 创建UserProfileDTO**

```java
package com.linkx.server.module.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String signature;
    private Integer gender;
    private String region;
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 创建UpdateProfileRequest**

```java
package com.linkx.server.module.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 1, max = 50, message = "昵称长度1-50位")
    private String nickname;

    private String avatar;

    @Size(max = 255, message = "签名最长255位")
    private String signature;

    private Integer gender;

    private String region;
}
```

- [ ] **Step 3: 创建UserService接口**

```java
package com.linkx.server.module.user.service;

import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;

import java.util.List;

public interface UserService {
    UserProfileDTO getProfile(Long userId);
    UserProfileDTO getMyProfile(Long userId);
    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);
    List<UserProfileDTO> searchUsers(String keyword);
}
```

- [ ] **Step 4: 创建UserServiceImpl**

```java
package com.linkx.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;
import com.linkx.server.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;

    @Override
    public UserProfileDTO getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toDTO(user);
    }

    @Override
    public UserProfileDTO getMyProfile(Long userId) {
        return getProfile(userId);
    }

    @Override
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getRegion() != null) {
            user.setRegion(request.getRegion());
        }

        userMapper.updateById(user);
        return toDTO(user);
    }

    @Override
    public List<UserProfileDTO> searchUsers(String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getNickname, keyword)
        );
        wrapper.last("LIMIT 20");

        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private UserProfileDTO toDTO(SysUser user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setSignature(user.getSignature());
        dto.setGender(user.getGender());
        dto.setRegion(user.getRegion());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
```

- [ ] **Step 5: 创建UserController**

```java
package com.linkx.server.module.user.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;
import com.linkx.server.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.getMyProfile(userId));
    }

    @GetMapping("/{userId}")
    public Result<UserProfileDTO> getProfile(@PathVariable Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public Result<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.updateProfile(userId, request));
    }

    @GetMapping("/search")
    public Result<List<UserProfileDTO>> searchUsers(@RequestParam String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }
}
```

- [ ] **Step 6: 更新SecurityConfig放行搜索接口（可选，搜索需要登录）**

无需修改，搜索接口需要认证。

- [ ] **Step 7: 验证编译**

Run: `mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 8: 提交**

```bash
git add linkx-server/src/main/java/com/linkx/server/module/user/
git commit -m "feat: Sprint 2 - 添加用户资料查看/修改和搜索用户功能"
```

---

## API总结

| API | 方法 | 路径 | 说明 | 认证 |
|-----|------|------|------|------|
| 我的资料 | GET | `/api/user/me` | 获取当前用户资料 | 需要 |
| 用户资料 | GET | `/api/user/{userId}` | 获取指定用户资料 | 需要 |
| 修改资料 | PUT | `/api/user/me` | 修改当前用户资料 | 需要 |
| 搜索用户 | GET | `/api/user/search?keyword=xxx` | 按用户名/昵称搜索 | 需要 |
