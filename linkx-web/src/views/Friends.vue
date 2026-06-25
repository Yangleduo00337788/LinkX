<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="content-area">
    <!-- Friends List Panel -->
    <!-- 行注：渲染容器 -->
    <div class="friends-panel">
      <!-- 行注：渲染容器 -->
      <div class="panel-header">
        <!-- 行注：展示“联系人”文案 -->
        <span class="header-title">联系人</span>
        <!-- 行注：渲染按钮 -->
        <button class="refresh-btn" @click="loadAll" title="刷新">
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标折线 -->
            <polyline points="23 4 23 10 17 10"/>
            <!-- 行注：补充图标路径 -->
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="friends-search">
        <!-- 行注：渲染容器 -->
        <div class="search-input-wrapper">
          <!-- 行注：渲染图标容器 -->
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="11" cy="11" r="8"/>
            <!-- 行注：补充图标线段 -->
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          <!-- 行注：结束图标容器 -->
          </svg>
          <!-- 行注：渲染输入框 -->
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索用户..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="friends-list">
        <!-- 行注：渲染容器 -->
        <div
          v-for="f in friends"
          :key="f.friendId"
          class="friend-item"
          @click="startChat(f.friendId)"
        >
          <!-- 行注：渲染容器 -->
          <div class="friend-avatar" @click.stop="showFriendInfo(f)">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="f.friendAvatar" :src="f.friendAvatar" class="avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else>{{ f.friendNickname?.charAt(0) }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="friend-info">
            <!-- 行注：渲染容器 -->
            <div class="friend-name">{{ f.friendNickname }}</div>
            <!-- 行注：展示“@{{ f.friendUserna”文案 -->
            <div class="friend-username">@{{ f.friendUsername }}</div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="friend-actions">
            <!-- 行注：渲染按钮 -->
            <button class="action-btn" @click.stop="startChat(f.friendId)" title="发消息">
              <!-- 行注：渲染图标容器 -->
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="friends.length === 0" class="empty-state">
          <!-- 行注：渲染容器 -->
          <div class="empty-icon">
            <!-- 行注：渲染图标容器 -->
            <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <!-- 行注：补充图标路径 -->
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <!-- 行注：补充图标圆形路径 -->
              <circle cx="9" cy="7" r="4"/>
              <!-- 行注：补充图标路径 -->
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <!-- 行注：补充图标路径 -->
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            <!-- 行注：结束图标容器 -->
            </svg>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：展示“暂无好友”文案 -->
          <div class="empty-title">暂无好友</div>
          <!-- 行注：展示“去添加一些好友吧”文案 -->
          <div class="empty-subtitle">去添加一些好友吧</div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- Right Panel -->
    <!-- 行注：渲染容器 -->
    <div class="right-panel">
      <!-- 行注：渲染容器 -->
      <div class="tabs-header">
        <!-- 行注：渲染按钮 -->
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'requests' }"
          @click="activeTab = 'requests'"
        >
          <!-- 行注：展示“好友申请”文案 -->
          好友申请
          <!-- 行注：渲染文本节点 -->
          <span v-if="requests.length" class="tab-badge">{{ requests.length }}</span>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'group-requests' }"
          @click="activeTab = 'group-requests'"
        >
          <!-- 行注：展示“群通知”文案 -->
          群通知
          <!-- 行注：渲染文本节点 -->
          <span v-if="groupRequests.length" class="tab-badge">{{ groupRequests.length }}</span>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'join-group' }"
          @click="activeTab = 'join-group'"
        >
          <!-- 行注：展示“加入群聊”文案 -->
          加入群聊
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'my-groups' }"
          @click="activeTab = 'my-groups'"
        >
          <!-- 行注：展示“我的群聊”文案 -->
          我的群聊
          <!-- 行注：渲染文本节点 -->
          <span v-if="myGroups.length" class="tab-badge">{{ myGroups.length }}</span>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'search' }"
          @click="activeTab = 'search'"
        >
          <!-- 行注：展示“搜索用户”文案 -->
          搜索用户
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="tab-content">
        <!-- Requests Tab -->
        <!-- 行注：渲染容器 -->
        <div v-if="activeTab === 'requests'" class="tab-pane">
          <!-- 行注：渲染 FriendsRequestsPanel 组件 -->
          <FriendsRequestsPanel
            :requests="requests"
            @accept="handleAccept"
            @reject="handleReject"
          />
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="activeTab === 'group-requests'" class="tab-pane">
          <!-- 行注：渲染 FriendsGroupRequestsPanel 组件 -->
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
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="activeTab === 'join-group'" class="tab-pane">
          <!-- 行注：渲染容器 -->
          <div class="join-group-search-section">
            <!-- 行注：渲染容器 -->
            <div class="join-group-search-wrapper">
              <!-- 行注：渲染图标容器 -->
              <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="11" cy="11" r="8"/>
                <!-- 行注：补充图标线段 -->
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：渲染输入框 -->
              <input
                v-model="groupSearchKeyword"
                type="text"
                class="join-group-search-input"
                placeholder="输入群名称或群 ID 搜索..."
                maxlength="100"
                @keyup.enter="handleGroupSearch"
              />
              <!-- 行注：渲染按钮 -->
              <button v-if="groupSearchKeyword" class="join-group-search-clear" @click="clearGroupSearch">
                <!-- 行注：渲染图标容器 -->
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标线段 -->
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <!-- 行注：补充图标线段 -->
                  <line x1="6" y1="6" x2="18" y2="18"/>
                <!-- 行注：结束图标容器 -->
                </svg>
              <!-- 行注：结束按钮 -->
              </button>
              <!-- 行注：渲染按钮 -->
              <button class="join-group-search-btn" :disabled="searchingGroup" @click="handleGroupSearch">
                <!-- 行注：渲染动态文本 -->
                {{ searchingGroup ? '搜索中...' : '搜索' }}
              <!-- 行注：结束按钮 -->
              </button>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-if="groupSearchResults.length > 0" class="group-search-results">
            <!-- 行注：渲染容器 -->
            <div class="group-search-results-header">
              <!-- 行注：展示“搜索结果”文案 -->
              <span>搜索结果</span>
              <!-- 行注：渲染文本节点 -->
              <span class="group-search-results-count">{{ groupSearchResults.length }} 个群</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div
              v-for="g in groupSearchResults"
              :key="g.id"
              class="group-search-result-card"
            >
              <!-- 行注：渲染容器 -->
              <div class="group-search-result-avatar">
                <!-- 行注：渲染图片 -->
                <ProtectedImage v-if="g.groupAvatar" :src="g.groupAvatar" class="avatar-img" />
                <!-- 行注：渲染文本节点 -->
                <span v-else>{{ g.groupName?.charAt(0) || '群' }}</span>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-search-result-info">
                <!-- 行注：渲染容器 -->
                <div class="group-search-result-name">{{ g.groupName }}</div>
                <!-- 行注：渲染容器 -->
                <div class="group-search-result-meta">
                  <!-- 行注：展示“群号：{{ g.id }}”文案 -->
                  <span class="group-search-result-id">群号：{{ g.id }}</span>
                  <!-- 行注：展示“·”文案 -->
                  <span class="group-search-result-sep">·</span>
                  <!-- 行注：渲染文本节点 -->
                  <span>{{ g.memberCount || 0 }} 人</span>
                  <!-- 行注：开始定义模板区域 -->
                  <template v-if="g.myRole !== null && g.myRole !== undefined">
                    <!-- 行注：展示“·”文案 -->
                    <span class="group-search-result-sep">·</span>
                    <!-- 行注：展示“已加入”文案 -->
                    <span class="group-search-result-self">已加入</span>
                  <!-- 行注：结束模板区域 -->
                  </template>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div v-if="g.notice" class="group-search-result-notice">{{ g.notice }}</div>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-search-result-actions">
                <!-- 行注：渲染按钮 -->
                <button
                  class="group-preview-btn"
                  @click="openGroupPreview(g)"
                >
                  <!-- 行注：展示“查看资料”文案 -->
                  查看资料
                <!-- 行注：结束按钮 -->
                </button>
                <!-- 行注：渲染按钮 -->
                <button
                  v-if="isJoinedGroup(g.id, g.myRole)"
                  class="group-search-enter-btn"
                  @click="startGroupChat(g.id)"
                >
                  <!-- 行注：展示“进入群聊”文案 -->
                  进入群聊
                <!-- 行注：结束按钮 -->
                </button>
                <!-- 行注：渲染按钮 -->
                <button
                  v-else-if="hasPendingJoinRequest(g.id)"
                  class="group-search-pending-btn"
                  disabled
                >
                  <!-- 行注：展示“申请已提交”文案 -->
                  申请已提交
                <!-- 行注：结束按钮 -->
                </button>
                <!-- 行注：渲染按钮 -->
                <button
                  v-else
                  class="group-search-join-btn"
                  @click="openJoinGroupModal(g)"
                >
                  <!-- 行注：展示“申请加入”文案 -->
                  申请加入
                <!-- 行注：结束按钮 -->
                </button>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-else-if="groupSearchKeyword && !searchingGroup" class="empty-state">
            <!-- 行注：渲染容器 -->
            <div class="empty-icon">
              <!-- 行注：渲染图标容器 -->
              <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="11" cy="11" r="8"/>
                <!-- 行注：补充图标线段 -->
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“未找到相关群聊”文案 -->
            <div class="empty-title">未找到相关群聊</div>
            <!-- 行注：展示“请尝试其他关键词”文案 -->
            <div class="empty-subtitle">请尝试其他关键词</div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-else-if="!groupSearchKeyword" class="join-group-shell">
            <!-- 行注：渲染容器 -->
            <div class="join-group-card">
              <!-- 行注：渲染容器 -->
              <div class="join-group-head">
                <!-- 行注：渲染容器 -->
                <div>
                  <!-- 行注：展示“按群 ID 申请加入”文案 -->
                  <div class="join-group-title">按群 ID 申请加入</div>
                  <!-- 行注：展示“输入群 ID 并附带申请说明，等待群”文案 -->
                  <div class="join-group-subtitle">输入群 ID 并附带申请说明，等待群主或管理员处理</div>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div class="join-group-icon">
                  <!-- 行注：渲染图标容器 -->
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <!-- 行注：补充图标路径 -->
                    <path d="M12 5v14"/>
                    <!-- 行注：补充图标路径 -->
                    <path d="M5 12h14"/>
                    <!-- 行注：补充图标矩形路径 -->
                    <rect x="3" y="3" width="18" height="18" rx="4"/>
                  <!-- 行注：结束图标容器 -->
                  </svg>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="join-group-form">
                <!-- 行注：展示“群 ID”文案 -->
                <label class="join-group-label" for="join-group-id">群 ID</label>
                <!-- 行注：渲染输入框 -->
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
                <!-- 行注：展示“申请说明”文案 -->
                <label class="join-group-label" for="join-group-message">申请说明</label>
                <!-- 行注：渲染文本域 -->
                <textarea
                  id="join-group-message"
                  v-model="joinGroupMessage"
                  class="join-group-textarea"
                  rows="4"
                  maxlength="255"
                  placeholder="例如：我是产品组同事，申请加入项目沟通群"
                ></textarea>
                <!-- 行注：渲染容器 -->
                <div class="join-group-foot">
                  <!-- 行注：展示“提交后会进入群通知，等待对方确认。”文案 -->
                  <div class="join-group-hint">提交后会进入群通知，等待对方确认。</div>
                  <!-- 行注：渲染按钮 -->
                  <button class="join-group-btn" :disabled="joiningGroup" @click="handleJoinGroup">
                    <!-- 行注：渲染动态文本 -->
                    {{ joiningGroup ? '提交中...' : '发送申请' }}
                  <!-- 行注：结束按钮 -->
                  </button>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="activeTab === 'my-groups'" class="tab-pane">
          <!-- 行注：渲染容器 -->
          <div v-if="myGroups.length > 0" class="group-grid">
            <!-- 行注：渲染容器 -->
            <div
              v-for="group in myGroups"
              :key="group.id"
              class="group-card"
              @click="startGroupChat(group.id)"
            >
              <!-- 行注：渲染容器 -->
              <div class="group-card-top">
                <!-- 行注：渲染容器 -->
                <div class="group-card-avatar">
                  <!-- 行注：渲染图片 -->
                  <ProtectedImage v-if="group.groupAvatar" :src="group.groupAvatar" class="avatar-img" />
                  <!-- 行注：渲染文本节点 -->
                  <span v-else>{{ group.groupName?.charAt(0) || '群' }}</span>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div class="group-card-meta">
                  <!-- 行注：渲染容器 -->
                  <div class="group-card-name">{{ group.groupName }}</div>
                  <!-- 行注：渲染容器 -->
                  <div class="group-card-id-row">
                    <!-- 行注：展示“群号：{{ group.id }}”文案 -->
                    <span class="group-card-id">群号：{{ group.id }}</span>
                    <!-- 行注：展示“复制群号”文案 -->
                    <button class="group-copy-btn" @click.stop="copyGroupId(group.id)">复制群号</button>
                  <!-- 行注：结束容器 -->
                  </div>
                  <!-- 行注：渲染容器 -->
                  <div class="group-card-subtitle">
                    <!-- 行注：渲染动态文本 -->
                    {{ group.memberCount || 0 }} 人
                    <!-- 行注：开始定义模板区域 -->
                    <template v-if="group.notice"> · {{ group.notice }}</template>
                  <!-- 行注：结束容器 -->
                  </div>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染文本节点 -->
                <span class="group-role-badge" :class="groupRoleClass(group.myRole)">
                  <!-- 行注：渲染动态文本 -->
                  {{ groupRoleText(group.myRole) }}
                <!-- 行注：结束文本节点 -->
                </span>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-card-bottom">
                <!-- 行注：渲染容器 -->
                <div class="group-card-time">
                  <!-- 行注：渲染动态文本 -->
                  {{ group.createTime ? `创建于 ${group.createTime.substring(0, 10)}` : '群聊已创建' }}
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div class="group-card-actions">
                  <!-- 行注：展示“查看资料”文案 -->
                  <button class="group-preview-btn" @click.stop="openGroupPreview(group)">查看资料</button>
                  <!-- 行注：展示“进入群聊”文案 -->
                  <button class="group-enter-btn" @click.stop="startGroupChat(group.id)">进入群聊</button>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-else class="empty-state">
            <!-- 行注：渲染容器 -->
            <div class="empty-icon">
              <!-- 行注：渲染图标容器 -->
              <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <!-- 行注：补充图标矩形路径 -->
                <rect x="3" y="3" width="18" height="18" rx="4"/>
                <!-- 行注：补充图标路径 -->
                <path d="M12 8v8"/>
                <!-- 行注：补充图标路径 -->
                <path d="M8 12h8"/>
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“暂无我的群聊”文案 -->
            <div class="empty-title">暂无我的群聊</div>
            <!-- 行注：展示“创建群聊或通过邀请、申请加入群聊后会”文案 -->
            <div class="empty-subtitle">创建群聊或通过邀请、申请加入群聊后会显示在这里</div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- Search Tab -->
        <!-- 行注：渲染容器 -->
        <div v-if="activeTab === 'search'" class="tab-pane">
          <!-- 行注：渲染 FriendsSearchPanel 组件 -->
          <FriendsSearchPanel
            :search-keyword="searchKeyword"
            :search-results="searchResults"
            @add-friend="handleAddFriend"
          />
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
  <!-- 行注：渲染 FriendsGroupPreviewDialog 组件 -->
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
  <!-- 行注：渲染 FriendsUserDialog 组件 -->
  <FriendsUserDialog
    :visible="showUserInfo"
    :selected-friend="selectedFriend"
    @close="closeUserInfo"
    @send-message="friendId => { closeUserInfo(); startChat(friendId) }"
    @blacklist="handleBlacklist"
  />
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 联系人页面，处理好友、群组和申请管理。
 */
