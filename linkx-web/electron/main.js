/**
 * Electron 主进程入口，负责窗口、托盘、通知和自动更新。
 */
const { app, BrowserWindow, ipcMain, Notification, nativeImage, clipboard, shell } = require('electron')
const fs = require('fs')
const http = require('http')
const path = require('path')

let mainWindow = null
let appRendererBaseUrl = ''
let trayUnreadCount = 0
let isDev = !!process.env.ELECTRON_DEV_URL
let localRendererServer = null
const LOCAL_RENDERER_HOST = '127.0.0.1'
const LOCAL_RENDERER_PORT = Number(process.env.LINKX_ELECTRON_PORT || 39689)
const APP_USER_MODEL_ID = 'com.linkx.im'
const TOAST_ACTIVATOR_CLSID = '2E35D3A8-5D13-4D74-A824-6C6C3D4B8F71'

// 仅在开发环境输出调试日志，避免生产环境日志过于嘈杂。
function debugLog(...args) {
  if (isDev) {
    console.log(...args)
  }
}

// Windows 通知依赖 AppUserModelId，开发态和打包态需要使用不同标识。
function getRuntimeAppUserModelId() {
  return app.isPackaged ? APP_USER_MODEL_ID : process.execPath
}

// 为 Windows 桌面通知创建快捷方式，保证通知中心能够正确识别应用。
function ensureWindowsNotificationShortcut() {
  if (process.platform !== 'win32') {
    return
  }
  try {
    const programsDir = path.join(process.env.APPDATA || app.getPath('appData'), 'Microsoft', 'Windows', 'Start Menu', 'Programs')
    const shortcutPath = path.join(programsDir, 'LinkX IM.lnk')
    fs.mkdirSync(programsDir, { recursive: true })
    shell.writeShortcutLink(shortcutPath, 'create', {
      target: process.execPath,
      cwd: path.dirname(process.execPath),
      description: 'LinkX IM',
      appUserModelId: getRuntimeAppUserModelId(),
      toastActivatorClsid: TOAST_ACTIVATOR_CLSID
    })
  } catch (error) {
    console.warn('ensureWindowsNotificationShortcut failed:', error)
  }
}

const STATIC_MIME_TYPES = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.ico': 'image/x-icon',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2'
}

// 根据扩展名推导静态资源响应头，供本地渲染服务使用。
function resolveContentType(filePath) {
  return STATIC_MIME_TYPES[path.extname(filePath).toLowerCase()] || 'application/octet-stream'
}

// 只允许打开 http/https 外链，避免渲染进程借此执行危险协议。
function resolveSafeExternalUrl(rawUrl) {
  const normalized = typeof rawUrl === 'string' ? rawUrl.trim() : ''
  if (!normalized) {
    return null
  }
  try {
    const parsed = new URL(normalized)
    if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
      return null
    }
    return parsed.href
  } catch {
    return null
  }
}

// 生产态用本地 HTTP 服务托管前端构建产物，并阻止目录穿越访问。
function createStaticRequestHandler(distDir) {
  return (request, response) => {
    const requestUrl = new URL(request.url || '/', `http://${LOCAL_RENDERER_HOST}:${LOCAL_RENDERER_PORT}`)
    const rawPath = decodeURIComponent(requestUrl.pathname)
    const normalizedPath = rawPath === '/' ? '/index.html' : rawPath
    const requestedFilePath = path.normalize(path.join(distDir, normalizedPath))
    const relativePath = path.relative(distDir, requestedFilePath)

    if (relativePath.startsWith('..') || path.isAbsolute(relativePath)) {
      response.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' })
      response.end('Forbidden')
      return
    }

    const finalFilePath = fs.existsSync(requestedFilePath) && fs.statSync(requestedFilePath).isFile()
      ? requestedFilePath
      : path.join(distDir, 'index.html')

    fs.readFile(finalFilePath, (error, content) => {
      if (error) {
        console.error('Static server read error:', error.message)
        response.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' })
        response.end('Internal Server Error')
        return
      }

      response.writeHead(200, {
        'Cache-Control': 'no-store',
        'Content-Type': resolveContentType(finalFilePath)
      })
      response.end(content)
    })
  }
}

