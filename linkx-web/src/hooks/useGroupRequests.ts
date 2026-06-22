import { computed, ref, type ComputedRef } from 'vue'
import { groupApi } from '../api/client'
import type { GroupRequestItem } from '../types/chat'

interface FeedbackApi {
  success: (content: string) => void
  info: (content: string) => void
  error: (content: string) => void
}

interface GroupRequestMessageTextOptions {
  inviteText?: string
  joinText?: string
}

interface UseGroupRequestsOptions {
  groupId?: ComputedRef<string>
  message: FeedbackApi
  afterAccept?: () => Promise<unknown>
  afterReject?: () => Promise<unknown>
  acceptSuccessText?: string
  rejectSuccessText?: string
  staleInfoText?: string
  actionErrorText?: string
  requestMessageText?: GroupRequestMessageTextOptions
}

export function groupRequestTypeText(requestType: number) {
  return requestType === 1 ? '邀请入群' : '申请入群'
}

export function groupRequestTagClass(requestType: number) {
  return requestType === 1 ? 'invite' : 'join'
}

export function buildGroupRequestMessage(
  request: GroupRequestItem,
  textOptions: GroupRequestMessageTextOptions = {}
) {
  const actor = request.fromNickname || request.fromUsername || '成员'
  const suffix = request.message?.trim() ? `：${request.message.trim()}` : ''
  const inviteText = textOptions.inviteText || '邀请你加入群聊'
  const joinText = textOptions.joinText || '申请加入群聊'
  return `${actor} ${request.requestType === 1 ? inviteText : joinText}${suffix}`
}

export function formatGroupRequestTime(time?: string) {
  return time?.substring(0, 16)?.replace('T', ' ') || ''
}

export function useGroupRequests(options: UseGroupRequestsOptions) {
  const groupRequests = ref<GroupRequestItem[]>([])
  const loadingGroupRequests = ref(false)
  const requestActionLoadingIds = ref<string[]>([])

  const sortedGroupRequests = computed(() => {
    return [...groupRequests.value].sort((left, right) => String(right.id).localeCompare(String(left.id)))
  })

  const currentGroupRequests = computed(() => {
    if (!options.groupId) {
      return sortedGroupRequests.value
    }
    return sortedGroupRequests.value.filter(item => String(item.groupId) === String(options.groupId?.value))
  })

  const requestActionLoadingId = computed(() => requestActionLoadingIds.value[0] || null)

  async function loadGroupRequests() {
    loadingGroupRequests.value = true
    try {
      const response: any = await groupApi.getRequests()
      groupRequests.value = response.data || []
    } catch (error) {
      console.error('loadGroupRequests error:', error)
    } finally {
      loadingGroupRequests.value = false
    }
  }

  function normalizeRequestId(requestId: string | number) {
    return String(requestId)
  }

  function isRequestActionLoading(requestId: string | number) {
    return requestActionLoadingIds.value.includes(normalizeRequestId(requestId))
  }

  function beginRequestAction(requestId: string | number) {
    const normalizedRequestId = normalizeRequestId(requestId)
    if (requestActionLoadingIds.value.includes(normalizedRequestId)) {
      return false
    }
    requestActionLoadingIds.value = [...requestActionLoadingIds.value, normalizedRequestId]
    return true
  }

  function endRequestAction(requestId: string | number) {
    const normalizedRequestId = normalizeRequestId(requestId)
    requestActionLoadingIds.value = requestActionLoadingIds.value.filter(id => id !== normalizedRequestId)
  }

  function isStaleGroupRequestError(error: any) {
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)
    const serverMessage = String(error?.response?.data?.message || '')
    return code === 404 || /已处理|不存在/.test(serverMessage)
  }

  async function reloadAfterAction(callback?: () => Promise<unknown>) {
    const tasks: Promise<unknown>[] = [loadGroupRequests()]
    if (callback) {
      tasks.push(Promise.resolve(callback()))
    }
    await Promise.all(tasks)
  }

  async function handleAcceptGroupRequest(requestId: number | string) {
    if (!beginRequestAction(requestId)) {
      return
    }
    try {
      await groupApi.acceptRequest(requestId)
      options.message.success(options.acceptSuccessText || '已处理群通知')
      await reloadAfterAction(options.afterAccept)
    } catch (error: any) {
      console.error('handleAcceptGroupRequest error:', error)
      if (isStaleGroupRequestError(error)) {
        await reloadAfterAction(options.afterAccept)
        options.message.info(error.response?.data?.message || options.staleInfoText || '群通知状态已更新')
        return
      }
      options.message.error(error.response?.data?.message || options.actionErrorText || '处理群通知失败')
    } finally {
      endRequestAction(requestId)
    }
  }

  async function handleRejectGroupRequest(requestId: number | string) {
    if (!beginRequestAction(requestId)) {
      return
    }
    try {
      await groupApi.rejectRequest(requestId)
      options.message.success(options.rejectSuccessText || '已拒绝')
      await reloadAfterAction(options.afterReject)
    } catch (error: any) {
      console.error('handleRejectGroupRequest error:', error)
      if (isStaleGroupRequestError(error)) {
        await reloadAfterAction(options.afterReject)
        options.message.info(error.response?.data?.message || options.staleInfoText || '群通知状态已更新')
        return
      }
      options.message.error(error.response?.data?.message || options.actionErrorText || '处理群通知失败')
    } finally {
      endRequestAction(requestId)
    }
  }

  return {
    groupRequests,
    loadingGroupRequests,
    requestActionLoadingId,
    requestActionLoadingIds,
    sortedGroupRequests,
    currentGroupRequests,
    loadGroupRequests,
    groupRequestTypeText,
    groupRequestTagClass,
    buildGroupRequestMessage: (request: GroupRequestItem) => buildGroupRequestMessage(request, options.requestMessageText),
    formatRequestTime: formatGroupRequestTime,
    isRequestActionLoading,
    handleAcceptGroupRequest,
    handleRejectGroupRequest
  }
}
