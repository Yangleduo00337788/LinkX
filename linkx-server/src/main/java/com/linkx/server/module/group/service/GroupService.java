package com.linkx.server.module.group.service;  // 行注：声明当前文件所在包 com.linkx.server.module.group.service

import com.linkx.server.module.group.dto.GroupDTO;  // 行注：引入 GroupDTO 类型
import com.linkx.server.module.group.dto.GroupDetailDTO;  // 行注：引入 GroupDetailDTO 类型
import com.linkx.server.module.group.dto.GroupNoticeItemDTO;
import com.linkx.server.module.group.dto.GroupRequestDTO;  // 行注：引入 GroupRequestDTO 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 群组生命周期与成员管理：创建、邀请/入群申请、角色、禁言、转让群主、群偏好与公告等。
 */
// 行注：定义 GroupService 接口
public interface GroupService {
    /**
     * 创建群。
     *
     * @param operatorId 操作人用户 ID
     * @param groupName 群名称
     * @param groupAvatar 群头像 URL
     * @param notice 群公告
     * @param memberIds 成员 ID 列表
     * @return 群信息
     */
    // 行注：调用创建群
    GroupDTO createGroup(Long operatorId, String groupName, String groupAvatar, String notice, List<Long> memberIds);
    /**
     * 获取我的群。
     *
     * @param userId 用户 ID
     * @return 群信息列表
     */
    List<GroupDTO> getMyGroups(Long userId);  // 行注：调用获取我的Groups
    /**
     * 获取待处理申请。
     *
     * @param userId 用户 ID
     * @return 群申请列表
     */
    List<GroupRequestDTO> getPendingRequests(Long userId);  // 行注：调用获取Pending请求
    /**
     * 获取群详情。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @return 群详情
     */
    GroupDetailDTO getGroupDetail(Long userId, Long groupId);  // 行注：调用获取群详情
    /**
     * 添加成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberIds 成员 ID 列表
     */
    void addMembers(Long operatorId, Long groupId, List<Long> memberIds);  // 行注：调用添加Members
    /**
     * 邀请成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberIds 成员 ID 列表
     * @param message 附言内容
     */
    void inviteMembers(Long operatorId, Long groupId, List<Long> memberIds, String message);  // 行注：调用邀请Members
    /**
     * 提交加入申请。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param message 附言内容
     */
    void submitJoinRequest(Long userId, Long groupId, String message);  // 行注：调用submit加入请求
    /**
     * 通过申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    void acceptRequest(Long userId, Long requestId);  // 行注：调用accept请求
    /**
     * 拒绝申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    void rejectRequest(Long userId, Long requestId);  // 行注：调用reject请求
    /**
     * 移除成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    void removeMember(Long operatorId, Long groupId, Long memberUserId);  // 行注：调用移除成员
    /**
     * 设置管理员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    void setAdmin(Long operatorId, Long groupId, Long memberUserId);  // 行注：调用设置Admin
    /**
     * 移除管理员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    void removeAdmin(Long operatorId, Long groupId, Long memberUserId);  // 行注：调用移除Admin
    /**
     * 处理dissolve群。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     */
    void dissolveGroup(Long operatorId, Long groupId);  // 行注：调用dissolve群
    /**
     * 设置成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     * @param muteMinutes 禁言分钟数
     */
    void muteMember(Long operatorId, Long groupId, Long memberUserId, Integer muteMinutes);  // 行注：调用mute成员
    /**
     * 解除成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    void unmuteMember(Long operatorId, Long groupId, Long memberUserId);  // 行注：调用unmute成员
    /**
     * 更新资料。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param groupName 群名称
     * @param groupAvatar 群头像 URL
     */
    void updateProfile(Long operatorId, Long groupId, String groupName, String groupAvatar);  // 行注：调用更新Profile
    /**
     * 更新公告。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param notice 群公告
     */
    void updateNotice(Long operatorId, Long groupId, String notice);  // 行注：调用更新Notice
    /**
     * 更新Preferences。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param groupRemark 群内备注
     * @param notificationMuted 是否开启消息免打扰
     */
    void updatePreferences(Long userId, Long groupId, String groupRemark, Boolean notificationMuted, String memberCardName);

    List<GroupNoticeItemDTO> listGroupNotices(Long userId, Long groupId);

    void createGroupNotice(Long operatorId, Long groupId, String content, Boolean pinned);

    void setGroupNoticePinned(Long operatorId, Long groupId, Long noticeId, Boolean pinned);
    /**
     * 标记公告已读。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     */
    void markNoticeRead(Long userId, Long groupId);  // 行注：调用标记Notice已读
    /**
     * 获取群媒体。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param mediaType 媒体类型
     * @param keyword 搜索关键词
     * @param size 返回条数
     * @return 消息列表
     */
    List<MessageDTO> getGroupMedia(Long userId, Long groupId, String mediaType, String keyword, int size);  // 行注：调用获取群Media
    /**
     * 搜索群Messages。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param keyword 搜索关键词
     * @param size 返回条数
     * @return 消息列表
     */
    List<MessageDTO> searchGroupMessages(Long userId, Long groupId, String keyword, int size);  // 行注：调用搜索群消息
    /**
     * 退出群。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     */
    void leaveGroup(Long userId, Long groupId);  // 行注：调用leave群
    /**
     * 转让Owner。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param newOwnerId 新群主用户 ID
     */
    void transferOwner(Long operatorId, Long groupId, Long newOwnerId);  // 行注：调用transfer所有者
    /**
     * 搜索群。
     *
     * @param userId 用户 ID
     * @param keyword 搜索关键词
     * @return 群信息列表
     */
    List<GroupDTO> searchGroups(Long userId, String keyword);  // 行注：调用搜索Groups
}  // 行注：结束当前代码块
