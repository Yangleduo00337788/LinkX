package com.linkx.server.module.friend.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.friend.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;  // 行注：引入 LambdaUpdateWrapper 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.TextNormalizer;
import com.linkx.server.entity.SysFriend;  // 行注：引入 SysFriend 类型
import com.linkx.server.entity.SysFriendRequest;  // 行注：引入 SysFriendRequest 类型
import com.linkx.server.entity.ImSession;  // 行注：引入 ImSession 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.ImSessionMapper;  // 行注：引入 ImSessionMapper 类型
import com.linkx.server.mapper.SysFriendMapper;  // 行注：引入 SysFriendMapper 类型
import com.linkx.server.mapper.SysFriendRequestMapper;  // 行注：引入 SysFriendRequestMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.blacklist.service.BlacklistService;  // 行注：引入 BlacklistService 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.friend.dto.FriendDTO;  // 行注：引入 FriendDTO 类型
import com.linkx.server.module.friend.dto.FriendRequestDTO;  // 行注：引入 FriendRequestDTO 类型
import com.linkx.server.module.friend.service.FriendService;  // 行注：引入 FriendService 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.dao.DuplicateKeyException;  // 行注：引入 DuplicateKeyException 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.transaction.annotation.Transactional;  // 行注：引入 Transactional 类型

import java.util.List;  // 行注：引入 List 类型
import java.util.Objects;  // 行注：引入 Objects 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 好友业务：申请、同意时建立双向 {@link SysFriend} 并创建单聊 {@link ImSession}；
 * 校验黑名单、重复申请、已是好友等。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 FriendServiceImpl 类
public class FriendServiceImpl implements FriendService {

    private final SysFriendMapper friendMapper;  // 行注：注入好友Mapper依赖
    private final SysFriendRequestMapper requestMapper;  // 行注：注入请求Mapper依赖
    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final ImSessionMapper sessionMapper;  // 行注：注入会话Mapper依赖
    private final BlacklistService blacklistService;  // 行注：注入黑名单服务依赖

