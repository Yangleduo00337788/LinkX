import { ref, type ComputedRef } from 'vue'
import { groupApi } from '../api/client'
import type { GroupMediaItem } from '../types/chat'
import { formatSize } from '../utils/chat'
import { hydrateFileAccessUrls, resolveFileAccessUrl } from '../utils/file-access'
import { openSafeExternalUrl } from '../utils/url'

interface FeedbackApi {
  success: (content: string) => void
  error: (content: string) => void
}

interface UseGroupMembersResourcesOptions {
  groupId: ComputedRef<string>
  message: FeedbackApi
  isGroupUnavailableError: (error: any) => boolean
  handleUnavailableGroup: (messageText: string) => Promise<void>
}

export function useGroupMembersResources(options: UseGroupMembersResourcesOptions) {
  const mediaType = ref<'all' | 'image' | 'file'>('all')
  const mediaKeyword = ref('')
  const groupMediaItems = ref<GroupMediaItem[]>([])
  const groupMediaLoading = ref(false)
  const messageSearchKeyword = ref('')
  const groupMessageSearchResults = ref<GroupMediaItem[]>([])
  const groupMessageSearchLoading = ref(false)

  function resetResourceState() {
    mediaType.value = 'all'
    mediaKeyword.value = ''
    groupMediaItems.value = []
    messageSearchKeyword.value = ''
    groupMessageSearchResults.value = []
  }

  function formatFileSize(fileSize?: number) {
    return formatSize(fileSize) || '0 B'
  }

  function isImageMedia(item: GroupMediaItem) {
    return Number(item.msgType) === 1
  }

  function getMediaTypeText(item: GroupMediaItem) {
    if (Number(item.msgType) === 1) {
      return '图片'
    }
    if (Number(item.msgType) === 2) {
      return '文件'
    }
    if (Number(item.msgType) === 3) {
      return '系统'
    }
    return '消息'
  }

  function getMessageSearchPreview(item: GroupMediaItem) {
    if (Number(item.msgType) === 1) {
      return item.fileName ? `[图片] ${item.fileName}` : '[图片]'
    }
    if (Number(item.msgType) === 2) {
      return item.fileName ? `[文件] ${item.fileName}` : '[文件]'
    }
    if (Number(item.msgType) === 3) {
      return `[系统] ${item.content}`
    }
    return item.content
  }

  async function resolveMediaAccessUrl(item?: GroupMediaItem | null) {
    if (!item?.content) {
      return ''
    }
    if (item.accessUrl) {
      return item.accessUrl
    }
    return resolveFileAccessUrl(item.content)
  }

  async function copyMediaLink(item?: GroupMediaItem | null) {
    const accessUrl = await resolveMediaAccessUrl(item)
    if (!accessUrl) {
      options.message.error('无法生成访问链接')
      return
    }
    try {
      await navigator.clipboard.writeText(accessUrl)
      options.message.success('链接已复制')
    } catch {
      options.message.error('复制链接失败')
    }
  }

  async function openMediaResource(item?: GroupMediaItem | null) {
    const accessUrl = await resolveMediaAccessUrl(item)
    if (!accessUrl) {
      options.message.error('资源访问链接不可用')
      return
    }
    try {
      await openSafeExternalUrl(accessUrl)
    } catch (error: any) {
      options.message.error(error.message || '打开资源失败')
    }
  }

  async function loadGroupMedia() {
    if (!options.groupId.value) {
      groupMediaItems.value = []
      return
    }
    groupMediaLoading.value = true
    try {
      const response: any = await groupApi.getMedia(options.groupId.value, {
        mediaType: mediaType.value,
        keyword: mediaKeyword.value.trim() || undefined,
        size: 200
      })
      groupMediaItems.value = await hydrateFileAccessUrls(response.data || [])
    } catch (error: any) {
      console.error('loadGroupMedia error:', error)
      if (options.isGroupUnavailableError(error)) {
        groupMediaItems.value = []
        await options.handleUnavailableGroup('当前群聊已不可访问')
        return
      }
      options.message.error(error.response?.data?.message || '加载群相册/文件库失败')
    } finally {
      groupMediaLoading.value = false
    }
  }

  async function searchGroupMessages() {
    const keyword = messageSearchKeyword.value.trim()
    if (!options.groupId.value || !keyword) {
      groupMessageSearchResults.value = []
      return
    }
    groupMessageSearchLoading.value = true
    try {
      const response: any = await groupApi.searchMessages(options.groupId.value, keyword, 100)
      groupMessageSearchResults.value = response.data || []
    } catch (error: any) {
      console.error('searchGroupMessages error:', error)
      if (options.isGroupUnavailableError(error)) {
        groupMessageSearchResults.value = []
        await options.handleUnavailableGroup('当前群聊已不可访问')
        return
      }
      options.message.error(error.response?.data?.message || '搜索群消息失败')
    } finally {
      groupMessageSearchLoading.value = false
    }
  }

  return {
    mediaType,
    mediaKeyword,
    groupMediaItems,
    groupMediaLoading,
    messageSearchKeyword,
    groupMessageSearchResults,
    groupMessageSearchLoading,
    resetResourceState,
    formatFileSize,
    isImageMedia,
    getMediaTypeText,
    getMessageSearchPreview,
    copyMediaLink,
    openMediaResource,
    loadGroupMedia,
    searchGroupMessages
  }
}
