/**
 * 封装桌面端自动更新检查、下载与安装流程。
 */
const { autoUpdater } = require('electron-updater')
const { app, ipcMain, dialog } = require('electron')

let log
try {
  log = require('electron-log')
  autoUpdater.logger = log
} catch (e) {
  log = console
}

autoUpdater.autoDownload = false
autoUpdater.autoInstallOnAppQuit = true

let mainWindow = null

function initUpdater(window) {
  mainWindow = window

  autoUpdater.on('checking-for-update', () => {
    console.log('Checking for update...')
    sendToRenderer('updater:checking')
  })

  autoUpdater.on('update-available', (info) => {
    console.log('Update available:', info.version)
    sendToRenderer('updater:available', {
      version: info.version,
      releaseDate: info.releaseDate,
      releaseNotes: info.releaseNotes
    })

    dialog.showMessageBox(mainWindow, {
      type: 'info',
      title: '发现新版本',
      message: `发现新版本 ${info.version}`,
      detail: info.releaseNotes || '是否立即下载更新？',
      buttons: ['下载更新', '稍后再说'],
      defaultId: 0,
      cancelId: 1
    }).then(({ response }) => {
      if (response === 0) {
        autoUpdater.downloadUpdate()
      }
    })
  })

  autoUpdater.on('update-not-available', () => {
    console.log('Update not available')
    sendToRenderer('updater:not-available')
  })

  autoUpdater.on('download-progress', (progress) => {
    console.log(`Download progress: ${Math.round(progress.percent)}%`)
    sendToRenderer('updater:progress', {
      percent: progress.percent,
      transferred: progress.transferred,
      total: progress.total,
      bytesPerSecond: progress.bytesPerSecond
    })
  })

  autoUpdater.on('update-downloaded', (info) => {
    console.log('Update downloaded:', info.version)
    sendToRenderer('updater:downloaded', { version: info.version })

    dialog.showMessageBox(mainWindow, {
      type: 'info',
      title: '更新已就绪',
      message: '新版本已下载完成',
      detail: '应用将在重启后更新到新版本。是否立即重启？',
      buttons: ['立即重启', '稍后重启'],
      defaultId: 0,
      cancelId: 1
    }).then(({ response }) => {
      if (response === 0) {
        autoUpdater.quitAndInstall()
      }
    })
  })

  autoUpdater.on('error', (err) => {
    console.error('Update error:', err.message)
    sendToRenderer('updater:error', { message: err.message })
  })

  ipcMain.handle('updater:check', async () => {
    try {
      const result = await autoUpdater.checkForUpdates()
      return { available: !!result?.updateInfo, version: result?.updateInfo?.version }
    } catch (err) {
      console.error('Check for updates failed:', err.message)
      return { available: false, error: err.message }
    }
  })

  ipcMain.handle('updater:install', () => {
    autoUpdater.quitAndInstall()
  })

  ipcMain.handle('updater:download', () => {
    autoUpdater.downloadUpdate()
  })

  ipcMain.handle('updater:cancel', () => {
    autoUpdater.autoDownload = false
  })
}

function sendToRenderer(channel, data) {
  if (mainWindow && !mainWindow.isDestroyed()) {
    mainWindow.webContents.send(channel, data)
  }
}

function checkOnStartup() {
  if (app.isPackaged) {
    setTimeout(() => {
      autoUpdater.checkForUpdates().catch(err => {
        console.error('Startup update check failed:', err.message)
      })
    }, 3000)
  }
}

module.exports = { initUpdater, checkOnStartup }
