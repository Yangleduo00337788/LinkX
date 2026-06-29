<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="content-area">
    <!-- 行注：渲染 SessionSidebar 组件 -->
    <SessionSidebar
      :sessions="sessions"
      :draft-preview-by-key="draftPreviewByKey"
      :loading-sessions="loadingSessions"
      :current-target-id="currentTargetId"
      :current-session-type="currentSessionType"
      :search-text="searchText"
      :flash-session-key="flashSessionKey"
      @create-group="openCreateGroupModal"
      @select-session="selectSession"
      @update:search-text="updateSearchText"
    />
    <!-- 行注：渲染容器 -->
    <div class="chat-panel" :class="{ 'has-group-drawer': showGroupDrawer && isGroupSession }">
      <!-- 行注：开始定义模板区域 -->
      <template v-if="currentTargetId">
        <div class="chat-panel-row">
        <div class="chat-main-column">
        <!-- 行注：渲染容器 -->
        <div class="chat-header">
          <!-- 行注：渲染容器 -->
          <div class="chat-header-info">
            <!-- 行注：渲染容器 -->
            <div class="chat-avatar" :class="{ group: isGroupSession }">
              <!-- 行注：渲染图片 -->
              <ProtectedImage v-if="currentSessionAvatar" :src="currentSessionAvatar" class="avatar-img" />
              <!-- 行注：渲染文本节点 -->
              <span v-else>{{ currentSessionName?.charAt(0) || (isGroupSession ? '群' : '聊') }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="chat-meta">
              <!-- 行注：渲染容器 -->
              <div class="chat-name-row">
                <!-- 行注：渲染文本节点 -->
                <span class="chat-name">{{ currentSessionName }}</span>
                <!-- 行注：展示“群聊”文案 -->
                <span v-if="isGroupSession" class="chat-session-tag">群聊</span>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染文本节点 -->
              <span class="chat-subtitle">
                <!-- 行注：开始定义模板区域 -->
                <template v-if="isGroupSession">
                  <!-- 行注：渲染动态文本 -->
                  {{ currentMemberCount }} 位成员
                  <!-- 行注：开始定义模板区域 -->
                  <template v-if="currentNotice"> · 公告：{{ currentNotice }}</template>
                <!-- 行注：结束模板区域 -->
                </template>
                <!-- 行注：开始定义模板区域 -->
                <template v-else>
                  <!-- 行注：渲染动态文本 -->
                  {{ currentSession?.targetOnline ? '在线' : '离线' }}
                  <!-- 行注：开始定义模板区域 -->
                  <template v-if="!wsConnected"> · 实时连接重连中{{ wsReconnectAttempt ? `（第 ${wsReconnectAttempt} 次）` : '' }}</template>
                <!-- 行注：结束模板区域 -->
                </template>
              <!-- 行注：结束文本节点 -->
              </span>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="chat-header-actions">
            <div v-if="isGroupSession" class="header-menu-anchor">
              <button
                ref="groupToolsBtnRef"
                type="button"
                class="header-action-btn"
                title="群相册 / 群文件 / 群精华"
                @click="showGroupToolsMenu = !showGroupToolsMenu"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="7" height="7" rx="1" />
                  <rect x="14" y="3" width="7" height="7" rx="1" />
                  <rect x="3" y="14" width="7" height="7" rx="1" />
                  <rect x="14" y="14" width="7" height="7" rx="1" />
                </svg>
              </button>
              <div v-if="showGroupToolsMenu" ref="groupToolsMenuRef" class="dropdown-menu header-dropdown">
                <div class="dropdown-item" @click="openGroupDrawerTab('album')">群相册</div>
                <div class="dropdown-item" @click="openGroupDrawerTab('files')">群文件</div>
                <div class="dropdown-item" @click="openGroupDrawerTab('highlights')">群精华</div>
              </div>
            </div>
            <button ref="menuBtnRef" class="header-action-btn" title="更多" @click="showMenu = !showMenu">
              <!-- 行注：渲染图标容器 -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="12" cy="12" r="1" />
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="19" cy="12" r="1" />
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="5" cy="12" r="1" />
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染容器 -->
            <div v-if="showMenu" ref="menuRef" class="dropdown-menu">
              <!-- 行注：渲染容器 -->
              <div class="dropdown-item" @click="refreshCurrentSession">
                <!-- 行注：渲染图标容器 -->
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标折线 -->
                  <polyline points="23 4 23 10 17 10" />
                  <!-- 行注：补充图标路径 -->
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                <!-- 行注：结束图标容器 -->
                </svg>
                <!-- 行注：展示“刷新消息”文案 -->
                刷新消息
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div v-if="!isGroupSession" class="dropdown-item" @click="openSingleChatSettings">
                好友与会话备注
              </div>
              <div v-if="isGroupSession" class="dropdown-item" @click="openGroupManagePage">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="8.5" cy="7" r="4" />
                  <path d="M20 8v6" />
                  <path d="M17 11h6" />
                </svg>
                群管理
              </div>
              <div class="dropdown-item" @click="handleToggleSessionPinned">
                {{ currentSession?.pinned ? '取消置顶' : '置顶聊天' }}
              </div>
              <div class="dropdown-item" @click="handleToggleNotificationMuted">
                {{ currentNotificationMuted ? '关闭免打扰' : '消息免打扰' }}
              </div>
              <div class="dropdown-item" @click="handleClearChatHistory">清空聊天记录</div>
              <div class="dropdown-item" @click="openReportChatModal">举报{{ isGroupSession ? '群聊' : '好友' }}</div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="isGroupSession && currentMuted" class="chat-state-bar">
          <!-- 行注：展示“你已被禁言 currentMuteT”文案 -->
          你已被禁言{{ currentMuteTimeText ? `，截止 ${currentMuteTimeText}` : '' }}，暂时无法发送消息。
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div ref="messagesRef" class="chat-messages" @scroll="checkIfAtBottom">
          <!-- 行注：渲染 MessagePane 组件 -->
          <MessagePane
            :messages="messages"
            :loading-messages="loadingMessages"
            :is-group-session="isGroupSession"
            :user-avatar="userStore.avatar"
            :user-nickname="userStore.nickname"
            :active-jump-message-key="activeJumpMessageKey"
            :show-mention-banner="showMentionBanner"
            :mention-banner-text="mentionBannerText"
            :mention-banner-action-text="mentionBannerActionText"
            :get-resolved-message-file-url="getResolvedMessageFileUrl"
            :get-message-text-segments="getMessageTextSegments"
            @mention-banner-click="handleMentionBannerClick"
            @dismiss-mention-banner="dismissMentionBanner"
            @show-message-menu="showMsgMenu"
            @message-media-load="handleMessageMediaLoad"
            @preview-image="previewImage"
            @download-file="downloadFile"
            @retry-failed-message="retryFailedMessage"
            @open-external-link="openMessageExternalLink"
          />
        <!-- 行注：结束容器 -->
        </div>

        <!-- 行注：渲染 MessageComposer 组件 -->
        <MessageComposer
          :input-message="inputMessage"
          :sending="sending"
          :current-muted="currentMuted"
          :show-emoji-picker="showEmojiPicker"
          :show-mention-menu="showMentionMenu"
          :emojis="emojis"
          :mention-candidates="mentionCandidates"
          :mention-highlighted-index="mentionHighlightedIndex"
          :textarea-ref-setter="setTextareaRef"
          :emoji-ref-setter="setEmojiRef"
          :mention-menu-ref-setter="setMentionMenuRef"
          @update:input-message="updateInputMessage"
          @toggle-emoji-picker="toggleEmojiPicker"
          @insert-emoji="insertEmoji"
          @trigger-file-upload="triggerFileUpload"
          @trigger-image-upload="triggerImageUpload"
          @input-keydown="handleComposerKeydown"
          @paste="handleComposerPaste"
          @input-change="handleInputChange"
          @sync-mention-menu="syncMentionMenu"
          @send="sendMessage"
          @select-mention-candidate="selectMentionCandidate"
        />
        </div>
        <GroupDetailPanel
          v-if="isGroupSession"
          :visible="showGroupDrawer"
          :active-tab="groupDrawerTab"
          :group-detail="groupDetail"
          :group-profile-name="groupProfileDraft.groupName"
          :group-profile-avatar-preview="groupProfileDraft.avatarPreview"
          :notice-draft="noticeDraft"
          :current-user-id="currentUserId"
          :can-edit-group-profile="canEditGroupProfile"
          :updating-group-profile="updatingGroupProfile"
          :is-group-profile-changed="isGroupProfileChanged"
          :can-edit-notice="canEditNotice"
          :updating-notice="updatingNotice"
          :is-group-notice-changed="isGroupNoticeChanged"
          :can-manage-members="canManageMembers"
          :can-dissolve-group="canDissolveGroup"
          :is-group-owner="isGroupOwner"
          :can-operate-member="canOperateMember"
          :can-toggle-admin="canToggleAdmin"
          inline
          @close="closeGroupDrawer"
          @update:active-tab="groupDrawerTab = $event"
          @trigger-group-profile-avatar-upload="triggerGroupProfileAvatarUpload"
          @update:group-profile-name="updateGroupProfileName"
          @update:notice-draft="updateNoticeDraft"
          @save-group-profile="submitUpdateGroupProfile"
          @copy-group-id="copyGroupId"
          @save-notice="submitUpdateNotice"
          @open-add-members-modal="openAddMembersModal"
          @toggle-admin-role="toggleAdminRole"
          @toggle-mute-member="toggleMuteMember"
          @remove-member="handleRemoveMember"
          @dissolve-group="handleDissolveGroup"
          @open-transfer-owner-modal="openTransferOwnerModal"
          @leave-group="handleLeaveGroup"
          @preview-media="previewImage"
          @download-media="downloadFile"
          :group-preference-draft="groupPreferenceDraft"
          :member-card-draft="groupPreferenceDraft.memberCardName"
          @update:group-remark="groupPreferenceDraft.groupRemark = $event"
          @update:member-card-name="groupPreferenceDraft.memberCardName = $event"
          @update:group-notification-muted="groupPreferenceDraft.notificationMuted = $event"
          @save-group-preferences="submitGroupPreferencesFromDrawer"
          @open-full-manage="openGroupManagePage"
        />
        </div>
      </template>
      <!-- 行注：渲染容器 -->
      <div v-else class="panel-placeholder">
        <!-- 行注：渲染容器 -->
        <div class="placeholder-icon">
          <!-- 行注：渲染图标容器 -->
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <!-- 行注：补充图标路径 -->
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“选择一个会话开始聊天”文案 -->
        <div class="placeholder-title">选择一个会话开始聊天</div>
        <!-- 行注：展示“支持单聊、群聊与实时消息同步”文案 -->
        <div class="placeholder-text">支持单聊、群聊与实时消息同步</div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
  <!-- 行注：渲染输入框 -->
  <input ref="fileInputRef" type="file" style="display: none" @change="handleFileUpload" />
  <!-- 行注：渲染输入框 -->
  <input ref="imageInputRef" type="file" accept="image/*" style="display: none" @change="handleImageUpload" />
  <!-- 行注：渲染输入框 -->
  <input ref="groupAvatarInputRef" type="file" accept="image/*" style="display: none" @change="handleGroupAvatarSelected" />
  <Teleport to="body">
    <div
      v-if="showMsgContextMenu"
      ref="msgContextMenuRef"
      class="msg-context-menu"
      :style="{ left: `${msgMenuX}px`, top: `${msgMenuY}px` }"
      @mousedown.stop
      @contextmenu.prevent
    >
      <button type="button" class="msg-context-item" @click="copySelectedMessage">复制</button>
      <button
        v-if="selectedMsg && !selectedMsg.isMe"
        type="button"
        class="msg-context-item"
        @click="openReportModal"
      >
        举报
      </button>
      <button
        type="button"
        class="msg-context-item"
        :disabled="!canRecallMessage(selectedMsg)"
        @click="handleRecallMessage"
      >
        撤回
      </button>
    </div>
  </Teleport>
  <n-modal v-model:show="showReportModal" preset="dialog" title="举报消息" positive-text="提交" negative-text="取消" @positive-click="submitReportMessage">
    <n-input v-model:value="reportReasonDetail" type="textarea" placeholder="请简要说明举报原因（可选）" :rows="3" />
  </n-modal>
  <n-modal
    v-model:show="showReportChatModal"
    preset="dialog"
    :title="isGroupSession ? '举报群聊' : '举报好友'"
    positive-text="提交"
    negative-text="取消"
    @positive-click="submitReportChat"
  >
    <n-input v-model:value="reportChatReasonDetail" type="textarea" placeholder="请简要说明举报原因（可选）" :rows="3" />
  </n-modal>
  <n-modal
    v-model:show="showSingleChatSettingsModal"
    preset="dialog"
    title="好友与会话备注"
    positive-text="保存"
    negative-text="取消"
    @positive-click="submitSingleChatSettings"
  >
    <p class="section-hint" style="margin: 0 0 12px; font-size: 13px; color: var(--text-secondary)">
      好友备注会同步到通讯录；会话备注仅影响本会话在列表中的展示名称。
    </p>
    <label class="field-label">好友备注</label>
    <n-input v-model:value="singleChatSettingsDraft.friendRemark" maxlength="64" placeholder="对方在好友列表中的名称" />
    <label class="field-label" style="margin-top: 12px">会话备注</label>
    <n-input v-model:value="singleChatSettingsDraft.sessionRemark" maxlength="100" placeholder="仅本会话列表展示" />
  </n-modal>
  <!-- 行注：渲染 ChatDialogs 组件 -->
  <ChatDialogs
    :show-image-preview="showImagePreview"
    :preview-image-url="previewImageUrl"
    :show-download-modal="showDownloadModal"
    :download-file-name="downloadFileName"
    :download-file-size="downloadFileSize"
    :download-progress="downloadProgress"
    :show-create-group-modal="showCreateGroupModal"
    :create-group-form="createGroupForm"
    :friends="friends"
    :creating-group="creatingGroup"
    :show-add-members-modal="showAddMembersModal"
    :available-friends-for-current-group="availableFriendsForCurrentGroup"
    :add-members-selection="addMembersSelection"
    :add-members-message="addMembersMessage"
    :adding-members="addingMembers"
    :show-transfer-owner-modal="showTransferOwnerModal"
    :transfer-owner-selection="transferOwnerSelection"
    :transferring-owner="transferringOwner"
    :show-mute-member-modal="showMuteMemberModal"
    :mute-target-member="muteTargetMember"
    :mute-minutes-input="muteMinutesInput"
    :muting-member="mutingMember"
    :group-detail="groupDetail"
    :confirm-dialog="confirmDialog"
    :show-notice-reminder="showNoticeReminder"
    :group-notice-update-time-text="groupNoticeUpdateTimeText"
    :acknowledging-notice-reminder="acknowledgingNoticeReminder"
    @close-image-preview="closeImagePreview"
    @close-download-dialog="closeDownloadDialog"
    @open-downloaded-file="openDownloadedFile"
    @save-downloaded-file="saveDownloadedFile"
    @close-create-group-modal="closeCreateGroupModal"
    @trigger-group-avatar-upload="triggerGroupAvatarUpload"
    @update:create-group-name="updateCreateGroupName"
    @update:create-group-notice="updateCreateGroupNotice"
    @update:create-group-member-ids="updateCreateGroupMemberIds"
    @submit-create-group="submitCreateGroup"
    @close-add-members-modal="closeAddMembersModal"
    @update:add-members-selection="updateAddMembersSelection"
    @update:add-members-message="updateAddMembersMessage"
    @submit-add-members="submitAddMembers"
    @close-transfer-owner-modal="closeTransferOwnerModal"
    @update:transfer-owner-selection="updateTransferOwnerSelection"
    @submit-transfer-owner="submitTransferOwner"
    @close-mute-member-modal="closeMuteMemberModal"
    @update:mute-minutes-input="updateMuteMinutesInput"
    @submit-mute-member="submitMuteMember"
    @update:confirm-dialog-visible="setConfirmDialogVisible"
    @cancel-confirm-dialog="cancelConfirmDialog"
    @confirm-confirm-dialog="confirmConfirmDialog"
    @update:show-notice-reminder="setShowNoticeReminder"
    @acknowledge-notice-reminder="acknowledgeNoticeReminder"
  />
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 聊天主页面，组合会话列表、消息面板、输入区与群管理弹窗。
 */
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'  // 行注：引入 computed, onMounted, onUnmounted, reactive, ref, watch 能力
import { useRoute, useRouter } from 'vue-router'  // 行注：引入 useRoute, useRouter 能力
import { storeToRefs } from 'pinia'  // 行注：引入 storeToRefs 能力
import { NInput, NModal, useMessage } from 'naive-ui'  // 行注：引入 useMessage 能力
import ProtectedImage from '../components/ProtectedImage.vue'
import SessionSidebar from '../components/chat/SessionSidebar.vue'
import MessagePane from '../components/chat/MessagePane.vue'  // 行注：引入 MessagePane 组件
import MessageComposer from '../components/chat/MessageComposer.vue'  // 行注：引入 MessageComposer 组件
import ChatDialogs from '../components/chat/ChatDialogs.vue'  // 行注：引入 ChatDialogs 组件
import GroupDetailPanel from '../components/chat/GroupDetailPanel.vue'
import { useChatRuntime } from '../hooks/useChatRuntime'  // 行注：引入 useChatRuntime 能力
import { useGroupManagement } from '../hooks/useGroupManagement'  // 行注：引入 useGroupManagement 能力
import { useChatComposer } from '../hooks/useChatComposer'  // 行注：引入 useChatComposer 能力
import { useChatMessageActions } from '../hooks/useChatMessageActions'  // 行注：引入 useChatMessageActions 能力
import { useConfirmDialog } from '../hooks/useConfirmDialog'  // 行注：引入 useConfirmDialog 能力
import { useChatSessionActions } from '../hooks/useChatSessionActions'
import { groupApi } from '../api/client'
import { useChatStore } from '../stores/chat'  // 行注：引入 useChatStore 能力
import { useChatDraftStore } from '../stores/chatDraft'
import { useUserStore } from '../stores/user'  // 行注：引入 useUserStore 能力
import {
  clearDraftOnServer,
  flushDraftSync,
  scheduleDraftSync
} from '../utils/chatDraftSync'
import {  // 行注：引入 { 模块
  GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
  GROUP_ROLE_MEMBER,  // 行注：补充当前配置项
  GROUP_ROLE_OWNER,  // 行注：补充当前配置项
  SESSION_TYPE_GROUP,  // 行注：补充当前配置项
  type ChatSession,  // 行注：补充当前配置项
  type GroupDetail  // 行注：补充当前表达式
} from '../types/chat'  // 行注：补充当前表达式
import { buildSessionKey, formatDateTime } from '../utils/chat'
import { setupFileDrop } from '../utils/fileDrop'
import { getElectronAPI } from '../utils/electron'
import { base64PayloadToFile } from '../utils/droppedFile'
import { openSafeExternalUrl } from '../utils/url'

const route = useRoute()  // 行注：获取 route 组合式能力
const router = useRouter()  // 行注：获取路由实例
const message = useMessage()  // 行注：获取全局消息实例
const userStore = useUserStore()  // 行注：获取 userStore 组合式能力
const chatStore = useChatStore()  // 行注：获取 chatStore 组合式能力
const chatDraftStore = useChatDraftStore()
const previousSessionKey = ref<string | null>(null)
const msgContextMenuRef = ref<HTMLElement | null>(null)
const showReportModal = ref(false)
const reportReasonDetail = ref('')
const { sessions, messages, friends, currentTargetId, currentSessionType, searchText } = storeToRefs(chatStore)  // 行注：声明当前变量
const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()  // 行注：声明当前变量

const groupDetail = ref<GroupDetail | null>(null)  // 行注：初始化 groupDetail 变量
const showGroupDrawer = ref(false)  // 行注：初始化 showGroupDrawer 响应式状态
const groupDrawerTab = ref<'info' | 'members' | 'files' | 'album' | 'highlights' | 'manage'>('info')
const showGroupToolsMenu = ref(false)
const groupToolsBtnRef = ref<HTMLElement>()
const groupToolsMenuRef = ref<HTMLElement>()
const showReportChatModal = ref(false)
const reportChatReasonDetail = ref('')
const noticeDraft = ref('')  // 行注：初始化 noticeDraft 响应式状态
const groupProfileDraft = reactive({  // 行注：开始解构当前返回值
  groupName: '',  // 行注：设置 groupName 配置项
  avatarPreview: '',  // 行注：设置 avatarPreview 配置项
  avatarFile: null as File | null  // 行注：设置 avatarFile 配置项
})  // 行注：结束当前调用配置

const loadingMessages = ref(false)  // 行注：初始化 loadingMessages 响应式状态
const loadingSessions = ref(false)  // 行注：初始化 loadingSessions 响应式状态
const showMenu = ref(false)  // 行注：初始化 showMenu 响应式状态
const messagesRef = ref<HTMLElement>()  // 行注：初始化 messagesRef 状态
const groupAvatarInputRef = ref<HTMLInputElement>()  // 行注：初始化 groupAvatarInputRef 状态
const menuRef = ref<HTMLElement>()  // 行注：初始化 menuRef 状态
const menuBtnRef = ref<HTMLElement>()  // 行注：初始化 menuBtnRef 状态
const groupProfileDirty = ref(false)  // 行注：初始化 groupProfileDirty 响应式状态
const groupNoticeDirty = ref(false)  // 行注：初始化 groupNoticeDirty 响应式状态

const currentSession = computed(() => {  // 行注：开始解构当前返回值
  if (!currentTargetId.value) {  // 行注：判断当前条件是否成立
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return sessions.value.find(session => {  // 行注：返回当前结果
    return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(currentTargetId.value || '', currentSessionType.value)  // 行注：返回当前结果
  }) || null  // 行注：补充当前表达式
})  // 行注：结束当前调用配置

const isGroupSession = computed(() => currentSessionType.value === SESSION_TYPE_GROUP)  // 行注：声明 isGroupSession 计算属性
const currentSessionName = computed(() => groupDetail.value?.groupRemark || groupDetail.value?.groupName || currentSession.value?.targetNickname || '')  // 行注：声明 currentSessionName 计算属性
const currentSessionAvatar = computed(() => groupDetail.value?.groupAvatar || currentSession.value?.targetAvatar || '')  // 行注：声明 currentSessionAvatar 计算属性
const currentMemberCount = computed(() => groupDetail.value?.memberCount || currentSession.value?.memberCount || 0)  // 行注：声明 currentMemberCount 计算属性
const currentNotice = computed(() => groupDetail.value?.notice || currentSession.value?.notice || '')  // 行注：声明 currentNotice 计算属性
const currentGroupRole = computed(() => groupDetail.value?.myRole ?? currentSession.value?.myRole ?? GROUP_ROLE_MEMBER)  // 行注：声明 currentGroupRole 计算属性
const currentMuted = computed(() => Boolean(groupDetail.value?.muted ?? currentSession.value?.muted))  // 行注：声明 currentMuted 计算属性
const currentMuteTimeText = computed(() => formatDateTime(groupDetail.value?.muteTime || currentSession.value?.muteTime || ''))  // 行注：声明 currentMuteTimeText 计算属性
const currentNotificationMuted = computed(() => Boolean(groupDetail.value?.notificationMuted ?? currentSession.value?.notificationMuted))  // 行注：声明 currentNotificationMuted 计算属性
const canManageMembers = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canManageMembers 计算属性
const canEditGroupProfile = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canEditGroupProfile 计算属性
const canEditNotice = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canEditNotice 计算属性
const canMentionAll = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)  // 行注：声明 canMentionAll 计算属性
const canDissolveGroup = computed(() => isGroupSession.value && currentGroupRole.value === GROUP_ROLE_OWNER)  // 行注：声明 canDissolveGroup 计算属性
const isGroupOwner = computed(() => isGroupSession.value && currentGroupRole.value === GROUP_ROLE_OWNER)  // 行注：声明 isGroupOwner 计算属性
const currentUserId = computed(() => String(userStore.userId || ''))  // 行注：声明 currentUserId 计算属性
const groupNoticeUpdateTimeText = computed(() => formatDateTime(groupDetail.value?.noticeUpdateTime || ''))  // 行注：声明 groupNoticeUpdateTimeText 计算属性

let syncGroupProfileDraftImpl: (detail?: GroupDetail | null) => void = () => undefined  // 行注：初始化 syncGroupProfileDraftImpl 方法
let closeGroupDrawerImpl: (options?: { force?: boolean }) => Promise<boolean> | boolean = () => true  // 行注：初始化 closeGroupDrawerImpl 方法
let sendMessageImpl: () => Promise<void> = async () => undefined  // 行注：初始化 sendMessageImpl 变量

const runtime = useChatRuntime({  // 行注：开始解构当前返回值
  route,  // 行注：传入 route 参数
  router,  // 行注：传入 router 参数
  message,  // 行注：传入 message 参数
  sessions,  // 行注：传入 sessions 参数
  messages,  // 行注：传入 messages 参数
  friends,  // 行注：传入 friends 参数
  currentTargetId,  // 行注：传入 currentTargetId 参数
  currentSessionType,  // 行注：传入 currentSessionType 参数
  groupDetail,  // 行注：传入 groupDetail 参数
  showGroupDrawer,  // 行注：传入 showGroupDrawer 参数
  noticeDraft,  // 行注：传入 noticeDraft 参数
  isGroupSession,  // 行注：传入 isGroupSession 参数
  currentSessionName,  // 行注：传入 currentSessionName 参数
  currentNotificationMuted,  // 行注：传入 currentNotificationMuted 参数
  isGroupProfileChanged: computed(() => groupProfileDirty.value),  // 行注：设置 isGroupProfileChanged 配置项
  isGroupNoticeChanged: computed(() => groupNoticeDirty.value),  // 行注：设置 isGroupNoticeChanged 配置项
  messagesRef,  // 行注：传入 messagesRef 参数
  loadingSessions,  // 行注：传入 loadingSessions 参数
  loadingMessages,  // 行注：传入 loadingMessages 参数
  showMenu,  // 行注：传入 showMenu 参数
  syncGroupProfileDraft: detail => syncGroupProfileDraftImpl(detail),  // 行注：设置 syncGroupProfileDraft 配置项
  closeGroupDrawer: options => closeGroupDrawerImpl(options),  // 行注：设置 closeGroupDrawer 配置项
  getToken: () => userStore.token,  // 行注：传入 getToken 回调
  getCurrentUserId: () => userStore.userId,  // 行注：传入 getCurrentUserId 回调
  getCurrentUserNickname: () => userStore.nickname,  // 行注：传入 getCurrentUserNickname 回调
  getCurrentUserAvatar: () => userStore.avatar,  // 行注：传入 getCurrentUserAvatar 回调
  onResetConversationState: () => {  // 行注：传入 onResetConversationState 回调
    chatStore.resetConversation()  // 行注：调用 resetConversation 方法
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置

const {  // 行注：开始解构当前返回值
  initialized,  // 行注：解构 initialized 方法
  flashSessionKey,  // 行注：解构 flashSessionKey 状态
  showNoticeReminder,  // 行注：解构 showNoticeReminder 方法
  acknowledgingNoticeReminder,  // 行注：解构 acknowledgingNoticeReminder 状态
  activeJumpMessageKey,  // 行注：解构 activeJumpMessageKey 状态
  showMentionBanner,  // 行注：解构 showMentionBanner 方法
  mentionBannerText,  // 行注：解构 mentionBannerText 状态
  mentionBannerActionText,  // 行注：解构 mentionBannerActionText 状态
  wsConnected,  // 行注：解构 wsConnected 状态
  wsReconnectAttempt,  // 行注：解构 wsReconnectAttempt 状态
  connectChatSocket,  // 行注：解构 connectChatSocket 方法
  disconnectChatSocket,  // 行注：解构 disconnectChatSocket 方法
  loadFriends,  // 行注：解构 loadFriends 方法
  loadSessions,  // 行注：解构 loadSessions 方法
  selectSession,  // 行注：解构 selectSession 状态
  initializeRouteSession,  // 行注：解构 initializeRouteSession 方法
  refreshCurrentSession,  // 行注：解构 refreshCurrentSession 方法
  acknowledgeNoticeReminder,  // 行注：解构 acknowledgeNoticeReminder 状态
  dismissMentionBanner,  // 行注：解构 dismissMentionBanner 状态
  handleMentionBannerClick,  // 行注：解构 handleMentionBannerClick 方法
  checkIfAtBottom,  // 行注：解构 checkIfAtBottom 状态
  handleMessageMediaLoad,  // 行注：解构 handleMessageMediaLoad 方法
  getMessageTextSegments,  // 行注：解构 getMessageTextSegments 方法
  getResolvedMessageFileUrl,  // 行注：解构 getResolvedMessageFileUrl 方法
  ensureMessageFileAccessUrl,  // 行注：解构 ensureMessageFileAccessUrl 状态
  toDisplayMessage,  // 行注：解构 toDisplayMessage 状态
  upsertSession,  // 行注：解构 upsertSession 状态
  upsertMessage,  // 行注：解构 upsertMessage 状态
  applyGroupDetail,  // 行注：解构 applyGroupDetail 方法
  removeSessionByTarget,  // 行注：解构 removeSessionByTarget 方法
  resetCurrentConversationState,  // 行注：解构 resetCurrentConversationState 方法
  sendChatCommand,  // 行注：解构 sendChatCommand 状态
  loadGroupDetail,  // 行注：解构 loadGroupDetail 方法
  cleanupMessageResources,  // 行注：解构 cleanupMessageResources 状态
  clearActiveJumpHighlight,  // 行注：解构 clearActiveJumpHighlight 方法
  scrollMessagesToBottom  // 行注：解构 scrollMessagesToBottom 状态
} = runtime  // 行注：补充当前表达式

const groupManagement = useGroupManagement({  // 行注：开始解构当前返回值
  friends,  // 行注：传入 friends 参数
  sessions,  // 行注：传入 sessions 参数
  currentTargetId,  // 行注：传入 currentTargetId 参数
  currentGroupRole,  // 行注：传入 currentGroupRole 参数
  currentSessionName,  // 行注：传入 currentSessionName 参数
  isGroupSession,  // 行注：传入 isGroupSession 参数
  canManageMembers,  // 行注：传入 canManageMembers 参数
  canEditGroupProfile,  // 行注：传入 canEditGroupProfile 参数
  canEditNotice,  // 行注：传入 canEditNotice 参数
  groupDetail,  // 行注：传入 groupDetail 参数
  showGroupDrawer,  // 行注：传入 showGroupDrawer 参数
  noticeDraft,  // 行注：传入 noticeDraft 参数
  groupProfileDraft,  // 行注：传入 groupProfileDraft 参数
  groupAvatarInputRef,  // 行注：传入 groupAvatarInputRef 参数
  getCurrentUserId: () => userStore.userId,  // 行注：传入 getCurrentUserId 回调
  message,  // 行注：传入 message 参数
  router,  // 行注：传入 router 参数
  openConfirmDialog,  // 行注：传入 openConfirmDialog 参数
  loadSessions: () => loadSessions(),  // 行注：传入 loadSessions 回调
  loadGroupDetail: (groupId, syncDraft) => loadGroupDetail(groupId, syncDraft),  // 行注：设置 loadGroupDetail 配置项
  selectSession: (session: ChatSession, syncRoute = true) => selectSession(session, syncRoute),  // 行注：设置 selectSession 配置项
  upsertSession,  // 行注：传入 upsertSession 参数
  applyGroupDetail,  // 行注：传入 applyGroupDetail 参数
  removeSessionByTarget,  // 行注：传入 removeSessionByTarget 参数
  resetCurrentConversationState,
  showMenu,
  onGroupDrawerTab: tab => {
    groupDrawerTab.value = tab
  }
})

const sessionActions = useChatSessionActions({
  message,
  currentTargetId,
  currentSessionType,
  currentSession,
  isGroupSession,
  upsertSession,
  loadSessions: () => loadSessions(),
  refreshCurrentSession,
  messages,
  openConfirmDialog
})

const {
  toggleSessionPinned,
  toggleNotificationMuted,
  clearChatHistory,
  reportCurrentChat,
  saveGroupRemark,
  saveFriendRemark,
  saveSessionRemark
} = sessionActions

const showSingleChatSettingsModal = ref(false)
const singleChatSettingsDraft = reactive({ friendRemark: '', sessionRemark: '' })

function openSingleChatSettings() {
  showMenu.value = false
  if (isGroupSession.value || !currentTargetId.value) return
  singleChatSettingsDraft.friendRemark = currentSession.value?.friendRemark || ''
  singleChatSettingsDraft.sessionRemark = currentSession.value?.sessionRemark || ''
  showSingleChatSettingsModal.value = true
}

async function submitSingleChatSettings() {
  await saveFriendRemark(singleChatSettingsDraft.friendRemark)
  await saveSessionRemark(singleChatSettingsDraft.sessionRemark)
  showSingleChatSettingsModal.value = false
  return true
}

const groupPreferenceDraft = reactive({
  groupRemark: '',
  notificationMuted: false,
  memberCardName: ''
})

function syncGroupPreferenceDraftFromDetail() {
  groupPreferenceDraft.groupRemark = groupDetail.value?.groupRemark || ''
  groupPreferenceDraft.notificationMuted = Boolean(groupDetail.value?.notificationMuted)
  groupPreferenceDraft.memberCardName = groupDetail.value?.memberCardName || ''
}

watch(groupDetail, () => syncGroupPreferenceDraftFromDetail(), { deep: true })

function openGroupManagePage() {
  showMenu.value = false
  if (!currentTargetId.value || !isGroupSession.value) return
  void router.push({ name: 'GroupMembers', params: { groupId: String(currentTargetId.value) } })
}

async function handleToggleSessionPinned() {
  showMenu.value = false
  await toggleSessionPinned()
}

async function handleToggleNotificationMuted() {
  showMenu.value = false
  await toggleNotificationMuted()
}

async function handleClearChatHistory() {
  showMenu.value = false
  await clearChatHistory()
}

function openReportChatModal() {
  showMenu.value = false
  reportChatReasonDetail.value = ''
  showReportChatModal.value = true
}

async function submitReportChat() {
  const ok = await reportCurrentChat(reportChatReasonDetail.value)
  if (ok) showReportChatModal.value = false
  return ok
}

async function submitGroupPreferencesFromDrawer() {
  if (!currentTargetId.value) return
  try {
    await groupApi.updatePreferences(currentTargetId.value, {
      groupRemark: groupPreferenceDraft.groupRemark.trim(),
      notificationMuted: groupPreferenceDraft.notificationMuted,
      memberCardName: groupPreferenceDraft.memberCardName.trim()
    })
    message.success('已保存群偏好')
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  }
}

syncGroupProfileDraftImpl = groupManagement.syncGroupProfileDraft  // 行注：更新 syncGroupProfileDraftImpl 值
closeGroupDrawerImpl = groupManagement.closeGroupDrawer  // 行注：更新 closeGroupDrawerImpl 值

const {  // 行注：开始解构当前返回值
  showCreateGroupModal,  // 行注：解构 showCreateGroupModal 状态
  showAddMembersModal,  // 行注：解构 showAddMembersModal 状态
  creatingGroup,  // 行注：解构 creatingGroup 状态
  addMembersSelection,  // 行注：解构 addMembersSelection 状态
  addMembersMessage,  // 行注：解构 addMembersMessage 状态
  addingMembers,  // 行注：解构 addingMembers 状态
  updatingNotice,  // 行注：解构 updatingNotice 状态
  updatingGroupProfile,  // 行注：解构 updatingGroupProfile 状态
  showTransferOwnerModal,  // 行注：解构 showTransferOwnerModal 状态
  transferOwnerSelection,  // 行注：解构 transferOwnerSelection 状态
  transferringOwner,  // 行注：解构 transferringOwner 状态
  showMuteMemberModal,  // 行注：解构 showMuteMemberModal 状态
  muteTargetMember,  // 行注：解构 muteTargetMember 状态
  muteMinutesInput,  // 行注：解构 muteMinutesInput 状态
  mutingMember,  // 行注：解构 mutingMember 状态
  createGroupForm,  // 行注：解构 createGroupForm 状态
  isGroupProfileChanged,  // 行注：解构 isGroupProfileChanged 状态
  isGroupNoticeChanged,  // 行注：解构 isGroupNoticeChanged 状态
  availableFriendsForCurrentGroup,  // 行注：解构 availableFriendsForCurrentGroup 状态
  resetCreateGroupForm,  // 行注：解构 resetCreateGroupForm 方法
  resetGroupProfileDraft,  // 行注：解构 resetGroupProfileDraft 方法
  openCreateGroupModal,  // 行注：解构 openCreateGroupModal 方法
  closeCreateGroupModal,  // 行注：解构 closeCreateGroupModal 方法
  triggerGroupAvatarUpload,  // 行注：解构 triggerGroupAvatarUpload 方法
  triggerGroupProfileAvatarUpload,  // 行注：解构 triggerGroupProfileAvatarUpload 方法
  handleGroupAvatarSelected,  // 行注：解构 handleGroupAvatarSelected 方法
  submitCreateGroup,  // 行注：解构 submitCreateGroup 方法
  openGroupDrawer,  // 行注：解构 openGroupDrawer 方法
  closeGroupDrawer,  // 行注：解构 closeGroupDrawer 方法
  openGroupMembersPage,  // 行注：解构 openGroupMembersPage 方法
  openAddMembersModal,  // 行注：解构 openAddMembersModal 方法
  closeAddMembersModal,  // 行注：解构 closeAddMembersModal 方法
  submitAddMembers,  // 行注：解构 submitAddMembers 方法
  submitUpdateNotice,  // 行注：解构 submitUpdateNotice 方法
  submitUpdateGroupProfile,  // 行注：解构 submitUpdateGroupProfile 方法
  canOperateMember,  // 行注：解构 canOperateMember 状态
  canToggleAdmin,  // 行注：解构 canToggleAdmin 状态
  toggleAdminRole,  // 行注：解构 toggleAdminRole 方法
  toggleMuteMember,  // 行注：解构 toggleMuteMember 方法
  closeMuteMemberModal,  // 行注：解构 closeMuteMemberModal 方法
  submitMuteMember,  // 行注：解构 submitMuteMember 方法
  handleRemoveMember,  // 行注：解构 handleRemoveMember 方法
  handleDissolveGroup,  // 行注：解构 handleDissolveGroup 方法
  handleLeaveGroup,  // 行注：解构 handleLeaveGroup 方法
  openTransferOwnerModal,  // 行注：解构 openTransferOwnerModal 方法
  closeTransferOwnerModal,  // 行注：解构 closeTransferOwnerModal 方法
  submitTransferOwner  // 行注：解构 submitTransferOwner 方法
} = groupManagement  // 行注：补充当前表达式

async function openGroupDrawerTab(tab: typeof groupDrawerTab.value) {
  showGroupToolsMenu.value = false
  showMenu.value = false
  groupDrawerTab.value = tab
  await openGroupDrawer()
}

watch(isGroupProfileChanged, value => {  // 行注：监听状态变化
  groupProfileDirty.value = value  // 行注：更新 groupProfileDirty 状态
}, { immediate: true })  // 行注：补充当前表达式

watch(isGroupNoticeChanged, value => {  // 行注：监听状态变化
  groupNoticeDirty.value = value  // 行注：更新 groupNoticeDirty 状态
}, { immediate: true })  // 行注：补充当前表达式

const composer = useChatComposer({  // 行注：开始解构当前返回值
  currentMuted,  // 行注：传入 currentMuted 参数
  isGroupSession,  // 行注：传入 isGroupSession 参数
  canMentionAll,  // 行注：传入 canMentionAll 参数
  groupDetail,  // 行注：传入 groupDetail 参数
  getCurrentUserId: () => userStore.userId,  // 行注：传入 getCurrentUserId 回调
  onSend: () => sendMessageImpl()  // 行注：传入 onSend 回调
})  // 行注：结束当前调用配置

const {  // 行注：开始解构当前返回值
  inputMessage,  // 行注：解构 inputMessage 状态
  showEmojiPicker,  // 行注：解构 showEmojiPicker 方法
  showMentionMenu,  // 行注：解构 showMentionMenu 状态
  mentionHighlightedIndex,  // 行注：解构 mentionHighlightedIndex 状态
  mentionCandidates,  // 行注：解构 mentionCandidates 状态
  textareaRef,  // 行注：解构 textareaRef 状态
  fileInputRef,  // 行注：解构 fileInputRef 状态
  imageInputRef,  // 行注：解构 imageInputRef 状态
  emojiRef,  // 行注：解构 emojiRef 状态
  mentionMenuRef,  // 行注：解构 mentionMenuRef 状态
  setTextareaRef,  // 行注：解构 setTextareaRef 方法
  setEmojiRef,  // 行注：解构 setEmojiRef 方法
  setMentionMenuRef,  // 行注：解构 setMentionMenuRef 方法
  closeMentionMenu,  // 行注：解构 closeMentionMenu 方法
  syncMentionMenu,  // 行注：解构 syncMentionMenu 方法
  handleInputChange,  // 行注：解构 handleInputChange 方法
  handleInputKeydown,  // 行注：解构 handleInputKeydown 方法
  selectMentionCandidate,  // 行注：解构 selectMentionCandidate 状态
  insertEmoji,  // 行注：解构 insertEmoji 状态
  triggerFileUpload,  // 行注：解构 triggerFileUpload 方法
  triggerImageUpload,  // 行注：解构 triggerImageUpload 方法
  resolveOutgoingMentions  // 行注：解构 resolveOutgoingMentions 方法
} = composer  // 行注：补充当前表达式

void fileInputRef  // 行注：补充当前表达式
void imageInputRef  // 行注：补充当前表达式

const messageActions = useChatMessageActions({  // 行注：开始解构当前返回值
  message,  // 行注：传入 message 参数
  messages,  // 行注：传入 messages 参数
  currentTargetId,  // 行注：传入 currentTargetId 参数
  currentSessionType,  // 行注：传入 currentSessionType 参数
  currentMuted,  // 行注：传入 currentMuted 参数
  inputMessage,  // 行注：传入 inputMessage 参数
  textareaRef,  // 行注：传入 textareaRef 参数
  closeMentionMenu,  // 行注：传入 closeMentionMenu 参数
  getCurrentUserId: () => userStore.userId,  // 行注：传入 getCurrentUserId 回调
  getCurrentUserNickname: () => userStore.nickname,  // 行注：传入 getCurrentUserNickname 回调
  getCurrentUserAvatar: () => userStore.avatar,  // 行注：传入 getCurrentUserAvatar 回调
  sendChatCommand,  // 行注：传入 sendChatCommand 参数
  toDisplayMessage,  // 行注：传入 toDisplayMessage 参数
  upsertMessage,  // 行注：传入 upsertMessage 参数
  scrollMessagesToBottom,  // 行注：传入 scrollMessagesToBottom 参数
  ensureMessageFileAccessUrl,  // 行注：传入 ensureMessageFileAccessUrl 参数
  getResolvedMessageFileUrl,  // 行注：传入 getResolvedMessageFileUrl 参数
  onAfterSendSuccess: async () => {
    if (!currentTargetId.value) return
    chatDraftStore.clearDraft(currentTargetId.value, currentSessionType.value)
    await clearDraftOnServer(currentTargetId.value, currentSessionType.value)
  }
})  // 行注：结束当前调用配置

const {  // 行注：开始解构当前返回值
  sending,  // 行注：解构 sending 状态
  showMsgContextMenu,  // 行注：解构 showMsgContextMenu 状态
  showImagePreview,  // 行注：解构 showImagePreview 状态
  showDownloadModal,  // 行注：解构 showDownloadModal 状态
  msgMenuX,  // 行注：解构 msgMenuX 状态
  msgMenuY,  // 行注：解构 msgMenuY 状态
  selectedMsg,  // 行注：解构 selectedMsg 状态
  previewImageUrl,  // 行注：解构 previewImageUrl 状态
  downloadFileName,  // 行注：解构 downloadFileName 状态
  downloadFileSize,  // 行注：解构 downloadFileSize 状态
  downloadProgress,  // 行注：解构 downloadProgress 状态
  handleSend,  // 行注：解构 handleSend 方法
  handleFileUpload,  // 行注：解构 handleFileUpload 方法
  handleImageUpload,  // 行注：解构 handleImageUpload 方法
  retryFailedMessage,  // 行注：解构 retryFailedMessage 状态
  previewImage,  // 行注：解构 previewImage 状态
  closeImagePreview,  // 行注：解构 closeImagePreview 方法
  downloadFile,  // 行注：解构 downloadFile 状态
  openDownloadedFile,  // 行注：解构 openDownloadedFile 方法
  saveDownloadedFile,  // 行注：解构 saveDownloadedFile 状态
  showMsgMenu,  // 行注：解构 showMsgMenu 状态
  canRecallMessage,  // 行注：解构 canRecallMessage 状态
  handleRecallMessage,  // 行注：解构 handleRecallMessage 方法
  handleCopyMessage,
  handleReportMessage,
  handleDroppedFiles,
  sendFileFromComposer
} = messageActions

sendMessageImpl = async () => {  // 行注：执行当前调用逻辑
  await handleSend(resolveOutgoingMentions(inputMessage.value))  // 行注：调用 handleSend 方法
}  // 行注：结束当前代码块

// 模板层统一调用该方法发送消息，内部再转交给组合式逻辑实现。
function sendMessage() {  // 行注：定义 sendMessage 方法
  return sendMessageImpl()  // 行注：返回当前结果
}  // 行注：结束当前代码块

const emojis = ['😀', '😂', '🤣', '😍', '🥰', '😘', '😎', '🤔', '😏', '😢', '😭', '😡', '🥳', '😱', '🥺', '😴', '🤗', '😈', '👻', '💀', '🤡', '👽', '🤖', '💩', '❤️', '🔥', '👍', '👎', '👏', '🙏', '💪', '🎉', '🎊', '☕', '🌹', '🍀', '🌈', '☀️', '🌙', '⭐']  // 行注：初始化 emojis 变量

// 会话搜索关键字存回全局状态，便于侧边栏和页面共享筛选条件。
function updateSearchText(value: string) {  // 行注：定义 updateSearchText 方法
  searchText.value = value  // 行注：更新 searchText 状态
}  // 行注：结束当前代码块

const draftPreviewByKey = computed(() => ({ ...chatDraftStore.draftsByKey }))

function persistDraftForCurrentSession() {
  if (!currentTargetId.value) return
  chatDraftStore.setDraft(currentTargetId.value, currentSessionType.value, inputMessage.value)
  scheduleDraftSync(currentTargetId.value, currentSessionType.value, inputMessage.value)
}

async function flushDraftForSession(targetId: string, sessionType: number, content: string) {
  chatDraftStore.setDraft(targetId, sessionType, content)
  await flushDraftSync(targetId, sessionType, content)
}

function restoreInputFromDraft(targetId: string, sessionType: number) {
  inputMessage.value = chatDraftStore.getDraft(targetId, sessionType)
}

// 输入框内容由页面层同步到组合式状态，保持单一数据源。
function updateInputMessage(value: string) {
  inputMessage.value = value
  persistDraftForCurrentSession()
}

// 切换表情面板显示状态。
function toggleEmojiPicker() {  // 行注：定义 toggleEmojiPicker 方法
  showEmojiPicker.value = !showEmojiPicker.value  // 行注：更新 showEmojiPicker 状态
}  // 行注：结束当前代码块

// 复制消息后顺手关闭右键菜单，保持交互闭环。
async function copySelectedMessage() {
  await handleCopyMessage()
  showMsgContextMenu.value = false
}

function openReportModal() {
  reportReasonDetail.value = ''
  showMsgContextMenu.value = false
  showReportModal.value = true
}

async function submitReportMessage() {
  try {
    await handleReportMessage(reportReasonDetail.value)
    showReportModal.value = false
    reportReasonDetail.value = ''
    return true
  } catch {
    return false
  }
}

function closeDownloadDialog() {  // 行注：定义 closeDownloadDialog 方法
  showDownloadModal.value = false  // 行注：更新 showDownloadModal 状态
}  // 行注：结束当前代码块

function updateCreateGroupName(value: string) {  // 行注：定义 updateCreateGroupName 方法
  createGroupForm.groupName = value  // 行注：更新 createGroupForm.groupName 值
}  // 行注：结束当前代码块

function updateCreateGroupNotice(value: string) {  // 行注：定义 updateCreateGroupNotice 方法
  createGroupForm.notice = value  // 行注：更新 createGroupForm.notice 值
}  // 行注：结束当前代码块

function updateCreateGroupMemberIds(value: Array<string | number>) {  // 行注：定义 updateCreateGroupMemberIds 方法
  createGroupForm.memberIds = value  // 行注：更新 createGroupForm.memberIds 值
}  // 行注：结束当前代码块

function updateAddMembersSelection(value: Array<string | number>) {  // 行注：定义 updateAddMembersSelection 方法
  addMembersSelection.value = value  // 行注：更新 addMembersSelection 状态
}  // 行注：结束当前代码块

function updateAddMembersMessage(value: string) {  // 行注：定义 updateAddMembersMessage 方法
  addMembersMessage.value = value  // 行注：更新 addMembersMessage 状态
}  // 行注：结束当前代码块

function updateTransferOwnerSelection(value: string | number | null) {  // 行注：定义 updateTransferOwnerSelection 方法
  transferOwnerSelection.value = value  // 行注：更新 transferOwnerSelection 状态
}  // 行注：结束当前代码块

function updateMuteMinutesInput(value: string) {  // 行注：定义 updateMuteMinutesInput 方法
  muteMinutesInput.value = value  // 行注：更新 muteMinutesInput 状态
}  // 行注：结束当前代码块

function updateGroupProfileName(value: string) {  // 行注：定义 updateGroupProfileName 方法
  groupProfileDraft.groupName = value  // 行注：更新 groupProfileDraft.groupName 值
}  // 行注：结束当前代码块

function updateNoticeDraft(value: string) {  // 行注：定义 updateNoticeDraft 方法
  noticeDraft.value = value  // 行注：更新 noticeDraft 状态
}  // 行注：结束当前代码块

/** 仅同步 visible；取消/确认由 ConfirmDialog 的 cancel/confirm 事件处理，勿在关窗时误调 cancel。 */
function setConfirmDialogVisible(value: boolean) {
  confirmDialog.visible = value
}

function setShowNoticeReminder(value: boolean) {  // 行注：定义 setShowNoticeReminder 方法
  showNoticeReminder.value = value  // 行注：更新 showNoticeReminder 状态
}  // 行注：结束当前代码块

// 复制群号用于邀请或排障，失败时给出明确提示。
async function copyGroupId(groupId: string | number) {  // 行注：定义异步 copyGroupId 方法
  try {  // 行注：尝试执行可能失败的逻辑
    await navigator.clipboard.writeText(String(groupId))  // 行注：写入剪贴板
    message.success(`群号 ${groupId} 已复制`)  // 行注：提示成功信息
  } catch {  // 行注：捕获并处理异常
    message.error('复制群号失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

// 全局点击事件统一处理菜单、表情框、@面板和右键菜单的关闭逻辑。
function handleClickOutside(event: MouseEvent) {  // 行注：定义 handleClickOutside 方法
  const target = event.target as Node  // 行注：初始化 target 变量
  if (showMenu.value && menuRef.value && menuBtnRef.value && !menuRef.value.contains(target) && !menuBtnRef.value.contains(target)) {  // 行注：判断当前条件是否成立
    showMenu.value = false  // 行注：更新 showMenu 状态
  }  // 行注：结束当前代码块
  if (showEmojiPicker.value && emojiRef.value && !emojiRef.value.contains(target)) {  // 行注：判断当前条件是否成立
    showEmojiPicker.value = false  // 行注：更新 showEmojiPicker 状态
  }  // 行注：结束当前代码块
  if (showMentionMenu.value && mentionMenuRef.value && !mentionMenuRef.value.contains(target) && !textareaRef.value?.contains(target)) {  // 行注：判断当前条件是否成立
    closeMentionMenu()  // 行注：调用 closeMentionMenu 方法
  }  // 行注：结束当前代码块
  if (
    showMsgContextMenu.value &&
    msgContextMenuRef.value &&
    !msgContextMenuRef.value.contains(target)
  ) {
    showMsgContextMenu.value = false
  }
}  // 行注：结束当前代码块

// 监听全局快捷键，目前主要用于 ESC 关闭图片预览。
function handleWindowKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && showImagePreview.value) {
    closeImagePreview()
  }
}

function handleComposerKeydown(event: KeyboardEvent) {
  handleInputKeydown(event)
}

const CLIPBOARD_IMAGE_PREF_KEY = 'linkx.clipboardImagePaste'

function isClipboardImagePasteEnabled() {
  return localStorage.getItem(CLIPBOARD_IMAGE_PREF_KEY) !== '0'
}

async function handleComposerPaste(event: ClipboardEvent) {
  if (!currentTargetId.value || currentMuted.value || !isClipboardImagePasteEnabled()) {
    return
  }
  const items = event.clipboardData?.items
  if (!items?.length) {
    return
  }
  for (const item of items) {
    if (!item.type.startsWith('image/')) {
      continue
    }
    const blob = item.getAsFile()
    if (!blob) {
      continue
    }
    event.preventDefault()
    const ext = item.type.split('/')[1]?.replace('jpeg', 'jpg') || 'png'
    const file = new File([blob], `paste-${Date.now()}.${ext}`, { type: item.type })
    await sendFileFromComposer(file)
    return
  }
  const api = getElectronAPI()
  if (!api?.readClipboardImage) {
    return
  }
  try {
    const payload = await api.readClipboardImage()
    if (!payload?.data) {
      return
    }
    event.preventDefault()
    const file = base64PayloadToFile({
      name: `paste-${Date.now()}.png`,
      mimeType: payload.mimeType || 'image/png',
      size: payload.size,
      data: payload.data
    })
    await sendFileFromComposer(file)
  } catch {
    /* ignore */
  }
}

async function openMessageExternalLink(href: string) {
  try {
    await openSafeExternalUrl(href)
  } catch (error: unknown) {
    const err = error as { message?: string }
    message.error(err.message || '无法打开链接')
  }
}

let teardownFileDrop: (() => void) | null = null

watch(() => route.fullPath, () => {  // 行注：监听状态变化
  void initializeRouteSession()  // 行注：调用 initializeRouteSession 方法
})  // 行注：结束当前调用配置

watch(
  () => [currentTargetId.value, currentSessionType.value] as const,
  async ([nextId, nextType], prev) => {
    const prevId = prev?.[0]
    const prevType = prev?.[1]
    if (prevId && prevType != null && (prevId !== nextId || prevType !== nextType)) {
      const prevKey = buildSessionKey(prevId, prevType)
      if (previousSessionKey.value === prevKey) {
        await flushDraftForSession(prevId, prevType, inputMessage.value)
      }
    }
    if (nextId && nextType != null) {
      previousSessionKey.value = buildSessionKey(nextId, nextType)
      restoreInputFromDraft(nextId, nextType)
    } else {
      previousSessionKey.value = null
    }
  }
)

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('keydown', handleWindowKeydown)
  teardownFileDrop = setupFileDrop(files => {
    if (!currentTargetId.value) {
      message.warning('请先选择一个会话再发送文件')
      return
    }
    void handleDroppedFiles(files)
  })
  connectChatSocket()
  loadingSessions.value = true  // 行注：更新 loadingSessions 状态
  try {  // 行注：尝试执行可能失败的逻辑
    await chatDraftStore.hydrateFromServer()
    await Promise.all([loadFriends(), loadSessions()])  // 行注：并行执行多项异步任务
    initialized.value = true  // 行注：更新 initialized 状态
    await initializeRouteSession()  // 行注：调用 initializeRouteSession 方法
  } finally {  // 行注：执行收尾清理逻辑
    loadingSessions.value = false  // 行注：更新 loadingSessions 状态
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置

onUnmounted(() => {
  if (currentTargetId.value) {
    void flushDraftForSession(
      currentTargetId.value,
      currentSessionType.value,
      inputMessage.value
    )
  }
  chatDraftStore.reset()
  teardownFileDrop?.()
  teardownFileDrop = null
  clearActiveJumpHighlight()
  disconnectChatSocket()  // 行注：调用 disconnectChatSocket 方法
  cleanupMessageResources()  // 行注：调用 cleanupMessageResources 方法
  resetCreateGroupForm()  // 行注：调用 resetCreateGroupForm 方法
  resetGroupProfileDraft()  // 行注：调用 resetGroupProfileDraft 方法
  chatStore.resetChatState()  // 行注：调用 resetChatState 方法
  groupDetail.value = null  // 行注：更新 groupDetail 状态
  showGroupDrawer.value = false  // 行注：更新 showGroupDrawer 状态
  noticeDraft.value = ''  // 行注：更新 noticeDraft 状态
  document.removeEventListener('click', handleClickOutside)  // 行注：移除点击事件监听
  window.removeEventListener('keydown', handleWindowKeydown)  // 行注：移除键盘按下事件监听
})  // 行注：结束当前调用配置
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.content-area {  /* 行注：定义 .content-area 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background: var(--linkx-bg-card);
}

.chat-panel-row {
  flex: 1;
  display: flex;
  min-height: 0;
  min-width: 0;
}

.chat-main-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.chat-header {  /* 行注：定义 .chat-header 样式 */
  height: 72px;  /* 行注：设置 height 样式 */
  padding: 0 20px;  /* 行注：设置 padding 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.chat-header-info,  /* 行注：补充 .chat-header-info 选择器 */
.chat-name-row,  /* 行注：补充 .chat-name-row 选择器 */
.chat-header-actions,  /* 行注：补充 .chat-header-actions 选择器 */
.dropdown-item,  /* 行注：补充 .dropdown-item 选择器 */
.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.chat-header-info {  /* 行注：定义 .chat-header-info 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.chat-avatar {  /* 行注：定义 .chat-avatar 样式 */
  width: 42px;  /* 行注：设置 width 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: #fff;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.chat-avatar.group {  /* 行注：定义 .chat-avatar.group 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.chat-meta {  /* 行注：定义 .chat-meta 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.chat-name-row {
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}

.chat-name {
  color: var(--linkx-text);
  font-size: clamp(15px, 2.5vw, 18px);
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.chat-session-tag {  /* 行注：定义 .chat-session-tag 样式 */
  padding: 2px 8px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  background: rgba(77, 107, 255, 0.12);  /* 行注：设置 background 样式 */
  color: #90a7ff;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.chat-subtitle {
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  word-break: break-word;
}

.header-menu-anchor {
  position: relative;
}

.header-dropdown {
  right: 0;
  left: auto;
  min-width: 140px;
}

.chat-header-actions {  /* 行注：定义 .chat-header-actions 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.header-action-btn {  /* 行注：定义 .header-action-btn 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.header-action-btn:hover,  /* 行注：补充 .header-action-btn:hover 选择器 */
.dropdown-item:hover,  /* 行注：补充 .dropdown-item:hover 选择器 */
.msg-context-item:hover:not(:disabled) {  /* 行注：定义 .msg-context-item:hover:not(:disabled) 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.dropdown-menu,  /* 行注：补充 .dropdown-menu 选择器 */
.msg-context-menu {  /* 行注：定义 .msg-context-menu 样式 */
  z-index: 10050;
  min-width: 160px;  /* 行注：设置 min-width 样式 */
  padding: 8px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.dropdown-menu {  /* 行注：定义 .dropdown-menu 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  top: calc(100% + 8px);  /* 行注：设置 top 样式 */
  right: 0;  /* 行注：设置 right 样式 */
}  /* 行注：结束当前样式块 */

.msg-context-menu {  /* 行注：定义 .msg-context-menu 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
}  /* 行注：结束当前样式块 */

.dropdown-item,  /* 行注：补充 .dropdown-item 选择器 */
.msg-context-item {  /* 行注：定义 .msg-context-item 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  padding: 10px 12px;  /* 行注：设置 padding 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font: inherit;  /* 行注：设置 font 样式 */
  text-align: left;  /* 行注：设置 text-align 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.msg-context-item:disabled {  /* 行注：定义 .msg-context-item:disabled 样式 */
  opacity: 0.45;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.chat-state-bar {  /* 行注：定义 .chat-state-bar 样式 */
  padding: 10px 20px;  /* 行注：设置 padding 样式 */
  border-bottom: 1px solid rgba(255, 170, 0, 0.2);  /* 行注：设置 border-bottom 样式 */
  background: color-mix(in srgb, rgba(255, 170, 0, 0.14) 70%, var(--linkx-bg-card));  /* 行注：设置 background 样式 */
  color: #d08b10;  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.chat-messages {  /* 行注：定义 .chat-messages 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  padding: 18px 20px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.placeholder-icon {  /* 行注：定义 .placeholder-icon 样式 */
  opacity: 0.24;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.placeholder-title {  /* 行注：定义 .placeholder-title 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.placeholder-text {  /* 行注：定义 .placeholder-text 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */
</style>
