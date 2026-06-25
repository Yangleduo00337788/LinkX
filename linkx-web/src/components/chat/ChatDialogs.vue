<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染 ChatMediaPreview 组件 -->
  <ChatMediaPreview
    :visible="showImagePreview"
    :image-url="previewImageUrl"
    @close="$emit('close-image-preview')"
  />
  <!-- 行注：渲染 ChatDownloadDialog 组件 -->
  <ChatDownloadDialog
    :visible="showDownloadModal"
    :file-name="downloadFileName"
    :file-size="downloadFileSize"
    :progress="downloadProgress"
    @close="$emit('close-download-dialog')"
    @open="$emit('open-downloaded-file')"
    @save="$emit('save-downloaded-file')"
  />
  <!-- 行注：渲染 CreateGroupModal 组件 -->
  <CreateGroupModal
    :visible="showCreateGroupModal"
    :group-name="createGroupForm.groupName"
    :notice="createGroupForm.notice"
    :member-ids="createGroupForm.memberIds"
    :avatar-preview="createGroupForm.avatarPreview"
    :friends="friends"
    :creating-group="creatingGroup"
    @close="$emit('close-create-group-modal')"
    @trigger-avatar-upload="$emit('trigger-group-avatar-upload')"
    @update:group-name="$emit('update:create-group-name', $event)"
    @update:notice="$emit('update:create-group-notice', $event)"
    @update:member-ids="$emit('update:create-group-member-ids', $event)"
    @submit="$emit('submit-create-group')"
  />
  <!-- 行注：渲染 AddMembersModal 组件 -->
  <AddMembersModal
    :visible="showAddMembersModal"
    :available-friends="availableFriendsForCurrentGroup"
    :selected-member-ids="addMembersSelection"
    :invite-message="addMembersMessage"
    :adding-members="addingMembers"
    @close="$emit('close-add-members-modal')"
    @update:selected-member-ids="$emit('update:add-members-selection', $event)"
    @update:invite-message="$emit('update:add-members-message', $event)"
    @submit="$emit('submit-add-members')"
  />
  <!-- 行注：渲染 TransferOwnerModal 组件 -->
  <TransferOwnerModal
    :visible="showTransferOwnerModal"
    :members="groupDetail?.members || []"
    :selected-owner-id="transferOwnerSelection"
    :transferring-owner="transferringOwner"
    @close="$emit('close-transfer-owner-modal')"
    @update:selected-owner-id="$emit('update:transfer-owner-selection', $event)"
    @submit="$emit('submit-transfer-owner')"
  />
  <!-- 行注：渲染 MuteMemberModal 组件 -->
  <MuteMemberModal
    :visible="showMuteMemberModal"
    :member="muteTargetMember"
    :mute-minutes-input="muteMinutesInput"
    :muting-member="mutingMember"
    @close="$emit('close-mute-member-modal')"
    @update:mute-minutes-input="$emit('update:mute-minutes-input', $event)"
    @submit="$emit('submit-mute-member')"
  />
  <!-- 行注：渲染 GroupDetailPanel 组件 -->
  <GroupDetailPanel
    :visible="showGroupDrawer"
    :group-detail="groupDetail"
    :group-profile-name="groupProfileName"
    :group-profile-avatar-preview="groupProfileAvatarPreview"
    :notice-draft="noticeDraft"
    :current-user-id="currentUserId"
    :can-edit-group-profile="canEditGroupProfile"
    :updating-group-profile="updatingGroupProfile"
    :is-group-profile-changed="isGroupProfileChanged"
    :can-edit-notice="canEditNotice"
    :updating-notice="updatingNotice"
    :is-group-notice-changed="isGroupNoticeChanged"
    :can-manage-members="canManageMembers"
    :can-dissolve-group="canDissolveGroup"
    :is-group-owner="isGroupOwner"
    :can-operate-member="canOperateMember"
    :can-toggle-admin="canToggleAdmin"
    @close="$emit('close-group-drawer')"
    @trigger-group-profile-avatar-upload="$emit('trigger-group-profile-avatar-upload')"
    @update:group-profile-name="$emit('update:group-profile-name', $event)"
    @update:notice-draft="$emit('update:notice-draft', $event)"
    @save-group-profile="$emit('save-group-profile')"
    @copy-group-id="$emit('copy-group-id', $event)"
    @save-notice="$emit('save-notice')"
    @open-group-members-page="$emit('open-group-members-page')"
    @open-add-members-modal="$emit('open-add-members-modal')"
    @toggle-admin-role="$emit('toggle-admin-role', $event)"
    @toggle-mute-member="$emit('toggle-mute-member', $event)"
    @remove-member="$emit('remove-member', $event)"
    @dissolve-group="$emit('dissolve-group')"
    @open-transfer-owner-modal="$emit('open-transfer-owner-modal')"
    @leave-group="$emit('leave-group')"
  />
  <!-- 行注：渲染 ConfirmDialog 组件 -->
  <ConfirmDialog
    v-model:visible="confirmDialogVisibleProxy"
    :title="confirmDialog.title"
    :subtitle="confirmDialog.subtitle"
    :description="confirmDialog.description"
    :cancel-text="confirmDialog.cancelText"
    :confirm-text="confirmDialog.confirmText"
    :confirm-type="confirmDialog.confirmType"
    @cancel="$emit('cancel-confirm-dialog')"
    @confirm="$emit('confirm-confirm-dialog')"
  />
  <!-- 行注：渲染 GroupNoticeDialog 组件 -->
  <GroupNoticeDialog
    v-model:visible="noticeReminderVisibleProxy"
    :group-name="groupDetail?.groupName"
    :notice="groupDetail?.notice"
    :update-time-text="groupNoticeUpdateTimeText"
    :loading="acknowledgingNoticeReminder"
    @acknowledge="$emit('acknowledge-notice-reminder')"
  />
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * ChatDialogs 组件，负责当前界面片段的展示与交互。
 */
