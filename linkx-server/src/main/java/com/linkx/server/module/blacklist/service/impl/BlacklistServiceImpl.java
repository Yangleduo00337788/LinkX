package com.linkx.server.module.blacklist.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.blacklist.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.entity.ImSession;  // 行注：引入 ImSession 类型
import com.linkx.server.entity.SysBlacklist;  // 行注：引入 SysBlacklist 类型
import com.linkx.server.entity.SysFriend;  // 行注：引入 SysFriend 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.ImSessionMapper;  // 行注：引入 ImSessionMapper 类型
import com.linkx.server.mapper.SysBlacklistMapper;  // 行注：引入 SysBlacklistMapper 类型
import com.linkx.server.mapper.SysFriendMapper;  // 行注：引入 SysFriendMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.blacklist.dto.BlacklistUserDTO;  // 行注：引入 BlacklistUserDTO 类型
import com.linkx.server.module.blacklist.service.BlacklistService;  // 行注：引入 BlacklistService 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.transaction.annotation.Transactional;  // 行注：引入 Transactional 类型

import java.util.List;  // 行注：引入 List 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 拉黑时写入 {@link SysBlacklist}，并清理单向好友关系与会话侧展示（按业务规则）。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 BlacklistServiceImpl 类
public class BlacklistServiceImpl implements BlacklistService {

    private final SysBlacklistMapper blacklistMapper;  // 行注：注入黑名单Mapper依赖
    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final SysFriendMapper friendMapper;  // 行注：注入好友Mapper依赖
    private final ImSessionMapper sessionMapper;  // 行注：注入会话Mapper依赖

    /**
     * 添加黑名单。
     *
     * @param userId 用户 ID
     * @param targetUserId 目标用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义添加黑名单方法
    public void addBlacklist(Long userId, Long targetUserId) {
        // 行注：判断是否满足当前条件
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        SysUser targetUser = userMapper.selectById(targetUserId);  // 行注：初始化target用户
        // 行注：判断是否满足当前条件
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (blacklistMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已在黑名单中");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        SysBlacklist blacklist = new SysBlacklist();  // 行注：初始化黑名单
        blacklist.setUserId(userId);  // 行注：调用设置用户ID
        blacklist.setBlacklistUserId(targetUserId);  // 行注：调用设置黑名单用户ID
        blacklistMapper.insert(blacklist);  // 行注：调用insert

        LambdaQueryWrapper<SysFriend> deleteWrapper = new LambdaQueryWrapper<>();  // 行注：初始化删除条件封装器
        // 行注：补充当前表达式片段
        deleteWrapper.and(w -> w
                // 行注：继续调用等值条件
                .eq(SysFriend::getUserId, userId).eq(SysFriend::getFriendId, targetUserId)
                // 行注：继续调用or
                .or()
                .eq(SysFriend::getUserId, targetUserId).eq(SysFriend::getFriendId, userId));  // 行注：继续调用等值条件
        friendMapper.delete(deleteWrapper);  // 行注：调用删除

        deleteSingleSessions(userId, targetUserId);  // 行注：调用删除单聊会话列表
    }  // 行注：结束当前代码块

    /**
     * 移除黑名单。
     *
     * @param userId 用户 ID
     * @param targetUserId 目标用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义移除黑名单方法
    public void removeBlacklist(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);
        blacklistMapper.delete(wrapper);
        restoreFriendshipIfNotBlocked(userId, targetUserId);
    }

    /** 取消拉黑后恢复双向好友（拉黑时已删好友关系，不会自动加回）。 */
    private void restoreFriendshipIfNotBlocked(Long userId, Long targetUserId) {
        if (isBlacklisted(userId, targetUserId) || isBlacklisted(targetUserId, userId)) {
            return;
        }
        ensureFriendRow(userId, targetUserId);
        ensureFriendRow(targetUserId, userId);
    }

    private void ensureFriendRow(Long userId, Long friendId) {
        LambdaQueryWrapper<SysFriend> w = new LambdaQueryWrapper<>();
        w.eq(SysFriend::getUserId, userId).eq(SysFriend::getFriendId, friendId);
        if (friendMapper.selectCount(w) > 0) {
            return;
        }
        SysFriend row = new SysFriend();
        row.setUserId(userId);
        row.setFriendId(friendId);
        friendMapper.insert(row);
    }

    /**
     * 获取黑名单。
     *
     * @param userId 用户 ID
     * @return 黑名单用户DTO列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取黑名单方法
    public List<BlacklistUserDTO> getBlacklist(Long userId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysBlacklist::getUserId, userId)
                .orderByDesc(SysBlacklist::getCreateTime);  // 行注：继续调用排序降序
        List<SysBlacklist> list = blacklistMapper.selectList(wrapper);  // 行注：初始化列表
        // 行注：判断是否满足当前条件
        if (list.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：调用流
        java.util.Set<Long> blacklistedUserIds = list.stream()
                // 行注：继续调用映射
                .map(SysBlacklist::getBlacklistUserId)
                .collect(java.util.stream.Collectors.toSet());  // 行注：继续调用收集
        // 行注：调用selectBatchID列表
        java.util.Map<Long, SysUser> userMap = userMapper.selectBatchIds(blacklistedUserIds).stream()
                .collect(java.util.stream.Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));  // 行注：继续调用收集

        return list.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(b -> userMap.get(b.getBlacklistUserId()))
                // 行注：继续调用过滤
                .filter(u -> u != null)
                // 行注：继续调用映射
                .map(this::toBlacklistUserDTO)
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /**
     * 判断Blacklisted。
     *
     * @param userId 用户 ID
     * @param targetUserId 目标用户 ID
     * @return 是否满足条件
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义是否Blacklisted方法
    public boolean isBlacklisted(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);  // 行注：继续调用等值条件
        return blacklistMapper.selectCount(wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义转为黑名单用户DTO方法
    private BlacklistUserDTO toBlacklistUserDTO(SysUser user) {
        BlacklistUserDTO dto = new BlacklistUserDTO();  // 行注：初始化DTO
        dto.setId(user.getId());  // 行注：调用设置ID
        dto.setUsername(user.getUsername());  // 行注：调用设置Username
        dto.setNickname(user.getNickname());  // 行注：调用设置Nickname
        dto.setAvatar(user.getAvatar());  // 行注：调用设置头像
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义删除单聊会话列表方法
    private void deleteSingleSessions(Long userId, Long targetUserId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_SINGLE)
                // 行注：继续调用and
                .and(w -> w.eq(ImSession::getUserId, userId).eq(ImSession::getTargetId, targetUserId)
                        // 行注：继续调用or
                        .or()
                        .eq(ImSession::getUserId, targetUserId).eq(ImSession::getTargetId, userId));  // 行注：继续调用等值条件
        sessionMapper.delete(wrapper);  // 行注：调用删除
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