// 打包后启动本地静态服务，再让窗口加载本地地址。
function startLocalRendererServer() {
  if (localRendererServer?.url) {
    return Promise.resolve(localRendererServer.url)
  }

  const distDir = path.join(__dirname, '..', 'dist')
  const indexPath = path.join(distDir, 'index.html')
  if (!fs.existsSync(indexPath)) {
    throw new Error(`Renderer build output not found: ${indexPath}`)
  }

  return new Promise((resolve, reject) => {
    const server = http.createServer(createStaticRequestHandler(distDir))
    server.on('error', reject)
    server.listen(LOCAL_RENDERER_PORT, LOCAL_RENDERER_HOST, () => {
      const address = server.address()
      if (!address || typeof address === 'string') {
        reject(new Error('Failed to resolve local renderer server address'))
        return
      }

      const url = `http://${LOCAL_RENDERER_HOST}:${address.port}`
      localRendererServer = { server, url }
      debugLog('Local renderer server started:', url)
      resolve(url)
    })
  })
}

// 应用退出前优雅关闭本地静态服务，释放端口。
function stopLocalRendererServer() {
  if (!localRendererServer?.server) {
    return Promise.resolve()
  }

  const { server } = localRendererServer
  localRendererServer = null
  return new Promise(resolve => {
    server.close(() => resolve())
  })
}

// 创建主窗口并串联菜单、托盘、更新器和 IPC 初始化。
async function createWindow() {
  debugLog('Creating window...', { isDev, url: process.env.ELECTRON_DEV_URL })

  const iconPath = path.join(__dirname, 'icons', 'tray.png')

  mainWindow = new BrowserWindow({
    width: 1180,
    height: 760,
    minWidth: 980,
    minHeight: 640,
    title: 'LinkX IM',
    frame: false,
    titleBarStyle: 'hidden',
    backgroundColor: '#0f0f12',
    icon: iconPath,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js'),
      spellcheck: false
    },
    show: false,
    roundedCorners: true
  })

  mainWindow.once('ready-to-show', () => {
    debugLog('Window ready to show')
    mainWindow.show()
  })

  mainWindow.webContents.on('did-fail-load', (event, errorCode, errorDescription) => {
    console.error('Failed to load:', errorCode, errorDescription)
  })

  mainWindow.webContents.on('console-message', (event, level, message) => {
    debugLog('Renderer:', { level, message })
  })

  if (isDev) {
    const url = process.env.ELECTRON_DEV_URL
    appRendererBaseUrl = String(url).replace(/#.*$/, '').replace(/\/?$/, '/')
    debugLog('Loading URL:', url)
    await mainWindow.loadURL(url)
  } else {
    const url = await startLocalRendererServer()
    appRendererBaseUrl = url.endsWith('/') ? url : `${url}/`
    debugLog('Loading local renderer URL:', url)
    await mainWindow.loadURL(url)
  }

  if (isDev) {
    mainWindow.webContents.openDevTools({ mode: 'detach' })
  }

  try {
    const { createMenu } = require('./menu')
    createMenu(mainWindow)
    debugLog('Menu loaded')
  } catch (e) {
    console.error('Menu load error:', e.message)
  }

  try {
    const { createTray } = require('./tray')
    createTray(mainWindow)
    debugLog('Tray loaded')
  } catch (e) {
    console.error('Tray load error:', e.message)
  }

  try {
    const { initUpdater, checkOnStartup } = require('./updater')
    initUpdater(mainWindow)
    checkOnStartup()
    debugLog('Updater loaded')
  } catch (e) {
    console.error('Updater load error:', e.message)
  }

  setupFileDropOnWindow(mainWindow)
  ensureWindowsNotificationShortcut()
  setupIPC()
  debugLog('IPC setup complete')
}

const MAX_DROP_FILE_BYTES = 80 * 1024 * 1024

function normalizeDroppedPaths(filePaths) {
  if (!Array.isArray(filePaths)) {
    return []
  }
  return filePaths.filter(p => typeof p === 'string' && p.length > 0)
}

