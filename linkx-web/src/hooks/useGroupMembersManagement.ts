import { ref, type ComputedRef, type Ref } from 'vue'
import type { Router } from 'vue-router'
import { fileApi, groupApi } from '../api/client'
import type { ConfirmDialogOptions } from './useConfirmDialog'
import {
  GROUP_ROLE_ADMIN,
  GROUP_ROLE_MEMBER,
  GROUP_ROLE_OWNER,
  type GroupDetail,
  type GroupMember
} from '../types/chat'
import {
  resetGroupProfileDraftState,
  syncGroupProfileDraftState,
  type GroupProfileDraftState
} from '../utils/group-draft'

interface FeedbackApi {
  success: (content: string) => void
  warning: (content: string) => void
  error: (content: string) => void
}

interface UseGroupMembersManagementOptions {
  groupId: ComputedRef<string>
  groupDetail: Ref<GroupDetail | null>
  currentGroupRole: ComputedRef<number>
  canManageMembers: ComputedRef<boolean>
  canEditGroupProfile: ComputedRef<boolean>
  canEditNotice: ComputedRef<boolean>
  isGroupOwner: ComputedRef<boolean>
  groupProfileDraft: GroupProfileDraftState
  noticeDraft: Ref<string>
  groupAvatarInputRef: Ref<HTMLInputElement | undefined>
  getCurrentUserId: () => string | number
  message: FeedbackApi
  router: Router
  openConfirmDialog: (options: ConfirmDialogOptions) => Promise<boolean>
  loadGroupDetail: () => Promise<boolean>
  applyGroupDetail: (detail: GroupDetail | null, syncDraft?: boolean) => void
}

