import { getElectronAPI } from './electron'

type NotificationSoundType = 'message' | 'attention'
const IN_APP_NOTIFICATION_EVENT = 'linkx-in-app-notification'

let audioContext: AudioContext | null = null
let soundThrottleTimer = 0

export interface InAppNotificationPayload {
  id: string
  title: string
  body: string
  icon?: string
  targetId?: string
  sessionType?: number
  messageId?: string
  attention?: boolean
}

function emitInAppNotification(payload: InAppNotificationPayload) {
  if (typeof window === 'undefined') {
    return
  }
  window.dispatchEvent(new CustomEvent(IN_APP_NOTIFICATION_EVENT, {
    detail: payload
  }))
}

function buildNotificationId() {
  return `notify-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

export async function showNotification(
  title: string,
  body: string,
  icon?: string,
  payload: Omit<InAppNotificationPayload, 'id' | 'title' | 'body' | 'icon'> = {}
): Promise<boolean> {
  emitInAppNotification({
    id: buildNotificationId(),
    title,
    body,
    icon,
    ...payload
  })
  const api = getElectronAPI()
  if (api) {
    return await api.showNotification(title, body, icon)
  }

  if ('Notification' in window) {
    if (Notification.permission === 'granted') {
      new Notification(title, { body, icon })
      return true
    }

    if (Notification.permission !== 'denied') {
      const permission = await Notification.requestPermission()
      if (permission === 'granted') {
        new Notification(title, { body, icon })
        return true
      }
    }
  }

  return false
}

function getAudioContext(): AudioContext | null {
  if (typeof window === 'undefined') {
    return null
  }
  const AudioContextConstructor = window.AudioContext || (window as typeof window & {
    webkitAudioContext?: typeof AudioContext
  }).webkitAudioContext
  if (!AudioContextConstructor) {
    return null
  }
  if (!audioContext) {
    audioContext = new AudioContextConstructor()
  }
  return audioContext
}

function createTone(
  context: AudioContext,
  frequency: number,
  startAt: number,
  duration: number,
  volume: number
) {
  const oscillator = context.createOscillator()
  const gainNode = context.createGain()
  oscillator.type = 'sine'
  oscillator.frequency.setValueAtTime(frequency, startAt)
  gainNode.gain.setValueAtTime(0.0001, startAt)
  gainNode.gain.exponentialRampToValueAtTime(volume, startAt + 0.02)
  gainNode.gain.exponentialRampToValueAtTime(0.0001, startAt + duration)
  oscillator.connect(gainNode)
  gainNode.connect(context.destination)
  oscillator.start(startAt)
  oscillator.stop(startAt + duration + 0.03)
}

export async function playNotificationSound(type: NotificationSoundType = 'message'): Promise<boolean> {
  const api = getElectronAPI()
  if (api?.playNotificationSound) {
    return await api.playNotificationSound(type)
  }
  const now = Date.now()
  if (now - soundThrottleTimer < 280) {
    return false
  }
  soundThrottleTimer = now

  const context = getAudioContext()
  if (!context) {
    return false
  }

  try {
    if (context.state === 'suspended') {
      await context.resume()
    }
    const startAt = context.currentTime + 0.01
    if (type === 'attention') {
      createTone(context, 740, startAt, 0.12, 0.04)
      createTone(context, 988, startAt + 0.16, 0.18, 0.05)
      return true
    }
    createTone(context, 660, startAt, 0.1, 0.035)
    createTone(context, 784, startAt + 0.12, 0.14, 0.04)
    return true
  } catch (error) {
    console.warn('playNotificationSound failed:', error)
    return false
  }
}

export function onNewMessage(callback: (senderName: string, content: string) => void): () => void {
  const handler = (event: CustomEvent) => {
    const { senderName, content } = event.detail
    callback(senderName, content)
  }

  window.addEventListener('new-message', handler as EventListener)
  return () => window.removeEventListener('new-message', handler as EventListener)
}

export function emitNewMessage(senderName: string, content: string): void {
  window.dispatchEvent(new CustomEvent('new-message', {
    detail: { senderName, content }
  }))
}

export function onInAppNotification(callback: (payload: InAppNotificationPayload) => void): () => void {
  const handler = (event: Event) => {
    const customEvent = event as CustomEvent<InAppNotificationPayload>
    callback(customEvent.detail)
  }
  window.addEventListener(IN_APP_NOTIFICATION_EVENT, handler)
  return () => window.removeEventListener(IN_APP_NOTIFICATION_EVENT, handler)
}
