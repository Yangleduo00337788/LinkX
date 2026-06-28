#!/usr/bin/env node
/**
 * 对比 linkx-server 暴露的 HTTP 路径与 linkx-web / linkx-admin 中的 /api 引用。
 * 用法：node scripts/api-audit.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')

const SERVER_JAVA = path.join(root, 'linkx-server/src/main/java')
const WEB_SRC = path.join(root, 'linkx-web/src')
const ADMIN_SRC = path.join(root, 'linkx-admin/src')

function walk(dir, ext, out = []) {
  if (!fs.existsSync(dir)) return out
  for (const name of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, name.name)
    if (name.isDirectory()) walk(p, ext, out)
    else if (name.name.endsWith(ext)) out.push(p)
  }
  return out
}

function normalizeApiPath(raw) {
  let s = raw.trim()
  if (!s.startsWith('/api')) return null
  s = s.split('?')[0]
  s = s.replace(/\$\{[^}]+\}/g, '{id}')
  s = s.replace(/`[^`]*\$\{[^}]+\}[^`]*`/g, (m) => m.replace(/\$\{[^}]+\}/g, '{id}'))
  if (!s.startsWith('/api')) {
    const m = s.match(/\/api\/[\w\-/{}]+/)
    s = m ? m[0] : s
  }
  return s.replace(/\/+/g, '/').replace(/\/$/, '') || '/api'
}

function collectBackend() {
  const paths = new Set()
  const classPrefix = new Map()
  const files = walk(SERVER_JAVA, '.java')
  const mappingRe = /@(Get|Post|Put|Delete|Patch)Mapping(?:\(\s*(?:value\s*=\s*)?["']([^"']+)["'])?/g
  const classReqRe = /@RequestMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']/g

  for (const file of files) {
    const text = fs.readFileSync(file, 'utf8')
    if (!text.includes('Mapping')) continue
    let classBase = ''
    const cm = text.match(/@RequestMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']/)
    if (cm) classBase = cm[1].replace(/\/$/, '')

    let m
    const re = /@(Get|Post|Put|Delete|Patch)Mapping(?:\(\s*(?:value\s*=\s*)?["']([^"']*)["'])?/g
    while ((m = re.exec(text)) !== null) {
      const sub = (m[2] ?? '').trim()
      let full = classBase
      if (sub) full = `${classBase}/${sub}`.replace(/\/+/g, '/')
      if (full.startsWith('/api')) paths.add(full.replace(/\/$/, '') || full)
    }
    const bare = /@(Get|Post|Put|Delete|Patch)Mapping\s*\(\s*\)/g
    while (bare.exec(text)) {
      if (classBase.startsWith('/api')) paths.add(classBase)
    }
  }
  return paths
}

function collectFrontend(dir, label) {
  const paths = new Set()
  const exts = ['.ts', '.vue', '.js']
  for (const ext of exts) {
    for (const file of walk(dir, ext)) {
      const text = fs.readFileSync(file, 'utf8')
      const re = /['"`](\/api\/[^'"`?]+)['"`]/g
      let m
      while ((m = re.exec(text)) !== null) {
        const n = normalizeApiPath(m[1])
        if (n) paths.add(n)
      }
      const tpl = /api\.(get|post|put|delete)\(\s*[`'"]([^`'"]+)/gi
      while ((m = tpl.exec(text)) !== null) {
        const n = normalizeApiPath(m[2])
        if (n) paths.add(n)
      }
    }
  }
  return { label, paths }
}

function matchBackend(frontPath, backendSet) {
  if (backendSet.has(frontPath)) return true
  const prefix = frontPath.replace(/\{id\}/g, '')
  for (const b of backendSet) {
    if (frontPath.startsWith(b) || b.startsWith(frontPath.split('/').slice(0, -1).join('/'))) {
      const pattern = b.replace(/\{[^}]+\}/g, '[^/]+').replace(/\$\{[^}]+\}/g, '[^/]+')
      try {
        const re = new RegExp(`^${pattern.replace(/\//g, '\\/')}$`)
        const test = frontPath.replace(/\/\d+/g, '/{id}')
        if (re.test(test) || re.test(frontPath)) return true
      } catch {
        /* ignore */
      }
    }
    const bParts = b.split('/')
    const fParts = frontPath.split('/')
    if (bParts.length === fParts.length) {
      let ok = true
      for (let i = 0; i < bParts.length; i++) {
        if (bParts[i] !== fParts[i] && !bParts[i].includes('{') && fParts[i] !== '{id}' && !/^\d+$/.test(fParts[i])) {
          ok = false
          break
        }
      }
      if (ok) return true
    }
  }
  for (const b of backendSet) {
    const re = new RegExp(
      '^' +
        b
          .split('/')
          .map((seg) => (seg.startsWith('{') ? '[^/]+' : seg.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')))
          .join('/') +
        '$'
    )
    const normalized = frontPath.replace(/\/\d+/g, '/999')
    const backendNorm = b.replace(/\{[^}]+\}/g, '999')
    if (re.test(frontPath) || re.test(normalized) || frontPath.startsWith(b.split('{')[0])) return true
  }
  return false
}