function readDroppedFilesPayload(filePaths) {
  const paths = normalizeDroppedPaths(filePaths)
  const result = []
  for (const filePath of paths) {
    try {
      const stat = fs.statSync(filePath)
      if (!stat.isFile()) {
        continue
      }
      if (stat.size > MAX_DROP_FILE_BYTES) {
        continue
      }
      const buffer = fs.readFileSync(filePath)
      const name = path.basename(filePath)
      const ext = path.extname(name).toLowerCase()
      let mimeType = 'application/octet-stream'
      if (['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp'].includes(ext)) {
        mimeType = ext === '.jpg' || ext === '.jpeg' ? 'image/jpeg' : `image/${ext.slice(1)}`
      } else if (ext === '.svg') {
        mimeType = 'image/svg+xml'
      }
      result.push({
        name,
        mimeType,
        size: stat.size,
        data: buffer.toString('base64')
      })
    } catch (error) {
      console.warn('readDroppedFilesPayload skip:', filePath, error.message)
    }
  }
  return result
}

function focusMainWindowFromNotification() {
  if (!mainWindow || mainWindow.isDestroyed()) {
    return
  }
  if (mainWindow.isMinimized()) {
    mainWindow.restore()
  }
  mainWindow.show()
  mainWindow.focus()
  try {
    const { stopWindowAttention } = require('./tray')
    stopWindowAttention()
  } catch {
    /* ignore */
  }
}

function setupFileDropOnWindow(window) {
  if (!window?.webContents) {
    return
  }
  window.webContents.on('will-navigate', (event, url) => {
    if (url && !url.startsWith('devtools://')) {
      const parsed = resolveSafeExternalUrl(url)
      if (parsed) {
        event.preventDefault()
        shell.openExternal(parsed)
      }
    }
  })
  window.webContents.setWindowOpenHandler(({ url }) => {
    const safe = resolveSafeExternalUrl(url)
    if (safe) {
      shell.openExternal(safe)
    }
    return { action: 'deny' }
  })
  window.webContents.on('dom-ready', () => {
    window.webContents.executeJavaScript(`
      (function() {
        if (window.__linkxFileDropBound) return;
        window.__linkxFileDropBound = true;
        document.addEventListener('dragover', (e) => { e.preventDefault(); e.stopPropagation(); });
        document.addEventListener('drop', (e) => {
          e.preventDefault();
          e.stopPropagation();
          const paths = [];
          for (const file of e.dataTransfer?.files || []) {
            if (file.path) paths.push(file.path);
          }
          if (paths.length && window.electronAPI?.readDroppedFiles) {
            window.electronAPI.readDroppedFiles(paths).then((files) => {
              if (files?.length) {
                window.dispatchEvent(new CustomEvent('linkx-electron-file-drop', { detail: files }));
              }
            }).catch(() => {});
          }
        });
      })();
    `).catch(err => console.warn('file drop inject failed:', err.message))
  })
}