import ProtectedImage from '../components/ProtectedImage.vue'
import FriendsGroupPreviewDialog from '../components/friends/FriendsGroupPreviewDialog.vue'
import FriendsGroupRequestsPanel from '../components/friends/FriendsGroupRequestsPanel.vue'  // 行注：引入 FriendsGroupRequestsPanel 组件
import FriendsRequestsPanel from '../components/friends/FriendsRequestsPanel.vue'  // 行注：引入 FriendsRequestsPanel 组件
import FriendsSearchPanel from '../components/friends/FriendsSearchPanel.vue'  // 行注：引入 FriendsSearchPanel 组件
import FriendsUserDialog from '../components/friends/FriendsUserDialog.vue'  // 行注：引入 FriendsUserDialog 组件
import { ref, onMounted, onUnmounted } from 'vue'  // 行注：引入 ref, onMounted, onUnmounted 能力
import { useRouter } from 'vue-router'  // 行注：引入 useRouter 能力
import { useFriendsGroups } from '../hooks/useFriendsGroups'  // 行注：引入 useFriendsGroups 能力
import { useFriendsUsers } from '../hooks/useFriendsUsers'  // 行注：引入 useFriendsUsers 能力
import { useMessage } from 'naive-ui'  // 行注：引入 useMessage 能力
import { useGroupRequests } from '../hooks/useGroupRequests'  // 行注：引入 useGroupRequests 能力
import { useUserStore } from '../stores/user'  // 行注：引入 useUserStore 能力

