/**
 * 与服务端 /api/user/chat-drafts 同步草稿（防抖写入、登录后拉取）。
 */
import { sessionApi } from '../api/client'
import { buildSessionKey } from './chat'

const timers = new Map<string, ReturnType<typeof setTimeout>>()
const DEBOUNCE_MS = 600

function unwrapList(payload: unknown): Array<{ targetId: number | string; sessionType: number; draftContent?: string }> {
  if (!payload || typeof payload !== 'object') return []
  const root = payload as Record<string, unknown>
  const data = root.data !== undefined ? root.data : payload
  if (Array.isArray(data)) return data as Array<{ targetId: number | string; sessionType: number; draftContent?: string }>
  if (data && typeof data === 'object' && Array.isArray((data as { records?: unknown }).records)) {
    return (data as { records: Array<{ targetId: number | string; sessionType: number; draftContent?: string }> }).records
  }
  return []
}

export async function loadDraftsFromServer(): Promise<Record<string, string>> {
  try {
    const res = await sessionApi.listDrafts()
    const list = unwrapList(res)
    const map: Record<string, string> = {}
    for (const row of list) {
      const key = buildSessionKey(row.targetId, Number(row.sessionType))
      const text = (row.draftContent ?? '').trim()
      if (text) map[key] = text
    }
    return map
  } catch (e) {
    console.warn('loadDraftsFromServer failed', e)
    return {}
  }
}

export function scheduleDraftSync(
  targetId: string | number,
  sessionType: number,
  draftContent: string
) {
  const key = buildSessionKey(targetId, sessionType)
  const existing = timers.get(key)
  if (existing) clearTimeout(existing)
  const trimmed = draftContent.trim()
  timers.set(
    key,
    setTimeout(() => {
      timers.delete(key)
      void persistDraft(targetId, sessionType, trimmed)
    }, DEBOUNCE_MS)
  )
}

export function flushDraftSync(targetId: string | number, sessionType: number, draftContent: string) {
  const key = buildSessionKey(targetId, sessionType)
  const existing = timers.get(key)
  if (existing) {
    clearTimeout(existing)
    timers.delete(key)
  }
  return persistDraft(targetId, sessionType, draftContent.trim())
}

async function persistDraft(targetId: string | number, sessionType: number, trimmed: string) {
  try {
    if (!trimmed) {
      await sessionApi.deleteDraft(targetId, sessionType)
      return
    }
    await sessionApi.saveDraft({ targetId, sessionType, draftContent: trimmed })
  } catch (e) {
    console.warn('persistDraft failed', e)
  }
}

export async function clearDraftOnServer(targetId: string | number, sessionType: number) {
  const key = buildSessionKey(targetId, sessionType)
  const existing = timers.get(key)
  if (existing) {
    clearTimeout(existing)
    timers.delete(key)
  }
  try {
    await sessionApi.deleteDraft(targetId, sessionType)
  } catch (e) {
    console.warn('clearDraftOnServer failed', e)
  }
}