import { computed } from 'vue'  // 行注：引入 computed 能力
import type { ConfirmType } from '../../hooks/useConfirmDialog'  // 行注：引入 type { ConfirmType } 模块
import type { FriendItem, GroupDetail, GroupMember } from '../../types/chat'  // 行注：引入 type { FriendItem, GroupDetail, GroupMember } 模块
import ConfirmDialog from '../ConfirmDialog.vue'  // 行注：引入 ConfirmDialog 组件
import GroupNoticeDialog from '../GroupNoticeDialog.vue'  // 行注：引入 GroupNoticeDialog 组件
import ChatDownloadDialog from './ChatDownloadDialog.vue'  // 行注：引入 ChatDownloadDialog 组件
import ChatMediaPreview from './ChatMediaPreview.vue'  // 行注：引入 ChatMediaPreview 组件
import AddMembersModal from './AddMembersModal.vue'  // 行注：引入 AddMembersModal 组件
import CreateGroupModal from './CreateGroupModal.vue'  // 行注：引入 CreateGroupModal 组件
import GroupDetailPanel from './GroupDetailPanel.vue'  // 行注：引入 GroupDetailPanel 组件
import MuteMemberModal from './MuteMemberModal.vue'  // 行注：引入 MuteMemberModal 组件
import TransferOwnerModal from './TransferOwnerModal.vue'  // 行注：引入 TransferOwnerModal 组件

interface CreateGroupFormState {  // 行注：开始当前逻辑块
  groupName: string  // 行注：设置 groupName 配置项
  notice: string  // 行注：设置 notice 配置项
  memberIds: Array<string | number>  // 行注：设置 memberIds 配置项
  avatarPreview: string  // 行注：设置 avatarPreview 配置项
}  // 行注：结束当前代码块

