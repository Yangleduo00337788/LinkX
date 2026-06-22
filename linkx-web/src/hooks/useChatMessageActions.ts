import { ref, type ComputedRef, type Ref } from 'vue'
import { fileApi } from '../api/client'
import { resolveFileAccessUrl } from '../utils/file-access'
import { getDateTimeTimestamp } from '../utils/datetime'
import { openSafeExternalUrl, resolveSafeDownloadUrl, triggerSafeDownload } from '../utils/url'
import {
  MESSAGE_STATUS_RECALLED,
  MESSAGE_TYPE_FILE,
  MESSAGE_TYPE_IMAGE,
  MESSAGE_TYPE_TEXT,
  type DisplayMessage
} from '../types/chat'
import {
  buildSessionKey,
  createLocalMessageId,
  formatSize,
  getFileName
} from '../utils/chat'

interface FeedbackApi {
  success: (content: string) => void
  warning: (content: string) => void
  error: (content: string) => void
}

interface UseChatMessageActionsOptions {
  message: FeedbackApi
  messages: Ref<DisplayMessage[]>
  currentTargetId: Ref<string | null>
  currentSessionType: Ref<number>
  currentMuted: ComputedRef<boolean>
  inputMessage: Ref<string>
  textareaRef: Ref<HTMLTextAreaElement | undefined>
  closeMentionMenu: () => void
  getCurrentUserId: () => string | number
  getCurrentUserNickname: () => string
  getCurrentUserAvatar: () => string
  sendChatCommand: (action: string, data?: Record<string, unknown>) => Promise<any>
  toDisplayMessage: (item: any) => DisplayMessage
  upsertMessage: (message: DisplayMessage) => void
  scrollMessagesToBottom: (force?: boolean) => void
  ensureMessageFileAccessUrl: (rawUrl: string) => Promise<string>
  getResolvedMessageFileUrl: (messageItem: DisplayMessage) => string
}