const router = useRouter()  // 行注：获取路由实例
const message = useMessage()  // 行注：获取全局消息实例
const userStore = useUserStore()  // 行注：获取 userStore 组合式能力
const activeTab = ref('requests')  // 行注：初始化 activeTab 响应式状态
const refreshingPanels = ref(false)  // 行注：初始化 refreshingPanels 响应式状态

const PANEL_REFRESH_INTERVAL = 10000  // 行注：初始化 PANEL_REFRESH_INTERVAL 变量
let lastPanelRefreshAt = 0  // 行注：初始化 lastPanelRefreshAt 变量

const {  // 行注：开始解构当前返回值
  searchKeyword,  // 行注：解构 searchKeyword 状态
  friends,  // 行注：解构 friends 状态
  requests,  // 行注：解构 requests 状态
  searchResults,  // 行注：解构 searchResults 状态
  showUserInfo,  // 行注：解构 showUserInfo 状态
  selectedFriend,  // 行注：解构 selectedFriend 状态
  loadFriends,  // 行注：解构 loadFriends 方法
  loadRequests,  // 行注：解构 loadRequests 方法
  handleSearch,  // 行注：解构 handleSearch 方法
  handleAddFriend,  // 行注：解构 handleAddFriend 方法
  handleAccept,  // 行注：解构 handleAccept 方法
  handleReject,  // 行注：解构 handleReject 方法
  startChat,  // 行注：解构 startChat 方法
  handleBlacklist,  // 行注：解构 handleBlacklist 方法
  showFriendInfo,  // 行注：解构 showFriendInfo 状态
  closeUserInfo  // 行注：解构 closeUserInfo 方法
} = useFriendsUsers({  // 行注：从 useFriendsUsers 中解构所需能力
  activeTab,  // 行注：传入 activeTab 参数
  router,  // 行注：传入 router 参数
  message,  // 行注：传入 message 参数
  getCurrentNickname: () => userStore.nickname?.trim() || '新朋友'  // 行注：传入 getCurrentNickname 回调
})  // 行注：结束当前调用配置

