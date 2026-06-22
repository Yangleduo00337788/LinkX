<template>
  <div class="content-area">
    <SessionSidebar
      :sessions="sessions"
      :loading-sessions="loadingSessions"
      :current-target-id="currentTargetId"
      :current-session-type="currentSessionType"
      :search-text="searchText"
      :flash-session-key="flashSessionKey"
      @create-group="openCreateGroupModal"
      @select-session="selectSession"
      @update:search-text="updateSearchText"
    />

    <div class="chat-panel">
      <template v-if="currentTargetId">
        <div class="chat-header">
          <div class="chat-header-info">
            <div class="chat-avatar" :class="{ group: isGroupSession }">
              <img v-if="currentSessionAvatar" :src="currentSessionAvatar" class="avatar-img" />
              <span v-else>{{ currentSessionName?.charAt(0) || (isGroupSession ? '群' : '聊') }}</span>
            </div>
            <div class="chat-meta">
              <div class="chat-name-row">
                <span class="chat-name">{{ currentSessionName }}</span>
                <span v-if="isGroupSession" class="chat-session-tag">群聊</span>
              </div>
              <span class="chat-subtitle">
                <template v-if="isGroupSession">
                  {{ currentMemberCount }} 位成员
                  <template v-if="currentNotice"> · 公告：{{ currentNotice }}</template>
                </template>
                <template v-else>
                  {{ currentSession?.targetOnline ? '在线' : '离线' }}
                  <template v-if="!wsConnected"> · 实时连接重连中{{ wsReconnectAttempt ? `（第 ${wsReconnectAttempt} 次）` : '' }}</template>
                </template>
              </span>
            </div>
          </div>

          <div class="chat-header-actions">
            <button v-if="isGroupSession" class="header-action-btn" title="群设置" @click="openGroupDrawer">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 1v4" />
                <path d="M12 19v4" />
                <path d="M4.22 4.22l2.83 2.83" />
                <path d="M16.95 16.95l2.83 2.83" />
                <path d="M1 12h4" />
                <path d="M19 12h4" />
                <path d="M4.22 19.78l2.83-2.83" />
                <path d="M16.95 7.05l2.83-2.83" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            </button>

            <button ref="menuBtnRef" class="header-action-btn" title="更多" @click="showMenu = !showMenu">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="1" />
                <circle cx="19" cy="12" r="1" />
                <circle cx="5" cy="12" r="1" />
              </svg>
            </button>

            <div v-if="showMenu" ref="menuRef" class="dropdown-menu">
              <div class="dropdown-item" @click="refreshCurrentSession">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
                刷新消息
              </div>
              <div v-if="isGroupSession" class="dropdown-item" @click="openGroupDrawer">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="8.5" cy="7" r="4" />
                  <path d="M20 8v6" />
                  <path d="M17 11h6" />
                </svg>
                群管理
              </div>
            </div>
          </div>
        </div>

        <div v-if="isGroupSession && currentMuted" class="chat-state-bar">
          你已被禁言{{ currentMuteTimeText ? `，截止 ${currentMuteTimeText}` : '' }}，暂时无法发送消息。
        </div>

        <div ref="messagesRef" class="chat-messages" @scroll="checkIfAtBottom">
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
          />
        </div>

        <div
          v-if="showMsgContextMenu"
          class="msg-context-menu"
          :style="{ left: `${msgMenuX}px`, top: `${msgMenuY}px` }"
        >
          <button class="msg-context-item" @click="copySelectedMessage">复制</button>
          <button class="msg-context-item" :disabled="!canRecallMessage(selectedMsg)" @click="handleRecallMessage">撤回</button>
        </div>

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
          @input-keydown="handleInputKeydown"
          @input-change="handleInputChange"
          @sync-mention-menu="syncMentionMenu"
          @send="sendMessage"
          @select-mention-candidate="selectMentionCandidate"
        />
      </template>

      <div v-else class="panel-placeholder">
        <div class="placeholder-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
          </svg>
        </div>
        <div class="placeholder-title">选择一个会话开始聊天</div>
        <div class="placeholder-text">支持单聊、群聊与实时消息同步</div>
      </div>
    </div>
  </div>

  <input ref="fileInputRef" type="file" style="display: none" @change="handleFileUpload" />
  <input ref="imageInputRef" type="file" accept="image/*" style="display: none" @change="handleImageUpload" />
  <input ref="groupAvatarInputRef" type="file" accept="image/*" style="display: none" @change="handleGroupAvatarSelected" />

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
    :show-group-drawer="showGroupDrawer"
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
    @close-group-drawer="closeGroupDrawer"
    @trigger-group-profile-avatar-upload="triggerGroupProfileAvatarUpload"
    @update:group-profile-name="updateGroupProfileName"
    @update:notice-draft="updateNoticeDraft"
    @save-group-profile="submitUpdateGroupProfile"
    @copy-group-id="copyGroupId"
    @save-notice="submitUpdateNotice"
    @open-group-members-page="openGroupMembersPage"
    @open-add-members-modal="openAddMembersModal"
    @toggle-admin-role="toggleAdminRole"
    @toggle-mute-member="toggleMuteMember"
    @remove-member="handleRemoveMember"
    @dissolve-group="handleDissolveGroup"
    @open-transfer-owner-modal="openTransferOwnerModal"
    @leave-group="handleLeaveGroup"
    @update:confirm-dialog-visible="setConfirmDialogVisible"
    @cancel-confirm-dialog="cancelConfirmDialog"
    @confirm-confirm-dialog="confirmConfirmDialog"
    @update:show-notice-reminder="setShowNoticeReminder"
    @acknowledge-notice-reminder="acknowledgeNoticeReminder"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useMessage } from 'naive-ui'
