<template>
  <div class="content-area">
    <!-- Friends List Panel -->
    <div class="friends-panel">
      <div class="panel-header">
        <span class="header-title">联系人</span>
        <button class="refresh-btn" @click="loadAll" title="刷新">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
        </button>
      </div>

      <div class="friends-search">
        <div class="search-input-wrapper">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>

      <div class="friends-list">
        <div
          v-for="f in friends"
          :key="f.friendId"
          class="friend-item"
          @click="startChat(f.friendId)"
        >
          <div class="friend-avatar" @click.stop="showFriendInfo(f)">
            <img v-if="f.friendAvatar" :src="f.friendAvatar" class="avatar-img" />
            <span v-else>{{ f.friendNickname?.charAt(0) }}</span>
          </div>
          <div class="friend-info">
            <div class="friend-name">{{ f.friendNickname }}</div>
            <div class="friend-username">@{{ f.friendUsername }}</div>
          </div>
          <div class="friend-actions">
            <button class="action-btn" @click.stop="startChat(f.friendId)" title="发消息">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </button>
          </div>
        </div>

        <div v-if="friends.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="empty-title">暂无好友</div>
          <div class="empty-subtitle">去添加一些好友吧</div>
        </div>
      </div>
    </div>

    <!-- Right Panel -->
    <div class="right-panel">
      <div class="tabs-header">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'requests' }"
          @click="activeTab = 'requests'"
        >
          好友申请
          <span v-if="requests.length" class="tab-badge">{{ requests.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'group-requests' }"
          @click="activeTab = 'group-requests'"
        >
          群通知
          <span v-if="groupRequests.length" class="tab-badge">{{ groupRequests.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'join-group' }"
          @click="activeTab = 'join-group'"
        >
          加入群聊
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'my-groups' }"
          @click="activeTab = 'my-groups'"
        >
          我的群聊
          <span v-if="myGroups.length" class="tab-badge">{{ myGroups.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'search' }"
          @click="activeTab = 'search'"
        >
          搜索用户
        </button>
      </div>

      <div class="tab-content">
        <!-- Requests Tab -->
        <div v-if="activeTab === 'requests'" class="tab-pane">
          <FriendsRequestsPanel
            :requests="requests"
            @accept="handleAccept"
            @reject="handleReject"
          />
        </div>

        <div v-if="activeTab === 'group-requests'" class="tab-pane">
          <FriendsGroupRequestsPanel
            :group-requests="groupRequests"
            :group-request-type-text="groupRequestTypeText"
            :group-request-tag-class="groupRequestTagClass"
            :build-group-request-message="buildGroupRequestMessage"
            :format-request-time="formatRequestTime"
            :is-request-action-loading="isRequestActionLoading"
            @accept="handleAcceptGroupRequest"
            @reject="handleRejectGroupRequest"
          />
        </div>

        <div v-if="activeTab === 'join-group'" class="tab-pane">
          <div class="join-group-search-section">
            <div class="join-group-search-wrapper">
              <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input
                v-model="groupSearchKeyword"
                type="text"
                class="join-group-search-input"
                placeholder="输入群名称或群 ID 搜索..."
                maxlength="100"
                @keyup.enter="handleGroupSearch"
              />
              <button v-if="groupSearchKeyword" class="join-group-search-clear" @click="clearGroupSearch">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
              <button class="join-group-search-btn" :disabled="searchingGroup" @click="handleGroupSearch">
                {{ searchingGroup ? '搜索中...' : '搜索' }}
              </button>
            </div>
          </div>

          <div v-if="groupSearchResults.length > 0" class="group-search-results">
            <div class="group-search-results-header">
              <span>搜索结果</span>
              <span class="group-search-results-count">{{ groupSearchResults.length }} 个群</span>
            </div>
            <div
              v-for="g in groupSearchResults"
              :key="g.id"
              class="group-search-result-card"
            >
              <div class="group-search-result-avatar">
                <img v-if="g.groupAvatar" :src="g.groupAvatar" class="avatar-img" />
                <span v-else>{{ g.groupName?.charAt(0) || '群' }}</span>
              </div>
              <div class="group-search-result-info">
                <div class="group-search-result-name">{{ g.groupName }}</div>
                <div class="group-search-result-meta">
                  <span class="group-search-result-id">群号：{{ g.id }}</span>
                  <span class="group-search-result-sep">·</span>
                  <span>{{ g.memberCount || 0 }} 人</span>
                  <template v-if="g.myRole !== null && g.myRole !== undefined">
                    <span class="group-search-result-sep">·</span>
                    <span class="group-search-result-self">已加入</span>
                  </template>
                </div>
                <div v-if="g.notice" class="group-search-result-notice">{{ g.notice }}</div>
              </div>
              <div class="group-search-result-actions">
                <button
                  class="group-preview-btn"
                  @click="openGroupPreview(g)"
                >
                  查看资料
                </button>
                <button
                  v-if="isJoinedGroup(g.id, g.myRole)"
                  class="group-search-enter-btn"
                  @click="startGroupChat(g.id)"
                >
                  进入群聊
                </button>
                <button
                  v-else-if="hasPendingJoinRequest(g.id)"
                  class="group-search-pending-btn"
                  disabled
                >
                  申请已提交
                </button>
                <button
                  v-else
                  class="group-search-join-btn"
                  @click="openJoinGroupModal(g)"
                >
                  申请加入
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="groupSearchKeyword && !searchingGroup" class="empty-state">
            <div class="empty-icon">
              <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
            </div>
            <div class="empty-title">未找到相关群聊</div>
            <div class="empty-subtitle">请尝试其他关键词</div>
          </div>

          <div v-else-if="!groupSearchKeyword" class="join-group-shell">
            <div class="join-group-card">
              <div class="join-group-head">
                <div>
                  <div class="join-group-title">按群 ID 申请加入</div>
                  <div class="join-group-subtitle">输入群 ID 并附带申请说明，等待群主或管理员处理</div>
                </div>
                <div class="join-group-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M12 5v14"/>
                    <path d="M5 12h14"/>
                    <rect x="3" y="3" width="18" height="18" rx="4"/>
                  </svg>
                </div>
              </div>

              <div class="join-group-form">
                <label class="join-group-label" for="join-group-id">群 ID</label>
                <input
                  id="join-group-id"
                  v-model="joinGroupId"
                  type="text"
                  inputmode="numeric"
                  class="join-group-input"
                  placeholder="请输入群 ID"
                  maxlength="30"
                  @keyup.enter="handleJoinGroup"
                />

                <label class="join-group-label" for="join-group-message">申请说明</label>
                <textarea
                  id="join-group-message"
                  v-model="joinGroupMessage"
                  class="join-group-textarea"
                  rows="4"
                  maxlength="255"
                  placeholder="例如：我是产品组同事，申请加入项目沟通群"
                ></textarea>

                <div class="join-group-foot">
                  <div class="join-group-hint">提交后会进入群通知，等待对方确认。</div>
                  <button class="join-group-btn" :disabled="joiningGroup" @click="handleJoinGroup">
                    {{ joiningGroup ? '提交中...' : '发送申请' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'my-groups'" class="tab-pane">
          <div v-if="myGroups.length > 0" class="group-grid">
            <div
              v-for="group in myGroups"
              :key="group.id"
              class="group-card"
              @click="startGroupChat(group.id)"
            >
              <div class="group-card-top">
                <div class="group-card-avatar">
                  <img v-if="group.groupAvatar" :src="group.groupAvatar" class="avatar-img" />
                  <span v-else>{{ group.groupName?.charAt(0) || '群' }}</span>
                </div>
                <div class="group-card-meta">
                  <div class="group-card-name">{{ group.groupName }}</div>
                  <div class="group-card-id-row">
                    <span class="group-card-id">群号：{{ group.id }}</span>
                    <button class="group-copy-btn" @click.stop="copyGroupId(group.id)">复制群号</button>
                  </div>
                  <div class="group-card-subtitle">
                    {{ group.memberCount || 0 }} 人
                    <template v-if="group.notice"> · {{ group.notice }}</template>
                  </div>
                </div>
                <span class="group-role-badge" :class="groupRoleClass(group.myRole)">
                  {{ groupRoleText(group.myRole) }}
                </span>
              </div>

              <div class="group-card-bottom">
                <div class="group-card-time">
                  {{ group.createTime ? `创建于 ${group.createTime.substring(0, 10)}` : '群聊已创建' }}
                </div>
                <div class="group-card-actions">
                  <button class="group-preview-btn" @click.stop="openGroupPreview(group)">查看资料</button>
                  <button class="group-enter-btn" @click.stop="startGroupChat(group.id)">进入群聊</button>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <div class="empty-icon">
              <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="4"/>
                <path d="M12 8v8"/>
                <path d="M8 12h8"/>
              </svg>
            </div>
            <div class="empty-title">暂无我的群聊</div>
            <div class="empty-subtitle">创建群聊或通过邀请、申请加入群聊后会显示在这里</div>
          </div>
        </div>

        <!-- Search Tab -->
        <div v-if="activeTab === 'search'" class="tab-pane">
          <FriendsSearchPanel
            :search-keyword="searchKeyword"
            :search-results="searchResults"
            @add-friend="handleAddFriend"
          />
        </div>
      </div>
    </div>
  </div>

  <FriendsGroupPreviewDialog
    :visible="showGroupPreview"
    :preview-group-detail="previewGroupDetail"
    :has-pending-join-request="hasPendingJoinRequest"
    :is-joined-group="isJoinedGroup"
    :group-role-text="groupRoleText"
    :group-role-class="groupRoleClass"
    @close="closeGroupPreview"
    @copy-group-id="copyGroupId"
    @enter-group-chat="startGroupChat"
    @apply-join-from-preview="applyJoinFromPreview"
  />

  <FriendsUserDialog
    :visible="showUserInfo"
    :selected-friend="selectedFriend"
    @close="closeUserInfo"
    @send-message="friendId => { closeUserInfo(); startChat(friendId) }"
    @blacklist="handleBlacklist"
  />
</template>

<script setup lang="ts">
import FriendsGroupPreviewDialog from '../components/friends/FriendsGroupPreviewDialog.vue'
import FriendsGroupRequestsPanel from '../components/friends/FriendsGroupRequestsPanel.vue'
import FriendsRequestsPanel from '../components/friends/FriendsRequestsPanel.vue'
import FriendsSearchPanel from '../components/friends/FriendsSearchPanel.vue'
import FriendsUserDialog from '../components/friends/FriendsUserDialog.vue'
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useFriendsGroups } from '../hooks/useFriendsGroups'
import { useFriendsUsers } from '../hooks/useFriendsUsers'
import { useMessage } from 'naive-ui'
import { useGroupRequests } from '../hooks/useGroupRequests'
import { useUserStore } from '../stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const activeTab = ref('requests')
const refreshingPanels = ref(false)

const PANEL_REFRESH_INTERVAL = 10000
let lastPanelRefreshAt = 0

const {
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
} = useFriendsUsers({
  activeTab,
  router,
  message,
  getCurrentNickname: () => userStore.nickname?.trim() || '新朋友'
})

const {
  joinGroupId,
  joinGroupMessage,
  joiningGroup,
  groupSearchKeyword,
  searchingGroup,
  groupSearchResults,
  myGroups,
  showGroupPreview,
  previewGroupDetail,
  hasPendingJoinRequest,
  isJoinedGroup,
  loadMyGroups,
  handleJoinGroup,
  handleGroupSearch,
  clearGroupSearch,
  openJoinGroupModal,
  applyJoinFromPreview,
  groupRoleText,
  groupRoleClass,
  startGroupChat,
  copyGroupId,
  openGroupPreview,
  refreshOpenGroupPreview,
  closeGroupPreview
} = useFriendsGroups({
  activeTab,
  router,
  message
})

const {
  groupRequests,
  loadGroupRequests,
  groupRequestTypeText,
  groupRequestTagClass,
  buildGroupRequestMessage,
  formatRequestTime,
  isRequestActionLoading,
  handleAcceptGroupRequest,
  handleRejectGroupRequest
} = useGroupRequests({
  message,
  afterAccept: () => refreshPanelsAfterGroupRequestAction(),
  afterReject: () => refreshPanelsAfterGroupRequestAction()
})

async function refreshPanelsAfterGroupRequestAction() {
  await Promise.all([loadFriends(), loadRequests(), loadMyGroups()])
  await refreshOpenGroupPreview()
  lastPanelRefreshAt = Date.now()
}

async function refreshPanels(force = false) {
  const now = Date.now()
  if (refreshingPanels.value) {
    return
  }
  if (!force && now - lastPanelRefreshAt < PANEL_REFRESH_INTERVAL) {
    return
  }
  refreshingPanels.value = true
  try {
    await Promise.all([loadFriends(), loadRequests(), loadGroupRequests(), loadMyGroups()])
    await refreshOpenGroupPreview()
    lastPanelRefreshAt = Date.now()
  } finally {
    refreshingPanels.value = false
  }
}

function queueRefreshPanels(force = false) {
  void refreshPanels(force).catch((error: any) => {
    console.error('refreshPanels error:', error)
  })
}

async function loadAll() {
  try {
    await refreshPanels(true)
    message.success('已刷新')
  } catch (e) {
    message.error('刷新失败')
  }
}

function handleEscape(e: KeyboardEvent) {
  if (e.key !== 'Escape') {
    return
  }
  if (showGroupPreview.value) {
    closeGroupPreview()
    return
  }
  closeUserInfo()
}

function handleWindowFocus() {
  queueRefreshPanels(false)
}

function handleVisibilityChange() {
  if (document.visibilityState === 'visible') {
    queueRefreshPanels(false)
  }
}

onMounted(async () => {
  try {
    await refreshPanels(true)
  } catch (error: any) {
    console.error('initFriendsPage error:', error)
    message.error(error.response?.data?.message || '初始化联系人页失败')
  }
  document.addEventListener('keydown', handleEscape)
  window.addEventListener('focus', handleWindowFocus)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
  window.removeEventListener('focus', handleWindowFocus)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.content-area {
  display: flex;
  height: 100%;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

/* Friends Panel */
.friends-panel {
  width: 320px;
  background: var(--linkx-bg-card);
  border-right: 1px solid var(--linkx-border);
  display: flex;
  flex-direction: column;
  min-width: 280px;
}

.panel-header {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--linkx-border);
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--linkx-text);
  letter-spacing: 0.3px;
}

.refresh-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.refresh-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-primary);
}

.friends-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--linkx-border);
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--linkx-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 12px 0 36px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 13px;
  transition: var(--linkx-transition);
  outline: none;
}

