import { ref, type Ref } from 'vue'
import type { Router } from 'vue-router'
import { blacklistApi, friendApi, userApi } from '../api/client'

interface FeedbackApi {
  success: (content: string) => void
  info: (content: string) => void
  error: (content: string) => void
}

interface FriendRequestItem {
  id: number
  fromAvatar?: string
  fromNickname?: string
  message?: string
  createTime?: string
}

interface FriendListItem {
  friendId: string | number
  friendAvatar?: string
  friendNickname?: string
  friendUsername?: string
}

interface SearchUserItem {
  id: string | number
  avatar?: string
  nickname?: string
  username?: string
}

interface UseFriendsUsersOptions {
  activeTab: Ref<string>
  router: Router
  message: FeedbackApi
  getCurrentNickname: () => string
}

export function useFriendsUsers(options: UseFriendsUsersOptions) {
  const searchKeyword = ref('')
  const friends = ref<FriendListItem[]>([])
  const requests = ref<FriendRequestItem[]>([])
  const searchResults = ref<SearchUserItem[]>([])
  const showUserInfo = ref(false)
  const selectedFriend = ref<FriendListItem | null>(null)

  async function loadFriends() {
    const response: any = await friendApi.getList()
    friends.value = response.data || []
    if (!selectedFriend.value) {
      return
    }
    const latestFriend = friends.value.find(friend => String(friend.friendId) === String(selectedFriend.value?.friendId)) || null
    if (!latestFriend) {
      closeUserInfo()
      return
    }
    selectedFriend.value = latestFriend
  }

  async function loadRequests() {
    const response: any = await friendApi.getRequests()
    requests.value = response.data || []
  }

  async function handleSearch() {
    const keyword = searchKeyword.value.trim()
    if (!keyword) {
      options.activeTab.value = 'search'
      searchResults.value = []
      return
    }
    options.activeTab.value = 'search'
    try {
      searchKeyword.value = keyword
      const response: any = await userApi.search(keyword)
      searchResults.value = response.data || []
      if (searchResults.value.length === 0) {
        options.message.info('未找到相关用户')
      }
    } catch {
      options.message.error('搜索失败')
    }
  }

  async function handleAddFriend(userId: string | number) {
    try {
      await friendApi.sendRequest(userId, `我是${options.getCurrentNickname()}`)
      options.message.success('已发送好友申请')
    } catch (error: any) {
      options.message.error(error.response?.data?.message || '发送失败')
    }
  }

  async function handleAccept(requestId: number) {
    try {
      await friendApi.accept(requestId)
      options.message.success('已接受好友申请')
      await Promise.all([loadRequests(), loadFriends()])
    } catch (error: any) {
      console.error('handleAccept error:', error)
      options.message.error(error.response?.data?.message || '操作失败')
    }
  }

  async function handleReject(requestId: number) {
    try {
      await friendApi.reject(requestId)
      options.message.info('已拒绝')
      await loadRequests()
    } catch (error: any) {
      console.error('handleReject error:', error)
      options.message.error(error.response?.data?.message || '操作失败')
    }
  }

  function startChat(targetId: string | number) {
    options.router.push(`/chat/${targetId}`)
  }

  async function handleBlacklist(targetUserId: string | number) {
    try {
      await blacklistApi.add(targetUserId)
      options.message.success('已拉黑')
      closeUserInfo()
      await loadFriends()
    } catch (error: any) {
      options.message.error(error.response?.data?.message || '操作失败')
    }
  }

  function showFriendInfo(friend: FriendListItem) {
    selectedFriend.value = friend
    showUserInfo.value = true
  }

  function closeUserInfo() {
    showUserInfo.value = false
    selectedFriend.value = null
  }

  return {
    searchKeyword,
    friends,
    requests,
    searchResults,
    showUserInfo,
    selectedFriend,
    loadFriends,
    loadRequests,
    handleSearch,
    handleAddFriend,
    handleAccept,
    handleReject,
    startChat,
    handleBlacklist,
    showFriendInfo,
    closeUserInfo
  }
}
