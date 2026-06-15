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
            <div v-if="session.unreadCount > 0" class="unread-badge">
              {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
            </div>
          </div>
          <div class="session-info">
            <div class="session-header">
              <div class="session-title-row">
                <span class="session-name">{{ session.targetNickname }}</span>
                <span v-if="session.sessionType === SESSION_TYPE_GROUP" class="session-tag">{{ session.memberCount || 0 }}人</span>
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
                  单聊会话
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
              v-for="(msg, index) in messages"
              :key="msg.id || index"
              class="message-row"
              :class="{ self: msg.isMe, system: msg.isSystem }"
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
                <div class="msg-bubble" :class="{ self: msg.isMe, recalled: msg.status === MESSAGE_STATUS_RECALLED }" @contextmenu.prevent="showMsgMenu($event, msg)">
                  <template v-if="msg.status === MESSAGE_STATUS_RECALLED">
                    <span class="recalled-text">[消息已撤回]</span>
                  </template>
                  <template v-else-if="msg.msgType === MESSAGE_TYPE_IMAGE">
                    <img :src="msg.content" class="msg-image" @click="previewImage(msg.content)" />
                  </template>
                  <template v-else-if="msg.msgType === MESSAGE_TYPE_FILE">
                    <a :href="msg.content" target="_blank" class="msg-file" @click.prevent="downloadFile(msg)">
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
                    {{ msg.content }}
                  </template>
                </div>
                <div class="msg-meta" :class="{ self: msg.isMe }">
                  <span class="msg-time">{{ msg.time }}</span>
                  <span v-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && !isGroupSession" class="msg-status" :class="{ read: msg.readStatus === '已读' }">
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
              @keydown.enter.exact.prevent="handleSend"
              @input="autoResize"
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

  <Teleport to="body">
    <div v-if="showImagePreview" class="image-preview-overlay" @click.self="closeImagePreview">
      <button class="image-preview-close" type="button" @click="closeImagePreview">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
      <div class="image-preview-stage">
        <div class="image-preview-container" @click.stop>
          <img :src="previewImageUrl" class="image-preview-img" />
        </div>
      </div>
    </div>
  </Teleport>

  <div v-if="showDownloadModal" class="download-overlay" @click.self="showDownloadModal = false">
    <div class="download-dialog">
      <div class="download-header">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--linkx-primary)" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <div class="download-filename">{{ downloadFileName }}</div>
        <div class="download-size">{{ downloadFileSize || '未知大小' }}</div>
      </div>
      <div v-if="downloadProgress < 100" class="download-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: downloadProgress + '%' }"></div>
        </div>
        <div class="progress-text">{{ downloadProgress }}%</div>
      </div>
      <div v-if="downloadProgress >= 100" class="download-actions">
        <button class="download-btn open" @click="openDownloadedFile">打开</button>
        <button class="download-btn save" @click="saveDownloadedFile">保存</button>
      </div>
      <button class="download-cancel" @click="showDownloadModal = false">取消</button>
    </div>
  </div>

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
              <button v-if="canManageMembers" class="text-btn" @click="openAddMembersModal">邀请进群</button>
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
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon, useMessage } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { chatApi, fileApi, friendApi, groupApi, userApi } from '../api/client'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { useConfirmDialog } from '../hooks/useConfirmDialog'
import { useUserStore } from '../stores/user'
import { showNotification } from '../utils/notify'

const SESSION_TYPE_SINGLE = 1
const SESSION_TYPE_GROUP = 2
const MESSAGE_TYPE_TEXT = 0
const MESSAGE_TYPE_IMAGE = 1
const MESSAGE_TYPE_FILE = 2
const MESSAGE_TYPE_SYSTEM = 3
const MESSAGE_STATUS_RECALLED = 1
const GROUP_ROLE_MEMBER = 0
const GROUP_ROLE_ADMIN = 1
const GROUP_ROLE_OWNER = 2

interface FriendItem {
  friendId: string | number
  friendNickname: string
  friendAvatar?: string
  friendUsername: string
}

interface ChatSession {
  id?: string | number
  userId?: string | number
  targetId: string | number
  sessionType: number
  targetNickname: string
  targetUsername?: string
  targetAvatar?: string
  lastMessage?: string
  lastMessageTime?: string
  unreadCount: number
  memberCount?: number
  myRole?: number
  notice?: string
  muted?: boolean
  muteTime?: string
  isDraft?: boolean
}

