/**
 * 封装渲染进程访问 Electron 能力的兼容调用。
 */
export interface UpdateInfo {
  version: string
  releaseDate?: string
  releaseNotes?: string
}

export interface UpdateProgress {
  percent: number
  transferred: number
  total: number
  bytesPerSecond: number
}

export interface NotificationNavigatePayload {
  targetId: string
  sessionType?: number
  messageId?: string
}

export interface ClipboardImagePayload {
  mimeType: string
  data: string
  size: number
}

export interface DroppedFilePayload {
  name: string
  mimeType: string
  size: number
  data: string
}

export interface ElectronAPI {
  minimizeWindow: () => Promise<void>
  maximizeWindow: () => Promise<void>
  closeWindow: () => Promise<void>
  isMaximized: () => Promise<boolean>
  onWindowMaximize: (callback: (maximized: boolean) => void) => void

  showNotification: (
    title: string,
    body: string,
    icon?: string,
    payload?: NotificationNavigatePayload
  ) => Promise<boolean>
  playNotificationSound: (type?: 'message' | 'attention') => Promise<boolean>
  onNotificationNavigate: (callback: (data: NotificationNavigatePayload) => void) => void
  removeNotificationNavigateListener: () => void

  readDroppedFiles: (paths: string[]) => Promise<DroppedFilePayload[]>
  onFileDrop: (callback: (files: string[]) => void) => void
  removeFileDropListener: () => void

  setTrayUnreadCount: (count: number) => Promise<number>
  onTrayAction: (callback: (data: { action: string }) => void) => void
  removeTrayActionListener: () => void

  getVersions: () => Promise<{
    electron: string
    node: string
    chrome: string
    app: string
  }>
  getPlatform: () => Promise<string>

  checkForUpdates: () => Promise<{ available: boolean; version?: string; error?: string }>
  downloadUpdate: () => Promise<void>
  installUpdate: () => Promise<void>
  cancelUpdate: () => Promise<void>
  onUpdateChecking: (callback: () => void) => void
  onUpdateAvailable: (callback: (info: UpdateInfo) => void) => void
  onUpdateNotAvailable: (callback: () => void) => void
  onUpdateDownloadProgress: (callback: (progress: UpdateProgress) => void) => void
  onUpdateDownloaded: (callback: (info: UpdateInfo) => void) => void
  onUpdateError: (callback: (err: { message: string }) => void) => void

  setAutoLaunch: (enable: boolean) => Promise<void>
  getAutoLaunch: () => Promise<boolean>

  readClipboard: () => Promise<string>
  writeClipboard: (text: string) => Promise<void>
  readClipboardImage: () => Promise<ClipboardImagePayload | null>

  openExternal: (url: string) => Promise<void>
  toggleDevTools: () => Promise<void>
}

declare global {
  interface Window {
    electronAPI?: ElectronAPI
  }
}

export function getElectronAPI(): ElectronAPI | null {
  return window.electronAPI || null
}

export function isElectron(): boolean {
  return !!window.electronAPI
}