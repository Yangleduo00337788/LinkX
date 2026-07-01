/**
 * 独立子窗口（发起群聊、添加朋友、添加群成员等）。
 */
const { BrowserWindow } = require('electron')
const path = require('path')

const childWindows = new Map()

function buildHashUrl(appBaseUrl, routeHash) {
  const base = String(appBaseUrl || '').replace(/#.*$/, '').replace(/\/?$/, '/')
  const hash = routeHash.startsWith('#') ? routeHash : `#${routeHash}`
  return `${base}${hash}`
}

function openChildWindow(mainWindow, getAppBaseUrl, key, options) {
  const { width, height, routeHash, title = 'LinkX IM' } = options
  const existing = childWindows.get(key)
  if (existing && !existing.isDestroyed()) {
    existing.focus()
    return existing
  }

  const iconPath = path.join(__dirname, 'icons', 'tray.png')
  const win = new BrowserWindow({
    width,
    height,
    minWidth: width,
    minHeight: height,
    maxWidth: width,
    maxHeight: height,
    resizable: false,
    title,
    parent: mainWindow && !mainWindow.isDestroyed() ? mainWindow : undefined,
    modal: false,
    show: false,
    frame: true,
    backgroundColor: '#ededed',
    icon: iconPath,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js'),
      spellcheck: false
    }
  })

  win.setMenuBarVisibility(false)
  const url = buildHashUrl(getAppBaseUrl(), routeHash)
  void win.loadURL(url)
  win.once('ready-to-show', () => win.show())
  win.on('closed', () => {
    childWindows.delete(key)
  })
  childWindows.set(key, win)
  return win
}

function registerChildWindowIpc(ipcMain, getMainWindow, getAppBaseUrl) {
  ipcMain.handle('child-window:open', (_, payload) => {
    const main = typeof getMainWindow === 'function' ? getMainWindow() : null
    const type = payload?.type
    if (type === 'create-group') {
      openChildWindow(main, getAppBaseUrl, 'create-group', {
        width: 718,
        height: 427,
        routeHash: '#/child/create-group',
        title: '发起群聊'
      })
      return true
    }
    if (type === 'add-friend') {
      openChildWindow(main, getAppBaseUrl, 'add-friend', {
        width: 360,
        height: 560,
        routeHash: '#/child/add-friend',
        title: '添加朋友'
      })
      return true
    }
    if (type === 'add-group-members') {
      const groupId = encodeURIComponent(String(payload?.groupId || ''))
      openChildWindow(main, getAppBaseUrl, `add-members-${groupId}`, {
        width: 718,
        height: 427,
        routeHash: `#/child/add-group-members?groupId=${groupId}`,
        title: '添加群成员'
      })
      return true
    }
    return false
  })

  ipcMain.handle('child-window:close-self', () => {
    const win = BrowserWindow.getFocusedWindow()
    if (win && win !== getMainWindow?.()) {
      win.close()
      return true
    }
    return false
  })
}

module.exports = { openChildWindow, registerChildWindowIpc, childWindows }