import SessionSidebar from '../components/chat/SessionSidebar.vue'
import MessagePane from '../components/chat/MessagePane.vue'
import MessageComposer from '../components/chat/MessageComposer.vue'
import ChatDialogs from '../components/chat/ChatDialogs.vue'
import { useChatRuntime } from '../hooks/useChatRuntime'
import { useGroupManagement } from '../hooks/useGroupManagement'
import { useChatComposer } from '../hooks/useChatComposer'
import { useChatMessageActions } from '../hooks/useChatMessageActions'
import { useConfirmDialog } from '../hooks/useConfirmDialog'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import {
  GROUP_ROLE_ADMIN,
  GROUP_ROLE_MEMBER,
  GROUP_ROLE_OWNER,
  SESSION_TYPE_GROUP,
  type ChatSession,
  type GroupDetail
} from '../types/chat'
import { buildSessionKey, formatDateTime } from '../utils/chat'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const chatStore = useChatStore()
const { sessions, messages, friends, currentTargetId, currentSessionType, searchText } = storeToRefs(chatStore)
const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()

const groupDetail = ref<GroupDetail | null>(null)
const showGroupDrawer = ref(false)
const noticeDraft = ref('')
const groupProfileDraft = reactive({
  groupName: '',
  avatarPreview: '',
  avatarFile: null as File | null
})

const loadingMessages = ref(false)
const loadingSessions = ref(false)
const showMenu = ref(false)
const messagesRef = ref<HTMLElement>()
const groupAvatarInputRef = ref<HTMLInputElement>()
const menuRef = ref<HTMLElement>()
const menuBtnRef = ref<HTMLElement>()
const groupProfileDirty = ref(false)
const groupNoticeDirty = ref(false)

const currentSession = computed(() => {
  if (!currentTargetId.value) {
    return null
  }
  return sessions.value.find(session => {
    return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(currentTargetId.value || '', currentSessionType.value)
  }) || null
})

const isGroupSession = computed(() => currentSessionType.value === SESSION_TYPE_GROUP)
const currentSessionName = computed(() => groupDetail.value?.groupRemark || groupDetail.value?.groupName || currentSession.value?.targetNickname || '')
const currentSessionAvatar = computed(() => groupDetail.value?.groupAvatar || currentSession.value?.targetAvatar || '')
const currentMemberCount = computed(() => groupDetail.value?.memberCount || currentSession.value?.memberCount || 0)
const currentNotice = computed(() => groupDetail.value?.notice || currentSession.value?.notice || '')
const currentGroupRole = computed(() => groupDetail.value?.myRole ?? currentSession.value?.myRole ?? GROUP_ROLE_MEMBER)
const currentMuted = computed(() => Boolean(groupDetail.value?.muted ?? currentSession.value?.muted))
const currentMuteTimeText = computed(() => formatDateTime(groupDetail.value?.muteTime || currentSession.value?.muteTime || ''))
const currentNotificationMuted = computed(() => Boolean(groupDetail.value?.notificationMuted ?? currentSession.value?.notificationMuted))
const canManageMembers = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditGroupProfile = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditNotice = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canMentionAll = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canDissolveGroup = computed(() => isGroupSession.value && currentGroupRole.value === GROUP_ROLE_OWNER)
const isGroupOwner = computed(() => isGroupSession.value && currentGroupRole.value === GROUP_ROLE_OWNER)
const currentUserId = computed(() => String(userStore.userId || ''))
const groupNoticeUpdateTimeText = computed(() => formatDateTime(groupDetail.value?.noticeUpdateTime || ''))

