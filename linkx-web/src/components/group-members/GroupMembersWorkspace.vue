<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card member-panel">
    <!-- 行注：渲染容器 -->
    <div class="member-panel-head">
      <!-- 行注：渲染容器 -->
      <div class="member-panel-title-wrap">
        <!-- 行注：展示“成员工作区”文案 -->
        <div class="member-panel-kicker">成员工作区</div>
        <!-- 行注：展示“成员列表”文案 -->
        <div class="panel-title">成员列表</div>
        <!-- 行注：展示“支持查看角色、禁言状态与快捷成员管理”文案 -->
        <div class="panel-subtitle">支持查看角色、禁言状态与快捷成员管理</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="member-panel-overview">
        <!-- 行注：渲染容器 -->
        <div class="overview-badge">
          <!-- 行注：展示“当前筛选”文案 -->
          <span class="overview-label">当前筛选</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ activeRoleFilterLabel }}</strong>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="overview-badge">
          <!-- 行注：展示“可管理成员”文案 -->
          <span class="overview-label">可管理成员</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ manageableMemberCount }}</strong>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="overview-badge">
          <!-- 行注：展示“搜索命中”文案 -->
          <span class="overview-label">搜索命中</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ filteredMembers.length }}</strong>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="member-panel-toolbar">
      <!-- 行注：渲染容器 -->
      <div class="toolbar-hint">
        <!-- 行注：展示“当前展示 filteredMembe”文案 -->
        当前展示 {{ filteredMembers.length }} 名成员
        <!-- 行注：展示“，关键词 “{{ searchTex”文案 -->
        <span v-if="searchText.trim()">，关键词 “{{ searchText.trim() }}”</span>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="toolbar-hint">
        <!-- 行注：展示“你可以直接在右侧完成管理员、禁言和移”文案 -->
        <span v-if="canManageMembers">你可以直接在右侧完成管理员、禁言和移出操作</span>
        <!-- 行注：展示“当前仅支持浏览成员与状态信息”文案 -->
        <span v-else>当前仅支持浏览成员与状态信息</span>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染section 节点 -->
    <section v-if="canManageMembers" class="request-panel">
      <!-- 行注：渲染容器 -->
      <div class="request-panel-head">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：展示“入群审批”文案 -->
          <div class="panel-title">入群审批</div>
          <!-- 行注：展示“集中处理当前群的入群申请与邀请通知”文案 -->
          <div class="panel-subtitle">集中处理当前群的入群申请与邀请通知</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="request-panel-count">{{ currentGroupRequests.length }} 条待处理</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-if="currentGroupRequests.length > 0" class="request-list">
        <!-- 行注：渲染article 节点 -->
        <article v-for="request in currentGroupRequests" :key="request.id" class="request-card">
          <!-- 行注：渲染容器 -->
          <div class="request-avatar" :class="{ invite: request.requestType === 1 }">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="request.groupAvatar" :src="request.groupAvatar" class="avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else>{{ request.groupName?.charAt(0) || '群' }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="request-info">
            <!-- 行注：渲染容器 -->
            <div class="request-title-row">
              <!-- 行注：渲染文本节点 -->
              <span class="request-name">{{ request.groupName }}</span>
              <!-- 行注：渲染文本节点 -->
              <span class="request-type-tag" :class="groupRequestTagClass(request.requestType)">
                <!-- 行注：渲染动态文本 -->
                {{ groupRequestTypeText(request.requestType) }}
              <!-- 行注：结束文本节点 -->
              </span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="request-message">{{ buildGroupRequestMessage(request) }}</div>
            <!-- 行注：渲染容器 -->
            <div class="request-time">{{ formatRequestTime(request.createTime) }}</div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="request-actions">
            <!-- 行注：渲染按钮 -->
            <button
              class="request-action-btn accept"
              :disabled="requestActionLoadingId === request.id"
              @click="$emit('accept-group-request', request.id)"
            >
              <!-- 行注：展示“通过”文案 -->
              通过
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染按钮 -->
            <button
              class="request-action-btn reject"
              :disabled="requestActionLoadingId === request.id"
              @click="$emit('reject-group-request', request.id)"
            >
              <!-- 行注：展示“拒绝”文案 -->
              拒绝
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束article 节点 -->
        </article>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-else class="request-empty-state">
        <!-- 行注：展示“当前群暂无待处理入群申请”文案 -->
        当前群暂无待处理入群申请
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束section 节点 -->
    </section>
    <!-- 行注：渲染容器 -->
    <div v-if="filteredMembers.length > 0" class="member-list">
      <!-- 行注：渲染article 节点 -->
      <article v-for="member in filteredMembers" :key="member.userId" class="member-card">
        <!-- 行注：渲染容器 -->
        <div class="member-card-main">
          <!-- 行注：渲染容器 -->
          <div class="member-avatar">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="member.avatar" :src="member.avatar" class="avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else>{{ member.nickname?.charAt(0) || member.username?.charAt(0) || '群' }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="member-info">
            <!-- 行注：渲染容器 -->
            <div class="member-name-row">
              <!-- 行注：渲染文本节点 -->
              <span class="member-name">{{ member.nickname || member.username }}</span>
              <!-- 行注：渲染文本节点 -->
              <span class="role-chip" :class="roleClass(member.role)">{{ roleText(member.role) }}</span>
              <!-- 行注：展示“我”文案 -->
              <span v-if="String(member.userId) === String(currentUserId)" class="self-tag">我</span>
              <!-- 行注：展示“已禁言”文案 -->
              <span v-if="member.muted" class="mute-tag">已禁言</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“@{{ member.usernam”文案 -->
            <div class="member-username">@{{ member.username }}</div>
            <!-- 行注：渲染容器 -->
            <div class="member-status">
              <!-- 行注：展示“禁言至 {{ formatDateT”文案 -->
              <span v-if="member.muted && member.muteTime">禁言至 {{ formatDateTime(member.muteTime) }}</span>
              <!-- 行注：展示“当前可正常发言”文案 -->
              <span v-else>当前可正常发言</span>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="member-card-side">
          <!-- 行注：渲染容器 -->
          <div class="member-side-caption">
            <!-- 行注：渲染文本节点 -->
            <span>{{ canOperateMember(member) ? '可快捷管理' : '仅浏览' }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-if="canOperateMember(member)" class="member-actions">
            <!-- 行注：渲染按钮 -->
            <button
              v-if="canToggleAdmin(member)"
              class="mini-btn"
              :disabled="actionLoading"
              @click="$emit('toggle-admin-role', member)"
            >
              <!-- 行注：渲染动态文本 -->
              {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染按钮 -->
            <button
              class="mini-btn"
              :disabled="actionLoading"
              @click="$emit('toggle-mute-member', member)"
            >
              <!-- 行注：渲染动态文本 -->
              {{ member.muted ? '解除禁言' : '禁言成员' }}
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染按钮 -->
            <button
              class="mini-btn danger"
              :disabled="actionLoading"
              @click="$emit('remove-member', member)"
            >
              <!-- 行注：展示“移出群聊”文案 -->
              移出群聊
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束article 节点 -->
      </article>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-else class="empty-state">
      <!-- 行注：展示“没有找到匹配的成员”文案 -->
      <div class="empty-title">没有找到匹配的成员</div>
      <!-- 行注：展示“换个关键词或筛选条件试试”文案 -->
      <div class="empty-subtitle">换个关键词或筛选条件试试</div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束section 节点 -->
  </section>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
import ProtectedImage from '../ProtectedImage.vue'
import {
  GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
  type GroupMember,  // 行注：补充当前配置项
  type GroupRequestItem  // 行注：补充当前表达式
} from '../../types/chat'  // 行注：补充当前表达式
import { formatDateTime, roleClass, roleText } from '../../utils/chat'  // 行注：引入 formatDateTime, roleClass, roleText 能力

defineProps<{  // 行注：开始当前逻辑块
  activeRoleFilterLabel: string  // 行注：设置 activeRoleFilterLabel 配置项
  manageableMemberCount: number  // 行注：设置 manageableMemberCount 配置项
  filteredMembers: GroupMember[]  // 行注：设置 filteredMembers 配置项
  searchText: string  // 行注：设置 searchText 配置项
  canManageMembers: boolean  // 行注：设置 canManageMembers 配置项
  currentGroupRequests: GroupRequestItem[]  // 行注：设置 currentGroupRequests 配置项
  requestActionLoadingId: string | number | null  // 行注：设置 requestActionLoadingId 配置项
  actionLoading: boolean  // 行注：设置 actionLoading 配置项
  currentUserId: string | number  // 行注：设置 currentUserId 配置项
  groupRequestTypeText: (requestType: number) => string  // 行注：设置 groupRequestTypeText 配置项
  groupRequestTagClass: (requestType: number) => string  // 行注：设置 groupRequestTagClass 配置项
  buildGroupRequestMessage: (request: GroupRequestItem) => string  // 行注：设置 buildGroupRequestMessage 配置项
  formatRequestTime: (time?: string) => string  // 行注：设置 formatRequestTime 配置项
  canOperateMember: (member: GroupMember) => boolean  // 行注：设置 canOperateMember 配置项
  canToggleAdmin: (member: GroupMember) => boolean  // 行注：设置 canToggleAdmin 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'accept-group-request', requestId: string | number): void  // 行注：执行当前调用逻辑
  (event: 'reject-group-request', requestId: string | number): void  // 行注：执行当前调用逻辑
  (event: 'toggle-admin-role', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'toggle-mute-member', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'remove-member', member: GroupMember): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.panel-card,  /* 行注：补充 .panel-card 选择器 */
.member-card {  /* 行注：定义 .member-card 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.panel-card {  /* 行注：定义 .panel-card 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  padding: 22px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.member-panel {  /* 行注：定义 .member-panel 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 100%;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.member-panel-head {  /* 行注：定义 .member-panel-head 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 18px;  /* 行注：设置 gap 样式 */
  padding-bottom: 18px;  /* 行注：设置 padding-bottom 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.member-panel-title-wrap {  /* 行注：定义 .member-panel-title-wrap 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.member-panel-kicker {  /* 行注：定义 .member-panel-kicker 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  letter-spacing: 0.08em;  /* 行注：设置 letter-spacing 样式 */
  text-transform: uppercase;  /* 行注：设置 text-transform 样式 */
}  /* 行注：结束当前样式块 */

.panel-title {  /* 行注：定义 .panel-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 17px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.panel-subtitle,  /* 行注：补充 .panel-subtitle 选择器 */
.toolbar-hint,  /* 行注：补充 .toolbar-hint 选择器 */
.member-username,  /* 行注：补充 .member-username 选择器 */
.member-status,  /* 行注：补充 .member-status 选择器 */
.request-message,  /* 行注：补充 .request-message 选择器 */
.request-time,  /* 行注：补充 .request-time 选择器 */
.empty-subtitle,  /* 行注：补充 .empty-subtitle 选择器 */
.member-side-caption {  /* 行注：定义 .member-side-caption 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.panel-subtitle {  /* 行注：定义 .panel-subtitle 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-panel-overview {  /* 行注：定义 .member-panel-overview 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: repeat(3, minmax(110px, 1fr));  /* 行注：设置 grid-template-columns 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  width: min(420px, 100%);  /* 行注：设置 width 样式 */
}  /* 行注：结束当前样式块 */

.overview-badge {  /* 行注：定义 .overview-badge 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  padding: 14px 14px 12px;  /* 行注：设置 padding 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.overview-label {  /* 行注：定义 .overview-label 样式 */
  display: block;  /* 行注：设置 display 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.overview-badge strong {  /* 行注：定义 .overview-badge strong 样式 */
  display: block;  /* 行注：设置 display 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 20px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-panel-toolbar {  /* 行注：定义 .member-panel-toolbar 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  margin: 18px 0 20px;  /* 行注：设置 margin 样式 */
  padding: 14px 16px;  /* 行注：设置 padding 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: color-mix(in srgb, var(--linkx-bg-hover) 78%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 82%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.toolbar-hint {  /* 行注：定义 .toolbar-hint 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.request-panel {  /* 行注：定义 .request-panel 样式 */
  margin-bottom: 20px;  /* 行注：设置 margin-bottom 样式 */
  padding: 18px;  /* 行注：设置 padding 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  background: color-mix(in srgb, var(--linkx-bg-hover) 52%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 82%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.request-panel-head {  /* 行注：定义 .request-panel-head 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  margin-bottom: 14px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.request-panel-count {  /* 行注：定义 .request-panel-count 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  min-width: 72px;  /* 行注：设置 min-width 样式 */
  height: 28px;  /* 行注：设置 height 样式 */
  padding: 0 12px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  background: rgba(0, 214, 143, 0.14);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.request-list,  /* 行注：补充 .request-list 选择器 */
.member-list {  /* 行注：定义 .member-list 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.request-card {  /* 行注：定义 .request-card 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: 52px minmax(0, 1fr) auto;  /* 行注：设置 grid-template-columns 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  padding: 14px;  /* 行注：设置 padding 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.request-avatar,  /* 行注：补充 .request-avatar 选择器 */
.member-avatar {  /* 行注：定义 .member-avatar 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.request-avatar {  /* 行注：定义 .request-avatar 样式 */
  width: 52px;  /* 行注：设置 width 样式 */
  height: 52px;  /* 行注：设置 height 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);  /* 行注：设置 background 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.request-avatar.invite {  /* 行注：定义 .request-avatar.invite 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.request-info,  /* 行注：补充 .request-info 选择器 */
.member-info {  /* 行注：定义 .member-info 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
}  /* 行注：结束当前样式块 */

.request-title-row,  /* 行注：补充 .request-title-row 选择器 */
.member-name-row,  /* 行注：补充 .member-name-row 选择器 */
.member-actions,  /* 行注：补充 .member-actions 选择器 */
.request-actions {  /* 行注：定义 .request-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.request-name,  /* 行注：补充 .request-name 选择器 */
.member-name {  /* 行注：定义 .member-name 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.request-name {  /* 行注：定义 .request-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.request-type-tag {  /* 行注：定义 .request-type-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  height: 22px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  border: 1px solid transparent;  /* 行注：设置 border 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.request-type-tag.join {  /* 行注：定义 .request-type-tag.join 样式 */
  color: #ffb020;  /* 行注：设置 color 样式 */
  background: rgba(255, 176, 32, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(255, 176, 32, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.request-type-tag.invite {  /* 行注：定义 .request-type-tag.invite 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(0, 214, 143, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.request-message {  /* 行注：定义 .request-message 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.request-time {  /* 行注：定义 .request-time 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.request-actions,  /* 行注：补充 .request-actions 选择器 */
.member-actions {  /* 行注：定义 .member-actions 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.request-action-btn,  /* 行注：补充 .request-action-btn 选择器 */
.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.request-action-btn {  /* 行注：定义 .request-action-btn 样式 */
  min-width: 72px;  /* 行注：设置 min-width 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: 12px;  /* 行注：设置 border-radius 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.request-action-btn.accept {  /* 行注：定义 .request-action-btn.accept 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.request-action-btn.reject {  /* 行注：定义 .request-action-btn.reject 样式 */
  background: rgba(255, 107, 107, 0.12);  /* 行注：设置 background 样式 */
  color: #ff6b6b;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.request-empty-state,  /* 行注：补充 .request-empty-state 选择器 */
.empty-state {  /* 行注：定义 .empty-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.request-empty-state {  /* 行注：定义 .request-empty-state 样式 */
  padding: 18px;  /* 行注：设置 padding 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px dashed color-mix(in srgb, var(--linkx-border) 86%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.member-card {  /* 行注：定义 .member-card 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: minmax(0, 1fr) 220px;  /* 行注：设置 grid-template-columns 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 18px;  /* 行注：设置 gap 样式 */
  border-radius: 20px;  /* 行注：设置 border-radius 样式 */
  padding: 18px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.member-card-main {  /* 行注：定义 .member-card-main 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.member-avatar {  /* 行注：定义 .member-avatar 样式 */
  width: 52px;  /* 行注：设置 width 样式 */
  height: 52px;  /* 行注：设置 height 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-name {  /* 行注：定义 .member-name 样式 */
  font-size: 15px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-username,  /* 行注：补充 .member-username 选择器 */
.member-status {  /* 行注：定义 .member-status 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-card-side {  /* 行注：定义 .member-card-side 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: flex-end;  /* 行注：设置 align-items 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.member-side-caption {  /* 行注：定义 .member-side-caption 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.4;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.role-chip,  /* 行注：补充 .role-chip 选择器 */
.self-tag,  /* 行注：补充 .self-tag 选择器 */
.mute-tag {  /* 行注：定义 .mute-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  height: 24px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.role-chip.owner {  /* 行注：定义 .role-chip.owner 样式 */
  background: rgba(255, 187, 80, 0.18);  /* 行注：设置 background 样式 */
  color: #d88914;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.role-chip.admin {  /* 行注：定义 .role-chip.admin 样式 */
  background: rgba(80, 145, 255, 0.16);  /* 行注：设置 background 样式 */
  color: #4f86ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.role-chip.member,  /* 行注：补充 .role-chip.member 选择器 */
.self-tag {  /* 行注：定义 .self-tag 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.mute-tag {  /* 行注：定义 .mute-tag 样式 */
  background: rgba(240, 160, 32, 0.16);  /* 行注：设置 background 样式 */
  color: #d88914;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  height: 38px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn:hover {  /* 行注：定义 .mini-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn.danger {  /* 行注：定义 .mini-btn.danger 样式 */
  color: #ff6b6b;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-state {  /* 行注：定义 .empty-state 样式 */
  min-height: 240px;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .member-panel-head,  /* 行注：补充 .member-panel-head 选择器 */
  .member-panel-toolbar,  /* 行注：补充 .member-panel-toolbar 选择器 */
  .member-card,  /* 行注：补充 .member-card 选择器 */
  .request-card {  /* 行注：定义 .request-card 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */

  .member-panel-head,  /* 行注：补充 .member-panel-head 选择器 */
  .member-panel-toolbar {  /* 行注：定义 .member-panel-toolbar 样式 */
    display: grid;  /* 行注：设置 display 样式 */
  }  /* 行注：结束当前样式块 */

  .member-panel-overview {  /* 行注：定义 .member-panel-overview 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .member-card-side {  /* 行注：定义 .member-card-side 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .member-actions,  /* 行注：补充 .member-actions 选择器 */
  .request-actions {  /* 行注：定义 .request-actions 样式 */
    justify-content: flex-start;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 768px) {  /* 行注：声明响应式样式区块 */
  .member-panel-toolbar,  /* 行注：补充 .member-panel-toolbar 选择器 */
  .request-panel-head {  /* 行注：定义 .request-panel-head 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .member-actions {  /* 行注：定义 .member-actions 样式 */
    justify-content: stretch;  /* 行注：设置 justify-content 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .mini-btn {  /* 行注：定义 .mini-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */

  .member-panel-overview {  /* 行注：定义 .member-panel-overview 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */

  .request-actions {  /* 行注：定义 .request-actions 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
