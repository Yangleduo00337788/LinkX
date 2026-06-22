<template>
  <ChatMediaPreview
    :visible="showImagePreview"
    :image-url="previewImageUrl"
    @close="$emit('close-image-preview')"
  />

  <ChatDownloadDialog
    :visible="showDownloadModal"
    :file-name="downloadFileName"
    :file-size="downloadFileSize"
    :progress="downloadProgress"
    @close="$emit('close-download-dialog')"
    @open="$emit('open-downloaded-file')"
    @save="$emit('save-downloaded-file')"
  />

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
    :members="groupDetail?.members || []"
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
    :muting-member="mutingMember"
    @close="$emit('close-mute-member-modal')"
    @update:mute-minutes-input="$emit('update:mute-minutes-input', $event)"
    @submit="$emit('submit-mute-member')"
  />

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

  <GroupNoticeDialog
    v-model:visible="noticeReminderVisibleProxy"
    :group-name="groupDetail?.groupName"
    :notice="groupDetail?.notice"
    :update-time-text="groupNoticeUpdateTimeText"
    :loading="acknowledgingNoticeReminder"
    @acknowledge="$emit('acknowledge-notice-reminder')"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ConfirmType } from '../../hooks/useConfirmDialog'
import type { FriendItem, GroupDetail, GroupMember } from '../../types/chat'
import ConfirmDialog from '../ConfirmDialog.vue'
import GroupNoticeDialog from '../GroupNoticeDialog.vue'
import ChatDownloadDialog from './ChatDownloadDialog.vue'
import ChatMediaPreview from './ChatMediaPreview.vue'
import AddMembersModal from './AddMembersModal.vue'
import CreateGroupModal from './CreateGroupModal.vue'
import GroupDetailPanel from './GroupDetailPanel.vue'
import MuteMemberModal from './MuteMemberModal.vue'
import TransferOwnerModal from './TransferOwnerModal.vue'

interface CreateGroupFormState {
  groupName: string
  notice: string
  memberIds: Array<string | number>
  avatarPreview: string
}

interface ConfirmDialogState {
  visible: boolean
  title: string
  subtitle: string
  description: string
  cancelText: string
  confirmText: string
  confirmType: ConfirmType
}

const props = defineProps<{
  showImagePreview: boolean
  previewImageUrl: string
  showDownloadModal: boolean
  downloadFileName: string
  downloadFileSize: string
  downloadProgress: number
  showCreateGroupModal: boolean
  createGroupForm: CreateGroupFormState
  friends: FriendItem[]
  creatingGroup: boolean
  showAddMembersModal: boolean
  availableFriendsForCurrentGroup: FriendItem[]
  addMembersSelection: Array<string | number>
  addMembersMessage: string
  addingMembers: boolean
  showTransferOwnerModal: boolean
  transferOwnerSelection: string | number | null
  transferringOwner: boolean
  showMuteMemberModal: boolean
  muteTargetMember: GroupMember | null
  muteMinutesInput: string
  mutingMember: boolean
  showGroupDrawer: boolean
  groupDetail: GroupDetail | null
  groupProfileName: string
  groupProfileAvatarPreview: string
  noticeDraft: string
  currentUserId: string
  canEditGroupProfile: boolean
  updatingGroupProfile: boolean
  isGroupProfileChanged: boolean
  canEditNotice: boolean
  updatingNotice: boolean
  isGroupNoticeChanged: boolean
  canManageMembers: boolean
  canDissolveGroup: boolean
  isGroupOwner: boolean
  canOperateMember: (member: GroupMember) => boolean
  canToggleAdmin: (member: GroupMember) => boolean
  confirmDialog: ConfirmDialogState
  showNoticeReminder: boolean
  groupNoticeUpdateTimeText: string
  acknowledgingNoticeReminder: boolean
}>()

const emit = defineEmits<{
  (event: 'close-image-preview'): void
  (event: 'close-download-dialog'): void
  (event: 'open-downloaded-file'): void
  (event: 'save-downloaded-file'): void
  (event: 'close-create-group-modal'): void
  (event: 'trigger-group-avatar-upload'): void
  (event: 'update:create-group-name', value: string): void
  (event: 'update:create-group-notice', value: string): void
  (event: 'update:create-group-member-ids', value: Array<string | number>): void
  (event: 'submit-create-group'): void
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
  (event: 'close-group-drawer'): void
  (event: 'trigger-group-profile-avatar-upload'): void
  (event: 'update:group-profile-name', value: string): void
  (event: 'update:notice-draft', value: string): void
  (event: 'save-group-profile'): void
  (event: 'copy-group-id', value: string | number): void
  (event: 'save-notice'): void
  (event: 'open-group-members-page'): void
  (event: 'open-add-members-modal'): void
  (event: 'toggle-admin-role', member: GroupMember): void
  (event: 'toggle-mute-member', member: GroupMember): void
  (event: 'remove-member', member: GroupMember): void
  (event: 'dissolve-group'): void
  (event: 'open-transfer-owner-modal'): void
  (event: 'leave-group'): void
  (event: 'update:confirm-dialog-visible', value: boolean): void
  (event: 'cancel-confirm-dialog'): void
  (event: 'confirm-confirm-dialog'): void
  (event: 'update:show-notice-reminder', value: boolean): void
  (event: 'acknowledge-notice-reminder'): void
}>()

const confirmDialogVisibleProxy = computed({
  get: () => props.confirmDialog.visible,
  set: value => emit('update:confirm-dialog-visible', value)
})

const noticeReminderVisibleProxy = computed({
  get: () => props.showNoticeReminder,
  set: value => emit('update:show-notice-reminder', value)
})
</script>