.search-input::placeholder {
  color: var(--linkx-text-muted);
}

.search-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.friends-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  cursor: pointer;
  border-radius: var(--linkx-radius);
  transition: var(--linkx-transition-fast);
}

.friend-item:hover {
  background: var(--linkx-bg-hover);
}

.friend-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px var(--linkx-primary-glow);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
  margin-bottom: 2px;
}

.friend-username {
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.friend-actions {
  opacity: 0;
  transition: var(--linkx-transition-fast);
}

.friend-item:hover .friend-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.action-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-primary);
}

/* Right Panel */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

.tabs-header {
  display: flex;
  padding: 16px 20px 0;
  gap: 8px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius) var(--linkx-radius) 0 0;
  color: var(--linkx-text-secondary);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
  position: relative;
}

.tab-btn:hover {
  color: var(--linkx-text);
}

.tab-btn.active {
  color: var(--linkx-primary);
  background: var(--linkx-bg-card);
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--linkx-primary);
  border-radius: 2px 2px 0 0;
}

.tab-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--linkx-error);
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: var(--linkx-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-content {
  flex: 1;
  overflow: hidden;
  padding: 16px 20px;
}

.tab-pane {
  height: 100%;
  overflow-y: auto;
}

/* Group Search */
.join-group-search-section {
  margin-bottom: 16px;
}

.join-group-search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.join-group-search-wrapper .search-icon {
  position: absolute;
  left: 14px;
  color: var(--linkx-text-muted);
  pointer-events: none;
  z-index: 1;
}

.join-group-search-input {
  flex: 1;
  height: 42px;
  padding: 0 100px 0 38px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 14px;
  outline: none;
  transition: var(--linkx-transition);
}

.join-group-search-input::placeholder {
  color: var(--linkx-text-muted);
}

.join-group-search-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.join-group-search-clear {
  position: absolute;
  right: 96px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 50%;
  color: var(--linkx-text-muted);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.join-group-search-clear:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.join-group-search-btn {
  min-width: 80px;
  height: 42px;
  padding: 0 16px;
  background: var(--linkx-primary);
  border: none;
  border-radius: var(--linkx-radius);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
  flex-shrink: 0;
}

.join-group-search-btn:hover:not(:disabled) {
  background: var(--linkx-primary-hover);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
}

.join-group-search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.group-search-results {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.group-search-results-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
  margin-bottom: 4px;
}

.group-search-results-count {
  font-size: 12px;
  font-weight: 500;
  color: var(--linkx-text-muted);
}

.group-search-result-card {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  gap: 14px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  transition: var(--linkx-transition);
}

.group-search-result-card:hover {
  border-color: var(--linkx-primary);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
}

.group-search-result-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.22);
  overflow: hidden;
}

