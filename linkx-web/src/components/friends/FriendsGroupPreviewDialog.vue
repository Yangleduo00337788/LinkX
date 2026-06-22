<template>
  <Teleport to="body">
    <div v-if="visible" class="group-preview-overlay" @click.self="$emit('close')">
      <div class="group-preview-popup">
        <button class="group-preview-close" @click="$emit('close')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>

        <div v-if="previewGroupDetail" class="group-preview-body">
          <div class="group-preview-hero">
            <div class="group-preview-avatar">
              <img v-if="previewGroupDetail.groupAvatar" :src="previewGroupDetail.groupAvatar" class="avatar-img" />
              <span v-else>{{ previewGroupDetail.groupName?.charAt(0) || '群' }}</span>
            </div>
            <div class="group-preview-meta">
              <div class="group-preview-name-row">
                <div class="group-preview-name">{{ previewGroupDetail.groupName }}</div>
                <span class="group-role-badge" :class="groupRoleClass(previewGroupDetail.myRole)">
                  {{ groupRoleText(previewGroupDetail.myRole) }}
                </span>
              </div>
              <div class="group-preview-subtitle">
                {{ previewGroupDetail.memberCount || 0 }} / {{ previewGroupDetail.maxMembers || 0 }} 人
                <template v-if="previewGroupDetail.createTime">
                  · 创建于 {{ previewGroupDetail.createTime.substring(0, 10) }}
                </template>
              </div>
              <div class="group-preview-id-row">
                <span class="group-preview-id">群号：{{ previewGroupDetail.id }}</span>
                <button class="group-copy-btn" @click="$emit('copy-group-id', previewGroupDetail.id)">复制群号</button>
              </div>
            </div>
          </div>

          <div class="group-preview-section">
            <div class="group-preview-section-title">群公告</div>
            <div class="group-preview-notice">
              {{ previewGroupDetail.notice?.trim() || '暂无群公告' }}
            </div>
          </div>

          <div class="group-preview-section">
            <div class="group-preview-section-title">成员预览</div>
            <div class="group-preview-members">
              <div
                v-for="member in previewGroupDetail.members.slice(0, 8)"
                :key="member.userId"
                class="group-preview-member"
              >
                <div class="group-preview-member-avatar">
                  <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                  <span v-else>{{ member.nickname?.charAt(0) || member.username?.charAt(0) || '群' }}</span>
                </div>
                <div class="group-preview-member-name">{{ member.nickname || member.username }}</div>
              </div>
            </div>
          </div>

          <div class="group-preview-actions">
            <button class="group-preview-secondary" @click="$emit('close')">关闭</button>
            <button
              v-if="isJoinedGroup(previewGroupDetail.id, previewGroupDetail.myRole)"
              class="group-enter-btn solid"
              @click="$emit('enter-group-chat', previewGroupDetail.id)"
            >
              进入群聊
            </button>
            <button
              v-else-if="hasPendingJoinRequest(previewGroupDetail.id)"
              class="group-enter-btn solid disabled"
              disabled
            >
              申请已提交
            </button>
            <button
              v-else
              class="group-enter-btn solid"
              @click="$emit('apply-join-from-preview', previewGroupDetail)"
            >
              申请加入
            </button>
          </div>
        </div>

        <div v-else class="group-preview-loading">
          <div class="group-preview-loading-spinner"></div>
          <span>加载群资料中...</span>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
interface GroupPreviewMember {
  userId: string | number
  username?: string
  nickname?: string
  avatar?: string
}

interface GroupPreviewDetail {
  id: string | number
  groupName: string
  groupAvatar?: string
  notice?: string
  memberCount?: number
  maxMembers?: number
  myRole?: number
  createTime?: string
  members: GroupPreviewMember[]
}

defineProps<{
  visible: boolean
  previewGroupDetail: GroupPreviewDetail | null
  hasPendingJoinRequest: (groupId: string | number) => boolean
  isJoinedGroup: (groupId: string | number, role?: number) => boolean
  groupRoleText: (role?: number) => string
  groupRoleClass: (role?: number) => string
}>()

