/**
 * Electron 预加载脚本，向渲染进程安全暴露 IPC 能力。
 */
const { contextBridge, ipcRenderer } = require('electron')

// 通过 contextBridge 只暴露白名单能力，避免渲染进程直接接触完整的 Node API。
contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimizeWindow: () => ipcRenderer.invoke('window:minimize'),
  maximizeWindow: () => ipcRenderer.invoke('window:maximize'),
  closeWindow: () => ipcRenderer.invoke('window:close'),
  isMaximized: () => ipcRenderer.invoke('window:isMaximized'),

  // 窗口状态监听
  onWindowMaximize: (callback) => ipcRenderer.on('window:maximized', (_, maximized) => callback(maximized)),

  // 系统通知
  showNotification: (title, body, icon, payload) => ipcRenderer.invoke('notification:show', title, body, icon, payload),
  playNotificationSound: (type) => ipcRenderer.invoke('notification:playSound', type),
  onNotificationNavigate: (callback) => ipcRenderer.on('notification:navigate', (_, data) => callback(data)),
  removeNotificationNavigateListener: () => ipcRenderer.removeAllListeners('notification:navigate'),

  // 文件拖拽
  notifyFileDrop: (paths) => ipcRenderer.invoke('file:readDropped', paths),
  onFileDrop: (callback) => ipcRenderer.on('file:drop', (_, files) => callback(files)),
  removeFileDropListener: () => ipcRenderer.removeAllListeners('file:drop'),
  readDroppedFiles: (paths) => ipcRenderer.invoke('file:readDropped', paths),

  // 托盘未读
  setTrayUnreadCount: (count) => ipcRenderer.invoke('tray:setUnreadCount', count),

  // 系统信息
  getVersions: () => ipcRenderer.invoke('app:versions'),
  getPlatform: () => ipcRenderer.invoke('app:platform'),

  // 自动更新
  checkForUpdates: () => ipcRenderer.invoke('updater:check'),
  downloadUpdate: () => ipcRenderer.invoke('updater:download'),
  installUpdate: () => ipcRenderer.invoke('updater:install'),
  cancelUpdate: () => ipcRenderer.invoke('updater:cancel'),
  onUpdateChecking: (callback) => ipcRenderer.on('updater:checking', () => callback()),
  onUpdateAvailable: (callback) => ipcRenderer.on('updater:available', (_, info) => callback(info)),
  onUpdateNotAvailable: (callback) => ipcRenderer.on('updater:not-available', () => callback()),
  onUpdateDownloadProgress: (callback) => ipcRenderer.on('updater:progress', (_, progress) => callback(progress)),
  onUpdateDownloaded: (callback) => ipcRenderer.on('updater:downloaded', (_, info) => callback(info)),
  onUpdateError: (callback) => ipcRenderer.on('updater:error', (_, err) => callback(err)),

  // 开机自启
  setAutoLaunch: (enable) => ipcRenderer.invoke('app:setAutoLaunch', enable),
  getAutoLaunch: () => ipcRenderer.invoke('app:getAutoLaunch'),

  // 剪贴板
  readClipboard: () => ipcRenderer.invoke('clipboard:read'),
  writeClipboard: (text) => ipcRenderer.invoke('clipboard:write', text),
  readClipboardImage: () => ipcRenderer.invoke('clipboard:readImage'),

  // 外部链接
  openExternal: (url) => ipcRenderer.invoke('shell:openExternal', url),

  // 开发工具
  toggleDevTools: () => ipcRenderer.invoke('app:toggleDevTools'),
})
