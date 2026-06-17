const { Tray, Menu, nativeImage, app } = require('electron')
const path = require('path')
const fs = require('fs')

let tray = null
let mainWindow = null
let trayIcon = null
const MAX_BALLOON_CONTENT_LENGTH = 160

function focusMainWindow() {
  if (!mainWindow || mainWindow.isDestroyed()) {
    return
  }
  if (mainWindow.isMinimized()) {
    mainWindow.restore()
  }
  mainWindow.show()
  mainWindow.focus()
  stopWindowAttention()
}

function stopWindowAttention() {
  if (!mainWindow || mainWindow.isDestroyed()) {
    return
  }
  mainWindow.flashFrame(false)
}

function startWindowAttention() {
  if (!mainWindow || mainWindow.isDestroyed()) {
    return false
  }
  mainWindow.flashFrame(true)
  return true
}

function normalizeBalloonContent(content) {
  const text = String(content || '').trim()
  if (!text) {
    return '你有一条新消息'
  }
  if (text.length <= MAX_BALLOON_CONTENT_LENGTH) {
    return text
  }
  return `${text.slice(0, MAX_BALLOON_CONTENT_LENGTH - 1)}...`
}

function createTray(window) {
  mainWindow = window

  const iconPath = path.join(__dirname, 'icons', 'tray.png')
  let icon

  if (fs.existsSync(iconPath)) {
    icon = nativeImage.createFromPath(iconPath)
  } else {
    icon = nativeImage.createEmpty()
  }
  trayIcon = icon

  tray = new Tray(icon)
  tray.setToolTip('LinkX IM')

  updateTrayMenu()

  tray.on('click', () => {
    focusMainWindow()
  })

  mainWindow.on('close', (e) => {
    if (!app.isQuitting) {
      e.preventDefault()
      mainWindow.hide()
    }
  })
  mainWindow.on('focus', stopWindowAttention)
  mainWindow.on('show', stopWindowAttention)
  mainWindow.on('restore', stopWindowAttention)

  return tray
}

function updateTrayMenu() {
  const contextMenu = Menu.buildFromTemplate([
    {
      label: '显示窗口',
      click: () => {
        if (mainWindow) {
          mainWindow.show()
          mainWindow.focus()
        }
      }
    },
    { type: 'separator' },
    {
      label: '退出',
      click: () => {
        app.isQuitting = true
        app.quit()
      }
    }
  ])

  tray.setContextMenu(contextMenu)
}

function destroyTray() {
  if (tray) {
    tray.destroy()
    tray = null
  }
  trayIcon = null
}

function showTrayBalloon(title, body) {
  if (process.platform !== 'win32' || !tray || typeof tray.displayBalloon !== 'function') {
    return false
  }
  try {
    tray.displayBalloon({
      title: String(title || 'LinkX IM'),
      content: normalizeBalloonContent(body),
      icon: trayIcon,
      iconType: 'custom',
      noSound: true
    })
    return true
  } catch (error) {
    console.warn('showTrayBalloon failed:', error)
    return false
  }
}

module.exports = { createTray, destroyTray, showTrayBalloon, startWindowAttention, stopWindowAttention }