defineEmits<{
  (event: 'close'): void
  (event: 'copy-group-id', groupId: string | number): void
  (event: 'enter-group-chat', groupId: string | number): void
  (event: 'apply-join-from-preview', group: GroupPreviewDetail): void
}>()
</script>

<style scoped>
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

.group-preview-id {
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

.group-enter-btn:disabled {
  cursor: not-allowed;
  opacity: 0.72;
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

.group-enter-btn:hover {
  background: var(--linkx-primary);
  color: white;
}

.group-enter-btn.solid {
  background: var(--linkx-primary);
  color: white;
}

.group-enter-btn.solid:hover {
  background: var(--linkx-primary-hover);
}

.group-enter-btn.solid.disabled {
  background: rgba(0, 214, 143, 0.2);
  color: var(--linkx-primary);
}

.group-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.56);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.group-preview-popup {
  width: min(760px, calc(100vw - 32px));
  min-height: 300px;
  background:
    radial-gradient(circle at top right, rgba(0, 166, 255, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(0, 214, 143, 0.05), rgba(0, 214, 143, 0)),
    var(--linkx-bg-card);
  border: 1px solid rgba(0, 166, 255, 0.14);
  border-radius: 24px;
  box-shadow: 0 28px 90px rgba(0, 0, 0, 0.26);
  position: relative;
  overflow: hidden;
}

.group-preview-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
  z-index: 1;
}

.group-preview-close:hover {
  background: var(--linkx-error);
  color: white;
}

.group-preview-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 28px;
}

.group-preview-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding-right: 28px;
}

.group-preview-avatar {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  box-shadow: 0 12px 28px rgba(0, 102, 255, 0.24);
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-preview-meta {
  flex: 1;
  min-width: 0;
}

.group-preview-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.group-preview-name {
  font-size: 24px;
  font-weight: 800;
  color: var(--linkx-text);
  letter-spacing: 0.01em;
}

.group-preview-subtitle {
  font-size: 13px;
  line-height: 1.6;
  color: var(--linkx-text-secondary);
}

.group-preview-id-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.group-preview-section {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--linkx-border);
  border-radius: 18px;
  padding: 18px;
}

.group-preview-section-title {
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--linkx-text-secondary);
  margin-bottom: 12px;
}

.group-preview-notice {
  font-size: 14px;
  line-height: 1.8;
  color: var(--linkx-text);
  white-space: pre-wrap;
  word-break: break-word;
}

.group-preview-members {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(84px, 1fr));
  gap: 12px;
}

.group-preview-member {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.group-preview-member-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, #00d68f 0%, #00a6ff 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  overflow: hidden;
  box-shadow: 0 8px 18px rgba(0, 166, 255, 0.14);
}

.group-preview-member-name {
  width: 100%;
  font-size: 12px;
  color: var(--linkx-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-preview-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.group-preview-secondary {
  min-width: 88px;
  height: 38px;
  padding: 0 16px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: transparent;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.group-preview-secondary:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.group-preview-loading {
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: var(--linkx-text-secondary);
}

.group-preview-loading-spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid rgba(0, 214, 143, 0.16);
  border-top-color: var(--linkx-primary);
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 820px) {
  .group-preview-body {
    padding: 24px 20px;
  }

  .group-preview-hero {
    align-items: flex-start;
    padding-right: 20px;
  }
}

@media (max-width: 560px) {
  .group-preview-popup {
    width: calc(100vw - 16px);
  }

  .group-preview-body {
    padding: 20px 16px;
  }

  .group-preview-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
    padding-right: 0;
  }

  .group-preview-name-row {
    flex-wrap: wrap;
  }

  .group-preview-actions {
    flex-direction: column;
  }

  .group-preview-actions > button {
    width: 100%;
  }
}
</style>
