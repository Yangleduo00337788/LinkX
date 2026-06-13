const { app, BrowserWindow } = require('electron')
const path = require('path')

function createWindow() {
  const win = new BrowserWindow({
    width: 960,
    height: 650,
    minWidth: 800,
    minHeight: 500,
    title: 'LinkX IM',
    icon: path.join(__dirname, '../public/favicon.svg'),
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true
    },
    titleBarStyle: 'default',
    frame: true
  })

  if (process.env.ELECTRON_DEV_URL) {
    win.loadURL(process.env.ELECTRON_DEV_URL)
  } else {
    win.loadFile(path.join(__dirname, '../dist/index.html'))
  }
}

app.whenReady().then(createWindow)

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})
