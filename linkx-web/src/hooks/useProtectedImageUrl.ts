/**
 * 将 sys_file 的 fileUrl（/uploads/...）解析为可展示的 blob URL。
 */
import { ref, watch, onUnmounted, type Ref } from 'vue'
import { resolveFileAccessUrl } from '../utils/file-access'

function isDirectImageSrc(url: string) {
  const t = url.trim()
  return t.startsWith('blob:') || t.startsWith('data:')
}

export function useProtectedImageUrl(fileUrlRef: Ref<string | undefined | null>) {
  const displayUrl = ref('')

  watch(
    fileUrlRef,
    async (raw, _prev, onCleanup) => {
      let cancelled = false
      onCleanup(() => {
        cancelled = true
      })

      const normalized = raw?.trim() || ''
      if (!normalized) {
        displayUrl.value = ''
        return
      }
      if (isDirectImageSrc(normalized)) {
        displayUrl.value = normalized
        return
      }

      displayUrl.value = ''
      const blob = await resolveFileAccessUrl(normalized, true)
      if (!cancelled) {
        displayUrl.value = blob || ''
      }
    },
    { immediate: true }
  )

  onUnmounted(() => {
    displayUrl.value = ''
  })

  return displayUrl
}