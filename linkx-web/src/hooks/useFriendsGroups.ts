import { ref, type Ref } from 'vue'
import type { Router } from 'vue-router'
import { groupApi } from '../api/client'
import { GROUP_ROLE_ADMIN, GROUP_ROLE_OWNER } from '../types/chat'

interface FeedbackApi {
  success: (content: string) => void
  info: (content: string) => void
  warning: (content: string) => void
  error: (content: string) => void
}

interface GroupListItem {
  id: string | number
  groupName: string
  groupAvatar?: string
  notice?: string
  memberCount?: number
  myRole?: number
  createTime?: string
}

interface GroupSearchResultItem extends GroupListItem {}

interface GroupPreviewMember {
  userId: string | number
  username?: string
  nickname?: string
  avatar?: string
}

interface GroupPreviewDetail extends GroupListItem {
  maxMembers?: number
  members: GroupPreviewMember[]
}

interface UseFriendsGroupsOptions {
  activeTab: Ref<string>
  router: Router
  message: FeedbackApi
}

export function useFriendsGroups(options: UseFriendsGroupsOptions) {
  const joinGroupId = ref('')
  const joinGroupMessage = ref('')
  const joiningGroup = ref(false)
  const groupSearchKeyword = ref('')
  const searchingGroup = ref(false)
  const groupSearchResults = ref<GroupSearchResultItem[]>([])
  const myGroups = ref<GroupListItem[]>([])
  const showGroupPreview = ref(false)
  const previewGroupDetail = ref<GroupPreviewDetail | null>(null)
  const pendingJoinGroupIds = ref<string[]>([])
  const previewGroupId = ref<string | null>(null)

  function normalizeGroupId(groupId: string | number) {
    return String(groupId)
  }

  function hasPendingJoinRequest(groupId: string | number) {
    return pendingJoinGroupIds.value.includes(normalizeGroupId(groupId))
  }

  function markJoinRequestPending(groupId: string | number) {
    const normalizedGroupId = normalizeGroupId(groupId)
    if (pendingJoinGroupIds.value.includes(normalizedGroupId)) {
      return
    }
    pendingJoinGroupIds.value = [...pendingJoinGroupIds.value, normalizedGroupId]
  }

  function clearPendingJoinRequest(groupId: string | number) {
    const normalizedGroupId = normalizeGroupId(groupId)
    pendingJoinGroupIds.value = pendingJoinGroupIds.value.filter(id => id !== normalizedGroupId)
  }

  function isJoinedGroup(groupId: string | number, role?: number) {
    if (role !== null && role !== undefined) {
      return true
    }
    const normalizedGroupId = normalizeGroupId(groupId)
    return myGroups.value.some(group => normalizeGroupId(group.id) === normalizedGroupId)
  }

  function syncGroupAccessState() {
    const membershipRoleMap = new Map<string, number | undefined>(
      myGroups.value.map(group => [normalizeGroupId(group.id), group.myRole])
    )

    if (pendingJoinGroupIds.value.length > 0) {
      pendingJoinGroupIds.value = pendingJoinGroupIds.value.filter(groupId => !membershipRoleMap.has(groupId))
    }

    if (groupSearchResults.value.length > 0) {
      groupSearchResults.value = groupSearchResults.value.map(group => ({
        ...group,
        myRole: membershipRoleMap.get(normalizeGroupId(group.id))
      }))
    }

    if (previewGroupDetail.value) {
      previewGroupDetail.value = {
        ...previewGroupDetail.value,
        myRole: membershipRoleMap.get(normalizeGroupId(previewGroupDetail.value.id))
      }
    }
  }

  async function loadMyGroups() {
    const response: any = await groupApi.mine()
    myGroups.value = response.data || []
    syncGroupAccessState()
  }

  async function handleJoinGroup() {
    const normalizedGroupId = joinGroupId.value.trim()
    if (!normalizedGroupId) {
      options.message.warning('请输入群 ID')
      return
    }
    if (!/^\d+$/.test(normalizedGroupId)) {
      options.message.warning('群 ID 仅支持数字')
      return
    }
    if (hasPendingJoinRequest(normalizedGroupId)) {
      options.message.info('该群申请已提交，请等待处理')
      return
    }

    joiningGroup.value = true
    try {
      await groupApi.joinRequest(normalizedGroupId, joinGroupMessage.value.trim())
      markJoinRequestPending(normalizedGroupId)
      joinGroupId.value = ''
      joinGroupMessage.value = ''
      options.message.success('入群申请已提交')
    } catch (error: any) {
      options.message.error(error.response?.data?.message || '提交入群申请失败')
    } finally {
      joiningGroup.value = false
    }
  }

  async function handleGroupSearch() {
    const keyword = groupSearchKeyword.value.trim()
    if (!keyword) {
      groupSearchResults.value = []
      return
    }
    searchingGroup.value = true
    try {
      const response: any = await groupApi.search(keyword)
      groupSearchResults.value = response.data || []
      syncGroupAccessState()
    } catch (error: any) {
      options.message.error(error.response?.data?.message || '搜索群聊失败')
      groupSearchResults.value = []
    } finally {
      searchingGroup.value = false
    }
  }

  function clearGroupSearch() {
    groupSearchKeyword.value = ''
    groupSearchResults.value = []
  }

  function closeGroupPreview() {
    showGroupPreview.value = false
    previewGroupDetail.value = null
    previewGroupId.value = null
  }

  function openJoinGroupModal(group: Pick<GroupListItem, 'id' | 'groupName'>) {
    options.activeTab.value = 'join-group'
    joinGroupId.value = String(group.id)
    joinGroupMessage.value = ''
    showGroupPreview.value = false
    previewGroupId.value = null
    options.message.info(`申请加入「${group.groupName}」，请填写申请说明后发送`)
  }

  function applyJoinFromPreview(group: GroupPreviewDetail) {
    openJoinGroupModal(group)
    previewGroupDetail.value = null
  }

  function groupRoleText(role?: number) {
    if (role === GROUP_ROLE_OWNER) {
      return '群主'
    }
    if (role === GROUP_ROLE_ADMIN) {
      return '管理员'
    }
    return '成员'
  }

  function groupRoleClass(role?: number) {
    if (role === GROUP_ROLE_OWNER) {
      return 'owner'
    }
    if (role === GROUP_ROLE_ADMIN) {
      return 'admin'
    }
    return 'member'
  }

  function startGroupChat(groupId: string | number) {
    options.router.push({
      path: `/chat/${groupId}`,
      query: { sessionType: '2' }
    })
  }

  async function copyGroupId(groupId: string | number) {
    try {
      await navigator.clipboard.writeText(String(groupId))
      options.message.success(`群号 ${groupId} 已复制`)
    } catch {
      options.message.error('复制群号失败')
    }
  }

  function isUnavailableGroupError(error: any) {
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)
    return code === 403 || code === 404
  }

  async function refreshGroupPreview(groupId: string | number, config: { silent?: boolean } = {}) {
    const normalizedGroupId = String(groupId)
    previewGroupId.value = normalizedGroupId
    try {
      const response: any = await groupApi.detail(normalizedGroupId)
      if (previewGroupId.value !== normalizedGroupId) {
        return
      }
      previewGroupDetail.value = response.data || null
      if (response.data?.myRole !== null && response.data?.myRole !== undefined) {
        clearPendingJoinRequest(normalizedGroupId)
      }
    } catch (error: any) {
      if (previewGroupId.value !== normalizedGroupId) {
        return
      }
      if (isUnavailableGroupError(error)) {
        closeGroupPreview()
        if (config.silent) {
          return
        }
        options.message.warning(error.response?.data?.message || '当前群聊资料已失效')
        return
      }
      if (config.silent) {
        return
      }
      closeGroupPreview()
      options.message.error(error.response?.data?.message || '获取群资料失败')
    }
  }

  async function openGroupPreview(group: Pick<GroupListItem, 'id'>) {
    showGroupPreview.value = true
    previewGroupDetail.value = null
    await refreshGroupPreview(group.id)
  }

  async function refreshOpenGroupPreview() {
    if (!showGroupPreview.value || !previewGroupId.value) {
      return
    }
    await refreshGroupPreview(previewGroupId.value, { silent: true })
  }

  return {
    joinGroupId,
    joinGroupMessage,
    joiningGroup,
    groupSearchKeyword,
    searchingGroup,
    groupSearchResults,
    myGroups,
    showGroupPreview,
    previewGroupDetail,
    hasPendingJoinRequest,
    isJoinedGroup,
    loadMyGroups,
    handleJoinGroup,
    handleGroupSearch,
    clearGroupSearch,
    openJoinGroupModal,
    applyJoinFromPreview,
    groupRoleText,
    groupRoleClass,
    startGroupChat,
    copyGroupId,
    openGroupPreview,
    refreshOpenGroupPreview,
    closeGroupPreview
  }
}