interface ConfirmDialogState {  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  title: string  // 行注：设置 title 配置项
  subtitle: string  // 行注：设置 subtitle 配置项
  description: string  // 行注：设置 description 配置项
  cancelText: string  // 行注：设置 cancelText 配置项
  confirmText: string  // 行注：设置 confirmText 配置项
  confirmType: ConfirmType  // 行注：设置 confirmType 配置项
}  // 行注：结束当前代码块

const props = defineProps<{  // 行注：开始解构当前返回值
  showImagePreview: boolean  // 行注：设置 showImagePreview 配置项
  previewImageUrl: string  // 行注：设置 previewImageUrl 配置项
  showDownloadModal: boolean  // 行注：设置 showDownloadModal 配置项
  downloadFileName: string  // 行注：设置 downloadFileName 配置项
  downloadFileSize: string  // 行注：设置 downloadFileSize 配置项
  downloadProgress: number  // 行注：设置 downloadProgress 配置项
  showCreateGroupModal: boolean  // 行注：设置 showCreateGroupModal 配置项
  createGroupForm: CreateGroupFormState  // 行注：设置 createGroupForm 配置项
  friends: FriendItem[]  // 行注：设置 friends 配置项
  creatingGroup: boolean  // 行注：设置 creatingGroup 配置项
  showAddMembersModal: boolean  // 行注：设置 showAddMembersModal 配置项
  availableFriendsForCurrentGroup: FriendItem[]  // 行注：设置 availableFriendsForCurrentGroup 配置项
  addMembersSelection: Array<string | number>  // 行注：设置 addMembersSelection 配置项
  addMembersMessage: string  // 行注：设置 addMembersMessage 配置项
  addingMembers: boolean  // 行注：设置 addingMembers 配置项
  showTransferOwnerModal: boolean  // 行注：设置 showTransferOwnerModal 配置项
  transferOwnerSelection: string | number | null  // 行注：设置 transferOwnerSelection 配置项
  transferringOwner: boolean  // 行注：设置 transferringOwner 配置项
  showMuteMemberModal: boolean  // 行注：设置 showMuteMemberModal 配置项
  muteTargetMember: GroupMember | null  // 行注：设置 muteTargetMember 配置项
  muteMinutesInput: string  // 行注：设置 muteMinutesInput 配置项
  mutingMember: boolean  // 行注：设置 mutingMember 配置项
  showGroupDrawer: boolean  // 行注：设置 showGroupDrawer 配置项
  groupDetail: GroupDetail | null  // 行注：设置 groupDetail 配置项
  groupProfileName: string  // 行注：设置 groupProfileName 配置项
  groupProfileAvatarPreview: string  // 行注：设置 groupProfileAvatarPreview 配置项
  noticeDraft: string  // 行注：设置 noticeDraft 配置项
  currentUserId: string  // 行注：设置 currentUserId 配置项
  canEditGroupProfile: boolean  // 行注：设置 canEditGroupProfile 配置项
  updatingGroupProfile: boolean  // 行注：设置 updatingGroupProfile 配置项
  isGroupProfileChanged: boolean  // 行注：设置 isGroupProfileChanged 配置项
  canEditNotice: boolean  // 行注：设置 canEditNotice 配置项
  updatingNotice: boolean  // 行注：设置 updatingNotice 配置项
  isGroupNoticeChanged: boolean  // 行注：设置 isGroupNoticeChanged 配置项
  canManageMembers: boolean  // 行注：设置 canManageMembers 配置项
  canDissolveGroup: boolean  // 行注：设置 canDissolveGroup 配置项
  isGroupOwner: boolean  // 行注：设置 isGroupOwner 配置项
  canOperateMember: (member: GroupMember) => boolean  // 行注：设置 canOperateMember 配置项
  canToggleAdmin: (member: GroupMember) => boolean  // 行注：设置 canToggleAdmin 配置项
  confirmDialog: ConfirmDialogState  // 行注：设置 confirmDialog 配置项
  showNoticeReminder: boolean  // 行注：设置 showNoticeReminder 配置项
  groupNoticeUpdateTimeText: string  // 行注：设置 groupNoticeUpdateTimeText 配置项
  acknowledgingNoticeReminder: boolean  // 行注：设置 acknowledgingNoticeReminder 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{  // 行注：开始解构当前返回值
  (event: 'close-image-preview'): void  // 行注：执行当前调用逻辑
  (event: 'close-download-dialog'): void  // 行注：执行当前调用逻辑
  (event: 'open-downloaded-file'): void  // 行注：执行当前调用逻辑
  (event: 'save-downloaded-file'): void  // 行注：执行当前调用逻辑
  (event: 'close-create-group-modal'): void  // 行注：执行当前调用逻辑
  (event: 'trigger-group-avatar-upload'): void  // 行注：执行当前调用逻辑
  (event: 'update:create-group-name', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:create-group-notice', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:create-group-member-ids', value: Array<string | number>): void  // 行注：执行当前调用逻辑
  (event: 'submit-create-group'): void  // 行注：执行当前调用逻辑
  (event: 'close-add-members-modal'): void  // 行注：执行当前调用逻辑
  (event: 'update:add-members-selection', value: Array<string | number>): void  // 行注：执行当前调用逻辑
  (event: 'update:add-members-message', value: string): void  // 行注：执行当前调用逻辑
  (event: 'submit-add-members'): void  // 行注：执行当前调用逻辑
  (event: 'close-transfer-owner-modal'): void  // 行注：执行当前调用逻辑
  (event: 'update:transfer-owner-selection', value: string | number | null): void  // 行注：执行当前调用逻辑
  (event: 'submit-transfer-owner'): void  // 行注：执行当前调用逻辑
  (event: 'close-mute-member-modal'): void  // 行注：执行当前调用逻辑
  (event: 'update:mute-minutes-input', value: string): void  // 行注：执行当前调用逻辑
  (event: 'submit-mute-member'): void  // 行注：执行当前调用逻辑
  (event: 'close-group-drawer'): void  // 行注：执行当前调用逻辑
  (event: 'trigger-group-profile-avatar-upload'): void  // 行注：执行当前调用逻辑
  (event: 'update:group-profile-name', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:notice-draft', value: string): void  // 行注：执行当前调用逻辑
  (event: 'save-group-profile'): void  // 行注：执行当前调用逻辑
  (event: 'copy-group-id', value: string | number): void  // 行注：执行当前调用逻辑
  (event: 'save-notice'): void  // 行注：执行当前调用逻辑
  (event: 'open-group-members-page'): void  // 行注：执行当前调用逻辑
  (event: 'open-add-members-modal'): void  // 行注：执行当前调用逻辑
  (event: 'toggle-admin-role', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'toggle-mute-member', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'remove-member', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'dissolve-group'): void  // 行注：执行当前调用逻辑
  (event: 'open-transfer-owner-modal'): void  // 行注：执行当前调用逻辑
  (event: 'leave-group'): void  // 行注：执行当前调用逻辑
  (event: 'update:confirm-dialog-visible', value: boolean): void  // 行注：执行当前调用逻辑
  (event: 'cancel-confirm-dialog'): void  // 行注：执行当前调用逻辑
  (event: 'confirm-confirm-dialog'): void  // 行注：执行当前调用逻辑
  (event: 'update:show-notice-reminder', value: boolean): void  // 行注：执行当前调用逻辑
  (event: 'acknowledge-notice-reminder'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑

const confirmDialogVisibleProxy = computed({  // 行注：开始解构当前返回值
  get: () => props.confirmDialog.visible,  // 行注：传入 get 回调
  set: value => emit('update:confirm-dialog-visible', value)  // 行注：设置 set 配置项
})  // 行注：结束当前调用配置

const noticeReminderVisibleProxy = computed({  // 行注：开始解构当前返回值
  get: () => props.showNoticeReminder,  // 行注：传入 get 回调
  set: value => emit('update:show-notice-reminder', value)  // 行注：设置 set 配置项
})  // 行注：结束当前调用配置
</script>