let syncGroupProfileDraftImpl: (detail?: GroupDetail | null) => void = () => undefined
let closeGroupDrawerImpl: (options?: { force?: boolean }) => Promise<boolean> | boolean = () => true
let sendMessageImpl: () => Promise<void> = async () => undefined

const runtime = useChatRuntime({
  route,
  router,
  message,
  sessions,
  messages,
  friends,
  currentTargetId,
  currentSessionType,
  groupDetail,
  showGroupDrawer,
  noticeDraft,
  isGroupSession,
  currentSessionName,
  currentNotificationMuted,
  isGroupProfileChanged: computed(() => groupProfileDirty.value),
  isGroupNoticeChanged: computed(() => groupNoticeDirty.value),
  messagesRef,
  loadingSessions,
  loadingMessages,
  showMenu,
  syncGroupProfileDraft: detail => syncGroupProfileDraftImpl(detail),
  closeGroupDrawer: options => closeGroupDrawerImpl(options),
  getToken: () => userStore.token,
  getCurrentUserId: () => userStore.userId,
  getCurrentUserNickname: () => userStore.nickname,
  getCurrentUserAvatar: () => userStore.avatar,
  onResetConversationState: () => {
    chatStore.resetConversation()
  }
})

const {
  initialized,
  flashSessionKey,
  showNoticeReminder,
  acknowledgingNoticeReminder,
  activeJumpMessageKey,
  showMentionBanner,
  mentionBannerText,
  mentionBannerActionText,
  wsConnected,
  wsReconnectAttempt,
  connectChatSocket,
  disconnectChatSocket,
  loadFriends,
  loadSessions,
  selectSession,
  initializeRouteSession,
  refreshCurrentSession,
  acknowledgeNoticeReminder,
  dismissMentionBanner,
  handleMentionBannerClick,
  checkIfAtBottom,
  handleMessageMediaLoad,
  getMessageTextSegments,
  getResolvedMessageFileUrl,
  ensureMessageFileAccessUrl,
  toDisplayMessage,
  upsertSession,
  upsertMessage,
  applyGroupDetail,
  removeSessionByTarget,
  resetCurrentConversationState,
  sendChatCommand,
  loadGroupDetail,
  cleanupMessageResources,
  clearActiveJumpHighlight,
  scrollMessagesToBottom
} = runtime

const groupManagement = useGroupManagement({
  friends,
  sessions,
  currentTargetId,
  currentGroupRole,
  currentSessionName,
  isGroupSession,
  canManageMembers,
  canEditGroupProfile,
  canEditNotice,
  groupDetail,
  showGroupDrawer,
  noticeDraft,
  groupProfileDraft,
  groupAvatarInputRef,
  getCurrentUserId: () => userStore.userId,
  message,
  router,
  openConfirmDialog,
  loadSessions: () => loadSessions(),
  loadGroupDetail: (groupId, syncDraft) => loadGroupDetail(groupId, syncDraft),
  selectSession: (session: ChatSession, syncRoute = true) => selectSession(session, syncRoute),
  upsertSession,
  applyGroupDetail,
  removeSessionByTarget,
  resetCurrentConversationState,
  showMenu
})

syncGroupProfileDraftImpl = groupManagement.syncGroupProfileDraft
closeGroupDrawerImpl = groupManagement.closeGroupDrawer

