/**
 * 处理群资料、成员管理、公告与群权限相关操作。
 */
import { computed, reactive, ref, type ComputedRef, type Ref } from 'vue'  // 行注：引入 computed, reactive, ref, type ComputedRef, type Ref 能力
import type { Router } from 'vue-router'  // 行注：引入 type { Router } 模块
import { fileApi, groupApi } from '../api/client'  // 行注：引入 fileApi, groupApi 能力
import type { ConfirmDialogOptions } from './useConfirmDialog'  // 行注：引入 type { ConfirmDialogOptions } 模块
import {  // 行注：引入 { 模块
  GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
  GROUP_ROLE_MEMBER,  // 行注：补充当前配置项
  GROUP_ROLE_OWNER,  // 行注：补充当前配置项
  SESSION_TYPE_GROUP,  // 行注：补充当前配置项
  type ChatSession,  // 行注：补充当前配置项
  type FriendItem,  // 行注：补充当前配置项
  type GroupDetail,  // 行注：补充当前配置项
  type GroupMember  // 行注：补充当前表达式
} from '../types/chat'  // 行注：补充当前表达式
import {  // 行注：引入 { 模块
  isGroupNoticeDraftChanged,  // 行注：补充 isGroupNoticeDraftChanged 配置项
  isGroupProfileDraftChanged,  // 行注：补充当前配置项
  resetGroupProfileDraftState,  // 行注：补充当前配置项
  revokeBlobUrl,  // 行注：补充当前配置项
  syncGroupProfileDraftState  // 行注：补充当前表达式
} from '../utils/group-draft'  // 行注：补充当前表达式
import { buildSessionKey } from '../utils/chat'  // 行注：引入 buildSessionKey 能力

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  warning: (content: string) => void  // 行注：设置 warning 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface GroupProfileDraft {  // 行注：开始当前逻辑块
  groupName: string  // 行注：设置 groupName 配置项
  avatarPreview: string  // 行注：设置 avatarPreview 配置项
  avatarFile: File | null  // 行注：设置 avatarFile 配置项
}  // 行注：结束当前代码块

interface UseGroupManagementOptions {  // 行注：开始当前逻辑块
  friends: Ref<FriendItem[]>  // 行注：设置 friends 配置项
  sessions: Ref<ChatSession[]>  // 行注：设置 sessions 配置项
  currentTargetId: Ref<string | null>  // 行注：设置 currentTargetId 配置项
  currentGroupRole: ComputedRef<number>  // 行注：设置 currentGroupRole 配置项
  currentSessionName: ComputedRef<string>  // 行注：设置 currentSessionName 配置项
  isGroupSession: ComputedRef<boolean>  // 行注：设置 isGroupSession 配置项
  canManageMembers: ComputedRef<boolean>  // 行注：设置 canManageMembers 配置项
  canEditGroupProfile: ComputedRef<boolean>  // 行注：设置 canEditGroupProfile 配置项
  canEditNotice: ComputedRef<boolean>  // 行注：设置 canEditNotice 配置项
  groupDetail: Ref<GroupDetail | null>  // 行注：设置 groupDetail 配置项
  showGroupDrawer: Ref<boolean>  // 行注：设置 showGroupDrawer 配置项
  noticeDraft: Ref<string>  // 行注：设置 noticeDraft 配置项
  groupProfileDraft: GroupProfileDraft  // 行注：设置 groupProfileDraft 配置项
  groupAvatarInputRef: Ref<HTMLInputElement | undefined>  // 行注：设置 groupAvatarInputRef 配置项
  getCurrentUserId: () => string | number  // 行注：传入 getCurrentUserId 回调
  message: FeedbackApi  // 行注：设置 message 配置项
  router: Router  // 行注：设置 router 配置项
  openConfirmDialog: (options: ConfirmDialogOptions) => Promise<boolean>  // 行注：设置 openConfirmDialog 配置项
  loadSessions: () => Promise<void>  // 行注：传入 loadSessions 回调
  loadGroupDetail: (groupId: string | number, syncDraft?: boolean) => Promise<boolean>  // 行注：设置 loadGroupDetail 配置项
  selectSession: (session: ChatSession, syncRoute?: boolean) => Promise<void>  // 行注：设置 selectSession 配置项
  upsertSession: (session: ChatSession) => ChatSession  // 行注：设置 upsertSession 配置项
  applyGroupDetail: (detail: GroupDetail | null, syncDraft?: boolean) => void  // 行注：设置 applyGroupDetail 配置项
  removeSessionByTarget: (targetId: string | number, sessionType: number) => void  // 行注：设置 removeSessionByTarget 配置项
  resetCurrentConversationState: () => void  // 行注：传入 resetCurrentConversationState 回调
  showMenu: Ref<boolean>  // 行注：设置 showMenu 配置项
}  // 行注：结束当前代码块

