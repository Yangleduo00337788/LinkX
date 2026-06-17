const { app, BrowserWindow, ipcMain, Notification, nativeImage, clipboard, shell } = require('electron')
const fs = require('fs')
const http = require('http')
const path = require('path')

let mainWindow = null
let isDev = !!process.env.ELECTRON_DEV_URL
let localRendererServer = null
const LOCAL_RENDERER_HOST = '127.0.0.1'
const LOCAL_RENDERER_PORT = Number(process.env.LINKX_ELECTRON_PORT || 39689)
const APP_USER_MODEL_ID = 'com.linkx.im'
const TOAST_ACTIVATOR_CLSID = '2E35D3A8-5D13-4D74-A824-6C6C3D4B8F71'

function getRuntimeAppUserModelId() {
  return app.isPackaged ? APP_USER_MODEL_ID : process.execPath
}

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

function resolveContentType(filePath) {
  return STATIC_MIME_TYPES[path.extname(filePath).toLowerCase()] || 'application/octet-stream'
}

function createStaticRequestHandler(distDir) {
  return (request, response) => {
    const requestUrl = new URL(request.url || '/', `http://${LOCAL_RENDERER_HOST}:${LOCAL_RENDERER_PORT}`)
    const rawPath = decodeURIComponent(requestUrl.pathname)
    const normalizedPath = rawPath === '/' ? '/index.html' : rawPath
    const requestedFilePath = path.normalize(path.join(distDir, normalizedPath))

    if (!requestedFilePath.startsWith(distDir)) {
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
      console.log('Local renderer server started:', url)
      resolve(url)
    })
  })
}

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

async function createWindow() {
  console.log('Creating window...')
  console.log('isDev:', isDev)
  console.log('ELECTRON_DEV_URL:', process.env.ELECTRON_DEV_URL)

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
    console.log('Window ready to show')
    mainWindow.show()
  })

  mainWindow.webContents.on('did-fail-load', (event, errorCode, errorDescription) => {
    console.error('Failed to load:', errorCode, errorDescription)
  })

  mainWindow.webContents.on('console-message', (event, level, message) => {
    console.log('Renderer:', message)
  })

  if (isDev) {
    const url = process.env.ELECTRON_DEV_URL
    console.log('Loading URL:', url)
    await mainWindow.loadURL(url)
  } else {
    const url = await startLocalRendererServer()
    console.log('Loading local renderer URL:', url)
    await mainWindow.loadURL(url)
  }

  if (isDev) {
    mainWindow.webContents.openDevTools({ mode: 'detach' })
  }

  try {
    const { createMenu } = require('./menu')
    createMenu(mainWindow)
    console.log('Menu loaded')
  } catch (e) {
    console.error('Menu load error:', e.message)
  }

  try {
    const { createTray } = require('./tray')
    createTray(mainWindow)
    console.log('Tray loaded')
  } catch (e) {
    console.error('Tray load error:', e.message)
  }

  try {
    const { initUpdater, checkOnStartup } = require('./updater')
    initUpdater(mainWindow)
    checkOnStartup()
    console.log('Updater loaded')
  } catch (e) {
    console.error('Updater load error:', e.message)
  }

  ensureWindowsNotificationShortcut()
  setupIPC()
  console.log('IPC setup complete')
}

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

  ipcMain.handle('notification:show', (_, title, body, icon) => {
    const supported = Notification.isSupported()
    const shouldUseTrayBalloonFallback = process.platform === 'win32' && !app.isPackaged
    let shown = false
    if (supported) {
      const notification = new Notification({
        title,
        body,
        silent: false,
        icon: icon || path.join(__dirname, 'icons', 'tray.png')
      })
      notification.on('click', () => {
        if (!mainWindow) {
          return
        }
        if (mainWindow.isMinimized()) {
          mainWindow.restore()
        }
        mainWindow.show()
        mainWindow.focus()
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
  ipcMain.handle('shell:openExternal', (_, url) => shell.openExternal(url))

  ipcMain.handle('app:toggleDevTools', () => {
    if (mainWindow) mainWindow.webContents.toggleDevTools()
  })

  ipcMain.handle('app:setAutoLaunch', (_, enable) => {
    app.setLoginItemSettings({ openAtLogin: enable, name: 'LinkX IM' })
  })

  ipcMain.handle('app:getAutoLaunch', () => {
    return app.getLoginItemSettings().openAtLogin
  })
}

if (process.platform === 'win32') {
  app.setAppUserModelId(getRuntimeAppUserModelId())
}

app.whenReady().then(() => {
  console.log('App ready')
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
