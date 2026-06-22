import type { GroupDetail } from '../types/chat'

export interface GroupProfileDraftState {
  groupName: string
  avatarPreview: string
  avatarFile: File | null
}

export function revokeBlobUrl(url?: string) {
  if (url?.startsWith('blob:')) {
    URL.revokeObjectURL(url)
  }
}

export function resetGroupProfileDraftState(draft: GroupProfileDraftState) {
  revokeBlobUrl(draft.avatarPreview)
  draft.groupName = ''
  draft.avatarPreview = ''
  draft.avatarFile = null
}

export function syncGroupProfileDraftState(
  draft: GroupProfileDraftState,
  detail?: GroupDetail | null
) {
  revokeBlobUrl(draft.avatarPreview)
  draft.groupName = detail?.groupName || ''
  draft.avatarPreview = detail?.groupAvatar || ''
  draft.avatarFile = null
}

export function isGroupProfileDraftChanged(
  draft: GroupProfileDraftState,
  detail?: GroupDetail | null
) {
  if (!detail) {
    return false
  }
  const normalizedName = draft.groupName.trim()
  const currentName = detail.groupName?.trim() || ''
  const currentAvatar = detail.groupAvatar || ''
  return normalizedName !== currentName
    || Boolean(draft.avatarFile)
    || draft.avatarPreview !== currentAvatar
}

export function isGroupNoticeDraftChanged(noticeDraft: string, detail?: GroupDetail | null) {
  if (!detail) {
    return false
  }
  return noticeDraft.trim() !== (detail.notice || '').trim()
}
