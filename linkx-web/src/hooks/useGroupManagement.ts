import { computed, reactive, ref, type ComputedRef, type Ref } from 'vue'
import type { Router } from 'vue-router'
import { fileApi, groupApi } from '../api/client'
import type { ConfirmDialogOptions } from './useConfirmDialog'
import {
  GROUP_ROLE_ADMIN,
  GROUP_ROLE_MEMBER,
  GROUP_ROLE_OWNER,
  SESSION_TYPE_GROUP,
  type ChatSession,
  type FriendItem,
  type GroupDetail,
  type GroupMember
} from '../types/chat'
import {
  isGroupNoticeDraftChanged,
  isGroupProfileDraftChanged,
  resetGroupProfileDraftState,
  revokeBlobUrl,
  syncGroupProfileDraftState
} from '../utils/group-draft'
import { buildSessionKey } from '../utils/chat'

interface FeedbackApi {
  success: (content: string) => void
  warning: (content: string) => void
  error: (content: string) => void
}

interface GroupProfileDraft {
  groupName: string
  avatarPreview: string
  avatarFile: File | null
}

interface UseGroupManagementOptions {
  friends: Ref<FriendItem[]>
  sessions: Ref<ChatSession[]>
  currentTargetId: Ref<string | null>
  currentGroupRole: ComputedRef<number>
  currentSessionName: ComputedRef<string>
  isGroupSession: ComputedRef<boolean>
  canManageMembers: ComputedRef<boolean>
  canEditGroupProfile: ComputedRef<boolean>
  canEditNotice: ComputedRef<boolean>
  groupDetail: Ref<GroupDetail | null>
  showGroupDrawer: Ref<boolean>
  noticeDraft: Ref<string>
  groupProfileDraft: GroupProfileDraft
  groupAvatarInputRef: Ref<HTMLInputElement | undefined>
  getCurrentUserId: () => string | number
  message: FeedbackApi
  router: Router
  openConfirmDialog: (options: ConfirmDialogOptions) => Promise<boolean>
  loadSessions: () => Promise<void>
  loadGroupDetail: (groupId: string | number, syncDraft?: boolean) => Promise<boolean>
  selectSession: (session: ChatSession, syncRoute?: boolean) => Promise<void>
  upsertSession: (session: ChatSession) => ChatSession
  applyGroupDetail: (detail: GroupDetail | null, syncDraft?: boolean) => void
  removeSessionByTarget: (targetId: string | number, sessionType: number) => void
  resetCurrentConversationState: () => void
  showMenu: Ref<boolean>
}