export function useChatMessageActions(options: UseChatMessageActionsOptions) {
  const sending = ref(false)
  const showMsgContextMenu = ref(false)
  const showImagePreview = ref(false)
  const showDownloadModal = ref(false)
  const msgMenuX = ref(0)
  const msgMenuY = ref(0)
  const selectedMsg = ref<DisplayMessage | null>(null)
  const previewImageUrl = ref('')
  const downloadFileName = ref('')
  const downloadFileSize = ref('')
  const downloadProgress = ref(0)
  const downloadFileUrl = ref('')

  let sendLock = false

  function getCurrentSessionKey() {
    if (!options.currentTargetId.value) {
      return ''
    }
    return buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)
  }

  function getFileSizeText(messageItem: DisplayMessage) {
    return formatSize(messageItem.fileSize)
  }

  function createPendingMessage(payload: {
    content: string
    msgType: number
    fileName?: string
    fileSize?: number
    mentionAll?: boolean
    mentionUserIds?: string[]
    mentionDisplayNames?: string[]
    retryFile?: File
  }) {
    if (!options.currentTargetId.value) {
      throw new Error('当前会话不存在')
    }
    const now = new Date()
    const clientMessageId = createLocalMessageId('client')
    const localId = createLocalMessageId('pending')
    const pendingMessage: DisplayMessage = {
      id: localId,
      localId,
      clientMessageId,
      isMe: true,
      isSystem: false,
      name: options.getCurrentUserNickname() || '我',
      fromAvatar: options.getCurrentUserAvatar() || '',
      content: payload.content,
      msgType: payload.msgType,
      status: 0,
      readTime: '',
      createTime: now.toISOString(),
      time: now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      readStatus: '未读',
      deliveryStatus: 'sending',
      fileName: payload.fileName || '',
      fileSize: payload.fileSize,
      sessionType: options.currentSessionType.value,
      targetId: String(options.currentTargetId.value),
      mentionAll: Boolean(payload.mentionAll),
      mentionUserIds: payload.mentionUserIds || [],
      mentionDisplayNames: payload.mentionDisplayNames || [],
      mentionedMe: false,
      retryFile: payload.retryFile
    }
    options.upsertMessage(pendingMessage)
    return pendingMessage
  }

  function markMessageDelivery(localId: string, deliveryStatus: DisplayMessage['deliveryStatus']) {
    const messageIndex = options.messages.value.findIndex(messageItem => messageItem.localId === localId)
    if (messageIndex === -1) {
      return
    }
    options.messages.value.splice(messageIndex, 1, {
      ...options.messages.value[messageIndex],
      deliveryStatus
    })
  }

  function patchMessage(localId: string, patch: Partial<DisplayMessage>) {
    const messageIndex = options.messages.value.findIndex(messageItem => messageItem.localId === localId)
    if (messageIndex === -1) {
      return
    }
    options.messages.value.splice(messageIndex, 1, {
      ...options.messages.value[messageIndex],
      ...patch
    })
  }

  async function sendPendingTextMessage(localMessage: DisplayMessage, content: string) {
    const response: any = await options.sendChatCommand('SEND_MESSAGE', {
      toUserId: localMessage.targetId,
      content,
      sessionType: localMessage.sessionType,
      clientMessageId: localMessage.clientMessageId,
      mentionAll: localMessage.mentionAll,
      mentionUserIds: localMessage.mentionUserIds
    })
    if (response?.data) {
      options.upsertMessage(options.toDisplayMessage(response.data))
      return
    }
    markMessageDelivery(localMessage.localId, 'sent')
  }

  async function sendPendingFileMessage(localMessage: DisplayMessage, file: File, msgType: number) {
    let fileId = localMessage.uploadedFileId
    if (!fileId) {
      const uploadResponse: any = msgType === MESSAGE_TYPE_IMAGE
        ? await fileApi.uploadImage(file)
        : await fileApi.uploadFile(file)
      fileId = uploadResponse.data?.id
      patchMessage(localMessage.localId, {
        uploadedFileId: fileId,
        fileName: uploadResponse.data?.originalName || localMessage.fileName,
        fileSize: uploadResponse.data?.fileSize != null ? Number(uploadResponse.data.fileSize) : localMessage.fileSize
      })
    }
    const response: any = await options.sendChatCommand('SEND_FILE_MESSAGE', {
      toUserId: localMessage.targetId,
      fileId,
      msgType,
      sessionType: localMessage.sessionType,
      clientMessageId: localMessage.clientMessageId
    })
    if (response?.data) {
      options.upsertMessage(options.toDisplayMessage(response.data))
      return
    }
    markMessageDelivery(localMessage.localId, 'sent')
  }

  async function executePendingMessage(localMessage: DisplayMessage, executor: () => Promise<void>, failureMessage: string) {
    try {
      await executor()
      options.scrollMessagesToBottom(true)
    } catch (error: any) {
      console.error('executePendingMessage error:', error)
      markMessageDelivery(localMessage.localId, 'failed')
      options.message.error(error.response?.data?.message || failureMessage)
    }
  }

  async function handleSend(outgoingMentions?: {
    mentionAll: boolean
    mentionUserIds: string[]
    mentionDisplayNames: string[]
  }) {
    if (!options.currentTargetId.value || !options.inputMessage.value.trim()) {
      return
    }
    if (options.currentMuted.value) {
      options.message.warning('你已被禁言，暂时无法发送消息')
      return
    }
    if (sendLock) {
      return
    }

    sendLock = true
    sending.value = true
    try {
      const content = options.inputMessage.value.trim()
      const localMessage = createPendingMessage({
        content,
        msgType: MESSAGE_TYPE_TEXT,
        mentionAll: Boolean(outgoingMentions?.mentionAll),
        mentionUserIds: outgoingMentions?.mentionUserIds || [],
        mentionDisplayNames: outgoingMentions?.mentionDisplayNames || []
      })
      options.inputMessage.value = ''
      options.closeMentionMenu()
      if (options.textareaRef.value) {
        options.textareaRef.value.style.height = 'auto'
      }
      options.scrollMessagesToBottom(true)
      await executePendingMessage(localMessage, () => sendPendingTextMessage(localMessage, content), '发送失败')
    } finally {
      sending.value = false
      sendLock = false
    }
  }

  async function handleFileUpload(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file || !options.currentTargetId.value) {
      return
    }
    if (options.currentMuted.value) {
      options.message.warning('你已被禁言，暂时无法发送文件')
      input.value = ''
      return
    }

    try {
      const localMessage = createPendingMessage({
        content: file.name,
        msgType: MESSAGE_TYPE_FILE,
        fileName: file.name,
        fileSize: file.size,
        retryFile: file
      })
      options.scrollMessagesToBottom(true)
      await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_FILE), '发送文件失败')
    } finally {
      input.value = ''
    }
  }

  async function handleImageUpload(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file || !options.currentTargetId.value) {
      return
    }
    if (options.currentMuted.value) {
      options.message.warning('你已被禁言，暂时无法发送图片')
      input.value = ''
      return
    }

    try {
      const localPreviewUrl = URL.createObjectURL(file)
      const localMessage = createPendingMessage({
        content: localPreviewUrl,
        msgType: MESSAGE_TYPE_IMAGE,
        fileName: file.name,
        fileSize: file.size,
        retryFile: file
      })
      options.scrollMessagesToBottom(true)
      await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_IMAGE), '发送图片失败')
    } finally {
      input.value = ''
    }
  }

  async function retryFailedMessage(messageItem: DisplayMessage) {
    if (messageItem.deliveryStatus !== 'failed') {
      return
    }
    if (buildSessionKey(messageItem.targetId, messageItem.sessionType) !== getCurrentSessionKey()) {
      options.message.warning('请回到原会话后重试发送')
      return
    }
    markMessageDelivery(messageItem.localId, 'sending')
    if (messageItem.msgType === MESSAGE_TYPE_TEXT) {
      await executePendingMessage(messageItem, () => sendPendingTextMessage(messageItem, messageItem.content), '发送失败')
      return
    }
    if (messageItem.retryFile) {
      const failureMessage = messageItem.msgType === MESSAGE_TYPE_IMAGE ? '发送图片失败' : '发送文件失败'
      await executePendingMessage(messageItem, () => sendPendingFileMessage(messageItem, messageItem.retryFile!, messageItem.msgType), failureMessage)
    }
  }

  async function previewImage(messageItem: DisplayMessage) {
    const resolvedUrl = options.getResolvedMessageFileUrl(messageItem) || await options.ensureMessageFileAccessUrl(messageItem.content)
    if (!resolvedUrl) {
      options.message.error('图片访问链接已失效，请稍后重试')
      return
    }
    previewImageUrl.value = resolvedUrl
    showImagePreview.value = true
  }

  function closeImagePreview() {
    showImagePreview.value = false
    previewImageUrl.value = ''
  }

  async function downloadFile(messageItem: DisplayMessage) {
    const resolvedUrl = options.getResolvedMessageFileUrl(messageItem) || await options.ensureMessageFileAccessUrl(messageItem.content)
    const safeDownloadUrl = resolveSafeDownloadUrl(resolvedUrl)
    if (!safeDownloadUrl) {
      options.message.error('文件链接无效或协议不受支持')
      return
    }
    downloadFileName.value = messageItem.fileName || getFileName(messageItem.content)
    downloadFileSize.value = getFileSizeText(messageItem)
    downloadFileUrl.value = safeDownloadUrl
    downloadProgress.value = 100
    showDownloadModal.value = true
  }

  async function openDownloadedFile() {
    try {
      await openSafeExternalUrl(downloadFileUrl.value)
      showDownloadModal.value = false
    } catch (error: any) {
      options.message.error(error.message || '打开文件失败')
    }
  }

  function saveDownloadedFile() {
    try {
      triggerSafeDownload(downloadFileUrl.value, downloadFileName.value)
      showDownloadModal.value = false
    } catch (error: any) {
      options.message.error(error.message || '保存文件失败')
    }
  }

  function showMsgMenu(event: MouseEvent, messageItem: DisplayMessage) {
    if (messageItem.isSystem) {
      showMsgContextMenu.value = false
      return
    }
    selectedMsg.value = messageItem
    msgMenuX.value = event.clientX
    msgMenuY.value = event.clientY
    showMsgContextMenu.value = true
  }

  function canRecallMessage(messageItem: DisplayMessage | null) {
    if (!messageItem || messageItem.isSystem || !messageItem.isMe || messageItem.status === MESSAGE_STATUS_RECALLED || !messageItem.createTime || messageItem.deliveryStatus !== 'sent') {
      return false
    }
    const messageTime = getDateTimeTimestamp(messageItem.createTime)
    return Date.now() - messageTime < 2 * 60 * 1000
  }

  async function handleRecallMessage() {
    if (!selectedMsg.value || !options.currentTargetId.value) {
      return
    }
    try {
      const recalledMessage = selectedMsg.value
      await options.sendChatCommand('RECALL_MESSAGE', { messageId: recalledMessage.id })
      showMsgContextMenu.value = false
      selectedMsg.value = null
      options.upsertMessage({
        ...recalledMessage,
        status: MESSAGE_STATUS_RECALLED,
        readStatus: '已撤回'
      })
      options.message.success('消息已撤回')
    } catch (error: any) {
      console.error('handleRecallMessage error:', error)
      options.message.error(error.response?.data?.message || '撤回失败')
    }
  }

  async function handleCopyMessage() {
    if (!selectedMsg.value?.content) {
      return
    }
    const messageItem = selectedMsg.value
    try {
      if (messageItem.msgType === MESSAGE_TYPE_IMAGE || messageItem.msgType === MESSAGE_TYPE_FILE) {
        if (messageItem.content.startsWith('blob:')) {
          options.message.warning('文件上传完成后才能复制链接')
          return
        }
        const accessUrl = await resolveFileAccessUrl(messageItem.content, true)
        if (!accessUrl) {
          options.message.error('无法生成访问链接')
          return
        }
        await navigator.clipboard.writeText(accessUrl)
        options.message.success('访问链接已复制')
        return
      }
      await navigator.clipboard.writeText(messageItem.content)
      options.message.success('已复制')
    } catch (error) {
      options.message.error('复制失败')
    }
  }

  return {
    sending,
    showMsgContextMenu,
    showImagePreview,
    showDownloadModal,
    msgMenuX,
    msgMenuY,
    selectedMsg,
    previewImageUrl,
    downloadFileName,
    downloadFileSize,
    downloadProgress,
    handleSend,
    handleFileUpload,
    handleImageUpload,
    retryFailedMessage,
    previewImage,
    closeImagePreview,
    downloadFile,
    openDownloadedFile,
    saveDownloadedFile,
    showMsgMenu,
    canRecallMessage,
    handleRecallMessage,
    handleCopyMessage
  }
}
