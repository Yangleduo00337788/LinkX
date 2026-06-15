const fs = require('node:fs')
const path = require('node:path')
const { spawn } = require('node:child_process')

const projectRoot = path.resolve(__dirname, '..')
const electronDist = path.join(projectRoot, 'node_modules', 'electron', 'dist')
const cacheDir = path.join(projectRoot, '.electron-builder-cache')
const cliPath = path.join(projectRoot, 'node_modules', 'electron-builder', 'out', 'cli', 'cli.js')
const trayPngPath = path.join(projectRoot, 'electron', 'icons', 'tray.png')
const appIcoPath = path.join(projectRoot, 'electron', 'icons', 'app.ico')

function fail(message) {
  console.error(`[electron-builder] ${message}`)
  process.exit(1)
}

function readPngDimensions(pngBuffer) {
  const pngSignature = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a])

  if (!pngBuffer.subarray(0, 8).equals(pngSignature)) {
    fail(`invalid PNG source: ${trayPngPath}`)
  }

  return {
    width: pngBuffer.readUInt32BE(16),
    height: pngBuffer.readUInt32BE(20),
  }
}

function ensureWindowsIcon() {
  if (fs.existsSync(appIcoPath)) {
    return
  }

  if (!fs.existsSync(trayPngPath)) {
    fail(`tray icon not found: ${trayPngPath}`)
  }

  const pngBuffer = fs.readFileSync(trayPngPath)
  const { width, height } = readPngDimensions(pngBuffer)

  if (width < 256 || height < 256) {
    fail(`Windows icon source must be at least 256x256, received ${width}x${height}`)
  }

  const icoHeader = Buffer.alloc(22)
  icoHeader.writeUInt16LE(0, 0)
  icoHeader.writeUInt16LE(1, 2)
  icoHeader.writeUInt16LE(1, 4)
  icoHeader.writeUInt8(0, 6)
  icoHeader.writeUInt8(0, 7)
  icoHeader.writeUInt8(0, 8)
  icoHeader.writeUInt8(0, 9)
  icoHeader.writeUInt16LE(1, 10)
  icoHeader.writeUInt16LE(32, 12)
  icoHeader.writeUInt32LE(pngBuffer.length, 14)
  icoHeader.writeUInt32LE(22, 18)

  fs.writeFileSync(appIcoPath, Buffer.concat([icoHeader, pngBuffer]))
}

if (!fs.existsSync(cliPath)) {
  fail('electron-builder CLI not found. Please run npm install first.')
}

if (!fs.existsSync(electronDist)) {
  fail('local Electron distribution not found at node_modules/electron/dist.')
}

ensureWindowsIcon()
fs.mkdirSync(cacheDir, { recursive: true })

const args = [`-c.electronDist=${electronDist}`, ...process.argv.slice(2)]

const child = spawn(process.execPath, [cliPath, ...args], {
  cwd: projectRoot,
  stdio: 'inherit',
  env: {
    ...process.env,
    // Keep builder artifacts inside the project to avoid unstable user-profile caches.
    ELECTRON_BUILDER_CACHE: cacheDir,
    ELECTRON_OVERRIDE_DIST_PATH: electronDist,
  },
})

child.on('error', (error) => {
  fail(error.message)
})

child.on('exit', (code) => {
  process.exit(code ?? 1)
})
