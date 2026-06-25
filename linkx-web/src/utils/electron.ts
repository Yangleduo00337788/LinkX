/**
 * 封装渲染进程访问 Electron 能力的兼容调用。
 */
export interface UpdateInfo {  // 行注：导出当前能力
  version: string  // 行注：设置 version 配置项
  releaseDate?: string  // 行注：补充当前表达式
  releaseNotes?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export interface UpdateProgress {  // 行注：导出当前能力
  percent: number  // 行注：设置 percent 配置项
  transferred: number  // 行注：设置 transferred 配置项
  total: number  // 行注：设置 total 配置项
  bytesPerSecond: number  // 行注：设置 bytesPerSecond 配置项
}  // 行注：结束当前代码块

export interface ElectronAPI {  // 行注：导出当前能力
  // 窗口控制
  minimizeWindow: () => Promise<void>  // 行注：传入 minimizeWindow 回调
  maximizeWindow: () => Promise<void>  // 行注：传入 maximizeWindow 回调
  closeWindow: () => Promise<void>  // 行注：传入 closeWindow 回调
  isMaximized: () => Promise<boolean>  // 行注：传入 isMaximized 回调
  onWindowMaximize: (callback: (maximized: boolean) => void) => void  // 行注：设置 onWindowMaximize 配置项

  // 系统通知
  showNotification: (title: string, body: string, icon?: string) => Promise<boolean>  // 行注：设置 showNotification 配置项
  playNotificationSound: (type?: 'message' | 'attention') => Promise<boolean>  // 行注：设置 playNotificationSound 配置项

  // 文件拖拽
  onFileDrop: (callback: (files: string[]) => void) => void  // 行注：设置 onFileDrop 配置项
  removeFileDropListener: () => void  // 行注：传入 removeFileDropListener 回调

  // 系统信息
  getVersions: () => Promise<{  // 行注：传入 getVersions 回调
    electron: string  // 行注：设置 electron 配置项
    node: string  // 行注：设置 node 配置项
    chrome: string  // 行注：设置 chrome 配置项
    app: string  // 行注：设置 app 配置项
  }>  // 行注：补充当前表达式
  getPlatform: () => Promise<string>  // 行注：传入 getPlatform 回调

  // 自动更新
  checkForUpdates: () => Promise<{ available: boolean; version?: string; error?: string }>  // 行注：传入 checkForUpdates 回调
  downloadUpdate: () => Promise<void>  // 行注：传入 downloadUpdate 回调
  installUpdate: () => Promise<void>  // 行注：传入 installUpdate 回调
  cancelUpdate: () => Promise<void>  // 行注：传入 cancelUpdate 回调
  onUpdateChecking: (callback: () => void) => void  // 行注：设置 onUpdateChecking 配置项
  onUpdateAvailable: (callback: (info: UpdateInfo) => void) => void  // 行注：设置 onUpdateAvailable 配置项
  onUpdateNotAvailable: (callback: () => void) => void  // 行注：设置 onUpdateNotAvailable 配置项
  onUpdateDownloadProgress: (callback: (progress: UpdateProgress) => void) => void  // 行注：设置 onUpdateDownloadProgress 配置项
  onUpdateDownloaded: (callback: (info: UpdateInfo) => void) => void  // 行注：设置 onUpdateDownloaded 配置项
  onUpdateError: (callback: (err: { message: string }) => void) => void  // 行注：设置 onUpdateError 配置项

  // 开机自启
  setAutoLaunch: (enable: boolean) => Promise<void>  // 行注：设置 setAutoLaunch 配置项
  getAutoLaunch: () => Promise<boolean>  // 行注：传入 getAutoLaunch 回调

  // 剪贴板
  readClipboard: () => Promise<string>  // 行注：传入 readClipboard 回调
  writeClipboard: (text: string) => Promise<void>  // 行注：设置 writeClipboard 配置项

  // 外部链接
  openExternal: (url: string) => Promise<void>  // 行注：设置 openExternal 配置项

  // 开发工具
  toggleDevTools: () => Promise<void>  // 行注：传入 toggleDevTools 回调
}  // 行注：结束当前代码块

declare global {  // 行注：开始当前逻辑块
  interface Window {  // 行注：开始当前逻辑块
    electronAPI?: ElectronAPI  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export function getElectronAPI(): ElectronAPI | null {  // 行注：导出当前能力
  return window.electronAPI || null  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function isElectron(): boolean {  // 行注：导出当前能力
  return !!window.electronAPI  // 行注：返回当前结果
}  // 行注：结束当前代码块
