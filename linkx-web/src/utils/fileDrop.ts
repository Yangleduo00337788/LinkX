import { getElectronAPI } from './electron'

type FileDropCallback = (files: File[]) => void

export function setupFileDrop(callback: FileDropCallback): () => void {
  const api = getElectronAPI()

  if (api) {
    api.onFileDrop((files: string[]) => {
      const fileObjects = files.map(path => {
        const name = path.split(/[/\\]/).pop() || 'unknown'
        return new File([''], name, { type: 'application/octet-stream' })
      })
      callback(fileObjects)
    })

    return () => {
      api.removeFileDropListener()
    }
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