const {
  showCreateGroupModal,
  showAddMembersModal,
  creatingGroup,
  addMembersSelection,
  addMembersMessage,
  addingMembers,
  updatingNotice,
  updatingGroupProfile,
  showTransferOwnerModal,
  transferOwnerSelection,
  transferringOwner,
  showMuteMemberModal,
  muteTargetMember,
  muteMinutesInput,
  mutingMember,
  createGroupForm,
  isGroupProfileChanged,
  isGroupNoticeChanged,
  availableFriendsForCurrentGroup,
  resetCreateGroupForm,
  resetGroupProfileDraft,
  openCreateGroupModal,
  closeCreateGroupModal,
  triggerGroupAvatarUpload,
  triggerGroupProfileAvatarUpload,
  handleGroupAvatarSelected,
  submitCreateGroup,
  openGroupDrawer,
  closeGroupDrawer,
  openGroupMembersPage,
  openAddMembersModal,
  closeAddMembersModal,
  submitAddMembers,
  submitUpdateNotice,
  submitUpdateGroupProfile,
  canOperateMember,
  canToggleAdmin,
  toggleAdminRole,
  toggleMuteMember,
  closeMuteMemberModal,
  submitMuteMember,
  handleRemoveMember,
  handleDissolveGroup,
  handleLeaveGroup,
  openTransferOwnerModal,
  closeTransferOwnerModal,
  submitTransferOwner
} = groupManagement

watch(isGroupProfileChanged, value => {
  groupProfileDirty.value = value
}, { immediate: true })

watch(isGroupNoticeChanged, value => {
  groupNoticeDirty.value = value
}, { immediate: true })

const composer = useChatComposer({
  currentMuted,
  isGroupSession,
  canMentionAll,
  groupDetail,
  getCurrentUserId: () => userStore.userId,
  onSend: () => sendMessageImpl()
})

const {
  inputMessage,
  showEmojiPicker,
  showMentionMenu,
  mentionHighlightedIndex,
  mentionCandidates,
  textareaRef,
  fileInputRef,
  imageInputRef,
  emojiRef,
  mentionMenuRef,
  setTextareaRef,
  setEmojiRef,
  setMentionMenuRef,
  closeMentionMenu,
  syncMentionMenu,
  handleInputChange,
  handleInputKeydown,
  selectMentionCandidate,
  insertEmoji,
  triggerFileUpload,
  triggerImageUpload,
  resolveOutgoingMentions
} = composer

void fileInputRef
void imageInputRef

const messageActions = useChatMessageActions({
  message,
  messages,
  currentTargetId,
  currentSessionType,
  currentMuted,
  inputMessage,
  textareaRef,
  closeMentionMenu,
  getCurrentUserId: () => userStore.userId,
  getCurrentUserNickname: () => userStore.nickname,
  getCurrentUserAvatar: () => userStore.avatar,
  sendChatCommand,
  toDisplayMessage,
  upsertMessage,
  scrollMessagesToBottom,
  ensureMessageFileAccessUrl,
  getResolvedMessageFileUrl
})

const {
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
} = messageActions

sendMessageImpl = async () => {
  await handleSend(resolveOutgoingMentions(inputMessage.value))
}

function sendMessage() {
  return sendMessageImpl()
}

const emojis = ['😀', '😂', '🤣', '😍', '🥰', '😘', '😎', '🤔', '😏', '😢', '😭', '😡', '🥳', '😱', '🥺', '😴', '🤗', '😈', '👻', '💀', '🤡', '👽', '🤖', '💩', '❤️', '🔥', '👍', '👎', '👏', '🙏', '💪', '🎉', '🎊', '☕', '🌹', '🍀', '🌈', '☀️', '🌙', '⭐']

function updateSearchText(value: string) {
  searchText.value = value
}

function updateInputMessage(value: string) {
  inputMessage.value = value
}

function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
}

async function copySelectedMessage() {
  await handleCopyMessage()
  showMsgContextMenu.value = false
}

function closeDownloadDialog() {
  showDownloadModal.value = false
}

function updateCreateGroupName(value: string) {
  createGroupForm.groupName = value
}

function updateCreateGroupNotice(value: string) {
  createGroupForm.notice = value
}

function updateCreateGroupMemberIds(value: Array<string | number>) {
  createGroupForm.memberIds = value
}

function updateAddMembersSelection(value: Array<string | number>) {
  addMembersSelection.value = value
}

function updateAddMembersMessage(value: string) {
  addMembersMessage.value = value
}

function updateTransferOwnerSelection(value: string | number | null) {
  transferOwnerSelection.value = value
}

function updateMuteMinutesInput(value: string) {
  muteMinutesInput.value = value
}

function updateGroupProfileName(value: string) {
  groupProfileDraft.groupName = value
}

function updateNoticeDraft(value: string) {
  noticeDraft.value = value
}

