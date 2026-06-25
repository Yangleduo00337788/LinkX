<!-- 行注：开始定义模板区域 -->
<template>
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
    :members="transferableMembers"
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
    :muting-member="actionLoading"
    @close="$emit('close-mute-member-modal')"
    @update:mute-minutes-input="$emit('update:mute-minutes-input', $event)"
    @submit="$emit('submit-mute-member')"
  />
  <!-- 行注：渲染 ConfirmDialog 组件 -->
  <ConfirmDialog
    :visible="confirmDialog.visible"
    :title="confirmDialog.title"
    :subtitle="confirmDialog.subtitle"
    :description="confirmDialog.description"
    :cancel-text="confirmDialog.cancelText"
    :confirm-text="confirmDialog.confirmText"
    :confirm-type="confirmDialog.confirmType"
    @update:visible="$emit('update:confirm-dialog-visible', $event)"
    @cancel="$emit('cancel-confirm-dialog')"
    @confirm="$emit('confirm-confirm-dialog')"
  />
  <!-- 行注：渲染 GroupNoticeDialog 组件 -->
  <GroupNoticeDialog
    :visible="showNoticeReminder"
    :group-name="groupName"
    :notice="groupNotice"
    :update-time-text="groupNoticeUpdateTimeText"
    :loading="acknowledgingNoticeReminder"
    @update:visible="$emit('update:show-notice-reminder', $event)"
    @acknowledge="$emit('acknowledge-notice-reminder')"
  />
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupMembersDialogs 组件，负责当前界面片段的展示与交互。
 */
import type { ConfirmType } from '../../hooks/useConfirmDialog'  // 行注：引入 type { ConfirmType } 模块
import type { FriendItem, GroupMember } from '../../types/chat'  // 行注：引入 type { FriendItem, GroupMember } 模块
import ConfirmDialog from '../ConfirmDialog.vue'  // 行注：引入 ConfirmDialog 组件
import GroupNoticeDialog from '../GroupNoticeDialog.vue'  // 行注：引入 GroupNoticeDialog 组件
import AddMembersModal from '../chat/AddMembersModal.vue'  // 行注：引入 AddMembersModal 组件
import MuteMemberModal from '../chat/MuteMemberModal.vue'  // 行注：引入 MuteMemberModal 组件
import TransferOwnerModal from '../chat/TransferOwnerModal.vue'  // 行注：引入 TransferOwnerModal 组件

interface ConfirmDialogState {  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  title: string  // 行注：设置 title 配置项
  subtitle: string  // 行注：设置 subtitle 配置项
  description: string  // 行注：设置 description 配置项
  cancelText: string  // 行注：设置 cancelText 配置项
  confirmText: string  // 行注：设置 confirmText 配置项
  confirmType: ConfirmType  // 行注：设置 confirmType 配置项
}  // 行注：结束当前代码块

defineProps<{  // 行注：开始当前逻辑块
  showAddMembersModal: boolean  // 行注：设置 showAddMembersModal 配置项
  availableFriendsForCurrentGroup: FriendItem[]  // 行注：设置 availableFriendsForCurrentGroup 配置项
  addMembersSelection: Array<string | number>  // 行注：设置 addMembersSelection 配置项
  addMembersMessage: string  // 行注：设置 addMembersMessage 配置项
  addingMembers: boolean  // 行注：设置 addingMembers 配置项
  showTransferOwnerModal: boolean  // 行注：设置 showTransferOwnerModal 配置项
  transferableMembers: GroupMember[]  // 行注：设置 transferableMembers 配置项
  transferOwnerSelection: string | number | null  // 行注：设置 transferOwnerSelection 配置项
  transferringOwner: boolean  // 行注：设置 transferringOwner 配置项
  showMuteMemberModal: boolean  // 行注：设置 showMuteMemberModal 配置项
  muteTargetMember: GroupMember | null  // 行注：设置 muteTargetMember 配置项
  muteMinutesInput: string  // 行注：设置 muteMinutesInput 配置项
  actionLoading: boolean  // 行注：设置 actionLoading 配置项
  confirmDialog: ConfirmDialogState  // 行注：设置 confirmDialog 配置项
  showNoticeReminder: boolean  // 行注：设置 showNoticeReminder 配置项
  groupName?: string  // 行注：补充当前表达式
  groupNotice?: string  // 行注：补充当前表达式
  groupNoticeUpdateTimeText: string  // 行注：设置 groupNoticeUpdateTimeText 配置项
  acknowledgingNoticeReminder: boolean  // 行注：设置 acknowledgingNoticeReminder 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
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
  (event: 'update:confirm-dialog-visible', value: boolean): void  // 行注：执行当前调用逻辑
  (event: 'cancel-confirm-dialog'): void  // 行注：执行当前调用逻辑
  (event: 'confirm-confirm-dialog'): void  // 行注：执行当前调用逻辑
  (event: 'update:show-notice-reminder', value: boolean): void  // 行注：执行当前调用逻辑
  (event: 'acknowledge-notice-reminder'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
