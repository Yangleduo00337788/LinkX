/**
 * useGroupRequests 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { computed, ref, type ComputedRef } from 'vue'  // 行注：引入 computed, ref, type ComputedRef 能力
import { groupApi } from '../api/client'  // 行注：引入 groupApi 能力
import type { GroupRequestItem } from '../types/chat'  // 行注：引入 type { GroupRequestItem } 模块

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  info: (content: string) => void  // 行注：设置 info 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface GroupRequestMessageTextOptions {  // 行注：开始当前逻辑块
  inviteText?: string  // 行注：补充当前表达式
  joinText?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface UseGroupRequestsOptions {  // 行注：开始当前逻辑块
  groupId?: ComputedRef<string>  // 行注：补充当前表达式
  message: FeedbackApi  // 行注：设置 message 配置项
  afterAccept?: () => Promise<unknown>  // 行注：执行当前调用逻辑
  afterReject?: () => Promise<unknown>  // 行注：执行当前调用逻辑
  acceptSuccessText?: string  // 行注：补充当前表达式
  rejectSuccessText?: string  // 行注：补充当前表达式
  staleInfoText?: string  // 行注：补充当前表达式
  actionErrorText?: string  // 行注：补充当前表达式
  requestMessageText?: GroupRequestMessageTextOptions  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export function groupRequestTypeText(requestType: number) {  // 行注：导出当前能力
  return requestType === 1 ? '邀请入群' : '申请入群'  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function groupRequestTagClass(requestType: number) {  // 行注：导出当前能力
  return requestType === 1 ? 'invite' : 'join'  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function buildGroupRequestMessage(  // 行注：导出当前能力
  request: GroupRequestItem,  // 行注：设置 request 配置项
  textOptions: GroupRequestMessageTextOptions = {}  // 行注：设置 textOptions 配置项
) {  // 行注：开始当前逻辑块
  const actor = request.fromNickname || request.fromUsername || '成员'  // 行注：初始化 actor 变量
  const suffix = request.message?.trim() ? `：${request.message.trim()}` : ''  // 行注：初始化 suffix 变量
  const inviteText = textOptions.inviteText || '邀请你加入群聊'  // 行注：初始化 inviteText 变量
  const joinText = textOptions.joinText || '申请加入群聊'  // 行注：初始化 joinText 变量
  return `${actor} ${request.requestType === 1 ? inviteText : joinText}${suffix}`  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function formatGroupRequestTime(time?: string) {  // 行注：导出当前能力
  return time?.substring(0, 16)?.replace('T', ' ') || ''  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function useGroupRequests(options: UseGroupRequestsOptions) {  // 行注：导出当前能力
  const groupRequests = ref<GroupRequestItem[]>([])  // 行注：初始化 groupRequests 集合
  const loadingGroupRequests = ref(false)  // 行注：初始化 loadingGroupRequests 响应式状态
  const requestActionLoadingIds = ref<string[]>([])  // 行注：初始化 requestActionLoadingIds 变量

  const sortedGroupRequests = computed(() => {  // 行注：开始解构当前返回值
    return [...groupRequests.value].sort((left, right) => String(right.id).localeCompare(String(left.id)))  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const currentGroupRequests = computed(() => {  // 行注：开始解构当前返回值
    if (!options.groupId) {  // 行注：判断当前条件是否成立
      return sortedGroupRequests.value  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return sortedGroupRequests.value.filter(item => String(item.groupId) === String(options.groupId?.value))  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const requestActionLoadingId = computed(() => requestActionLoadingIds.value[0] || null)  // 行注：声明 requestActionLoadingId 计算属性

  async function loadGroupRequests() {  // 行注：定义异步 loadGroupRequests 方法
    loadingGroupRequests.value = true  // 行注：更新 loadingGroupRequests 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.getRequests()  // 行注：接收 response 异步结果
      groupRequests.value = response.data || []  // 行注：更新 groupRequests 状态
    } catch (error) {  // 行注：捕获并处理异常
      console.error('loadGroupRequests error:', error)  // 行注：输出错误日志
    } finally {  // 行注：执行收尾清理逻辑
      loadingGroupRequests.value = false  // 行注：更新 loadingGroupRequests 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function normalizeRequestId(requestId: string | number) {  // 行注：定义 normalizeRequestId 方法
    return String(requestId)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function isRequestActionLoading(requestId: string | number) {  // 行注：定义 isRequestActionLoading 方法
    return requestActionLoadingIds.value.includes(normalizeRequestId(requestId))  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function beginRequestAction(requestId: string | number) {  // 行注：定义 beginRequestAction 方法
    const normalizedRequestId = normalizeRequestId(requestId)  // 行注：初始化 normalizedRequestId 状态
    if (requestActionLoadingIds.value.includes(normalizedRequestId)) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    requestActionLoadingIds.value = [...requestActionLoadingIds.value, normalizedRequestId]  // 行注：更新 requestActionLoadingIds 状态
    return true  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function endRequestAction(requestId: string | number) {  // 行注：定义 endRequestAction 方法
    const normalizedRequestId = normalizeRequestId(requestId)  // 行注：初始化 normalizedRequestId 状态
    requestActionLoadingIds.value = requestActionLoadingIds.value.filter(id => id !== normalizedRequestId)  // 行注：调用 filter 方法
  }  // 行注：结束当前代码块

  function isStaleGroupRequestError(error: any) {  // 行注：定义 isStaleGroupRequestError 方法
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)  // 行注：初始化 code 变量
    const serverMessage = String(error?.response?.data?.message || '')  // 行注：初始化 serverMessage 实例
    return code === 404 || /已处理|不存在/.test(serverMessage)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function reloadAfterAction(callback?: () => Promise<unknown>) {  // 行注：定义异步 reloadAfterAction 方法
    const tasks: Promise<unknown>[] = [loadGroupRequests()]  // 行注：初始化 tasks 变量
    if (callback) {  // 行注：判断当前条件是否成立
      tasks.push(Promise.resolve(callback()))  // 行注：跳转到目标路由
    }  // 行注：结束当前代码块
    await Promise.all(tasks)  // 行注：并行执行多项异步任务
  }  // 行注：结束当前代码块

  async function handleAcceptGroupRequest(requestId: number | string) {  // 行注：定义异步 handleAcceptGroupRequest 方法
    if (!beginRequestAction(requestId)) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.acceptRequest(requestId)  // 行注：调用 acceptRequest 方法
      options.message.success(options.acceptSuccessText || '已处理群通知')  // 行注：提示成功信息
      await reloadAfterAction(options.afterAccept)  // 行注：调用 reloadAfterAction 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleAcceptGroupRequest error:', error)  // 行注：输出错误日志
      if (isStaleGroupRequestError(error)) {  // 行注：判断当前条件是否成立
        await reloadAfterAction(options.afterAccept)  // 行注：调用 reloadAfterAction 方法
        options.message.info(error.response?.data?.message || options.staleInfoText || '群通知状态已更新')  // 行注：调用 info 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.message.error(error.response?.data?.message || options.actionErrorText || '处理群通知失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      endRequestAction(requestId)  // 行注：调用 endRequestAction 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleRejectGroupRequest(requestId: number | string) {  // 行注：定义异步 handleRejectGroupRequest 方法
    if (!beginRequestAction(requestId)) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.rejectRequest(requestId)  // 行注：调用 rejectRequest 方法
      options.message.success(options.rejectSuccessText || '已拒绝')  // 行注：提示成功信息
      await reloadAfterAction(options.afterReject)  // 行注：调用 reloadAfterAction 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleRejectGroupRequest error:', error)  // 行注：输出错误日志
      if (isStaleGroupRequestError(error)) {  // 行注：判断当前条件是否成立
        await reloadAfterAction(options.afterReject)  // 行注：调用 reloadAfterAction 方法
        options.message.info(error.response?.data?.message || options.staleInfoText || '群通知状态已更新')  // 行注：调用 info 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.message.error(error.response?.data?.message || options.actionErrorText || '处理群通知失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      endRequestAction(requestId)  // 行注：调用 endRequestAction 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    groupRequests,  // 行注：补充 groupRequests 配置项
    loadingGroupRequests,  // 行注：补充当前配置项
    requestActionLoadingId,  // 行注：补充当前配置项
    requestActionLoadingIds,  // 行注：补充当前配置项
    sortedGroupRequests,  // 行注：补充当前配置项
    currentGroupRequests,  // 行注：补充当前配置项
    loadGroupRequests,  // 行注：补充当前配置项
    groupRequestTypeText,  // 行注：补充当前配置项
    groupRequestTagClass,  // 行注：补充当前配置项
    buildGroupRequestMessage: (request: GroupRequestItem) => buildGroupRequestMessage(request, options.requestMessageText),  // 行注：设置 buildGroupRequestMessage 配置项
    formatRequestTime: formatGroupRequestTime,  // 行注：设置 formatRequestTime 配置项
    isRequestActionLoading,  // 行注：补充当前配置项
    handleAcceptGroupRequest,  // 行注：补充当前配置项
    handleRejectGroupRequest  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
