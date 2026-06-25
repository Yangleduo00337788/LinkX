/**
 * useFriendsUsers 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { ref, type Ref } from 'vue'  // 行注：引入 ref, type Ref 能力
import type { Router } from 'vue-router'  // 行注：引入 type { Router } 模块
import { blacklistApi, friendApi, userApi } from '../api/client'  // 行注：引入 blacklistApi, friendApi, userApi 能力

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  info: (content: string) => void  // 行注：设置 info 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface FriendRequestItem {  // 行注：开始当前逻辑块
  id: number  // 行注：设置 id 配置项
  fromAvatar?: string  // 行注：补充当前表达式
  fromNickname?: string  // 行注：补充当前表达式
  message?: string  // 行注：补充当前表达式
  createTime?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface FriendListItem {  // 行注：开始当前逻辑块
  friendId: string | number  // 行注：设置 friendId 配置项
  friendAvatar?: string  // 行注：补充当前表达式
  friendNickname?: string  // 行注：补充当前表达式
  friendUsername?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface SearchUserItem {  // 行注：开始当前逻辑块
  id: string | number  // 行注：设置 id 配置项
  avatar?: string  // 行注：补充当前表达式
  nickname?: string  // 行注：补充当前表达式
  username?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface UseFriendsUsersOptions {  // 行注：开始当前逻辑块
  activeTab: Ref<string>  // 行注：设置 activeTab 配置项
  router: Router  // 行注：设置 router 配置项
  message: FeedbackApi  // 行注：设置 message 配置项
  getCurrentNickname: () => string  // 行注：传入 getCurrentNickname 回调
}  // 行注：结束当前代码块

export function useFriendsUsers(options: UseFriendsUsersOptions) {  // 行注：导出当前能力
  const searchKeyword = ref('')  // 行注：初始化 searchKeyword 响应式状态
  const friends = ref<FriendListItem[]>([])  // 行注：初始化 friends 集合
  const requests = ref<FriendRequestItem[]>([])  // 行注：初始化 requests 集合
  const searchResults = ref<SearchUserItem[]>([])  // 行注：初始化 searchResults 集合
  const showUserInfo = ref(false)  // 行注：初始化 showUserInfo 响应式状态
  const selectedFriend = ref<FriendListItem | null>(null)  // 行注：初始化 selectedFriend 变量

  async function loadFriends() {  // 行注：定义异步 loadFriends 方法
    const response: any = await friendApi.getList()  // 行注：接收 response 异步结果
    friends.value = response.data || []  // 行注：更新 friends 状态
    if (!selectedFriend.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const latestFriend = friends.value.find(friend => String(friend.friendId) === String(selectedFriend.value?.friendId)) || null  // 行注：初始化 latestFriend 变量
    if (!latestFriend) {  // 行注：判断当前条件是否成立
      closeUserInfo()  // 行注：调用 closeUserInfo 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    selectedFriend.value = latestFriend  // 行注：更新 selectedFriend 状态
  }  // 行注：结束当前代码块

  async function loadRequests() {  // 行注：定义异步 loadRequests 方法
    const response: any = await friendApi.getRequests()  // 行注：接收 response 异步结果
    requests.value = response.data || []  // 行注：更新 requests 状态
  }  // 行注：结束当前代码块

  async function handleSearch() {  // 行注：定义异步 handleSearch 方法
    const keyword = searchKeyword.value.trim()  // 行注：初始化 keyword 状态
    if (!keyword) {  // 行注：判断当前条件是否成立
      options.activeTab.value = 'search'  // 行注：更新 options.activeTab 状态
      searchResults.value = []  // 行注：更新 searchResults 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    options.activeTab.value = 'search'  // 行注：更新 options.activeTab 状态
    try {  // 行注：尝试执行可能失败的逻辑
      searchKeyword.value = keyword  // 行注：更新 searchKeyword 状态
      const response: any = await userApi.search(keyword)  // 行注：接收 response 异步结果
      searchResults.value = response.data || []  // 行注：更新 searchResults 状态
      if (searchResults.value.length === 0) {  // 行注：判断当前条件是否成立
        options.message.info('未找到相关用户')  // 行注：调用 info 方法
      }  // 行注：结束当前代码块
    } catch {  // 行注：捕获并处理异常
      options.message.error('搜索失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleAddFriend(userId: string | number) {  // 行注：定义异步 handleAddFriend 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await friendApi.sendRequest(userId, `我是${options.getCurrentNickname()}`)  // 行注：调用 sendRequest 方法
      options.message.success('已发送好友申请')  // 行注：提示成功信息
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.response?.data?.message || '发送失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleAccept(requestId: number) {  // 行注：定义异步 handleAccept 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await friendApi.accept(requestId)  // 行注：调用 accept 方法
      options.message.success('已接受好友申请')  // 行注：提示成功信息
      await Promise.all([loadRequests(), loadFriends()])  // 行注：并行执行多项异步任务
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleAccept error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '操作失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function handleReject(requestId: number) {  // 行注：定义异步 handleReject 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await friendApi.reject(requestId)  // 行注：调用 reject 方法
      options.message.info('已拒绝')  // 行注：调用 info 方法
      await loadRequests()  // 行注：调用 loadRequests 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('handleReject error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '操作失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function startChat(targetId: string | number) {  // 行注：定义 startChat 方法
    options.router.push(`/chat/${targetId}`)  // 行注：跳转到目标路由
  }  // 行注：结束当前代码块

  async function handleBlacklist(targetUserId: string | number) {  // 行注：定义异步 handleBlacklist 方法
    try {  // 行注：尝试执行可能失败的逻辑
      await blacklistApi.add(targetUserId)  // 行注：调用 add 方法
      options.message.success('已拉黑')  // 行注：提示成功信息
      closeUserInfo()  // 行注：调用 closeUserInfo 方法
      await loadFriends()  // 行注：调用 loadFriends 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      options.message.error(error.response?.data?.message || '操作失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function showFriendInfo(friend: FriendListItem) {  // 行注：定义 showFriendInfo 方法
    selectedFriend.value = friend  // 行注：更新 selectedFriend 状态
    showUserInfo.value = true  // 行注：更新 showUserInfo 状态
  }  // 行注：结束当前代码块

  function closeUserInfo() {  // 行注：定义 closeUserInfo 方法
    showUserInfo.value = false  // 行注：更新 showUserInfo 状态
    selectedFriend.value = null  // 行注：更新 selectedFriend 状态
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    searchKeyword,  // 行注：补充 searchKeyword 配置项
    friends,  // 行注：补充当前配置项
    requests,  // 行注：补充当前配置项
    searchResults,  // 行注：补充当前配置项
    showUserInfo,  // 行注：补充当前配置项
    selectedFriend,  // 行注：补充当前配置项
    loadFriends,  // 行注：补充当前配置项
    loadRequests,  // 行注：补充当前配置项
    handleSearch,  // 行注：补充当前配置项
    handleAddFriend,  // 行注：补充当前配置项
    handleAccept,  // 行注：补充当前配置项
    handleReject,  // 行注：补充当前配置项
    startChat,  // 行注：补充当前配置项
    handleBlacklist,  // 行注：补充当前配置项
    showFriendInfo,  // 行注：补充当前配置项
    closeUserInfo  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
