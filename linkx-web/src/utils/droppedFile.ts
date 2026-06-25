/**
 * 将主进程读取的拖拽文件载荷转为浏览器 File 对象。
 */
export interface DroppedFilePayload {
  name: string
  mimeType: string
  size: number
  data: string
}

export function base64PayloadToFile(payload: DroppedFilePayload): File {
  const binary = atob(payload.data)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i)
  }
  return new File([bytes], payload.name, { type: payload.mimeType || 'application/octet-stream' })
}

export function isImageMime(mime: string): boolean {
  return mime.startsWith('image/')
}