interface GroupMember {
  userId: string | number
  username: string
  nickname: string
  avatar?: string
  role: number
  muted?: boolean
  muteTime?: string
}

interface GroupDetail {
  id: string | number
  groupName: string
  groupAvatar?: string
  notice?: string
  noticeUpdateTime?: string
  ownerId: string | number
  maxMembers: number
  memberCount: number
  myRole: number
  muted?: boolean
  muteTime?: string
  members: GroupMember[]
}

interface DisplayMessage {
  id: string | number
  isMe: boolean
  isSystem: boolean
  name: string
  fromAvatar?: string
  content: string
  msgType: number
  status: number
  createTime: string
  time: string
  readStatus: string
  fileName?: string
  fileSize?: number
}

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
const initialized = ref(false)

const messagesRef = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const fileInputRef = ref<HTMLInputElement>()
const imageInputRef = ref<HTMLInputElement>()
const groupAvatarInputRef = ref<HTMLInputElement>()
const menuRef = ref<HTMLElement>()
const menuBtnRef = ref<HTMLElement>()
const emojiRef = ref<HTMLElement>()

const msgMenuX = ref(0)
const msgMenuY = ref(0)
const selectedMsg = ref<DisplayMessage | null>(null)
const previewImageUrl = ref('')
const downloadFileName = ref('')
const downloadFileSize = ref('')
const downloadProgress = ref(0)
const downloadFileUrl = ref('')
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
const flashSessionKey = ref<string | null>(null)
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

let refreshTimer: ReturnType<typeof setInterval> | null = null
let previousMessageCount = 0
let wasAtBottom = true
let switchingSession = false

const currentSession = computed(() => {
  if (!currentTargetId.value) {
    return null
  }
  return sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(currentTargetId.value!, currentSessionType.value)) || null
})

const isGroupSession = computed(() => currentSessionType.value === SESSION_TYPE_GROUP)
const currentSessionName = computed(() => groupDetail.value?.groupName || currentSession.value?.targetNickname || '')
const currentSessionAvatar = computed(() => groupDetail.value?.groupAvatar || currentSession.value?.targetAvatar || '')
const currentMemberCount = computed(() => groupDetail.value?.memberCount || currentSession.value?.memberCount || 0)
const currentNotice = computed(() => groupDetail.value?.notice || currentSession.value?.notice || '')
const currentGroupRole = computed(() => groupDetail.value?.myRole ?? currentSession.value?.myRole ?? GROUP_ROLE_MEMBER)
const currentMuted = computed(() => Boolean(groupDetail.value?.muted ?? currentSession.value?.muted))
const currentMuteTimeText = computed(() => formatDateTime(groupDetail.value?.muteTime || currentSession.value?.muteTime || ''))
const canManageMembers = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditGroupProfile = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditNotice = computed(() => isGroupSession.value && currentGroupRole.value >= GROUP_ROLE_ADMIN)
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

const emojis = ['😀', '😂', '🤣', '😍', '🥰', '😘', '😎', '🤔', '😏', '😢', '😭', '😡', '🥳', '😱', '🥺', '😴', '🤗', '😈', '👻', '💀', '🤡', '👽', '🤖', '💩', '❤️', '🔥', '👍', '👎', '👏', '🙏', '💪', '🎉', '🎊', '☕', '🌹', '🍀', '🌈', '☀️', '🌙', '⭐']

function buildSessionKey(targetId: string | number, sessionType: number) {
  return `${sessionType}-${String(targetId)}`
}

function upsertSession(nextSession: ChatSession) {
  const sessionKey = buildSessionKey(nextSession.targetId, nextSession.sessionType)
  const sessionIndex = sessions.value.findIndex(session => buildSessionKey(session.targetId, session.sessionType) === sessionKey)
  if (sessionIndex === -1) {
    sessions.value = [nextSession, ...sessions.value]
    return nextSession
  }

  const mergedSession = {
    ...sessions.value[sessionIndex],
    ...nextSession
  }
  sessions.value.splice(sessionIndex, 1, mergedSession)
  return mergedSession
}

function isCurrentSession(session: ChatSession) {
  if (!currentTargetId.value) {
    return false
  }
  return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(currentTargetId.value, currentSessionType.value)
}

