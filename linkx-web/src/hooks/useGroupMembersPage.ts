import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { chatApi, friendApi, groupApi } from '../api/client'
import { useConfirmDialog } from './useConfirmDialog'
import { useChatSocket } from './useChatSocket'
import { useGroupMembersManagement } from './useGroupMembersManagement'
import { useGroupMembersRequests } from './useGroupMembersRequests'
import { useGroupMembersResources } from './useGroupMembersResources'
import { useUserStore } from '../stores/user'
import {
  GROUP_ROLE_ADMIN,
  GROUP_ROLE_MEMBER,
  GROUP_ROLE_OWNER,
  type FriendItem,
  type GroupDetail,
  type GroupPreferenceDraftState,
  type GroupRoleFilter,
  type GroupRoleFilterOption
} from '../types/chat'
import {
  formatDateTime,
  roleClass,
  roleText
} from '../utils/chat'

export function useGroupMembersPage() {
  const route = useRoute()
  const router = useRouter()
  const message = useMessage()
  const userStore = useUserStore()
  const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()

  const groupAvatarInputRef = ref<HTMLInputElement>()
  const groupDetail = ref<GroupDetail | null>(null)
  const friends = ref<FriendItem[]>([])
  const pageLoading = ref(false)
  const searchText = ref('')
  const roleFilter = ref<GroupRoleFilter>('all')
  const showNoticeReminder = ref(false)
  const acknowledgingNoticeReminder = ref(false)
  const noticeDraft = ref('')
  const groupPreferenceDraft = reactive<GroupPreferenceDraftState>({
    groupRemark: '',
    notificationMuted: false
  })
  const savingGroupPreferences = ref(false)
  const closingUnavailableGroupPage = ref(false)

  const groupProfileDraft = reactive({
    groupName: '',
    avatarPreview: '',
    avatarFile: null as File | null
  })

  const roleFilters: GroupRoleFilterOption[] = [
    { label: '全部', value: 'all' },
    { label: '群主', value: 'owner' },
    { label: '管理员', value: 'admin' },
    { label: '普通成员', value: 'member' },
    { label: '已禁言', value: 'muted' }
  ]

  const groupId = computed(() => String(route.params.groupId || ''))
  const currentGroupRole = computed(() => groupDetail.value?.myRole ?? GROUP_ROLE_MEMBER)
  const canManageMembers = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
  const canEditGroupProfile = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
  const canEditNotice = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
  const isGroupOwner = computed(() => currentGroupRole.value === GROUP_ROLE_OWNER)
  const canDissolveGroup = computed(() => isGroupOwner.value)

  const ownerCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_OWNER).length)
  const adminCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_ADMIN).length)
  const mutedCount = computed(() => (groupDetail.value?.members || []).filter(member => member.muted).length)
  const memberCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_MEMBER).length)
  const activeRoleFilterLabel = computed(() => roleFilters.find(option => option.value === roleFilter.value)?.label || '全部')
  const groupDisplayName = computed(() => groupDetail.value?.groupRemark || groupDetail.value?.groupName || '')
  const isGroupPreferenceChanged = computed(() => {
    if (!groupDetail.value) {
      return false
    }
    return groupPreferenceDraft.groupRemark.trim() !== (groupDetail.value.groupRemark || '').trim()
      || groupPreferenceDraft.notificationMuted !== Boolean(groupDetail.value.notificationMuted)
  })
  const isGroupProfileChanged = computed(() => {
    if (!groupDetail.value) {
      return false
    }
    const normalizedName = groupProfileDraft.groupName.trim()
    const currentName = groupDetail.value.groupName?.trim() || ''
    const currentAvatar = groupDetail.value.groupAvatar || ''
    return normalizedName !== currentName
      || Boolean(groupProfileDraft.avatarFile)
      || groupProfileDraft.avatarPreview !== currentAvatar
  })
  const isGroupNoticeChanged = computed(() => {
    if (!groupDetail.value) {
      return false
    }
    return noticeDraft.value.trim() !== (groupDetail.value.notice || '').trim()
  })
  const availableFriendsForCurrentGroup = computed(() => {
    const memberIds = new Set((groupDetail.value?.members || []).map(member => String(member.userId)))
    return friends.value.filter(friend => !memberIds.has(String(friend.friendId)))
  })
  const transferableMembers = computed(() => {
    return (groupDetail.value?.members || []).filter(member => {
      return String(member.userId) !== String(userStore.userId) && member.role !== GROUP_ROLE_OWNER
    })
  })
  const {
    loadingGroupRequests,
    requestActionLoadingId,
    currentGroupRequests,
    loadGroupRequests,
    groupRequestTypeText,
    groupRequestTagClass,
    buildGroupRequestMessage,
    formatRequestTime,
    handleAcceptGroupRequest,
    handleRejectGroupRequest
  } = useGroupMembersRequests({
    groupId,
    message,
    refreshGroupDetail: () => loadGroupDetail()
  })

  const {
    mediaType,
    mediaKeyword,
    groupMediaItems,
    groupMediaLoading,
    messageSearchKeyword,
    groupMessageSearchResults,
    groupMessageSearchLoading,
    resetResourceState,
    formatFileSize,
    isImageMedia,
    getMediaTypeText,
    getMessageSearchPreview,
    copyMediaLink,
    openMediaResource,
    loadGroupMedia,
    searchGroupMessages
  } = useGroupMembersResources({
    groupId,
    message,
    isGroupUnavailableError,
    handleUnavailableGroup: (messageText: string) => closeUnavailableGroupPage({
      notify: true,
      messageText
    })
  })

  const {
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
  } = useGroupMembersManagement({
    groupId,
    groupDetail,
    currentGroupRole,
    canManageMembers,
    canEditGroupProfile,
    canEditNotice,
    isGroupOwner,
    groupProfileDraft,
    noticeDraft,
    groupAvatarInputRef,
    getCurrentUserId: () => userStore.userId || '',
    message,
    router,
    openConfirmDialog,
    loadGroupDetail: () => loadGroupDetail(),
    applyGroupDetail
  })

  const manageableMemberCount = computed(() => {
    return (groupDetail.value?.members || []).filter(member => canOperateMember(member)).length
  })

  const filteredMembers = computed(() => {
    const keyword = searchText.value.trim().toLowerCase()
    const members = [...(groupDetail.value?.members || [])].sort((left, right) => {
      if (left.role !== right.role) {
        return right.role - left.role
      }
      return String(left.userId).localeCompare(String(right.userId))
    })
    return members.filter(member => {
      if (roleFilter.value === 'owner' && member.role !== GROUP_ROLE_OWNER) {
        return false
      }
      if (roleFilter.value === 'admin' && member.role !== GROUP_ROLE_ADMIN) {
        return false
      }
      if (roleFilter.value === 'member' && member.role !== GROUP_ROLE_MEMBER) {
        return false
      }
      if (roleFilter.value === 'muted' && !member.muted) {
        return false
      }
      if (!keyword) {
        return true
      }
      const searchableText = `${member.nickname || ''} ${member.username || ''}`.toLowerCase()
      return searchableText.includes(keyword)
    })
  })

  function syncGroupPreferenceDraft(detail?: GroupDetail | null) {
    groupPreferenceDraft.groupRemark = detail?.groupRemark || ''
    groupPreferenceDraft.notificationMuted = Boolean(detail?.notificationMuted)
  }

  function syncNoticeReminder(detail: GroupDetail | null) {
    showNoticeReminder.value = Boolean(detail?.noticeUnread && detail.notice?.trim())
  }

  function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {
    groupDetail.value = detail
    if (syncDraft || !isGroupNoticeChanged.value) {
      noticeDraft.value = detail?.notice || ''
    }
    if (syncDraft || !isGroupProfileChanged.value) {
      syncGroupProfileDraft(detail)
    }
    if (syncDraft || !isGroupPreferenceChanged.value) {
      syncGroupPreferenceDraft(detail)
    }
    syncNoticeReminder(detail)
  }

  function isGroupUnavailableError(error: any) {
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)
    return code === 403 || code === 404
  }

  async function closeUnavailableGroupPage(options: { notify?: boolean; messageText?: string } = {}) {
    if (closingUnavailableGroupPage.value) {
      return
    }
    closingUnavailableGroupPage.value = true
    applyGroupDetail(null)
    resetResourceState()
    showNoticeReminder.value = false
    showAddMembersModal.value = false
    showTransferOwnerModal.value = false
    showMuteMemberModal.value = false
    muteTargetMember.value = null
    muteMinutesInput.value = '10'
    try {
      if (options.notify) {
        message.warning(options.messageText || '当前群聊已不可访问')
      }
      await router.replace('/chat')
    } finally {
      closingUnavailableGroupPage.value = false
    }
  }

  async function loadGroupDetail(showSuccess = false) {
    if (!groupId.value) {
      return false
    }
    pageLoading.value = true
    try {
      const response: any = await groupApi.detail(groupId.value)
      applyGroupDetail(response.data || null, true)
      if (showSuccess) {
        message.success('群成员已刷新')
      }
      return true
    } catch (error: any) {
      console.error('loadGroupDetail error:', error)
      if (isGroupUnavailableError(error)) {
        await closeUnavailableGroupPage({
          notify: true,
          messageText: '当前群聊已不可访问'
        })
        return false
      }
      message.error(error.response?.data?.message || '加载群成员失败')
      return false
    } finally {
      pageLoading.value = false
    }
  }

  function handleRealtimeGroupDetail(detail: GroupDetail | null) {
    if (!detail?.id || String(detail.id) !== groupId.value || closingUnavailableGroupPage.value) {
      return
    }
    applyGroupDetail(detail, false)
  }

  async function handleRealtimeGroupRemoved(payload: any) {
    const removedGroupId = payload?.groupId ? String(payload.groupId) : ''
    if (!removedGroupId || removedGroupId !== groupId.value) {
      return
    }
    await closeUnavailableGroupPage({
      notify: true,
      messageText: '你已不在该群聊中'
    })
  }

  function handleRealtimeEvent(payload: any) {
    if (!payload?.type) {
      return
    }
    if (payload.type === 'GROUP_DETAIL') {
      handleRealtimeGroupDetail(payload.data?.detail || null)
      return
    }
    if (payload.type === 'GROUP_REMOVED') {
      void handleRealtimeGroupRemoved(payload.data)
    }
  }

  const { connect: connectChatSocket } = useChatSocket({
    token: () => userStore.token,
    createTicket: async () => {
      const response: any = await chatApi.createWsTicket()
      return String(response.data?.ticket || '')
    },
    onMessage: handleRealtimeEvent,
    onOpen: () => {
      if (groupId.value && !closingUnavailableGroupPage.value) {
        void loadGroupDetail()
      }
    }
  })

  async function loadFriends() {
    try {
      const response: any = await friendApi.getList()
      friends.value = response.data || []
    } catch (error) {
      console.error('loadFriends error:', error)
    }
  }

  async function refreshPageData() {
    const detailLoaded = await loadGroupDetail(true)
    if (!detailLoaded) {
      return
    }
    await Promise.all([
      loadGroupRequests(),
      loadGroupMedia(),
      messageSearchKeyword.value.trim() ? searchGroupMessages() : Promise.resolve()
    ])
  }

  async function copyGroupId() {
    if (!groupDetail.value) {
      return
    }
    try {
      await navigator.clipboard.writeText(String(groupDetail.value.id))
      message.success(`群号 ${groupDetail.value.id} 已复制`)
    } catch {
      message.error('复制群号失败')
    }
  }

  function openGroupChat(targetMessage?: { id?: string | number | null }) {
    if (!groupId.value) {
      router.push('/chat')
      return
    }
    const query: Record<string, string> = { sessionType: '2' }
    if (targetMessage?.id != null) {
      query.messageId = String(targetMessage.id)
    }
    router.push({
      path: `/chat/${groupId.value}`,
      query
    })
  }

  async function submitGroupPreferences() {
    if (!groupId.value || !isGroupPreferenceChanged.value) {
      return
    }
    savingGroupPreferences.value = true
    try {
      await groupApi.updatePreferences(groupId.value, {
        groupRemark: groupPreferenceDraft.groupRemark.trim(),
        notificationMuted: groupPreferenceDraft.notificationMuted
      })
      if (groupDetail.value) {
        applyGroupDetail({
          ...groupDetail.value,
          groupRemark: groupPreferenceDraft.groupRemark.trim(),
          notificationMuted: groupPreferenceDraft.notificationMuted
        }, true)
      }
      message.success('群偏好已保存')
    } catch (error: any) {
      console.error('submitGroupPreferences error:', error)
      message.error(error.response?.data?.message || '保存群偏好失败')
    } finally {
      savingGroupPreferences.value = false
    }
  }

  async function acknowledgeNoticeReminder() {
    if (!groupId.value || !groupDetail.value) {
      showNoticeReminder.value = false
      return
    }
    acknowledgingNoticeReminder.value = true
    try {
      await groupApi.markNoticeRead(groupId.value)
      applyGroupDetail({
        ...groupDetail.value,
        noticeUnread: false,
        noticeReadTime: groupDetail.value.noticeUpdateTime || groupDetail.value.noticeReadTime
      }, false)
      showNoticeReminder.value = false
    } catch (error: any) {
      console.error('acknowledgeNoticeReminder error:', error)
      message.error(error.response?.data?.message || '确认群公告失败')
    } finally {
      acknowledgingNoticeReminder.value = false
    }
  }

  watch(() => route.params.groupId, () => {
    searchText.value = ''
    roleFilter.value = 'all'
    mediaKeyword.value = ''
    mediaType.value = 'all'
    messageSearchKeyword.value = ''
    groupMessageSearchResults.value = []
    void refreshPageData()
  })

  onMounted(() => {
    void connectChatSocket()
    void loadFriends()
    void refreshPageData()
  })

  onUnmounted(() => {
    resetGroupProfileDraft()
  })

  return {
    GROUP_ROLE_ADMIN,
    roleText,
    roleClass,
    formatDateTime,
    confirmDialog,
    cancelConfirmDialog,
    confirmConfirmDialog,
    userStore,
    groupAvatarInputRef,
    groupDetail,
    friends,
    pageLoading,
    loadingGroupRequests,
    actionLoading,
    requestActionLoadingId,
    searchText,
    roleFilter,
    updatingGroupProfile,
    updatingNotice,
    showNoticeReminder,
    acknowledgingNoticeReminder,
    noticeDraft,
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
    groupPreferenceDraft,
    savingGroupPreferences,
    mediaType,
    mediaKeyword,
    groupMediaItems,
    groupMediaLoading,
    messageSearchKeyword,
    groupMessageSearchResults,
    groupMessageSearchLoading,
    groupProfileDraft,
    roleFilters,
    currentGroupRole,
    canManageMembers,
    canEditGroupProfile,
    canEditNotice,
    isGroupOwner,
    canDissolveGroup,
    ownerCount,
    adminCount,
    mutedCount,
    memberCount,
    activeRoleFilterLabel,
    manageableMemberCount,
    groupDisplayName,
    isGroupPreferenceChanged,
    isGroupProfileChanged,
    isGroupNoticeChanged,
    availableFriendsForCurrentGroup,
    transferableMembers,
    currentGroupRequests,
    filteredMembers,
    canOperateMember,
    canToggleAdmin,
    discardGroupProfileDrafts,
    syncGroupPreferenceDraft,
    refreshPageData,
    copyGroupId,
    openGroupChat,
    formatFileSize,
    isImageMedia,
    getMediaTypeText,
    getMessageSearchPreview,
    copyMediaLink,
    openMediaResource,
    submitGroupPreferences,
    loadGroupMedia,
    searchGroupMessages,
    triggerGroupAvatarUpload,
    handleGroupAvatarSelected,
    groupRequestTypeText,
    groupRequestTagClass,
    buildGroupRequestMessage,
    formatRequestTime,
    handleAcceptGroupRequest,
    handleRejectGroupRequest,
    submitUpdateNotice,
    acknowledgeNoticeReminder,
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