// 将窗口控制、通知、剪贴板和系统能力通过 IPC 暴露给渲染进程。
function setupIPC() {
  ipcMain.handle('window:minimize', () => {
    if (mainWindow) mainWindow.minimize()
  })

  ipcMain.handle('window:maximize', () => {
    if (mainWindow) {
      if (mainWindow.isMaximized()) {
        mainWindow.unmaximize()
      } else {
        mainWindow.maximize()
      }
    }
  })

  ipcMain.handle('window:close', () => {
    if (mainWindow) mainWindow.close()
  })

  ipcMain.handle('window:isMaximized', () => {
    return mainWindow ? mainWindow.isMaximized() : false
  })

  mainWindow.on('maximize', () => {
    mainWindow.webContents.send('window:maximized', true)
  })

  mainWindow.on('unmaximize', () => {
    mainWindow.webContents.send('window:maximized', false)
  })

  ipcMain.handle('notification:show', (_, title, body, icon, payload) => {
    const supported = Notification.isSupported()
    const shouldUseTrayBalloonFallback = process.platform === 'win32' && !app.isPackaged
    let shown = false
    const navPayload = payload && typeof payload === 'object' ? payload : null
    if (supported) {
      const notification = new Notification({
        title,
        body,
        silent: false,
        icon: icon || path.join(__dirname, 'icons', 'tray.png')
      })
      notification.on('click', () => {
        focusMainWindowFromNotification()
        if (mainWindow && !mainWindow.isDestroyed() && navPayload?.targetId) {
          mainWindow.webContents.send('notification:navigate', {
            targetId: String(navPayload.targetId),
            sessionType: navPayload.sessionType != null ? Number(navPayload.sessionType) : undefined,
            messageId: navPayload.messageId != null ? String(navPayload.messageId) : undefined
          })
        }
      })
      notification.show()
      shown = true
    }
    if (shouldUseTrayBalloonFallback) {
      try {
        const { showTrayBalloon } = require('./tray')
        shown = showTrayBalloon(title, body) || shown
      } catch (error) {
        console.warn('Tray balloon fallback error:', error)
      }
    }
    if (mainWindow && !mainWindow.isDestroyed() && !mainWindow.isFocused()) {
      try {
        const { startWindowAttention } = require('./tray')
        startWindowAttention()
      } catch (error) {
        console.warn('Window attention fallback error:', error)
      }
    }
    return shown
  })

  ipcMain.handle('notification:playSound', (_, type = 'message') => {
    try {
      shell.beep()
      if (type === 'attention') {
        setTimeout(() => shell.beep(), 180)
      }
      return true
    } catch (error) {
      console.warn('notification:playSound failed:', error)
      return false
    }
  })

  ipcMain.handle('app:versions', () => ({
    electron: process.versions.electron,
    node: process.versions.node,
    chrome: process.versions.chrome,
    app: app.getVersion()
  }))

  ipcMain.handle('app:platform', () => process.platform)

  ipcMain.handle('clipboard:read', () => clipboard.readText())
  ipcMain.handle('clipboard:write', (_, text) => clipboard.writeText(text))

  ipcMain.handle('clipboard:readImage', () => {
    const image = clipboard.readImage()
    if (image.isEmpty()) {
      return null
    }
    const png = image.toPNG()
    return {
      mimeType: 'image/png',
      data: png.toString('base64'),
      size: png.length
    }
  })

  ipcMain.handle('file:readDropped', (_, filePaths) => readDroppedFilesPayload(filePaths))

  ipcMain.handle('tray:setUnreadCount', (_, count) => {
    const next = Math.max(0, Math.min(999, Number(count) || 0))
    trayUnreadCount = next
    try {
      const { setTrayUnreadCount } = require('./tray')
      setTrayUnreadCount(next)
    } catch (error) {
      console.warn('tray:setUnreadCount failed:', error)
    }
    return next
  })
  ipcMain.handle('shell:openExternal', (_, url) => {
    const safeUrl = resolveSafeExternalUrl(url)
    if (!safeUrl) {
      throw new Error('Unsupported external URL')
    }
    return shell.openExternal(safeUrl)
  })

  ipcMain.handle('app:toggleDevTools', () => {
    if (mainWindow) mainWindow.webContents.toggleDevTools()
  })

  ipcMain.handle('app:setAutoLaunch', (_, enable) => {
    app.setLoginItemSettings({ openAtLogin: enable, name: 'LinkX IM' })
  })

  ipcMain.handle('app:getAutoLaunch', () => {
    return app.getLoginItemSettings().openAtLogin
  })

  try {
    const { registerChildWindowIpc } = require('./childWindows')
    registerChildWindowIpc(
      ipcMain,
      () => mainWindow,
      () => appRendererBaseUrl
    )
  } catch (e) {
    console.error('Child windows IPC error:', e.message)
  }
}

if (process.platform === 'win32') {
  app.setAppUserModelId(getRuntimeAppUserModelId())
}

app.whenReady().then(() => {
  debugLog('App ready')
  createWindow().catch(error => {
    console.error('Create window error:', error)
    app.quit()
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})

app.on('before-quit', () => {
  app.isQuitting = true
  stopLocalRendererServer().catch(error => {
    console.error('Stop local renderer server error:', error)
  })
})