function formatTime(time?: string) {
  if (!time) {
    return ''
  }
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  if (days === 1) {
    return '昨天'
  }
  if (days < 7) {
    const weekdays = ['日', '一', '二', '三', '四', '五', '六']
    return `周${weekdays[date.getDay()]}`
  }
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

function formatDateTime(time?: string) {
  if (!time) {
    return ''
  }
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

function formatSize(bytes?: number) {
  if (!bytes) {
    return ''
  }
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function roleText(role?: number) {
  if (role === GROUP_ROLE_OWNER) {
    return '群主'
  }
  if (role === GROUP_ROLE_ADMIN) {
    return '管理员'
  }
  return '成员'
}

function roleClass(role?: number) {
  if (role === GROUP_ROLE_OWNER) {
    return 'owner'
  }
  if (role === GROUP_ROLE_ADMIN) {
    return 'admin'
  }
  return 'member'
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

function previewImage(url: string) {
  previewImageUrl.value = url
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

function getFileName(url: string) {
  if (!url) {
    return '文件'
  }
  const segments = url.split('/')
  const name = segments[segments.length - 1]
  const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i
  return uuidPattern.test(name) ? '文件' : name
}

function getFileSizeText(msg: DisplayMessage) {
  return formatSize(msg.fileSize)
}

function getMessagePreview(msg: Pick<DisplayMessage, 'content' | 'msgType' | 'status' | 'isSystem'>) {
  if (msg.status === MESSAGE_STATUS_RECALLED) {
    return '[消息已撤回]'
  }
  if (msg.isSystem || msg.msgType === MESSAGE_TYPE_TEXT) {
    return msg.content
  }
  if (msg.msgType === MESSAGE_TYPE_IMAGE) {
    return '[图片]'
  }
  if (msg.msgType === MESSAGE_TYPE_FILE) {
    return '[文件]'
  }
  return '[消息]'
}

function downloadFile(msg: DisplayMessage) {
  downloadFileName.value = msg.fileName || getFileName(msg.content)
  downloadFileSize.value = getFileSizeText(msg)
  downloadFileUrl.value = msg.content
  downloadProgress.value = 0
  showDownloadModal.value = true
  const interval = setInterval(() => {
    downloadProgress.value += Math.random() * 30
    if (downloadProgress.value >= 100) {
      downloadProgress.value = 100
      clearInterval(interval)
    }
  }, 200)
}

function openDownloadedFile() {
  window.open(downloadFileUrl.value, '_blank')
  showDownloadModal.value = false
}

function saveDownloadedFile() {
  const anchor = document.createElement('a')
  anchor.href = downloadFileUrl.value
  anchor.download = downloadFileName.value
  anchor.click()
  showDownloadModal.value = false
}

function insertEmoji(emoji: string) {
  inputMessage.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
  nextTick(autoResize)
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
    const response: any = await chatApi.getSessions()
    const nextSessions = (response.data || []).map((item: any) => ({
      ...item,
      sessionType: Number(item.sessionType || SESSION_TYPE_SINGLE),
      unreadCount: Number(item.unreadCount || 0)
    }))

    if (sessions.value.length > 0) {
      const sessionMap = new Map(sessions.value.map(session => [buildSessionKey(session.targetId, session.sessionType), session]))
      for (const session of nextSessions) {
        const oldSession = sessionMap.get(buildSessionKey(session.targetId, session.sessionType))
        if (oldSession && oldSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {
          flashSessionKey.value = buildSessionKey(session.targetId, session.sessionType)
          setTimeout(() => {
            if (flashSessionKey.value === buildSessionKey(session.targetId, session.sessionType)) {
              flashSessionKey.value = null
            }
          }, 2000)
        }
      }
    }

    const draftSessions = sessions.value.filter(existing => existing.isDraft && !nextSessions.some((item: ChatSession) => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(existing.targetId, existing.sessionType)))
    sessions.value = [...draftSessions, ...nextSessions]

    if (!currentTargetId.value && sessions.value.length > 0 && !route.params.targetId) {
      await selectSession(sessions.value[0], false)
    }
  } catch (error) {
    console.error('loadSessions error:', error)
  }
}

async function loadGroupDetail(groupId: string | number, syncDraft = true) {
  try {
    const response: any = await groupApi.detail(groupId)
    const detail = response.data || null
    groupDetail.value = detail
    if (syncDraft || !isGroupNoticeChanged.value) {
      noticeDraft.value = detail?.notice || ''
    }
    if (syncDraft || !isGroupProfileChanged.value) {
      syncGroupProfileDraft(detail)
    }

    const sessionKey = buildSessionKey(groupId, SESSION_TYPE_GROUP)
    const session = sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === sessionKey)
    if (session && detail) {
      session.targetNickname = detail.groupName
      session.targetAvatar = detail.groupAvatar || ''
      session.memberCount = detail.memberCount
      session.notice = detail.notice || ''
      session.myRole = detail.myRole
      session.muted = detail.muted
      session.muteTime = detail.muteTime
    }
  } catch (error) {
    console.error('loadGroupDetail error:', error)
    message.error('获取群详情失败')
  }
}

async function selectSession(session: ChatSession, syncRoute = true) {
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
  previousMessageCount = 0
  wasAtBottom = true
  switchingSession = true

  if (currentSessionType.value === SESSION_TYPE_GROUP) {
    await loadGroupDetail(session.targetId)
  } else {
    groupDetail.value = null
    noticeDraft.value = ''
    resetGroupProfileDraft()
    showGroupDrawer.value = false
  }

  await loadMessages(String(session.targetId), currentSessionType.value)

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

  if (!routeTargetId) {
    if (!currentTargetId.value && sessions.value.length > 0) {
      await selectSession(sessions.value[0], false)
    }
    return
  }

  const existingSession = sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(routeTargetId, routeSessionType))
  if (existingSession) {
    if (!currentTargetId.value || buildSessionKey(currentTargetId.value, currentSessionType.value) !== buildSessionKey(routeTargetId, routeSessionType)) {
      await selectSession(existingSession, false)
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
        muted: detail.muted,
        muteTime: detail.muteTime,
        isDraft: true
      }
      sessions.value = [draftSession, ...sessions.value]
      await selectSession(draftSession, false)
    } catch (error) {
      message.error('打开群聊失败')
    }
    return
  }

  try {
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
    await selectSession(draftSession, false)
  } catch (error) {
    message.error('打开会话失败')
  }
}

async function loadMessages(targetId: string, sessionType: number, isPoll = false) {
  if (!isPoll) {
    loadingMessages.value = true
  }
  try {
    const response: any = await chatApi.getHistory(targetId, sessionType)
    const myId = String(userStore.userId)
    const rawMessages = response.data || []
    const nextMessages: DisplayMessage[] = rawMessages.map((item: any) => ({
      id: item.id,
      isMe: Number(item.msgType ?? MESSAGE_TYPE_TEXT) !== MESSAGE_TYPE_SYSTEM && String(item.fromUserId) === myId,
      isSystem: Number(item.msgType ?? MESSAGE_TYPE_TEXT) === MESSAGE_TYPE_SYSTEM,
      name: item.fromNickname,
      fromAvatar: item.fromAvatar || '',
      content: item.content,
      msgType: Number(item.msgType ?? MESSAGE_TYPE_TEXT),
      status: Number(item.status ?? 0),
      createTime: item.createTime,
      time: item.createTime?.substring(11, 16) || '',
      readStatus: Number(item.status) === MESSAGE_STATUS_RECALLED ? '已撤回' : '已读',
      fileName: item.fileName || '',
      fileSize: item.fileSize ? Number(item.fileSize) : undefined
    }))

    const hasNewMessages = nextMessages.length > previousMessageCount && previousMessageCount > 0
    const sameMessages = nextMessages.length === previousMessageCount
      && nextMessages.length > 0
      && nextMessages[nextMessages.length - 1].id === messages.value[messages.value.length - 1]?.id
      && nextMessages[nextMessages.length - 1].status === messages.value[messages.value.length - 1]?.status

    if (sameMessages) {
      await chatApi.markRead(targetId, sessionType).catch(() => undefined)
      return
    }

    messages.value = nextMessages
    previousMessageCount = nextMessages.length

    if (hasNewMessages) {
      const lastMessage = nextMessages[nextMessages.length - 1]
      if (!lastMessage.isMe) {
        showNotification(
          lastMessage.isSystem ? '群系统通知' : lastMessage.name || '新消息',
          getMessagePreview(lastMessage)
        )
      }
      flashSessionKey.value = buildSessionKey(targetId, sessionType)
      setTimeout(() => {
        if (flashSessionKey.value === buildSessionKey(targetId, sessionType)) {
          flashSessionKey.value = null
        }
      }, 2000)
    }

    await nextTick()
    if (messagesRef.value && (!isPoll || switchingSession || (hasNewMessages && wasAtBottom))) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }

    switchingSession = false
    await chatApi.markRead(targetId, sessionType).catch(() => undefined)
  } catch (error) {
    console.error('loadMessages error:', error)
    if (!isPoll) {
      messages.value = []
    }
  } finally {
    if (!isPoll) {
      loadingMessages.value = false
    }
  }
}

async function refreshCurrentSession() {
  showMenu.value = false
  if (!currentTargetId.value) {
    return
  }
  if (isGroupSession.value) {
    await loadGroupDetail(currentTargetId.value)
  }
  await loadMessages(currentTargetId.value, currentSessionType.value)
  await loadSessions()
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

  sending.value = true
  try {
    await chatApi.send(currentTargetId.value, inputMessage.value.trim(), currentSessionType.value)
    inputMessage.value = ''
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
    await loadMessages(currentTargetId.value, currentSessionType.value)
    await loadSessions()
  } catch (error: any) {
    console.error('handleSend error:', error)
    message.error(error.response?.data?.message || '发送失败')
  } finally {
    sending.value = false
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
    const uploadResponse: any = await fileApi.uploadFile(file)
    const fileId = uploadResponse.data?.id
    await chatApi.sendFile(currentTargetId.value, fileId, MESSAGE_TYPE_FILE, currentSessionType.value)
    await loadMessages(currentTargetId.value, currentSessionType.value)
    await loadSessions()
  } catch (error: any) {
    console.error('handleFileUpload error:', error)
    message.error(error.response?.data?.message || '发送文件失败')
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
    const uploadResponse: any = await fileApi.uploadImage(file)
    const fileId = uploadResponse.data?.id
    await chatApi.sendFile(currentTargetId.value, fileId, MESSAGE_TYPE_IMAGE, currentSessionType.value)
    await loadMessages(currentTargetId.value, currentSessionType.value)
    await loadSessions()
  } catch (error: any) {
    console.error('handleImageUpload error:', error)
    message.error(error.response?.data?.message || '发送图片失败')
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
  if (!msg || msg.isSystem || !msg.isMe || msg.status === MESSAGE_STATUS_RECALLED || !msg.createTime) {
    return false
  }
  const messageTime = new Date(msg.createTime).getTime()
  return Date.now() - messageTime < 2 * 60 * 1000
}

async function handleRecallMessage() {
  if (!selectedMsg.value || !currentTargetId.value) {
    return
  }
  try {
    await chatApi.recall(selectedMsg.value.id)
    await loadMessages(currentTargetId.value, currentSessionType.value)
    await loadSessions()
  } catch (error: any) {
    console.error('handleRecallMessage error:', error)
    message.error(error.response?.data?.message || '撤回失败')
  }
}

async function handleCopyMessage() {
  if (!selectedMsg.value?.content) {
    return
  }
  await navigator.clipboard.writeText(selectedMsg.value.content)
  message.success('已复制')
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
  if (createGroupForm.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(createGroupForm.avatarPreview)
  }
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
  if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(groupProfileDraft.avatarPreview)
  }
  groupProfileDraft.groupName = ''
  groupProfileDraft.avatarPreview = ''
  groupProfileDraft.avatarFile = null
  if (groupAvatarInputRef.value) {
    groupAvatarInputRef.value.value = ''
  }
}

function syncGroupProfileDraft(detail?: GroupDetail | null) {
  if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(groupProfileDraft.avatarPreview)
  }
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
    if (createGroupForm.avatarPreview.startsWith('blob:')) {
      URL.revokeObjectURL(createGroupForm.avatarPreview)
    }
    createGroupForm.avatarFile = file
    createGroupForm.avatarPreview = previewUrl
    return
  }
  if (showGroupDrawer.value && canEditGroupProfile.value) {
    if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
      URL.revokeObjectURL(groupProfileDraft.avatarPreview)
    }
    groupProfileDraft.avatarFile = file
    groupProfileDraft.avatarPreview = previewUrl
    return
  }
  URL.revokeObjectURL(previewUrl)
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
      const uploadResponse: any = await fileApi.uploadImage(createGroupForm.avatarFile)
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
        muted: Boolean(createdGroup.muted),
        muteTime: createdGroup.muteTime || '',
        isDraft: true
      })
    }
    if (groupSession) {
      await selectSession(groupSession)
    }
    message.success('群聊创建成功')
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
  showAddMembersModal.value = true
}

