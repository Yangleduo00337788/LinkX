<template>
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
              @click="$emit('accept-group-request', request.id)"
            >
              通过
            </button>
            <button
              class="request-action-btn reject"
              :disabled="requestActionLoadingId === request.id"
              @click="$emit('reject-group-request', request.id)"
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
              <span v-if="String(member.userId) === String(currentUserId)" class="self-tag">我</span>
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
              @click="$emit('toggle-admin-role', member)"
            >
              {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
            </button>
            <button
              class="mini-btn"
              :disabled="actionLoading"
              @click="$emit('toggle-mute-member', member)"
            >
              {{ member.muted ? '解除禁言' : '禁言成员' }}
            </button>
            <button
              class="mini-btn danger"
              :disabled="actionLoading"
              @click="$emit('remove-member', member)"
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
</template>

<script setup lang="ts">
import {
  GROUP_ROLE_ADMIN,
  type GroupMember,
  type GroupRequestItem
} from '../../types/chat'
import { formatDateTime, roleClass, roleText } from '../../utils/chat'

defineProps<{
  activeRoleFilterLabel: string
  manageableMemberCount: number
  filteredMembers: GroupMember[]
  searchText: string
  canManageMembers: boolean
  currentGroupRequests: GroupRequestItem[]
  requestActionLoadingId: string | number | null
  actionLoading: boolean
  currentUserId: string | number
  groupRequestTypeText: (requestType: number) => string
  groupRequestTagClass: (requestType: number) => string
  buildGroupRequestMessage: (request: GroupRequestItem) => string
  formatRequestTime: (time?: string) => string
  canOperateMember: (member: GroupMember) => boolean
  canToggleAdmin: (member: GroupMember) => boolean
}>()

defineEmits<{
  (event: 'accept-group-request', requestId: string | number): void
  (event: 'reject-group-request', requestId: string | number): void
  (event: 'toggle-admin-role', member: GroupMember): void
  (event: 'toggle-mute-member', member: GroupMember): void
  (event: 'remove-member', member: GroupMember): void
}>()
</script>

<style scoped>
.panel-card,
.member-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-md);
}

.panel-card {
  border-radius: var(--linkx-radius-lg);
  padding: 22px;
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

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
}

.panel-subtitle,
.toolbar-hint,
.member-username,
.member-status,
.request-message,
.request-time,
.empty-subtitle,
.member-side-caption {
  color: var(--linkx-text-muted);
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
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

.request-list,
.member-list {
  display: grid;
  gap: 16px;
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

.request-avatar,
.member-avatar {
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
}

.request-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);
  font-size: 18px;
}

.request-avatar.invite {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.request-info,
.member-info {
  min-width: 0;
  flex: 1;
}

.request-title-row,
.member-name-row,
.member-actions,
.request-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.request-name,
.member-name {
  color: var(--linkx-text);
  font-weight: 700;
}

.request-name {
  font-size: 14px;
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
  font-size: 13px;
  line-height: 1.6;
}

.request-time {
  margin-top: 6px;
  font-size: 12px;
}

.request-actions,
.member-actions {
  gap: 10px;
}

.request-action-btn,
.mini-btn {
  transition: var(--linkx-transition-fast);
}

.request-action-btn {
  min-width: 72px;
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
}

.request-action-btn.accept {
  background: rgba(0, 214, 143, 0.12);
  color: var(--linkx-primary);
}

.request-action-btn.reject {
  background: rgba(255, 107, 107, 0.12);
  color: #ff6b6b;
}

.request-empty-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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

.member-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  align-items: center;
  gap: 18px;
  border-radius: 20px;
  padding: 18px;
}

.member-card-main {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.member-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  font-size: 18px;
}

.member-name {
  font-size: 15px;
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
  font-size: 12px;
  line-height: 1.4;
}

.role-chip,
.self-tag,
.mute-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.role-chip.owner {
  background: rgba(255, 187, 80, 0.18);
  color: #d88914;
}

.role-chip.admin {
  background: rgba(80, 145, 255, 0.16);
  color: #4f86ff;
}

.role-chip.member,
.self-tag {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
}

.mute-tag {
  background: rgba(240, 160, 32, 0.16);
  color: #d88914;
}

.mini-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 13px;
  font-weight: 600;
}

.mini-btn:hover {
  background: var(--linkx-bg-hover);
}

.mini-btn.danger {
  color: #ff6b6b;
}

.empty-state {
  min-height: 240px;
}

.empty-title {
  color: var(--linkx-text);
  font-size: 16px;
  font-weight: 700;
}

@media (max-width: 1100px) {
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

  .member-card-side {
    align-items: flex-start;
  }

  .member-actions,
  .request-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .member-panel-toolbar,
  .request-panel-head {
    flex-direction: column;
    align-items: stretch;
  }

  .member-actions {
    justify-content: stretch;
    width: 100%;
  }

  .mini-btn {
    width: 100%;
    justify-content: center;
  }

  .member-panel-overview {
    grid-template-columns: 1fr;
  }

  .request-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