export function useGroupMembersManagement(options: UseGroupMembersManagementOptions) {
  const actionLoading = ref(false)
  const updatingGroupProfile = ref(false)
  const updatingNotice = ref(false)
  const showAddMembersModal = ref(false)
  const addMembersSelection = ref<Array<string | number>>([])
  const addMembersMessage = ref('')
  const addingMembers = ref(false)
  const showTransferOwnerModal = ref(false)
  const transferOwnerSelection = ref<string | number | null>(null)
  const transferringOwner = ref(false)
  const showMuteMemberModal = ref(false)
  const muteTargetMember = ref<GroupMember | null>(null)
  const muteMinutesInput = ref('10')

  function canOperateMember(member: GroupMember) {
    const myId = String(options.getCurrentUserId())
    if (!options.canManageMembers.value || String(member.userId) === myId) {
      return false
    }
    if (options.currentGroupRole.value === GROUP_ROLE_OWNER) {
      return member.role !== GROUP_ROLE_OWNER
    }
    return member.role === GROUP_ROLE_MEMBER
  }

  function canToggleAdmin(member: GroupMember) {
    return options.currentGroupRole.value === GROUP_ROLE_OWNER
      && String(member.userId) !== String(options.getCurrentUserId())
      && member.role !== GROUP_ROLE_OWNER
  }

  function resetGroupProfileDraft() {
    resetGroupProfileDraftState(options.groupProfileDraft)
    if (options.groupAvatarInputRef.value) {
      options.groupAvatarInputRef.value.value = ''
    }
  }

  function syncGroupProfileDraft(detail?: GroupDetail | null) {
    syncGroupProfileDraftState(options.groupProfileDraft, detail)
    if (options.groupAvatarInputRef.value) {
      options.groupAvatarInputRef.value.value = ''
    }
  }

  function discardGroupProfileDrafts() {
    options.noticeDraft.value = options.groupDetail.value?.notice || ''
    syncGroupProfileDraft(options.groupDetail.value)
  }

  function triggerGroupAvatarUpload() {
    if (!options.canEditGroupProfile.value) {
      return
    }
    options.groupAvatarInputRef.value?.click()
  }

  function handleGroupAvatarSelected(event: Event) {
    if (!options.canEditGroupProfile.value) {
      return
    }
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) {
      return
    }
    const previewUrl = URL.createObjectURL(file)
    if (options.groupProfileDraft.avatarPreview.startsWith('blob:')) {
      URL.revokeObjectURL(options.groupProfileDraft.avatarPreview)
    }
    options.groupProfileDraft.avatarFile = file
    options.groupProfileDraft.avatarPreview = previewUrl
  }

  async function submitUpdateNotice() {
    if (!options.groupId.value || !options.canEditNotice.value) {
      return
    }
    updatingNotice.value = true
    try {
      await groupApi.updateNotice(options.groupId.value, options.noticeDraft.value.trim())
      await options.loadGroupDetail()
      options.message.success('群公告已更新')
    } catch (error: any) {
      console.error('submitUpdateNotice error:', error)
      options.message.error(error.response?.data?.message || '更新群公告失败')
    } finally {
      updatingNotice.value = false
    }
  }

  async function submitUpdateGroupProfile() {
    if (!options.groupId.value || !options.groupDetail.value || !options.canEditGroupProfile.value) {
      return
    }
    const nextGroupName = options.groupProfileDraft.groupName.trim()
    if (!nextGroupName) {
      options.message.warning('请输入群名称')
      return
    }
    updatingGroupProfile.value = true
    try {
      let groupAvatar = options.groupDetail.value.groupAvatar || ''
      if (options.groupProfileDraft.avatarFile) {
        const uploadResponse: any = await fileApi.uploadAvatar(options.groupProfileDraft.avatarFile)
        groupAvatar = uploadResponse.data?.fileUrl || ''
      }
      await groupApi.updateProfile(options.groupId.value, {
        groupName: nextGroupName,
        groupAvatar
      })
      if (options.groupDetail.value) {
        options.applyGroupDetail({
          ...options.groupDetail.value,
          groupName: nextGroupName,
          groupAvatar
        }, true)
      }
      options.message.success('群资料已更新')
    } catch (error: any) {
      console.error('submitUpdateGroupProfile error:', error)
      options.message.error(error.response?.data?.message || '更新群资料失败')
    } finally {
      updatingGroupProfile.value = false
    }
  }

  function openAddMembersModal() {
    if (!options.canManageMembers.value) {
      return
    }
    addMembersSelection.value = []
    addMembersMessage.value = ''
    showAddMembersModal.value = true
  }

  function closeAddMembersModal() {
    if (addingMembers.value) {
      return
    }
    showAddMembersModal.value = false
    addMembersSelection.value = []
    addMembersMessage.value = ''
  }

  function resetAddMembersModal() {
    showAddMembersModal.value = false
    addMembersSelection.value = []
    addMembersMessage.value = ''
  }

  async function submitAddMembers() {
    if (!options.groupId.value || addMembersSelection.value.length === 0) {
      return
    }
    addingMembers.value = true
    try {
      await groupApi.inviteMembers(options.groupId.value, addMembersSelection.value, addMembersMessage.value.trim())
      resetAddMembersModal()
      options.message.success('邀请已发送')
    } catch (error: any) {
      console.error('submitAddMembers error:', error)
      options.message.error(error.response?.data?.message || '邀请成员失败')
    } finally {
      addingMembers.value = false
    }
  }

  function openTransferOwnerModal() {
    if (!options.isGroupOwner.value) {
      return
    }
    transferOwnerSelection.value = null
    showTransferOwnerModal.value = true
  }

  function closeTransferOwnerModal() {
    if (transferringOwner.value) {
      return
    }
    showTransferOwnerModal.value = false
    transferOwnerSelection.value = null
  }

  function resetTransferOwnerModal() {
    showTransferOwnerModal.value = false
    transferOwnerSelection.value = null
  }

  async function submitTransferOwner() {
    if (!options.groupId.value || !transferOwnerSelection.value) {
      return
    }
    const member = options.groupDetail.value?.members.find(item => String(item.userId) === String(transferOwnerSelection.value))
    const memberName = member?.nickname || member?.username || '该成员'
    const confirmed = await options.openConfirmDialog({
      title: '确认转让群主',
      subtitle: memberName,
      description: `确认将群主转让给 ${memberName} 吗？转让后你将变为普通成员。`,
      confirmText: '确认转让',
      confirmType: 'danger'
    })
    if (!confirmed) {
      return
    }
    transferringOwner.value = true
    try {
      await groupApi.transferOwner(options.groupId.value, transferOwnerSelection.value)
      resetTransferOwnerModal()
      await options.loadGroupDetail()
      options.message.success('群主已转让')
    } catch (error: any) {
      console.error('submitTransferOwner error:', error)
      options.message.error(error.response?.data?.message || '转让群主失败')
    } finally {
      transferringOwner.value = false
    }
  }

  async function toggleAdminRole(memberItem: GroupMember) {
    if (!options.groupDetail.value || !canToggleAdmin(memberItem)) {
      return
    }
    const nextActionText = memberItem.role === GROUP_ROLE_ADMIN ? '取消该成员的管理员身份' : '将该成员设为管理员'
    const confirmed = await options.openConfirmDialog({
      title: '确认成员权限变更',
      subtitle: nextActionText,
      description: `确认${nextActionText}吗？`,
      confirmText: '确认操作'
    })
    if (!confirmed) {
      return
    }
    actionLoading.value = true
    try {
      if (memberItem.role === GROUP_ROLE_ADMIN) {
        await groupApi.removeAdmin(options.groupDetail.value.id, memberItem.userId)
        options.message.success('已取消管理员')
      } else {
        await groupApi.setAdmin(options.groupDetail.value.id, memberItem.userId)
        options.message.success('已设为管理员')
      }
      await options.loadGroupDetail()
    } catch (error: any) {
      console.error('toggleAdminRole error:', error)
      options.message.error(error.response?.data?.message || '管理员操作失败')
    } finally {
      actionLoading.value = false
    }
  }

  function openMuteMemberModal(memberItem: GroupMember) {
    muteTargetMember.value = memberItem
    muteMinutesInput.value = '10'
    showMuteMemberModal.value = true
  }

  function closeMuteMemberModal() {
    if (actionLoading.value) {
      return
    }
    muteTargetMember.value = null
    muteMinutesInput.value = '10'
    showMuteMemberModal.value = false
  }

  async function toggleMuteMember(memberItem: GroupMember) {
    if (!options.groupDetail.value) {
      return
    }
    if (memberItem.muted) {
      actionLoading.value = true
      try {
        await groupApi.unmuteMember(options.groupDetail.value.id, memberItem.userId)
        options.message.success('已解除禁言')
        await options.loadGroupDetail()
      } catch (error: any) {
        console.error('toggleMuteMember error:', error)
        options.message.error(error.response?.data?.message || '操作失败')
      } finally {
        actionLoading.value = false
      }
      return
    }
    openMuteMemberModal(memberItem)
  }

  async function submitMuteMember() {
    if (!options.groupDetail.value || !muteTargetMember.value) {
      return
    }
    const muteMinutes = Number(muteMinutesInput.value)
    if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {
      options.message.warning('请输入 1 到 43200 之间的整数分钟数')
      return
    }
    actionLoading.value = true
    try {
      await groupApi.muteMember(options.groupDetail.value.id, muteTargetMember.value.userId, muteMinutes)
      options.message.success('已设置禁言')
      closeMuteMemberModal()
      await options.loadGroupDetail()
    } catch (error: any) {
      console.error('submitMuteMember error:', error)
      options.message.error(error.response?.data?.message || '设置禁言失败')
    } finally {
      actionLoading.value = false
    }
  }

  async function handleRemoveMember(memberItem: GroupMember) {
    if (!options.groupDetail.value) {
      return
    }
    const confirmed = await options.openConfirmDialog({
      title: '确认移出成员',
      subtitle: memberItem.nickname || memberItem.username || '该成员',
      description: `确认将 ${memberItem.nickname || memberItem.username} 移出群聊吗？`,
      confirmText: '确认移出',
      confirmType: 'danger'
    })
    if (!confirmed) {
      return
    }
    actionLoading.value = true
    try {
      await groupApi.removeMember(options.groupDetail.value.id, memberItem.userId)
      options.message.success('成员已移出群聊')
      await options.loadGroupDetail()
    } catch (error: any) {
      console.error('handleRemoveMember error:', error)
      options.message.error(error.response?.data?.message || '移除成员失败')
    } finally {
      actionLoading.value = false
    }
  }

  async function handleDissolveGroup() {
    if (!options.groupId.value) {
      return
    }
    const confirmed = await options.openConfirmDialog({
      title: '确认解散群聊',
      subtitle: '该操作不可恢复',
      description: '解散后，当前群聊与会话记录入口将被移除。',
      confirmText: '确认解散',
      confirmType: 'danger'
    })
    if (!confirmed) {
      return
    }
    try {
      await groupApi.dissolve(options.groupId.value)
      options.message.success('群聊已解散')
      await options.router.replace('/chat')
    } catch (error: any) {
      console.error('handleDissolveGroup error:', error)
      options.message.error(error.response?.data?.message || '解散群聊失败')
    }
  }

  async function handleLeaveGroup() {
    if (!options.groupId.value) {
      return
    }
    const confirmed = await options.openConfirmDialog({
      title: '确认退出群聊',
      subtitle: options.groupDetail.value?.groupName || '当前群聊',
      description: '退出后你将不再接收该群消息，需要重新被邀请才能再次加入。',
      confirmText: '确认退出',
      confirmType: 'danger'
    })
    if (!confirmed) {
      return
    }
    try {
      await groupApi.leaveGroup(options.groupId.value)
      options.message.success('已退出群聊')
      await options.router.replace('/chat')
    } catch (error: any) {
      console.error('handleLeaveGroup error:', error)
      options.message.error(error.response?.data?.message || '退出群聊失败')
    }
  }

  return {
    actionLoading,
    updatingGroupProfile,
    updatingNotice,
    showAddMembersModal,
    addMembersSelection,
    addMembersMessage,
    addingMembers,
    showTransferOwnerModal,
    transferOwnerSelection,
    transferringOwner,
    showMuteMemberModal,
    muteTargetMember,
    muteMinutesInput,
    canOperateMember,
    canToggleAdmin,
    resetGroupProfileDraft,
    syncGroupProfileDraft,
    discardGroupProfileDrafts,
    triggerGroupAvatarUpload,
    handleGroupAvatarSelected,
    submitUpdateNotice,
    submitUpdateGroupProfile,
    openAddMembersModal,
    closeAddMembersModal,
    submitAddMembers,
    openTransferOwnerModal,
    closeTransferOwnerModal,
    submitTransferOwner,
    toggleAdminRole,
    toggleMuteMember,
    closeMuteMemberModal,
    submitMuteMember,
    handleRemoveMember,
    handleDissolveGroup,
    handleLeaveGroup
  }
}
