/**
 * fileDrop 工具模块，封装当前前端场景下的通用方法。
 */
import { getElectronAPI } from './electron'
import type { DroppedFilePayload } from './droppedFile'
import { base64PayloadToFile } from './droppedFile'

type FileDropCallback = (files: File[]) => void

const ELECTRON_DROP_EVENT = 'linkx-electron-file-drop'

export function setupFileDrop(callback: FileDropCallback): () => void {
  const api = getElectronAPI()

  const handleElectronDrop = (event: Event) => {
    const custom = event as CustomEvent<DroppedFilePayload[]>
    const payloads = custom.detail
    if (!Array.isArray(payloads) || payloads.length === 0) {
      return
    }
    const files = payloads.map(base64PayloadToFile)
    callback(files)
  }

  window.addEventListener(ELECTRON_DROP_EVENT, handleElectronDrop)

  if (api?.onFileDrop) {
    api.onFileDrop((paths: string[]) => {
      if (!api.readDroppedFiles || !paths?.length) {
        return
      }
      void api.readDroppedFiles(paths).then(payloads => {
        if (payloads?.length) {
          callback(payloads.map(base64PayloadToFile))
        }
      })
    })
  }

  const handleDragOver = (e: DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
  }

  const handleDrop = (e: DragEvent) => {
    e.preventDefault()
    e.stopPropagation()

    const files = Array.from(e.dataTransfer?.files || [])
    if (files.length > 0) {
      callback(files)
    }
  }

  document.addEventListener('dragover', handleDragOver)
  document.addEventListener('drop', handleDrop)

  return () => {
    window.removeEventListener(ELECTRON_DROP_EVENT, handleElectronDrop)
    api?.removeFileDropListener?.()
    document.removeEventListener('dragover', handleDragOver)
    document.removeEventListener('drop', handleDrop)
  }
}

export function isImageFile(file: File): boolean {
  return file.type.startsWith('image/')
}

export function isVideoFile(file: File): boolean {
  return file.type.startsWith('video/')
}

export function isAudioFile(file: File): boolean {
  return file.type.startsWith('audio/')
}

export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export function getFileExtension(filename: string): string {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2).toLowerCase()
}