export function useGroupManagement(options: UseGroupManagementOptions) {  // 行注：导出当前能力
  const showCreateGroupModal = ref(false)  // 行注：初始化 showCreateGroupModal 响应式状态
  const showAddMembersModal = ref(false)  // 行注：初始化 showAddMembersModal 响应式状态
  const creatingGroup = ref(false)  // 行注：初始化 creatingGroup 响应式状态
  const addingMembers = ref(false)  // 行注：初始化 addingMembers 响应式状态
  const updatingNotice = ref(false)  // 行注：初始化 updatingNotice 响应式状态
  const updatingGroupProfile = ref(false)  // 行注：初始化 updatingGroupProfile 响应式状态
  const showTransferOwnerModal = ref(false)  // 行注：初始化 showTransferOwnerModal 响应式状态
  const transferOwnerSelection = ref<string | number | null>(null)  // 行注：初始化 transferOwnerSelection 变量
  const transferringOwner = ref(false)  // 行注：初始化 transferringOwner 响应式状态
  const showMuteMemberModal = ref(false)  // 行注：初始化 showMuteMemberModal 响应式状态
  const muteTargetMember = ref<GroupMember | null>(null)  // 行注：初始化 muteTargetMember 变量
  const muteMinutesInput = ref('10')  // 行注：初始化 muteMinutesInput 响应式状态
  const mutingMember = ref(false)  // 行注：初始化 mutingMember 响应式状态
  const addMembersSelection = ref<Array<string | number>>([])  // 行注：初始化 addMembersSelection 变量
  const addMembersMessage = ref('')  // 行注：初始化 addMembersMessage 响应式状态

  const createGroupForm = reactive({  // 行注：开始解构当前返回值
    groupName: '',  // 行注：设置 groupName 配置项
    notice: '',  // 行注：设置 notice 配置项
    memberIds: [] as Array<string | number>,  // 行注：设置 memberIds 配置项
    avatarPreview: '',  // 行注：设置 avatarPreview 配置项
    avatarFile: null as File | null  // 行注：设置 avatarFile 配置项
  })  // 行注：结束当前调用配置

  const isGroupProfileChanged = computed(() => {  // 行注：开始解构当前返回值
    return isGroupProfileDraftChanged(options.groupProfileDraft, options.groupDetail.value)  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const isGroupNoticeChanged = computed(() => {  // 行注：开始解构当前返回值
    return isGroupNoticeDraftChanged(options.noticeDraft.value, options.groupDetail.value)  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const hasUnsavedGroupDrawerChanges = computed(() => {  // 行注：开始解构当前返回值
    return isGroupProfileChanged.value || isGroupNoticeChanged.value  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const availableFriendsForCurrentGroup = computed(() => {  // 行注：开始解构当前返回值
    const memberIds = new Set((options.groupDetail.value?.members || []).map(member => String(member.userId)))  // 行注：初始化 memberIds 变量
    return options.friends.value.filter(friend => !memberIds.has(String(friend.friendId)))  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  function resetCreateGroupForm() {  // 行注：定义 resetCreateGroupForm 方法
    revokeBlobUrl(createGroupForm.avatarPreview)  // 行注：调用 revokeBlobUrl 方法
    createGroupForm.groupName = ''  // 行注：更新 createGroupForm.groupName 值
    createGroupForm.notice = ''  // 行注：更新 createGroupForm.notice 值
    createGroupForm.memberIds = []  // 行注：更新 createGroupForm.memberIds 值
    createGroupForm.avatarPreview = ''  // 行注：更新 createGroupForm.avatarPreview 值
    createGroupForm.avatarFile = null  // 行注：更新 createGroupForm.avatarFile 值
    if (options.groupAvatarInputRef.value) {  // 行注：判断当前条件是否成立
      options.groupAvatarInputRef.value.value = ''  // 行注：更新 options.groupAvatarInputRef.value 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function resetGroupProfileDraft() {  // 行注：定义 resetGroupProfileDraft 方法
    resetGroupProfileDraftState(options.groupProfileDraft)  // 行注：调用 resetGroupProfileDraftState 方法
    if (options.groupAvatarInputRef.value) {  // 行注：判断当前条件是否成立
      options.groupAvatarInputRef.value.value = ''  // 行注：更新 options.groupAvatarInputRef.value 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function syncGroupProfileDraft(detail?: GroupDetail | null) {  // 行注：定义 syncGroupProfileDraft 方法
    syncGroupProfileDraftState(options.groupProfileDraft, detail)  // 行注：调用 syncGroupProfileDraftState 方法
    if (options.groupAvatarInputRef.value) {  // 行注：判断当前条件是否成立
      options.groupAvatarInputRef.value.value = ''  // 行注：更新 options.groupAvatarInputRef.value 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function discardGroupDrawerDrafts() {  // 行注：定义 discardGroupDrawerDrafts 方法
    options.noticeDraft.value = options.groupDetail.value?.notice || ''  // 行注：更新 options.noticeDraft 状态
    syncGroupProfileDraft(options.groupDetail.value)  // 行注：调用 syncGroupProfileDraft 方法
  }  // 行注：结束当前代码块

  function openCreateGroupModal() {  // 行注：定义 openCreateGroupModal 方法
    resetCreateGroupForm()  // 行注：调用 resetCreateGroupForm 方法
    showCreateGroupModal.value = true  // 行注：更新 showCreateGroupModal 状态
  }  // 行注：结束当前代码块

  function closeCreateGroupModal() {  // 行注：定义 closeCreateGroupModal 方法
    showCreateGroupModal.value = false  // 行注：更新 showCreateGroupModal 状态
    resetCreateGroupForm()  // 行注：调用 resetCreateGroupForm 方法
  }  // 行注：结束当前代码块

  function triggerGroupAvatarUpload() {  // 行注：定义 triggerGroupAvatarUpload 方法
    options.groupAvatarInputRef.value?.click()  // 行注：调用 click 方法
  }  // 行注：结束当前代码块

  function triggerGroupProfileAvatarUpload() {  // 行注：定义 triggerGroupProfileAvatarUpload 方法
    if (!options.canEditGroupProfile.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    options.groupAvatarInputRef.value?.click()  // 行注：调用 click 方法
  }  // 行注：结束当前代码块

  function handleGroupAvatarSelected(event: Event) {  // 行注：定义 handleGroupAvatarSelected 方法
    const input = event.target as HTMLInputElement  // 行注：初始化 input 变量
    const file = input.files?.[0]  // 行注：初始化 file 变量
    if (!file) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const previewUrl = URL.createObjectURL(file)  // 行注：初始化 previewUrl 变量
    if (showCreateGroupModal.value) {  // 行注：判断当前条件是否成立
      revokeBlobUrl(createGroupForm.avatarPreview)  // 行注：调用 revokeBlobUrl 方法
      createGroupForm.avatarFile = file  // 行注：更新 createGroupForm.avatarFile 值
      createGroupForm.avatarPreview = previewUrl  // 行注：更新 createGroupForm.avatarPreview 值
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.showGroupDrawer.value && options.canEditGroupProfile.value) {  // 行注：判断当前条件是否成立
      revokeBlobUrl(options.groupProfileDraft.avatarPreview)  // 行注：调用 revokeBlobUrl 方法
      options.groupProfileDraft.avatarFile = file  // 行注：更新 options.groupProfileDraft.avatarFile 值
      options.groupProfileDraft.avatarPreview = previewUrl  // 行注：更新 options.groupProfileDraft.avatarPreview 值
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    revokeBlobUrl(previewUrl)  // 行注：调用 revokeBlobUrl 方法
  }  // 行注：结束当前代码块

  async function submitCreateGroup() {  // 行注：定义异步 submitCreateGroup 方法
    if (!createGroupForm.groupName.trim()) {  // 行注：判断当前条件是否成立
      options.message.warning('请输入群名称')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (createGroupForm.memberIds.length === 0) {  // 行注：判断当前条件是否成立
      options.message.warning('请至少选择一位好友')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    creatingGroup.value = true  // 行注：更新 creatingGroup 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const requestedGroupName = createGroupForm.groupName.trim()  // 行注：初始化 requestedGroupName 变量
      const requestedNotice = createGroupForm.notice.trim()  // 行注：初始化 requestedNotice 变量
      const requestedMemberCount = createGroupForm.memberIds.length + 1  // 行注：初始化 requestedMemberCount 变量
      let groupAvatar = ''  // 行注：初始化 groupAvatar 变量
      if (createGroupForm.avatarFile) {  // 行注：判断当前条件是否成立
        const uploadResponse: any = await fileApi.uploadAvatar(createGroupForm.avatarFile)  // 行注：接收 uploadResponse 异步结果
        groupAvatar = uploadResponse.data?.fileUrl || ''  // 行注：更新 groupAvatar 值
      }  // 行注：结束当前代码块

      const response: any = await groupApi.create({  // 行注：开始解构当前返回值
        groupName: requestedGroupName,  // 行注：设置 groupName 配置项
        groupAvatar,  // 行注：传入 groupAvatar 参数
        notice: requestedNotice,  // 行注：设置 notice 配置项
        memberIds: createGroupForm.memberIds.map(item => String(item))  // 行注：设置 memberIds 配置项
      })  // 行注：结束当前调用配置

      const createdGroup = response.data || {}  // 行注：初始化 createdGroup 变量
      closeCreateGroupModal()  // 行注：调用 closeCreateGroupModal 方法
      await options.loadSessions()  // 行注：调用 loadSessions 方法

      const groupId = createdGroup.id  // 行注：初始化 groupId 状态
      let groupSession = options.sessions.value.find(session => {  // 行注：开始解构当前返回值
        return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(groupId, SESSION_TYPE_GROUP)  // 行注：返回当前结果
      })  // 行注：结束当前调用配置

      if (!groupSession && groupId) {  // 行注：判断当前条件是否成立
        groupSession = options.upsertSession({  // 行注：更新 groupSession 值
          targetId: groupId,  // 行注：设置 targetId 配置项
          sessionType: SESSION_TYPE_GROUP,  // 行注：设置 sessionType 配置项
          targetNickname: createdGroup.groupName || requestedGroupName,  // 行注：设置 targetNickname 配置项
          targetAvatar: createdGroup.groupAvatar || groupAvatar,  // 行注：设置 targetAvatar 配置项
          lastMessage: '',  // 行注：设置 lastMessage 配置项
          lastMessageTime: createdGroup.createTime || '',  // 行注：设置 lastMessageTime 配置项
          unreadCount: 0,  // 行注：设置 unreadCount 配置项
          memberCount: Number(createdGroup.memberCount || requestedMemberCount),  // 行注：设置 memberCount 配置项
          myRole: Number(createdGroup.myRole ?? GROUP_ROLE_OWNER),  // 行注：设置 myRole 配置项
          notice: createdGroup.notice || requestedNotice,  // 行注：设置 notice 配置项
          noticeUnread: false,  // 行注：设置 noticeUnread 配置项
          muted: Boolean(createdGroup.muted),  // 行注：设置 muted 配置项
          muteTime: createdGroup.muteTime || '',  // 行注：设置 muteTime 配置项
          isDraft: true  // 行注：设置 isDraft 配置项
        })  // 行注：结束当前调用配置
      }  // 行注：结束当前代码块

      if (groupSession) {  // 行注：判断当前条件是否成立
        await options.selectSession(groupSession)  // 行注：调用 selectSession 方法
      }  // 行注：结束当前代码块
      options.message.success(`群聊创建成功，群号：${groupId}`)  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitCreateGroup error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '创建群聊失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      creatingGroup.value = false  // 行注：更新 creatingGroup 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function openGroupDrawer() {  // 行注：定义异步 openGroupDrawer 方法
    options.showMenu.value = false  // 行注：更新 options.showMenu 状态
    if (!options.currentTargetId.value || !options.isGroupSession.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    options.showGroupDrawer.value = true  // 行注：更新 options.showGroupDrawer 状态
    await options.loadGroupDetail(options.currentTargetId.value)  // 行注：调用 loadGroupDetail 方法
  }  // 行注：结束当前代码块

  async function closeGroupDrawer(config: { force?: boolean } = {}) {  // 行注：定义异步 closeGroupDrawer 方法
    if (!config.force && options.showGroupDrawer.value && hasUnsavedGroupDrawerChanges.value) {  // 行注：判断当前条件是否成立
      const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
        title: '放弃未保存修改？',  // 行注：设置 title 配置项
        subtitle: '群名称或群公告有未保存内容',  // 行注：设置 subtitle 配置项
        description: '确认关闭后，将丢弃当前未保存的群资料与群公告修改。',  // 行注：设置 description 配置项
        cancelText: '继续编辑',  // 行注：设置 cancelText 配置项
        confirmText: '确认关闭'  // 行注：设置 confirmText 配置项
      })  // 行注：结束当前调用配置
      if (!confirmed) {  // 行注：判断当前条件是否成立
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    discardGroupDrawerDrafts()  // 行注：调用 discardGroupDrawerDrafts 方法
    options.showGroupDrawer.value = false  // 行注：更新 options.showGroupDrawer 状态
    return true  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function openGroupMembersPage() {  // 行注：定义异步 openGroupMembersPage 方法
    const targetGroupId = options.groupDetail.value?.id || options.currentTargetId.value  // 行注：初始化 targetGroupId 状态
    if (!targetGroupId || !options.isGroupSession.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const closed = await closeGroupDrawer()  // 行注：接收 closed 异步结果
    if (!closed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    await options.router.push({  // 行注：开始当前逻辑块
      path: `/groups/${targetGroupId}/members`,  // 行注：设置 path 配置项
      query: { from: 'chat' }  // 行注：设置 query 配置项
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function openAddMembersModal() {  // 行注：定义 openAddMembersModal 方法
    addMembersSelection.value = []  // 行注：更新 addMembersSelection 状态
    addMembersMessage.value = ''  // 行注：更新 addMembersMessage 状态
    showAddMembersModal.value = true  // 行注：更新 showAddMembersModal 状态
  }  // 行注：结束当前代码块

  function closeAddMembersModal() {  // 行注：定义 closeAddMembersModal 方法
    showAddMembersModal.value = false  // 行注：更新 showAddMembersModal 状态
    addMembersSelection.value = []  // 行注：更新 addMembersSelection 状态
    addMembersMessage.value = ''  // 行注：更新 addMembersMessage 状态
  }  // 行注：结束当前代码块

  async function submitAddMembers() {  // 行注：定义异步 submitAddMembers 方法
    if (!options.currentTargetId.value || addMembersSelection.value.length === 0) {  // 行注：判断当前条件是否成立
      options.message.warning('请选择要邀请的成员')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    addingMembers.value = true  // 行注：更新 addingMembers 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.inviteMembers(options.currentTargetId.value, addMembersSelection.value, addMembersMessage.value.trim())  // 行注：调用 inviteMembers 方法
      closeAddMembersModal()  // 行注：调用 closeAddMembersModal 方法
      options.message.success('入群邀请已发送')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitAddMembers error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '邀请成员失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      addingMembers.value = false  // 行注：更新 addingMembers 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function submitUpdateNotice() {  // 行注：定义异步 submitUpdateNotice 方法
    if (!options.currentTargetId.value || !options.canEditNotice.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    updatingNotice.value = true  // 行注：更新 updatingNotice 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.updateNotice(options.currentTargetId.value, options.noticeDraft.value.trim())  // 行注：调用 updateNotice 方法
      await options.loadGroupDetail(options.currentTargetId.value)  // 行注：调用 loadGroupDetail 方法
      options.message.success('群公告已更新')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitUpdateNotice error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '更新群公告失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      updatingNotice.value = false  // 行注：更新 updatingNotice 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function submitUpdateGroupProfile() {  // 行注：定义异步 submitUpdateGroupProfile 方法
    if (!options.currentTargetId.value || !options.groupDetail.value || !options.canEditGroupProfile.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const nextGroupName = options.groupProfileDraft.groupName.trim()  // 行注：初始化 nextGroupName 变量
    if (!nextGroupName) {  // 行注：判断当前条件是否成立
      options.message.warning('请输入群名称')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    updatingGroupProfile.value = true  // 行注：更新 updatingGroupProfile 状态
    try {  // 行注：尝试执行可能失败的逻辑
      let groupAvatar = options.groupDetail.value.groupAvatar || ''  // 行注：初始化 groupAvatar 变量
      if (options.groupProfileDraft.avatarFile) {  // 行注：判断当前条件是否成立
        const uploadResponse: any = await fileApi.uploadAvatar(options.groupProfileDraft.avatarFile)  // 行注：接收 uploadResponse 异步结果
        groupAvatar = uploadResponse.data?.fileUrl || ''  // 行注：更新 groupAvatar 值
      }  // 行注：结束当前代码块
      await groupApi.updateProfile(options.currentTargetId.value, {  // 行注：开始当前逻辑块
        groupName: nextGroupName,  // 行注：设置 groupName 配置项
        groupAvatar  // 行注：补充当前表达式
      })  // 行注：结束当前调用配置
      if (options.groupDetail.value) {  // 行注：判断当前条件是否成立
        options.applyGroupDetail({  // 行注：开始当前逻辑块
          ...options.groupDetail.value,  // 行注：补充当前配置项
          groupName: nextGroupName,  // 行注：设置 groupName 配置项
          groupAvatar  // 行注：传入 groupAvatar 参数
        }, true)  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
      options.message.success('群资料已更新')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitUpdateGroupProfile error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '更新群资料失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      updatingGroupProfile.value = false  // 行注：更新 updatingGroupProfile 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function canOperateMember(member: GroupMember) {  // 行注：定义 canOperateMember 方法
    const myId = String(options.getCurrentUserId())  // 行注：初始化 myId 状态
    if (!options.canManageMembers.value || String(member.userId) === myId) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.currentGroupRole.value === GROUP_ROLE_OWNER) {  // 行注：判断当前条件是否成立
      return member.role !== GROUP_ROLE_OWNER  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return member.role === GROUP_ROLE_MEMBER  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function canToggleAdmin(member: GroupMember) {  // 行注：定义 canToggleAdmin 方法
    return options.currentGroupRole.value === GROUP_ROLE_OWNER  // 行注：返回当前结果
      && String(member.userId) !== String(options.getCurrentUserId())  // 行注：调用 getCurrentUserId 方法
      && member.role !== GROUP_ROLE_OWNER  // 行注：补充当前表达式
  }  // 行注：结束当前代码块

  async function toggleAdminRole(memberItem: GroupMember) {  // 行注：定义异步 toggleAdminRole 方法
    if (!options.currentTargetId.value || !canToggleAdmin(memberItem)) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const nextActionText = memberItem.role === GROUP_ROLE_ADMIN ? '取消该成员的管理员身份' : '将该成员设为管理员'  // 行注：初始化 nextActionText 变量
    const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
      title: '确认成员权限变更',  // 行注：设置 title 配置项
      subtitle: nextActionText,  // 行注：设置 subtitle 配置项
      description: `确认${nextActionText}吗？`,  // 行注：设置 description 配置项
      confirmText: '确认操作'  // 行注：设置 confirmText 配置项
    })  // 行注：结束当前调用配置
    if (!confirmed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      if (memberItem.role === GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
        await groupApi.removeAdmin(options.currentTargetId.value, memberItem.userId)  // 行注：调用 removeAdmin 方法
        options.message.success('已取消管理员')  // 行注：提示成功信息
      } else {  // 行注：开始当前逻辑块
        await groupApi.setAdmin(options.currentTargetId.value, memberItem.userId)  // 行注：调用 setAdmin 方法
        options.message.success('已设为管理员')  // 行注：提示成功信息
      }  // 行注：结束当前代码块
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('toggleAdminRole error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '管理员操作失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function openMuteMemberModal(memberItem: GroupMember) {  // 行注：定义 openMuteMemberModal 方法
    muteTargetMember.value = memberItem  // 行注：更新 muteTargetMember 状态
    muteMinutesInput.value = '10'  // 行注：更新 muteMinutesInput 状态
    showMuteMemberModal.value = true  // 行注：更新 showMuteMemberModal 状态
  }  // 行注：结束当前代码块

  function resetMuteMemberModal() {  // 行注：定义 resetMuteMemberModal 方法
    showMuteMemberModal.value = false  // 行注：更新 showMuteMemberModal 状态
    muteTargetMember.value = null  // 行注：更新 muteTargetMember 状态
    muteMinutesInput.value = '10'  // 行注：更新 muteMinutesInput 状态
  }  // 行注：结束当前代码块

  function closeMuteMemberModal() {  // 行注：定义 closeMuteMemberModal 方法
    if (mutingMember.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    resetMuteMemberModal()  // 行注：调用 resetMuteMemberModal 方法
  }  // 行注：结束当前代码块

  async function toggleMuteMember(memberItem: GroupMember) {  // 行注：定义异步 toggleMuteMember 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      if (memberItem.muted) {  // 行注：判断当前条件是否成立
        await groupApi.unmuteMember(options.currentTargetId.value, memberItem.userId)  // 行注：调用 unmuteMember 方法
        options.message.success('已解除禁言')  // 行注：提示成功信息
      } else {  // 行注：开始当前逻辑块
        openMuteMemberModal(memberItem)  // 行注：调用 openMuteMemberModal 方法
      }  // 行注：结束当前代码块
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('toggleMuteMember error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '操作失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function submitMuteMember() {  // 行注：定义异步 submitMuteMember 方法
    if (!options.currentTargetId.value || !muteTargetMember.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const muteMinutes = Number(muteMinutesInput.value)  // 行注：初始化 muteMinutes 变量
    if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {  // 行注：判断当前条件是否成立
      options.message.warning('请输入 1 到 43200 之间的整数分钟数')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    mutingMember.value = true  // 行注：更新 mutingMember 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.muteMember(options.currentTargetId.value, muteTargetMember.value.userId, muteMinutes)  // 行注：调用 muteMember 方法
      resetMuteMemberModal()  // 行注：调用 resetMuteMemberModal 方法
      options.message.success('已设置禁言')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitMuteMember error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '设置禁言失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      mutingMember.value = false  // 行注：更新 mutingMember 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleRemoveMember(memberItem: GroupMember) {  // 行注：定义异步 handleRemoveMember 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
      title: '确认移出成员',  // 行注：设置 title 配置项
      subtitle: memberItem.nickname || memberItem.username || '该成员',  // 行注：设置 subtitle 配置项
      description: `确认将 ${memberItem.nickname || memberItem.username} 移出群聊吗？`,  // 行注：设置 description 配置项
      confirmText: '确认移出',  // 行注：设置 confirmText 配置项
      confirmType: 'danger'  // 行注：设置 confirmType 配置项
    })  // 行注：结束当前调用配置
    if (!confirmed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.removeMember(options.currentTargetId.value, memberItem.userId)  // 行注：调用 removeMember 方法
      options.message.success('成员已移出群聊')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleRemoveMember error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '移除成员失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleDissolveGroup() {  // 行注：定义异步 handleDissolveGroup 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
      title: '确认解散群聊',  // 行注：设置 title 配置项
      subtitle: '该操作不可恢复',  // 行注：设置 subtitle 配置项
      description: '解散后，当前群聊与会话记录入口将被移除。',  // 行注：设置 description 配置项
      confirmText: '确认解散',  // 行注：设置 confirmText 配置项
      confirmType: 'danger'  // 行注：设置 confirmType 配置项
    })  // 行注：结束当前调用配置
    if (!confirmed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.dissolve(options.currentTargetId.value)  // 行注：调用 dissolve 方法
      await closeGroupDrawer({ force: true })  // 行注：调用 closeGroupDrawer 方法
      options.removeSessionByTarget(options.currentTargetId.value, SESSION_TYPE_GROUP)  // 行注：调用 removeSessionByTarget 方法
      options.resetCurrentConversationState()  // 行注：调用 resetCurrentConversationState 方法
      await options.router.replace('/chat')  // 行注：替换当前路由
      options.message.success('群聊已解散')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleDissolveGroup error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '解散群聊失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleLeaveGroup() {  // 行注：定义异步 handleLeaveGroup 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
      title: '确认退出群聊',  // 行注：设置 title 配置项
      subtitle: options.currentSessionName.value || '当前群聊',  // 行注：设置 subtitle 配置项
      description: '退出后你将不再接收该群消息，需要重新被邀请后才能再次加入。',  // 行注：设置 description 配置项
      confirmText: '确认退出',  // 行注：设置 confirmText 配置项
      confirmType: 'danger'  // 行注：设置 confirmType 配置项
    })  // 行注：结束当前调用配置
    if (!confirmed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.leaveGroup(options.currentTargetId.value)  // 行注：调用 leaveGroup 方法
      await closeGroupDrawer({ force: true })  // 行注：调用 closeGroupDrawer 方法
      options.removeSessionByTarget(options.currentTargetId.value, SESSION_TYPE_GROUP)  // 行注：调用 removeSessionByTarget 方法
      options.resetCurrentConversationState()  // 行注：调用 resetCurrentConversationState 方法
      await options.router.replace('/chat')  // 行注：替换当前路由
      options.message.success('已退出群聊')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleLeaveGroup error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '退出群聊失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function openTransferOwnerModal() {  // 行注：定义 openTransferOwnerModal 方法
    transferOwnerSelection.value = null  // 行注：更新 transferOwnerSelection 状态
    showTransferOwnerModal.value = true  // 行注：更新 showTransferOwnerModal 状态
  }  // 行注：结束当前代码块

  function closeTransferOwnerModal() {  // 行注：定义 closeTransferOwnerModal 方法
    showTransferOwnerModal.value = false  // 行注：更新 showTransferOwnerModal 状态
    transferOwnerSelection.value = null  // 行注：更新 transferOwnerSelection 状态
  }  // 行注：结束当前代码块

  async function submitTransferOwner() {  // 行注：定义异步 submitTransferOwner 方法
    if (!options.currentTargetId.value || !transferOwnerSelection.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const member = options.groupDetail.value?.members.find(item => String(item.userId) === String(transferOwnerSelection.value))  // 行注：初始化 member 变量
    const memberName = member?.nickname || member?.username || '该成员'  // 行注：初始化 memberName 变量
    const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
      title: '确认转让群主',  // 行注：设置 title 配置项
      subtitle: memberName,  // 行注：设置 subtitle 配置项
      description: `确认将群主转让给 ${memberName} 吗？转让后你将变为普通成员。`,  // 行注：设置 description 配置项
      confirmText: '确认转让',  // 行注：设置 confirmText 配置项
      confirmType: 'danger'  // 行注：设置 confirmType 配置项
    })  // 行注：结束当前调用配置
    if (!confirmed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    transferringOwner.value = true  // 行注：更新 transferringOwner 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.transferOwner(options.currentTargetId.value, transferOwnerSelection.value)  // 行注：调用 transferOwner 方法
      closeTransferOwnerModal()  // 行注：调用 closeTransferOwnerModal 方法
      options.message.success('群主已转让')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitTransferOwner error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '转让群主失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      transferringOwner.value = false  // 行注：更新 transferringOwner 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    showCreateGroupModal,  // 行注：传入 showCreateGroupModal 参数
    showAddMembersModal,  // 行注：传入 showAddMembersModal 参数
    creatingGroup,  // 行注：传入 creatingGroup 参数
    addMembersSelection,  // 行注：传入 addMembersSelection 参数
    addMembersMessage,  // 行注：传入 addMembersMessage 参数
    addingMembers,  // 行注：传入 addingMembers 参数
    updatingNotice,  // 行注：传入 updatingNotice 参数
    updatingGroupProfile,  // 行注：传入 updatingGroupProfile 参数
    showTransferOwnerModal,  // 行注：传入 showTransferOwnerModal 参数
    transferOwnerSelection,  // 行注：传入 transferOwnerSelection 参数
    transferringOwner,  // 行注：传入 transferringOwner 参数
    showMuteMemberModal,  // 行注：传入 showMuteMemberModal 参数
    muteTargetMember,  // 行注：传入 muteTargetMember 参数
    muteMinutesInput,  // 行注：传入 muteMinutesInput 参数
    mutingMember,  // 行注：传入 mutingMember 参数
    createGroupForm,  // 行注：传入 createGroupForm 参数
    isGroupProfileChanged,  // 行注：传入 isGroupProfileChanged 参数
    isGroupNoticeChanged,  // 行注：传入 isGroupNoticeChanged 参数
    hasUnsavedGroupDrawerChanges,  // 行注：传入 hasUnsavedGroupDrawerChanges 参数
    availableFriendsForCurrentGroup,  // 行注：传入 availableFriendsForCurrentGroup 参数
    resetCreateGroupForm,  // 行注：传入 resetCreateGroupForm 参数
    resetGroupProfileDraft,  // 行注：传入 resetGroupProfileDraft 参数
    syncGroupProfileDraft,  // 行注：传入 syncGroupProfileDraft 参数
    discardGroupDrawerDrafts,  // 行注：传入 discardGroupDrawerDrafts 参数
    openCreateGroupModal,  // 行注：传入 openCreateGroupModal 参数
    closeCreateGroupModal,  // 行注：传入 closeCreateGroupModal 参数
    triggerGroupAvatarUpload,  // 行注：传入 triggerGroupAvatarUpload 参数
    triggerGroupProfileAvatarUpload,  // 行注：传入 triggerGroupProfileAvatarUpload 参数
    handleGroupAvatarSelected,  // 行注：传入 handleGroupAvatarSelected 参数
    submitCreateGroup,  // 行注：传入 submitCreateGroup 参数
    openGroupDrawer,  // 行注：传入 openGroupDrawer 参数
    closeGroupDrawer,  // 行注：传入 closeGroupDrawer 参数
    openGroupMembersPage,  // 行注：传入 openGroupMembersPage 参数
    openAddMembersModal,  // 行注：传入 openAddMembersModal 参数
    closeAddMembersModal,  // 行注：传入 closeAddMembersModal 参数
    submitAddMembers,  // 行注：传入 submitAddMembers 参数
    submitUpdateNotice,  // 行注：传入 submitUpdateNotice 参数
    submitUpdateGroupProfile,  // 行注：传入 submitUpdateGroupProfile 参数
    canOperateMember,  // 行注：传入 canOperateMember 参数
    canToggleAdmin,  // 行注：传入 canToggleAdmin 参数
    toggleAdminRole,  // 行注：传入 toggleAdminRole 参数
    toggleMuteMember,  // 行注：传入 toggleMuteMember 参数
    openMuteMemberModal,  // 行注：传入 openMuteMemberModal 参数
    closeMuteMemberModal,  // 行注：传入 closeMuteMemberModal 参数
    submitMuteMember,  // 行注：传入 submitMuteMember 参数
    handleRemoveMember,  // 行注：传入 handleRemoveMember 参数
    handleDissolveGroup,  // 行注：传入 handleDissolveGroup 参数
    handleLeaveGroup,  // 行注：传入 handleLeaveGroup 参数
    openTransferOwnerModal,  // 行注：传入 openTransferOwnerModal 参数
    closeTransferOwnerModal,  // 行注：传入 closeTransferOwnerModal 参数
    submitTransferOwner  // 行注：传入 submitTransferOwner 参数
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
