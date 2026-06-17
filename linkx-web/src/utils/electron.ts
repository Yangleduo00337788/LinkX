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

export interface ElectronAPI {
  // 窗口控制
  minimizeWindow: () => Promise<void>
  maximizeWindow: () => Promise<void>
  closeWindow: () => Promise<void>
  isMaximized: () => Promise<boolean>
  onWindowMaximize: (callback: (maximized: boolean) => void) => void

  // 系统通知
  showNotification: (title: string, body: string, icon?: string) => Promise<boolean>
  playNotificationSound: (type?: 'message' | 'attention') => Promise<boolean>

  // 文件拖拽
  onFileDrop: (callback: (files: string[]) => void) => void
  removeFileDropListener: () => void

  // 系统信息
  getVersions: () => Promise<{
    electron: string
    node: string
    chrome: string
    app: string
  }>
  getPlatform: () => Promise<string>

  // 自动更新
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

  // 开机自启
  setAutoLaunch: (enable: boolean) => Promise<void>
  getAutoLaunch: () => Promise<boolean>

  // 剪贴板
  readClipboard: () => Promise<string>
  writeClipboard: (text: string) => Promise<void>

  // 外部链接
  openExternal: (url: string) => Promise<void>

  // 开发工具
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
