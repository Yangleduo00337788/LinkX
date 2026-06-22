<template>
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

  <TransferOwnerModal
    :visible="showTransferOwnerModal"
    :members="transferableMembers"
    :selected-owner-id="transferOwnerSelection"
    :transferring-owner="transferringOwner"
    @close="$emit('close-transfer-owner-modal')"
    @update:selected-owner-id="$emit('update:transfer-owner-selection', $event)"
    @submit="$emit('submit-transfer-owner')"
  />

  <MuteMemberModal
    :visible="showMuteMemberModal"
    :member="muteTargetMember"
    :mute-minutes-input="muteMinutesInput"
    :muting-member="actionLoading"
    @close="$emit('close-mute-member-modal')"
    @update:mute-minutes-input="$emit('update:mute-minutes-input', $event)"
    @submit="$emit('submit-mute-member')"
  />

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

  <GroupNoticeDialog
    :visible="showNoticeReminder"
    :group-name="groupName"
    :notice="groupNotice"
    :update-time-text="groupNoticeUpdateTimeText"
    :loading="acknowledgingNoticeReminder"
    @update:visible="$emit('update:show-notice-reminder', $event)"
    @acknowledge="$emit('acknowledge-notice-reminder')"
  />
</template>

<script setup lang="ts">
import type { ConfirmType } from '../../hooks/useConfirmDialog'
import type { FriendItem, GroupMember } from '../../types/chat'
import ConfirmDialog from '../ConfirmDialog.vue'
import GroupNoticeDialog from '../GroupNoticeDialog.vue'
import AddMembersModal from '../chat/AddMembersModal.vue'
import MuteMemberModal from '../chat/MuteMemberModal.vue'
import TransferOwnerModal from '../chat/TransferOwnerModal.vue'

interface ConfirmDialogState {
  visible: boolean
  title: string
  subtitle: string
  description: string
  cancelText: string
  confirmText: string
  confirmType: ConfirmType
}

defineProps<{
  showAddMembersModal: boolean
  availableFriendsForCurrentGroup: FriendItem[]
  addMembersSelection: Array<string | number>
  addMembersMessage: string
  addingMembers: boolean
  showTransferOwnerModal: boolean
  transferableMembers: GroupMember[]
  transferOwnerSelection: string | number | null
  transferringOwner: boolean
  showMuteMemberModal: boolean
  muteTargetMember: GroupMember | null
  muteMinutesInput: string
  actionLoading: boolean
  confirmDialog: ConfirmDialogState
  showNoticeReminder: boolean
  groupName?: string
  groupNotice?: string
  groupNoticeUpdateTimeText: string
  acknowledgingNoticeReminder: boolean
}>()

defineEmits<{
  (event: 'close-add-members-modal'): void
  (event: 'update:add-members-selection', value: Array<string | number>): void
  (event: 'update:add-members-message', value: string): void
  (event: 'submit-add-members'): void
  (event: 'close-transfer-owner-modal'): void
  (event: 'update:transfer-owner-selection', value: string | number | null): void
  (event: 'submit-transfer-owner'): void
  (event: 'close-mute-member-modal'): void
  (event: 'update:mute-minutes-input', value: string): void
  (event: 'submit-mute-member'): void
  (event: 'update:confirm-dialog-visible', value: boolean): void
  (event: 'cancel-confirm-dialog'): void
  (event: 'confirm-confirm-dialog'): void
  (event: 'update:show-notice-reminder', value: boolean): void
  (event: 'acknowledge-notice-reminder'): void
}>()
</script>