function setConfirmDialogVisible(value: boolean) {
  if (value) {
    confirmDialog.visible = true
    return
  }
  if (confirmDialog.visible) {
    cancelConfirmDialog()
  }
}

function setShowNoticeReminder(value: boolean) {
  showNoticeReminder.value = value
}

async function copyGroupId(groupId: string | number) {
  try {
    await navigator.clipboard.writeText(String(groupId))
    message.success(`群号 ${groupId} 已复制`)
  } catch {
    message.error('复制群号失败')
  }
}

function handleClickOutside(event: MouseEvent) {
  const target = event.target as Node
  if (showMenu.value && menuRef.value && menuBtnRef.value && !menuRef.value.contains(target) && !menuBtnRef.value.contains(target)) {
    showMenu.value = false
  }
  if (showEmojiPicker.value && emojiRef.value && !emojiRef.value.contains(target)) {
    showEmojiPicker.value = false
  }
  if (showMentionMenu.value && mentionMenuRef.value && !mentionMenuRef.value.contains(target) && !textareaRef.value?.contains(target)) {
    closeMentionMenu()
  }
  if (showMsgContextMenu.value) {
    showMsgContextMenu.value = false
  }
}

function handleWindowKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && showImagePreview.value) {
    closeImagePreview()
  }
}

watch(() => route.fullPath, () => {
  void initializeRouteSession()
})

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('keydown', handleWindowKeydown)
  connectChatSocket()
  loadingSessions.value = true
  try {
    await Promise.all([loadFriends(), loadSessions()])
    initialized.value = true
    await initializeRouteSession()
  } finally {
    loadingSessions.value = false
  }
})

onUnmounted(() => {
  clearActiveJumpHighlight()
  disconnectChatSocket()
  cleanupMessageResources()
  resetCreateGroupForm()
  resetGroupProfileDraft()
  chatStore.resetChatState()
  groupDetail.value = null
  showGroupDrawer.value = false
  noticeDraft.value = ''
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('keydown', handleWindowKeydown)
})
</script>

<style scoped>
.content-area {
  display: flex;
  height: 100%;
  min-width: 0;
  min-height: 0;
  background: var(--linkx-bg);
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background: var(--linkx-bg-card);
}

.chat-header {
  height: 72px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--linkx-border);
}

.chat-header-info,
.chat-name-row,
.chat-header-actions,
.dropdown-item,
.panel-placeholder {
  display: flex;
  align-items: center;
}

.chat-header-info {
  gap: 14px;
  min-width: 0;
}

.chat-avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--linkx-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: #fff;
  font-weight: 700;
  flex-shrink: 0;
}

.chat-avatar.group {
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.chat-name-row {
  gap: 8px;
}

.chat-name {
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
}

.chat-session-tag {
  padding: 2px 8px;
  border-radius: var(--linkx-radius-full);
  background: rgba(77, 107, 255, 0.12);
  color: #90a7ff;
  font-size: 12px;
  font-weight: 600;
}

.chat-subtitle {
  color: var(--linkx-text-muted);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-header-actions {
  position: relative;
  gap: 8px;
}

.header-action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: transparent;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.header-action-btn:hover,
.dropdown-item:hover,
.msg-context-item:hover:not(:disabled) {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.dropdown-menu,
.msg-context-menu {
  z-index: 1200;
  min-width: 160px;
  padding: 8px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg-card);
  box-shadow: var(--linkx-shadow-lg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
}

.msg-context-menu {
  position: fixed;
}

.dropdown-item,
.msg-context-item {
  width: 100%;
  gap: 10px;
  padding: 10px 12px;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: transparent;
  color: var(--linkx-text-secondary);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.msg-context-item:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.chat-state-bar {
  padding: 10px 20px;
  border-bottom: 1px solid rgba(255, 170, 0, 0.2);
  background: color-mix(in srgb, rgba(255, 170, 0, 0.14) 70%, var(--linkx-bg-card));
  color: #d08b10;
  font-size: 13px;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px 20px;
}

.panel-placeholder {
  flex: 1;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  color: var(--linkx-text-muted);
}

.placeholder-icon {
  opacity: 0.24;
}

.placeholder-title {
  color: var(--linkx-text-secondary);
  font-size: 18px;
  font-weight: 600;
}

.placeholder-text {
  color: var(--linkx-text-muted);
  font-size: 13px;
}
</style>
