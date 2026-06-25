/**
 * useGroupMembersPage 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'  // 行注：引入 computed, onMounted, onUnmounted, reactive, ref, watch 能力
import { useRoute, useRouter } from 'vue-router'  // 行注：引入 useRoute, useRouter 能力
import { useMessage } from 'naive-ui'  // 行注：引入 useMessage 能力
import { chatApi, friendApi, groupApi } from '../api/client'  // 行注：引入 chatApi, friendApi, groupApi 能力
import { useConfirmDialog } from './useConfirmDialog'  // 行注：引入 useConfirmDialog 能力
import { useChatSocket } from './useChatSocket'  // 行注：引入 useChatSocket 能力
import { useGroupMembersManagement } from './useGroupMembersManagement'  // 行注：引入 useGroupMembersManagement 能力
import { useGroupMembersRequests } from './useGroupMembersRequests'  // 行注：引入 useGroupMembersRequests 能力
import { useGroupMembersResources } from './useGroupMembersResources'  // 行注：引入 useGroupMembersResources 能力
import { useUserStore } from '../stores/user'  // 行注：引入 useUserStore 能力
import {  // 行注：引入 { 模块
  GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
  GROUP_ROLE_MEMBER,  // 行注：补充当前配置项
  GROUP_ROLE_OWNER,  // 行注：补充当前配置项
  type FriendItem,  // 行注：补充当前配置项
  type GroupDetail,  // 行注：补充当前配置项
  type GroupPreferenceDraftState,  // 行注：补充当前配置项
  type GroupRoleFilter,  // 行注：补充当前配置项
  type GroupRoleFilterOption  // 行注：补充当前表达式
} from '../types/chat'  // 行注：补充当前表达式
import {  // 行注：引入 { 模块
  formatDateTime,  // 行注：补充 formatDateTime 配置项
  roleClass,  // 行注：补充当前配置项
  roleText  // 行注：补充当前表达式
} from '../utils/chat'  // 行注：补充当前表达式

export function useGroupMembersPage() {  // 行注：导出当前能力
  const route = useRoute()  // 行注：获取 route 组合式能力
  const router = useRouter()  // 行注：获取路由实例
  const message = useMessage()  // 行注：获取全局消息实例
  const userStore = useUserStore()  // 行注：获取 userStore 组合式能力
  const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()  // 行注：声明当前变量

  const groupAvatarInputRef = ref<HTMLInputElement>()  // 行注：初始化 groupAvatarInputRef 状态
  const groupDetail = ref<GroupDetail | null>(null)  // 行注：初始化 groupDetail 变量
  const friends = ref<FriendItem[]>([])  // 行注：初始化 friends 集合
  const pageLoading = ref(false)  // 行注：初始化 pageLoading 响应式状态
  const searchText = ref('')  // 行注：初始化 searchText 响应式状态
  const roleFilter = ref<GroupRoleFilter>('all')  // 行注：初始化 roleFilter 变量
  const showNoticeReminder = ref(false)  // 行注：初始化 showNoticeReminder 响应式状态
  const acknowledgingNoticeReminder = ref(false)  // 行注：初始化 acknowledgingNoticeReminder 响应式状态
  const noticeDraft = ref('')  // 行注：初始化 noticeDraft 响应式状态
  const groupPreferenceDraft = reactive<GroupPreferenceDraftState>({  // 行注：开始解构当前返回值
    groupRemark: '',  // 行注：设置 groupRemark 配置项
    notificationMuted: false  // 行注：设置 notificationMuted 配置项
  })  // 行注：结束当前调用配置
  const savingGroupPreferences = ref(false)  // 行注：初始化 savingGroupPreferences 响应式状态
  const closingUnavailableGroupPage = ref(false)  // 行注：初始化 closingUnavailableGroupPage 响应式状态

  const groupProfileDraft = reactive({  // 行注：开始解构当前返回值
    groupName: '',  // 行注：设置 groupName 配置项
    avatarPreview: '',  // 行注：设置 avatarPreview 配置项
    avatarFile: null as File | null  // 行注：设置 avatarFile 配置项
  })  // 行注：结束当前调用配置

  const roleFilters: GroupRoleFilterOption[] = [  // 行注：初始化 roleFilters 变量
    { label: '全部', value: 'all' },  // 行注：补充当前配置项
    { label: '群主', value: 'owner' },  // 行注：补充当前配置项
    { label: '管理员', value: 'admin' },  // 行注：补充当前配置项
    { label: '普通成员', value: 'member' },  // 行注：补充当前配置项
    { label: '已禁言', value: 'muted' }  // 行注：补充当前表达式
  ]  // 行注：补充当前表达式

  const groupId = computed(() => String(route.params.groupId || ''))  // 行注：声明 groupId 计算属性
  const currentGroupRole = computed(() => groupDetail.value?.myRole ?? GROUP_ROLE_MEMBER)  // 行注：声明 currentGroupRole 计算属性
  const canManageMembers = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canManageMembers 计算属性
  const canEditGroupProfile = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canEditGroupProfile 计算属性
  const canEditNotice = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canEditNotice 计算属性
  const isGroupOwner = computed(() => currentGroupRole.value === GROUP_ROLE_OWNER)  // 行注：声明 isGroupOwner 计算属性
  const canDissolveGroup = computed(() => isGroupOwner.value)  // 行注：声明 canDissolveGroup 计算属性

  const ownerCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_OWNER).length)  // 行注：声明 ownerCount 计算属性
  const adminCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_ADMIN).length)  // 行注：声明 adminCount 计算属性
  const mutedCount = computed(() => (groupDetail.value?.members || []).filter(member => member.muted).length)  // 行注：声明 mutedCount 计算属性
  const memberCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_MEMBER).length)  // 行注：声明 memberCount 计算属性
  const activeRoleFilterLabel = computed(() => roleFilters.find(option => option.value === roleFilter.value)?.label || '全部')  // 行注：声明 activeRoleFilterLabel 计算属性
  const groupDisplayName = computed(() => groupDetail.value?.groupRemark || groupDetail.value?.groupName || '')  // 行注：声明 groupDisplayName 计算属性
  const isGroupPreferenceChanged = computed(() => {  // 行注：开始解构当前返回值
    if (!groupDetail.value) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return groupPreferenceDraft.groupRemark.trim() !== (groupDetail.value.groupRemark || '').trim()  // 行注：返回当前结果
      || groupPreferenceDraft.notificationMuted !== Boolean(groupDetail.value.notificationMuted)  // 行注：执行当前调用逻辑
  })  // 行注：结束当前调用配置
  const isGroupProfileChanged = computed(() => {  // 行注：开始解构当前返回值
    if (!groupDetail.value) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const normalizedName = groupProfileDraft.groupName.trim()  // 行注：初始化 normalizedName 变量
    const currentName = groupDetail.value.groupName?.trim() || ''  // 行注：初始化 currentName 变量
    const currentAvatar = groupDetail.value.groupAvatar || ''  // 行注：初始化 currentAvatar 变量
    return normalizedName !== currentName  // 行注：返回当前结果
      || Boolean(groupProfileDraft.avatarFile)  // 行注：执行当前调用逻辑
      || groupProfileDraft.avatarPreview !== currentAvatar  // 行注：补充当前表达式
  })  // 行注：结束当前调用配置
  const isGroupNoticeChanged = computed(() => {  // 行注：开始解构当前返回值
    if (!groupDetail.value) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return noticeDraft.value.trim() !== (groupDetail.value.notice || '').trim()  // 行注：返回当前结果
  })  // 行注：结束当前调用配置
  const availableFriendsForCurrentGroup = computed(() => {  // 行注：开始解构当前返回值
    const memberIds = new Set((groupDetail.value?.members || []).map(member => String(member.userId)))  // 行注：初始化 memberIds 变量
    return friends.value.filter(friend => !memberIds.has(String(friend.friendId)))  // 行注：返回当前结果
  })  // 行注：结束当前调用配置
  const transferableMembers = computed(() => {  // 行注：开始解构当前返回值
    return (groupDetail.value?.members || []).filter(member => {  // 行注：返回当前结果
      return String(member.userId) !== String(userStore.userId) && member.role !== GROUP_ROLE_OWNER  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
  })  // 行注：结束当前调用配置
  const {  // 行注：开始解构当前返回值
    loadingGroupRequests,  // 行注：解构 loadingGroupRequests 方法
    requestActionLoadingId,  // 行注：解构 requestActionLoadingId 状态
    currentGroupRequests,  // 行注：解构 currentGroupRequests 状态
    loadGroupRequests,  // 行注：解构 loadGroupRequests 方法
    groupRequestTypeText,  // 行注：解构 groupRequestTypeText 状态
    groupRequestTagClass,  // 行注：解构 groupRequestTagClass 状态
    buildGroupRequestMessage,  // 行注：解构 buildGroupRequestMessage 方法
    formatRequestTime,  // 行注：解构 formatRequestTime 方法
    handleAcceptGroupRequest,  // 行注：解构 handleAcceptGroupRequest 方法
    handleRejectGroupRequest  // 行注：解构 handleRejectGroupRequest 方法
  } = useGroupMembersRequests({  // 行注：从 useGroupMembersRequests 中解构所需能力
    groupId,  // 行注：传入 groupId 参数
    message,  // 行注：传入 message 参数
    refreshGroupDetail: () => loadGroupDetail()  // 行注：传入 refreshGroupDetail 回调
  })  // 行注：结束当前调用配置

  const {  // 行注：开始解构当前返回值
    mediaType,  // 行注：解构 mediaType 状态
    mediaKeyword,  // 行注：解构 mediaKeyword 状态
    groupMediaItems,  // 行注：解构 groupMediaItems 状态
    groupMediaLoading,  // 行注：解构 groupMediaLoading 状态
    messageSearchKeyword,  // 行注：解构 messageSearchKeyword 状态
    groupMessageSearchResults,  // 行注：解构 groupMessageSearchResults 状态
    groupMessageSearchLoading,  // 行注：解构 groupMessageSearchLoading 状态
    resetResourceState,  // 行注：解构 resetResourceState 方法
    formatFileSize,  // 行注：解构 formatFileSize 方法
    isImageMedia,  // 行注：解构 isImageMedia 状态
    getMediaTypeText,  // 行注：解构 getMediaTypeText 方法
    getMessageSearchPreview,  // 行注：解构 getMessageSearchPreview 方法
    copyMediaLink,  // 行注：解构 copyMediaLink 方法
    openMediaResource,  // 行注：解构 openMediaResource 方法
    loadGroupMedia,  // 行注：解构 loadGroupMedia 方法
    searchGroupMessages  // 行注：解构 searchGroupMessages 状态
  } = useGroupMembersResources({  // 行注：从 useGroupMembersResources 中解构所需能力
    groupId,  // 行注：传入 groupId 参数
    message,  // 行注：传入 message 参数
    isGroupUnavailableError,  // 行注：传入 isGroupUnavailableError 参数
    handleUnavailableGroup: (messageText: string) => closeUnavailableGroupPage({  // 行注：设置 handleUnavailableGroup 配置项
      notify: true,  // 行注：设置 notify 配置项
      messageText  // 行注：传入 messageText 参数
    })  // 行注：结束当前调用配置
  })  // 行注：结束当前调用配置

  const {  // 行注：开始解构当前返回值
    actionLoading,  // 行注：解构 actionLoading 状态
    updatingGroupProfile,  // 行注：解构 updatingGroupProfile 状态
    updatingNotice,  // 行注：解构 updatingNotice 状态
    showAddMembersModal,  // 行注：解构 showAddMembersModal 状态
    addMembersSelection,  // 行注：解构 addMembersSelection 状态
    addMembersMessage,  // 行注：解构 addMembersMessage 状态
    addingMembers,  // 行注：解构 addingMembers 状态
    showTransferOwnerModal,  // 行注：解构 showTransferOwnerModal 状态
    transferOwnerSelection,  // 行注：解构 transferOwnerSelection 状态
    transferringOwner,  // 行注：解构 transferringOwner 状态
    showMuteMemberModal,  // 行注：解构 showMuteMemberModal 状态
    muteTargetMember,  // 行注：解构 muteTargetMember 状态
    muteMinutesInput,  // 行注：解构 muteMinutesInput 状态
    canOperateMember,  // 行注：解构 canOperateMember 状态
    canToggleAdmin,  // 行注：解构 canToggleAdmin 状态
    resetGroupProfileDraft,  // 行注：解构 resetGroupProfileDraft 方法
    syncGroupProfileDraft,  // 行注：解构 syncGroupProfileDraft 方法
    discardGroupProfileDrafts,  // 行注：解构 discardGroupProfileDrafts 状态
    triggerGroupAvatarUpload,  // 行注：解构 triggerGroupAvatarUpload 方法
    handleGroupAvatarSelected,  // 行注：解构 handleGroupAvatarSelected 方法
    submitUpdateNotice,  // 行注：解构 submitUpdateNotice 方法
    submitUpdateGroupProfile,  // 行注：解构 submitUpdateGroupProfile 方法
    openAddMembersModal,  // 行注：解构 openAddMembersModal 方法
    closeAddMembersModal,  // 行注：解构 closeAddMembersModal 方法
    submitAddMembers,  // 行注：解构 submitAddMembers 方法
    openTransferOwnerModal,  // 行注：解构 openTransferOwnerModal 方法
    closeTransferOwnerModal,  // 行注：解构 closeTransferOwnerModal 方法
    submitTransferOwner,  // 行注：解构 submitTransferOwner 方法
    toggleAdminRole,  // 行注：解构 toggleAdminRole 方法
    toggleMuteMember,  // 行注：解构 toggleMuteMember 方法
    closeMuteMemberModal,  // 行注：解构 closeMuteMemberModal 方法
    submitMuteMember,  // 行注：解构 submitMuteMember 方法
    handleRemoveMember,  // 行注：解构 handleRemoveMember 方法
    handleDissolveGroup,  // 行注：解构 handleDissolveGroup 方法
    handleLeaveGroup  // 行注：解构 handleLeaveGroup 方法
  } = useGroupMembersManagement({  // 行注：从 useGroupMembersManagement 中解构所需能力
    groupId,  // 行注：传入 groupId 参数
    groupDetail,  // 行注：传入 groupDetail 参数
    currentGroupRole,  // 行注：传入 currentGroupRole 参数
    canManageMembers,  // 行注：传入 canManageMembers 参数
    canEditGroupProfile,  // 行注：传入 canEditGroupProfile 参数
    canEditNotice,  // 行注：传入 canEditNotice 参数
    isGroupOwner,  // 行注：传入 isGroupOwner 参数
    groupProfileDraft,  // 行注：传入 groupProfileDraft 参数
    noticeDraft,  // 行注：传入 noticeDraft 参数
    groupAvatarInputRef,  // 行注：传入 groupAvatarInputRef 参数
    getCurrentUserId: () => userStore.userId || '',  // 行注：传入 getCurrentUserId 回调
    message,  // 行注：传入 message 参数
    router,  // 行注：传入 router 参数
    openConfirmDialog,  // 行注：传入 openConfirmDialog 参数
    loadGroupDetail: () => loadGroupDetail(),  // 行注：传入 loadGroupDetail 回调
    applyGroupDetail  // 行注：传入 applyGroupDetail 参数
  })  // 行注：结束当前调用配置

  const manageableMemberCount = computed(() => {  // 行注：开始解构当前返回值
    return (groupDetail.value?.members || []).filter(member => canOperateMember(member)).length  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const filteredMembers = computed(() => {  // 行注：开始解构当前返回值
    const keyword = searchText.value.trim().toLowerCase()  // 行注：初始化 keyword 状态
    const members = [...(groupDetail.value?.members || [])].sort((left, right) => {  // 行注：开始解构当前返回值
      if (left.role !== right.role) {  // 行注：判断当前条件是否成立
        return right.role - left.role  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      return String(left.userId).localeCompare(String(right.userId))  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
    return members.filter(member => {  // 行注：返回当前结果
      if (roleFilter.value === 'owner' && member.role !== GROUP_ROLE_OWNER) {  // 行注：判断当前条件是否成立
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (roleFilter.value === 'admin' && member.role !== GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (roleFilter.value === 'member' && member.role !== GROUP_ROLE_MEMBER) {  // 行注：判断当前条件是否成立
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (roleFilter.value === 'muted' && !member.muted) {  // 行注：判断当前条件是否成立
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (!keyword) {  // 行注：判断当前条件是否成立
        return true  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      const searchableText = `${member.nickname || ''} ${member.username || ''}`.toLowerCase()  // 行注：初始化 searchableText 变量
      return searchableText.includes(keyword)  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
  })  // 行注：结束当前调用配置

  function syncGroupPreferenceDraft(detail?: GroupDetail | null) {  // 行注：定义 syncGroupPreferenceDraft 方法
    groupPreferenceDraft.groupRemark = detail?.groupRemark || ''  // 行注：更新 groupPreferenceDraft.groupRemark 值
    groupPreferenceDraft.notificationMuted = Boolean(detail?.notificationMuted)  // 行注：更新 groupPreferenceDraft.notificationMuted 值
  }  // 行注：结束当前代码块

  function syncNoticeReminder(detail: GroupDetail | null) {  // 行注：定义 syncNoticeReminder 方法
    showNoticeReminder.value = Boolean(detail?.noticeUnread && detail.notice?.trim())  // 行注：更新 showNoticeReminder 状态
  }  // 行注：结束当前代码块

  function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {  // 行注：定义 applyGroupDetail 方法
    groupDetail.value = detail  // 行注：更新 groupDetail 状态
    if (syncDraft || !isGroupNoticeChanged.value) {  // 行注：判断当前条件是否成立
      noticeDraft.value = detail?.notice || ''  // 行注：更新 noticeDraft 状态
    }  // 行注：结束当前代码块
    if (syncDraft || !isGroupProfileChanged.value) {  // 行注：判断当前条件是否成立
      syncGroupProfileDraft(detail)  // 行注：调用 syncGroupProfileDraft 方法
    }  // 行注：结束当前代码块
    if (syncDraft || !isGroupPreferenceChanged.value) {  // 行注：判断当前条件是否成立
      syncGroupPreferenceDraft(detail)  // 行注：调用 syncGroupPreferenceDraft 方法
    }  // 行注：结束当前代码块
    syncNoticeReminder(detail)  // 行注：调用 syncNoticeReminder 方法
  }  // 行注：结束当前代码块

  function isGroupUnavailableError(error: any) {  // 行注：定义 isGroupUnavailableError 方法
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)  // 行注：初始化 code 变量
    return code === 403 || code === 404  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function closeUnavailableGroupPage(options: { notify?: boolean; messageText?: string } = {}) {  // 行注：定义异步 closeUnavailableGroupPage 方法
    if (closingUnavailableGroupPage.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    closingUnavailableGroupPage.value = true  // 行注：更新 closingUnavailableGroupPage 状态
    applyGroupDetail(null)  // 行注：调用 applyGroupDetail 方法
    resetResourceState()  // 行注：调用 resetResourceState 方法
    showNoticeReminder.value = false  // 行注：更新 showNoticeReminder 状态
    showAddMembersModal.value = false  // 行注：更新 showAddMembersModal 状态
    showTransferOwnerModal.value = false  // 行注：更新 showTransferOwnerModal 状态
    showMuteMemberModal.value = false  // 行注：更新 showMuteMemberModal 状态
    muteTargetMember.value = null  // 行注：更新 muteTargetMember 状态
    muteMinutesInput.value = '10'  // 行注：更新 muteMinutesInput 状态
    try {  // 行注：尝试执行可能失败的逻辑
      if (options.notify) {  // 行注：判断当前条件是否成立
        message.warning(options.messageText || '当前群聊已不可访问')  // 行注：提示警告信息
      }  // 行注：结束当前代码块
      await router.replace('/chat')  // 行注：替换当前路由
    } finally {  // 行注：执行收尾清理逻辑
      closingUnavailableGroupPage.value = false  // 行注：更新 closingUnavailableGroupPage 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function loadGroupDetail(showSuccess = false) {  // 行注：定义异步 loadGroupDetail 方法
    if (!groupId.value) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    pageLoading.value = true  // 行注：更新 pageLoading 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.detail(groupId.value)  // 行注：接收 response 异步结果
      applyGroupDetail(response.data || null, true)  // 行注：调用 applyGroupDetail 方法
      if (showSuccess) {  // 行注：判断当前条件是否成立
        message.success('群成员已刷新')  // 行注：提示成功信息
      }  // 行注：结束当前代码块
      return true  // 行注：返回当前结果
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('loadGroupDetail error:', error)  // 行注：输出错误日志
      if (isGroupUnavailableError(error)) {  // 行注：判断当前条件是否成立
        await closeUnavailableGroupPage({  // 行注：开始当前逻辑块
          notify: true,  // 行注：设置 notify 配置项
          messageText: '当前群聊已不可访问'  // 行注：设置 messageText 配置项
        })  // 行注：结束当前调用配置
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      message.error(error.response?.data?.message || '加载群成员失败')  // 行注：提示错误信息
      return false  // 行注：返回当前结果
    } finally {  // 行注：执行收尾清理逻辑
      pageLoading.value = false  // 行注：更新 pageLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function handleRealtimeGroupDetail(detail: GroupDetail | null) {  // 行注：定义 handleRealtimeGroupDetail 方法
    if (!detail?.id || String(detail.id) !== groupId.value || closingUnavailableGroupPage.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    applyGroupDetail(detail, false)  // 行注：调用 applyGroupDetail 方法
  }  // 行注：结束当前代码块

  async function handleRealtimeGroupRemoved(payload: any) {  // 行注：定义异步 handleRealtimeGroupRemoved 方法
    const removedGroupId = payload?.groupId ? String(payload.groupId) : ''  // 行注：初始化 removedGroupId 方法
    if (!removedGroupId || removedGroupId !== groupId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    await closeUnavailableGroupPage({  // 行注：开始当前逻辑块
      notify: true,  // 行注：设置 notify 配置项
      messageText: '你已不在该群聊中'  // 行注：设置 messageText 配置项
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function handleRealtimeEvent(payload: any) {  // 行注：定义 handleRealtimeEvent 方法
    if (!payload?.type) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (payload.type === 'GROUP_DETAIL') {  // 行注：判断当前条件是否成立
      handleRealtimeGroupDetail(payload.data?.detail || null)  // 行注：调用 handleRealtimeGroupDetail 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (payload.type === 'GROUP_REMOVED') {  // 行注：判断当前条件是否成立
      void handleRealtimeGroupRemoved(payload.data)  // 行注：调用 handleRealtimeGroupRemoved 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  const { connect: connectChatSocket } = useChatSocket({  // 行注：开始解构当前返回值
    token: () => userStore.token,  // 行注：传入 token 回调
    createTicket: async () => {  // 行注：设置 createTicket 配置项
      const response: any = await chatApi.createWsTicket()  // 行注：接收 response 异步结果
      return String(response.data?.ticket || '')  // 行注：返回当前结果
    },  // 行注：补充当前配置项
    onMessage: handleRealtimeEvent,  // 行注：设置 onMessage 配置项
    onOpen: () => {  // 行注：传入 onOpen 回调
      if (groupId.value && !closingUnavailableGroupPage.value) {  // 行注：判断当前条件是否成立
        void loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
  })  // 行注：结束当前调用配置

  async function loadFriends() {  // 行注：定义异步 loadFriends 方法
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await friendApi.getList()  // 行注：接收 response 异步结果
      friends.value = response.data || []  // 行注：更新 friends 状态
    } catch (error) {  // 行注：捕获并处理异常
      console.error('loadFriends error:', error)  // 行注：输出错误日志
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function refreshPageData() {  // 行注：定义异步 refreshPageData 方法
    const detailLoaded = await loadGroupDetail(true)  // 行注：接收 detailLoaded 异步结果
    if (!detailLoaded) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    await Promise.all([  // 行注：补充当前表达式
      loadGroupRequests(),  // 行注：调用 loadGroupRequests 方法
      loadGroupMedia(),  // 行注：调用 loadGroupMedia 方法
      messageSearchKeyword.value.trim() ? searchGroupMessages() : Promise.resolve()  // 行注：调用 trim 方法
    ])  // 行注：补充当前表达式
  }  // 行注：结束当前代码块

  async function copyGroupId() {  // 行注：定义异步 copyGroupId 方法
    if (!groupDetail.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await navigator.clipboard.writeText(String(groupDetail.value.id))  // 行注：写入剪贴板
      message.success(`群号 ${groupDetail.value.id} 已复制`)  // 行注：提示成功信息
    } catch {  // 行注：捕获并处理异常
      message.error('复制群号失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function openGroupChat(targetMessage?: { id?: string | number | null }) {  // 行注：定义 openGroupChat 方法
    if (!groupId.value) {  // 行注：判断当前条件是否成立
      router.push('/chat')  // 行注：跳转到目标路由
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const query: Record<string, string> = { sessionType: '2' }  // 行注：初始化 query 变量
    if (targetMessage?.id != null) {  // 行注：判断当前条件是否成立
      query.messageId = String(targetMessage.id)  // 行注：更新 query.messageId 值
    }  // 行注：结束当前代码块
    router.push({  // 行注：开始当前逻辑块
      path: `/chat/${groupId.value}`,  // 行注：设置 path 配置项
      query  // 行注：传入 query 参数
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  async function submitGroupPreferences() {  // 行注：定义异步 submitGroupPreferences 方法
    if (!groupId.value || !isGroupPreferenceChanged.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    savingGroupPreferences.value = true  // 行注：更新 savingGroupPreferences 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.updatePreferences(groupId.value, {  // 行注：开始当前逻辑块
        groupRemark: groupPreferenceDraft.groupRemark.trim(),  // 行注：设置 groupRemark 配置项
        notificationMuted: groupPreferenceDraft.notificationMuted  // 行注：设置 notificationMuted 配置项
      })  // 行注：结束当前调用配置
      if (groupDetail.value) {  // 行注：判断当前条件是否成立
        applyGroupDetail({  // 行注：开始当前逻辑块
          ...groupDetail.value,  // 行注：补充当前配置项
          groupRemark: groupPreferenceDraft.groupRemark.trim(),  // 行注：设置 groupRemark 配置项
          notificationMuted: groupPreferenceDraft.notificationMuted  // 行注：设置 notificationMuted 配置项
        }, true)  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
      message.success('群偏好已保存')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitGroupPreferences error:', error)  // 行注：输出错误日志
      message.error(error.response?.data?.message || '保存群偏好失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      savingGroupPreferences.value = false  // 行注：更新 savingGroupPreferences 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function acknowledgeNoticeReminder() {  // 行注：定义异步 acknowledgeNoticeReminder 方法
    if (!groupId.value || !groupDetail.value) {  // 行注：判断当前条件是否成立
      showNoticeReminder.value = false  // 行注：更新 showNoticeReminder 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    acknowledgingNoticeReminder.value = true  // 行注：更新 acknowledgingNoticeReminder 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.markNoticeRead(groupId.value)  // 行注：调用 markNoticeRead 方法
      applyGroupDetail({  // 行注：开始当前逻辑块
        ...groupDetail.value,  // 行注：补充当前配置项
        noticeUnread: false,  // 行注：设置 noticeUnread 配置项
        noticeReadTime: groupDetail.value.noticeUpdateTime || groupDetail.value.noticeReadTime  // 行注：设置 noticeReadTime 配置项
      }, false)  // 行注：补充当前表达式
      showNoticeReminder.value = false  // 行注：更新 showNoticeReminder 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('acknowledgeNoticeReminder error:', error)  // 行注：输出错误日志
      message.error(error.response?.data?.message || '确认群公告失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      acknowledgingNoticeReminder.value = false  // 行注：更新 acknowledgingNoticeReminder 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  watch(() => route.params.groupId, () => {  // 行注：监听状态变化
    searchText.value = ''  // 行注：更新 searchText 状态
    roleFilter.value = 'all'  // 行注：更新 roleFilter 状态
    mediaKeyword.value = ''  // 行注：更新 mediaKeyword 状态
    mediaType.value = 'all'  // 行注：更新 mediaType 状态
    messageSearchKeyword.value = ''  // 行注：更新 messageSearchKeyword 状态
    groupMessageSearchResults.value = []  // 行注：更新 groupMessageSearchResults 状态
    void refreshPageData()  // 行注：调用 refreshPageData 方法
  })  // 行注：结束当前调用配置

  onMounted(() => {  // 行注：注册组件挂载后的初始化逻辑
    void connectChatSocket()  // 行注：调用 connectChatSocket 方法
    void loadFriends()  // 行注：调用 loadFriends 方法
    void refreshPageData()  // 行注：调用 refreshPageData 方法
  })  // 行注：结束当前调用配置

  onUnmounted(() => {  // 行注：注册组件卸载时的清理逻辑
    resetGroupProfileDraft()  // 行注：调用 resetGroupProfileDraft 方法
  })  // 行注：结束当前调用配置

  return {  // 行注：返回当前结果
    GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
    roleText,  // 行注：补充当前配置项
    roleClass,  // 行注：补充当前配置项
    formatDateTime,  // 行注：补充当前配置项
    confirmDialog,  // 行注：补充当前配置项
    cancelConfirmDialog,  // 行注：补充当前配置项
    confirmConfirmDialog,  // 行注：补充当前配置项
    userStore,  // 行注：补充当前配置项
    groupAvatarInputRef,  // 行注：补充当前配置项
    groupDetail,  // 行注：补充当前配置项
    friends,  // 行注：补充当前配置项
    pageLoading,  // 行注：补充当前配置项
    loadingGroupRequests,  // 行注：补充当前配置项
    actionLoading,  // 行注：补充当前配置项
    requestActionLoadingId,  // 行注：补充当前配置项
    searchText,  // 行注：补充当前配置项
    roleFilter,  // 行注：补充当前配置项
    updatingGroupProfile,  // 行注：补充当前配置项
    updatingNotice,  // 行注：补充当前配置项
    showNoticeReminder,  // 行注：补充当前配置项
    acknowledgingNoticeReminder,  // 行注：补充当前配置项
    noticeDraft,  // 行注：补充当前配置项
    showAddMembersModal,  // 行注：补充当前配置项
    addMembersSelection,  // 行注：补充当前配置项
    addMembersMessage,  // 行注：补充当前配置项
    addingMembers,  // 行注：补充当前配置项
    showTransferOwnerModal,  // 行注：补充当前配置项
    transferOwnerSelection,  // 行注：补充当前配置项
    transferringOwner,  // 行注：补充当前配置项
    showMuteMemberModal,  // 行注：补充当前配置项
    muteTargetMember,  // 行注：补充当前配置项
    muteMinutesInput,  // 行注：补充当前配置项
    groupPreferenceDraft,  // 行注：补充当前配置项
    savingGroupPreferences,  // 行注：补充当前配置项
    mediaType,  // 行注：补充当前配置项
    mediaKeyword,  // 行注：补充当前配置项
    groupMediaItems,  // 行注：补充当前配置项
    groupMediaLoading,  // 行注：补充当前配置项
    messageSearchKeyword,  // 行注：补充当前配置项
    groupMessageSearchResults,  // 行注：补充当前配置项
    groupMessageSearchLoading,  // 行注：补充当前配置项
    groupProfileDraft,  // 行注：补充当前配置项
    roleFilters,  // 行注：补充当前配置项
    currentGroupRole,  // 行注：补充当前配置项
    canManageMembers,  // 行注：补充当前配置项
    canEditGroupProfile,  // 行注：补充当前配置项
    canEditNotice,  // 行注：补充当前配置项
    isGroupOwner,  // 行注：补充当前配置项
    canDissolveGroup,  // 行注：补充当前配置项
    ownerCount,  // 行注：补充当前配置项
    adminCount,  // 行注：补充当前配置项
    mutedCount,  // 行注：补充当前配置项
    memberCount,  // 行注：补充当前配置项
    activeRoleFilterLabel,  // 行注：补充当前配置项
    manageableMemberCount,  // 行注：补充当前配置项
    groupDisplayName,  // 行注：补充当前配置项
    isGroupPreferenceChanged,  // 行注：补充当前配置项
    isGroupProfileChanged,  // 行注：补充当前配置项
    isGroupNoticeChanged,  // 行注：补充当前配置项
    availableFriendsForCurrentGroup,  // 行注：补充当前配置项
    transferableMembers,  // 行注：补充当前配置项
    currentGroupRequests,  // 行注：补充当前配置项
    filteredMembers,  // 行注：补充当前配置项
    canOperateMember,  // 行注：补充当前配置项
    canToggleAdmin,  // 行注：补充当前配置项
    discardGroupProfileDrafts,  // 行注：补充当前配置项
    syncGroupPreferenceDraft,  // 行注：补充当前配置项
    refreshPageData,  // 行注：补充当前配置项
    copyGroupId,  // 行注：补充当前配置项
    openGroupChat,  // 行注：补充当前配置项
    formatFileSize,  // 行注：补充当前配置项
    isImageMedia,  // 行注：补充当前配置项
    getMediaTypeText,  // 行注：补充当前配置项
    getMessageSearchPreview,  // 行注：补充当前配置项
    copyMediaLink,  // 行注：补充当前配置项
    openMediaResource,  // 行注：补充当前配置项
    submitGroupPreferences,  // 行注：补充当前配置项
    loadGroupMedia,  // 行注：补充当前配置项
    searchGroupMessages,  // 行注：补充当前配置项
    triggerGroupAvatarUpload,  // 行注：补充当前配置项
    handleGroupAvatarSelected,  // 行注：补充当前配置项
    groupRequestTypeText,  // 行注：补充当前配置项
    groupRequestTagClass,  // 行注：补充当前配置项
    buildGroupRequestMessage,  // 行注：补充当前配置项
    formatRequestTime,  // 行注：补充当前配置项
    handleAcceptGroupRequest,  // 行注：补充当前配置项
    handleRejectGroupRequest,  // 行注：补充当前配置项
    submitUpdateNotice,  // 行注：补充当前配置项
    acknowledgeNoticeReminder,  // 行注：补充当前配置项
    submitUpdateGroupProfile,  // 行注：补充当前配置项
    openAddMembersModal,  // 行注：补充当前配置项
    closeAddMembersModal,  // 行注：补充当前配置项
    submitAddMembers,  // 行注：补充当前配置项
    openTransferOwnerModal,  // 行注：补充当前配置项
    closeTransferOwnerModal,  // 行注：补充当前配置项
    submitTransferOwner,  // 行注：补充当前配置项
    toggleAdminRole,  // 行注：补充当前配置项
    toggleMuteMember,  // 行注：补充当前配置项
    closeMuteMemberModal,  // 行注：补充当前配置项
    submitMuteMember,  // 行注：补充当前配置项
    handleRemoveMember,  // 行注：补充当前配置项
    handleDissolveGroup,  // 行注：补充当前配置项
    handleLeaveGroup  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