.group-search-result-info {
  flex: 1;
  min-width: 0;
}

.group-search-result-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
  margin-bottom: 4px;
}

.group-search-result-meta {
  font-size: 12px;
  color: var(--linkx-text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.group-search-result-id {
  color: var(--linkx-text-muted);
}

.group-search-result-sep {
  color: var(--linkx-text-muted);
}

.group-search-result-self {
  color: var(--linkx-primary);
  font-weight: 600;
}

.group-search-result-notice {
  font-size: 12px;
  color: var(--linkx-text-muted);
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-search-result-actions {
  flex-shrink: 0;
}

.group-search-join-btn {
  min-width: 88px;
  height: 34px;
  padding: 0 14px;
  background: var(--linkx-primary);
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: white;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.group-search-join-btn:hover {
  background: var(--linkx-primary-hover);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
  transform: translateY(-1px);
}

.group-search-pending-btn {
  min-width: 88px;
  height: 34px;
  padding: 0 14px;
  background: rgba(0, 214, 143, 0.12);
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-primary);
  font-size: 12px;
  font-weight: 700;
  cursor: not-allowed;
  opacity: 0.72;
}

.group-search-enter-btn {
  min-width: 88px;
  height: 34px;
  padding: 0 14px;
  background: rgba(0, 166, 255, 0.1);
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: #007aff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.group-search-enter-btn:hover {
  background: rgba(0, 166, 255, 0.16);
  color: #005fe0;
}

.join-group-shell {
  min-height: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 8px;
}

.join-group-card {
  width: min(680px, 100%);
  background:
    linear-gradient(180deg, rgba(0, 214, 143, 0.08) 0%, rgba(0, 214, 143, 0) 140px),
    var(--linkx-bg-card);
  border: 1px solid rgba(0, 214, 143, 0.12);
  border-radius: calc(var(--linkx-radius-lg) + 2px);
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.join-group-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 24px 18px;
}

.join-group-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 6px;
}

.join-group-subtitle {
  font-size: 13px;
  line-height: 1.6;
  color: var(--linkx-text-secondary);
}

.join-group-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-primary);
  background: rgba(0, 214, 143, 0.12);
  border: 1px solid rgba(0, 214, 143, 0.16);
  flex-shrink: 0;
}

