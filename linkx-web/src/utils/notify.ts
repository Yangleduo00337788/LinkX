import { getElectronAPI } from './electron'

export async function showNotification(title: string, body: string, icon?: string): Promise<boolean> {
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