export function useGroupManagement(options: UseGroupManagementOptions) {
  const showCreateGroupModal = ref(false)
  const showAddMembersModal = ref(false)
  const creatingGroup = ref(false)
  const addingMembers = ref(false)
  const updatingNotice = ref(false)
  const updatingGroupProfile = ref(false)
  const showTransferOwnerModal = ref(false)
  const transferOwnerSelection = ref<string | number | null>(null)
  const transferringOwner = ref(false)
  const showMuteMemberModal = ref(false)
  const muteTargetMember = ref<GroupMember | null>(null)
  const muteMinutesInput = ref('10')
  const mutingMember = ref(false)
  const addMembersSelection = ref<Array<string | number>>([])
  const addMembersMessage = ref('')

  const createGroupForm = reactive({
    groupName: '',
    notice: '',
    memberIds: [] as Array<string | number>,
    avatarPreview: '',
    avatarFile: null as File | null
  })

  const isGroupProfileChanged = computed(() => {
    return isGroupProfileDraftChanged(options.groupProfileDraft, options.groupDetail.value)
  })

  const isGroupNoticeChanged = computed(() => {
    return isGroupNoticeDraftChanged(options.noticeDraft.value, options.groupDetail.value)
  })

  const hasUnsavedGroupDrawerChanges = computed(() => {
    return isGroupProfileChanged.value || isGroupNoticeChanged.value
  })

  const availableFriendsForCurrentGroup = computed(() => {
    const memberIds = new Set((options.groupDetail.value?.members || []).map(member => String(member.userId)))
    return options.friends.value.filter(friend => !memberIds.has(String(friend.friendId)))
  })

  function resetCreateGroupForm() {
    revokeBlobUrl(createGroupForm.avatarPreview)
    createGroupForm.groupName = ''
    createGroupForm.notice = ''
    createGroupForm.memberIds = []
    createGroupForm.avatarPreview = ''
    createGroupForm.avatarFile = null
    if (options.groupAvatarInputRef.value) {
      options.groupAvatarInputRef.value.value = ''
    }
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

  function discardGroupDrawerDrafts() {
    options.noticeDraft.value = options.groupDetail.value?.notice || ''
    syncGroupProfileDraft(options.groupDetail.value)
  }

  function openCreateGroupModal() {
    resetCreateGroupForm()
    showCreateGroupModal.value = true
  }

  function closeCreateGroupModal() {
    showCreateGroupModal.value = false
    resetCreateGroupForm()
  }

  function triggerGroupAvatarUpload() {
    options.groupAvatarInputRef.value?.click()
  }

  function triggerGroupProfileAvatarUpload() {
    if (!options.canEditGroupProfile.value) {
      return
    }
    options.groupAvatarInputRef.value?.click()
  }

  function handleGroupAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) {
      return
    }
    const previewUrl = URL.createObjectURL(file)
    if (showCreateGroupModal.value) {
      revokeBlobUrl(createGroupForm.avatarPreview)
      createGroupForm.avatarFile = file
      createGroupForm.avatarPreview = previewUrl
      return
    }
    if (options.showGroupDrawer.value && options.canEditGroupProfile.value) {
      revokeBlobUrl(options.groupProfileDraft.avatarPreview)
      options.groupProfileDraft.avatarFile = file
      options.groupProfileDraft.avatarPreview = previewUrl
      return
    }
    revokeBlobUrl(previewUrl)
  }

  async function submitCreateGroup() {
    if (!createGroupForm.groupName.trim()) {
      options.message.warning('请输入群名称')
      return
    }
    if (createGroupForm.memberIds.length === 0) {
      options.message.warning('请至少选择一位好友')
      return
    }

    creatingGroup.value = true
    try {
      const requestedGroupName = createGroupForm.groupName.trim()
      const requestedNotice = createGroupForm.notice.trim()
      const requestedMemberCount = createGroupForm.memberIds.length + 1
      let groupAvatar = ''
      if (createGroupForm.avatarFile) {
        const uploadResponse: any = await fileApi.uploadAvatar(createGroupForm.avatarFile)
        groupAvatar = uploadResponse.data?.fileUrl || ''
      }

      const response: any = await groupApi.create({
        groupName: requestedGroupName,
        groupAvatar,
        notice: requestedNotice,
        memberIds: createGroupForm.memberIds.map(item => String(item))
      })

      const createdGroup = response.data || {}
      closeCreateGroupModal()
      await options.loadSessions()

      const groupId = createdGroup.id
      let groupSession = options.sessions.value.find(session => {
        return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(groupId, SESSION_TYPE_GROUP)
      })

      if (!groupSession && groupId) {
        groupSession = options.upsertSession({
          targetId: groupId,
          sessionType: SESSION_TYPE_GROUP,
          targetNickname: createdGroup.groupName || requestedGroupName,
          targetAvatar: createdGroup.groupAvatar || groupAvatar,
          lastMessage: '',
          lastMessageTime: createdGroup.createTime || '',
          unreadCount: 0,
          memberCount: Number(createdGroup.memberCount || requestedMemberCount),
          myRole: Number(createdGroup.myRole ?? GROUP_ROLE_OWNER),
          notice: createdGroup.notice || requestedNotice,
          noticeUnread: false,
          muted: Boolean(createdGroup.muted),
          muteTime: createdGroup.muteTime || '',
          isDraft: true
        })
      }

      if (groupSession) {
        await options.selectSession(groupSession)
      }
      options.message.success(`群聊创建成功，群号：${groupId}`)
    } catch (error: any) {
      console.error('submitCreateGroup error:', error)
      options.message.error(error.response?.data?.message || '创建群聊失败')
    } finally {
      creatingGroup.value = false
    }
  }

  async function openGroupDrawer() {
    options.showMenu.value = false
    if (!options.currentTargetId.value || !options.isGroupSession.value) {
      return
    }
    options.showGroupDrawer.value = true
    await options.loadGroupDetail(options.currentTargetId.value)
  }

  async function closeGroupDrawer(config: { force?: boolean } = {}) {
    if (!config.force && options.showGroupDrawer.value && hasUnsavedGroupDrawerChanges.value) {
      const confirmed = await options.openConfirmDialog({
        title: '放弃未保存修改？',
        subtitle: '群名称或群公告有未保存内容',
        description: '确认关闭后，将丢弃当前未保存的群资料与群公告修改。',
        cancelText: '继续编辑',
        confirmText: '确认关闭'
      })
      if (!confirmed) {
        return false
      }
    }
    discardGroupDrawerDrafts()
    options.showGroupDrawer.value = false
    return true
  }

  async function openGroupMembersPage() {
    const targetGroupId = options.groupDetail.value?.id || options.currentTargetId.value
    if (!targetGroupId || !options.isGroupSession.value) {
      return
    }
    const closed = await closeGroupDrawer()
    if (!closed) {
      return
    }
    await options.router.push({
      path: `/groups/${targetGroupId}/members`,
      query: { from: 'chat' }
    })
  }

  function openAddMembersModal() {
    addMembersSelection.value = []
    addMembersMessage.value = ''
    showAddMembersModal.value = true
  }

  function closeAddMembersModal() {
    showAddMembersModal.value = false
    addMembersSelection.value = []
    addMembersMessage.value = ''
  }

  async function submitAddMembers() {
    if (!options.currentTargetId.value || addMembersSelection.value.length === 0) {
      options.message.warning('请选择要邀请的成员')
      return
    }
    addingMembers.value = true
    try {
      await groupApi.inviteMembers(options.currentTargetId.value, addMembersSelection.value, addMembersMessage.value.trim())
      closeAddMembersModal()
      options.message.success('入群邀请已发送')
    } catch (error: any) {
      console.error('submitAddMembers error:', error)
      options.message.error(error.response?.data?.message || '邀请成员失败')
    } finally {
      addingMembers.value = false
    }
  }

  async function submitUpdateNotice() {
    if (!options.currentTargetId.value || !options.canEditNotice.value) {
      return
    }
    updatingNotice.value = true
    try {
      await groupApi.updateNotice(options.currentTargetId.value, options.noticeDraft.value.trim())
      await options.loadGroupDetail(options.currentTargetId.value)
      options.message.success('群公告已更新')
    } catch (error: any) {
      console.error('submitUpdateNotice error:', error)
      options.message.error(error.response?.data?.message || '更新群公告失败')
    } finally {
      updatingNotice.value = false
    }
  }

  async function submitUpdateGroupProfile() {
    if (!options.currentTargetId.value || !options.groupDetail.value || !options.canEditGroupProfile.value) {
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
      await groupApi.updateProfile(options.currentTargetId.value, {
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

  async function toggleAdminRole(memberItem: GroupMember) {
    if (!options.currentTargetId.value || !canToggleAdmin(memberItem)) {
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
    try {
      if (memberItem.role === GROUP_ROLE_ADMIN) {
        await groupApi.removeAdmin(options.currentTargetId.value, memberItem.userId)
        options.message.success('已取消管理员')
      } else {
        await groupApi.setAdmin(options.currentTargetId.value, memberItem.userId)
        options.message.success('已设为管理员')
      }
    } catch (error: any) {
      console.error('toggleAdminRole error:', error)
      options.message.error(error.response?.data?.message || '管理员操作失败')
    }
  }

  function openMuteMemberModal(memberItem: GroupMember) {
    muteTargetMember.value = memberItem
    muteMinutesInput.value = '10'
    showMuteMemberModal.value = true
  }

  function resetMuteMemberModal() {
    showMuteMemberModal.value = false
    muteTargetMember.value = null
    muteMinutesInput.value = '10'
  }

  function closeMuteMemberModal() {
    if (mutingMember.value) {
      return
    }
    resetMuteMemberModal()
  }

  async function toggleMuteMember(memberItem: GroupMember) {
    if (!options.currentTargetId.value) {
      return
    }
    try {
      if (memberItem.muted) {
        await groupApi.unmuteMember(options.currentTargetId.value, memberItem.userId)
        options.message.success('已解除禁言')
      } else {
        openMuteMemberModal(memberItem)
      }
    } catch (error: any) {
      console.error('toggleMuteMember error:', error)
      options.message.error(error.response?.data?.message || '操作失败')
    }
  }

  async function submitMuteMember() {
    if (!options.currentTargetId.value || !muteTargetMember.value) {
      return
    }
    const muteMinutes = Number(muteMinutesInput.value)
    if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {
      options.message.warning('请输入 1 到 43200 之间的整数分钟数')
      return
    }
    mutingMember.value = true
    try {
      await groupApi.muteMember(options.currentTargetId.value, muteTargetMember.value.userId, muteMinutes)
      resetMuteMemberModal()
      options.message.success('已设置禁言')
    } catch (error: any) {
      console.error('submitMuteMember error:', error)
      options.message.error(error.response?.data?.message || '设置禁言失败')
    } finally {
      mutingMember.value = false
    }
  }

  async function handleRemoveMember(memberItem: GroupMember) {
    if (!options.currentTargetId.value) {
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
    try {
      await groupApi.removeMember(options.currentTargetId.value, memberItem.userId)
      options.message.success('成员已移出群聊')
    } catch (error: any) {
      console.error('handleRemoveMember error:', error)
      options.message.error(error.response?.data?.message || '移除成员失败')
    }
  }

  async function handleDissolveGroup() {
    if (!options.currentTargetId.value) {
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
      await groupApi.dissolve(options.currentTargetId.value)
      await closeGroupDrawer({ force: true })
      options.removeSessionByTarget(options.currentTargetId.value, SESSION_TYPE_GROUP)
      options.resetCurrentConversationState()
      await options.router.replace('/chat')
      options.message.success('群聊已解散')
    } catch (error: any) {
      console.error('handleDissolveGroup error:', error)
      options.message.error(error.response?.data?.message || '解散群聊失败')
    }
  }

  async function handleLeaveGroup() {
    if (!options.currentTargetId.value) {
      return
    }
    const confirmed = await options.openConfirmDialog({
      title: '确认退出群聊',
      subtitle: options.currentSessionName.value || '当前群聊',
      description: '退出后你将不再接收该群消息，需要重新被邀请后才能再次加入。',
      confirmText: '确认退出',
      confirmType: 'danger'
    })
    if (!confirmed) {
      return
    }
    try {
      await groupApi.leaveGroup(options.currentTargetId.value)
      await closeGroupDrawer({ force: true })
      options.removeSessionByTarget(options.currentTargetId.value, SESSION_TYPE_GROUP)
      options.resetCurrentConversationState()
      await options.router.replace('/chat')
      options.message.success('已退出群聊')
    } catch (error: any) {
      console.error('handleLeaveGroup error:', error)
      options.message.error(error.response?.data?.message || '退出群聊失败')
    }
  }

  function openTransferOwnerModal() {
    transferOwnerSelection.value = null
    showTransferOwnerModal.value = true
  }

  function closeTransferOwnerModal() {
    showTransferOwnerModal.value = false
    transferOwnerSelection.value = null
  }

  async function submitTransferOwner() {
    if (!options.currentTargetId.value || !transferOwnerSelection.value) {
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
      await groupApi.transferOwner(options.currentTargetId.value, transferOwnerSelection.value)
      closeTransferOwnerModal()
      options.message.success('群主已转让')
    } catch (error: any) {
      console.error('submitTransferOwner error:', error)
      options.message.error(error.response?.data?.message || '转让群主失败')
    } finally {
      transferringOwner.value = false
    }
  }

  return {
    showCreateGroupModal,
    showAddMembersModal,
    creatingGroup,
    addMembersSelection,
    addMembersMessage,
    addingMembers,
    updatingNotice,
    updatingGroupProfile,
    showTransferOwnerModal,
    transferOwnerSelection,
    transferringOwner,
    showMuteMemberModal,
    muteTargetMember,
    muteMinutesInput,
    mutingMember,
    createGroupForm,
    isGroupProfileChanged,
    isGroupNoticeChanged,
    hasUnsavedGroupDrawerChanges,
    availableFriendsForCurrentGroup,
    resetCreateGroupForm,
    resetGroupProfileDraft,
    syncGroupProfileDraft,
    discardGroupDrawerDrafts,
    openCreateGroupModal,
    closeCreateGroupModal,
    triggerGroupAvatarUpload,
    triggerGroupProfileAvatarUpload,
    handleGroupAvatarSelected,
    submitCreateGroup,
    openGroupDrawer,
    closeGroupDrawer,
    openGroupMembersPage,
    openAddMembersModal,
    closeAddMembersModal,
    submitAddMembers,
    submitUpdateNotice,
    submitUpdateGroupProfile,
    canOperateMember,
    canToggleAdmin,
    toggleAdminRole,
    toggleMuteMember,
    openMuteMemberModal,
    closeMuteMemberModal,
    submitMuteMember,
    handleRemoveMember,
    handleDissolveGroup,
    handleLeaveGroup,
    openTransferOwnerModal,
    closeTransferOwnerModal,
    submitTransferOwner
  }
}