const {  // 行注：开始解构当前返回值
  joinGroupId,  // 行注：解构 joinGroupId 状态
  joinGroupMessage,  // 行注：解构 joinGroupMessage 状态
  joiningGroup,  // 行注：解构 joiningGroup 状态
  groupSearchKeyword,  // 行注：解构 groupSearchKeyword 状态
  searchingGroup,  // 行注：解构 searchingGroup 状态
  groupSearchResults,  // 行注：解构 groupSearchResults 状态
  myGroups,  // 行注：解构 myGroups 状态
  showGroupPreview,  // 行注：解构 showGroupPreview 状态
  previewGroupDetail,  // 行注：解构 previewGroupDetail 状态
  hasPendingJoinRequest,  // 行注：解构 hasPendingJoinRequest 状态
  isJoinedGroup,  // 行注：解构 isJoinedGroup 状态
  loadMyGroups,  // 行注：解构 loadMyGroups 方法
  handleJoinGroup,  // 行注：解构 handleJoinGroup 方法
  handleGroupSearch,  // 行注：解构 handleGroupSearch 方法
  clearGroupSearch,  // 行注：解构 clearGroupSearch 方法
  openJoinGroupModal,  // 行注：解构 openJoinGroupModal 方法
  applyJoinFromPreview,  // 行注：解构 applyJoinFromPreview 方法
  groupRoleText,  // 行注：解构 groupRoleText 状态
  groupRoleClass,  // 行注：解构 groupRoleClass 状态
  startGroupChat,  // 行注：解构 startGroupChat 方法
  copyGroupId,  // 行注：解构 copyGroupId 方法
  openGroupPreview,  // 行注：解构 openGroupPreview 方法
  refreshOpenGroupPreview,  // 行注：解构 refreshOpenGroupPreview 方法
  closeGroupPreview  // 行注：解构 closeGroupPreview 方法
} = useFriendsGroups({  // 行注：从 useFriendsGroups 中解构所需能力
  activeTab,  // 行注：传入 activeTab 参数
  router,  // 行注：传入 router 参数
  message  // 行注：传入 message 参数
})  // 行注：结束当前调用配置

