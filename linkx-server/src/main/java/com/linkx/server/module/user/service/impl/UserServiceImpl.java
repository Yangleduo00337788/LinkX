package com.linkx.server.module.user.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.user.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.UploadAssetUrlUtils;  // 行注：引入 UploadAssetUrlUtils 类型
import com.linkx.server.config.LinkxAppProperties;  // 行注：引入 LinkxAppProperties 类型
import com.linkx.server.entity.SysFriend;  // 行注：引入 SysFriend 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.SysFriendMapper;  // 行注：引入 SysFriendMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.user.dto.UpdateProfileRequest;  // 行注：引入 UpdateProfileRequest 类型
import com.linkx.server.module.user.dto.UserProfileDTO;  // 行注：引入 UserProfileDTO 类型
import com.linkx.server.module.user.service.UserService;  // 行注：引入 UserService 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.util.List;  // 行注：引入 List 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 用户资料实现：好友关系校验、头像 URL 规范化、搜索去重与条数限制。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 UserServiceImpl 类
public class UserServiceImpl implements UserService {

    private static final int SEARCH_KEYWORD_MIN_LENGTH = 2;  // 行注：定义搜索KEYWORD最小长度常量
    private static final int SEARCH_RESULT_LIMIT = 20;  // 行注：定义搜索结果限制常量

    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final SysFriendMapper friendMapper;  // 行注：注入好友Mapper依赖
    private final LinkxAppProperties linkxAppProperties;  // 行注：注入linkx应用属性依赖

    /**
     * 获取资料。
     *
     * @param requesterId 请求发起用户 ID
     * @param userId 用户 ID
     * @return 用户资料
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取Profile方法
    public UserProfileDTO getProfile(Long requesterId, Long userId) {
        // 行注：判断是否满足当前条件
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户ID无效");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (requesterId != null && requesterId.equals(userId)) {
            return getMyProfile(userId);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (requesterId == null || !hasFriendRelation(requesterId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅可查看好友资料");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        SysUser user = requireActiveUser(userId);  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return toDTO(user);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 获取我的资料。
     *
     * @param userId 用户 ID
     * @return 用户资料
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取我的Profile方法
    public UserProfileDTO getMyProfile(Long userId) {
        SysUser user = requireActiveUser(userId);  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return toDTO(user);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 更新资料。
     *
     * @param userId 用户 ID
     * @param request 当前请求或请求对象
     * @return 用户资料
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义更新Profile方法
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = requireActiveUser(userId);  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (request.getNickname() != null) {
            String trimmedNickname = request.getNickname().trim();  // 行注：初始化trimmedNickname
            // 行注：判断是否满足当前条件
            if (trimmedNickname.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "昵称不能为空");  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
            user.setNickname(trimmedNickname);  // 行注：调用设置Nickname
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (request.getAvatar() != null) {
            // 行注：调用设置头像
            user.setAvatar(UploadAssetUrlUtils.normalizeAvatarUrl(request.getAvatar(), linkxAppProperties.getUpload().getUrl(), "头像"));
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (request.getGender() != null) {
            user.setGender(request.getGender());  // 行注：调用设置Gender
        }  // 行注：结束当前代码块

        userMapper.updateById(user);  // 行注：调用更新ID
        return toDTO(user);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 搜索用户。
     *
     * @param requesterId 请求发起用户 ID
     * @param keyword 搜索关键词
     * @return 用户资料列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义搜索Users方法
    public List<UserProfileDTO> searchUsers(Long requesterId, String keyword) {
        // 行注：判断是否满足当前条件
        if (requesterId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (keyword == null || keyword.trim().length() < SEARCH_KEYWORD_MIN_LENGTH) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String trimmedKeyword = keyword.trim();  // 行注：初始化trimmedKeyword
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysUser::getStatus, 1)
                // 行注：继续调用ne
                .ne(SysUser::getId, requesterId)
                // 行注：继续调用and
                .and(w -> w
                        // 行注：继续调用like
                        .like(SysUser::getUsername, trimmedKeyword)
                        // 行注：继续调用or
                        .or()
                        // 行注：继续调用like
                        .like(SysUser::getNickname, trimmedKeyword)
                );  // 行注：结束当前参数配置
        wrapper.last("LIMIT " + SEARCH_RESULT_LIMIT);  // 行注：调用最后

        List<SysUser> users = userMapper.selectList(wrapper);  // 行注：初始化users
        return users.stream().map(this::toDTO).collect(Collectors.toList());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否包含好友Relation方法
    private boolean hasFriendRelation(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysFriend> forward = new LambdaQueryWrapper<>();  // 行注：初始化forward
        // 行注：调用等值条件
        forward.eq(SysFriend::getUserId, userId)
                .eq(SysFriend::getFriendId, targetUserId);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (friendMapper.selectCount(forward) > 0) {
            return true;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFriend> reverse = new LambdaQueryWrapper<>();  // 行注：初始化reverse
        // 行注：调用等值条件
        reverse.eq(SysFriend::getUserId, targetUserId)
                .eq(SysFriend::getFriendId, userId);  // 行注：继续调用等值条件
        return friendMapper.selectCount(reverse) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义require启用用户方法
    private SysUser requireActiveUser(Long userId) {
        SysUser user = userMapper.selectById(userId);  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return user;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义转为DTO方法
    private UserProfileDTO toDTO(SysUser user) {
        UserProfileDTO dto = new UserProfileDTO();  // 行注：初始化DTO
        dto.setId(user.getId());  // 行注：调用设置ID
        dto.setUsername(user.getUsername());  // 行注：调用设置Username
        dto.setNickname(user.getNickname());  // 行注：调用设置Nickname
        dto.setAvatar(user.getAvatar());  // 行注：调用设置头像
        dto.setGender(user.getGender());  // 行注：调用设置Gender
        dto.setCreateTime(user.getCreateTime());  // 行注：调用设置创建时间
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
