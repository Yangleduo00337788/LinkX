<template>
  <div class="group-members-page">
    <input ref="groupAvatarInputRef" type="file" accept="image/*" class="hidden-file-input" @change="handleGroupAvatarSelected" />

    <div class="page-shell">
      <div class="page-header">
        <div class="page-header-main">
          <button class="back-btn" @click="() => openGroupChat()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6" />
            </svg>
            返回群聊
          </button>
          <div>
            <div class="page-title">群成员</div>
            <div class="page-subtitle">独立浏览群成员，并在这里直接完成成员权限管理</div>
          </div>
        </div>
        <div class="page-header-actions">
          <button class="secondary-btn" @click="copyGroupId">复制群号</button>
          <button class="primary-btn" :disabled="pageLoading || loadingGroupRequests" @click="refreshPageData">
            {{ pageLoading || loadingGroupRequests ? '刷新中...' : '刷新页面' }}
          </button>
        </div>
      </div>

      <div v-if="groupDetail" class="page-content">
        <aside class="side-column">
          <section class="group-hero-card">
            <div class="group-hero-top">
              <div class="group-avatar">
                <img v-if="groupDetail.groupAvatar" :src="groupDetail.groupAvatar" class="avatar-img" />
                <span v-else>{{ groupDetail.groupName?.charAt(0) || '群' }}</span>
              </div>
              <div class="group-hero-main">
                <div class="group-name-row">
                  <h1>{{ groupDisplayName }}</h1>
                  <span class="role-chip self-role" :class="roleClass(groupDetail.myRole)">我是{{ roleText(groupDetail.myRole) }}</span>
                </div>
                <div v-if="groupDetail.groupRemark" class="group-alias-hint">原群名：{{ groupDetail.groupName }}</div>
                <div class="group-meta-row">
                  <span>群号 {{ groupDetail.id }}</span>
                  <span>{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</span>
                </div>
                <div class="group-notice">
                  {{ groupDetail.notice?.trim() || '暂无群公告' }}
                </div>
              </div>
            </div>
            <div class="group-stat-row">
              <div class="stat-pill">
                <span class="stat-label">群主</span>
                <strong>{{ ownerCount }}</strong>
              </div>
              <div class="stat-pill">
                <span class="stat-label">管理员</span>
                <strong>{{ adminCount }}</strong>
              </div>
              <div class="stat-pill">
                <span class="stat-label">普通成员</span>
                <strong>{{ memberCount }}</strong>
              </div>
              <div class="stat-pill warn">
                <span class="stat-label">已禁言</span>
                <strong>{{ mutedCount }}</strong>
              </div>
            </div>
          </section>

          <section class="panel-card profile-card">
            <div class="panel-title-row">
              <div>
                <div class="panel-title">群资料</div>
                <div class="panel-note">{{ canEditGroupProfile ? '支持直接编辑群名称、头像与公告' : '仅群主和管理员可编辑群资料' }}</div>
              </div>
            </div>

            <div class="profile-editor">
              <button
                class="profile-avatar-editor"
                :class="{ editable: canEditGroupProfile }"
                :disabled="!canEditGroupProfile || updatingGroupProfile"
                @click="triggerGroupAvatarUpload"
              >
                <img
                  v-if="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
                  :src="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
                  class="avatar-img"
                />
                <span v-else>{{ groupProfileDraft.groupName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
                <span v-if="canEditGroupProfile" class="profile-avatar-mask">更换头像</span>
              </button>

              <div class="profile-fields">
                <label class="field-label" for="groupNameInput">群名称</label>
                <input
                  id="groupNameInput"
                  v-model="groupProfileDraft.groupName"
                  type="text"
                  class="text-input filled-input"
                  maxlength="30"
                  :disabled="!canEditGroupProfile || updatingGroupProfile"
                  placeholder="请输入群名称"
                />

                <label class="field-label" for="groupNoticeInput">群公告</label>
                <textarea
                  id="groupNoticeInput"
                  v-model="noticeDraft"
                  class="text-area"
                  rows="4"
                  maxlength="255"
                  :disabled="!canEditNotice || updatingNotice"
                  :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
                ></textarea>
              </div>
            </div>

            <div class="profile-action-row">
              <button
                class="secondary-btn"
                :disabled="(!isGroupProfileChanged && !isGroupNoticeChanged) || updatingGroupProfile || updatingNotice"
                @click="discardGroupProfileDrafts"
              >
                还原修改
              </button>
              <button
                v-if="canEditNotice"
                class="secondary-btn"
                :disabled="!isGroupNoticeChanged || updatingNotice"
                @click="submitUpdateNotice"
              >
                {{ updatingNotice ? '保存公告中...' : '保存公告' }}
              </button>
              <button
                v-if="canEditGroupProfile"
                class="primary-btn"
                :disabled="!isGroupProfileChanged || updatingGroupProfile"
                @click="submitUpdateGroupProfile"
              >
                {{ updatingGroupProfile ? '保存资料中...' : '保存资料' }}
              </button>
            </div>
          </section>

          <section class="panel-card preference-card">
            <div class="panel-title-row">
              <div>
                <div class="panel-title">我的群偏好</div>
                <div class="panel-note">只对当前账号生效，支持群备注和消息免打扰。</div>
              </div>
            </div>

            <div class="preference-form">
              <label class="field-label" for="groupRemarkInput">群备注</label>
              <input
                id="groupRemarkInput"
                v-model="groupPreferenceDraft.groupRemark"
                type="text"
                class="text-input filled-input"
                maxlength="100"
                placeholder="给这个群设置一个你自己的备注名"
              />

              <label class="preference-switch">
                <input v-model="groupPreferenceDraft.notificationMuted" type="checkbox" />
                <span>群免打扰</span>
                <small>开启后该群新消息不再弹出桌面通知</small>
              </label>
            </div>

            <div class="profile-action-row">
              <button
                class="secondary-btn"
                :disabled="!isGroupPreferenceChanged || savingGroupPreferences"
                @click="syncGroupPreferenceDraft(groupDetail)"
              >
                还原偏好
              </button>
              <button
                class="primary-btn"
                :disabled="!isGroupPreferenceChanged || savingGroupPreferences"
                @click="submitGroupPreferences"
              >
                {{ savingGroupPreferences ? '保存中...' : '保存偏好' }}
              </button>
            </div>
          </section>

          <section class="panel-card filter-card">
            <div class="panel-title">筛选成员</div>
            <div class="search-shell">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <input
                v-model="searchText"
                type="text"
                class="search-input"
                placeholder="搜索昵称或用户名"
              />
            </div>
            <div class="role-filter-group">
              <button
                v-for="option in roleFilters"
                :key="option.value"
                class="role-filter-btn"
                :class="{ active: roleFilter === option.value }"
                @click="roleFilter = option.value"
              >
                {{ option.label }}
              </button>
            </div>
            <div class="filter-hint">
              当前展示 {{ filteredMembers.length }} / {{ groupDetail.members.length }} 名成员
            </div>
          </section>

          <section class="panel-card manage-card">
            <div class="panel-title">群管理</div>
            <div class="manage-subtitle">把常用群操作集中到这里处理，减少在聊天页和成员页之间来回切换。</div>

            <div class="manage-summary-grid">
              <div class="manage-summary-item">
                <span class="manage-summary-label">我的权限</span>
                <strong>{{ roleText(currentGroupRole) }}</strong>
              </div>
              <div class="manage-summary-item">
                <span class="manage-summary-label">可邀请好友</span>
                <strong>{{ availableFriendsForCurrentGroup.length }}</strong>
              </div>
            </div>

            <div class="manage-action-list">
              <button v-if="canManageMembers" class="secondary-btn action-btn" @click="openAddMembersModal">
                邀请成员
              </button>
              <button v-if="isGroupOwner" class="secondary-btn action-btn" @click="openTransferOwnerModal">
                转让群主
              </button>
              <button v-if="!isGroupOwner" class="secondary-btn action-btn warn" @click="handleLeaveGroup">
                退出群聊
              </button>
              <button v-if="canDissolveGroup" class="secondary-btn action-btn danger" @click="handleDissolveGroup">
                解散群聊
              </button>
            </div>

            <div class="manage-tip">
              <span v-if="canManageMembers">你可以直接邀请好友或处理群管理动作。</span>
              <span v-else>当前仅支持浏览成员信息，如需更多操作请联系群管理员。</span>
            </div>
          </section>
        </aside>

        <div class="main-column">
          <section class="panel-card member-panel">
            <div class="member-panel-head">
              <div class="member-panel-title-wrap">
                <div class="member-panel-kicker">成员工作区</div>
                <div class="panel-title">成员列表</div>
                <div class="panel-subtitle">支持查看角色、禁言状态与快捷成员管理</div>
              </div>
              <div class="member-panel-overview">
                <div class="overview-badge">
                  <span class="overview-label">当前筛选</span>
                  <strong>{{ activeRoleFilterLabel }}</strong>
                </div>
                <div class="overview-badge">
                  <span class="overview-label">可管理成员</span>
                  <strong>{{ manageableMemberCount }}</strong>
                </div>
                <div class="overview-badge">
                  <span class="overview-label">搜索命中</span>
                  <strong>{{ filteredMembers.length }}</strong>
                </div>
              </div>
            </div>

            <div class="member-panel-toolbar">
              <div class="toolbar-hint">
                当前展示 {{ filteredMembers.length }} 名成员
                <span v-if="searchText.trim()">，关键词 “{{ searchText.trim() }}”</span>
              </div>
              <div class="toolbar-hint">
                <span v-if="canManageMembers">你可以直接在右侧完成管理员、禁言和移出操作</span>
                <span v-else>当前仅支持浏览成员与状态信息</span>
              </div>
            </div>

            <section v-if="canManageMembers" class="request-panel">
              <div class="request-panel-head">
                <div>
                  <div class="panel-title">入群审批</div>
                  <div class="panel-subtitle">集中处理当前群的入群申请与邀请通知</div>
                </div>
                <div class="request-panel-count">{{ currentGroupRequests.length }} 条待处理</div>
              </div>

              <div v-if="currentGroupRequests.length > 0" class="request-list">
                <article v-for="request in currentGroupRequests" :key="request.id" class="request-card">
                  <div class="request-avatar" :class="{ invite: request.requestType === 1 }">
                    <img v-if="request.groupAvatar" :src="request.groupAvatar" class="avatar-img" />
                    <span v-else>{{ request.groupName?.charAt(0) || '群' }}</span>
                  </div>
                  <div class="request-info">
                    <div class="request-title-row">
                      <span class="request-name">{{ request.groupName }}</span>
                      <span class="request-type-tag" :class="groupRequestTagClass(request.requestType)">
                        {{ groupRequestTypeText(request.requestType) }}
                      </span>
                    </div>
                    <div class="request-message">{{ buildGroupRequestMessage(request) }}</div>
                    <div class="request-time">{{ formatRequestTime(request.createTime) }}</div>
                  </div>
                  <div class="request-actions">
                    <button
                      class="request-action-btn accept"
                      :disabled="requestActionLoadingId === request.id"
                      @click="handleAcceptGroupRequest(request.id)"
                    >
                      通过
                    </button>
                    <button
                      class="request-action-btn reject"
                      :disabled="requestActionLoadingId === request.id"
                      @click="handleRejectGroupRequest(request.id)"
                    >
                      拒绝
                    </button>
                  </div>
                </article>
              </div>
              <div v-else class="request-empty-state">
                当前群暂无待处理入群申请
              </div>
            </section>

            <div v-if="filteredMembers.length > 0" class="member-list">
              <article v-for="member in filteredMembers" :key="member.userId" class="member-card">
                <div class="member-card-main">
                  <div class="member-avatar">
                    <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                    <span v-else>{{ member.nickname?.charAt(0) || member.username?.charAt(0) || '群' }}</span>
                  </div>
                  <div class="member-info">
                    <div class="member-name-row">
                      <span class="member-name">{{ member.nickname || member.username }}</span>
                      <span class="role-chip" :class="roleClass(member.role)">{{ roleText(member.role) }}</span>
                      <span v-if="String(member.userId) === String(userStore.userId)" class="self-tag">我</span>
                      <span v-if="member.muted" class="mute-tag">已禁言</span>
                    </div>
                    <div class="member-username">@{{ member.username }}</div>
                    <div class="member-status">
                      <span v-if="member.muted && member.muteTime">禁言至 {{ formatDateTime(member.muteTime) }}</span>
                      <span v-else>当前可正常发言</span>
                    </div>
                  </div>
                </div>
                <div class="member-card-side">
                  <div class="member-side-caption">
                    <span>{{ canOperateMember(member) ? '可快捷管理' : '仅浏览' }}</span>
                  </div>
                  <div v-if="canOperateMember(member)" class="member-actions">
                    <button
                      v-if="canToggleAdmin(member)"
                      class="mini-btn"
                      :disabled="actionLoading"
                      @click="toggleAdminRole(member)"
                    >
                      {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
                    </button>
                    <button
                      class="mini-btn"
                      :disabled="actionLoading"
                      @click="toggleMuteMember(member)"
                    >
                      {{ member.muted ? '解除禁言' : '禁言成员' }}
                    </button>
                    <button
                      class="mini-btn danger"
                      :disabled="actionLoading"
                      @click="handleRemoveMember(member)"
                    >
                      移出群聊
                    </button>
                  </div>
                </div>
              </article>
            </div>

            <div v-else class="empty-state">
              <div class="empty-title">没有找到匹配的成员</div>
              <div class="empty-subtitle">换个关键词或筛选条件试试</div>
            </div>
          </section>

          <section class="panel-card search-panel">
            <div class="workspace-head">
              <div>
                <div class="panel-title">群聊消息搜索</div>
                <div class="panel-subtitle">按关键词检索当前群的历史消息、文件消息和系统记录。</div>
              </div>
              <button class="secondary-btn compact-btn" :disabled="groupMessageSearchLoading" @click="searchGroupMessages">
                {{ groupMessageSearchLoading ? '搜索中...' : '开始搜索' }}
              </button>
            </div>

            <div class="workspace-toolbar">
              <div class="search-shell workspace-search">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8" />
                  <path d="M21 21l-4.35-4.35" />
                </svg>
                <input
                  v-model="messageSearchKeyword"
                  type="text"
                  class="search-input"
                  placeholder="搜索群内历史消息"
                  @keyup.enter="searchGroupMessages"
                />
              </div>
            </div>

            <div v-if="groupMessageSearchResults.length > 0" class="search-result-list">
              <article v-for="item in groupMessageSearchResults" :key="item.id" class="search-result-card">
                <div class="result-main">
                  <div class="result-title-row">
                    <strong>{{ item.fromNickname || '成员' }}</strong>
                    <span class="result-type-tag">{{ getMediaTypeText(item) }}</span>
                    <span class="result-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                  <div class="result-content">{{ getMessageSearchPreview(item) }}</div>
                </div>
                <div class="result-actions">
                  <button v-if="item.content" class="mini-btn" @click="() => openGroupChat(item)">去群聊查看</button>
                  <button v-if="item.fileName && item.content" class="mini-btn" @click="openMediaResource(item)">打开附件</button>
                </div>
              </article>
            </div>
            <div v-else class="empty-state compact">
              <div class="empty-title">{{ messageSearchKeyword.trim() ? '没有找到匹配消息' : '输入关键词后开始搜索' }}</div>
              <div class="empty-subtitle">{{ messageSearchKeyword.trim() ? '试试成员名、消息文本或文件名' : '支持搜索文本消息、系统消息和文件名' }}</div>
            </div>
          </section>

          <section class="panel-card media-panel">
            <div class="workspace-head">
              <div>
                <div class="panel-title">群相册 / 文件库</div>
                <div class="panel-subtitle">汇总查看当前群里发送过的图片和文件。</div>
              </div>
              <button class="secondary-btn compact-btn" :disabled="groupMediaLoading" @click="loadGroupMedia">
                {{ groupMediaLoading ? '刷新中...' : '刷新列表' }}
              </button>
            </div>

            <div class="workspace-toolbar media-toolbar">
              <div class="tab-group">
                <button class="tab-btn" :class="{ active: mediaType === 'all' }" @click="mediaType = 'all'; void loadGroupMedia()">全部</button>
                <button class="tab-btn" :class="{ active: mediaType === 'image' }" @click="mediaType = 'image'; void loadGroupMedia()">图片</button>
                <button class="tab-btn" :class="{ active: mediaType === 'file' }" @click="mediaType = 'file'; void loadGroupMedia()">文件</button>
              </div>
              <div class="search-shell workspace-search">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8" />
                  <path d="M21 21l-4.35-4.35" />
                </svg>
                <input
                  v-model="mediaKeyword"
                  type="text"
                  class="search-input"
                  placeholder="搜索文件名或类型"
                  @keyup.enter="loadGroupMedia"
                />
              </div>
            </div>

            <div v-if="groupMediaItems.length > 0" class="media-list">
              <article v-for="item in groupMediaItems" :key="item.id" class="media-card">
                <div class="media-cover" :class="{ image: isImageMedia(item) }">
                  <img v-if="isImageMedia(item) && item.accessUrl" :src="item.accessUrl" :alt="item.fileName || '群图片'" />
                  <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                  </svg>
                </div>
                <div class="media-info">
                  <div class="media-name">{{ item.fileName || (isImageMedia(item) ? '群图片' : '群文件') }}</div>
                  <div class="media-meta">
                    <span>{{ item.fromNickname || '成员' }}</span>
                    <span>{{ formatDateTime(item.createTime) }}</span>
                    <span>{{ formatFileSize(item.fileSize) }}</span>
                  </div>
                  <div class="media-actions">
                    <button class="mini-btn" @click="openMediaResource(item)">打开</button>
                    <button class="mini-btn" @click="copyMediaLink(item)">复制链接</button>
                  </div>
                </div>
              </article>
            </div>
            <div v-else class="empty-state compact">
              <div class="empty-title">{{ mediaKeyword.trim() ? '没有找到匹配文件' : '当前群还没有图片或文件' }}</div>
              <div class="empty-subtitle">{{ mediaKeyword.trim() ? '尝试其他关键词或切换分类' : '后续在群里发送的图片和文件会自动汇总到这里' }}</div>
            </div>
          </section>
        </div>
      </div>

      <div v-else class="loading-panel">
        <div class="loading-spinner"></div>
        <div class="loading-text">正在加载群成员...</div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showAddMembersModal" class="modal-overlay" @click.self="closeAddMembersModal">
        <div class="modal-card picker-modal">
          <div class="modal-header">
            <div>
              <div class="modal-title">邀请成员</div>
              <div class="modal-subtitle">向好友发送入群邀请，待对方确认后加入</div>
            </div>
            <button class="modal-close" @click="closeAddMembersModal">×</button>
          </div>

          <div v-if="availableFriendsForCurrentGroup.length > 0" class="member-picker-list">
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
          <div v-else class="picker-empty-state">所有好友都已在群内，暂无可邀请成员。</div>

          <div class="modal-body">
            <label class="form-label" for="addMembersMessage">邀请说明</label>
            <textarea
              id="addMembersMessage"
              v-model="addMembersMessage"
              class="text-area"
              rows="3"
              maxlength="255"
              placeholder="选填，邀请说明会出现在对方的群通知中"
            ></textarea>
          </div>

          <div class="modal-actions">
            <button class="secondary-btn" :disabled="addingMembers" @click="closeAddMembersModal">取消</button>
            <button class="primary-btn" :disabled="addingMembers || addMembersSelection.length === 0" @click="submitAddMembers">
              {{ addingMembers ? '邀请中...' : '确认邀请' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="showTransferOwnerModal" class="modal-overlay" @click.self="closeTransferOwnerModal">
        <div class="modal-card picker-modal">
          <div class="modal-header">
            <div>
              <div class="modal-title">转让群主</div>
              <div class="modal-subtitle">选择一位成员成为新的群主</div>
            </div>
            <button class="modal-close" @click="closeTransferOwnerModal">×</button>
          </div>

          <div v-if="transferableMembers.length > 0" class="member-picker-list">
            <label v-for="member in transferableMembers" :key="member.userId" class="member-picker-item">
              <input v-model="transferOwnerSelection" :value="member.userId" type="radio" name="transferOwner" />
              <div class="member-picker-avatar">
                <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                <span v-else>{{ member.nickname?.charAt(0) || member.username?.charAt(0) || '群' }}</span>
              </div>
              <div class="member-picker-info">
                <span class="member-picker-name">{{ member.nickname || member.username }}</span>
                <span class="member-picker-meta">@{{ member.username }} · {{ roleText(member.role) }}</span>
              </div>
            </label>
          </div>
          <div v-else class="picker-empty-state">群内没有其他成员可转让。</div>

          <div class="modal-actions">
            <button class="secondary-btn" :disabled="transferringOwner" @click="closeTransferOwnerModal">取消</button>
            <button class="primary-btn" :disabled="!transferOwnerSelection || transferringOwner" @click="submitTransferOwner">
              {{ transferringOwner ? '转让中...' : '确认转让' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="showMuteMemberModal" class="modal-overlay" @click.self="closeMuteMemberModal">
        <div class="modal-card mute-modal">
          <div class="modal-header">
            <div>
              <div class="modal-title">设置成员禁言</div>
              <div class="modal-subtitle">{{ muteTargetMember?.nickname || muteTargetMember?.username || '成员' }}</div>
            </div>
            <button class="modal-close" @click="closeMuteMemberModal">×</button>
          </div>
          <div class="modal-body">
            <label class="form-label" for="muteMinutes">禁言时长（分钟）</label>
            <input
              id="muteMinutes"
              v-model="muteMinutesInput"
              type="number"
              min="1"
              max="43200"
              class="text-input"
              placeholder="请输入 1-43200 之间的整数"
            />
          </div>
          <div class="modal-actions">
            <button class="secondary-btn" :disabled="actionLoading" @click="closeMuteMemberModal">取消</button>
            <button class="primary-btn" :disabled="actionLoading" @click="submitMuteMember">
              {{ actionLoading ? '提交中...' : '确认禁言' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

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
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { fileApi, friendApi, groupApi } from '../api/client'
import { useUserStore } from '../stores/user'
import { useConfirmDialog } from '../hooks/useConfirmDialog'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import GroupNoticeDialog from '../components/GroupNoticeDialog.vue'
import { parseDateTime } from '../utils/datetime'
import { hydrateFileAccessUrls, resolveFileAccessUrl } from '../utils/file-access'
import { openSafeExternalUrl } from '../utils/url'

const GROUP_ROLE_MEMBER = 0
const GROUP_ROLE_ADMIN = 1
const GROUP_ROLE_OWNER = 2

interface GroupMember {
  userId: string | number
  username: string
  nickname: string
  avatar?: string
  role: number
  muted?: boolean
  muteTime?: string
}

interface FriendItem {
  friendId: string | number
  friendNickname: string
  friendAvatar?: string
  friendUsername: string
}

interface GroupDetail {
  id: string | number
  groupName: string
  groupAvatar?: string
  groupRemark?: string
  notice?: string
  noticeUpdateTime?: string
  noticeReadTime?: string
  noticeUnread?: boolean
  ownerId: string | number
  maxMembers: number
  memberCount: number
  myRole: number
  muted?: boolean
  muteTime?: string
  notificationMuted?: boolean
  members: GroupMember[]
}

interface GroupMediaItem {
  id: string | number
  fromUserId?: string | number
  fromNickname?: string
  fromAvatar?: string
  content: string
  accessUrl?: string
  msgType: number
  fileName?: string
  fileSize?: number
  fileType?: string
  createTime?: string
}

interface GroupRequestItem {
  id: number | string
  groupId: string | number
  groupName: string
  groupAvatar?: string
  fromNickname?: string
  fromUsername?: string
  message?: string
  requestType: number
  createTime?: string
}

type RoleFilter = 'all' | 'owner' | 'admin' | 'member' | 'muted'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()

const groupAvatarInputRef = ref<HTMLInputElement>()
const groupDetail = ref<GroupDetail | null>(null)
const friends = ref<FriendItem[]>([])
const groupRequests = ref<GroupRequestItem[]>([])
const pageLoading = ref(false)
const loadingGroupRequests = ref(false)
const actionLoading = ref(false)
const requestActionLoadingId = ref<string | number | null>(null)
const searchText = ref('')
const roleFilter = ref<RoleFilter>('all')
const updatingGroupProfile = ref(false)
const updatingNotice = ref(false)
const showNoticeReminder = ref(false)
const acknowledgingNoticeReminder = ref(false)
const noticeDraft = ref('')
const showAddMembersModal = ref(false)
const addMembersSelection = ref<Array<string | number>>([])
const addMembersMessage = ref('')
const addingMembers = ref(false)
const showTransferOwnerModal = ref(false)
const transferOwnerSelection = ref<string | number | null>(null)
const transferringOwner = ref(false)
const showMuteMemberModal = ref(false)
const muteTargetMember = ref<GroupMember | null>(null)
const muteMinutesInput = ref('10')
const groupPreferenceDraft = reactive({
  groupRemark: '',
  notificationMuted: false
})
const savingGroupPreferences = ref(false)
const mediaType = ref<'all' | 'image' | 'file'>('all')
const mediaKeyword = ref('')
const groupMediaItems = ref<GroupMediaItem[]>([])
const groupMediaLoading = ref(false)
const messageSearchKeyword = ref('')
const groupMessageSearchResults = ref<GroupMediaItem[]>([])
const groupMessageSearchLoading = ref(false)

const groupProfileDraft = reactive({
  groupName: '',
  avatarPreview: '',
  avatarFile: null as File | null
})

const roleFilters = [
  { label: '全部', value: 'all' as RoleFilter },
  { label: '群主', value: 'owner' as RoleFilter },
  { label: '管理员', value: 'admin' as RoleFilter },
  { label: '普通成员', value: 'member' as RoleFilter },
  { label: '已禁言', value: 'muted' as RoleFilter }
]

const groupId = computed(() => String(route.params.groupId || ''))
const currentGroupRole = computed(() => groupDetail.value?.myRole ?? GROUP_ROLE_MEMBER)
const canManageMembers = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditGroupProfile = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditNotice = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
const isGroupOwner = computed(() => currentGroupRole.value === GROUP_ROLE_OWNER)
const canDissolveGroup = computed(() => isGroupOwner.value)

const ownerCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_OWNER).length)
const adminCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_ADMIN).length)
const mutedCount = computed(() => (groupDetail.value?.members || []).filter(member => member.muted).length)
const memberCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_MEMBER).length)
const activeRoleFilterLabel = computed(() => roleFilters.find(option => option.value === roleFilter.value)?.label || '全部')
const manageableMemberCount = computed(() => (groupDetail.value?.members || []).filter(member => canOperateMember(member)).length)
const groupDisplayName = computed(() => groupDetail.value?.groupRemark || groupDetail.value?.groupName || '')
const isGroupPreferenceChanged = computed(() => {
  if (!groupDetail.value) {
    return false
  }
  return groupPreferenceDraft.groupRemark.trim() !== (groupDetail.value.groupRemark || '').trim()
    || groupPreferenceDraft.notificationMuted !== Boolean(groupDetail.value.notificationMuted)
})
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
const availableFriendsForCurrentGroup = computed(() => {
  const memberIds = new Set((groupDetail.value?.members || []).map(member => String(member.userId)))
  return friends.value.filter(friend => !memberIds.has(String(friend.friendId)))
})
const transferableMembers = computed(() => (groupDetail.value?.members || []).filter(member => {
  return String(member.userId) !== String(userStore.userId) && member.role !== GROUP_ROLE_OWNER
}))
const currentGroupRequests = computed(() => {
  return groupRequests.value
    .filter(item => String(item.groupId) === String(groupId.value))
    .sort((left, right) => String(right.id).localeCompare(String(left.id)))
})

const filteredMembers = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  const members = [...(groupDetail.value?.members || [])].sort((left, right) => {
    if (left.role !== right.role) {
      return right.role - left.role
    }
    return String(left.userId).localeCompare(String(right.userId))
  })
  return members.filter(member => {
    if (roleFilter.value === 'owner' && member.role !== GROUP_ROLE_OWNER) {
      return false
    }
    if (roleFilter.value === 'admin' && member.role !== GROUP_ROLE_ADMIN) {
      return false
    }
    if (roleFilter.value === 'member' && member.role !== GROUP_ROLE_MEMBER) {
      return false
    }
    if (roleFilter.value === 'muted' && !member.muted) {
      return false
    }
    if (!keyword) {
      return true
    }
    const searchableText = `${member.nickname || ''} ${member.username || ''}`.toLowerCase()
    return searchableText.includes(keyword)
  })
})

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

function formatDateTime(time?: string) {
  if (!time) {
    return ''
  }
  const date = parseDateTime(time)
  if (!date) {
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

async function loadGroupDetail(showSuccess = false) {
  if (!groupId.value) {
    return
  }
  pageLoading.value = true
  try {
    const response: any = await groupApi.detail(groupId.value)
    applyGroupDetail(response.data || null, true)
    if (showSuccess) {
      message.success('群成员已刷新')
    }
  } catch (error: any) {
    console.error('loadGroupDetail error:', error)
    message.error(error.response?.data?.message || '加载群成员失败')
  } finally {
    pageLoading.value = false
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

function discardGroupProfileDrafts() {
  noticeDraft.value = groupDetail.value?.notice || ''
  syncGroupProfileDraft(groupDetail.value)
}

function syncGroupPreferenceDraft(detail?: GroupDetail | null) {
  groupPreferenceDraft.groupRemark = detail?.groupRemark || ''
  groupPreferenceDraft.notificationMuted = Boolean(detail?.notificationMuted)
}

function syncNoticeReminder(detail: GroupDetail | null) {
  if (!detail?.noticeUnread || !detail.notice?.trim()) {
    showNoticeReminder.value = false
    return
  }
  showNoticeReminder.value = true
}

function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {
  groupDetail.value = detail
  if (syncDraft || !isGroupNoticeChanged.value) {
    noticeDraft.value = detail?.notice || ''
  }
  if (syncDraft || !isGroupProfileChanged.value) {
    syncGroupProfileDraft(detail)
  }
  if (syncDraft || !isGroupPreferenceChanged.value) {
    syncGroupPreferenceDraft(detail)
  }
  syncNoticeReminder(detail)
}

async function loadFriends() {
  try {
    const response: any = await friendApi.getList()
    friends.value = response.data || []
  } catch (error) {
    console.error('loadFriends error:', error)
  }
}

async function loadGroupRequests() {
  loadingGroupRequests.value = true
  try {
    const response: any = await groupApi.getRequests()
    groupRequests.value = response.data || []
  } catch (error) {
    console.error('loadGroupRequests error:', error)
  } finally {
    loadingGroupRequests.value = false
  }
}

async function refreshPageData() {
  await Promise.all([
    loadGroupDetail(true),
    loadGroupRequests(),
    loadGroupMedia(),
    messageSearchKeyword.value.trim() ? searchGroupMessages() : Promise.resolve()
  ])
}

async function copyGroupId() {
  if (!groupDetail.value) {
    return
  }
  try {
    await navigator.clipboard.writeText(String(groupDetail.value.id))
    message.success(`群号 ${groupDetail.value.id} 已复制`)
  } catch (error) {
    message.error('复制群号失败')
  }
}

function openGroupChat(targetMessage?: GroupMediaItem) {
  if (!groupId.value) {
    router.push('/chat')
    return
  }
  const query: Record<string, string> = { sessionType: '2' }
  if (targetMessage?.id != null) {
    query.messageId = String(targetMessage.id)
  }
  router.push({
    path: `/chat/${groupId.value}`,
    query
  })
}

function formatFileSize(fileSize?: number) {
  const size = Number(fileSize || 0)
  if (!size) {
    return '0 B'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`
  }
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

function isImageMedia(item: GroupMediaItem) {
  return Number(item.msgType) === 1
}

function getMediaTypeText(item: GroupMediaItem) {
  if (Number(item.msgType) === 1) {
    return '图片'
  }
  if (Number(item.msgType) === 2) {
    return '文件'
  }
  if (Number(item.msgType) === 3) {
    return '系统'
  }
  return '消息'
}

function getMessageSearchPreview(item: GroupMediaItem) {
  if (Number(item.msgType) === 1) {
    return item.fileName ? `[图片] ${item.fileName}` : '[图片]'
  }
  if (Number(item.msgType) === 2) {
    return item.fileName ? `[文件] ${item.fileName}` : '[文件]'
  }
  if (Number(item.msgType) === 3) {
    return `[系统] ${item.content}`
  }
  return item.content
}

async function resolveMediaAccessUrl(item?: GroupMediaItem | null) {
  if (!item?.content) {
    return ''
  }
  if (item.accessUrl) {
    return item.accessUrl
  }
  return resolveFileAccessUrl(item.content)
}

async function copyMediaLink(item?: GroupMediaItem | null) {
  const accessUrl = await resolveMediaAccessUrl(item)
  if (!accessUrl) {
    message.error('无法生成访问链接')
    return
  }
  try {
    await navigator.clipboard.writeText(accessUrl)
    message.success('链接已复制')
  } catch (error) {
    message.error('复制链接失败')
  }
}

async function openMediaResource(item?: GroupMediaItem | null) {
  const accessUrl = await resolveMediaAccessUrl(item)
  if (!accessUrl) {
    message.error('资源访问链接不可用')
    return
  }
  try {
    await openSafeExternalUrl(accessUrl)
  } catch (error: any) {
    message.error(error.message || '打开资源失败')
  }
}

async function submitGroupPreferences() {
  if (!groupId.value || !isGroupPreferenceChanged.value) {
    return
  }
  savingGroupPreferences.value = true
  try {
    await groupApi.updatePreferences(groupId.value, {
      groupRemark: groupPreferenceDraft.groupRemark.trim(),
      notificationMuted: groupPreferenceDraft.notificationMuted
    })
    if (groupDetail.value) {
      applyGroupDetail({
        ...groupDetail.value,
        groupRemark: groupPreferenceDraft.groupRemark.trim(),
        notificationMuted: groupPreferenceDraft.notificationMuted
      }, true)
    }
    message.success('群偏好已保存')
  } catch (error: any) {
    console.error('submitGroupPreferences error:', error)
    message.error(error.response?.data?.message || '保存群偏好失败')
  } finally {
    savingGroupPreferences.value = false
  }
}

async function loadGroupMedia() {
  if (!groupId.value) {
    groupMediaItems.value = []
    return
  }
  groupMediaLoading.value = true
  try {
    const response: any = await groupApi.getMedia(groupId.value, {
      mediaType: mediaType.value,
      keyword: mediaKeyword.value.trim() || undefined,
      size: 200
    })
    groupMediaItems.value = await hydrateFileAccessUrls(response.data || [])
  } catch (error: any) {
    console.error('loadGroupMedia error:', error)
    message.error(error.response?.data?.message || '加载群相册/文件库失败')
  } finally {
    groupMediaLoading.value = false
  }
}

async function searchGroupMessages() {
  const keyword = messageSearchKeyword.value.trim()
  if (!groupId.value || !keyword) {
    groupMessageSearchResults.value = []
    return
  }
  groupMessageSearchLoading.value = true
  try {
    const response: any = await groupApi.searchMessages(groupId.value, keyword, 100)
    groupMessageSearchResults.value = response.data || []
  } catch (error: any) {
    console.error('searchGroupMessages error:', error)
    message.error(error.response?.data?.message || '搜索群消息失败')
  } finally {
    groupMessageSearchLoading.value = false
  }
}

function triggerGroupAvatarUpload() {
  if (!canEditGroupProfile.value) {
    return
  }
  groupAvatarInputRef.value?.click()
}

function handleGroupAvatarSelected(event: Event) {
  if (!canEditGroupProfile.value) {
    return
  }
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  const previewUrl = URL.createObjectURL(file)
  if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(groupProfileDraft.avatarPreview)
  }
  groupProfileDraft.avatarFile = file
  groupProfileDraft.avatarPreview = previewUrl
}

function groupRequestTypeText(requestType: number) {
  return requestType === 1 ? '邀请入群' : '申请入群'
}

function groupRequestTagClass(requestType: number) {
  return requestType === 1 ? 'invite' : 'join'
}

function buildGroupRequestMessage(request: GroupRequestItem) {
  const actor = request.fromNickname || request.fromUsername || '成员'
  const suffix = request.message?.trim() ? `：${request.message.trim()}` : ''
  if (request.requestType === 1) {
    return `${actor} 邀请加入当前群聊${suffix}`
  }
  return `${actor} 申请加入当前群聊${suffix}`
}

function formatRequestTime(time?: string) {
  return time?.substring(0, 16)?.replace('T', ' ') || ''
}

async function handleAcceptGroupRequest(requestId: number | string) {
  requestActionLoadingId.value = requestId
  try {
    await groupApi.acceptRequest(requestId)
    message.success('已通过入群申请')
    await Promise.all([loadGroupRequests(), loadGroupDetail()])
  } catch (error: any) {
    console.error('handleAcceptGroupRequest error:', error)
    message.error(error.response?.data?.message || '处理入群申请失败')
  } finally {
    requestActionLoadingId.value = null
  }
}

async function handleRejectGroupRequest(requestId: number | string) {
  requestActionLoadingId.value = requestId
  try {
    await groupApi.rejectRequest(requestId)
    message.success('已拒绝入群申请')
    await loadGroupRequests()
  } catch (error: any) {
    console.error('handleRejectGroupRequest error:', error)
    message.error(error.response?.data?.message || '处理入群申请失败')
  } finally {
    requestActionLoadingId.value = null
  }
}

async function submitUpdateNotice() {
  if (!groupId.value || !canEditNotice.value) {
    return
  }
  updatingNotice.value = true
  try {
    await groupApi.updateNotice(groupId.value, noticeDraft.value.trim())
    await loadGroupDetail()
    message.success('群公告已更新')
  } catch (error: any) {
    console.error('submitUpdateNotice error:', error)
    message.error(error.response?.data?.message || '更新群公告失败')
  } finally {
    updatingNotice.value = false
  }
}

async function acknowledgeNoticeReminder() {
  if (!groupId.value || !groupDetail.value) {
    showNoticeReminder.value = false
    return
  }
  acknowledgingNoticeReminder.value = true
  try {
    await groupApi.markNoticeRead(groupId.value)
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
  if (!groupId.value || !groupDetail.value || !canEditGroupProfile.value) {
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
    await groupApi.updateProfile(groupId.value, {
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

function openAddMembersModal() {
  if (!canManageMembers.value) {
    return
  }
  addMembersSelection.value = []
  addMembersMessage.value = ''
  showAddMembersModal.value = true
}

function closeAddMembersModal() {
  if (addingMembers.value) {
    return
  }
  showAddMembersModal.value = false
  addMembersSelection.value = []
  addMembersMessage.value = ''
}

function resetAddMembersModal() {
  showAddMembersModal.value = false
  addMembersSelection.value = []
  addMembersMessage.value = ''
}

async function submitAddMembers() {
  if (!groupId.value || addMembersSelection.value.length === 0) {
    return
  }
  addingMembers.value = true
  try {
    await groupApi.inviteMembers(groupId.value, addMembersSelection.value, addMembersMessage.value.trim())
    resetAddMembersModal()
    message.success('邀请已发送')
  } catch (error: any) {
    console.error('submitAddMembers error:', error)
    message.error(error.response?.data?.message || '邀请成员失败')
  } finally {
    addingMembers.value = false
  }
}

function openTransferOwnerModal() {
  if (!isGroupOwner.value) {
    return
  }
  transferOwnerSelection.value = null
  showTransferOwnerModal.value = true
}

function closeTransferOwnerModal() {
  if (transferringOwner.value) {
    return
  }
  showTransferOwnerModal.value = false
  transferOwnerSelection.value = null
}

function resetTransferOwnerModal() {
  showTransferOwnerModal.value = false
  transferOwnerSelection.value = null
}

async function submitTransferOwner() {
  if (!groupId.value || !transferOwnerSelection.value) {
    return
  }
  const member = groupDetail.value?.members.find(item => String(item.userId) === String(transferOwnerSelection.value))
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
    await groupApi.transferOwner(groupId.value, transferOwnerSelection.value)
    resetTransferOwnerModal()
    await loadGroupDetail()
    message.success('群主已转让')
  } catch (error: any) {
    console.error('submitTransferOwner error:', error)
    message.error(error.response?.data?.message || '转让群主失败')
  } finally {
    transferringOwner.value = false
  }
}

async function toggleAdminRole(memberItem: GroupMember) {
  if (!groupDetail.value || !canToggleAdmin(memberItem)) {
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

  actionLoading.value = true
  try {
    if (memberItem.role === GROUP_ROLE_ADMIN) {
      await groupApi.removeAdmin(groupDetail.value.id, memberItem.userId)
      message.success('已取消管理员')
    } else {
      await groupApi.setAdmin(groupDetail.value.id, memberItem.userId)
      message.success('已设为管理员')
    }
    await loadGroupDetail()
  } catch (error: any) {
    console.error('toggleAdminRole error:', error)
    message.error(error.response?.data?.message || '管理员操作失败')
  } finally {
    actionLoading.value = false
  }
}

function openMuteMemberModal(memberItem: GroupMember) {
  muteTargetMember.value = memberItem
  muteMinutesInput.value = '10'
  showMuteMemberModal.value = true
}

function closeMuteMemberModal() {
  if (actionLoading.value) {
    return
  }
  muteTargetMember.value = null
  muteMinutesInput.value = '10'
  showMuteMemberModal.value = false
}

async function toggleMuteMember(memberItem: GroupMember) {
  if (!groupDetail.value) {
    return
  }
  if (memberItem.muted) {
    actionLoading.value = true
    try {
      await groupApi.unmuteMember(groupDetail.value.id, memberItem.userId)
      message.success('已解除禁言')
      await loadGroupDetail()
    } catch (error: any) {
      console.error('toggleMuteMember error:', error)
      message.error(error.response?.data?.message || '操作失败')
    } finally {
      actionLoading.value = false
    }
    return
  }
  openMuteMemberModal(memberItem)
}

async function submitMuteMember() {
  if (!groupDetail.value || !muteTargetMember.value) {
    return
  }
  const muteMinutes = Number(muteMinutesInput.value)
  if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {
    message.warning('请输入 1 到 43200 之间的整数分钟数')
    return
  }

  actionLoading.value = true
  try {
    await groupApi.muteMember(groupDetail.value.id, muteTargetMember.value.userId, muteMinutes)
    message.success('已设置禁言')
    closeMuteMemberModal()
    await loadGroupDetail()
  } catch (error: any) {
    console.error('submitMuteMember error:', error)
    message.error(error.response?.data?.message || '设置禁言失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleRemoveMember(memberItem: GroupMember) {
  if (!groupDetail.value) {
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

  actionLoading.value = true
  try {
    await groupApi.removeMember(groupDetail.value.id, memberItem.userId)
    message.success('成员已移出群聊')
    await loadGroupDetail()
  } catch (error: any) {
    console.error('handleRemoveMember error:', error)
    message.error(error.response?.data?.message || '移除成员失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleDissolveGroup() {
  if (!groupId.value) {
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
    await groupApi.dissolve(groupId.value)
    message.success('群聊已解散')
    await router.replace('/chat')
  } catch (error: any) {
    console.error('handleDissolveGroup error:', error)
    message.error(error.response?.data?.message || '解散群聊失败')
  }
}

async function handleLeaveGroup() {
  if (!groupId.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认退出群聊',
    subtitle: groupDetail.value?.groupName || '当前群聊',
    description: '退出后你将不再接收该群消息，需要重新被邀请才能再次加入。',
    confirmText: '确认退出',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }

  try {
    await groupApi.leaveGroup(groupId.value)
    message.success('已退出群聊')
    await router.replace('/chat')
  } catch (error: any) {
    console.error('handleLeaveGroup error:', error)
    message.error(error.response?.data?.message || '退出群聊失败')
  }
}

watch(() => route.params.groupId, () => {
  searchText.value = ''
  roleFilter.value = 'all'
  mediaKeyword.value = ''
  mediaType.value = 'all'
  messageSearchKeyword.value = ''
  groupMessageSearchResults.value = []
  void loadGroupDetail()
  void loadGroupRequests()
  void loadGroupMedia()
})

onMounted(() => {
  void loadFriends()
  void loadGroupDetail()
  void loadGroupRequests()
  void loadGroupMedia()
})

onUnmounted(() => {
  resetGroupProfileDraft()
})
</script>

<style scoped>
.group-members-page {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 214, 143, 0.08), transparent 28%),
    radial-gradient(circle at left center, rgba(80, 145, 255, 0.08), transparent 24%),
    var(--linkx-bg);
}

.hidden-file-input {
  display: none;
}

.page-shell {
  width: min(1180px, 74vw, 100%);
  margin: 0 auto;
}

.page-header,
.group-hero-card,
.panel-card,
.member-card,
.modal-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-md);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--linkx-radius-lg);
}

.page-header-main,
.group-hero-top,
.member-card-main,
.member-name-row,
.group-meta-row,
.page-header-actions,
.group-stat-row,
.member-actions,
.modal-actions {
  display: flex;
  align-items: center;
}

.page-header-main {
  gap: 16px;
}

.page-header-actions,
.group-stat-row,
.member-actions,
.modal-actions {
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--linkx-text);
}

.page-subtitle,
.group-meta-row,
.member-username,
.member-status,
.filter-hint,
.panel-subtitle,
.modal-subtitle {
  color: var(--linkx-text-muted);
}

.page-subtitle {
  margin-top: 6px;
  font-size: 13px;
}

.back-btn,
.primary-btn,
.secondary-btn,
.mini-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  font-size: 13px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.back-btn,
.secondary-btn,
.mini-btn {
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
}

.primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.back-btn:hover,
.secondary-btn:hover,
.mini-btn:hover {
  background: var(--linkx-bg-hover);
}

.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.page-content {
  margin-top: 20px;
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  align-items: start;
  gap: 24px;
}

.side-column {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.group-hero-card,
.panel-card {
  border-radius: var(--linkx-radius-lg);
  padding: 22px;
}

.group-hero-card {
  position: relative;
  overflow: hidden;
}

.group-hero-card::after {
  content: '';
  position: absolute;
  inset: auto -60px -60px auto;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 214, 143, 0.14), transparent 68%);
  pointer-events: none;
}

.group-hero-top {
  align-items: flex-start;
  gap: 18px;
}

.group-avatar,
.member-avatar {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 24px;
  font-weight: 700;
}

.member-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  font-size: 18px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-hero-main,
.member-info {
  min-width: 0;
  flex: 1;
}

.group-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.group-name-row h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  color: var(--linkx-text);
}

.group-meta-row {
  gap: 14px;
  margin-top: 10px;
  font-size: 13px;
  flex-wrap: wrap;
}

.group-notice {
  margin-top: 14px;
  max-width: 680px;
  color: var(--linkx-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.stat-pill,
.role-chip,
.self-tag,
.mute-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-weight: 600;
}

.group-stat-row {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-pill {
  min-width: 0;
  padding: 14px 16px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  border-radius: 18px;
}

.stat-pill strong {
  color: var(--linkx-text);
  font-size: 20px;
}

.stat-pill.warn strong {
  color: #f0a020;
}

.stat-label {
  font-size: 12px;
}

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
}

.panel-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.panel-note {
  margin-top: 6px;
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-editor {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: start;
  gap: 16px;
}

.profile-avatar-editor {
  position: relative;
  width: 96px;
  height: 96px;
  border: 1px solid var(--linkx-border);
  border-radius: 28px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 30px;
  font-weight: 700;
  cursor: default;
}

.profile-avatar-editor.editable {
  cursor: pointer;
}

.profile-avatar-mask {
  position: absolute;
  inset: auto 0 0;
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.48);
  color: white;
  font-size: 12px;
  text-align: center;
}

.profile-fields {
  min-width: 0;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.field-label + .filled-input,
.field-label + .text-area {
  margin-bottom: 14px;
}

.filled-input {
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
}

.profile-action-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.preference-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.preference-form {
  display: grid;
  gap: 12px;
}

.preference-switch {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 6px 10px;
  align-items: start;
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
  color: var(--linkx-text);
}

.preference-switch input {
  margin-top: 3px;
}

.preference-switch span,
.preference-switch small {
  grid-column: 2;
}

.preference-switch small {
  color: var(--linkx-text-muted);
  line-height: 1.6;
}

.search-shell {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 44px;
  padding: 0 14px;
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  color: var(--linkx-text-muted);
}

.workspace-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.workspace-toolbar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: space-between;
  flex-wrap: wrap;
}

.workspace-search {
  margin-top: 0;
  min-width: min(300px, 100%);
  flex: 1;
}

.compact-btn {
  height: 34px;
  padding: 0 14px;
  font-size: 12px;
}

.tab-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.tab-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text-secondary);
  font-size: 12px;
  font-weight: 700;
  transition: var(--linkx-transition-fast);
}

.tab-btn.active {
  background: rgba(0, 214, 143, 0.14);
  border-color: rgba(0, 214, 143, 0.28);
  color: var(--linkx-primary);
}

.search-input,
.text-input {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--linkx-text);
  outline: none;
  font-size: 14px;
}

.role-filter-group {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.role-filter-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text-secondary);
  font-size: 13px;
  transition: var(--linkx-transition-fast);
}

.role-filter-btn.active {
  background: rgba(0, 214, 143, 0.14);
  border-color: rgba(0, 214, 143, 0.28);
  color: var(--linkx-primary);
}

.filter-hint {
  margin-top: 14px;
  font-size: 12px;
}

.manage-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.manage-subtitle {
  color: var(--linkx-text-muted);
  font-size: 13px;
  line-height: 1.7;
}

.manage-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.manage-summary-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
}

.manage-summary-label {
  display: block;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.manage-summary-item strong {
  display: block;
  margin-top: 8px;
  color: var(--linkx-text);
  font-size: 18px;
}

.manage-action-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.action-btn {
  width: 100%;
  justify-content: center;
}

.action-btn.warn {
  color: #d88914;
}

.action-btn.danger {
  color: #ff6b6b;
}

.manage-tip {
  color: var(--linkx-text-secondary);
  font-size: 12px;
  line-height: 1.7;
  padding: 12px 14px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--linkx-bg-hover) 68%, transparent);
}

.member-panel {
  min-width: 0;
  min-height: 100%;
}

.member-panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--linkx-border);
}

.member-panel-title-wrap {
  min-width: 0;
}

.member-panel-kicker {
  color: var(--linkx-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.member-panel-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(110px, 1fr));
  gap: 10px;
  width: min(420px, 100%);
}

.overview-badge {
  min-width: 0;
  padding: 14px 14px 12px;
  border-radius: 18px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);
}

.overview-label {
  display: block;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.overview-badge strong {
  display: block;
  margin-top: 8px;
  color: var(--linkx-text);
  font-size: 20px;
}

.member-panel-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 18px 0 20px;
  padding: 14px 16px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--linkx-bg-hover) 78%, transparent);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 82%, transparent);
}

.toolbar-hint {
  color: var(--linkx-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.request-panel {
  margin-bottom: 20px;
  padding: 18px;
  border-radius: 18px;
  background: color-mix(in srgb, var(--linkx-bg-hover) 52%, transparent);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 82%, transparent);
}

.request-panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.request-panel-count {
  flex-shrink: 0;
  min-width: 72px;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(0, 214, 143, 0.14);
  color: var(--linkx-primary);
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.request-list {
  display: grid;
  gap: 12px;
}

.request-card {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 18px;
  background: var(--linkx-bg-card);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);
}

.request-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.request-avatar.invite {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
}

.request-info {
  min-width: 0;
}

.request-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.request-name {
  color: var(--linkx-text);
  font-size: 14px;
  font-weight: 700;
}

.request-type-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 11px;
  font-weight: 700;
}

.request-type-tag.join {
  color: #ffb020;
  background: rgba(255, 176, 32, 0.12);
  border-color: rgba(255, 176, 32, 0.22);
}

.request-type-tag.invite {
  color: var(--linkx-primary);
  background: rgba(0, 214, 143, 0.12);
  border-color: rgba(0, 214, 143, 0.22);
}

.request-message {
  margin-top: 6px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.request-time {
  margin-top: 6px;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.request-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.request-action-btn {
  min-width: 72px;
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  transition: var(--linkx-transition-fast);
}

.request-action-btn.accept {
  background: rgba(0, 214, 143, 0.12);
  color: var(--linkx-primary);
}

.request-action-btn.reject {
  background: rgba(255, 107, 107, 0.12);
  color: #ff6b6b;
}

.request-empty-state {
  padding: 18px;
  border-radius: 16px;
  text-align: center;
  color: var(--linkx-text-muted);
  font-size: 13px;
  background: var(--linkx-bg-card);
  border: 1px dashed color-mix(in srgb, var(--linkx-border) 86%, transparent);
}

.search-result-list,
.media-list {
  margin-top: 18px;
  display: grid;
  gap: 14px;
}

.search-result-card,
.media-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 16px 18px;
  border-radius: 18px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
}

.result-main,
.media-info {
  min-width: 0;
}

.result-title-row,
.media-meta,
.media-actions,
.result-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.result-type-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(80, 145, 255, 0.12);
  color: #4f86ff;
  font-size: 11px;
  font-weight: 700;
}

.result-time,
.media-meta {
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.result-content {
  margin-top: 8px;
  color: var(--linkx-text-secondary);
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.media-card {
  grid-template-columns: 88px minmax(0, 1fr);
}

.media-cover {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--linkx-bg-card);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);
  color: var(--linkx-text-muted);
}

.media-cover.image {
  background: color-mix(in srgb, var(--linkx-bg-hover) 72%, transparent);
}

.media-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-name {
  color: var(--linkx-text);
  font-size: 15px;
  font-weight: 700;
  word-break: break-word;
}

.media-meta {
  margin-top: 8px;
}

.media-actions {
  margin-top: 12px;
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
}

.member-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.member-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: center;
  gap: 18px;
  border-radius: 20px;
  padding: 18px;
}

.member-card-main {
  align-items: flex-start;
  gap: 14px;
}

.member-name-row {
  gap: 8px;
  flex-wrap: wrap;
}

.member-name {
  color: var(--linkx-text);
  font-size: 15px;
  font-weight: 700;
}

.member-username,
.member-status {
  margin-top: 6px;
  font-size: 12px;
}

.member-card-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  min-width: 0;
}

.member-side-caption {
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.4;
}

.role-chip,
.self-tag,
.mute-tag {
  height: 24px;
  padding: 0 10px;
  font-size: 12px;
}

.role-chip.owner,
.self-role.owner {
  background: rgba(255, 187, 80, 0.18);
  color: #d88914;
}

.role-chip.admin,
.self-role.admin {
  background: rgba(80, 145, 255, 0.16);
  color: #4f86ff;
}

.role-chip.member,
.self-role.member,
.self-tag {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
}

.mute-tag {
  background: rgba(240, 160, 32, 0.16);
  color: #d88914;
}

.member-actions {
  margin-top: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.mini-btn.danger {
  color: #ff6b6b;
}

.empty-state,
.loading-panel {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-title,
.loading-text {
  color: var(--linkx-text);
  font-size: 16px;
  font-weight: 700;
}

.empty-subtitle {
  margin-top: 8px;
  color: var(--linkx-text-muted);
  font-size: 13px;
}

.empty-state.compact {
  min-height: 160px;
  padding: 12px 0;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
  animation: spin 0.8s linear infinite;
}

.loading-text {
  margin-top: 14px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 2100;
}

.mute-modal {
  width: min(420px, 100%);
  padding: 22px;
  border-radius: var(--linkx-radius-lg);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.modal-title {
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
}

.modal-subtitle {
  margin-top: 4px;
  font-size: 13px;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: var(--linkx-radius-md);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 18px;
  line-height: 1;
}

.modal-body {
  margin: 20px 0 18px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.text-input {
  height: 42px;
  padding: 0 14px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
}

.picker-modal {
  width: min(560px, 100%);
  padding: 22px;
  border-radius: var(--linkx-radius-lg);
}

.member-picker-list {
  display: grid;
  gap: 12px;
  max-height: 340px;
  margin-top: 18px;
  padding-right: 4px;
  overflow-y: auto;
}

.member-picker-item {
  display: grid;
  grid-template-columns: auto 48px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);
  cursor: pointer;
}

.member-picker-item input {
  margin: 0;
}

.member-picker-avatar {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.member-picker-info {
  min-width: 0;
}

.member-picker-name,
.member-picker-meta {
  display: block;
}

.member-picker-name {
  color: var(--linkx-text);
  font-size: 14px;
  font-weight: 700;
}

.member-picker-meta {
  margin-top: 4px;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.picker-empty-state {
  margin-top: 18px;
  padding: 18px;
  border-radius: 16px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-muted);
  font-size: 13px;
  line-height: 1.7;
  text-align: center;
}

.text-area {
  width: 100%;
  min-height: 92px;
  padding: 12px 14px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  resize: vertical;
  outline: none;
  font-size: 14px;
  line-height: 1.6;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1400px) {
  .page-shell {
    width: min(1120px, 84vw, 100%);
  }
}

@media (max-width: 1100px) {
  .page-shell {
    width: min(920px, 92vw, 100%);
  }

  .page-content {
    grid-template-columns: 1fr;
  }

  .side-column {
    position: static;
  }

  .member-panel-head,
  .member-panel-toolbar,
  .member-card,
  .request-card {
    grid-template-columns: 1fr;
  }

  .member-panel-head,
  .member-panel-toolbar {
    display: grid;
  }

  .member-panel-overview {
    width: 100%;
  }

  .manage-summary-grid {
    grid-template-columns: 1fr;
  }

  .profile-editor {
    grid-template-columns: 1fr;
  }

  .member-card-side {
    align-items: flex-start;
  }

  .member-actions {
    justify-content: flex-start;
  }

  .request-actions {
    justify-content: flex-start;
  }

  .search-result-card,
  .media-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .group-members-page {
    padding: 16px;
  }

  .page-shell {
    width: 100%;
  }

  .page-header,
  .page-header-main,
  .page-header-actions,
  .group-hero-top,
  .member-card-main,
  .member-panel-toolbar,
  .profile-action-row,
  .request-panel-head,
  .workspace-head,
  .workspace-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header-actions,
  .member-actions,
  .modal-actions {
    width: 100%;
  }

  .back-btn,
  .primary-btn,
  .secondary-btn,
  .mini-btn {
    width: 100%;
    justify-content: center;
  }

  .group-stat-row {
    grid-template-columns: 1fr;
  }

  .member-picker-item {
    grid-template-columns: auto 40px minmax(0, 1fr);
  }

  .member-picker-avatar {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    font-size: 16px;
  }

  .member-panel-overview {
    grid-template-columns: 1fr;
  }

  .member-actions {
    justify-content: stretch;
  }

  .request-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .tab-group,
  .media-actions,
  .result-actions,
  .result-title-row,
  .media-meta {
    width: 100%;
  }
}
</style>