function closeAddMembersModal() {
  showAddMembersModal.value = false
  addMembersSelection.value = []
}

async function submitAddMembers() {
  if (!currentTargetId.value || addMembersSelection.value.length === 0) {
    message.warning('请选择要邀请的成员')
    return
  }

  addingMembers.value = true
  try {
    await groupApi.inviteMembers(currentTargetId.value, addMembersSelection.value)
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
    await loadSessions()
    message.success('群公告已更新')
  } catch (error: any) {
    console.error('submitUpdateNotice error:', error)
    message.error(error.response?.data?.message || '更新群公告失败')
  } finally {
    updatingNotice.value = false
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
      const uploadResponse: any = await fileApi.uploadImage(groupProfileDraft.avatarFile)
      groupAvatar = uploadResponse.data?.fileUrl || ''
    }
    await groupApi.updateProfile(currentTargetId.value, {
      groupName: nextGroupName,
      groupAvatar
    })
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
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
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
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
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
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
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
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
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
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
    currentTargetId.value = null
    currentSessionType.value = SESSION_TYPE_SINGLE
    groupDetail.value = null
    messages.value = []
    previousMessageCount = 0
    await loadSessions()
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
    currentTargetId.value = null
    currentSessionType.value = SESSION_TYPE_SINGLE
    groupDetail.value = null
    messages.value = []
    previousMessageCount = 0
    await loadSessions()
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
    await loadGroupDetail(currentTargetId.value)
    await loadSessions()
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
  loadingSessions.value = true
  await Promise.all([loadFriends(), loadSessions()])
  initialized.value = true
  loadingSessions.value = false
  await initializeRouteSession()

  refreshTimer = setInterval(async () => {
    await loadSessions()
    if (currentTargetId.value) {
      await loadMessages(currentTargetId.value, currentSessionType.value, true)
    }
    if (currentTargetId.value && isGroupSession.value && showGroupDrawer.value) {
      await loadGroupDetail(currentTargetId.value, false)
    }
  }, 3000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
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
.danger-action-btn,
.download-btn {
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
.download-btn.open:hover {
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
.download-size,
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
.modal-actions,
.download-actions {
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
.download-cancel,
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
.emoji-picker {
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

.msg-status.read {
  color: var(--linkx-primary);
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
  color: inherit;
  text-decoration: none;
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

.image-preview-overlay,
.download-overlay,
.overlay-panel,
.drawer-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.image-preview-overlay {
  background: rgba(0, 0, 0, 0.9);
  z-index: 3000;
  flex-direction: column;
}

.image-preview-stage {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.image-preview-container {
  display: flex;
  max-width: 100%;
  max-height: 100%;
  align-items: center;
  justify-content: center;
}

.image-preview-img {
  display: block;
  max-width: min(100%, 1600px);
  max-height: calc(100vh - 64px);
  object-fit: contain;
  border-radius: var(--linkx-radius-sm);
}

.image-preview-close,
.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.image-preview-close {
  position: fixed;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  z-index: 3001;
}

.download-overlay,
.overlay-panel,
.drawer-overlay {
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.download-dialog,
.modal-card,
.group-drawer {
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
}

.download-dialog,
.modal-card {
  border-radius: var(--linkx-radius-lg);
  max-width: calc(100vw - 24px);
  max-height: calc(100vh - 24px);
}

.download-dialog {
  width: 360px;
  padding: 32px;
  text-align: center;
}

.download-header {
  margin-bottom: 24px;
}

.download-filename {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text);
  word-break: break-all;
}

.download-progress {
  margin-bottom: 24px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: var(--linkx-bg);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  margin-top: 8px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.download-btn,
.secondary-btn,
.danger-action-btn {
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.download-btn {
  flex: 1;
  padding: 12px;
}

.download-btn.open {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.download-btn.save,
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

.group-summary-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
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

  .download-dialog,
  .modal-card {
    width: calc(100vw - 20px);
    padding: 20px 16px;
  }

  .modal-actions,
  .download-actions,
  .danger-actions {
    flex-wrap: wrap;
  }

  .secondary-btn,
  .primary-btn,
  .download-btn,
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
