const { Tray, Menu, nativeImage, app } = require('electron')
const path = require('path')
const fs = require('fs')

let tray = null
let mainWindow = null

function createTray(window) {
  mainWindow = window

  const iconPath = path.join(__dirname, 'icons', 'tray.png')
  let icon

  if (fs.existsSync(iconPath)) {
    icon = nativeImage.createFromPath(iconPath)
  } else {
    icon = nativeImage.createEmpty()
  }

  tray = new Tray(icon)
  tray.setToolTip('LinkX IM')

  updateTrayMenu()

  tray.on('click', () => {
    if (mainWindow) {
      if (mainWindow.isVisible()) {
        mainWindow.focus()
      } else {
        mainWindow.show()
      }
    }
  })

  mainWindow.on('close', (e) => {
    if (!app.isQuitting) {
      e.preventDefault()
      mainWindow.hide()
    }
  })

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
}

module.exports = { createTray, destroyTray }
