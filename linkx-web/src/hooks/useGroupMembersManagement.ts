/**
 * useGroupMembersManagement 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { ref, type ComputedRef, type Ref } from 'vue'  // 行注：引入 ref, type ComputedRef, type Ref 能力
import type { Router } from 'vue-router'  // 行注：引入 type { Router } 模块
import { fileApi, groupApi } from '../api/client'  // 行注：引入 fileApi, groupApi 能力
import type { ConfirmDialogOptions } from './useConfirmDialog'  // 行注：引入 type { ConfirmDialogOptions } 模块
import {  // 行注：引入 { 模块
  GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
  GROUP_ROLE_MEMBER,  // 行注：补充当前配置项
  GROUP_ROLE_OWNER,  // 行注：补充当前配置项
  type GroupDetail,  // 行注：补充当前配置项
  type GroupMember  // 行注：补充当前表达式
} from '../types/chat'  // 行注：补充当前表达式
import {  // 行注：引入 { 模块
  resetGroupProfileDraftState,  // 行注：补充 resetGroupProfileDraftState 配置项
  syncGroupProfileDraftState,  // 行注：补充当前配置项
  type GroupProfileDraftState  // 行注：补充当前表达式
} from '../utils/group-draft'  // 行注：补充当前表达式

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  warning: (content: string) => void  // 行注：设置 warning 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface UseGroupMembersManagementOptions {  // 行注：开始当前逻辑块
  groupId: ComputedRef<string>  // 行注：设置 groupId 配置项
  groupDetail: Ref<GroupDetail | null>  // 行注：设置 groupDetail 配置项
  currentGroupRole: ComputedRef<number>  // 行注：设置 currentGroupRole 配置项
  canManageMembers: ComputedRef<boolean>  // 行注：设置 canManageMembers 配置项
  canEditGroupProfile: ComputedRef<boolean>  // 行注：设置 canEditGroupProfile 配置项
  canEditNotice: ComputedRef<boolean>  // 行注：设置 canEditNotice 配置项
  isGroupOwner: ComputedRef<boolean>  // 行注：设置 isGroupOwner 配置项
  groupProfileDraft: GroupProfileDraftState  // 行注：设置 groupProfileDraft 配置项
  noticeDraft: Ref<string>  // 行注：设置 noticeDraft 配置项
  groupAvatarInputRef: Ref<HTMLInputElement | undefined>  // 行注：设置 groupAvatarInputRef 配置项
  getCurrentUserId: () => string | number  // 行注：传入 getCurrentUserId 回调
  message: FeedbackApi  // 行注：设置 message 配置项
  router: Router  // 行注：设置 router 配置项
  openConfirmDialog: (options: ConfirmDialogOptions) => Promise<boolean>  // 行注：设置 openConfirmDialog 配置项
  loadGroupDetail: () => Promise<boolean>  // 行注：传入 loadGroupDetail 回调
  applyGroupDetail: (detail: GroupDetail | null, syncDraft?: boolean) => void  // 行注：设置 applyGroupDetail 配置项
}  // 行注：结束当前代码块

export function useGroupMembersManagement(options: UseGroupMembersManagementOptions) {  // 行注：导出当前能力
  const actionLoading = ref(false)  // 行注：初始化 actionLoading 响应式状态
  const updatingGroupProfile = ref(false)  // 行注：初始化 updatingGroupProfile 响应式状态
  const updatingNotice = ref(false)  // 行注：初始化 updatingNotice 响应式状态
  const showAddMembersModal = ref(false)  // 行注：初始化 showAddMembersModal 响应式状态
  const addMembersSelection = ref<Array<string | number>>([])  // 行注：初始化 addMembersSelection 变量
  const addMembersMessage = ref('')  // 行注：初始化 addMembersMessage 响应式状态
  const addingMembers = ref(false)  // 行注：初始化 addingMembers 响应式状态
  const showTransferOwnerModal = ref(false)  // 行注：初始化 showTransferOwnerModal 响应式状态
  const transferOwnerSelection = ref<string | number | null>(null)  // 行注：初始化 transferOwnerSelection 变量
  const transferringOwner = ref(false)  // 行注：初始化 transferringOwner 响应式状态
  const showMuteMemberModal = ref(false)  // 行注：初始化 showMuteMemberModal 响应式状态
  const muteTargetMember = ref<GroupMember | null>(null)  // 行注：初始化 muteTargetMember 变量
  const muteMinutesInput = ref('10')  // 行注：初始化 muteMinutesInput 响应式状态

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

  function discardGroupProfileDrafts() {  // 行注：定义 discardGroupProfileDrafts 方法
    options.noticeDraft.value = options.groupDetail.value?.notice || ''  // 行注：更新 options.noticeDraft 状态
    syncGroupProfileDraft(options.groupDetail.value)  // 行注：调用 syncGroupProfileDraft 方法
  }  // 行注：结束当前代码块

  function triggerGroupAvatarUpload() {  // 行注：定义 triggerGroupAvatarUpload 方法
    if (!options.canEditGroupProfile.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    options.groupAvatarInputRef.value?.click()  // 行注：调用 click 方法
  }  // 行注：结束当前代码块

  function handleGroupAvatarSelected(event: Event) {  // 行注：定义 handleGroupAvatarSelected 方法
    if (!options.canEditGroupProfile.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const input = event.target as HTMLInputElement  // 行注：初始化 input 变量
    const file = input.files?.[0]  // 行注：初始化 file 变量
    if (!file) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const previewUrl = URL.createObjectURL(file)  // 行注：初始化 previewUrl 变量
    if (options.groupProfileDraft.avatarPreview.startsWith('blob:')) {  // 行注：判断当前条件是否成立
      URL.revokeObjectURL(options.groupProfileDraft.avatarPreview)  // 行注：调用 revokeObjectURL 方法
    }  // 行注：结束当前代码块
    options.groupProfileDraft.avatarFile = file  // 行注：更新 options.groupProfileDraft.avatarFile 值
    options.groupProfileDraft.avatarPreview = previewUrl  // 行注：更新 options.groupProfileDraft.avatarPreview 值
  }  // 行注：结束当前代码块

  async function submitUpdateNotice() {  // 行注：定义异步 submitUpdateNotice 方法
    if (!options.groupId.value || !options.canEditNotice.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    updatingNotice.value = true  // 行注：更新 updatingNotice 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.updateNotice(options.groupId.value, options.noticeDraft.value.trim())  // 行注：调用 updateNotice 方法
      await options.loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
      options.message.success('群公告已更新')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitUpdateNotice error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '更新群公告失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      updatingNotice.value = false  // 行注：更新 updatingNotice 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function submitUpdateGroupProfile() {  // 行注：定义异步 submitUpdateGroupProfile 方法
    if (!options.groupId.value || !options.groupDetail.value || !options.canEditGroupProfile.value) {  // 行注：判断当前条件是否成立
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
      await groupApi.updateProfile(options.groupId.value, {  // 行注：开始当前逻辑块
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

  function openAddMembersModal() {  // 行注：定义 openAddMembersModal 方法
    if (!options.canManageMembers.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    addMembersSelection.value = []  // 行注：更新 addMembersSelection 状态
    addMembersMessage.value = ''  // 行注：更新 addMembersMessage 状态
    showAddMembersModal.value = true  // 行注：更新 showAddMembersModal 状态
  }  // 行注：结束当前代码块

  function closeAddMembersModal() {  // 行注：定义 closeAddMembersModal 方法
    if (addingMembers.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    showAddMembersModal.value = false  // 行注：更新 showAddMembersModal 状态
    addMembersSelection.value = []  // 行注：更新 addMembersSelection 状态
    addMembersMessage.value = ''  // 行注：更新 addMembersMessage 状态
  }  // 行注：结束当前代码块

  function resetAddMembersModal() {  // 行注：定义 resetAddMembersModal 方法
    showAddMembersModal.value = false  // 行注：更新 showAddMembersModal 状态
    addMembersSelection.value = []  // 行注：更新 addMembersSelection 状态
    addMembersMessage.value = ''  // 行注：更新 addMembersMessage 状态
  }  // 行注：结束当前代码块

  async function submitAddMembers() {  // 行注：定义异步 submitAddMembers 方法
    if (!options.groupId.value || addMembersSelection.value.length === 0) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    addingMembers.value = true  // 行注：更新 addingMembers 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.inviteMembers(options.groupId.value, addMembersSelection.value, addMembersMessage.value.trim())  // 行注：调用 inviteMembers 方法
      resetAddMembersModal()  // 行注：调用 resetAddMembersModal 方法
      options.message.success('邀请已发送')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitAddMembers error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '邀请成员失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      addingMembers.value = false  // 行注：更新 addingMembers 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function openTransferOwnerModal() {  // 行注：定义 openTransferOwnerModal 方法
    if (!options.isGroupOwner.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    transferOwnerSelection.value = null  // 行注：更新 transferOwnerSelection 状态
    showTransferOwnerModal.value = true  // 行注：更新 showTransferOwnerModal 状态
  }  // 行注：结束当前代码块

  function closeTransferOwnerModal() {  // 行注：定义 closeTransferOwnerModal 方法
    if (transferringOwner.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    showTransferOwnerModal.value = false  // 行注：更新 showTransferOwnerModal 状态
    transferOwnerSelection.value = null  // 行注：更新 transferOwnerSelection 状态
  }  // 行注：结束当前代码块

  function resetTransferOwnerModal() {  // 行注：定义 resetTransferOwnerModal 方法
    showTransferOwnerModal.value = false  // 行注：更新 showTransferOwnerModal 状态
    transferOwnerSelection.value = null  // 行注：更新 transferOwnerSelection 状态
  }  // 行注：结束当前代码块

  async function submitTransferOwner() {  // 行注：定义异步 submitTransferOwner 方法
    if (!options.groupId.value || !transferOwnerSelection.value) {  // 行注：判断当前条件是否成立
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
      await groupApi.transferOwner(options.groupId.value, transferOwnerSelection.value)  // 行注：调用 transferOwner 方法
      resetTransferOwnerModal()  // 行注：调用 resetTransferOwnerModal 方法
      await options.loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
      options.message.success('群主已转让')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitTransferOwner error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '转让群主失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      transferringOwner.value = false  // 行注：更新 transferringOwner 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function toggleAdminRole(memberItem: GroupMember) {  // 行注：定义异步 toggleAdminRole 方法
    if (!options.groupDetail.value || !canToggleAdmin(memberItem)) {  // 行注：判断当前条件是否成立
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
    actionLoading.value = true  // 行注：更新 actionLoading 状态
    try {  // 行注：尝试执行可能失败的逻辑
      if (memberItem.role === GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
        await groupApi.removeAdmin(options.groupDetail.value.id, memberItem.userId)  // 行注：调用 removeAdmin 方法
        options.message.success('已取消管理员')  // 行注：提示成功信息
      } else {  // 行注：开始当前逻辑块
        await groupApi.setAdmin(options.groupDetail.value.id, memberItem.userId)  // 行注：调用 setAdmin 方法
        options.message.success('已设为管理员')  // 行注：提示成功信息
      }  // 行注：结束当前代码块
      await options.loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('toggleAdminRole error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '管理员操作失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      actionLoading.value = false  // 行注：更新 actionLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function openMuteMemberModal(memberItem: GroupMember) {  // 行注：定义 openMuteMemberModal 方法
    muteTargetMember.value = memberItem  // 行注：更新 muteTargetMember 状态
    muteMinutesInput.value = '10'  // 行注：更新 muteMinutesInput 状态
    showMuteMemberModal.value = true  // 行注：更新 showMuteMemberModal 状态
  }  // 行注：结束当前代码块

  function closeMuteMemberModal() {  // 行注：定义 closeMuteMemberModal 方法
    if (actionLoading.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    muteTargetMember.value = null  // 行注：更新 muteTargetMember 状态
    muteMinutesInput.value = '10'  // 行注：更新 muteMinutesInput 状态
    showMuteMemberModal.value = false  // 行注：更新 showMuteMemberModal 状态
  }  // 行注：结束当前代码块

  async function toggleMuteMember(memberItem: GroupMember) {  // 行注：定义异步 toggleMuteMember 方法
    if (!options.groupDetail.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (memberItem.muted) {  // 行注：判断当前条件是否成立
      actionLoading.value = true  // 行注：更新 actionLoading 状态
      try {  // 行注：尝试执行可能失败的逻辑
        await groupApi.unmuteMember(options.groupDetail.value.id, memberItem.userId)  // 行注：调用 unmuteMember 方法
        options.message.success('已解除禁言')  // 行注：提示成功信息
        await options.loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
      } catch (error: any) {  // 行注：捕获并处理异常
        console.error('toggleMuteMember error:', error)  // 行注：输出错误日志
        options.message.error(error.response?.data?.message || '操作失败')  // 行注：提示错误信息
      } finally {  // 行注：执行收尾清理逻辑
        actionLoading.value = false  // 行注：更新 actionLoading 状态
      }  // 行注：结束当前代码块
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    openMuteMemberModal(memberItem)  // 行注：调用 openMuteMemberModal 方法
  }  // 行注：结束当前代码块

  async function submitMuteMember() {  // 行注：定义异步 submitMuteMember 方法
    if (!options.groupDetail.value || !muteTargetMember.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const muteMinutes = Number(muteMinutesInput.value)  // 行注：初始化 muteMinutes 变量
    if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {  // 行注：判断当前条件是否成立
      options.message.warning('请输入 1 到 43200 之间的整数分钟数')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    actionLoading.value = true  // 行注：更新 actionLoading 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.muteMember(options.groupDetail.value.id, muteTargetMember.value.userId, muteMinutes)  // 行注：调用 muteMember 方法
      options.message.success('已设置禁言')  // 行注：提示成功信息
      closeMuteMemberModal()  // 行注：调用 closeMuteMemberModal 方法
      await options.loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('submitMuteMember error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '设置禁言失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      actionLoading.value = false  // 行注：更新 actionLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleRemoveMember(memberItem: GroupMember) {  // 行注：定义异步 handleRemoveMember 方法
    if (!options.groupDetail.value) {  // 行注：判断当前条件是否成立
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
    actionLoading.value = true  // 行注：更新 actionLoading 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.removeMember(options.groupDetail.value.id, memberItem.userId)  // 行注：调用 removeMember 方法
      options.message.success('成员已移出群聊')  // 行注：提示成功信息
      await options.loadGroupDetail()  // 行注：调用 loadGroupDetail 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleRemoveMember error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '移除成员失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      actionLoading.value = false  // 行注：更新 actionLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleDissolveGroup() {  // 行注：定义异步 handleDissolveGroup 方法
    if (!options.groupId.value) {  // 行注：判断当前条件是否成立
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
      await groupApi.dissolve(options.groupId.value)  // 行注：调用 dissolve 方法
      options.message.success('群聊已解散')  // 行注：提示成功信息
      await options.router.replace('/chat')  // 行注：替换当前路由
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleDissolveGroup error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '解散群聊失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleLeaveGroup() {  // 行注：定义异步 handleLeaveGroup 方法
    if (!options.groupId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const confirmed = await options.openConfirmDialog({  // 行注：开始解构当前返回值
      title: '确认退出群聊',  // 行注：设置 title 配置项
      subtitle: options.groupDetail.value?.groupName || '当前群聊',  // 行注：设置 subtitle 配置项
      description: '退出后你将不再接收该群消息，需要重新被邀请才能再次加入。',  // 行注：设置 description 配置项
      confirmText: '确认退出',  // 行注：设置 confirmText 配置项
      confirmType: 'danger'  // 行注：设置 confirmType 配置项
    })  // 行注：结束当前调用配置
    if (!confirmed) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.leaveGroup(options.groupId.value)  // 行注：调用 leaveGroup 方法
      options.message.success('已退出群聊')  // 行注：提示成功信息
      await options.router.replace('/chat')  // 行注：替换当前路由
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleLeaveGroup error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '退出群聊失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    actionLoading,  // 行注：传入 actionLoading 参数
    updatingGroupProfile,  // 行注：传入 updatingGroupProfile 参数
    updatingNotice,  // 行注：传入 updatingNotice 参数
    showAddMembersModal,  // 行注：传入 showAddMembersModal 参数
    addMembersSelection,  // 行注：传入 addMembersSelection 参数
    addMembersMessage,  // 行注：传入 addMembersMessage 参数
    addingMembers,  // 行注：传入 addingMembers 参数
    showTransferOwnerModal,  // 行注：传入 showTransferOwnerModal 参数
    transferOwnerSelection,  // 行注：传入 transferOwnerSelection 参数
    transferringOwner,  // 行注：传入 transferringOwner 参数
    showMuteMemberModal,  // 行注：传入 showMuteMemberModal 参数
    muteTargetMember,  // 行注：传入 muteTargetMember 参数
    muteMinutesInput,  // 行注：传入 muteMinutesInput 参数
    canOperateMember,  // 行注：传入 canOperateMember 参数
    canToggleAdmin,  // 行注：传入 canToggleAdmin 参数
    resetGroupProfileDraft,  // 行注：传入 resetGroupProfileDraft 参数
    syncGroupProfileDraft,  // 行注：传入 syncGroupProfileDraft 参数
    discardGroupProfileDrafts,  // 行注：传入 discardGroupProfileDrafts 参数
    triggerGroupAvatarUpload,  // 行注：传入 triggerGroupAvatarUpload 参数
    handleGroupAvatarSelected,  // 行注：传入 handleGroupAvatarSelected 参数
    submitUpdateNotice,  // 行注：传入 submitUpdateNotice 参数
    submitUpdateGroupProfile,  // 行注：传入 submitUpdateGroupProfile 参数
    openAddMembersModal,  // 行注：传入 openAddMembersModal 参数
    closeAddMembersModal,  // 行注：传入 closeAddMembersModal 参数
    submitAddMembers,  // 行注：传入 submitAddMembers 参数
    openTransferOwnerModal,  // 行注：传入 openTransferOwnerModal 参数
    closeTransferOwnerModal,  // 行注：传入 closeTransferOwnerModal 参数
    submitTransferOwner,  // 行注：传入 submitTransferOwner 参数
    toggleAdminRole,  // 行注：传入 toggleAdminRole 参数
    toggleMuteMember,  // 行注：传入 toggleMuteMember 参数
    closeMuteMemberModal,  // 行注：传入 closeMuteMemberModal 参数
    submitMuteMember,  // 行注：传入 submitMuteMember 参数
    handleRemoveMember,  // 行注：传入 handleRemoveMember 参数
    handleDissolveGroup,  // 行注：传入 handleDissolveGroup 参数
    handleLeaveGroup  // 行注：传入 handleLeaveGroup 参数
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
