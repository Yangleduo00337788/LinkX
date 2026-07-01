/** 将后端 MessageSearchHitDTO 映射为前端展示结构 */
export interface MessageSearchHitView {
  id: number | string
  content?: string
  createTime?: string
  sessionType?: number
  targetId?: number | string
  fromUserId?: number | string
  targetName?: string
}

export function mapSearchHits(raw: unknown): MessageSearchHitView[] {
  const list = Array.isArray(raw) ? raw : (raw as { data?: unknown[] })?.data
  if (!Array.isArray(list)) return []
  return list.map((row: Record<string, unknown>) => ({
    id: (row.messageId ?? row.id) as number | string,
    content: String(row.contentPreview ?? row.content ?? ''),
    createTime: row.createTime as string | undefined,
    sessionType: row.sessionType as number | undefined,
    targetId: row.targetId as number | string | undefined,
    fromUserId: row.fromUserId as number | string | undefined,
    targetName: row.sessionTitle as string | undefined
  }))
}