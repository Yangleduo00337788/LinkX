/**
 * 配置系统托盘、托盘菜单和窗口提醒行为。
 */
const { Tray, Menu, nativeImage, app } = require('electron')
const path = require('path')
const fs = require('fs')

let tray = null
let mainWindow = null
let trayIcon = null
let trayUnreadCount = 0
const MAX_BALLOON_CONTENT_LENGTH = 160
const TRAY_TOOLTIP_BASE = 'LinkX IM'

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

// 创建系统托盘图标和托盘菜单，支持最小化驻留与快速操作。
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
  applyTrayTooltip()

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

function applyTrayTooltip() {
  if (!tray) {
    return
  }
  if (trayUnreadCount > 0) {
    tray.setToolTip(`${TRAY_TOOLTIP_BASE}（${trayUnreadCount > 99 ? '99+' : trayUnreadCount} 条未读）`)
  } else {
    tray.setToolTip(TRAY_TOOLTIP_BASE)
  }
}

function setTrayUnreadCount(count) {
  trayUnreadCount = Math.max(0, Math.min(999, Number(count) || 0))
  applyTrayTooltip()
  updateTrayMenu()
}

function updateTrayMenu() {
  if (!tray) {
    return
  }
  const unreadLabel = trayUnreadCount > 0
    ? `未读消息：${trayUnreadCount > 99 ? '99+' : trayUnreadCount} 条`
    : '暂无未读消息'

  const contextMenu = Menu.buildFromTemplate([
    { label: unreadLabel, enabled: false },
    { type: 'separator' },
    {
      label: '显示主窗口',
      click: () => focusMainWindow()
    },
    {
      label: '打开聊天',
      click: () => {
        focusMainWindow()
        if (mainWindow && !mainWindow.isDestroyed()) {
          mainWindow.webContents.send('tray:action', { action: 'open-chat' })
        }
      }
    },
    { type: 'separator' },
    {
      label: '退出 LinkX',
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

module.exports = {
  createTray,
  destroyTray,
  showTrayBalloon,
  startWindowAttention,
  stopWindowAttention,
  setTrayUnreadCount,
  focusMainWindow
}
