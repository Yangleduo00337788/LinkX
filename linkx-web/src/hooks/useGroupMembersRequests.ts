/**
 * useGroupMembersRequests 组合式逻辑，负责抽离可复用的状态与行为。
 */
import type { ComputedRef } from 'vue'  // 行注：引入 type { ComputedRef } 模块
import { useGroupRequests } from './useGroupRequests'  // 行注：引入 useGroupRequests 能力

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  info: (content: string) => void  // 行注：设置 info 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface UseGroupMembersRequestsOptions {  // 行注：开始当前逻辑块
  groupId: ComputedRef<string>  // 行注：设置 groupId 配置项
  message: FeedbackApi  // 行注：设置 message 配置项
  refreshGroupDetail: () => Promise<boolean>  // 行注：传入 refreshGroupDetail 回调
}  // 行注：结束当前代码块

export function useGroupMembersRequests(options: UseGroupMembersRequestsOptions) {  // 行注：导出当前能力
  return useGroupRequests({  // 行注：返回当前结果
    groupId: options.groupId,  // 行注：设置 groupId 配置项
    message: options.message,  // 行注：设置 message 配置项
    afterAccept: options.refreshGroupDetail,  // 行注：设置 afterAccept 配置项
    acceptSuccessText: '已通过入群申请',  // 行注：设置 acceptSuccessText 配置项
    rejectSuccessText: '已拒绝入群申请',  // 行注：设置 rejectSuccessText 配置项
    staleInfoText: '群申请状态已更新',  // 行注：设置 staleInfoText 配置项
    actionErrorText: '处理入群申请失败',  // 行注：设置 actionErrorText 配置项
    requestMessageText: {  // 行注：设置 requestMessageText 配置项
      inviteText: '邀请加入当前群聊',  // 行注：设置 inviteText 配置项
      joinText: '申请加入当前群聊'  // 行注：设置 joinText 配置项
    }  // 行注：结束当前代码块
  })  // 行注：结束当前调用配置
}  // 行注：结束当前代码块
