const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
  // 窗口控制
  minimizeWindow: () => ipcRenderer.invoke('window:minimize'),
  maximizeWindow: () => ipcRenderer.invoke('window:maximize'),
  closeWindow: () => ipcRenderer.invoke('window:close'),
  isMaximized: () => ipcRenderer.invoke('window:isMaximized'),

  // 窗口状态监听
  onWindowMaximize: (callback) => ipcRenderer.on('window:maximized', (_, maximized) => callback(maximized)),

  // 系统通知
  showNotification: (title, body, icon) => ipcRenderer.invoke('notification:show', title, body, icon),

  // 文件拖拽
  onFileDrop: (callback) => ipcRenderer.on('file:drop', (_, files) => callback(files)),
  removeFileDropListener: () => ipcRenderer.removeAllListeners('file:drop'),

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

  // 外部链接
  openExternal: (url) => ipcRenderer.invoke('shell:openExternal', url),

  // 开发工具
  toggleDevTools: () => ipcRenderer.invoke('app:toggleDevTools'),
})