    /**
     * 发送好友申请。
     *
     * @param fromUserId from用户Id
     * @param toUserId to用户Id
     * @param message 附言内容
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义发送好友请求方法
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        // 自己不能给自己发送好友申请，提前拦截无效输入。
        // 行注：判断是否满足当前条件
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 只允许向存在且启用中的账号发起申请。
        SysUser toUser = requireActiveUser(toUserId);  // 行注：初始化转为用户
        // 行注：判断是否满足当前条件
        if (toUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 双向黑名单都要校验：被对方拉黑时禁止打扰，对方已在自己黑名单中也不允许继续申请。
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(toUserId, fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被对方拉黑");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已将对方拉黑");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        LambdaQueryWrapper<SysFriend> existWrapper = new LambdaQueryWrapper<>();  // 行注：初始化exist条件封装器
        // 行注：补充当前表达式片段
        existWrapper.and(w -> w
                // 行注：继续调用等值条件
                .eq(SysFriend::getUserId, fromUserId)
                // 行注：继续调用等值条件
                .eq(SysFriend::getFriendId, toUserId)
        );  // 行注：结束当前参数配置
        // 行注：判断是否满足当前条件
        if (friendMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你们已经是好友了");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 同方向存在待处理申请时，不再重复创建记录，避免列表里出现多条相同请求。
        LambdaQueryWrapper<SysFriendRequest> pendingWrapper = new LambdaQueryWrapper<>();  // 行注：初始化pending条件封装器
        // 行注：调用等值条件
        pendingWrapper.eq(SysFriendRequest::getFromUserId, fromUserId)
                // 行注：继续调用等值条件
                .eq(SysFriendRequest::getToUserId, toUserId)
                .eq(SysFriendRequest::getStatus, 0);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (requestMapper.selectCount(pendingWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已发送过好友申请，请等待对方处理");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        LambdaQueryWrapper<SysFriendRequest> reversePendingWrapper = new LambdaQueryWrapper<>();  // 行注：初始化reversePending条件封装器
        // 行注：调用等值条件
        reversePendingWrapper.eq(SysFriendRequest::getFromUserId, toUserId)
                // 行注：继续调用等值条件
                .eq(SysFriendRequest::getToUserId, fromUserId)
                .eq(SysFriendRequest::getStatus, 0);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (requestMapper.selectCount(reversePendingWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "对方已向你发送好友申请，请先处理");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 只有通过了用户状态、黑名单、好友关系和重复申请校验后，才真正落待处理申请。
        SysFriendRequest request = new SysFriendRequest();  // 行注：初始化请求
        request.setFromUserId(fromUserId);  // 行注：调用设置用户ID
        request.setToUserId(toUserId);  // 行注：调用设置转为用户ID
        request.setMessage(message);  // 行注：调用设置消息
        request.setStatus(0);  // 行注：调用设置状态
        requestMapper.insert(request);  // 行注：调用insert
    }  // 行注：结束当前代码块

    /**
     * 获取待处理申请。
     *
     * @param userId 用户 ID
     * @return 好友申请DTO列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取Pending请求方法
    public List<FriendRequestDTO> getPendingRequests(Long userId) {
        LambdaQueryWrapper<SysFriendRequest> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysFriendRequest::getToUserId, userId)
                // 行注：继续调用等值条件
                .eq(SysFriendRequest::getStatus, 0)
                .orderByDesc(SysFriendRequest::getCreateTime);  // 行注：继续调用排序降序

        List<SysFriendRequest> requests = requestMapper.selectList(wrapper);  // 行注：初始化请求
        // 行注：判断是否满足当前条件
        if (requests.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：调用流
        Set<Long> fromUserIds = requests.stream()
                // 行注：继续调用映射
                .map(SysFriendRequest::getFromUserId)
                // 行注：继续调用过滤
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：调用selectBatchID列表
        java.util.Map<Long, SysUser> userMap = userMapper.selectBatchIds(fromUserIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));  // 行注：继续调用收集

        return requests.stream().map(r -> {  // 行注：返回处理结果
            FriendRequestDTO dto = new FriendRequestDTO();  // 行注：初始化DTO
            dto.setId(r.getId());  // 行注：调用设置ID
            dto.setFromUserId(r.getFromUserId());  // 行注：调用设置用户ID
            dto.setMessage(r.getMessage());  // 行注：调用设置消息
            dto.setStatus(r.getStatus());  // 行注：调用设置状态
            dto.setCreateTime(r.getCreateTime());  // 行注：调用设置创建时间

            SysUser fromUser = userMap.get(r.getFromUserId());  // 行注：初始化用户
            // 行注：判断是否满足当前条件
            if (fromUser != null) {
                dto.setFromUsername(fromUser.getUsername());  // 行注：调用设置Username
                dto.setFromNickname(fromUser.getNickname());  // 行注：调用设置Nickname
                dto.setFromAvatar(fromUser.getAvatar());  // 行注：调用设置头像
            }  // 行注：结束当前代码块
            return dto;  // 行注：返回处理结果
        }).collect(Collectors.toList());  // 行注：调用收集
    }  // 行注：结束当前代码块

    /**
     * 通过申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义accept请求方法
    public void acceptRequest(Long userId, Long requestId) {
        SysFriendRequest request = requestMapper.selectById(requestId);  // 行注：初始化请求
        // 行注：判断是否满足当前条件
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 通过条件更新“抢占”待处理申请，避免重复点击或并发处理把同一请求执行两次。
        // 行注：判断是否满足当前条件
        if (!claimPendingRequest(requestId, userId, 1)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 接受前再次校验黑名单，避免申请发出后双方关系发生变化。
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(request.getFromUserId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被对方拉黑");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(userId, request.getFromUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已将对方拉黑");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        SysFriend friend1 = new SysFriend();  // 行注：初始化好友1
        friend1.setUserId(request.getFromUserId());  // 行注：调用设置用户ID
        friend1.setFriendId(request.getToUserId());  // 行注：调用设置好友ID
        // 好友关系是双向各一条记录，便于双方独立查询、备注与后续删除。
        insertFriendIfAbsent(friend1);  // 行注：调用insert好友IfAbsent

        SysFriend friend2 = new SysFriend();  // 行注：初始化好友2
        friend2.setUserId(request.getToUserId());  // 行注：调用设置用户ID
        friend2.setFriendId(request.getFromUserId());  // 行注：调用设置好友ID
        insertFriendIfAbsent(friend2);  // 行注：调用insert好友IfAbsent
    }  // 行注：结束当前代码块

    /**
     * 拒绝申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义reject请求方法
    public void rejectRequest(Long userId, Long requestId) {
        SysFriendRequest request = requestMapper.selectById(requestId);  // 行注：初始化请求
        // 行注：判断是否满足当前条件
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 拒绝也走同样的状态抢占逻辑，保证待处理申请只能被消费一次。
        // 行注：判断是否满足当前条件
        if (!claimPendingRequest(requestId, userId, 2)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 获取好友列表。
     *
     * @param userId 用户 ID
     * @return 好友DTO列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取好友列表方法
    public List<FriendDTO> getFriendList(Long userId) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysFriend::getUserId, userId);  // 行注：调用等值条件

        List<SysFriend> friends = friendMapper.selectList(wrapper);  // 行注：初始化friends
        // 行注：判断是否满足当前条件
        if (friends.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：调用流
        Set<Long> friendUserIds = friends.stream()
                // 行注：继续调用映射
                .map(SysFriend::getFriendId)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：调用selectBatchID列表
        java.util.Map<Long, SysUser> userMap = userMapper.selectBatchIds(friendUserIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));  // 行注：继续调用收集

        return friends.stream().map(f -> {  // 行注：返回处理结果
            SysUser friendUser = userMap.get(f.getFriendId());  // 行注：初始化好友用户
            // 行注：判断是否满足当前条件
            if (friendUser == null) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块

            FriendDTO dto = new FriendDTO();  // 行注：初始化DTO
            dto.setId(f.getId());  // 行注：调用设置ID
            dto.setUserId(f.getUserId());  // 行注：调用设置用户ID
            dto.setFriendId(f.getFriendId());  // 行注：调用设置好友ID
            dto.setRemark(f.getRemark());  // 行注：调用设置Remark
            dto.setCreateTime(f.getCreateTime());  // 行注：调用设置创建时间
            dto.setFriendUsername(friendUser.getUsername());  // 行注：调用设置好友Username
            dto.setFriendNickname(friendUser.getNickname());  // 行注：调用设置好友Nickname
            dto.setFriendAvatar(friendUser.getAvatar());  // 行注：调用设置好友头像
            return dto;  // 行注：返回处理结果
        }).filter(Objects::nonNull).collect(Collectors.toList());  // 行注：调用过滤
    }  // 行注：结束当前代码块

    /**
     * 删除好友。
     *
     * @param userId 用户 ID
     * @param friendId 好友Id
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义删除好友方法
    public void deleteFriend(Long userId, Long friendId) {
        // 删除好友时要同时清掉正向和反向关系，保证双方列表一致。
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysFriend::getUserId, userId)
                .eq(SysFriend::getFriendId, friendId);  // 行注：继续调用等值条件
        friendMapper.delete(wrapper);  // 行注：调用删除

        LambdaQueryWrapper<SysFriend> reverseWrapper = new LambdaQueryWrapper<>();  // 行注：初始化reverse条件封装器
        // 行注：调用等值条件
        reverseWrapper.eq(SysFriend::getUserId, friendId)
                .eq(SysFriend::getFriendId, userId);  // 行注：继续调用等值条件
        friendMapper.delete(reverseWrapper);  // 行注：调用删除

        // 关系解除后顺手删除双向单聊会话，避免历史入口继续出现在会话列表中。
        deleteSingleSessions(userId, friendId);  // 行注：调用删除单聊会话列表
    }  // 行注：结束当前代码块

    @Override
    @Transactional
    public void updateFriendRemark(Long userId, Long friendId, String remark) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFriend::getUserId, userId).eq(SysFriend::getFriendId, friendId);
        SysFriend row = friendMapper.selectOne(wrapper);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "好友关系不存在");
        }
        String normalized = TextNormalizer.normalizeOptionalSingleLine(remark, 64, "好友备注");
        row.setRemark(normalized);
        friendMapper.updateById(row);
    }

    // 行注：定义删除单聊会话列表方法
    private void deleteSingleSessions(Long userId, Long friendId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_SINGLE)
                // 行注：继续调用and
                .and(w -> w.eq(ImSession::getUserId, userId).eq(ImSession::getTargetId, friendId)
                        // 行注：继续调用or
                        .or()
                        .eq(ImSession::getUserId, friendId).eq(ImSession::getTargetId, userId));  // 行注：继续调用等值条件
        sessionMapper.delete(wrapper);  // 行注：调用删除
    }  // 行注：结束当前代码块

    // 行注：定义claimPending请求方法
    private boolean claimPendingRequest(Long requestId, Long userId, int targetStatus) {
        // 只有“我收到的、当前仍是待处理”的申请才能被更新，天然具备幂等保护。
        LambdaUpdateWrapper<SysFriendRequest> wrapper = new LambdaUpdateWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(SysFriendRequest::getId, requestId)
                // 行注：继续调用等值条件
                .eq(SysFriendRequest::getToUserId, userId)
                // 行注：继续调用等值条件
                .eq(SysFriendRequest::getStatus, 0)
                .set(SysFriendRequest::getStatus, targetStatus);  // 行注：继续调用设置
        return requestMapper.update(null, wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义insert好友IfAbsent方法
    private void insertFriendIfAbsent(SysFriend friend) {
        // 行注：尝试执行可能失败的逻辑
        try {
            friendMapper.insert(friend);  // 行注：调用insert
        // 行注：执行当前方法调用
        } catch (DuplicateKeyException ignored) {
            // 并发同意或重试时可能撞上唯一索引，这里按幂等成功处理，避免返回 500。
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义require启用用户方法
    private SysUser requireActiveUser(Long userId) {
        SysUser user = userMapper.selectById(userId);  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null || !Objects.equals(user.getStatus(), 1)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return user;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
