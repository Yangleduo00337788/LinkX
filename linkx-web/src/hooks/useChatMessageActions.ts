/**
 * 处理消息发送、撤回、复制、下载和预览等操作。
 */
import { ref, type ComputedRef, type Ref } from 'vue'  // 行注：引入 ref, type ComputedRef, type Ref 能力
import { fileApi, groupApi, reportApi } from '../api/client'
import { isSocketTransportError, sendFileViaRest, sendTextViaRest } from '../utils/chatSendFallback'
import { resolveFileAccessUrl } from '../utils/file-access'  // 行注：引入 resolveFileAccessUrl 能力
import { getDateTimeTimestamp } from '../utils/datetime'  // 行注：引入 getDateTimeTimestamp 能力
import { openSafeExternalUrl, resolveSafeDownloadUrl, triggerSafeDownload } from '../utils/url'  // 行注：引入 openSafeExternalUrl, resolveSafeDownloadUrl, triggerSafeDownload 能力
import {  // 行注：引入 { 模块
  MESSAGE_STATUS_RECALLED,  // 行注：补充 MESSAGE_STATUS_RECALLED 配置项
  MESSAGE_TYPE_FILE,  // 行注：补充当前配置项
  MESSAGE_TYPE_IMAGE,  // 行注：补充当前配置项
  MESSAGE_TYPE_TEXT,  // 行注：补充当前配置项
  type DisplayMessage  // 行注：补充当前表达式
} from '../types/chat'  // 行注：补充当前表达式
import {  // 行注：引入 { 模块
  buildSessionKey,  // 行注：补充 buildSessionKey 配置项
  createLocalMessageId,  // 行注：补充当前配置项
  formatSize,  // 行注：补充当前配置项
  getFileName  // 行注：补充当前表达式
} from '../utils/chat'  // 行注：补充当前表达式

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  warning: (content: string) => void  // 行注：设置 warning 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface UseChatMessageActionsOptions {  // 行注：开始当前逻辑块
  message: FeedbackApi  // 行注：设置 message 配置项
  messages: Ref<DisplayMessage[]>  // 行注：设置 messages 配置项
  currentTargetId: Ref<string | null>  // 行注：设置 currentTargetId 配置项
  currentSessionType: Ref<number>  // 行注：设置 currentSessionType 配置项
  currentMuted: ComputedRef<boolean>  // 行注：设置 currentMuted 配置项
  inputMessage: Ref<string>  // 行注：设置 inputMessage 配置项
  textareaRef: Ref<HTMLTextAreaElement | undefined>  // 行注：设置 textareaRef 配置项
  closeMentionMenu: () => void  // 行注：传入 closeMentionMenu 回调
  getCurrentUserId: () => string | number  // 行注：传入 getCurrentUserId 回调
  getCurrentUserNickname: () => string  // 行注：传入 getCurrentUserNickname 回调
  getCurrentUserAvatar: () => string  // 行注：传入 getCurrentUserAvatar 回调
  sendChatCommand: (action: string, data?: Record<string, unknown>) => Promise<any>  // 行注：设置 sendChatCommand 配置项
  toDisplayMessage: (item: any) => DisplayMessage  // 行注：设置 toDisplayMessage 配置项
  upsertMessage: (message: DisplayMessage) => void  // 行注：设置 upsertMessage 配置项
  scrollMessagesToBottom: (force?: boolean) => void  // 行注：设置 scrollMessagesToBottom 配置项
  ensureMessageFileAccessUrl: (rawUrl: string) => Promise<string>  // 行注：设置 ensureMessageFileAccessUrl 配置项
  getResolvedMessageFileUrl: (messageItem: DisplayMessage) => string  // 行注：设置 getResolvedMessageFileUrl 配置项
  onAfterSendSuccess?: () => void | Promise<void>
}  // 行注：结束当前代码块

