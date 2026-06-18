<template>
  <div class="content-area">
    <div class="session-panel">
      <div class="panel-header">
        <span class="header-title">消息</span>
        <button class="create-group-btn" @click="openCreateGroupModal">建群</button>
      </div>

      <div class="session-search">
        <div class="search-input-wrapper">
          <n-icon :component="SearchOutline" class="search-icon" />
          <input
            v-model="searchText"
            type="text"
            placeholder="搜索会话..."
            class="search-input"
          />
          <div v-if="searchText" class="search-clear" @click="searchText = ''">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.2" />
              <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
          </div>
        </div>
      </div>

      <div class="session-list">
        <div
          v-for="session in filteredSessions"
          :key="buildSessionKey(session.targetId, session.sessionType)"
          class="session-item"
          :class="{ active: isCurrentSession(session), flash: flashSessionKey === buildSessionKey(session.targetId, session.sessionType) }"
          @click="selectSession(session)"
        >
          <div class="session-avatar" :class="{ 'has-unread': session.unreadCount > 0, group: session.sessionType === SESSION_TYPE_GROUP }">
            <img v-if="session.targetAvatar" :src="session.targetAvatar" class="avatar-img" />
            <span v-else class="avatar-text">{{ session.targetNickname?.charAt(0) || (session.sessionType === SESSION_TYPE_GROUP ? '群' : '聊') }}</span>
            <div class="session-type-badge" :class="{ group: session.sessionType === SESSION_TYPE_GROUP }">
              {{ session.sessionType === SESSION_TYPE_GROUP ? '群' : '单' }}
            </div>
            <div v-if="session.sessionType === SESSION_TYPE_SINGLE" class="online-indicator" :class="{ active: session.targetOnline }"></div>
            <div v-if="session.unreadCount > 0" class="unread-badge">
              {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
            </div>
          </div>
          <div class="session-info">
            <div class="session-header">
              <div class="session-title-row">
                <span class="session-name">{{ session.targetNickname }}</span>
                <span v-if="session.sessionType === SESSION_TYPE_GROUP" class="session-tag">{{ session.memberCount || 0 }}人</span>
                <span v-if="session.sessionType === SESSION_TYPE_GROUP && session.noticeUnread" class="session-tag notice">新公告</span>
              </div>
              <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
            </div>
            <div class="session-preview-row">
              <span class="session-preview">{{ session.lastMessage || (session.sessionType === SESSION_TYPE_GROUP ? '群聊已创建' : '暂无消息') }}</span>
              <span v-if="session.sessionType === SESSION_TYPE_GROUP && session.muted" class="session-muted">已禁言</span>
            </div>
          </div>
        </div>

        <div v-if="sessions.length === 0 && !loadingSessions" class="empty-state">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
          </div>
          <div class="empty-title">暂无会话</div>
          <div class="empty-subtitle">创建群聊或找好友开始聊天</div>
        </div>
      </div>
    </div>

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
            <button class="header-action-btn" title="更多" @click="showMenu = !showMenu" ref="menuBtnRef">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="1" />
                <circle cx="19" cy="12" r="1" />
                <circle cx="5" cy="12" r="1" />
              </svg>
            </button>
            <div v-if="showMenu" class="dropdown-menu" ref="menuRef">
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

        <div class="chat-messages" ref="messagesRef" @scroll="checkIfAtBottom">
          <div v-if="loadingMessages" class="chat-loading">
            <div class="loading-spinner"></div>
            <span>加载中...</span>
          </div>

          <template v-else>
            <div
              v-if="showMentionBanner"
              role="button"
              tabindex="0"
              class="mention-banner"
              @click="handleMentionBannerClick()"
              @keydown.enter.prevent="handleMentionBannerClick()"
              @keydown.space.prevent="handleMentionBannerClick()"
            >
              <span class="mention-banner-badge">特别提醒</span>
              <span class="mention-banner-text">{{ mentionBannerText }}</span>
              <span class="mention-banner-action">{{ mentionBannerActionText }}</span>
              <span class="mention-banner-close" @click.stop="dismissMentionBanner">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12" />
                </svg>
              </span>
            </div>
            <div
              v-for="(msg, index) in messages"
              :key="msg.id || index"
              class="message-row"
              :class="{ self: msg.isMe, system: msg.isSystem, located: activeJumpMessageKey === getMessageAnchorKey(msg) }"
              :data-message-key="getMessageAnchorKey(msg)"
            >
              <template v-if="msg.isSystem">
                <div class="system-message">{{ msg.content }}</div>
              </template>
              <template v-else>
              <div class="msg-avatar" :class="{ self: msg.isMe, group: isGroupSession && !msg.isMe }">
                <img v-if="msg.isMe ? userStore.avatar : msg.fromAvatar" :src="msg.isMe ? userStore.avatar : msg.fromAvatar" class="avatar-img" />
                <span v-else>{{ msg.isMe ? (userStore.nickname?.charAt(0) || '我') : (msg.name?.charAt(0) || '群') }}</span>
              </div>
              <div class="msg-content">
                <div v-if="isGroupSession && !msg.isMe" class="msg-sender">{{ msg.name }}</div>
                <div
                  class="msg-bubble"
                  :class="{ self: msg.isMe, recalled: msg.status === MESSAGE_STATUS_RECALLED }"
                  @contextmenu.prevent="showMsgMenu($event, msg)"
                >
                  <template v-if="msg.status === MESSAGE_STATUS_RECALLED">
                    <span class="recalled-text">[消息已撤回]</span>
                  </template>
                  <template v-else-if="msg.msgType === MESSAGE_TYPE_IMAGE">
                    <img :src="getResolvedMessageFileUrl(msg)" class="msg-image" @load="handleMessageMediaLoad" @click="previewImage(msg)" />
                  </template>
                  <template v-else-if="msg.msgType === MESSAGE_TYPE_FILE">
                    <button
                      v-if="msg.deliveryStatus !== 'sent'"
                      type="button"
                      class="msg-file pending"
                      :disabled="msg.deliveryStatus === 'sending'"
                    >
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                        <polyline points="14 2 14 8 20 8" />
                      </svg>
                      <div class="file-info">
                        <span class="file-name">{{ msg.fileName || getFileName(msg.content) }}</span>
                        <span class="file-size">{{ getFileSizeText(msg) || '待发送文件' }}</span>
                      </div>
                    </button>
                    <a v-else href="#" class="msg-file" @click.prevent="downloadFile(msg)">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                        <polyline points="14 2 14 8 20 8" />
                      </svg>
                      <div class="file-info">
                        <span class="file-name">{{ msg.fileName || getFileName(msg.content) }}</span>
                        <span class="file-size">{{ getFileSizeText(msg) || '未知大小' }}</span>
                      </div>
                    </a>
                  </template>
                  <template v-else>
                    <span
                      v-for="(segment, segmentIndex) in getMessageTextSegments(msg.content)"
                      :key="`${getMessageAnchorKey(msg)}-${segmentIndex}`"
                      :class="{ 'mention-text': segment.mention }"
                    >
                      {{ segment.text }}
                    </span>
                  </template>
                </div>
                <div class="msg-meta" :class="{ self: msg.isMe }">
                  <span class="msg-time">{{ msg.time }}</span>
                  <button
                    v-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && msg.deliveryStatus === 'failed'"
                    type="button"
                    class="msg-status retry"
                    @click="retryFailedMessage(msg)"
                  >
                    发送失败，点击重试
                  </button>
                  <span
                    v-else-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && msg.deliveryStatus === 'sending'"
                    class="msg-status pending"
                  >
                    发送中
                  </span>
                  <span v-else-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && !isGroupSession" class="msg-status" :class="{ read: msg.readStatus === '已读' }">
                    {{ msg.readStatus }}
                  </span>
                </div>
              </div>
              </template>
            </div>

            <div v-if="messages.length === 0" class="chat-empty">
              <div class="chat-empty-icon">
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
                </svg>
              </div>
              <div class="chat-empty-title">暂无消息</div>
              <div class="chat-empty-text">发送消息开始聊天吧</div>
            </div>
          </template>
        </div>

        <div
          v-if="showMsgContextMenu"
          class="msg-context-menu"
          :style="{ top: msgMenuY + 'px', left: msgMenuX + 'px' }"
          @click="showMsgContextMenu = false"
        >
          <div v-if="canRecallMessage(selectedMsg)" class="context-menu-item" @click="handleRecallMessage">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="1 4 1 10 7 10" />
              <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
            </svg>
            撤回
          </div>
          <div class="context-menu-item" @click="handleCopyMessage">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
            </svg>
            复制
          </div>
        </div>

        <div class="chat-input-area">
          <div class="input-toolbar">
            <div class="emoji-wrapper" ref="emojiRef">
              <button class="toolbar-btn" title="表情" @click="showEmojiPicker = !showEmojiPicker">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M8 14s1.5 2 4 2 4-2 4-2" />
                  <line x1="9" y1="9" x2="9.01" y2="9" />
                  <line x1="15" y1="9" x2="15.01" y2="9" />
                </svg>
              </button>
              <div v-if="showEmojiPicker" class="emoji-picker">
                <div class="emoji-grid">
                  <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
                </div>
              </div>
            </div>
            <button class="toolbar-btn" title="文件" :disabled="currentMuted" @click="triggerFileUpload">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </button>
            <button class="toolbar-btn" title="图片" :disabled="currentMuted" @click="triggerImageUpload">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <polyline points="21 15 16 10 5 21" />
              </svg>
            </button>
          </div>
          <div class="input-container" :class="{ disabled: currentMuted }">
            <textarea
              ref="textareaRef"
              v-model="inputMessage"
              :placeholder="currentMuted ? '你已被禁言，暂时无法发送消息' : '输入消息...'"
              class="message-input"
              rows="1"
              :disabled="currentMuted"
              @keydown="handleInputKeydown"
              @input="handleInputChange"
              @click="syncMentionMenu"
            ></textarea>
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || sending || currentMuted"
              :class="{ active: inputMessage.trim() && !currentMuted }"
              @click="handleSend"
            >
              <svg v-if="!sending" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
              <div v-else class="send-loading"></div>
            </button>
          </div>
          <div v-if="showMentionMenu" class="mention-menu" ref="mentionMenuRef">
            <div v-if="mentionCandidates.length > 0" class="mention-menu-list">
              <button
                v-for="(candidate, index) in mentionCandidates"
                :key="candidate.key"
                type="button"
                class="mention-menu-item"
                :class="{ active: index === mentionHighlightedIndex }"
                @mousedown.prevent="selectMentionCandidate(candidate)"
              >
                <span class="mention-menu-name">{{ candidate.label }}</span>
                <span class="mention-menu-meta">{{ candidate.meta }}</span>
              </button>
            </div>
            <div v-else class="mention-menu-empty">未找到可提及成员</div>
          </div>
        </div>
      </template>

      <div v-else class="chat-welcome">
        <div class="welcome-icon">
          <svg width="100" height="100" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.5">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
          </svg>
        </div>
        <div class="welcome-title">LinkX IM</div>
        <div class="welcome-text">选择一个会话或创建群聊开始沟通</div>
      </div>
    </div>
  </div>

  <input ref="fileInputRef" type="file" style="display: none" @change="handleFileUpload" />
  <input ref="imageInputRef" type="file" accept="image/*" style="display: none" @change="handleImageUpload" />
  <input ref="groupAvatarInputRef" type="file" accept="image/*" style="display: none" @change="handleGroupAvatarSelected" />

  <ChatMediaPreview
    :visible="showImagePreview"
    :image-url="previewImageUrl"
    @close="closeImagePreview"
  />

  <ChatDownloadDialog
    :visible="showDownloadModal"
    :file-name="downloadFileName"
    :file-size="downloadFileSize"
    :progress="downloadProgress"
    @close="showDownloadModal = false"
    @open="openDownloadedFile"
    @save="saveDownloadedFile"
  />

  <div v-if="showCreateGroupModal" class="overlay-panel" @click.self="closeCreateGroupModal">
    <div class="modal-card create-group-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">新建群聊</div>
          <div class="modal-subtitle">选择好友并填写群信息</div>
        </div>
        <button class="modal-close" @click="closeCreateGroupModal">×</button>
      </div>

      <div class="form-section">
        <label class="form-label">群头像</label>
        <div class="group-avatar-selector" @click="triggerGroupAvatarUpload">
          <img v-if="createGroupForm.avatarPreview" :src="createGroupForm.avatarPreview" class="group-avatar-preview" />
          <span v-else>{{ createGroupForm.groupName?.charAt(0) || '群' }}</span>
        </div>
      </div>

      <div class="form-section">
        <label class="form-label">群名称</label>
        <input v-model="createGroupForm.groupName" class="modal-input" placeholder="请输入群名称" maxlength="100" />
      </div>

      <div class="form-section">
        <label class="form-label">群公告</label>
        <textarea v-model="createGroupForm.notice" class="modal-textarea" rows="3" placeholder="选填，创建后可在群设置继续编辑"></textarea>
      </div>

      <div class="form-section">
        <div class="section-title-row">
          <label class="form-label">选择成员</label>
          <span class="section-count">已选 {{ createGroupForm.memberIds.length }} 人</span>
        </div>
        <div v-if="friends.length > 0" class="member-picker-list">
          <label v-for="friend in friends" :key="friend.friendId" class="member-picker-item">
            <input v-model="createGroupForm.memberIds" :value="friend.friendId" type="checkbox" />
            <div class="member-picker-avatar">
              <img v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
              <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
            </div>
            <div class="member-picker-info">
              <span class="member-picker-name">{{ friend.friendNickname }}</span>
              <span class="member-picker-meta">@{{ friend.friendUsername }}</span>
            </div>
          </label>
        </div>
        <div v-else class="panel-placeholder">暂无好友，请先添加好友再创建群聊。</div>
      </div>

      <div class="modal-actions">
        <button class="secondary-btn" @click="closeCreateGroupModal">取消</button>
        <button class="primary-btn" :disabled="creatingGroup" @click="submitCreateGroup">
          {{ creatingGroup ? '创建中...' : '创建群聊' }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="showAddMembersModal" class="overlay-panel" @click.self="closeAddMembersModal">
    <div class="modal-card add-members-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">邀请成员</div>
          <div class="modal-subtitle">向好友发送入群邀请，待对方确认后加入</div>
        </div>
        <button class="modal-close" @click="closeAddMembersModal">×</button>
      </div>

      <div v-if="availableFriendsForCurrentGroup.length > 0" class="member-picker-list compact">
        <label v-for="friend in availableFriendsForCurrentGroup" :key="friend.friendId" class="member-picker-item">
          <input v-model="addMembersSelection" :value="friend.friendId" type="checkbox" />
          <div class="member-picker-avatar">
            <img v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
            <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
          </div>
          <div class="member-picker-info">
            <span class="member-picker-name">{{ friend.friendNickname }}</span>
            <span class="member-picker-meta">@{{ friend.friendUsername }}</span>
          </div>
        </label>
      </div>
      <div v-else class="panel-placeholder">所有好友都已在群内，暂无可邀请成员。</div>

      <div class="form-section">
        <label class="form-label">邀请说明</label>
        <textarea
          v-model="addMembersMessage"
          class="modal-textarea"
          rows="3"
          maxlength="255"
          placeholder="选填，邀请说明会出现在对方的群通知中"
        ></textarea>
      </div>

      <div class="modal-actions">
        <button class="secondary-btn" @click="closeAddMembersModal">取消</button>
        <button class="primary-btn" :disabled="addingMembers" @click="submitAddMembers">
          {{ addingMembers ? '邀请中...' : '确认邀请' }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="showTransferOwnerModal" class="overlay-panel" @click.self="closeTransferOwnerModal">
    <div class="modal-card add-members-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">转让群主</div>
          <div class="modal-subtitle">选择一位成员成为新群主</div>
        </div>
        <button class="modal-close" @click="closeTransferOwnerModal">×</button>
      </div>

      <div v-if="groupDetail && groupDetail.members.length > 1" class="member-picker-list compact">
        <label v-for="member in groupDetail.members" :key="member.userId" class="member-picker-item">
          <input v-model="transferOwnerSelection" :value="member.userId" type="radio" name="transferOwner" />
          <div class="member-picker-avatar">
            <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
            <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
          </div>
          <div class="member-picker-info">
            <span class="member-picker-name">{{ member.nickname || member.username }}</span>
            <span class="member-picker-meta">@{{ member.username }} · {{ roleText(member.role) }}</span>
          </div>
        </label>
      </div>
      <div v-else class="panel-placeholder">群内没有其他成员可转让。</div>

      <div class="modal-actions">
        <button class="secondary-btn" @click="closeTransferOwnerModal">取消</button>
        <button class="primary-btn" :disabled="!transferOwnerSelection || transferringOwner" @click="submitTransferOwner">
          {{ transferringOwner ? '转让中...' : '确认转让' }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="showMuteMemberModal" class="overlay-panel" @click.self="closeMuteMemberModal">
    <div class="modal-card mute-member-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">设置禁言</div>
          <div class="modal-subtitle">为该成员设置禁言时长</div>
        </div>
        <button class="modal-close" @click="closeMuteMemberModal">×</button>
      </div>

      <div class="form-section">
        <label class="form-label">成员</label>
        <div class="readonly-field">
          {{ muteTargetMember?.nickname || muteTargetMember?.username || '未选择成员' }}
        </div>
      </div>

      <div class="form-section">
        <label class="form-label">禁言时长（分钟）</label>
        <input
          v-model="muteMinutesInput"
          type="number"
          min="1"
          max="43200"
          step="1"
          class="modal-input"
          placeholder="请输入 1 - 43200 之间的整数"
          @keyup.enter="submitMuteMember"
        />
      </div>

      <div class="modal-actions">
        <button class="secondary-btn" :disabled="mutingMember" @click="closeMuteMemberModal">取消</button>
        <button class="primary-btn" :disabled="mutingMember" @click="submitMuteMember">
          {{ mutingMember ? '提交中...' : '确认禁言' }}
        </button>
      </div>
    </div>
  </div>

  <div v-if="showGroupDrawer" class="drawer-overlay" @click.self="() => closeGroupDrawer()">
    <div class="group-drawer">
      <div class="drawer-header">
        <div>
          <div class="drawer-title">群设置</div>
          <div class="drawer-subtitle">管理群资料、成员和公告</div>
        </div>
        <button class="modal-close" @click="() => closeGroupDrawer()">×</button>
      </div>

      <template v-if="groupDetail">
        <div class="drawer-body">
          <div class="group-summary-card">
            <div
              class="group-summary-avatar"
              :class="{ editable: canEditGroupProfile }"
              @click="triggerGroupProfileAvatarUpload"
            >
              <img v-if="groupProfileDraft.avatarPreview || groupDetail.groupAvatar" :src="groupProfileDraft.avatarPreview || groupDetail.groupAvatar" class="avatar-img" />
              <span v-else>{{ groupProfileDraft.groupName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
              <div v-if="canEditGroupProfile" class="group-summary-avatar-mask">更换</div>
            </div>
            <div class="group-summary-info">
              <div class="group-profile-name-row">
                <div class="group-profile-name-shell">
                  <input
                    v-model="groupProfileDraft.groupName"
                    class="group-profile-name-input"
                    :disabled="!canEditGroupProfile || updatingGroupProfile"
                    maxlength="100"
                    placeholder="请输入群名称"
                  />
                  <div class="group-profile-hint">
                    {{ canEditGroupProfile ? '点击头像可更换群头像' : '仅群主和管理员可编辑群资料' }}
                  </div>
                </div>
                <button
                  v-if="canEditGroupProfile"
                  class="text-btn group-profile-save-btn"
                  :disabled="updatingGroupProfile || !isGroupProfileChanged"
                  :title="!isGroupProfileChanged ? '修改群名称或头像后才能保存' : '保存群资料'"
                  @click="submitUpdateGroupProfile"
                >
                  {{ updatingGroupProfile ? '保存中...' : '保存资料' }}
                </button>
              </div>
              <div class="group-summary-badges">
                <div class="group-summary-pill">{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</div>
                <div class="group-summary-pill role">我的角色：{{ roleText(groupDetail.myRole) }}</div>
              </div>
              <div class="group-summary-id-row">
                <span class="group-summary-id">群号：{{ groupDetail.id }}</span>
                <button class="text-btn group-id-copy-btn" @click="copyGroupId(groupDetail.id)">复制群号</button>
              </div>
            </div>
          </div>

          <div class="drawer-section">
            <div class="section-title-row">
              <span class="drawer-section-title">群公告</span>
              <button
                v-if="canEditNotice"
                class="text-btn"
                :disabled="updatingNotice || !isGroupNoticeChanged"
                :title="!isGroupNoticeChanged ? '修改群公告后才能保存' : '保存群公告'"
                @click="submitUpdateNotice"
              >
                {{ updatingNotice ? '保存中...' : '保存公告' }}
              </button>
            </div>
            <textarea
              v-model="noticeDraft"
              class="drawer-textarea"
              rows="4"
              :disabled="!canEditNotice"
              :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
            ></textarea>
            <div v-if="groupDetail.noticeUpdateTime" class="section-hint">最近更新：{{ formatDateTime(groupDetail.noticeUpdateTime) }}</div>
          </div>

          <div class="drawer-section">
            <div class="section-title-row">
              <span class="drawer-section-title">成员管理</span>
              <div class="section-actions">
                <button class="text-btn" @click="openGroupMembersPage">独立页查看</button>
                <button v-if="canManageMembers" class="text-btn" @click="openAddMembersModal">邀请进群</button>
              </div>
            </div>
            <div class="member-manage-list">
              <div v-for="member in groupDetail.members" :key="member.userId" class="member-row">
                <div class="member-main">
                  <div class="member-avatar">
                    <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                    <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
                  </div>
                  <div class="member-info">
                    <div class="member-name-row">
                      <span class="member-name">{{ member.nickname || member.username }}</span>
                      <div class="member-badges">
                        <span class="member-role-tag" :class="roleClass(member.role)">{{ roleText(member.role) }}</span>
                        <span v-if="String(member.userId) === String(userStore.userId)" class="member-self-tag">我</span>
                      </div>
                    </div>
                    <div class="member-meta">
                      <span class="member-username">@{{ member.username }}</span>
                      <span v-if="member.muted && member.muteTime" class="member-mute-text">
                        已禁言至 {{ formatDateTime(member.muteTime) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-if="canOperateMember(member)" class="member-actions">
                  <button v-if="canToggleAdmin(member)" class="mini-btn" @click="toggleAdminRole(member)">
                    {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
                  </button>
                  <button class="mini-btn" @click="toggleMuteMember(member)">{{ member.muted ? '解除禁言' : '禁言' }}</button>
                  <button class="mini-btn danger" @click="handleRemoveMember(member)">踢出</button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="canDissolveGroup || !isGroupOwner" class="drawer-section danger-zone">
            <div class="section-title-row">
              <span class="drawer-section-title">危险操作</span>
            </div>
            <div class="danger-actions">
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="handleDissolveGroup">解散群聊</button>
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="openTransferOwnerModal">转让群主</button>
              <button v-if="!isGroupOwner" class="danger-action-btn" @click="handleLeaveGroup">退出群聊</button>
            </div>
          </div>
        </div>
      </template>
      <div v-else class="panel-placeholder drawer-placeholder">正在加载群详情...</div>
    </div>
  </div>

  <ConfirmDialog
    v-model:visible="confirmDialog.visible"
    :title="confirmDialog.title"
    :subtitle="confirmDialog.subtitle"
    :description="confirmDialog.description"
    :cancel-text="confirmDialog.cancelText"
    :confirm-text="confirmDialog.confirmText"
    :confirm-type="confirmDialog.confirmType"
    @cancel="cancelConfirmDialog"
    @confirm="confirmConfirmDialog"
  />
  <GroupNoticeDialog
    v-model:visible="showNoticeReminder"
    :group-name="groupDetail?.groupName"
    :notice="groupDetail?.notice"
    :update-time-text="formatDateTime(groupDetail?.noticeUpdateTime)"
    :loading="acknowledgingNoticeReminder"
    @acknowledge="acknowledgeNoticeReminder"
  />
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon, useMessage } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { chatApi, fileApi, friendApi, groupApi, userApi } from '../api/client'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import GroupNoticeDialog from '../components/GroupNoticeDialog.vue'
import { useChatSocket } from '../hooks/useChatSocket'
import { useConfirmDialog } from '../hooks/useConfirmDialog'
import { getCachedFileAccessUrl, resolveFileAccessUrl } from '../utils/file-access'
import { openSafeExternalUrl, resolveSafeDownloadUrl, triggerSafeDownload } from '../utils/url'
import { useUserStore } from '../stores/user'
import { getDateTimeTimestamp } from '../utils/datetime'
import { playNotificationSound, removeInAppNotificationsByMessageIds, showNotification } from '../utils/notify'
import ChatDownloadDialog from './chat/ChatDownloadDialog.vue'
import ChatMediaPreview from './chat/ChatMediaPreview.vue'
import {
  GROUP_ROLE_ADMIN,
  GROUP_ROLE_MEMBER,
  GROUP_ROLE_OWNER,
  MESSAGE_STATUS_RECALLED,
  MESSAGE_TYPE_FILE,
  MESSAGE_TYPE_IMAGE,
  MESSAGE_TYPE_SYSTEM,
  MESSAGE_TYPE_TEXT,
  SESSION_TYPE_GROUP,
  SESSION_TYPE_SINGLE,
  type ChatSession,
  type DisplayMessage,
  type FriendItem,
  type GroupDetail,
  type GroupMember,
  type MentionCandidate
} from './chat/chat-types'
import {
  buildSessionKey,
  compareDisplayMessages,
  createLocalMessageId,
  escapeAttributeSelector,
  escapeRegExp,
  formatDateTime,
  formatSize,
  formatTime,
  getFileName,
  getMessageAnchorKey,
  getMessagePreview,
  normalizeSession,
  resolveMessageTargetId,
  roleClass,
  roleText
} from './chat/chat-utils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const sessions = ref<ChatSession[]>([])
const messages = ref<DisplayMessage[]>([])
const friends = ref<FriendItem[]>([])
const groupDetail = ref<GroupDetail | null>(null)

const currentTargetId = ref<string | null>(null)
const currentSessionType = ref<number>(SESSION_TYPE_SINGLE)
const searchText = ref('')
const inputMessage = ref('')
const sending = ref(false)
const loadingMessages = ref(false)
const loadingSessions = ref(false)
let sendLock = false
const showMenu = ref(false)
const showMsgContextMenu = ref(false)
const showImagePreview = ref(false)
const showDownloadModal = ref(false)
const showEmojiPicker = ref(false)
const showCreateGroupModal = ref(false)
const showGroupDrawer = ref(false)
const showAddMembersModal = ref(false)
const creatingGroup = ref(false)
const addingMembers = ref(false)
const updatingNotice = ref(false)
const showNoticeReminder = ref(false)
const acknowledgingNoticeReminder = ref(false)
const initialized = ref(false)
const syncingSocketState = ref(false)
const activeJumpMessageKey = ref('')
let activeJumpHighlightTimer: ReturnType<typeof setTimeout> | null = null

const messagesRef = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const fileInputRef = ref<HTMLInputElement>()
const imageInputRef = ref<HTMLInputElement>()
const groupAvatarInputRef = ref<HTMLInputElement>()
const menuRef = ref<HTMLElement>()
const menuBtnRef = ref<HTMLElement>()
const emojiRef = ref<HTMLElement>()
const mentionMenuRef = ref<HTMLElement>()

const msgMenuX = ref(0)
const msgMenuY = ref(0)
const selectedMsg = ref<DisplayMessage | null>(null)
const previewImageUrl = ref('')
const downloadFileName = ref('')
const downloadFileSize = ref('')
const downloadProgress = ref(0)
const downloadFileUrl = ref('')
const resolvedMessageFileUrls = ref<Record<string, string>>({})
const noticeDraft = ref('')
const updatingGroupProfile = ref(false)
const showTransferOwnerModal = ref(false)
const transferOwnerSelection = ref<string | number | null>(null)
const transferringOwner = ref(false)
const showMuteMemberModal = ref(false)
const muteTargetMember = ref<GroupMember | null>(null)
const muteMinutesInput = ref('10')
const mutingMember = ref(false)
const addMembersSelection = ref<Array<string | number>>([])
const addMembersMessage = ref('')
const flashSessionKey = ref<string | null>(null)
const showMentionMenu = ref(false)
const mentionQuery = ref('')
const mentionTriggerIndex = ref(-1)
const mentionHighlightedIndex = ref(0)
const mentionBannerQueue = ref<string[]>([])
const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()

const createGroupForm = reactive({
  groupName: '',
  notice: '',
  memberIds: [] as Array<string | number>,
  avatarPreview: '',
  avatarFile: null as File | null
})

const groupProfileDraft = reactive({
  groupName: '',
  avatarPreview: '',
  avatarFile: null as File | null
})

let wasAtBottom = true
let allowInitialHomeSessionAutoSelect = true

const currentSession = computed(() => {
  if (!currentTargetId.value) {
    return null
  }
  return sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(currentTargetId.value!, currentSessionType.value)) || null
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
const isGroupProfileChanged = computed(() => {
  if (!groupDetail.value) {
    return false
  }
  const normalizedName = groupProfileDraft.groupName.trim()
  const currentName = groupDetail.value.groupName?.trim() || ''
  const currentAvatar = groupDetail.value.groupAvatar || ''
  return normalizedName !== currentName || Boolean(groupProfileDraft.avatarFile) || groupProfileDraft.avatarPreview !== currentAvatar
})
const isGroupNoticeChanged = computed(() => {
  if (!groupDetail.value) {
    return false
  }
  return noticeDraft.value.trim() !== (groupDetail.value.notice || '').trim()
})
const hasUnsavedGroupDrawerChanges = computed(() => isGroupProfileChanged.value || isGroupNoticeChanged.value)

const filteredSessions = computed(() => {
  if (!searchText.value.trim()) {
    return sessions.value
  }
  const keyword = searchText.value.trim().toLowerCase()
  return sessions.value.filter(session => session.targetNickname?.toLowerCase().includes(keyword) || session.lastMessage?.toLowerCase().includes(keyword))
})

const availableFriendsForCurrentGroup = computed(() => {
  const memberIds = new Set((groupDetail.value?.members || []).map(member => String(member.userId)))
  return friends.value.filter(friend => !memberIds.has(String(friend.friendId)))
})

const mentionMenuBaseOptions = computed<MentionCandidate[]>(() => {
  if (!isGroupSession.value) {
    return []
  }
  const options: MentionCandidate[] = []
  if (canMentionAll.value) {
    options.push({
      key: 'all',
      label: '所有人',
      meta: '仅群主和管理员可用',
      insertToken: '所有人',
      isAll: true
    })
  }
  for (const member of groupDetail.value?.members || []) {
    if (String(member.userId) === String(userStore.userId)) {
      continue
    }
    const displayName = member.nickname || member.username || `成员${member.userId}`
    options.push({
      key: `user-${member.userId}`,
      label: displayName,
      meta: `@${member.username}`,
      insertToken: getMemberMentionToken(member),
      mentionUserId: String(member.userId),
      isAll: false
    })
  }
  return options
})

const mentionCandidates = computed(() => {
  const keyword = mentionQuery.value.trim().toLowerCase()
  if (!keyword) {
    return mentionMenuBaseOptions.value
  }
  return mentionMenuBaseOptions.value.filter(candidate => {
    const text = `${candidate.label} ${candidate.meta} ${candidate.insertToken}`.toLowerCase()
    return text.includes(keyword)
  })
})

const resolvedMentionBannerKeys = computed(() => {
  const existingKeys = new Set(messages.value.map(messageItem => getMessageAnchorKey(messageItem)))
  const seen = new Set<string>()
  return mentionBannerQueue.value.filter(key => {
    if (!existingKeys.has(key) || seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
})

const activeMentionBannerMessage = computed(() => {
  const activeKey = resolvedMentionBannerKeys.value[0]
  if (!activeKey) {
    return null
  }
  return messages.value.find(messageItem => getMessageAnchorKey(messageItem) === activeKey) || null
})

const showMentionBanner = computed(() => {
  return isGroupSession.value && Boolean(activeMentionBannerMessage.value)
})

const mentionBannerText = computed(() => {
  const messageItem = activeMentionBannerMessage.value
  if (!messageItem) {
    return ''
  }
  const senderName = messageItem.name || '有成员'
  const preview = getMessagePreview(messageItem)
  return `${senderName}提醒你留意这条消息：${preview}`
})

const mentionBannerActionText = computed(() => {
  const remainingCount = Math.max(0, resolvedMentionBannerKeys.value.length - 1)
  return remainingCount > 0 ? `定位后还有 ${remainingCount} 条` : '点击定位'
})

function handleRealtimeEvent(payload: any) {
  if (!payload?.type) {
    return
  }
  if (payload.type === 'CONNECTED') {
    return
  }
  if (payload.type === 'GROUP_DETAIL') {
    handleRealtimeGroupDetail(payload.data?.detail)
    return
  }
  if (payload.type === 'GROUP_REMOVED') {
    handleRealtimeGroupRemoved(payload.data)
    return
  }
  if (payload.type === 'MESSAGE') {
    handleRealtimeMessage(payload.data?.message)
    return
  }
  if (payload.type === 'SESSION') {
    handleRealtimeSession(payload.data?.session)
    return
  }
  if (payload.type === 'READ_RECEIPT') {
    handleRealtimeReadReceipt(payload.data)
    return
  }
  if (payload.type === 'ONLINE_STATUS') {
    handleRealtimeOnlineStatus(payload.data)
    return
  }
  if (payload.type === 'MESSAGE_RECALLED') {
    handleRealtimeMessageRecalled(payload.data?.message)
  }
}

async function syncSocketState() {
  if (!initialized.value || syncingSocketState.value) {
    return
  }
  syncingSocketState.value = true
  try {
    await loadFriends()
    await loadSessions()
    if (currentTargetId.value) {
      const syncTasks: Array<Promise<unknown>> = []
      syncTasks.push(loadMessages(currentTargetId.value, currentSessionType.value))
      if (currentSessionType.value === SESSION_TYPE_GROUP) {
        syncTasks.push(loadGroupDetail(currentTargetId.value, false))
      }
      await Promise.all(syncTasks)
    }
  } finally {
    syncingSocketState.value = false
  }
}

const {
  connected: wsConnected,
  reconnectAttempt: wsReconnectAttempt,
  connect: connectChatSocket,
  disconnect: disconnectChatSocket,
  sendRequest: sendChatSocketRequest
} = useChatSocket({
  token: () => userStore.token,
  createTicket: async () => {
    const res: any = await chatApi.createWsTicket()
    return String(res.data?.ticket || '')
  },
  onMessage: handleRealtimeEvent,
  onOpen: () => {
    void syncSocketState()
  }
})

function sendChatCommand(action: string, data: Record<string, unknown> = {}) {
  return sendChatSocketRequest(action, data)
}

const emojis = ['😀', '😂', '🤣', '😍', '🥰', '😘', '😎', '🤔', '😏', '😢', '😭', '😡', '🥳', '😱', '🥺', '😴', '🤗', '😈', '👻', '💀', '🤡', '👽', '🤖', '💩', '❤️', '🔥', '👍', '👎', '👏', '🙏', '💪', '🎉', '🎊', '☕', '🌹', '🍀', '🌈', '☀️', '🌙', '⭐']

function sortSessions() {
  sessions.value = [...sessions.value].sort((left, right) => {
    const leftTime = getDateTimeTimestamp(left.lastMessageTime)
    const rightTime = getDateTimeTimestamp(right.lastMessageTime)
    if (leftTime !== rightTime) {
      return rightTime - leftTime
    }
    return String(right.id || '').localeCompare(String(left.id || ''))
  })
}

function isFriendTarget(targetId: string | number) {
  return friends.value.some(friend => String(friend.friendId) === String(targetId))
}

function shouldPreserveDraftSession(session: ChatSession) {
  if (Number(session.sessionType || SESSION_TYPE_SINGLE) === SESSION_TYPE_GROUP) {
    return true
  }
  return isFriendTarget(session.targetId)
}

function upsertSession(nextSession: ChatSession) {
  const normalizedSession = normalizeSession(nextSession)
  const sessionKey = buildSessionKey(normalizedSession.targetId, normalizedSession.sessionType)
  const sessionIndex = sessions.value.findIndex(session => buildSessionKey(session.targetId, session.sessionType) === sessionKey)
  if (sessionIndex === -1) {
    sessions.value = [normalizedSession, ...sessions.value]
    sortSessions()
    return normalizedSession
  }

  const mergedSession = {
    ...sessions.value[sessionIndex],
    ...normalizedSession
  }
  sessions.value.splice(sessionIndex, 1, mergedSession)
  sortSessions()
  return mergedSession
}

function updateCurrentSessionFromStore(nextSession: ChatSession) {
  if (!currentTargetId.value || !isCurrentSession(nextSession)) {
    return
  }
  if (isGroupSession.value && groupDetail.value) {
    groupDetail.value = {
      ...groupDetail.value,
      groupAvatar: nextSession.targetAvatar || groupDetail.value.groupAvatar,
      groupRemark: nextSession.groupRemark ?? groupDetail.value.groupRemark,
      notice: nextSession.notice ?? groupDetail.value.notice,
      noticeUnread: nextSession.noticeUnread ?? groupDetail.value.noticeUnread,
      memberCount: nextSession.memberCount ?? groupDetail.value.memberCount,
      myRole: nextSession.myRole ?? groupDetail.value.myRole,
      muted: nextSession.muted ?? groupDetail.value.muted,
      muteTime: nextSession.muteTime ?? groupDetail.value.muteTime,
      notificationMuted: nextSession.notificationMuted ?? groupDetail.value.notificationMuted
    }
  }
}

function flashSession(targetId: string | number, sessionType: number) {
  const sessionKey = buildSessionKey(targetId, sessionType)
  flashSessionKey.value = sessionKey
  setTimeout(() => {
    if (flashSessionKey.value === sessionKey) {
      flashSessionKey.value = null
    }
  }, 2000)
}

function isCurrentSession(session: ChatSession) {
  if (!currentTargetId.value) {
    return false
  }
  return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(currentTargetId.value, currentSessionType.value)
}

function getRouteMessageId() {
  const rawMessageId = route.query.messageId
  if (Array.isArray(rawMessageId)) {
    return rawMessageId[0] ? String(rawMessageId[0]) : ''
  }
  return rawMessageId ? String(rawMessageId) : ''
}

function clearActiveJumpHighlight() {
  if (activeJumpHighlightTimer) {
    clearTimeout(activeJumpHighlightTimer)
    activeJumpHighlightTimer = null
  }
  activeJumpMessageKey.value = ''
}

function setActiveJumpHighlight(messageItem: DisplayMessage | null | undefined) {
  if (!messageItem) {
    clearActiveJumpHighlight()
    return
  }
  clearActiveJumpHighlight()
  activeJumpMessageKey.value = getMessageAnchorKey(messageItem)
  activeJumpHighlightTimer = setTimeout(() => {
    activeJumpMessageKey.value = ''
    activeJumpHighlightTimer = null
  }, 2600)
}

function findMessageByRouteMessageId(messageId: string) {
  if (!messageId) {
    return null
  }
  return messages.value.find(messageItem => String(messageItem.id) === messageId || getMessageAnchorKey(messageItem) === messageId) || null
}

function getMessageTextSegments(content: string) {
  const segments: Array<{ text: string, mention: boolean }> = []
  const pattern = /@[^\s,，。.！!？?；;：:]+/g
  let lastIndex = 0
  for (const match of content.matchAll(pattern)) {
    const index = match.index ?? 0
    if (index > lastIndex) {
      segments.push({
        text: content.slice(lastIndex, index),
        mention: false
      })
    }
    segments.push({
      text: match[0],
      mention: true
    })
    lastIndex = index + match[0].length
  }
  if (lastIndex < content.length) {
    segments.push({
      text: content.slice(lastIndex),
      mention: false
    })
  }
  if (segments.length === 0) {
    segments.push({
      text: content,
      mention: false
    })
  }
  return segments
}

function getMemberMentionToken(member: GroupMember) {
  const nickname = member.nickname?.trim()
  if (nickname) {
    const duplicateCount = (groupDetail.value?.members || []).filter(item => item.nickname?.trim() === nickname).length
    if (duplicateCount === 1) {
      return nickname
    }
  }
  return member.username?.trim() || nickname || `成员${member.userId}`
}

function closeMentionMenu() {
  showMentionMenu.value = false
  mentionQuery.value = ''
  mentionTriggerIndex.value = -1
  mentionHighlightedIndex.value = 0
}

function clearMentionBannerQueue() {
  mentionBannerQueue.value = []
}

function setMentionBannerQueue(messageItems: DisplayMessage[]) {
  mentionBannerQueue.value = messageItems
    .filter(messageItem => messageItem.mentionedMe && !messageItem.isMe && !messageItem.isSystem)
    .map(messageItem => getMessageAnchorKey(messageItem))
}

function appendMentionBanner(messageItem: DisplayMessage | null | undefined) {
  if (!messageItem?.mentionedMe || messageItem.isMe || messageItem.isSystem) {
    return
  }
  const nextKey = getMessageAnchorKey(messageItem)
  mentionBannerQueue.value = [nextKey, ...mentionBannerQueue.value.filter(key => key !== nextKey)]
}

function consumeMentionBanner(messageItem: DisplayMessage | null | undefined = activeMentionBannerMessage.value) {
  if (!messageItem) {
    return
  }
  const key = getMessageAnchorKey(messageItem)
  mentionBannerQueue.value = mentionBannerQueue.value.filter(item => item !== key)
}

function dismissMentionBanner() {
  consumeMentionBanner()
}

function scrollToMessage(messageItem: DisplayMessage | null | undefined, behavior: ScrollBehavior = 'smooth') {
  if (!messagesRef.value || !messageItem) {
    return
  }
  const selector = `[data-message-key="${escapeAttributeSelector(getMessageAnchorKey(messageItem))}"]`
  const target = messagesRef.value.querySelector<HTMLElement>(selector)
  if (!target) {
    return
  }
  target.scrollIntoView({
    behavior,
    block: 'center'
  })
}

async function clearRouteMessageQuery() {
  if (!getRouteMessageId()) {
    return
  }
  const nextQuery = { ...route.query }
  delete nextQuery.messageId
  await router.replace({
    path: route.path,
    query: nextQuery
  })
}

function scrollToActiveMentionMessage(behavior: ScrollBehavior = 'smooth') {
  if (!activeMentionBannerMessage.value) {
    return
  }
  scrollToMessage(activeMentionBannerMessage.value, behavior)
}

function handleMentionBannerClick() {
  const messageItem = activeMentionBannerMessage.value
  if (!messageItem) {
    return
  }
  scrollToMessage(messageItem)
  consumeMentionBanner(messageItem)
}

function resolveUnreadMentionMessages(messageList: DisplayMessage[], unreadCount: number) {
  if (!Number.isFinite(unreadCount) || unreadCount <= 0) {
    return []
  }
  const incomingMessages = messageList.filter(messageItem => !messageItem.isMe && !messageItem.isSystem)
  if (incomingMessages.length === 0) {
    return []
  }
  const unreadMessages = incomingMessages.slice(-Math.max(0, Math.trunc(unreadCount)))
  return unreadMessages.filter(messageItem => messageItem.mentionedMe).reverse()
}

function getMentionTrigger(text: string, cursor: number) {
  const beforeCursor = text.slice(0, cursor)
  const atIndex = beforeCursor.lastIndexOf('@')
  if (atIndex === -1) {
    return null
  }
  const beforeChar = atIndex > 0 ? beforeCursor[atIndex - 1] : ''
  if (beforeChar && !/\s/.test(beforeChar)) {
    return null
  }
  const query = beforeCursor.slice(atIndex + 1)
  if (/[\s@]/.test(query)) {
    return null
  }
  return {
    atIndex,
    query
  }
}

function syncMentionMenu() {
  if (!isGroupSession.value || !textareaRef.value || currentMuted.value) {
    closeMentionMenu()
    return
  }
  const cursor = textareaRef.value.selectionStart ?? inputMessage.value.length
  const trigger = getMentionTrigger(inputMessage.value, cursor)
  if (!trigger) {
    closeMentionMenu()
    return
  }
  mentionTriggerIndex.value = trigger.atIndex
  mentionQuery.value = trigger.query
  mentionHighlightedIndex.value = 0
  showMentionMenu.value = true
}

function handleInputChange() {
  autoResize()
  syncMentionMenu()
}

function selectMentionCandidate(candidate: MentionCandidate) {
  if (!textareaRef.value) {
    return
  }
  const cursor = textareaRef.value.selectionStart ?? inputMessage.value.length
  const replaceStart = mentionTriggerIndex.value >= 0 ? mentionTriggerIndex.value : cursor
  const before = inputMessage.value.slice(0, replaceStart)
  const after = inputMessage.value.slice(cursor)
  inputMessage.value = `${before}@${candidate.insertToken} ${after}`
  closeMentionMenu()
  nextTick(() => {
    if (!textareaRef.value) {
      return
    }
    const nextCursor = before.length + candidate.insertToken.length + 2
    textareaRef.value.focus()
    textareaRef.value.setSelectionRange(nextCursor, nextCursor)
    autoResize()
  })
}

function handleInputKeydown(event: KeyboardEvent) {
  if (showMentionMenu.value) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      if (mentionCandidates.value.length > 0) {
        mentionHighlightedIndex.value = (mentionHighlightedIndex.value + 1) % mentionCandidates.value.length
      }
      return
    }
    if (event.key === 'ArrowUp') {
      event.preventDefault()
      if (mentionCandidates.value.length > 0) {
        mentionHighlightedIndex.value = (mentionHighlightedIndex.value - 1 + mentionCandidates.value.length) % mentionCandidates.value.length
      }
      return
    }
    if (event.key === 'Enter' && !event.shiftKey) {
      if (mentionCandidates.value.length > 0) {
        event.preventDefault()
        selectMentionCandidate(mentionCandidates.value[mentionHighlightedIndex.value] || mentionCandidates.value[0])
        return
      }
    }
    if (event.key === 'Escape') {
      event.preventDefault()
      closeMentionMenu()
      return
    }
  }
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    void handleSend()
  }
}

function hasMentionToken(content: string, token: string, ignoreCase = false) {
  if (!content || !token) {
    return false
  }
  const flags = ignoreCase ? 'i' : ''
  const pattern = new RegExp(`(^|[\\s])@${escapeRegExp(token)}(?=$|[\\s,，。.！!？?；;：:])`, flags)
  return pattern.test(content)
}

function resolveOutgoingMentions(content: string) {
  if (!isGroupSession.value) {
    return {
      mentionAll: false,
      mentionUserIds: [] as string[],
      mentionDisplayNames: [] as string[]
    }
  }
  const mentionUserIds: string[] = []
  const mentionDisplayNames: string[] = []
  for (const member of groupDetail.value?.members || []) {
    if (String(member.userId) === String(userStore.userId)) {
      continue
    }
    if (!hasMentionToken(content, getMemberMentionToken(member))) {
      continue
    }
    mentionUserIds.push(String(member.userId))
    mentionDisplayNames.push(member.nickname || member.username || `成员${member.userId}`)
  }
  return {
    mentionAll: hasMentionToken(content, '所有人') || hasMentionToken(content, 'all', true),
    mentionUserIds,
    mentionDisplayNames
  }
}

function getNotificationTitle(messageItem: DisplayMessage) {
  if (messageItem.isSystem) {
    return '群系统通知'
  }
  const sessionName = sessions.value.find(session =>
    buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(messageItem.targetId, messageItem.sessionType)
  )?.targetNickname
  if (messageItem.mentionedMe) {
    return `${sessionName || currentSessionName.value || '群聊'} · 特别提醒`
  }
  return sessionName || messageItem.name || '新消息'
}

function shouldShowDesktopNotification(messageItem: DisplayMessage) {
  if (messageItem.isMe) {
    return false
  }
  if (messageItem.sessionType !== SESSION_TYPE_GROUP) {
    return true
  }
  const targetId = String(messageItem.targetId)
  if (currentTargetId.value && currentSessionType.value === SESSION_TYPE_GROUP && String(currentTargetId.value) === targetId) {
    return !currentNotificationMuted.value
  }
  const session = sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(targetId, SESSION_TYPE_GROUP))
  return !Boolean(session?.notificationMuted)
}

function getNotificationSoundType(messageItem: DisplayMessage) {
  if (messageItem.isSystem || messageItem.mentionedMe) {
    return 'attention' as const
  }
  return 'message' as const
}

function notifyIncomingMessage(messageItem: DisplayMessage) {
  const shouldNotify = shouldShowDesktopNotification(messageItem)
  const targetId = String(messageItem.targetId || '')
  const sessionType = Number(messageItem.sessionType || SESSION_TYPE_SINGLE)
  if (!shouldNotify) {
    return
  }
  void showNotification(getNotificationTitle(messageItem), getMessagePreview(messageItem), undefined, {
    targetId,
    sessionType,
    messageId: String(messageItem.id || ''),
    attention: Boolean(messageItem.isSystem || messageItem.mentionedMe)
  })
  void playNotificationSound(getNotificationSoundType(messageItem))
}

function releaseMessageResource(messageItem?: DisplayMessage | null) {
  if (!messageItem?.content?.startsWith('blob:')) {
    return
  }
  URL.revokeObjectURL(messageItem.content)
}

function cleanupMessageResources(messageList = messages.value) {
  for (const messageItem of messageList) {
    releaseMessageResource(messageItem)
  }
}

function shouldResolveRemoteMessageFile(messageItem?: DisplayMessage | null) {
  if (!messageItem) {
    return false
  }
  if (messageItem.msgType !== MESSAGE_TYPE_IMAGE && messageItem.msgType !== MESSAGE_TYPE_FILE) {
    return false
  }
  return Boolean(messageItem.content) && !messageItem.content.startsWith('blob:')
}

async function ensureMessageFileAccessUrl(rawUrl: string) {
  if (!rawUrl || rawUrl.startsWith('blob:')) {
    return rawUrl
  }
  const cachedAccessUrl = getCachedFileAccessUrl(rawUrl)
  if (cachedAccessUrl) {
    if (resolvedMessageFileUrls.value[rawUrl] !== cachedAccessUrl) {
      resolvedMessageFileUrls.value = {
        ...resolvedMessageFileUrls.value,
        [rawUrl]: cachedAccessUrl
      }
    }
    return cachedAccessUrl
  }
  const accessUrl = await resolveFileAccessUrl(rawUrl)
  if (accessUrl) {
    resolvedMessageFileUrls.value = {
      ...resolvedMessageFileUrls.value,
      [rawUrl]: accessUrl
    }
  }
  return accessUrl
}

async function preloadMessageFileUrls(messageList: DisplayMessage[]) {
  const targets = Array.from(new Set(
    messageList
      .filter(shouldResolveRemoteMessageFile)
      .map(messageItem => messageItem.content)
      .filter(Boolean)
  ))
  await Promise.all(targets.map(rawUrl => ensureMessageFileAccessUrl(rawUrl).catch(() => '')))
}

function getResolvedMessageFileUrl(messageItem: DisplayMessage) {
  if (!shouldResolveRemoteMessageFile(messageItem)) {
    return messageItem.content
  }
  const rawUrl = messageItem.content
  const cachedAccessUrl = getCachedFileAccessUrl(rawUrl)
  if (cachedAccessUrl && resolvedMessageFileUrls.value[rawUrl] !== cachedAccessUrl) {
    resolvedMessageFileUrls.value = {
      ...resolvedMessageFileUrls.value,
      [rawUrl]: cachedAccessUrl
    }
  }
  return cachedAccessUrl || ''
}

function revokeBlobUrl(url?: string) {
  if (url?.startsWith('blob:')) {
    URL.revokeObjectURL(url)
  }
}

function resetCurrentConversationState() {
  currentTargetId.value = null
  currentSessionType.value = SESSION_TYPE_SINGLE
  applyGroupDetail(null)
  cleanupMessageResources()
  resolvedMessageFileUrls.value = {}
  messages.value = []
}

function toDisplayMessage(item: any): DisplayMessage {
  const readTime = item.readTime || ''
  const isRecalled = Number(item.status ?? 0) === MESSAGE_STATUS_RECALLED
  const msgType = Number(item.msgType ?? MESSAGE_TYPE_TEXT)
  const sessionType = Number(item.sessionType || SESSION_TYPE_SINGLE)
  const isSystem = msgType === MESSAGE_TYPE_SYSTEM
  const isMe = !isSystem && String(item.fromUserId) === String(userStore.userId)
  const mentionUserIds = Array.isArray(item.mentionUserIds) ? item.mentionUserIds.map((id: string | number) => String(id)) : []
  const mentionAll = Boolean(item.mentionAll)
  const mentionDisplayNames = Array.isArray(item.mentionDisplayNames)
    ? item.mentionDisplayNames.filter((value: unknown): value is string => typeof value === 'string' && value.trim().length > 0)
    : []
  return {
    id: item.id,
    localId: item.clientMessageId || String(item.id || createLocalMessageId('server')),
    clientMessageId: item.clientMessageId || '',
    isMe,
    isSystem,
    name: item.fromNickname,
    fromAvatar: item.fromAvatar || '',
    content: item.content,
    msgType,
    status: Number(item.status ?? 0),
    readTime,
    createTime: item.createTime,
    time: item.createTime?.substring(11, 16) || '',
    readStatus: isRecalled ? '已撤回' : (readTime ? '已读' : '未读'),
    deliveryStatus: 'sent',
    fileName: item.fileName || '',
    fileSize: item.fileSize ? Number(item.fileSize) : undefined,
    sessionType,
    targetId: resolveMessageTargetId(item, isMe, sessionType),
    mentionAll,
    mentionUserIds,
    mentionDisplayNames,
    mentionedMe: sessionType === SESSION_TYPE_GROUP && !isSystem && !isMe && (mentionAll || mentionUserIds.includes(String(userStore.userId))),
    uploadedFileId: undefined
  }
}

function upsertMessage(nextMessage: DisplayMessage) {
  const messageIndex = messages.value.findIndex(item => {
    if (String(item.id) === String(nextMessage.id)) {
      return true
    }
    if (item.clientMessageId && nextMessage.clientMessageId && item.clientMessageId === nextMessage.clientMessageId) {
      return true
    }
    return item.localId === nextMessage.localId
  })
  if (messageIndex === -1) {
    messages.value = [...messages.value, nextMessage]
  } else {
    const previousMessage = messages.value[messageIndex]
    if (previousMessage.content !== nextMessage.content) {
      releaseMessageResource(previousMessage)
    }
    messages.value.splice(messageIndex, 1, {
      ...messages.value[messageIndex],
      ...nextMessage
    })
  }
  messages.value.sort(compareDisplayMessages)
  if (shouldResolveRemoteMessageFile(nextMessage)) {
    void ensureMessageFileAccessUrl(nextMessage.content)
  }
}

function handleRealtimeMessage(rawMessage: any) {
  if (!rawMessage) {
    return
  }
  const messageItem = toDisplayMessage(rawMessage)
  const messageSessionType = Number(rawMessage.sessionType || SESSION_TYPE_SINGLE)
  const messageTargetId = messageSessionType === SESSION_TYPE_GROUP
    ? String(rawMessage.toUserId)
    : String(messageItem.isMe ? rawMessage.toUserId : rawMessage.fromUserId)
  const sameCurrentSession = currentTargetId.value
    && buildSessionKey(messageTargetId, messageSessionType) === buildSessionKey(currentTargetId.value, currentSessionType.value)

  if (sameCurrentSession) {
    const shouldStickBottom = wasAtBottom
    upsertMessage(messageItem)
    if (!messageItem.isMe && messageItem.mentionedMe) {
      appendMentionBanner(messageItem)
    }
    nextTick(() => {
      if (messagesRef.value && shouldStickBottom) {
        messagesRef.value.scrollTop = messagesRef.value.scrollHeight
      }
    })
    if (!messageItem.isMe) {
      notifyIncomingMessage(messageItem)
      markCurrentSessionRead(messageTargetId, messageSessionType)
    }
    return
  }

  if (!messageItem.isMe) {
    notifyIncomingMessage(messageItem)
    flashSession(messageTargetId, messageSessionType)
  }
}

function handleRealtimeMessageRecalled(rawMessage: any) {
  if (!rawMessage) {
    return
  }
  const messageItem = toDisplayMessage(rawMessage)
  upsertMessage(messageItem)
  consumeMentionBanner(messageItem)
  removeInAppNotificationsByMessageIds([messageItem.id])
}

function handleRealtimeSession(rawSession: any) {
  if (!rawSession) {
    return
  }
  const previousSession = sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(rawSession.targetId, Number(rawSession.sessionType || SESSION_TYPE_SINGLE)))
  const session = upsertSession(rawSession)
  updateCurrentSessionFromStore(session)
  if (previousSession && previousSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {
    flashSession(session.targetId, session.sessionType)
  }
}

function handleRealtimeReadReceipt(payload: any) {
  if (!payload || Number(payload.sessionType) !== SESSION_TYPE_SINGLE) {
    return
  }
  const messageIds = new Set((payload.messageIds || []).map((id: string | number) => String(id)))
  if (messageIds.size === 0) {
    return
  }
  messages.value = messages.value.map(item => {
    if (!messageIds.has(String(item.id))) {
      return item
    }
    return {
      ...item,
      readTime: payload.readTime || item.readTime,
      readStatus: item.status === MESSAGE_STATUS_RECALLED ? '已撤回' : '已读'
    }
  })
}

function handleRealtimeOnlineStatus(payload: any) {
  if (!payload?.userId) {
    return
  }
  const targetUserId = String(payload.userId)
  sessions.value = sessions.value.map(session => {
    if (session.sessionType !== SESSION_TYPE_SINGLE || String(session.targetId) !== targetUserId) {
      return session
    }
    return {
      ...session,
      targetOnline: Boolean(payload.online)
    }
  })
}

function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {
  groupDetail.value = detail
  if (syncDraft || !isGroupNoticeChanged.value) {
    noticeDraft.value = detail?.notice || ''
  }
  if (syncDraft || !isGroupProfileChanged.value) {
    syncGroupProfileDraft(detail)
  }
  if (!detail?.noticeUnread || !detail.notice?.trim()) {
    showNoticeReminder.value = false
  } else {
    showNoticeReminder.value = true
  }

  if (!detail) {
    return
  }

  const sessionKey = buildSessionKey(detail.id, SESSION_TYPE_GROUP)
  const session = sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === sessionKey)
  if (session) {
    session.targetNickname = detail.groupRemark || detail.groupName
    session.targetAvatar = detail.groupAvatar || ''
    session.groupRemark = detail.groupRemark || ''
    session.memberCount = detail.memberCount
    session.notice = detail.notice || ''
    session.myRole = detail.myRole
    session.muted = detail.muted
    session.muteTime = detail.muteTime
    session.notificationMuted = Boolean(detail.notificationMuted)
  }
}

function removeSessionByTarget(targetId: string | number, sessionType: number) {
  const sessionKey = buildSessionKey(targetId, sessionType)
  sessions.value = sessions.value.filter(session => buildSessionKey(session.targetId, session.sessionType) !== sessionKey)
}

function isUnavailableConversationError(error: any) {
  const code = Number(error?.response?.data?.code || error?.response?.status || 0)
  return code === 403 || code === 404
}

async function closeUnavailableSingleSession(targetId: string | number, options: { notify?: boolean } = {}) {
  const normalizedTargetId = String(targetId)
  removeSessionByTarget(normalizedTargetId, SESSION_TYPE_SINGLE)

  const routeTargetId = route.params.targetId ? String(route.params.targetId) : ''
  const routeSessionType = Number(route.query.sessionType || SESSION_TYPE_SINGLE)
  const routeMatches = routeTargetId === normalizedTargetId && routeSessionType === SESSION_TYPE_SINGLE
  const currentMatches = Boolean(currentTargetId.value)
    && currentSessionType.value === SESSION_TYPE_SINGLE
    && String(currentTargetId.value) === normalizedTargetId

  if (currentMatches) {
    resetCurrentConversationState()
  }

  if (routeMatches) {
    await router.replace('/chat')
  }

  if ((routeMatches || currentMatches) && options.notify) {
    message.warning('当前单聊已不可用')
  }
}

async function closeUnavailableGroupSession(
  targetId: string | number,
  options: { notify?: boolean; messageText?: string } = {}
) {
  const normalizedTargetId = String(targetId)
  removeSessionByTarget(normalizedTargetId, SESSION_TYPE_GROUP)

  const routeTargetId = route.params.targetId ? String(route.params.targetId) : ''
  const routeSessionType = Number(route.query.sessionType || SESSION_TYPE_SINGLE)
  const routeMatches = routeTargetId === normalizedTargetId && routeSessionType === SESSION_TYPE_GROUP
  const currentMatches = Boolean(currentTargetId.value)
    && currentSessionType.value === SESSION_TYPE_GROUP
    && String(currentTargetId.value) === normalizedTargetId

  if (currentMatches) {
    await closeGroupDrawer({ force: true })
    resetCurrentConversationState()
  }

  if (routeMatches) {
    await router.replace('/chat')
  }

  if ((routeMatches || currentMatches) && options.notify) {
    message.warning(options.messageText || '当前群聊已不可用')
  }
}

async function handleRealtimeGroupRemoved(payload: any) {
  const groupId = payload?.groupId ? String(payload.groupId) : ''
  if (!groupId) {
    return
  }
  await closeUnavailableGroupSession(groupId, {
    notify: true,
    messageText: '你已不在该群聊中'
  })
}

function handleRealtimeGroupDetail(detail: GroupDetail | null) {
  if (!detail?.id) {
    return
  }
  const existingSession = sessions.value.find(session =>
    buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(detail.id, SESSION_TYPE_GROUP)
  )

  upsertSession({
    targetId: detail.id,
    sessionType: SESSION_TYPE_GROUP,
    targetNickname: detail.groupRemark || detail.groupName,
    targetAvatar: detail.groupAvatar || '',
    lastMessage: existingSession?.lastMessage || '',
    lastMessageTime: existingSession?.lastMessageTime || '',
    unreadCount: existingSession?.unreadCount || 0,
    memberCount: detail.memberCount,
    myRole: detail.myRole,
    groupRemark: detail.groupRemark || '',
    notice: detail.notice || '',
    noticeUnread: Boolean(detail.noticeUnread),
    muted: detail.muted,
    muteTime: detail.muteTime,
    notificationMuted: Boolean(detail.notificationMuted)
  })

  if (currentTargetId.value && currentSessionType.value === SESSION_TYPE_GROUP && String(currentTargetId.value) === String(detail.id)) {
    applyGroupDetail(detail, false)
  }
}

function autoResize() {
  if (!textareaRef.value) {
    return
  }
  textareaRef.value.style.height = 'auto'
  textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 120)}px`
}

function checkIfAtBottom() {
  if (!messagesRef.value) {
    return
  }
  const element = messagesRef.value
  wasAtBottom = element.scrollHeight - element.scrollTop - element.clientHeight < 50
}

function handleMessageMediaLoad() {
  if (!wasAtBottom) {
    return
  }
  scrollMessagesToBottom(true)
}

async function previewImage(messageItem: DisplayMessage) {
  const resolvedUrl = getResolvedMessageFileUrl(messageItem) || await ensureMessageFileAccessUrl(messageItem.content)
  if (!resolvedUrl) {
    message.error('图片访问链接已失效，请稍后重试')
    return
  }
  previewImageUrl.value = resolvedUrl
  showImagePreview.value = true
}

function closeImagePreview() {
  showImagePreview.value = false
  previewImageUrl.value = ''
}

function handleWindowKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && showImagePreview.value) {
    closeImagePreview()
  }
}

function getFileSizeText(msg: DisplayMessage) {
  return formatSize(msg.fileSize)
}

function createPendingMessage(options: {
  content: string
  msgType: number
  fileName?: string
  fileSize?: number
  mentionAll?: boolean
  mentionUserIds?: string[]
  mentionDisplayNames?: string[]
  retryFile?: File
}) {
  if (!currentTargetId.value) {
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
    name: userStore.nickname || '我',
    fromAvatar: userStore.avatar || '',
    content: options.content,
    msgType: options.msgType,
    status: 0,
    readTime: '',
    createTime: now.toISOString(),
    time: now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    readStatus: '未读',
    deliveryStatus: 'sending',
    fileName: options.fileName || '',
    fileSize: options.fileSize,
    sessionType: currentSessionType.value,
    targetId: String(currentTargetId.value),
    mentionAll: Boolean(options.mentionAll),
    mentionUserIds: options.mentionUserIds || [],
    mentionDisplayNames: options.mentionDisplayNames || [],
    mentionedMe: false,
    retryFile: options.retryFile
  }
  upsertMessage(pendingMessage)
  return pendingMessage
}

function markMessageDelivery(localId: string, deliveryStatus: DisplayMessage['deliveryStatus']) {
  const messageIndex = messages.value.findIndex(item => item.localId === localId)
  if (messageIndex === -1) {
    return
  }
  messages.value.splice(messageIndex, 1, {
    ...messages.value[messageIndex],
    deliveryStatus
  })
}

function patchMessage(localId: string, patch: Partial<DisplayMessage>) {
  const messageIndex = messages.value.findIndex(item => item.localId === localId)
  if (messageIndex === -1) {
    return
  }
  messages.value.splice(messageIndex, 1, {
    ...messages.value[messageIndex],
    ...patch
  })
}

function scrollMessagesToBottom(force = false) {
  nextTick(() => {
    if (!messagesRef.value) {
      return
    }
    if (!force && !wasAtBottom) {
      return
    }
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

async function sendPendingTextMessage(localMessage: DisplayMessage, content: string) {
  const response: any = await sendChatCommand('SEND_MESSAGE', {
    toUserId: localMessage.targetId,
    content,
    sessionType: localMessage.sessionType,
    clientMessageId: localMessage.clientMessageId,
    mentionAll: localMessage.mentionAll,
    mentionUserIds: localMessage.mentionUserIds
  })
  if (response?.data) {
    upsertMessage(toDisplayMessage(response.data))
  } else {
    markMessageDelivery(localMessage.localId, 'sent')
  }
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
  const sendResponse: any = await sendChatCommand('SEND_FILE_MESSAGE', {
    toUserId: localMessage.targetId,
    fileId,
    msgType,
    sessionType: localMessage.sessionType,
    clientMessageId: localMessage.clientMessageId
  })
  if (sendResponse?.data) {
    upsertMessage(toDisplayMessage(sendResponse.data))
  } else {
    markMessageDelivery(localMessage.localId, 'sent')
  }
}

async function executePendingMessage(localMessage: DisplayMessage, executor: () => Promise<void>, failureMessage: string) {
  try {
    await executor()
    scrollMessagesToBottom(true)
  } catch (error: any) {
    console.error('executePendingMessage error:', error)
    markMessageDelivery(localMessage.localId, 'failed')
    message.error(error.response?.data?.message || failureMessage)
    return
  }
}

async function retryFailedMessage(messageItem: DisplayMessage) {
  if (messageItem.deliveryStatus !== 'failed') {
    return
  }
  if (buildSessionKey(messageItem.targetId, messageItem.sessionType) !== buildSessionKey(currentTargetId.value || '', currentSessionType.value)) {
    message.warning('请回到原会话后重试发送')
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

async function downloadFile(msg: DisplayMessage) {
  const resolvedUrl = getResolvedMessageFileUrl(msg) || await ensureMessageFileAccessUrl(msg.content)
  const safeDownloadUrl = resolveSafeDownloadUrl(resolvedUrl)
  if (!safeDownloadUrl) {
    message.error('文件链接无效或协议不受支持')
    return
  }
  downloadFileName.value = msg.fileName || getFileName(msg.content)
  downloadFileSize.value = getFileSizeText(msg)
  downloadFileUrl.value = safeDownloadUrl
  downloadProgress.value = 100
  showDownloadModal.value = true
}

async function openDownloadedFile() {
  try {
    await openSafeExternalUrl(downloadFileUrl.value)
    showDownloadModal.value = false
  } catch (error: any) {
    message.error(error.message || '打开文件失败')
  }
}

function saveDownloadedFile() {
  try {
    triggerSafeDownload(downloadFileUrl.value, downloadFileName.value)
    showDownloadModal.value = false
  } catch (error: any) {
    message.error(error.message || '保存文件失败')
  }
}

function insertEmoji(emoji: string) {
  inputMessage.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
  nextTick(handleInputChange)
}

function triggerFileUpload() {
  if (!currentMuted.value) {
    fileInputRef.value?.click()
  }
}

function triggerImageUpload() {
  if (!currentMuted.value) {
    imageInputRef.value?.click()
  }
}

function triggerGroupAvatarUpload() {
  groupAvatarInputRef.value?.click()
}

function triggerGroupProfileAvatarUpload() {
  if (!canEditGroupProfile.value) {
    return
  }
  groupAvatarInputRef.value?.click()
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

async function loadFriends() {
  try {
    const response: any = await friendApi.getList()
    friends.value = response.data || []
  } catch (error) {
    console.error('loadFriends error:', error)
  }
}

async function loadSessions() {
  try {
    const response: any = await sendChatCommand('GET_SESSIONS')
    const nextSessions = (response.data || []).map((item: any) => normalizeSession(item))

    if (sessions.value.length > 0) {
      const sessionMap = new Map(sessions.value.map(session => [buildSessionKey(session.targetId, session.sessionType), session]))
      for (const session of nextSessions) {
        const oldSession = sessionMap.get(buildSessionKey(session.targetId, session.sessionType))
        if (oldSession && oldSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {
          flashSession(session.targetId, session.sessionType)
        }
      }
    }

    const draftSessions = sessions.value.filter(existing =>
      existing.isDraft
      && shouldPreserveDraftSession(existing)
      && !nextSessions.some((item: ChatSession) => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(existing.targetId, existing.sessionType))
    )
    sessions.value = [...draftSessions, ...nextSessions]
    sortSessions()

    const activeSingleTargetId = currentSessionType.value === SESSION_TYPE_SINGLE ? currentTargetId.value : null
    if (
      activeSingleTargetId
      && !sessions.value.some(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(activeSingleTargetId, currentSessionType.value))
      && !isFriendTarget(activeSingleTargetId)
    ) {
      await closeUnavailableSingleSession(activeSingleTargetId, { notify: true })
    }

    const activeGroupTargetId = currentSessionType.value === SESSION_TYPE_GROUP ? currentTargetId.value : null
    if (
      activeGroupTargetId
      && !sessions.value.some(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(activeGroupTargetId, SESSION_TYPE_GROUP))
    ) {
      await closeUnavailableGroupSession(activeGroupTargetId, { notify: true })
    }

  } catch (error) {
    console.error('loadSessions error:', error)
  }
}

async function loadGroupDetail(groupId: string | number, syncDraft = true) {
  try {
    const response: any = await groupApi.detail(groupId)
    const detail = response.data || null
    applyGroupDetail(detail, syncDraft)
    return true
  } catch (error) {
    console.error('loadGroupDetail error:', error)
    if (isUnavailableConversationError(error)) {
      await closeUnavailableGroupSession(groupId, { notify: true })
      return false
    }
    message.error('获取群详情失败')
    return false
  }
}

async function selectSession(
  session: ChatSession,
  syncRoute = true,
  options: {
    targetMessageId?: string
  } = {}
) {
  allowInitialHomeSessionAutoSelect = false
  const nextSessionKey = buildSessionKey(session.targetId, Number(session.sessionType || SESSION_TYPE_SINGLE))
  const currentSessionKey = currentTargetId.value
    ? buildSessionKey(currentTargetId.value, currentSessionType.value)
    : null
  if (showGroupDrawer.value && currentSessionKey && currentSessionKey !== nextSessionKey) {
    const closed = await closeGroupDrawer()
    if (!closed) {
      return
    }
  }

  currentTargetId.value = String(session.targetId)
  currentSessionType.value = Number(session.sessionType || SESSION_TYPE_SINGLE)
  showMenu.value = false
  wasAtBottom = true
  clearMentionBannerQueue()
  clearActiveJumpHighlight()

  if (currentSessionType.value === SESSION_TYPE_GROUP) {
    const detailLoaded = await loadGroupDetail(session.targetId)
    if (!detailLoaded) {
      return
    }
  } else {
    applyGroupDetail(null)
    showGroupDrawer.value = false
  }

  await loadMessages(String(session.targetId), currentSessionType.value, {
    unreadCountSnapshot: Number(session.unreadCount || 0),
    preferMentionScroll: Number(session.sessionType || SESSION_TYPE_SINGLE) === SESSION_TYPE_GROUP,
    targetMessageId: options.targetMessageId || ''
  })

  if (syncRoute) {
    await router.replace({
      path: `/chat/${session.targetId}`,
      query: currentSessionType.value === SESSION_TYPE_GROUP ? { sessionType: String(SESSION_TYPE_GROUP) } : {}
    })
  }
}

async function initializeRouteSession() {
  if (!initialized.value) {
    return
  }

  const routeTargetId = route.params.targetId ? String(route.params.targetId) : ''
  const routeSessionType = Number(route.query.sessionType || SESSION_TYPE_SINGLE)
  const routeMessageId = getRouteMessageId()

  if (!routeTargetId) {
    if (allowInitialHomeSessionAutoSelect && !currentTargetId.value && sessions.value.length > 0) {
      await selectSession(sessions.value[0], false)
    }
    return
  }

  const existingSession = sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(routeTargetId, routeSessionType))
  if (existingSession) {
    if (
      routeMessageId
      || !currentTargetId.value
      || buildSessionKey(currentTargetId.value, currentSessionType.value) !== buildSessionKey(routeTargetId, routeSessionType)
    ) {
      await selectSession(existingSession, false, { targetMessageId: routeMessageId })
    }
    return
  }

  if (routeSessionType === SESSION_TYPE_GROUP) {
    try {
      const response: any = await groupApi.detail(routeTargetId)
      const detail = response.data
      if (!detail) {
        return
      }
      const draftSession: ChatSession = {
        targetId: routeTargetId,
        sessionType: SESSION_TYPE_GROUP,
        targetNickname: detail.groupName,
        targetAvatar: detail.groupAvatar || '',
        lastMessage: '',
        lastMessageTime: detail.createTime || '',
        unreadCount: 0,
        memberCount: detail.memberCount,
        myRole: detail.myRole,
        notice: detail.notice || '',
        noticeUnread: Boolean(detail.noticeUnread),
        muted: detail.muted,
        muteTime: detail.muteTime,
        isDraft: true
      }
      sessions.value = [draftSession, ...sessions.value]
      await selectSession(draftSession, false, { targetMessageId: routeMessageId })
    } catch (error: any) {
      if (isUnavailableConversationError(error)) {
        await closeUnavailableGroupSession(routeTargetId, { notify: true })
        return
      }
      message.error('打开群聊失败')
    }
    return
  }

  try {
    if (!isFriendTarget(routeTargetId)) {
      await closeUnavailableSingleSession(routeTargetId, { notify: true })
      return
    }
    const response: any = await userApi.getUser(routeTargetId)
    const user = response.data
    if (!user) {
      return
    }
    const draftSession: ChatSession = {
      targetId: routeTargetId,
      sessionType: SESSION_TYPE_SINGLE,
      targetNickname: user.nickname,
      targetUsername: user.username,
      targetAvatar: user.avatar || '',
      lastMessage: '',
      lastMessageTime: '',
      unreadCount: 0,
      isDraft: true
    }
    sessions.value = [draftSession, ...sessions.value]
    await selectSession(draftSession, false, { targetMessageId: routeMessageId })
  } catch (error) {
    message.error('打开会话失败')
  }
}

async function markCurrentSessionRead(targetId = currentTargetId.value, sessionType = currentSessionType.value) {
  if (!targetId) {
    return
  }
  await sendChatCommand('MARK_READ', { targetId, sessionType }).catch(() => undefined)
}

async function loadMessages(
  targetId: string,
  sessionType: number,
  options: {
    unreadCountSnapshot?: number
    preferMentionScroll?: boolean
    targetMessageId?: string
  } = {}
) {
  loadingMessages.value = true
  let skipPostLoadHandling = false
  try {
    const targetMessageId = options.targetMessageId ? String(options.targetMessageId) : ''
    let nextMessages: DisplayMessage[] = []
    if (targetMessageId) {
      const aggregatedMessages = new Map<string, DisplayMessage>()
      const pageSize = 100
      const maxPages = 20
      let locatedTargetMessage = false
      for (let page = 1; page <= maxPages; page += 1) {
        const response: any = await sendChatCommand('GET_HISTORY', {
          targetId,
          sessionType,
          page,
          size: pageSize
        })
        const pageRawMessages = response.data || []
        for (const item of pageRawMessages) {
          const messageItem = toDisplayMessage(item)
          aggregatedMessages.set(getMessageAnchorKey(messageItem), messageItem)
          if (String(messageItem.id) === targetMessageId || getMessageAnchorKey(messageItem) === targetMessageId) {
            locatedTargetMessage = true
          }
        }
        if (locatedTargetMessage || pageRawMessages.length < pageSize) {
          break
        }
      }
      nextMessages = Array.from(aggregatedMessages.values()).sort(compareDisplayMessages)
    } else {
      const response: any = await sendChatCommand('GET_HISTORY', {
        targetId,
        sessionType
      })
      const rawMessages = response.data || []
      nextMessages = rawMessages.map((item: any) => toDisplayMessage(item))
    }
    const mentionTargets = options.preferMentionScroll
      ? resolveUnreadMentionMessages(nextMessages, Number(options.unreadCountSnapshot || 0))
      : []

    cleanupMessageResources(messages.value)
    messages.value = nextMessages
    resolvedMessageFileUrls.value = {}
    await preloadMessageFileUrls(nextMessages)
    if (mentionTargets.length > 0) {
      setMentionBannerQueue(mentionTargets)
    } else {
      clearMentionBannerQueue()
    }
    await markCurrentSessionRead(targetId, sessionType)
  } catch (error: any) {
    console.error('loadMessages error:', error)
    if (sessionType === SESSION_TYPE_GROUP && isUnavailableConversationError(error)) {
      skipPostLoadHandling = true
      await closeUnavailableGroupSession(targetId, { notify: true })
      return
    }
    cleanupMessageResources(messages.value)
    messages.value = []
    clearMentionBannerQueue()
  } finally {
    loadingMessages.value = false
    if (skipPostLoadHandling) {
      return
    }
    wasAtBottom = true
    await nextTick()
    const targetMessageId = options.targetMessageId ? String(options.targetMessageId) : ''
    const targetMessage = targetMessageId ? findMessageByRouteMessageId(targetMessageId) : null
    if (targetMessage) {
      scrollToMessage(targetMessage, 'auto')
      setActiveJumpHighlight(targetMessage)
      await clearRouteMessageQuery()
    } else if (targetMessageId) {
      message.warning('未找到目标聊天内容，可能已超出当前加载范围')
      await clearRouteMessageQuery()
      if (showMentionBanner.value && activeMentionBannerMessage.value) {
        scrollToActiveMentionMessage('auto')
      } else {
        scrollMessagesToBottom(true)
      }
    } else if (showMentionBanner.value && activeMentionBannerMessage.value) {
      scrollToActiveMentionMessage('auto')
    } else {
      scrollMessagesToBottom(true)
    }
  }
}

async function refreshCurrentSession() {
  showMenu.value = false
  await loadFriends()
  await loadSessions()
  if (!currentTargetId.value) {
    return
  }
  if (isGroupSession.value) {
    const detailLoaded = await loadGroupDetail(currentTargetId.value)
    if (!detailLoaded || !currentTargetId.value) {
      return
    }
  }
  await loadMessages(currentTargetId.value, currentSessionType.value)
  message.success('已刷新')
}

async function handleSend() {
  if (!currentTargetId.value || !inputMessage.value.trim()) {
    return
  }
  if (currentMuted.value) {
    message.warning('你已被禁言，暂时无法发送消息')
    return
  }
  if (sendLock) {
    return
  }

  sendLock = true
  sending.value = true
  try {
    const content = inputMessage.value.trim()
    const outgoingMentions = resolveOutgoingMentions(content)
    const localMessage = createPendingMessage({
      content,
      msgType: MESSAGE_TYPE_TEXT,
      mentionAll: outgoingMentions.mentionAll,
      mentionUserIds: outgoingMentions.mentionUserIds,
      mentionDisplayNames: outgoingMentions.mentionDisplayNames
    })
    inputMessage.value = ''
    closeMentionMenu()
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
    scrollMessagesToBottom(true)
    await executePendingMessage(localMessage, () => sendPendingTextMessage(localMessage, content), '发送失败')
  } finally {
    sending.value = false
    sendLock = false
  }
}

async function handleFileUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !currentTargetId.value) {
    return
  }
  if (currentMuted.value) {
    message.warning('你已被禁言，暂时无法发送文件')
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
    scrollMessagesToBottom(true)
    await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_FILE), '发送文件失败')
  } finally {
    input.value = ''
  }
}

async function handleImageUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !currentTargetId.value) {
    return
  }
  if (currentMuted.value) {
    message.warning('你已被禁言，暂时无法发送图片')
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
    scrollMessagesToBottom(true)
    await executePendingMessage(localMessage, () => sendPendingFileMessage(localMessage, file, MESSAGE_TYPE_IMAGE), '发送图片失败')
  } finally {
    input.value = ''
  }
}

function showMsgMenu(event: MouseEvent, msg: DisplayMessage) {
  if (msg.isSystem) {
    showMsgContextMenu.value = false
    return
  }
  selectedMsg.value = msg
  msgMenuX.value = event.clientX
  msgMenuY.value = event.clientY
  showMsgContextMenu.value = true
}

function canRecallMessage(msg: DisplayMessage | null) {
  if (!msg || msg.isSystem || !msg.isMe || msg.status === MESSAGE_STATUS_RECALLED || !msg.createTime || msg.deliveryStatus !== 'sent') {
    return false
  }
  const messageTime = getDateTimeTimestamp(msg.createTime)
  return Date.now() - messageTime < 2 * 60 * 1000
}

async function handleRecallMessage() {
  if (!selectedMsg.value || !currentTargetId.value) {
    return
  }
  try {
    const recalledMessage = selectedMsg.value
    await sendChatCommand('RECALL_MESSAGE', { messageId: recalledMessage.id })
    showMsgContextMenu.value = false
    selectedMsg.value = null
    upsertMessage({
      ...recalledMessage,
      status: MESSAGE_STATUS_RECALLED,
      readStatus: '已撤回'
    })
    message.success('消息已撤回')
  } catch (error: any) {
    console.error('handleRecallMessage error:', error)
    message.error(error.response?.data?.message || '撤回失败')
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
        message.warning('文件上传完成后才能复制链接')
        return
      }
      const accessUrl = await resolveFileAccessUrl(messageItem.content, true)
      if (!accessUrl) {
        message.error('无法生成访问链接')
        return
      }
      await navigator.clipboard.writeText(accessUrl)
      message.success('访问链接已复制')
      return
    }
    await navigator.clipboard.writeText(messageItem.content)
    message.success('已复制')
  } catch (error) {
    message.error('复制失败')
  }
}

async function copyGroupId(groupId: string | number) {
  try {
    await navigator.clipboard.writeText(String(groupId))
    message.success(`群号 ${groupId} 已复制`)
  } catch (error) {
    message.error('复制群号失败')
  }
}

function openCreateGroupModal() {
  resetCreateGroupForm()
  showCreateGroupModal.value = true
}

function closeCreateGroupModal() {
  showCreateGroupModal.value = false
  resetCreateGroupForm()
}

function resetCreateGroupForm() {
  revokeBlobUrl(createGroupForm.avatarPreview)
  createGroupForm.groupName = ''
  createGroupForm.notice = ''
  createGroupForm.memberIds = []
  createGroupForm.avatarPreview = ''
  createGroupForm.avatarFile = null
  if (groupAvatarInputRef.value) {
    groupAvatarInputRef.value.value = ''
  }
}

function resetGroupProfileDraft() {
  revokeBlobUrl(groupProfileDraft.avatarPreview)
  groupProfileDraft.groupName = ''
  groupProfileDraft.avatarPreview = ''
  groupProfileDraft.avatarFile = null
  if (groupAvatarInputRef.value) {
    groupAvatarInputRef.value.value = ''
  }
}

function syncGroupProfileDraft(detail?: GroupDetail | null) {
  revokeBlobUrl(groupProfileDraft.avatarPreview)
  groupProfileDraft.groupName = detail?.groupName || ''
  groupProfileDraft.avatarPreview = detail?.groupAvatar || ''
  groupProfileDraft.avatarFile = null
  if (groupAvatarInputRef.value) {
    groupAvatarInputRef.value.value = ''
  }
}

function discardGroupDrawerDrafts() {
  noticeDraft.value = groupDetail.value?.notice || ''
  syncGroupProfileDraft(groupDetail.value)
}

function handleGroupAvatarSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  const previewUrl = URL.createObjectURL(file)
  if (showCreateGroupModal.value) {
    revokeBlobUrl(createGroupForm.avatarPreview)
    createGroupForm.avatarFile = file
    createGroupForm.avatarPreview = previewUrl
    return
  }
  if (showGroupDrawer.value && canEditGroupProfile.value) {
    revokeBlobUrl(groupProfileDraft.avatarPreview)
    groupProfileDraft.avatarFile = file
    groupProfileDraft.avatarPreview = previewUrl
    return
  }
  revokeBlobUrl(previewUrl)
}

async function submitCreateGroup() {
  if (!createGroupForm.groupName.trim()) {
    message.warning('请输入群名称')
    return
  }
  if (createGroupForm.memberIds.length === 0) {
    message.warning('请至少选择一位好友')
    return
  }

  creatingGroup.value = true
  try {
    const requestedGroupName = createGroupForm.groupName.trim()
    const requestedNotice = createGroupForm.notice.trim()
    const requestedMemberCount = createGroupForm.memberIds.length + 1
    let groupAvatar = ''
    if (createGroupForm.avatarFile) {
      const uploadResponse: any = await fileApi.uploadAvatar(createGroupForm.avatarFile)
      groupAvatar = uploadResponse.data?.fileUrl || ''
    }

    const response: any = await groupApi.create({
      groupName: requestedGroupName,
      groupAvatar,
      notice: requestedNotice,
      memberIds: createGroupForm.memberIds.map(item => String(item))
    })

    const createdGroup = response.data || {}
    closeCreateGroupModal()
    await loadSessions()

    const groupId = createdGroup.id
    let groupSession = sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(groupId, SESSION_TYPE_GROUP))
    if (!groupSession && groupId) {
      groupSession = upsertSession({
        targetId: groupId,
        sessionType: SESSION_TYPE_GROUP,
        targetNickname: createdGroup.groupName || requestedGroupName,
        targetAvatar: createdGroup.groupAvatar || groupAvatar,
        lastMessage: '',
        lastMessageTime: createdGroup.createTime || '',
        unreadCount: 0,
        memberCount: Number(createdGroup.memberCount || requestedMemberCount),
        myRole: Number(createdGroup.myRole ?? GROUP_ROLE_OWNER),
        notice: createdGroup.notice || requestedNotice,
        noticeUnread: false,
        muted: Boolean(createdGroup.muted),
        muteTime: createdGroup.muteTime || '',
        isDraft: true
      })
    }
    if (groupSession) {
      await selectSession(groupSession)
    }
    message.success(`群聊创建成功，群号：${groupId}`)
  } catch (error: any) {
    console.error('submitCreateGroup error:', error)
    message.error(error.response?.data?.message || '创建群聊失败')
  } finally {
    creatingGroup.value = false
  }
}

async function openGroupDrawer() {
  showMenu.value = false
  if (!currentTargetId.value || !isGroupSession.value) {
    return
  }
  showGroupDrawer.value = true
  await loadGroupDetail(currentTargetId.value)
}

async function openGroupMembersPage() {
  const targetGroupId = groupDetail.value?.id || currentTargetId.value
  if (!targetGroupId || !isGroupSession.value) {
    return
  }
  const closed = await closeGroupDrawer()
  if (!closed) {
    return
  }
  await router.push({
    path: `/groups/${targetGroupId}/members`,
    query: { from: 'chat' }
  })
}

async function closeGroupDrawer(options: { force?: boolean } = {}) {
  if (!options.force && showGroupDrawer.value && hasUnsavedGroupDrawerChanges.value) {
    const confirmed = await openConfirmDialog({
      title: '放弃未保存修改',
      subtitle: '群名称或群公告有未保存内容',
      description: '确认关闭后，将丢弃当前未保存的群资料与群公告修改。',
      cancelText: '继续编辑',
      confirmText: '确认关闭'
    })
    if (!confirmed) {
      return false
    }
  }
  discardGroupDrawerDrafts()
  showGroupDrawer.value = false
  return true
}

function openAddMembersModal() {
  addMembersSelection.value = []
  addMembersMessage.value = ''
  showAddMembersModal.value = true
}

function closeAddMembersModal() {
  showAddMembersModal.value = false
  addMembersSelection.value = []
  addMembersMessage.value = ''
}

async function submitAddMembers() {
  if (!currentTargetId.value || addMembersSelection.value.length === 0) {
    message.warning('请选择要邀请的成员')
    return
  }

  addingMembers.value = true
  try {
    await groupApi.inviteMembers(currentTargetId.value, addMembersSelection.value, addMembersMessage.value.trim())
    closeAddMembersModal()
    message.success('入群邀请已发送')
  } catch (error: any) {
    console.error('submitAddMembers error:', error)
    message.error(error.response?.data?.message || '邀请成员失败')
  } finally {
    addingMembers.value = false
  }
}

async function submitUpdateNotice() {
  if (!currentTargetId.value || !canEditNotice.value) {
    return
  }
  updatingNotice.value = true
  try {
    await groupApi.updateNotice(currentTargetId.value, noticeDraft.value.trim())
    await loadGroupDetail(currentTargetId.value)
    message.success('群公告已更新')
  } catch (error: any) {
    console.error('submitUpdateNotice error:', error)
    message.error(error.response?.data?.message || '更新群公告失败')
  } finally {
    updatingNotice.value = false
  }
}

async function acknowledgeNoticeReminder() {
  if (!currentTargetId.value || currentSessionType.value !== SESSION_TYPE_GROUP || !groupDetail.value) {
    showNoticeReminder.value = false
    return
  }
  acknowledgingNoticeReminder.value = true
  try {
    await groupApi.markNoticeRead(currentTargetId.value)
    const activeSession = currentSession.value
    if (activeSession) {
      upsertSession({
        ...activeSession,
        noticeUnread: false
      })
    }
    applyGroupDetail({
      ...groupDetail.value,
      noticeUnread: false,
      noticeReadTime: groupDetail.value.noticeUpdateTime || groupDetail.value.noticeReadTime
    }, false)
    showNoticeReminder.value = false
  } catch (error: any) {
    console.error('acknowledgeNoticeReminder error:', error)
    message.error(error.response?.data?.message || '确认群公告失败')
  } finally {
    acknowledgingNoticeReminder.value = false
  }
}

async function submitUpdateGroupProfile() {
  if (!currentTargetId.value || !groupDetail.value || !canEditGroupProfile.value) {
    return
  }
  const nextGroupName = groupProfileDraft.groupName.trim()
  if (!nextGroupName) {
    message.warning('请输入群名称')
    return
  }

  updatingGroupProfile.value = true
  try {
    let groupAvatar = groupDetail.value.groupAvatar || ''
    if (groupProfileDraft.avatarFile) {
      const uploadResponse: any = await fileApi.uploadAvatar(groupProfileDraft.avatarFile)
      groupAvatar = uploadResponse.data?.fileUrl || ''
    }
    await groupApi.updateProfile(currentTargetId.value, {
      groupName: nextGroupName,
      groupAvatar
    })
    if (groupDetail.value) {
      applyGroupDetail({
        ...groupDetail.value,
        groupName: nextGroupName,
        groupAvatar
      }, true)
    }
    message.success('群资料已更新')
  } catch (error: any) {
    console.error('submitUpdateGroupProfile error:', error)
    message.error(error.response?.data?.message || '更新群资料失败')
  } finally {
    updatingGroupProfile.value = false
  }
}

function canOperateMember(member: GroupMember) {
  const myId = String(userStore.userId)
  if (!canManageMembers.value || String(member.userId) === myId) {
    return false
  }
  if (currentGroupRole.value === GROUP_ROLE_OWNER) {
    return member.role !== GROUP_ROLE_OWNER
  }
  return member.role === GROUP_ROLE_MEMBER
}

function canToggleAdmin(member: GroupMember) {
  return currentGroupRole.value === GROUP_ROLE_OWNER
    && String(member.userId) !== String(userStore.userId)
    && member.role !== GROUP_ROLE_OWNER
}

async function toggleAdminRole(memberItem: GroupMember) {
  if (!currentTargetId.value || !canToggleAdmin(memberItem)) {
    return
  }

  const nextActionText = memberItem.role === GROUP_ROLE_ADMIN ? '取消该成员的管理员身份' : '将该成员设为管理员'
  const confirmed = await openConfirmDialog({
    title: '确认成员权限变更',
    subtitle: nextActionText,
    description: `确认${nextActionText}吗？`,
    confirmText: '确认操作'
  })
  if (!confirmed) {
    return
  }

  try {
    if (memberItem.role === GROUP_ROLE_ADMIN) {
      await groupApi.removeAdmin(currentTargetId.value, memberItem.userId)
      message.success('已取消管理员')
    } else {
      await groupApi.setAdmin(currentTargetId.value, memberItem.userId)
      message.success('已设为管理员')
    }
  } catch (error: any) {
    console.error('toggleAdminRole error:', error)
    message.error(error.response?.data?.message || '管理员操作失败')
  }
}

async function toggleMuteMember(memberItem: GroupMember) {
  if (!currentTargetId.value) {
    return
  }
  try {
    if (memberItem.muted) {
      await groupApi.unmuteMember(currentTargetId.value, memberItem.userId)
      message.success('已解除禁言')
    } else {
      openMuteMemberModal(memberItem)
      return
    }
  } catch (error: any) {
    console.error('toggleMuteMember error:', error)
    message.error(error.response?.data?.message || '操作失败')
  }
}

function openMuteMemberModal(memberItem: GroupMember) {
  muteTargetMember.value = memberItem
  muteMinutesInput.value = '10'
  showMuteMemberModal.value = true
}

function resetMuteMemberModal() {
  showMuteMemberModal.value = false
  muteTargetMember.value = null
  muteMinutesInput.value = '10'
}

function closeMuteMemberModal() {
  if (mutingMember.value) {
    return
  }
  resetMuteMemberModal()
}

async function submitMuteMember() {
  if (!currentTargetId.value || !muteTargetMember.value) {
    return
  }
  const muteMinutes = Number(muteMinutesInput.value)
  if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {
    message.warning('请输入 1 到 43200 之间的整数分钟数')
    return
  }

  mutingMember.value = true
  try {
    await groupApi.muteMember(currentTargetId.value, muteTargetMember.value.userId, muteMinutes)
    resetMuteMemberModal()
    message.success('已设置禁言')
  } catch (error: any) {
    console.error('submitMuteMember error:', error)
    message.error(error.response?.data?.message || '设置禁言失败')
  } finally {
    mutingMember.value = false
  }
}

async function handleRemoveMember(memberItem: GroupMember) {
  if (!currentTargetId.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认移出成员',
    subtitle: memberItem.nickname || memberItem.username || '该成员',
    description: `确认将 ${memberItem.nickname || memberItem.username} 移出群聊吗？`,
    confirmText: '确认移出',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }
  try {
    await groupApi.removeMember(currentTargetId.value, memberItem.userId)
    message.success('成员已移出群聊')
  } catch (error: any) {
    console.error('handleRemoveMember error:', error)
    message.error(error.response?.data?.message || '移除成员失败')
  }
}

async function handleDissolveGroup() {
  if (!currentTargetId.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认解散群聊',
    subtitle: '该操作不可恢复',
    description: '解散后，当前群聊与会话记录入口将被移除。',
    confirmText: '确认解散',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }
  try {
    await groupApi.dissolve(currentTargetId.value)
    await closeGroupDrawer({ force: true })
    removeSessionByTarget(currentTargetId.value, SESSION_TYPE_GROUP)
    resetCurrentConversationState()
    await router.replace('/chat')
    message.success('群聊已解散')
  } catch (error: any) {
    console.error('handleDissolveGroup error:', error)
    message.error(error.response?.data?.message || '解散群聊失败')
  }
}

async function handleLeaveGroup() {
  if (!currentTargetId.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认退出群聊',
    subtitle: currentSessionName.value || '当前群聊',
    description: '退出后你将不再接收该群消息，需要重新被邀请才能再次加入。',
    confirmText: '确认退出',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }
  try {
    await groupApi.leaveGroup(currentTargetId.value)
    await closeGroupDrawer({ force: true })
    removeSessionByTarget(currentTargetId.value, SESSION_TYPE_GROUP)
    resetCurrentConversationState()
    await router.replace('/chat')
    message.success('已退出群聊')
  } catch (error: any) {
    console.error('handleLeaveGroup error:', error)
    message.error(error.response?.data?.message || '退出群聊失败')
  }
}

function openTransferOwnerModal() {
  transferOwnerSelection.value = null
  showTransferOwnerModal.value = true
}

function closeTransferOwnerModal() {
  showTransferOwnerModal.value = false
  transferOwnerSelection.value = null
}

async function submitTransferOwner() {
  if (!currentTargetId.value || !transferOwnerSelection.value) {
    return
  }
  const member = groupDetail.value?.members.find(m => String(m.userId) === String(transferOwnerSelection.value))
  const memberName = member?.nickname || member?.username || '该成员'
  const confirmed = await openConfirmDialog({
    title: '确认转让群主',
    subtitle: memberName,
    description: `确认将群主转让给 ${memberName} 吗？转让后你将变为普通成员。`,
    confirmText: '确认转让',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }
  transferringOwner.value = true
  try {
    await groupApi.transferOwner(currentTargetId.value, transferOwnerSelection.value)
    closeTransferOwnerModal()
    message.success('群主已转让')
  } catch (error: any) {
    console.error('submitTransferOwner error:', error)
    message.error(error.response?.data?.message || '转让群主失败')
  } finally {
    transferringOwner.value = false
  }
}

watch(() => route.fullPath, () => {
  initializeRouteSession()
})

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('keydown', handleWindowKeydown)
  connectChatSocket()
  loadingSessions.value = true
  await Promise.all([loadFriends(), loadSessions()])
  initialized.value = true
  loadingSessions.value = false
  await initializeRouteSession()
})

onUnmounted(() => {
  clearActiveJumpHighlight()
  disconnectChatSocket()
  cleanupMessageResources()
  resetCreateGroupForm()
  resetGroupProfileDraft()
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('keydown', handleWindowKeydown)
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

.session-panel {
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
}

.create-group-btn,
.primary-btn,
.secondary-btn,
.text-btn,
.mini-btn,
.danger-action-btn {
  transition: var(--linkx-transition-fast);
}

.create-group-btn,
.primary-btn {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: var(--linkx-primary);
  color: white;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.create-group-btn:hover,
.primary-btn:hover,
.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.session-search {
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
  font-size: 16px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 36px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 13px;
  transition: var(--linkx-transition);
  outline: none;
}

.search-input::placeholder,
.session-time,
.session-muted,
.chat-subtitle,
.msg-time,
.msg-status,
.modal-subtitle,
.drawer-subtitle,
.member-meta,
.member-picker-meta,
.section-hint {
  color: var(--linkx-text-muted);
}

.search-input:focus,
.modal-input:focus,
.modal-textarea:focus,
.drawer-textarea:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.search-clear {
  position: absolute;
  right: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
  cursor: pointer;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--linkx-radius);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.session-item:hover {
  background: var(--linkx-bg-hover);
}

.session-item.active {
  background: var(--linkx-bg-active);
}

.session-item.flash {
  animation: flashHighlight 2s ease-out;
}

@keyframes flashHighlight {
  0%,
  100% {
    background: transparent;
  }
  15%,
  45%,
  75% {
    background: var(--linkx-primary-glow);
  }
}

.session-avatar,
.chat-avatar,
.msg-avatar,
.member-avatar,
.member-picker-avatar,
.group-summary-avatar,
.group-avatar-selector {
  overflow: hidden;
}

.session-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.session-avatar.group,
.chat-avatar.group,
.msg-avatar.group,
.group-summary-avatar,
.group-avatar-selector {
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.session-type-badge,
.session-tag,
.chat-session-tag,
.member-role-tag,
.member-self-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-full);
  font-size: 11px;
  font-weight: 700;
}

.session-type-badge {
  position: absolute;
  left: -2px;
  bottom: -2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: rgba(0, 0, 0, 0.56);
  color: white;
}

.session-type-badge.group,
.chat-session-tag,
.member-role-tag.owner {
  background: rgba(77, 107, 255, 0.18);
  color: #90a7ff;
}

.session-tag.notice {
  background: rgba(0, 214, 143, 0.14);
  color: var(--linkx-primary);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: var(--linkx-error);
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: var(--linkx-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.online-indicator {
  position: absolute;
  right: -2px;
  bottom: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid var(--linkx-bg-card);
  background: #64748b;
}

.online-indicator.active {
  background: var(--linkx-primary);
}

.session-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-header,
.section-title-row,
.chat-header,
.chat-name-row,
.modal-header,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-title-row,
.session-preview-row,
.member-main,
.member-name-row,
.member-badges {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.session-name,
.chat-name,
.modal-title,
.drawer-title,
.group-summary-name,
.member-name,
.member-picker-name {
  color: var(--linkx-text);
  font-weight: 700;
}

.session-name,
.member-name,
.member-picker-name {
  font-size: 14px;
}

.member-name {
  line-height: 1.35;
}

.session-tag {
  padding: 2px 8px;
  background: rgba(77, 107, 255, 0.12);
  color: #90a7ff;
}

.session-preview,
.chat-empty-text,
.empty-subtitle,
.group-summary-meta,
.group-summary-role {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.session-preview {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

.chat-header {
  min-height: 64px;
  padding: 0 20px;
  background: var(--linkx-bg-card);
  border-bottom: 1px solid var(--linkx-border);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.chat-avatar,
.msg-avatar,
.member-avatar,
.member-picker-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--linkx-radius-sm);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  flex-shrink: 0;
}

.chat-avatar {
  width: 40px;
  height: 40px;
}

.chat-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.chat-name-row {
  justify-content: flex-start;
  gap: 8px;
}

.chat-name {
  font-size: 15px;
}

.chat-subtitle {
  max-width: 540px;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-session-tag,
.member-role-tag,
.member-self-tag {
  min-height: 20px;
  padding: 0 8px;
  line-height: 20px;
  letter-spacing: 0.01em;
}

.member-role-tag,
.member-self-tag {
  border: 1px solid transparent;
}

.member-role-tag.owner {
  background: rgba(77, 107, 255, 0.14);
  border-color: rgba(144, 167, 255, 0.16);
  color: #9cb0ff;
}

.member-role-tag.admin {
  background: rgba(0, 214, 143, 0.1);
  border-color: rgba(55, 216, 170, 0.14);
  color: #43ddb1;
}

.member-role-tag.member,
.member-self-tag {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.08);
  color: var(--linkx-text-secondary);
}

.chat-header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.header-action-btn,
.toolbar-btn,
.modal-close,
.secondary-btn,
.text-btn,
.mini-btn {
  border: none;
  background: transparent;
}

.header-action-btn,
.toolbar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.header-action-btn:hover,
.toolbar-btn:hover,
.modal-close:hover,
.secondary-btn:hover,
.text-btn:hover,
.mini-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.dropdown-menu,
.msg-context-menu,
.emoji-picker,
.mention-menu {
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  box-shadow: var(--linkx-shadow-lg);
}

.dropdown-menu {
  position: absolute;
  top: 42px;
  right: 0;
  min-width: 160px;
  z-index: 100;
  overflow: hidden;
}

.dropdown-item,
.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.dropdown-item:hover,
.context-menu-item:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.chat-state-bar {
  padding: 10px 20px;
  background: rgba(255, 170, 0, 0.08);
  border-bottom: 1px solid rgba(255, 170, 0, 0.2);
  color: #ffcc66;
  font-size: 12px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mention-banner {
  position: sticky;
  top: 0;
  z-index: 8;
  align-self: center;
  width: min(100%, 560px);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid rgba(255, 170, 0, 0.28);
  border-radius: var(--linkx-radius);
  background: color-mix(in srgb, rgba(255, 170, 0, 0.18) 70%, var(--linkx-bg-card));
  color: var(--linkx-text);
  box-shadow: var(--linkx-shadow-md);
  cursor: pointer;
}

.mention-banner-badge {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: var(--linkx-radius-full);
  background: rgba(255, 170, 0, 0.18);
  color: #c97b00;
  font-size: 12px;
  font-weight: 700;
}

.mention-banner-text {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mention-banner-action {
  flex-shrink: 0;
  color: var(--linkx-primary);
  font-size: 12px;
  font-weight: 600;
}

.mention-banner-close {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
}

.chat-loading,
.empty-state,
.chat-empty,
.chat-welcome,
.panel-placeholder.drawer-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chat-loading {
  flex: 1;
  gap: 8px;
  color: var(--linkx-text-muted);
}

.loading-spinner,
.send-loading {
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
}

.send-loading {
  width: 16px;
  height: 16px;
  border: 2px solid var(--linkx-text-muted);
  border-top-color: transparent;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  transition: background-color 0.25s ease, box-shadow 0.25s ease, padding 0.25s ease;
}

.message-row.located {
  padding: 8px 10px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--linkx-primary) 12%, transparent);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--linkx-primary) 22%, transparent);
}

.message-row.system {
  justify-content: center;
}

.message-row.self {
  flex-direction: row-reverse;
}

.msg-avatar.self {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
}

.msg-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-row.self .msg-content {
  align-items: flex-end;
}

.msg-sender {
  margin-left: 4px;
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.system-message {
  max-width: 72%;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
  word-break: break-word;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: var(--linkx-radius);
  background: var(--linkx-bubble-other);
  color: var(--linkx-bubble-other-text);
  box-shadow: var(--linkx-shadow-sm);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-bubble.self {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-row:not(.self) .msg-bubble {
  border-bottom-left-radius: 4px;
}

.msg-bubble.recalled {
  opacity: 0.6;
  background: var(--linkx-bg-hover) !important;
}

.mention-text {
  color: #e45252;
  font-weight: 600;
}

.recalled-text {
  font-style: italic;
  color: var(--linkx-text-muted);
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 4px;
}

.msg-meta.self {
  justify-content: flex-end;
  margin-right: 4px;
}

.msg-status {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 12px;
}

.msg-status.read {
  color: var(--linkx-primary);
}

.msg-status.pending {
  color: var(--linkx-text-muted);
}

.msg-status.retry {
  color: var(--linkx-error);
  cursor: pointer;
}

.msg-image {
  max-width: 240px;
  max-height: 240px;
  border-radius: var(--linkx-radius-sm);
  cursor: pointer;
}

.msg-file {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  color: inherit;
  text-decoration: none;
  border: none;
  background: transparent;
  padding: 0;
  text-align: left;
  font: inherit;
}

.msg-file.pending {
  cursor: default;
  opacity: 0.82;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-size {
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.msg-context-menu {
  position: fixed;
  min-width: 120px;
  z-index: 1000;
  overflow: hidden;
}

.chat-input-area {
  position: relative;
  padding: 12px 20px 16px;
  background: var(--linkx-bg-card);
  border-top: 1px solid var(--linkx-border);
}

.input-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.toolbar-btn:disabled,
.send-btn:disabled,
.input-container.disabled,
.create-group-btn:disabled,
.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.emoji-wrapper {
  position: relative;
}

.emoji-picker {
  position: absolute;
  bottom: 40px;
  left: 0;
  width: 320px;
  padding: 12px;
  z-index: 100;
}

.mention-menu {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: calc(100% - 8px);
  z-index: 120;
  overflow: hidden;
}

.mention-menu-list {
  max-height: 240px;
  overflow-y: auto;
}

.mention-menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border: none;
  background: transparent;
  color: var(--linkx-text-secondary);
  text-align: left;
  cursor: pointer;
}

.mention-menu-item:hover,
.mention-menu-item.active {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.mention-menu-name {
  color: inherit;
  font-size: 13px;
  font-weight: 600;
}

.mention-menu-meta,
.mention-menu-empty {
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.mention-menu-empty {
  padding: 12px 14px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-sm);
  font-size: 20px;
  cursor: pointer;
}

.emoji-item:hover {
  background: var(--linkx-bg-hover);
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 8px 12px;
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
}

.message-input {
  flex: 1;
  min-height: 24px;
  max-height: 120px;
  padding: 0;
  background: transparent;
  border: none;
  outline: none;
  box-shadow: none;
  resize: none;
  color: var(--linkx-text);
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
}

.message-input::placeholder {
  color: var(--linkx-text-muted);
}

.message-input:focus,
.message-input:focus-visible {
  outline: none;
  box-shadow: none;
}

.send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-muted);
  cursor: pointer;
}

.send-btn.active {
  background: var(--linkx-primary);
  color: white;
  box-shadow: 0 2px 8px var(--linkx-primary-glow);
}

.empty-state,
.chat-empty,
.chat-welcome {
  flex: 1;
  gap: 12px;
  color: var(--linkx-text-muted);
}

.empty-icon,
.chat-empty-icon,
.welcome-icon {
  opacity: 0.2;
}

.empty-title,
.chat-empty-title,
.welcome-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.welcome-title {
  font-size: 24px;
}

.overlay-panel,
.drawer-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.overlay-panel,
.drawer-overlay {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal-card,
.group-drawer {
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
}

.modal-card {
  border-radius: var(--linkx-radius-lg);
  max-width: calc(100vw - 24px);
  max-height: calc(100vh - 24px);
}
.secondary-btn,
.danger-action-btn {
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.secondary-btn {
  background: transparent;
  border: 1px solid var(--linkx-border);
  color: var(--linkx-text);
}

.secondary-btn,
.primary-btn {
  height: 40px;
  padding: 0 18px;
}

.overlay-panel {
  z-index: 2200;
  align-items: flex-start;
  padding: 56px 24px 24px;
}

.modal-card {
  width: min(720px, 100%);
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.create-group-modal {
  width: min(640px, 100%);
  max-height: min(720px, calc(100vh - 56px));
  overflow-y: auto;
}

.mute-member-modal {
  width: min(420px, 100%);
}

.add-members-modal {
  width: min(560px, 100%);
}

.modal-title,
.drawer-title {
  font-size: 18px;
}

.modal-header {
  margin-bottom: 20px;
}

.form-section,
.drawer-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 18px;
}

.form-label,
.drawer-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.modal-input,
.modal-textarea,
.drawer-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  resize: vertical;
  outline: none;
  transition: var(--linkx-transition-fast);
  font-family: inherit;
}

.readonly-field {
  width: 100%;
  min-height: 46px;
  padding: 12px 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  display: flex;
  align-items: center;
  line-height: 1.5;
}

.group-avatar-selector,
.group-summary-avatar {
  width: 72px;
  height: 72px;
  border-radius: var(--linkx-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  font-weight: 700;
  cursor: pointer;
}

.create-group-modal .modal-header {
  margin-bottom: 16px;
}

.create-group-modal .form-section {
  gap: 8px;
  margin-bottom: 14px;
}

.create-group-modal .group-avatar-selector {
  width: 60px;
  height: 60px;
  font-size: 24px;
}

.create-group-modal .modal-input,
.create-group-modal .modal-textarea {
  padding: 10px 12px;
}

.create-group-modal .member-picker-list {
  max-height: 220px;
}

.create-group-modal .member-picker-item {
  padding: 10px 12px;
}

.create-group-modal .member-picker-avatar {
  width: 32px;
  height: 32px;
}

.create-group-modal .modal-actions {
  margin-top: 4px;
}

.group-avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.member-picker-list,
.member-manage-list,
.drawer-body {
  overflow: auto;
}

.member-picker-list {
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  max-height: 260px;
}

.member-picker-list.compact {
  max-height: 360px;
}

.member-picker-item,
.member-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
}

.member-picker-item:not(:last-child),
.member-row:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.member-picker-item {
  cursor: pointer;
}

.member-picker-info,
.group-summary-info,
.member-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-summary-info,
.member-picker-info,
.member-info {
  flex: 1;
  min-width: 0;
}

.section-count,
.panel-placeholder {
  font-size: 13px;
  color: var(--linkx-text-muted);
}

.panel-placeholder {
  padding: 18px 14px;
  border: 1px dashed var(--linkx-border);
  border-radius: var(--linkx-radius);
}

.group-drawer {
  width: min(420px, 100%);
  height: 100%;
  margin-left: auto;
  border-radius: 0;
  display: flex;
  flex-direction: column;
}

.drawer-overlay {
  z-index: 1800;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--linkx-border);
}

.drawer-body {
  flex: 1;
  padding: 20px;
}

.group-summary-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  margin-bottom: 18px;
  box-shadow: none;
}

.group-summary-avatar {
  position: relative;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: none;
}

.group-summary-avatar.editable {
  cursor: pointer;
}

.group-summary-avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.52);
  color: white;
  font-size: 12px;
  font-weight: 600;
  opacity: 0;
  transition: var(--linkx-transition-fast);
}

.group-summary-avatar.editable:hover .group-summary-avatar-mask {
  opacity: 1;
}

.group-summary-info {
  gap: 8px;
}

.group-profile-name-row {
  display: flex;
  align-items: flex-start;
  width: 100%;
  gap: 10px;
  min-width: 0;
}

.group-profile-name-shell {
  flex: 1;
  min-width: 0;
  padding: 0;
  border: none;
  border-radius: 0;
  background: transparent;
}

.group-profile-name-input {
  width: 100%;
  min-width: 0;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-md);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.group-profile-name-input:disabled {
  background: transparent;
  border-color: transparent;
  cursor: default;
}

.group-profile-name-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
  outline: none;
}

.group-profile-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--linkx-text-muted);
}

.group-profile-save-btn {
  min-width: 84px;
  height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(77, 107, 255, 0.12);
  background: rgba(77, 107, 255, 0.06);
  color: var(--linkx-primary);
}

.group-profile-save-btn:hover {
  background: rgba(77, 107, 255, 0.12);
  color: #a8b8ff;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-summary-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}

.group-summary-id-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.group-summary-id {
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.group-summary-pill {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: var(--linkx-radius-full);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 12px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.group-summary-pill.role {
  color: #9cb0ff;
  background: rgba(77, 107, 255, 0.1);
  border-color: rgba(77, 107, 255, 0.16);
}

.text-btn {
  padding: 6px 10px;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-primary);
  cursor: pointer;
  flex-shrink: 0;
  white-space: nowrap;
}

.text-btn:disabled {
  color: var(--linkx-text-muted);
  cursor: not-allowed;
  opacity: 0.72;
}

.text-btn:disabled:hover {
  background: transparent;
  color: var(--linkx-text-muted);
}

.group-id-copy-btn {
  padding: 4px 10px;
  border: 1px solid rgba(77, 107, 255, 0.14);
  background: rgba(77, 107, 255, 0.08);
}

.member-actions {
  display: flex;
  flex-wrap: nowrap;
  justify-content: flex-end;
  gap: 6px;
  align-self: center;
  margin-left: auto;
  flex-shrink: 0;
}

.mini-btn,
.danger-action-btn {
  padding: 6px 10px;
  border-radius: var(--linkx-radius-sm);
  border: 1px solid var(--linkx-border);
  cursor: pointer;
  color: var(--linkx-text);
  background: transparent;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
  white-space: nowrap;
  transition: var(--linkx-transition-fast);
}

.mini-btn:hover {
  border-color: rgba(77, 107, 255, 0.22);
  background: rgba(77, 107, 255, 0.08);
  color: #9cb0ff;
}

.mini-btn.danger,
.danger-action-btn {
  border: 1px solid rgba(255, 107, 107, 0.18);
  color: white;
  background: var(--linkx-error);
}

.mini-btn.danger:hover,
.danger-action-btn:hover {
  border-color: rgba(255, 107, 107, 0.22);
  background: #ff5f5f;
}

.danger-actions {
  display: flex;
  gap: 12px;
}

.danger-action-btn {
  flex: 1;
  min-width: 0;
  width: auto;
}

.member-main {
  flex: 1;
  min-width: 0;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 12px;
}

.member-name-row {
  flex-wrap: wrap;
  justify-content: flex-start;
  row-gap: 6px;
}

.member-badges {
  flex-wrap: wrap;
  gap: 6px;
}

.member-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  line-height: 1.4;
  margin-top: 2px;
}

.member-username,
.member-mute-text {
  white-space: normal;
  word-break: break-word;
}

.member-username {
  font-size: 12px;
}

.member-mute-text {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #f0b56a;
  font-size: 12px;
}

.member-mute-text::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  flex-shrink: 0;
}

@media (max-width: 1100px) {
  .session-panel {
    width: 280px;
  }

  .chat-subtitle {
    max-width: 300px;
  }
}

@media (max-width: 820px) {
  .content-area {
    flex-direction: column;
  }

  .session-panel {
    width: 100%;
    min-width: 0;
    max-height: 38%;
    border-right: none;
    border-bottom: 1px solid var(--linkx-border);
  }

  .chat-header {
    padding: 12px 16px;
  }

  .chat-messages {
    padding: 16px;
  }

  .chat-input-area {
    padding: 10px 16px 14px;
  }

  .msg-content {
    max-width: 72%;
  }
}

@media (max-width: 640px) {
  .panel-header,
  .session-search,
  .chat-header,
  .chat-state-bar,
  .chat-messages,
  .chat-input-area,
  .drawer-header,
  .drawer-body {
    padding-left: 12px;
    padding-right: 12px;
  }

  .session-item,
  .member-picker-item,
  .member-row {
    padding: 10px 12px;
  }

  .msg-content {
    max-width: 82%;
  }

  .message-row {
    gap: 8px;
  }

  .modal-card {
    width: calc(100vw - 20px);
    padding: 20px 16px;
  }

  .modal-actions,
  .danger-actions {
    flex-wrap: wrap;
  }

  .secondary-btn,
  .primary-btn,
  .danger-action-btn {
    width: 100%;
  }

  .member-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .group-profile-name-row {
    align-items: stretch;
    flex-direction: column;
  }

  .group-drawer {
    width: 100%;
  }

  .emoji-picker {
    width: min(320px, calc(100vw - 24px));
    left: 0;
  }
}
</style>