.join-group-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 24px 24px;
}

.join-group-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--linkx-text-secondary);
  text-transform: uppercase;
}

.join-group-input,
.join-group-textarea {
  width: 100%;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 14px;
  transition: var(--linkx-transition);
  outline: none;
}

.join-group-input {
  height: 42px;
  padding: 0 14px;
}

.join-group-textarea {
  min-height: 112px;
  padding: 12px 14px;
  resize: vertical;
}

.join-group-input::placeholder,
.join-group-textarea::placeholder {
  color: var(--linkx-text-muted);
}

.join-group-input:focus,
.join-group-textarea:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.join-group-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 4px;
}

.join-group-hint {
  font-size: 12px;
  line-height: 1.6;
  color: var(--linkx-text-muted);
}

.join-group-btn {
  min-width: 120px;
  height: 40px;
  padding: 0 18px;
  background: var(--linkx-primary);
  border: none;
  border-radius: var(--linkx-radius);
  color: white;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--linkx-transition);
  flex-shrink: 0;
}

.join-group-btn:hover:not(:disabled) {
  background: var(--linkx-primary-hover);
  box-shadow: 0 8px 18px var(--linkx-primary-glow);
  transform: translateY(-1px);
}

.join-group-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.group-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}

.group-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
  background:
    linear-gradient(180deg, rgba(0, 166, 255, 0.08) 0%, rgba(0, 166, 255, 0) 120px),
    var(--linkx-bg-card);
  border: 1px solid rgba(0, 166, 255, 0.14);
  border-radius: calc(var(--linkx-radius) + 2px);
  transition: var(--linkx-transition);
  cursor: pointer;
}

