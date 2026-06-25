/**
 * 将聊天文本拆分为普通文本、@提及与可点击外链片段。
 */
import { resolveSafeExternalUrl } from './url'

export type ChatTextSegment =
  | { type: 'text'; text: string }
  | { type: 'mention'; text: string }
  | { type: 'link'; text: string; href: string }

const MENTION_PATTERN = /@[^\s,，。！？；：、]+/g
const URL_PATTERN = /https?:\/\/[^\s<>"']+/gi

export function splitChatTextSegments(content: string): ChatTextSegment[] {
  if (!content) {
    return [{ type: 'text', text: '' }]
  }

  const rawMatches: Array<{ index: number; length: number; segment: ChatTextSegment }> = []

  for (const match of content.matchAll(MENTION_PATTERN)) {
    const index = match.index ?? 0
    rawMatches.push({
      index,
      length: match[0].length,
      segment: { type: 'mention', text: match[0] }
    })
  }

  for (const match of content.matchAll(URL_PATTERN)) {
    const index = match.index ?? 0
    const href = resolveSafeExternalUrl(match[0])
    if (!href) {
      continue
    }
    rawMatches.push({
      index,
      length: match[0].length,
      segment: { type: 'link', text: match[0], href }
    })
  }

  rawMatches.sort((a, b) => a.index - b.index || b.length - a.length)

  const segments: ChatTextSegment[] = []
  let cursor = 0

  for (const item of rawMatches) {
    if (item.index < cursor) {
      continue
    }
    if (item.index > cursor) {
      segments.push({ type: 'text', text: content.slice(cursor, item.index) })
    }
    segments.push(item.segment)
    cursor = item.index + item.length
  }

  if (cursor < content.length) {
    segments.push({ type: 'text', text: content.slice(cursor) })
  }

  if (segments.length === 0) {
    return [{ type: 'text', text: content }]
  }

  return segments
}

/** @deprecated 使用 splitChatTextSegments；保留 mention-only 形状以兼容旧调用 */
export function getMentionOnlySegments(content: string): Array<{ text: string; mention: boolean }> {
  return splitChatTextSegments(content).map(seg => {
    if (seg.type === 'mention') {
      return { text: seg.text, mention: true }
    }
    if (seg.type === 'link') {
      return { text: seg.text, mention: false }
    }
    return { text: seg.text, mention: false }
  })
}