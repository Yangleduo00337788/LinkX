const { app, BrowserWindow, ipcMain, Notification, nativeImage, clipboard, shell } = require('electron')
const path = require('path')

let mainWindow = null
let isDev = !!process.env.ELECTRON_DEV_URL

function createWindow() {
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
    mainWindow.loadURL(url)
  } else {
    const distPath = path.join(__dirname, '..', 'dist', 'index.html')
    console.log('Loading file:', distPath)
    console.log('File exists:', require('fs').existsSync(distPath))
    mainWindow.loadFile(distPath)
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
    const { createTray, destroyTray } = require('./tray')
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
    if (Notification.isSupported()) {
      const notification = new Notification({ 
        title, 
        body, 
        silent: false,
        icon: icon || path.join(__dirname, 'icons', 'tray.png')
      })
      notification.show()
      return true
    }
    return false
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

app.whenReady().then(() => {
  console.log('App ready')
  createWindow()
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})

app.on('before-quit', () => {
  app.isQuitting = true
})