function main() {
  const backend = collectBackend()
  const web = collectFrontend(WEB_SRC, 'linkx-web')
  const admin = collectFrontend(ADMIN_SRC, 'linkx-admin')
  const allFront = new Set([...web.paths, ...admin.paths])

  const unusedBackend = [...backend].filter((b) => {
    for (const f of allFront) {
      if (matchBackend(f, new Set([b])) || f === b || f.startsWith(b + '/') || b.startsWith(f)) return false
      const fNorm = f.replace(/\/\d+/g, '/{id}')
      const bNorm = b
      if (fNorm === bNorm) return false
      if (f.startsWith(b.replace(/\{[^}]+}/g, ''))) return false
    }
    return ! [...allFront].some((f) => pathMatches(f, b))
  })

  const missingBackend = [...allFront].filter((f) => !pathMatches(f, backend))

  console.log('=== LinkX API 对账 ===\n')
  console.log(`后端路径（去重）: ${backend.size}`)
  console.log(`Web 引用: ${web.paths.size}  Admin 引用: ${admin.paths.size}\n`)

  if (missingBackend.length) {
    console.log('--- 前端引用但后端未匹配（需人工核对动态路径）---')
    missingBackend.sort().forEach((p) => console.log('  ', p))
    console.log()
  } else {
    console.log('--- 前端 /api 引用均能在后端找到对应前缀 ---\n')
  }

  if (unusedBackend.length) {
    console.log('--- 后端存在、前端未直接引用（可能仅 WS/内部使用）---')
    unusedBackend.sort().forEach((p) => console.log('  ', p))
    console.log()
  }

  const legacyNote = [
    '说明：AdminFriendController / AdminConversationController 等若不存在于仓库，',
    '则无需清理；朋友圈/通话等 /api/posts 等路径当前仓库无前端引用。'
  ]
  console.log(legacyNote.join('\n'))
}

function pathMatches(front, backendSet) {
  const f = front.replace(/\/\d+/g, '/{param}')
  for (const b of backendSet) {
    if (front === b) return true
    const segments = b.split('/')
    const fseg = front.split('/')
    if (segments.length !== fseg.length) continue
    let ok = true
    for (let i = 0; i < segments.length; i++) {
      const bs = segments[i]
      const fs = fseg[i]
      if (bs === fs) continue
      if (bs.startsWith('{') || /^\d+$/.test(fs)) continue
      ok = false
      break
    }
    if (ok) return true
  }
  for (const b of backendSet) {
    if (front.startsWith(b.split('{')[0])) return true
    const prefix = b.replace(/\{[^}]+\}/g, '')
    if (prefix && front.startsWith(prefix.replace(/\/$/, ''))) return true
  }
  return false
}

main()