const {  // 行注：开始解构当前返回值
  groupRequests,  // 行注：解构 groupRequests 状态
  loadGroupRequests,  // 行注：解构 loadGroupRequests 方法
  groupRequestTypeText,  // 行注：解构 groupRequestTypeText 状态
  groupRequestTagClass,  // 行注：解构 groupRequestTagClass 状态
  buildGroupRequestMessage,  // 行注：解构 buildGroupRequestMessage 方法
  formatRequestTime,  // 行注：解构 formatRequestTime 方法
  isRequestActionLoading,  // 行注：解构 isRequestActionLoading 状态
  handleAcceptGroupRequest,  // 行注：解构 handleAcceptGroupRequest 方法
  handleRejectGroupRequest  // 行注：解构 handleRejectGroupRequest 方法
} = useGroupRequests({  // 行注：从 useGroupRequests 中解构所需能力
  message,  // 行注：传入 message 参数
  afterAccept: () => refreshPanelsAfterGroupRequestAction(),  // 行注：传入 afterAccept 回调
  afterReject: () => refreshPanelsAfterGroupRequestAction()  // 行注：传入 afterReject 回调
})  // 行注：结束当前调用配置

async function refreshPanelsAfterGroupRequestAction() {  // 行注：定义异步 refreshPanelsAfterGroupRequestAction 方法
  await Promise.all([loadFriends(), loadRequests(), loadMyGroups()])  // 行注：并行执行多项异步任务
  await refreshOpenGroupPreview()  // 行注：调用 refreshOpenGroupPreview 方法
  lastPanelRefreshAt = Date.now()  // 行注：更新 lastPanelRefreshAt 值
}  // 行注：结束当前代码块