.group-card:hover {
  transform: translateY(-2px);
  border-color: rgba(0, 166, 255, 0.28);
  box-shadow: 0 12px 28px rgba(0, 102, 255, 0.14);
}

.group-card-top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.group-card-avatar {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 6px 16px rgba(0, 102, 255, 0.22);
}

.group-card-meta {
  flex: 1;
  min-width: 0;
}

.group-card-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 6px;
}

.group-card-id-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.group-card-id {
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.group-card-subtitle {
  font-size: 12px;
  line-height: 1.6;
  color: var(--linkx-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.group-role-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 10px;
  border-radius: var(--linkx-radius-full);
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid transparent;
}

.group-role-badge.owner {
  color: #ffb020;
  background: rgba(255, 176, 32, 0.12);
  border-color: rgba(255, 176, 32, 0.22);
}

.group-role-badge.admin {
  color: #7a5cff;
  background: rgba(122, 92, 255, 0.12);
  border-color: rgba(122, 92, 255, 0.22);
}

.group-role-badge.member {
  color: var(--linkx-primary);
  background: rgba(0, 214, 143, 0.12);
  border-color: rgba(0, 214, 143, 0.22);
}

.group-card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.group-card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-card-time {
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.group-preview-btn,
.group-enter-btn {
  min-width: 88px;
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: rgba(0, 214, 143, 0.12);
  color: var(--linkx-primary);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.group-preview-btn {
  background: rgba(0, 166, 255, 0.1);
  color: #007aff;
}

.group-copy-btn {
  height: 26px;
  padding: 0 10px;
  border: 1px solid rgba(0, 166, 255, 0.16);
  border-radius: var(--linkx-radius-full);
  background: rgba(0, 166, 255, 0.08);
  color: #007aff;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.group-copy-btn:hover {
  background: rgba(0, 166, 255, 0.14);
  color: #005fe0;
}

.group-preview-btn:hover {
  background: rgba(0, 166, 255, 0.16);
  color: #005fe0;
}

.group-enter-btn:hover {
  background: var(--linkx-primary);
  color: white;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--linkx-text-muted);
  gap: 16px;
}

.empty-icon {
  opacity: 0.3;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.empty-subtitle {
  font-size: 13px;
}

@media (max-width: 1100px) {
  .friends-panel {
    width: 280px;
    min-width: 260px;
  }

  .tab-content {
    padding: 14px 16px;
  }
}

@media (max-width: 820px) {
  .content-area {
    flex-direction: column;
  }

  .friends-panel {
    width: 100%;
    min-width: 0;
    max-height: 42%;
    border-right: none;
    border-bottom: 1px solid var(--linkx-border);
  }

  .friend-actions {
    opacity: 1;
  }

  .tabs-header {
    padding: 12px 12px 0;
    flex-wrap: wrap;
  }

  .tab-content {
    padding: 12px;
  }

}

@media (max-width: 560px) {
  .friend-item {
    padding: 12px 10px;
  }
}
</style>
