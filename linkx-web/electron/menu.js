const { Menu, shell, app } = require('electron')

function createMenu(mainWindow) {
  const template = [
    {
      label: '文件',
      submenu: [
        {
          label: '退出',
          accelerator: 'CmdOrCtrl+Q',
          click: () => {
            app.isQuitting = true
            app.quit()
          }
        }
      ]
    },
    {
      label: '编辑',
      submenu: [
        { label: '撤销', accelerator: 'CmdOrCtrl+Z', role: 'undo' },
        { label: '重做', accelerator: 'Shift+CmdOrCtrl+Z', role: 'redo' },
        { type: 'separator' },
        { label: '剪切', accelerator: 'CmdOrCtrl+X', role: 'cut' },
        { label: '复制', accelerator: 'CmdOrCtrl+C', role: 'copy' },
        { label: '粘贴', accelerator: 'CmdOrCtrl+V', role: 'paste' },
        { label: '全选', accelerator: 'CmdOrCtrl+A', role: 'selectAll' }
      ]
    },
    {
      label: '窗口',
      submenu: [
        {
          label: '最小化',
          accelerator: 'CmdOrCtrl+M',
          click: () => {
            if (mainWindow) mainWindow.minimize()
          }
        },
        {
          label: '最大化',
          accelerator: 'CmdOrCtrl+Shift+F',
          click: () => {
            if (mainWindow) {
              if (mainWindow.isMaximized()) {
                mainWindow.unmaximize()
              } else {
                mainWindow.maximize()
              }
            }
          }
        },
        { type: 'separator' },
        {
          label: '开发者工具',
          accelerator: 'F12',
          click: () => {
            if (mainWindow) mainWindow.webContents.toggleDevTools()
          }
        }
      ]
    },
    {
      label: '帮助',
      submenu: [
        {
          label: '关于 LinkX IM',
          click: () => {
            const { dialog } = require('electron')
            dialog.showMessageBox(mainWindow, {
              type: 'info',
              title: '关于 LinkX IM',
              message: 'LinkX IM',
              detail: `版本: ${app.getVersion()}\nElectron: ${process.versions.electron}\nNode: ${process.versions.node}\nChrome: ${process.versions.chrome}`,
            })
          }
        },
        { type: 'separator' },
        {
          label: '访问 GitHub',
          click: () => {
            shell.openExternal('https://github.com/linkx')
          }
        }
      ]
    }
  ]

  const menu = Menu.buildFromTemplate(template)
  Menu.setApplicationMenu(menu)
}

module.exports = { createMenu }
