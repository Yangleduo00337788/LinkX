package com.linkx.server.module.group.service;

import com.linkx.server.module.group.dto.GroupDTO;
import com.linkx.server.module.group.dto.GroupDetailDTO;
import com.linkx.server.module.group.dto.GroupRequestDTO;

import java.util.List;

public interface GroupService {
    GroupDTO createGroup(Long operatorId, String groupName, String groupAvatar, String notice, List<Long> memberIds);
    List<GroupDTO> getMyGroups(Long userId);
    List<GroupRequestDTO> getPendingRequests(Long userId);
    GroupDetailDTO getGroupDetail(Long userId, Long groupId);
    void addMembers(Long operatorId, Long groupId, List<Long> memberIds);
    void inviteMembers(Long operatorId, Long groupId, List<Long> memberIds, String message);
    void submitJoinRequest(Long userId, Long groupId, String message);
    void acceptRequest(Long userId, Long requestId);
    void rejectRequest(Long userId, Long requestId);
    void removeMember(Long operatorId, Long groupId, Long memberUserId);
    void setAdmin(Long operatorId, Long groupId, Long memberUserId);
    void removeAdmin(Long operatorId, Long groupId, Long memberUserId);
    void dissolveGroup(Long operatorId, Long groupId);
    void muteMember(Long operatorId, Long groupId, Long memberUserId, Integer muteMinutes);
    void unmuteMember(Long operatorId, Long groupId, Long memberUserId);
    void updateProfile(Long operatorId, Long groupId, String groupName, String groupAvatar);
    void updateNotice(Long operatorId, Long groupId, String notice);
    void leaveGroup(Long userId, Long groupId);
    void transferOwner(Long operatorId, Long groupId, Long newOwnerId);
    List<GroupDTO> searchGroups(Long userId, String keyword);
}
