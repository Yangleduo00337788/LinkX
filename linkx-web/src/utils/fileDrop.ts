/**
 * fileDrop 工具模块，封装当前前端场景下的通用方法。
 */
import { getElectronAPI } from './electron'  // 行注：引入 getElectronAPI 能力

type FileDropCallback = (files: File[]) => void  // 行注：执行当前调用逻辑

export function setupFileDrop(callback: FileDropCallback): () => void {  // 行注：导出当前能力
  const api = getElectronAPI()  // 行注：初始化 api 实例

  if (api) {  // 行注：判断当前条件是否成立
    api.onFileDrop((files: string[]) => {  // 行注：调用 onFileDrop 方法
      const fileObjects = files.map(path => {  // 行注：开始解构当前返回值
        const name = path.split(/[/\\]/).pop() || 'unknown'  // 行注：初始化 name 变量
        return new File([''], name, { type: 'application/octet-stream' })  // 行注：返回当前结果
      })  // 行注：结束当前调用配置
      callback(fileObjects)  // 行注：调用 callback 方法
    })  // 行注：结束当前调用配置

    return () => {  // 行注：返回当前结果
      api.removeFileDropListener()  // 行注：调用 removeFileDropListener 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  const handleDragOver = (e: DragEvent) => {  // 行注：定义 handleDragOver 方法
    e.preventDefault()  // 行注：调用 preventDefault 方法
    e.stopPropagation()  // 行注：调用 stopPropagation 方法
  }  // 行注：结束当前代码块

  const handleDrop = (e: DragEvent) => {  // 行注：定义 handleDrop 方法
    e.preventDefault()  // 行注：调用 preventDefault 方法
    e.stopPropagation()  // 行注：调用 stopPropagation 方法

    const files = Array.from(e.dataTransfer?.files || [])  // 行注：初始化 files 变量
    if (files.length > 0) {  // 行注：判断当前条件是否成立
      callback(files)  // 行注：调用 callback 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  document.addEventListener('dragover', handleDragOver)  // 行注：注册dragover事件监听
  document.addEventListener('drop', handleDrop)  // 行注：注册drop事件监听

  return () => {  // 行注：返回当前结果
    document.removeEventListener('dragover', handleDragOver)  // 行注：移除dragover事件监听
    document.removeEventListener('drop', handleDrop)  // 行注：移除drop事件监听
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export function isImageFile(file: File): boolean {  // 行注：导出当前能力
  return file.type.startsWith('image/')  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function isVideoFile(file: File): boolean {  // 行注：导出当前能力
  return file.type.startsWith('video/')  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function isAudioFile(file: File): boolean {  // 行注：导出当前能力
  return file.type.startsWith('audio/')  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function formatFileSize(bytes: number): string {  // 行注：导出当前能力
  if (bytes === 0) return '0 B'  // 行注：判断当前条件是否成立
  const k = 1024  // 行注：初始化 k 变量
  const sizes = ['B', 'KB', 'MB', 'GB']  // 行注：初始化 sizes 变量
  const i = Math.floor(Math.log(bytes) / Math.log(k))  // 行注：初始化 i 变量
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function getFileExtension(filename: string): string {  // 行注：导出当前能力
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2).toLowerCase()  // 行注：返回当前结果
}  // 行注：结束当前代码块
