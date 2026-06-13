<template>
  <div class="content-area" style="flex-direction: column;">
    <div class="panel-header">联系人</div>
    <div class="friends-search">
      <input v-model="searchKeyword" placeholder="搜索用户" @keyup.enter="handleSearch" />
    </div>
    <div style="flex: 1; overflow-y: auto; background: white;">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="好友列表" name="list">
          <div v-for="f in friends" :key="f.friendId" class="session-item">
            <div class="session-avatar">{{ f.friendNickname?.charAt(0) }}</div>
            <div class="session-info">
              <div class="session-name">{{ f.friendNickname }}</div>
              <div class="session-preview">@{{ f.friendUsername }}</div>
            </div>
            <button style="background:#07c160;color:white;border:none;padding:4px 12px;border-radius:4px;cursor:pointer;font-size:12px;"
                    @click="startChat(f.friendId)">聊天</button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="好友申请" name="requests">
          <div v-for="r in requests" :key="r.id" class="session-item">
            <div class="session-avatar" style="background:#faad14;">{{ r.fromNickname?.charAt(0) }}</div>
            <div class="session-info">
              <div class="session-name">{{ r.fromNickname }} <span style="font-size:12px;color:#999;">请求加你为好友</span></div>
              <div class="session-preview">{{ r.message || '无验证消息' }}</div>
            </div>
            <div style="display:flex;gap:6px;">
              <button style="background:#07c160;color:white;border:none;padding:4px 12px;border-radius:4px;cursor:pointer;font-size:12px;"
                      @click="handleAccept(r.id)">同意</button>
              <button style="background:#eee;color:#666;border:none;padding:4px 12px;border-radius:4px;cursor:pointer;font-size:12px;"
                      @click="handleReject(r.id)">拒绝</button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="搜索结果" name="search">
          <div v-for="u in searchResults" :key="u.id" class="session-item">
            <div class="session-avatar">{{ u.nickname?.charAt(0) }}</div>
            <div class="session-info">
              <div class="session-name">{{ u.nickname }}</div>
              <div class="session-preview">@{{ u.username }}</div>
            </div>
            <button style="background:#07c160;color:white;border:none;padding:4px 12px;border-radius:4px;cursor:pointer;font-size:12px;"
                    @click="handleAddFriend(u.id)">添加</button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { friendApi, userApi } from '../api/client'

const router = useRouter()
const activeTab = ref('list')
const searchKeyword = ref('')
const friends = ref<any[]>([])
const requests = ref<any[]>([])
const searchResults = ref<any[]>([])

onMounted(async () => {
  await loadFriends()
  await loadRequests()
})

async function loadFriends() {
  try { const res: any = await friendApi.getList(); friends.value = res.data || [] } catch (e) {}
}

async function loadRequests() {
  try { const res: any = await friendApi.getRequests(); requests.value = res.data || [] } catch (e) {}
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) return
  try { const res: any = await userApi.search(searchKeyword.value); searchResults.value = res.data || [] } catch (e) {}
}

async function handleAddFriend(userId: number) {
  try {
    await friendApi.sendRequest(userId, '我是' + localStorage.getItem('nickname'))
    alert('已发送好友申请')
  } catch (e: any) {
    alert(e.response?.data?.message || '发送失败')
  }
}

async function handleAccept(requestId: number) {
  try {
    await friendApi.accept(requestId)
    await loadRequests()
    await loadFriends()
  } catch (e) {}
}

async function handleReject(requestId: number) {
  try { await friendApi.reject(requestId); await loadRequests() } catch (e) {}
}

function startChat(targetId: number) {
  router.push(`/chat/${targetId}`)
}
</script>
