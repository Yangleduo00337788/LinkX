import type { ComputedRef } from 'vue'
import { useGroupRequests } from './useGroupRequests'

interface FeedbackApi {
  success: (content: string) => void
  info: (content: string) => void
  error: (content: string) => void
}

interface UseGroupMembersRequestsOptions {
  groupId: ComputedRef<string>
  message: FeedbackApi
  refreshGroupDetail: () => Promise<boolean>
}

export function useGroupMembersRequests(options: UseGroupMembersRequestsOptions) {
  return useGroupRequests({
    groupId: options.groupId,
    message: options.message,
    afterAccept: options.refreshGroupDetail,
    acceptSuccessText: '已通过入群申请',
    rejectSuccessText: '已拒绝入群申请',
    staleInfoText: '群申请状态已更新',
    actionErrorText: '处理入群申请失败',
    requestMessageText: {
      inviteText: '邀请加入当前群聊',
      joinText: '申请加入当前群聊'
    }
  })
}