export function useChatMessageActions(options: UseChatMessageActionsOptions) {  // 行注：导出当前能力
  const sending = ref(false)  // 行注：初始化 sending 响应式状态
  const showMsgContextMenu = ref(false)  // 行注：初始化 showMsgContextMenu 响应式状态
  const showImagePreview = ref(false)  // 行注：初始化 showImagePreview 响应式状态
  const showDownloadModal = ref(false)  // 行注：初始化 showDownloadModal 响应式状态
  const msgMenuX = ref(0)  // 行注：初始化 msgMenuX 响应式状态
  const msgMenuY = ref(0)  // 行注：初始化 msgMenuY 响应式状态
  const selectedMsg = ref<DisplayMessage | null>(null)  // 行注：初始化 selectedMsg 变量
  const previewImageUrl = ref('')  // 行注：初始化 previewImageUrl 响应式状态
  const downloadFileName = ref('')  // 行注：初始化 downloadFileName 响应式状态
  const downloadFileSize = ref('')  // 行注：初始化 downloadFileSize 响应式状态
  const downloadProgress = ref(0)  // 行注：初始化 downloadProgress 响应式状态
  const downloadFileUrl = ref('')  // 行注：初始化 downloadFileUrl 响应式状态

  let sendLock = false  // 行注：初始化 sendLock 变量

  function getCurrentSessionKey() {  // 行注：定义 getCurrentSessionKey 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return ''  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function getFileSizeText(messageItem: DisplayMessage) {  // 行注：定义 getFileSizeText 方法
    return formatSize(messageItem.fileSize)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function createPendingMessage(payload: {  // 行注：定义 createPendingMessage 方法
    content: string  // 行注：设置 content 配置项
    msgType: number  // 行注：设置 msgType 配置项
    fileName?: string  // 行注：补充当前表达式
    fileSize?: number  // 行注：补充当前表达式
    mentionAll?: boolean  // 行注：补充当前表达式
    mentionUserIds?: string[]  // 行注：补充当前表达式
    mentionDisplayNames?: string[]  // 行注：补充当前表达式
    retryFile?: File  // 行注：补充当前表达式
  }) {  // 行注：开始当前逻辑块
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      throw new Error('当前会话不存在')  // 行注：抛出异常并终止当前流程
    }  // 行注：结束当前代码块
    const now = new Date()  // 行注：初始化 now 变量
    const clientMessageId = createLocalMessageId('client')  // 行注：初始化 clientMessageId 状态
    const localId = createLocalMessageId('pending')  // 行注：初始化 localId 状态
    const pendingMessage: DisplayMessage = {  // 行注：开始解构当前返回值
      id: localId,  // 行注：设置 id 配置项
      localId,  // 行注：补充当前配置项
      clientMessageId,  // 行注：补充当前配置项
      isMe: true,  // 行注：设置 isMe 配置项
      isSystem: false,  // 行注：设置 isSystem 配置项
      name: options.getCurrentUserNickname() || '我',  // 行注：设置 name 配置项
      fromAvatar: options.getCurrentUserAvatar() || '',  // 行注：设置 fromAvatar 配置项
      content: payload.content,  // 行注：设置 content 配置项
      msgType: payload.msgType,  // 行注：设置 msgType 配置项
      status: 0,  // 行注：设置 status 配置项
      readTime: '',  // 行注：设置 readTime 配置项
      createTime: now.toISOString(),  // 行注：设置 createTime 配置项
      time: now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),  // 行注：设置 time 配置项
      readStatus: '未读',  // 行注：设置 readStatus 配置项
      deliveryStatus: 'sending',  // 行注：设置 deliveryStatus 配置项
      fileName: payload.fileName || '',  // 行注：设置 fileName 配置项
      fileSize: payload.fileSize,  // 行注：设置 fileSize 配置项
      sessionType: options.currentSessionType.value,  // 行注：设置 sessionType 配置项
      targetId: String(options.currentTargetId.value),  // 行注：设置 targetId 配置项
      mentionAll: Boolean(payload.mentionAll),  // 行注：设置 mentionAll 配置项
      mentionUserIds: payload.mentionUserIds || [],  // 行注：设置 mentionUserIds 配置项
      mentionDisplayNames: payload.mentionDisplayNames || [],  // 行注：设置 mentionDisplayNames 配置项
      mentionedMe: false,  // 行注：设置 mentionedMe 配置项
      retryFile: payload.retryFile  // 行注：设置 retryFile 配置项
    }  // 行注：结束当前代码块
    options.upsertMessage(pendingMessage)  // 行注：调用 upsertMessage 方法
    return pendingMessage  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function markMessageDelivery(localId: string, deliveryStatus: DisplayMessage['deliveryStatus']) {  // 行注：定义 markMessageDelivery 方法
    const messageIndex = options.messages.value.findIndex(messageItem => messageItem.localId === localId)  // 行注：初始化 messageIndex 变量
    if (messageIndex === -1) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    options.messages.value.splice(messageIndex, 1, {  // 行注：开始当前逻辑块
      ...options.messages.value[messageIndex],  // 行注：补充当前配置项
      deliveryStatus  // 行注：补充当前表达式
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function patchMessage(localId: string, patch: Partial<DisplayMessage>) {  // 行注：定义 patchMessage 方法
    const messageIndex = options.messages.value.findIndex(messageItem => messageItem.localId === localId)  // 行注：初始化 messageIndex 变量
    if (messageIndex === -1) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    options.messages.value.splice(messageIndex, 1, {  // 行注：开始当前逻辑块
      ...options.messages.value[messageIndex],  // 行注：补充当前配置项
      ...patch  // 行注：补充当前表达式
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  async function sendPendingTextMessage(localMessage: DisplayMessage, content: string) {
    let response: any
    try {
      response = await options.sendChatCommand('SEND_MESSAGE', {
        toUserId: localMessage.targetId,
        content,
        sessionType: localMessage.sessionType,
        clientMessageId: localMessage.clientMessageId,
        mentionAll: localMessage.mentionAll,
        mentionUserIds: localMessage.mentionUserIds
      })
    } catch (error) {
      if (!isSocketTransportError(error)) {
        throw error
      }
      const data = await sendTextViaRest({
        toUserId: localMessage.targetId!,
        content,
        sessionType: localMessage.sessionType ?? 1,
        clientMessageId: localMessage.clientMessageId,
        mentionAll: localMessage.mentionAll,
        mentionUserIds: localMessage.mentionUserIds
      })
      response = { data }
    }
    if (response?.data) {
      options.upsertMessage(options.toDisplayMessage(response.data))
      return
    }
    markMessageDelivery(localMessage.localId, 'sent')
  }

  async function sendPendingFileMessage(localMessage: DisplayMessage, file: File, msgType: number) {  // 行注：定义异步 sendPendingFileMessage 方法
    let fileId = localMessage.uploadedFileId  // 行注：初始化 fileId 状态
    if (!fileId) {  // 行注：判断当前条件是否成立
      const uploadResponse: any = msgType === MESSAGE_TYPE_IMAGE  // 行注：初始化 uploadResponse 变量
        ? await fileApi.uploadImage(file)  // 行注：调用 uploadImage 方法
        : await fileApi.uploadFile(file)  // 行注：调用 uploadFile 方法
      fileId = uploadResponse.data?.id  // 行注：更新 fileId 值
      patchMessage(localMessage.localId, {  // 行注：开始当前逻辑块
        uploadedFileId: fileId,  // 行注：设置 uploadedFileId 配置项
        fileName: uploadResponse.data?.originalName || localMessage.fileName,  // 行注：设置 fileName 配置项
        fileSize: uploadResponse.data?.fileSize != null ? Number(uploadResponse.data.fileSize) : localMessage.fileSize  // 行注：设置 fileSize 配置项
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
    let response: any
    try {
      response = await options.sendChatCommand('SEND_FILE_MESSAGE', {
        toUserId: localMessage.targetId,
        fileId,
        msgType,
        sessionType: localMessage.sessionType,
        clientMessageId: localMessage.clientMessageId
      })
    } catch (error) {
      if (!isSocketTransportError(error) || !fileId) {
        throw error
      }
      const data = await sendFileViaRest({
        toUserId: localMessage.targetId!,
        fileId: Number(fileId),
        msgType,
        sessionType: localMessage.sessionType ?? 1,
        clientMessageId: localMessage.clientMessageId
      })
      response = { data }
    }
    if (response?.data) {
      options.upsertMessage(options.toDisplayMessage(response.data))
      return
    }
    markMessageDelivery(localMessage.localId, 'sent')
  }

  async function executePendingMessage(localMessage: DisplayMessage, executor: () => Promise<void>, failureMessage: string) {  // 行注：定义异步 executePendingMessage 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await executor()  // 行注：调用 executor 方法
      options.scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('executePendingMessage error:', error)  // 行注：输出错误日志
      markMessageDelivery(localMessage.localId, 'failed')  // 行注：调用 markMessageDelivery 方法
      options.message.error(error.response?.data?.message || failureMessage)  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleSend(outgoingMentions?: {  // 行注：定义异步 handleSend 方法
    mentionAll: boolean  // 行注：设置 mentionAll 配置项
    mentionUserIds: string[]  // 行注：设置 mentionUserIds 配置项
    mentionDisplayNames: string[]  // 行注：设置 mentionDisplayNames 配置项
  }) {  // 行注：开始当前逻辑块
    if (!options.currentTargetId.value || !options.inputMessage.value.trim()) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.currentMuted.value) {  // 行注：判断当前条件是否成立
      options.message.warning('你已被禁言，暂时无法发送消息')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (sendLock) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    sendLock = true  // 行注：更新 sendLock 值
    sending.value = true  // 行注：更新 sending 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const content = options.inputMessage.value.trim()  // 行注：初始化 content 变量
      const localMessage = createPendingMessage({  // 行注：开始解构当前返回值
        content,  // 行注：传入 content 参数
        msgType: MESSAGE_TYPE_TEXT,  // 行注：设置 msgType 配置项
        mentionAll: Boolean(outgoingMentions?.mentionAll),  // 行注：设置 mentionAll 配置项
        mentionUserIds: outgoingMentions?.mentionUserIds || [],  // 行注：设置 mentionUserIds 配置项
        mentionDisplayNames: outgoingMentions?.mentionDisplayNames || []  // 行注：设置 mentionDisplayNames 配置项
      })  // 行注：结束当前调用配置
      options.inputMessage.value = ''  // 行注：更新 options.inputMessage 状态
      options.closeMentionMenu()  // 行注：调用 closeMentionMenu 方法
      if (options.textareaRef.value) {  // 行注：判断当前条件是否成立
        options.textareaRef.value.style.height = 'auto'  // 行注：更新 options.textareaRef.value.style.height 值
      }  // 行注：结束当前代码块
      options.scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
      await executePendingMessage(localMessage, () => sendPendingTextMessage(localMessage, content), '发送失败')  // 行注：调用 executePendingMessage 方法
      await options.onAfterSendSuccess?.()
    } finally {  // 行注：执行收尾清理逻辑
      sending.value = false  // 行注：更新 sending 状态
      sendLock = false  // 行注：更新 sendLock 值
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleFileUpload(event: Event) {  // 行注：定义异步 handleFileUpload 方法
    const input = event.target as HTMLInputElement  // 行注：初始化 input 变量
    const file = input.files?.[0]  // 行注：初始化 file 变量
    if (!file || !options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.currentMuted.value) {  // 行注：判断当前条件是否成立
      options.message.warning('你已被禁言，暂时无法发送文件')  // 行注：提示警告信息
      input.value = ''  // 行注：更新 input 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    try {  // 行注：尝试执行可能失败的逻辑
      const localMessage = createPendingMessage({  // 行注：开始解构当前返回值
        content: file.name,  // 行注：设置 content 配置项
        msgType: MESSAGE_TYPE_FILE,  // 行注：设置 msgType 配置项
        fileName: file.name,  // 行注：设置 fileName 配置项
        fileSize: file.size,  // 行注：设置 fileSize 配置项
        retryFile: file  // 行注：设置 retryFile 配置项
      })  // 行注：结束当前调用配置
      options.scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
      await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_FILE), '发送文件失败')  // 行注：调用 executePendingMessage 方法
    } finally {  // 行注：执行收尾清理逻辑
      input.value = ''  // 行注：更新 input 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function sendFileFromComposer(file: File) {
    if (!file || !options.currentTargetId.value) {
      return
    }
    if (options.currentMuted.value) {
      options.message.warning('你已被禁言，暂时无法发送文件')
      return
    }
    const isImage = file.type.startsWith('image/')
    try {
      if (isImage) {
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
      } else {
        const localMessage = createPendingMessage({
          content: file.name,
          msgType: MESSAGE_TYPE_FILE,
          fileName: file.name,
          fileSize: file.size,
          retryFile: file
        })
        options.scrollMessagesToBottom(true)
        await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_FILE), '发送文件失败')
      }
    } catch (error: any) {
      console.error('sendFileFromComposer error:', error)
      options.message.error(error?.message || '发送失败')
    }
  }

  async function handleDroppedFiles(files: File[]) {
    if (!files.length) {
      return
    }
    for (const file of files) {
      await sendFileFromComposer(file)
    }
  }

  async function handleImageUpload(event: Event) {  // 行注：定义异步 handleImageUpload 方法
    const input = event.target as HTMLInputElement  // 行注：初始化 input 变量
    const file = input.files?.[0]  // 行注：初始化 file 变量
    if (!file || !options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.currentMuted.value) {  // 行注：判断当前条件是否成立
      options.message.warning('你已被禁言，暂时无法发送图片')  // 行注：提示警告信息
      input.value = ''  // 行注：更新 input 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    try {  // 行注：尝试执行可能失败的逻辑
      const localPreviewUrl = URL.createObjectURL(file)  // 行注：初始化 localPreviewUrl 变量
      const localMessage = createPendingMessage({  // 行注：开始解构当前返回值
        content: localPreviewUrl,  // 行注：设置 content 配置项
        msgType: MESSAGE_TYPE_IMAGE,  // 行注：设置 msgType 配置项
        fileName: file.name,  // 行注：设置 fileName 配置项
        fileSize: file.size,  // 行注：设置 fileSize 配置项
        retryFile: file  // 行注：设置 retryFile 配置项
      })  // 行注：结束当前调用配置
      options.scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
      await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_IMAGE), '发送图片失败')  // 行注：调用 executePendingMessage 方法
    } finally {  // 行注：执行收尾清理逻辑
      input.value = ''  // 行注：更新 input 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function retryFailedMessage(messageItem: DisplayMessage) {  // 行注：定义异步 retryFailedMessage 方法
    if (messageItem.deliveryStatus !== 'failed') {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (buildSessionKey(messageItem.targetId, messageItem.sessionType) !== getCurrentSessionKey()) {  // 行注：判断当前条件是否成立
      options.message.warning('请回到原会话后重试发送')  // 行注：提示警告信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    markMessageDelivery(messageItem.localId, 'sending')  // 行注：调用 markMessageDelivery 方法
    if (messageItem.msgType === MESSAGE_TYPE_TEXT) {  // 行注：判断当前条件是否成立
      await executePendingMessage(messageItem, () => sendPendingTextMessage(messageItem, messageItem.content), '发送失败')  // 行注：调用 executePendingMessage 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (messageItem.retryFile) {  // 行注：判断当前条件是否成立
      const failureMessage = messageItem.msgType === MESSAGE_TYPE_IMAGE ? '发送图片失败' : '发送文件失败'  // 行注：初始化 failureMessage 实例
      await executePendingMessage(messageItem, () => sendPendingFileMessage(messageItem, messageItem.retryFile!, messageItem.msgType), failureMessage)  // 行注：调用 executePendingMessage 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function previewImage(messageItem: DisplayMessage) {  // 行注：定义异步 previewImage 方法
    const resolvedUrl = options.getResolvedMessageFileUrl(messageItem) || await options.ensureMessageFileAccessUrl(messageItem.content)  // 行注：初始化 resolvedUrl 方法
    if (!resolvedUrl) {  // 行注：判断当前条件是否成立
      options.message.error('图片访问链接已失效，请稍后重试')  // 行注：提示错误信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    previewImageUrl.value = resolvedUrl  // 行注：更新 previewImageUrl 状态
    showImagePreview.value = true  // 行注：更新 showImagePreview 状态
  }  // 行注：结束当前代码块

  function closeImagePreview() {  // 行注：定义 closeImagePreview 方法
    showImagePreview.value = false  // 行注：更新 showImagePreview 状态
    previewImageUrl.value = ''  // 行注：更新 previewImageUrl 状态
  }  // 行注：结束当前代码块

  async function downloadFile(messageItem: DisplayMessage) {  // 行注：定义异步 downloadFile 方法
    const resolvedUrl = options.getResolvedMessageFileUrl(messageItem) || await options.ensureMessageFileAccessUrl(messageItem.content)  // 行注：初始化 resolvedUrl 方法
    const safeDownloadUrl = resolveSafeDownloadUrl(resolvedUrl)  // 行注：初始化 safeDownloadUrl 变量
    if (!safeDownloadUrl) {  // 行注：判断当前条件是否成立
      options.message.error('文件链接无效或协议不受支持')  // 行注：提示错误信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    downloadFileName.value = messageItem.fileName || getFileName(messageItem.content)  // 行注：更新 downloadFileName 状态
    downloadFileSize.value = getFileSizeText(messageItem)  // 行注：更新 downloadFileSize 状态
    downloadFileUrl.value = safeDownloadUrl  // 行注：更新 downloadFileUrl 状态
    downloadProgress.value = 100  // 行注：更新 downloadProgress 状态
    showDownloadModal.value = true  // 行注：更新 showDownloadModal 状态
  }  // 行注：结束当前代码块

  async function openDownloadedFile() {  // 行注：定义异步 openDownloadedFile 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await openSafeExternalUrl(downloadFileUrl.value)  // 行注：调用 openSafeExternalUrl 方法
      showDownloadModal.value = false  // 行注：更新 showDownloadModal 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.message || '打开文件失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function saveDownloadedFile() {  // 行注：定义 saveDownloadedFile 方法
    try {  // 行注：尝试执行可能失败的逻辑
      triggerSafeDownload(downloadFileUrl.value, downloadFileName.value)  // 行注：调用 triggerSafeDownload 方法
      showDownloadModal.value = false  // 行注：更新 showDownloadModal 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.message || '保存文件失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function showMsgMenu(event: MouseEvent, messageItem: DisplayMessage) {
    if (messageItem.isSystem) {
      showMsgContextMenu.value = false
      return
    }
    event.preventDefault()
    selectedMsg.value = messageItem
    const menuW = 168
    const menuH = 132
    const pad = 8
    const maxX = Math.max(pad, window.innerWidth - menuW - pad)
    const maxY = Math.max(pad, window.innerHeight - menuH - pad)
    msgMenuX.value = Math.min(Math.max(event.clientX, pad), maxX)
    msgMenuY.value = Math.min(Math.max(event.clientY, pad), maxY)
    showMsgContextMenu.value = true
  }

  function canRecallMessage(messageItem: DisplayMessage | null) {  // 行注：定义 canRecallMessage 方法
    if (!messageItem || messageItem.isSystem || !messageItem.isMe || messageItem.status === MESSAGE_STATUS_RECALLED || !messageItem.createTime || messageItem.deliveryStatus !== 'sent') {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const messageTime = getDateTimeTimestamp(messageItem.createTime)  // 行注：初始化 messageTime 变量
    return Date.now() - messageTime < 2 * 60 * 1000  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function handleRecallMessage() {  // 行注：定义异步 handleRecallMessage 方法
    if (!selectedMsg.value || !options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      const recalledMessage = selectedMsg.value  // 行注：初始化 recalledMessage 实例
      await options.sendChatCommand('RECALL_MESSAGE', { messageId: recalledMessage.id })  // 行注：调用 sendChatCommand 方法
      showMsgContextMenu.value = false  // 行注：更新 showMsgContextMenu 状态
      selectedMsg.value = null  // 行注：更新 selectedMsg 状态
      options.upsertMessage({  // 行注：开始当前逻辑块
        ...recalledMessage,  // 行注：补充当前配置项
        status: MESSAGE_STATUS_RECALLED,  // 行注：设置 status 配置项
        readStatus: '已撤回'  // 行注：设置 readStatus 配置项
      })  // 行注：结束当前调用配置
      options.message.success('消息已撤回')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleRecallMessage error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '撤回失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleReportMessage(reasonDetail = '') {
    if (!selectedMsg.value) {
      return
    }
    const messageItem = selectedMsg.value
    try {
      await reportApi.submit({
        targetType: 'message',
        targetId: String(messageItem.id),
        reasonCategory: 'inappropriate',
        reasonDetail: reasonDetail.trim() || undefined
      })
      showMsgContextMenu.value = false
      selectedMsg.value = null
      options.message.success('举报已提交，我们会尽快处理')
    } catch (error: any) {
      options.message.error(error?.message || error.response?.data?.message || '举报提交失败')
      throw error
    }
  }

  async function handleCopyMessage() {  // 行注：定义异步 handleCopyMessage 方法
    if (!selectedMsg.value?.content) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const messageItem = selectedMsg.value  // 行注：初始化 messageItem 变量
    try {  // 行注：尝试执行可能失败的逻辑
      if (messageItem.msgType === MESSAGE_TYPE_IMAGE || messageItem.msgType === MESSAGE_TYPE_FILE) {  // 行注：判断当前条件是否成立
        if (messageItem.content.startsWith('blob:')) {  // 行注：判断当前条件是否成立
          options.message.warning('文件上传完成后才能复制链接')  // 行注：提示警告信息
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        const accessUrl = await resolveFileAccessUrl(messageItem.content, true)  // 行注：接收 accessUrl 异步结果
        if (!accessUrl) {  // 行注：判断当前条件是否成立
          options.message.error('无法生成访问链接')  // 行注：提示错误信息
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        await navigator.clipboard.writeText(accessUrl)  // 行注：写入剪贴板
        options.message.success('访问链接已复制')  // 行注：提示成功信息
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      await navigator.clipboard.writeText(messageItem.content)  // 行注：写入剪贴板
      options.message.success('已复制')  // 行注：提示成功信息
    } catch (error) {  // 行注：捕获并处理异常
      options.message.error('复制失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    sending,  // 行注：补充 sending 配置项
    showMsgContextMenu,  // 行注：补充当前配置项
    showImagePreview,  // 行注：补充当前配置项
    showDownloadModal,  // 行注：补充当前配置项
    msgMenuX,  // 行注：补充当前配置项
    msgMenuY,  // 行注：补充当前配置项
    selectedMsg,  // 行注：补充当前配置项
    previewImageUrl,  // 行注：补充当前配置项
    downloadFileName,  // 行注：补充当前配置项
    downloadFileSize,  // 行注：补充当前配置项
    downloadProgress,  // 行注：补充当前配置项
    handleSend,  // 行注：补充当前配置项
    handleFileUpload,
    handleImageUpload,
    handleDroppedFiles,
    sendFileFromComposer,
    retryFailedMessage,  // 行注：补充当前配置项
    previewImage,  // 行注：补充当前配置项
    closeImagePreview,  // 行注：补充当前配置项
    downloadFile,  // 行注：补充当前配置项
    openDownloadedFile,  // 行注：补充当前配置项
    saveDownloadedFile,  // 行注：补充当前配置项
    showMsgMenu,  // 行注：补充当前配置项
    canRecallMessage,  // 行注：补充当前配置项
    handleRecallMessage,  // 行注：补充当前配置项
    handleCopyMessage,
    handleReportMessage
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
