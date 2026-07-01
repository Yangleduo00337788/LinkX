import { describe, expect, it } from 'vitest'
import { mapSearchHits } from './messageSearchHit'

describe('mapSearchHits', () => {
  it('maps MessageSearchHitDTO fields', () => {
    const rows = mapSearchHits([
      {
        messageId: 99,
        targetId: 2,
        sessionType: 1,
        sessionTitle: 'Alice',
        contentPreview: 'hello',
        createTime: '2026-01-01T10:00:00'
      }
    ])
    expect(rows).toHaveLength(1)
    expect(rows[0].id).toBe(99)
    expect(rows[0].targetName).toBe('Alice')
    expect(rows[0].content).toBe('hello')
  })

  it('unwraps axios-style data envelope', () => {
    const rows = mapSearchHits({
      data: [{ messageId: 1, contentPreview: 'x', targetId: 1, sessionType: 2 }]
    })
    expect(rows[0].sessionType).toBe(2)
  })
})