async function refreshPanels(force = false) {  // 行注：定义异步 refreshPanels 方法
  const now = Date.now()  // 行注：记录 now 时间戳
  if (refreshingPanels.value) {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (!force && now - lastPanelRefreshAt < PANEL_REFRESH_INTERVAL) {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  refreshingPanels.value = true  // 行注：更新 refreshingPanels 状态
  try {  // 行注：尝试执行可能失败的逻辑
    await Promise.all([loadFriends(), loadRequests(), loadGroupRequests(), loadMyGroups()])  // 行注：并行执行多项异步任务
    await refreshOpenGroupPreview()  // 行注：调用 refreshOpenGroupPreview 方法
    lastPanelRefreshAt = Date.now()  // 行注：更新 lastPanelRefreshAt 值
  } finally {  // 行注：执行收尾清理逻辑
    refreshingPanels.value = false  // 行注：更新 refreshingPanels 状态
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

function queueRefreshPanels(force = false) {  // 行注：定义 queueRefreshPanels 方法
  void refreshPanels(force).catch((error: any) => {  // 行注：追加异常兜底处理
    console.error('refreshPanels error:', error)  // 行注：输出错误日志
  })  // 行注：结束当前调用配置
}  // 行注：结束当前代码块

async function loadAll() {  // 行注：定义异步 loadAll 方法
  try {  // 行注：尝试执行可能失败的逻辑
    await refreshPanels(true)  // 行注：调用 refreshPanels 方法
    message.success('已刷新')  // 行注：提示成功信息
  } catch (e) {  // 行注：捕获并处理异常
    message.error('刷新失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

function handleEscape(e: KeyboardEvent) {  // 行注：定义 handleEscape 方法
  if (e.key !== 'Escape') {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (showGroupPreview.value) {  // 行注：判断当前条件是否成立
    closeGroupPreview()  // 行注：调用 closeGroupPreview 方法
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  closeUserInfo()  // 行注：调用 closeUserInfo 方法
}  // 行注：结束当前代码块

function handleWindowFocus() {  // 行注：定义 handleWindowFocus 方法
  queueRefreshPanels(false)  // 行注：调用 queueRefreshPanels 方法
}  // 行注：结束当前代码块

function handleVisibilityChange() {  // 行注：定义 handleVisibilityChange 方法
  if (document.visibilityState === 'visible') {  // 行注：判断当前条件是否成立
    queueRefreshPanels(false)  // 行注：调用 queueRefreshPanels 方法
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

onMounted(async () => {  // 行注：注册组件挂载后的初始化逻辑
  try {  // 行注：尝试执行可能失败的逻辑
    await refreshPanels(true)  // 行注：调用 refreshPanels 方法
  } catch (error: any) {  // 行注：捕获并处理异常
    console.error('initFriendsPage error:', error)  // 行注：输出错误日志
    message.error(error.response?.data?.message || '初始化联系人页失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
  document.addEventListener('keydown', handleEscape)  // 行注：注册键盘按下事件监听
  window.addEventListener('focus', handleWindowFocus)  // 行注：注册获得焦点事件监听
  document.addEventListener('visibilitychange', handleVisibilityChange)  // 行注：注册可见性变化事件监听
})  // 行注：结束当前调用配置

onUnmounted(() => {  // 行注：注册组件卸载时的清理逻辑
  document.removeEventListener('keydown', handleEscape)  // 行注：移除键盘按下事件监听
  window.removeEventListener('focus', handleWindowFocus)  // 行注：移除获得焦点事件监听
  document.removeEventListener('visibilitychange', handleVisibilityChange)  // 行注：移除可见性变化事件监听
})  // 行注：结束当前调用配置
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.content-area {  /* 行注：定义 .content-area 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

/* Friends Panel */
.friends-panel {  /* 行注：定义 .friends-panel 样式 */
  width: 320px;  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-right: 1px solid var(--linkx-border);  /* 行注：设置 border-right 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  min-width: 280px;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.panel-header {  /* 行注：定义 .panel-header 样式 */
  height: 56px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.header-title {  /* 行注：定义 .header-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  letter-spacing: 0.3px;  /* 行注：设置 letter-spacing 样式 */
}  /* 行注：结束当前样式块 */

.refresh-btn {  /* 行注：定义 .refresh-btn 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.refresh-btn:hover {  /* 行注：定义 .refresh-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.friends-search {  /* 行注：定义 .friends-search 样式 */
  padding: 12px 16px;  /* 行注：设置 padding 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.search-input-wrapper {  /* 行注：定义 .search-input-wrapper 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.search-icon {  /* 行注：定义 .search-icon 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  left: 12px;  /* 行注：设置 left 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  pointer-events: none;  /* 行注：设置 pointer-events 样式 */
}  /* 行注：结束当前样式块 */

.search-input {  /* 行注：定义 .search-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  padding: 0 12px 0 36px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.search-input::placeholder {  /* 行注：定义 .search-input::placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.search-input:focus {  /* 行注：定义 .search-input:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.friends-list {  /* 行注：定义 .friends-list 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  padding: 8px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.friend-item {  /* 行注：定义 .friend-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 12px;  /* 行注：设置 padding 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.friend-item:hover {  /* 行注：定义 .friend-item:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.friend-avatar {  /* 行注：定义 .friend-avatar 样式 */
  width: 44px;  /* 行注：设置 width 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  box-shadow: 0 2px 8px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.friend-info {  /* 行注：定义 .friend-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.friend-name {  /* 行注：定义 .friend-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 2px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.friend-username {  /* 行注：定义 .friend-username 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.friend-actions {  /* 行注：定义 .friend-actions 样式 */
  opacity: 0;  /* 行注：设置 opacity 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.friend-item:hover .friend-actions {  /* 行注：定义 .friend-item:hover .friend-actions 样式 */
  opacity: 1;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.action-btn {  /* 行注：定义 .action-btn 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.action-btn:hover {  /* 行注：定义 .action-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

/* Right Panel */
.right-panel {  /* 行注：定义 .right-panel 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.tabs-header {  /* 行注：定义 .tabs-header 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  padding: 16px 20px 0;  /* 行注：设置 padding 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.tab-btn {  /* 行注：定义 .tab-btn 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  padding: 10px 20px;  /* 行注：设置 padding 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius) var(--linkx-radius) 0 0;  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
  position: relative;  /* 行注：设置 position 样式 */
}  /* 行注：结束当前样式块 */

.tab-btn:hover {  /* 行注：定义 .tab-btn:hover 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.tab-btn.active {  /* 行注：定义 .tab-btn.active 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.tab-btn.active::after {  /* 行注：定义 .tab-btn.active::after 样式 */
  content: '';  /* 行注：设置 content 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  bottom: 0;  /* 行注：设置 bottom 样式 */
  left: 0;  /* 行注：设置 left 样式 */
  right: 0;  /* 行注：设置 right 样式 */
  height: 2px;  /* 行注：设置 height 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border-radius: 2px 2px 0 0;  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.tab-badge {  /* 行注：定义 .tab-badge 样式 */
  min-width: 18px;  /* 行注：设置 min-width 样式 */
  height: 18px;  /* 行注：设置 height 样式 */
  padding: 0 5px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-error);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.tab-content {  /* 行注：定义 .tab-content 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  padding: 16px 20px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.tab-pane {  /* 行注：定义 .tab-pane 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
}  /* 行注：结束当前样式块 */

/* Group Search */
.join-group-search-section {  /* 行注：定义 .join-group-search-section 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-wrapper {  /* 行注：定义 .join-group-search-wrapper 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-wrapper .search-icon {  /* 行注：定义 .join-group-search-wrapper .search-icon 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  left: 14px;  /* 行注：设置 left 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  pointer-events: none;  /* 行注：设置 pointer-events 样式 */
  z-index: 1;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-input {  /* 行注：定义 .join-group-search-input 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  padding: 0 100px 0 38px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-input::placeholder {  /* 行注：定义 .join-group-search-input::placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-input:focus {  /* 行注：定义 .join-group-search-input:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-clear {  /* 行注：定义 .join-group-search-clear 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  right: 96px;  /* 行注：设置 right 样式 */
  width: 24px;  /* 行注：设置 width 样式 */
  height: 24px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-clear:hover {  /* 行注：定义 .join-group-search-clear:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-btn {  /* 行注：定义 .join-group-search-btn 样式 */
  min-width: 80px;  /* 行注：设置 min-width 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-btn:hover:not(:disabled) {  /* 行注：定义 .join-group-search-btn:hover:not(:disabled) 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.join-group-search-btn:disabled {  /* 行注：定义 .join-group-search-btn:disabled 样式 */
  opacity: 0.6;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-search-results {  /* 行注：定义 .group-search-results 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-search-results-header {  /* 行注：定义 .group-search-results-header 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.group-search-results-count {  /* 行注：定义 .group-search-results-count 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 500;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-card {  /* 行注：定义 .group-search-result-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 14px 16px;  /* 行注：设置 padding 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-card:hover {  /* 行注：定义 .group-search-result-card:hover 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-avatar {  /* 行注：定义 .group-search-result-avatar 样式 */
  width: 48px;  /* 行注：设置 width 样式 */
  height: 48px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.22);  /* 行注：设置 box-shadow 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-info {  /* 行注：定义 .group-search-result-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-name {  /* 行注：定义 .group-search-result-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-meta {  /* 行注：定义 .group-search-result-meta 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-id {  /* 行注：定义 .group-search-result-id 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-sep {  /* 行注：定义 .group-search-result-sep 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-self {  /* 行注：定义 .group-search-result-self 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-notice {  /* 行注：定义 .group-search-result-notice 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  margin-top: 4px;  /* 行注：设置 margin-top 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-search-result-actions {  /* 行注：定义 .group-search-result-actions 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.group-search-join-btn {  /* 行注：定义 .group-search-join-btn 样式 */
  min-width: 88px;  /* 行注：设置 min-width 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-search-join-btn:hover {  /* 行注：定义 .group-search-join-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.group-search-pending-btn {  /* 行注：定义 .group-search-pending-btn 样式 */
  min-width: 88px;  /* 行注：设置 min-width 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
  opacity: 0.72;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.group-search-enter-btn {  /* 行注：定义 .group-search-enter-btn 样式 */
  min-width: 88px;  /* 行注：设置 min-width 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 166, 255, 0.1);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: #007aff;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-search-enter-btn:hover {  /* 行注：定义 .group-search-enter-btn:hover 样式 */
  background: rgba(0, 166, 255, 0.16);  /* 行注：设置 background 样式 */
  color: #005fe0;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.join-group-shell {  /* 行注：定义 .join-group-shell 样式 */
  min-height: 100%;  /* 行注：设置 min-height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  padding-top: 8px;  /* 行注：设置 padding-top 样式 */
}  /* 行注：结束当前样式块 */

.join-group-card {  /* 行注：定义 .join-group-card 样式 */
  width: min(680px, 100%);  /* 行注：设置 width 样式 */
  background:  /* 行注：开始设置 background 样式 */
    linear-gradient(180deg, rgba(0, 214, 143, 0.08) 0%, rgba(0, 214, 143, 0) 140px),  /* 行注：补充 linear-gradient(180deg, rgba(0, 214, 143, 0.08) 0%, rgba(0, 214, 143, 0) 140px) 选择器 */
    var(--linkx-bg-card);  /* 行注：补充 background 的取值 */
  border: 1px solid rgba(0, 214, 143, 0.12);  /* 行注：设置 border 样式 */
  border-radius: calc(var(--linkx-radius-lg) + 2px);  /* 行注：设置 border-radius 样式 */
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.12);  /* 行注：设置 box-shadow 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.join-group-head {  /* 行注：定义 .join-group-head 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  padding: 24px 24px 18px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.join-group-title {  /* 行注：定义 .join-group-title 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 6px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.join-group-subtitle {  /* 行注：定义 .join-group-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.join-group-icon {  /* 行注：定义 .join-group-icon 样式 */
  width: 44px;  /* 行注：设置 width 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  border-radius: 14px;  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  border: 1px solid rgba(0, 214, 143, 0.16);  /* 行注：设置 border 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.join-group-form {  /* 行注：定义 .join-group-form 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 0 24px 24px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.join-group-label {  /* 行注：定义 .join-group-label 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  letter-spacing: 0.04em;  /* 行注：设置 letter-spacing 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  text-transform: uppercase;  /* 行注：设置 text-transform 样式 */
}  /* 行注：结束当前样式块 */

.join-group-input,  /* 行注：补充 .join-group-input 选择器 */
.join-group-textarea {  /* 行注：定义 .join-group-textarea 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.join-group-input {  /* 行注：定义 .join-group-input 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.join-group-textarea {  /* 行注：定义 .join-group-textarea 样式 */
  min-height: 112px;  /* 行注：设置 min-height 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
  resize: vertical;  /* 行注：设置 resize 样式 */
}  /* 行注：结束当前样式块 */

.join-group-input::placeholder,  /* 行注：补充 .join-group-input::placeholder 选择器 */
.join-group-textarea::placeholder {  /* 行注：定义 .join-group-textarea::placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.join-group-input:focus,  /* 行注：补充 .join-group-input:focus 选择器 */
.join-group-textarea:focus {  /* 行注：定义 .join-group-textarea:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.join-group-foot {  /* 行注：定义 .join-group-foot 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  margin-top: 4px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */

.join-group-hint {  /* 行注：定义 .join-group-hint 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.join-group-btn {  /* 行注：定义 .join-group-btn 样式 */
  min-width: 120px;  /* 行注：设置 min-width 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 18px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.join-group-btn:hover:not(:disabled) {  /* 行注：定义 .join-group-btn:hover:not(:disabled) 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 8px 18px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.join-group-btn:disabled {  /* 行注：定义 .join-group-btn:disabled 样式 */
  opacity: 0.65;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-grid {  /* 行注：定义 .group-grid 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));  /* 行注：设置 grid-template-columns 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-card {  /* 行注：定义 .group-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  padding: 18px;  /* 行注：设置 padding 样式 */
  background:  /* 行注：开始设置 background 样式 */
    linear-gradient(180deg, rgba(0, 166, 255, 0.08) 0%, rgba(0, 166, 255, 0) 120px),  /* 行注：补充 linear-gradient(180deg, rgba(0, 166, 255, 0.08) 0%, rgba(0, 166, 255, 0) 120px) 选择器 */
    var(--linkx-bg-card);  /* 行注：补充 background 的取值 */
  border: 1px solid rgba(0, 166, 255, 0.14);  /* 行注：设置 border 样式 */
  border-radius: calc(var(--linkx-radius) + 2px);  /* 行注：设置 border-radius 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-card:hover {  /* 行注：定义 .group-card:hover 样式 */
  transform: translateY(-2px);  /* 行注：设置 transform 样式 */
  border-color: rgba(0, 166, 255, 0.28);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 12px 28px rgba(0, 102, 255, 0.14);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.group-card-top {  /* 行注：定义 .group-card-top 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-card-avatar {  /* 行注：定义 .group-card-avatar 样式 */
  width: 54px;  /* 行注：设置 width 样式 */
  height: 54px;  /* 行注：设置 height 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  font-size: 20px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  box-shadow: 0 6px 16px rgba(0, 102, 255, 0.22);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.group-card-meta {  /* 行注：定义 .group-card-meta 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.group-card-name {  /* 行注：定义 .group-card-name 样式 */
  font-size: 15px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 6px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.group-card-id-row {  /* 行注：定义 .group-card-id-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  margin-bottom: 6px;  /* 行注：设置 margin-bottom 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.group-card-id {  /* 行注：定义 .group-card-id 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-card-subtitle {  /* 行注：定义 .group-card-subtitle 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  display: -webkit-box;  /* 行注：设置 display 样式 */
  -webkit-line-clamp: 2;  /* 行注：设置 -webkit-line-clamp 样式 */
  -webkit-box-orient: vertical;  /* 行注：设置 -webkit-box-orient 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-role-badge {  /* 行注：定义 .group-role-badge 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  height: 24px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  border: 1px solid transparent;  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.group-role-badge.owner {  /* 行注：定义 .group-role-badge.owner 样式 */
  color: #ffb020;  /* 行注：设置 color 样式 */
  background: rgba(255, 176, 32, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(255, 176, 32, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.group-role-badge.admin {  /* 行注：定义 .group-role-badge.admin 样式 */
  color: #7a5cff;  /* 行注：设置 color 样式 */
  background: rgba(122, 92, 255, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(122, 92, 255, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.group-role-badge.member {  /* 行注：定义 .group-role-badge.member 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(0, 214, 143, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.group-card-bottom {  /* 行注：定义 .group-card-bottom 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-card-actions {  /* 行注：定义 .group-card-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-card-time {  /* 行注：定义 .group-card-time 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-btn,  /* 行注：补充 .group-preview-btn 选择器 */
.group-enter-btn {  /* 行注：定义 .group-enter-btn 样式 */
  min-width: 88px;  /* 行注：设置 min-width 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-btn {  /* 行注：定义 .group-preview-btn 样式 */
  background: rgba(0, 166, 255, 0.1);  /* 行注：设置 background 样式 */
  color: #007aff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-copy-btn {  /* 行注：定义 .group-copy-btn 样式 */
  height: 26px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  border: 1px solid rgba(0, 166, 255, 0.16);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  background: rgba(0, 166, 255, 0.08);  /* 行注：设置 background 样式 */
  color: #007aff;  /* 行注：设置 color 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-copy-btn:hover {  /* 行注：定义 .group-copy-btn:hover 样式 */
  background: rgba(0, 166, 255, 0.14);  /* 行注：设置 background 样式 */
  color: #005fe0;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-btn:hover {  /* 行注：定义 .group-preview-btn:hover 样式 */
  background: rgba(0, 166, 255, 0.16);  /* 行注：设置 background 样式 */
  color: #005fe0;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-enter-btn:hover {  /* 行注：定义 .group-enter-btn:hover 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

/* Empty State */
.empty-state {  /* 行注：定义 .empty-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.empty-icon {  /* 行注：定义 .empty-icon 样式 */
  opacity: 0.3;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-subtitle {  /* 行注：定义 .empty-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .friends-panel {  /* 行注：定义 .friends-panel 样式 */
    width: 280px;  /* 行注：设置 width 样式 */
    min-width: 260px;  /* 行注：设置 min-width 样式 */
  }  /* 行注：结束当前样式块 */

  .tab-content {  /* 行注：定义 .tab-content 样式 */
    padding: 14px 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 820px) {  /* 行注：声明响应式样式区块 */
  .content-area {  /* 行注：定义 .content-area 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  }  /* 行注：结束当前样式块 */

  .friends-panel {  /* 行注：定义 .friends-panel 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    min-width: 0;  /* 行注：设置 min-width 样式 */
    max-height: 42%;  /* 行注：设置 max-height 样式 */
    border-right: none;  /* 行注：设置 border-right 样式 */
    border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
  }  /* 行注：结束当前样式块 */

  .friend-actions {  /* 行注：定义 .friend-actions 样式 */
    opacity: 1;  /* 行注：设置 opacity 样式 */
  }  /* 行注：结束当前样式块 */

  .tabs-header {  /* 行注：定义 .tabs-header 样式 */
    padding: 12px 12px 0;  /* 行注：设置 padding 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  }  /* 行注：结束当前样式块 */

  .tab-content {  /* 行注：定义 .tab-content 样式 */
    padding: 12px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .friend-item {  /* 行注：定义 .friend-item 样式 */
    padding: 12px 10px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
