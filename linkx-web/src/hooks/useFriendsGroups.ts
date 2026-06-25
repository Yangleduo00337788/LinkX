/**
 * useFriendsGroups 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { ref, type Ref } from 'vue'  // 行注：引入 ref, type Ref 能力
import type { Router } from 'vue-router'  // 行注：引入 type { Router } 模块
import { groupApi } from '../api/client'  // 行注：引入 groupApi 能力
import { GROUP_ROLE_ADMIN, GROUP_ROLE_OWNER } from '../types/chat'  // 行注：引入 GROUP_ROLE_ADMIN, GROUP_ROLE_OWNER 能力

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  info: (content: string) => void  // 行注：设置 info 配置项
  warning: (content: string) => void  // 行注：设置 warning 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface GroupListItem {  // 行注：开始当前逻辑块
  id: string | number  // 行注：设置 id 配置项
  groupName: string  // 行注：设置 groupName 配置项
  groupAvatar?: string  // 行注：补充当前表达式
  notice?: string  // 行注：补充当前表达式
  memberCount?: number  // 行注：补充当前表达式
  myRole?: number  // 行注：补充当前表达式
  createTime?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface GroupSearchResultItem extends GroupListItem {}  // 行注：补充当前表达式

interface GroupPreviewMember {  // 行注：开始当前逻辑块
  userId: string | number  // 行注：设置 userId 配置项
  username?: string  // 行注：补充当前表达式
  nickname?: string  // 行注：补充当前表达式
  avatar?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface GroupPreviewDetail extends GroupListItem {  // 行注：开始当前逻辑块
  maxMembers?: number  // 行注：补充当前表达式
  members: GroupPreviewMember[]  // 行注：设置 members 配置项
}  // 行注：结束当前代码块

interface UseFriendsGroupsOptions {  // 行注：开始当前逻辑块
  activeTab: Ref<string>  // 行注：设置 activeTab 配置项
  router: Router  // 行注：设置 router 配置项
  message: FeedbackApi  // 行注：设置 message 配置项
}  // 行注：结束当前代码块

export function useFriendsGroups(options: UseFriendsGroupsOptions) {  // 行注：导出当前能力
  const joinGroupId = ref('')  // 行注：初始化 joinGroupId 响应式状态
  const joinGroupMessage = ref('')  // 行注：初始化 joinGroupMessage 响应式状态
  const joiningGroup = ref(false)  // 行注：初始化 joiningGroup 响应式状态
  const groupSearchKeyword = ref('')  // 行注：初始化 groupSearchKeyword 响应式状态
  const searchingGroup = ref(false)  // 行注：初始化 searchingGroup 响应式状态
  const groupSearchResults = ref<GroupSearchResultItem[]>([])  // 行注：初始化 groupSearchResults 集合
  const myGroups = ref<GroupListItem[]>([])  // 行注：初始化 myGroups 集合
  const showGroupPreview = ref(false)  // 行注：初始化 showGroupPreview 响应式状态
  const previewGroupDetail = ref<GroupPreviewDetail | null>(null)  // 行注：初始化 previewGroupDetail 变量
  const pendingJoinGroupIds = ref<string[]>([])  // 行注：初始化 pendingJoinGroupIds 变量
  const previewGroupId = ref<string | null>(null)  // 行注：初始化 previewGroupId 状态

  function normalizeGroupId(groupId: string | number) {  // 行注：定义 normalizeGroupId 方法
    return String(groupId)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function hasPendingJoinRequest(groupId: string | number) {  // 行注：定义 hasPendingJoinRequest 方法
    return pendingJoinGroupIds.value.includes(normalizeGroupId(groupId))  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function markJoinRequestPending(groupId: string | number) {  // 行注：定义 markJoinRequestPending 方法
    const normalizedGroupId = normalizeGroupId(groupId)  // 行注：初始化 normalizedGroupId 状态
    if (pendingJoinGroupIds.value.includes(normalizedGroupId)) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    pendingJoinGroupIds.value = [...pendingJoinGroupIds.value, normalizedGroupId]  // 行注：更新 pendingJoinGroupIds 状态
  }  // 行注：结束当前代码块

  function clearPendingJoinRequest(groupId: string | number) {  // 行注：定义 clearPendingJoinRequest 方法
    const normalizedGroupId = normalizeGroupId(groupId)  // 行注：初始化 normalizedGroupId 状态
    pendingJoinGroupIds.value = pendingJoinGroupIds.value.filter(id => id !== normalizedGroupId)  // 行注：调用 filter 方法
  }  // 行注：结束当前代码块

  function isJoinedGroup(groupId: string | number, role?: number) {  // 行注：定义 isJoinedGroup 方法
    if (role !== null && role !== undefined) {  // 行注：判断当前条件是否成立
      return true  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const normalizedGroupId = normalizeGroupId(groupId)  // 行注：初始化 normalizedGroupId 状态
    return myGroups.value.some(group => normalizeGroupId(group.id) === normalizedGroupId)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function syncGroupAccessState() {  // 行注：定义 syncGroupAccessState 方法
    const membershipRoleMap = new Map<string, number | undefined>(  // 行注：初始化 membershipRoleMap 变量
      myGroups.value.map(group => [normalizeGroupId(group.id), group.myRole])  // 行注：调用 map 方法
    )  // 行注：结束当前调用

    if (pendingJoinGroupIds.value.length > 0) {  // 行注：判断当前条件是否成立
      pendingJoinGroupIds.value = pendingJoinGroupIds.value.filter(groupId => !membershipRoleMap.has(groupId))  // 行注：调用 filter 方法
    }  // 行注：结束当前代码块

    if (groupSearchResults.value.length > 0) {  // 行注：判断当前条件是否成立
      groupSearchResults.value = groupSearchResults.value.map(group => ({  // 行注：开始当前逻辑块
        ...group,  // 行注：补充当前配置项
        myRole: membershipRoleMap.get(normalizeGroupId(group.id))  // 行注：设置 myRole 配置项
      }))  // 行注：补充当前表达式
    }  // 行注：结束当前代码块

    if (previewGroupDetail.value) {  // 行注：判断当前条件是否成立
      previewGroupDetail.value = {  // 行注：更新 previewGroupDetail 状态
        ...previewGroupDetail.value,  // 行注：补充当前配置项
        myRole: membershipRoleMap.get(normalizeGroupId(previewGroupDetail.value.id))  // 行注：设置 myRole 配置项
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function loadMyGroups() {  // 行注：定义异步 loadMyGroups 方法
    const response: any = await groupApi.mine()  // 行注：接收 response 异步结果
    myGroups.value = response.data || []  // 行注：更新 myGroups 状态
    syncGroupAccessState()  // 行注：调用 syncGroupAccessState 方法
  }  // 行注：结束当前代码块

  async function handleJoinGroup() {  // 行注：定义异步 handleJoinGroup 方法
    const normalizedGroupId = joinGroupId.value.trim()  // 行注：初始化 normalizedGroupId 状态
    if (!normalizedGroupId) {  // 行注：判断当前条件是否成立
      options.message.warning('请输入群 ID')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (!/^\d+$/.test(normalizedGroupId)) {  // 行注：判断当前条件是否成立
      options.message.warning('群 ID 仅支持数字')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (hasPendingJoinRequest(normalizedGroupId)) {  // 行注：判断当前条件是否成立
      options.message.info('该群申请已提交，请等待处理')  // 行注：调用 info 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    joiningGroup.value = true  // 行注：更新 joiningGroup 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.joinRequest(normalizedGroupId, joinGroupMessage.value.trim())  // 行注：调用 joinRequest 方法
      markJoinRequestPending(normalizedGroupId)  // 行注：调用 markJoinRequestPending 方法
      joinGroupId.value = ''  // 行注：更新 joinGroupId 状态
      joinGroupMessage.value = ''  // 行注：更新 joinGroupMessage 状态
      options.message.success('入群申请已提交')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.response?.data?.message || '提交入群申请失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      joiningGroup.value = false  // 行注：更新 joiningGroup 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleGroupSearch() {  // 行注：定义异步 handleGroupSearch 方法
    const keyword = groupSearchKeyword.value.trim()  // 行注：初始化 keyword 状态
    if (!keyword) {  // 行注：判断当前条件是否成立
      groupSearchResults.value = []  // 行注：更新 groupSearchResults 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    searchingGroup.value = true  // 行注：更新 searchingGroup 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.search(keyword)  // 行注：接收 response 异步结果
      groupSearchResults.value = response.data || []  // 行注：更新 groupSearchResults 状态
      syncGroupAccessState()  // 行注：调用 syncGroupAccessState 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.response?.data?.message || '搜索群聊失败')  // 行注：提示错误信息
      groupSearchResults.value = []  // 行注：更新 groupSearchResults 状态
    } finally {  // 行注：执行收尾清理逻辑
      searchingGroup.value = false  // 行注：更新 searchingGroup 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function clearGroupSearch() {  // 行注：定义 clearGroupSearch 方法
    groupSearchKeyword.value = ''  // 行注：更新 groupSearchKeyword 状态
    groupSearchResults.value = []  // 行注：更新 groupSearchResults 状态
  }  // 行注：结束当前代码块

  function closeGroupPreview() {  // 行注：定义 closeGroupPreview 方法
    showGroupPreview.value = false  // 行注：更新 showGroupPreview 状态
    previewGroupDetail.value = null  // 行注：更新 previewGroupDetail 状态
    previewGroupId.value = null  // 行注：更新 previewGroupId 状态
  }  // 行注：结束当前代码块

  function openJoinGroupModal(group: Pick<GroupListItem, 'id' | 'groupName'>) {  // 行注：定义 openJoinGroupModal 方法
    options.activeTab.value = 'join-group'  // 行注：更新 options.activeTab 状态
    joinGroupId.value = String(group.id)  // 行注：更新 joinGroupId 状态
    joinGroupMessage.value = ''  // 行注：更新 joinGroupMessage 状态
    showGroupPreview.value = false  // 行注：更新 showGroupPreview 状态
    previewGroupId.value = null  // 行注：更新 previewGroupId 状态
    options.message.info(`申请加入「${group.groupName}」，请填写申请说明后发送`)  // 行注：调用 info 方法
  }  // 行注：结束当前代码块

  function applyJoinFromPreview(group: GroupPreviewDetail) {  // 行注：定义 applyJoinFromPreview 方法
    openJoinGroupModal(group)  // 行注：调用 openJoinGroupModal 方法
    previewGroupDetail.value = null  // 行注：更新 previewGroupDetail 状态
  }  // 行注：结束当前代码块

  function groupRoleText(role?: number) {  // 行注：定义 groupRoleText 方法
    if (role === GROUP_ROLE_OWNER) {  // 行注：判断当前条件是否成立
      return '群主'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (role === GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
      return '管理员'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return '成员'  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function groupRoleClass(role?: number) {  // 行注：定义 groupRoleClass 方法
    if (role === GROUP_ROLE_OWNER) {  // 行注：判断当前条件是否成立
      return 'owner'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (role === GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
      return 'admin'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return 'member'  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function startGroupChat(groupId: string | number) {  // 行注：定义 startGroupChat 方法
    options.router.push({  // 行注：开始当前逻辑块
      path: `/chat/${groupId}`,  // 行注：设置 path 配置项
      query: { sessionType: '2' }  // 行注：设置 query 配置项
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  async function copyGroupId(groupId: string | number) {  // 行注：定义异步 copyGroupId 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await navigator.clipboard.writeText(String(groupId))  // 行注：写入剪贴板
      options.message.success(`群号 ${groupId} 已复制`)  // 行注：提示成功信息
    } catch {  // 行注：捕获并处理异常
      options.message.error('复制群号失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function isUnavailableGroupError(error: any) {  // 行注：定义 isUnavailableGroupError 方法
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)  // 行注：初始化 code 变量
    return code === 403 || code === 404  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function refreshGroupPreview(groupId: string | number, config: { silent?: boolean } = {}) {  // 行注：定义异步 refreshGroupPreview 方法
    const normalizedGroupId = String(groupId)  // 行注：初始化 normalizedGroupId 状态
    previewGroupId.value = normalizedGroupId  // 行注：更新 previewGroupId 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.detail(normalizedGroupId)  // 行注：接收 response 异步结果
      if (previewGroupId.value !== normalizedGroupId) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      previewGroupDetail.value = response.data || null  // 行注：更新 previewGroupDetail 状态
      if (response.data?.myRole !== null && response.data?.myRole !== undefined) {  // 行注：判断当前条件是否成立
        clearPendingJoinRequest(normalizedGroupId)  // 行注：调用 clearPendingJoinRequest 方法
      }  // 行注：结束当前代码块
    } catch (error: any) {  // 行注：捕获并处理异常
      if (previewGroupId.value !== normalizedGroupId) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (isUnavailableGroupError(error)) {  // 行注：判断当前条件是否成立
        closeGroupPreview()  // 行注：调用 closeGroupPreview 方法
        if (config.silent) {  // 行注：判断当前条件是否成立
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        options.message.warning(error.response?.data?.message || '当前群聊资料已失效')  // 行注：提示警告信息
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (config.silent) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      closeGroupPreview()  // 行注：调用 closeGroupPreview 方法
      options.message.error(error.response?.data?.message || '获取群资料失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function openGroupPreview(group: Pick<GroupListItem, 'id'>) {  // 行注：定义异步 openGroupPreview 方法
    showGroupPreview.value = true  // 行注：更新 showGroupPreview 状态
    previewGroupDetail.value = null  // 行注：更新 previewGroupDetail 状态
    await refreshGroupPreview(group.id)  // 行注：调用 refreshGroupPreview 方法
  }  // 行注：结束当前代码块

  async function refreshOpenGroupPreview() {  // 行注：定义异步 refreshOpenGroupPreview 方法
    if (!showGroupPreview.value || !previewGroupId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    await refreshGroupPreview(previewGroupId.value, { silent: true })  // 行注：调用 refreshGroupPreview 方法
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    joinGroupId,  // 行注：传入 joinGroupId 参数
    joinGroupMessage,  // 行注：传入 joinGroupMessage 参数
    joiningGroup,  // 行注：传入 joiningGroup 参数
    groupSearchKeyword,  // 行注：传入 groupSearchKeyword 参数
    searchingGroup,  // 行注：传入 searchingGroup 参数
    groupSearchResults,  // 行注：传入 groupSearchResults 参数
    myGroups,  // 行注：传入 myGroups 参数
    showGroupPreview,  // 行注：传入 showGroupPreview 参数
    previewGroupDetail,  // 行注：传入 previewGroupDetail 参数
    hasPendingJoinRequest,  // 行注：传入 hasPendingJoinRequest 参数
    isJoinedGroup,  // 行注：传入 isJoinedGroup 参数
    loadMyGroups,  // 行注：传入 loadMyGroups 参数
    handleJoinGroup,  // 行注：传入 handleJoinGroup 参数
    handleGroupSearch,  // 行注：传入 handleGroupSearch 参数
    clearGroupSearch,  // 行注：传入 clearGroupSearch 参数
    openJoinGroupModal,  // 行注：传入 openJoinGroupModal 参数
    applyJoinFromPreview,  // 行注：传入 applyJoinFromPreview 参数
    groupRoleText,  // 行注：传入 groupRoleText 参数
    groupRoleClass,  // 行注：传入 groupRoleClass 参数
    startGroupChat,  // 行注：传入 startGroupChat 参数
    copyGroupId,  // 行注：传入 copyGroupId 参数
    openGroupPreview,  // 行注：传入 openGroupPreview 参数
    refreshOpenGroupPreview,  // 行注：传入 refreshOpenGroupPreview 参数
    closeGroupPreview  // 行注：传入 closeGroupPreview 参数
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
