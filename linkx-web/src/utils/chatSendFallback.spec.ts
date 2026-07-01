import { describe, expect, it } from 'vitest'
import { isSocketTransportError } from './chatSendFallback'

describe('isSocketTransportError', () => {
  it('detects socket timeout message', () => {
    expect(isSocketTransportError(new Error('WebSocket 连接超时'))).toBe(true)
  })

  it('detects SOCKET_NOT_CONNECTED code', () => {
    expect(isSocketTransportError({ message: 'fail', code: 'SOCKET_NOT_CONNECTED' })).toBe(true)
  })

  it('returns false for business errors', () => {
    expect(isSocketTransportError({ message: '你已被禁言', response: { status: 403 } })).toBe(false)
  })
})