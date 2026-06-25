/**
 * group-draft 工具模块，封装当前前端场景下的通用方法。
 */
import type { GroupDetail } from '../types/chat'  // 行注：引入 type { GroupDetail } 模块

export interface GroupProfileDraftState {  // 行注：导出当前能力
  groupName: string  // 行注：设置 groupName 配置项
  avatarPreview: string  // 行注：设置 avatarPreview 配置项
  avatarFile: File | null  // 行注：设置 avatarFile 配置项
}  // 行注：结束当前代码块

export function revokeBlobUrl(url?: string) {  // 行注：导出当前能力
  if (url?.startsWith('blob:')) {  // 行注：判断当前条件是否成立
    URL.revokeObjectURL(url)  // 行注：调用 revokeObjectURL 方法
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export function resetGroupProfileDraftState(draft: GroupProfileDraftState) {  // 行注：导出当前能力
  revokeBlobUrl(draft.avatarPreview)  // 行注：调用 revokeBlobUrl 方法
  draft.groupName = ''  // 行注：更新 draft.groupName 值
  draft.avatarPreview = ''  // 行注：更新 draft.avatarPreview 值
  draft.avatarFile = null  // 行注：更新 draft.avatarFile 值
}  // 行注：结束当前代码块

export function syncGroupProfileDraftState(  // 行注：导出当前能力
  draft: GroupProfileDraftState,  // 行注：设置 draft 配置项
  detail?: GroupDetail | null  // 行注：补充当前表达式
) {  // 行注：开始当前逻辑块
  revokeBlobUrl(draft.avatarPreview)  // 行注：调用 revokeBlobUrl 方法
  draft.groupName = detail?.groupName || ''  // 行注：更新 draft.groupName 值
  draft.avatarPreview = detail?.groupAvatar || ''  // 行注：更新 draft.avatarPreview 值
  draft.avatarFile = null  // 行注：更新 draft.avatarFile 值
}  // 行注：结束当前代码块

export function isGroupProfileDraftChanged(  // 行注：导出当前能力
  draft: GroupProfileDraftState,  // 行注：设置 draft 配置项
  detail?: GroupDetail | null  // 行注：补充当前表达式
) {  // 行注：开始当前逻辑块
  if (!detail) {  // 行注：判断当前条件是否成立
    return false  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const normalizedName = draft.groupName.trim()  // 行注：初始化 normalizedName 变量
  const currentName = detail.groupName?.trim() || ''  // 行注：初始化 currentName 变量
  const currentAvatar = detail.groupAvatar || ''  // 行注：初始化 currentAvatar 变量
  return normalizedName !== currentName  // 行注：返回当前结果
    || Boolean(draft.avatarFile)  // 行注：执行当前调用逻辑
    || draft.avatarPreview !== currentAvatar  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export function isGroupNoticeDraftChanged(noticeDraft: string, detail?: GroupDetail | null) {  // 行注：导出当前能力
  if (!detail) {  // 行注：判断当前条件是否成立
    return false  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return noticeDraft.trim() !== (detail.notice || '').trim()  // 行注：返回当前结果
}  // 行注：结束当前代码块
