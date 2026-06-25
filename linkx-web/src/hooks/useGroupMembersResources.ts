/**
 * useGroupMembersResources 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { ref, type ComputedRef } from 'vue'  // 行注：引入 ref, type ComputedRef 能力
import { groupApi } from '../api/client'  // 行注：引入 groupApi 能力
import type { GroupMediaItem } from '../types/chat'  // 行注：引入 type { GroupMediaItem } 模块
import { formatSize } from '../utils/chat'  // 行注：引入 formatSize 能力
import { hydrateFileAccessUrls, resolveFileAccessUrl } from '../utils/file-access'  // 行注：引入 hydrateFileAccessUrls, resolveFileAccessUrl 能力
import { openSafeExternalUrl } from '../utils/url'  // 行注：引入 openSafeExternalUrl 能力

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface UseGroupMembersResourcesOptions {  // 行注：开始当前逻辑块
  groupId: ComputedRef<string>  // 行注：设置 groupId 配置项
  message: FeedbackApi  // 行注：设置 message 配置项
  isGroupUnavailableError: (error: any) => boolean  // 行注：设置 isGroupUnavailableError 配置项
  handleUnavailableGroup: (messageText: string) => Promise<void>  // 行注：设置 handleUnavailableGroup 配置项
}  // 行注：结束当前代码块

export function useGroupMembersResources(options: UseGroupMembersResourcesOptions) {  // 行注：导出当前能力
  const mediaType = ref<'all' | 'image' | 'file'>('all')  // 行注：初始化 mediaType 变量
  const mediaKeyword = ref('')  // 行注：初始化 mediaKeyword 响应式状态
  const groupMediaItems = ref<GroupMediaItem[]>([])  // 行注：初始化 groupMediaItems 变量
  const groupMediaLoading = ref(false)  // 行注：初始化 groupMediaLoading 响应式状态
  const messageSearchKeyword = ref('')  // 行注：初始化 messageSearchKeyword 响应式状态
  const groupMessageSearchResults = ref<GroupMediaItem[]>([])  // 行注：初始化 groupMessageSearchResults 集合
  const groupMessageSearchLoading = ref(false)  // 行注：初始化 groupMessageSearchLoading 响应式状态

  function resetResourceState() {  // 行注：定义 resetResourceState 方法
    mediaType.value = 'all'  // 行注：更新 mediaType 状态
    mediaKeyword.value = ''  // 行注：更新 mediaKeyword 状态
    groupMediaItems.value = []  // 行注：更新 groupMediaItems 状态
    messageSearchKeyword.value = ''  // 行注：更新 messageSearchKeyword 状态
    groupMessageSearchResults.value = []  // 行注：更新 groupMessageSearchResults 状态
  }  // 行注：结束当前代码块

  function formatFileSize(fileSize?: number) {  // 行注：定义 formatFileSize 方法
    return formatSize(fileSize) || '0 B'  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function isImageMedia(item: GroupMediaItem) {  // 行注：定义 isImageMedia 方法
    return Number(item.msgType) === 1  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function getMediaTypeText(item: GroupMediaItem) {  // 行注：定义 getMediaTypeText 方法
    if (Number(item.msgType) === 1) {  // 行注：判断当前条件是否成立
      return '图片'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (Number(item.msgType) === 2) {  // 行注：判断当前条件是否成立
      return '文件'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (Number(item.msgType) === 3) {  // 行注：判断当前条件是否成立
      return '系统'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return '消息'  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function getMessageSearchPreview(item: GroupMediaItem) {  // 行注：定义 getMessageSearchPreview 方法
    if (Number(item.msgType) === 1) {  // 行注：判断当前条件是否成立
      return item.fileName ? `[图片] ${item.fileName}` : '[图片]'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (Number(item.msgType) === 2) {  // 行注：判断当前条件是否成立
      return item.fileName ? `[文件] ${item.fileName}` : '[文件]'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (Number(item.msgType) === 3) {  // 行注：判断当前条件是否成立
      return `[系统] ${item.content}`  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return item.content  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function resolveMediaAccessUrl(item?: GroupMediaItem | null) {  // 行注：定义异步 resolveMediaAccessUrl 方法
    if (!item?.content) {  // 行注：判断当前条件是否成立
      return ''  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (item.accessUrl) {  // 行注：判断当前条件是否成立
      return item.accessUrl  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return resolveFileAccessUrl(item.content)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function copyMediaLink(item?: GroupMediaItem | null) {  // 行注：定义异步 copyMediaLink 方法
    const accessUrl = await resolveMediaAccessUrl(item)  // 行注：接收 accessUrl 异步结果
    if (!accessUrl) {  // 行注：判断当前条件是否成立
      options.message.error('无法生成访问链接')  // 行注：提示错误信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await navigator.clipboard.writeText(accessUrl)  // 行注：写入剪贴板
      options.message.success('链接已复制')  // 行注：提示成功信息
    } catch {  // 行注：捕获并处理异常
      options.message.error('复制链接失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function openMediaResource(item?: GroupMediaItem | null) {  // 行注：定义异步 openMediaResource 方法
    const accessUrl = await resolveMediaAccessUrl(item)  // 行注：接收 accessUrl 异步结果
    if (!accessUrl) {  // 行注：判断当前条件是否成立
      options.message.error('资源访问链接不可用')  // 行注：提示错误信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      await openSafeExternalUrl(accessUrl)  // 行注：调用 openSafeExternalUrl 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.message || '打开资源失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function loadGroupMedia() {  // 行注：定义异步 loadGroupMedia 方法
    if (!options.groupId.value) {  // 行注：判断当前条件是否成立
      groupMediaItems.value = []  // 行注：更新 groupMediaItems 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    groupMediaLoading.value = true  // 行注：更新 groupMediaLoading 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.getMedia(options.groupId.value, {  // 行注：开始解构当前返回值
        mediaType: mediaType.value,  // 行注：设置 mediaType 配置项
        keyword: mediaKeyword.value.trim() || undefined,  // 行注：设置 keyword 配置项
        size: 200  // 行注：设置 size 配置项
      })  // 行注：结束当前调用配置
      groupMediaItems.value = await hydrateFileAccessUrls(response.data || [])  // 行注：更新 groupMediaItems 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('loadGroupMedia error:', error)  // 行注：输出错误日志
      if (options.isGroupUnavailableError(error)) {  // 行注：判断当前条件是否成立
        groupMediaItems.value = []  // 行注：更新 groupMediaItems 状态
        await options.handleUnavailableGroup('当前群聊已不可访问')  // 行注：调用 handleUnavailableGroup 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.message.error(error.response?.data?.message || '加载群相册/文件库失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      groupMediaLoading.value = false  // 行注：更新 groupMediaLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function searchGroupMessages() {  // 行注：定义异步 searchGroupMessages 方法
    const keyword = messageSearchKeyword.value.trim()  // 行注：初始化 keyword 状态
    if (!options.groupId.value || !keyword) {  // 行注：判断当前条件是否成立
      groupMessageSearchResults.value = []  // 行注：更新 groupMessageSearchResults 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    groupMessageSearchLoading.value = true  // 行注：更新 groupMessageSearchLoading 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.searchMessages(options.groupId.value, keyword, 100)  // 行注：接收 response 异步结果
      groupMessageSearchResults.value = response.data || []  // 行注：更新 groupMessageSearchResults 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('searchGroupMessages error:', error)  // 行注：输出错误日志
      if (options.isGroupUnavailableError(error)) {  // 行注：判断当前条件是否成立
        groupMessageSearchResults.value = []  // 行注：更新 groupMessageSearchResults 状态
        await options.handleUnavailableGroup('当前群聊已不可访问')  // 行注：调用 handleUnavailableGroup 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.message.error(error.response?.data?.message || '搜索群消息失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      groupMessageSearchLoading.value = false  // 行注：更新 groupMessageSearchLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    mediaType,  // 行注：补充 mediaType 配置项
    mediaKeyword,  // 行注：补充当前配置项
    groupMediaItems,  // 行注：补充当前配置项
    groupMediaLoading,  // 行注：补充当前配置项
    messageSearchKeyword,  // 行注：补充当前配置项
    groupMessageSearchResults,  // 行注：补充当前配置项
    groupMessageSearchLoading,  // 行注：补充当前配置项
    resetResourceState,  // 行注：补充当前配置项
    formatFileSize,  // 行注：补充当前配置项
    isImageMedia,  // 行注：补充当前配置项
    getMediaTypeText,  // 行注：补充当前配置项
    getMessageSearchPreview,  // 行注：补充当前配置项
    copyMediaLink,  // 行注：补充当前配置项
    openMediaResource,  // 行注：补充当前配置项
    loadGroupMedia,  // 行注：补充当前配置项
    searchGroupMessages  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
