<template>
  <aside v-if="visible" class="group-manage-sidebar">
    <header class="gms-header">
      <span class="gms-title">群管理</span>
      <button type="button" class="gms-close" aria-label="关闭" @click="$emit('close')">×</button>
    </header>

    <div class="gms-search">
      <input v-model="memberKeyword" type="text" placeholder="搜索群成员" class="gms-search-input" />
    </div>

    <div class="gms-members">
      <div class="gms-grid">
        <div v-for="m in gridMembers" :key="String(m.userId)" class="gms-cell">
          <div class="gms-avatar">
            <ProtectedImage v-if="m.avatar" :src="m.avatar" class="avatar-img" />
            <span v-else>{{ displayMemberName(m)?.charAt(0) || '群' }}</span>
          </div>
          <span class="gms-cell-name">{{ displayMemberName(m) }}</span>
        </div>
        <button v-if="showAddSlot" type="button" class="gms-cell gms-add" title="添加" @click="$emit('add-members')">
          <span class="gms-add-icon">+</span>
        </button>
      </div>
      <button
        v-if="filteredMembers.length > (showAddSlot ? DEFAULT_GRID_CELLS - 1 : DEFAULT_GRID_CELLS)"
        type="button"
        class="gms-more"
        @click="membersExpanded = !membersExpanded"
      >
        {{ membersExpanded ? '收起' : '查看更多' }}
      </button>
    </div>

    <div class="gms-fields">
      <div class="gms-row gms-row-edit gms-row-stack">
        <div class="gms-row-head">
          <span class="gms-label">群聊名称</span>
          <button
            v-if="canEditGroupProfile"
            type="button"
            class="gms-inline-save"
            :disabled="updatingGroupProfile || !isGroupProfileChanged"
            @click="$emit('save-group-profile')"
          >
            {{ updatingGroupProfile ? '保存中…' : '保存' }}
          </button>
        </div>
        <input
          v-if="canEditGroupProfile"
          :value="groupProfileName"
          class="gms-input gms-input-block"
          maxlength="100"
          placeholder="请输入群名称"
          :disabled="updatingGroupProfile"
          @input="$emit('update:groupProfileName', ($event.target as HTMLInputElement).value)"
        />
        <span v-else class="gms-value gms-value-block">{{ groupDetail?.groupName || '—' }}</span>
      </div>
      <div class="gms-row gms-row-block gms-row-stack">
        <div class="gms-row-head">
          <span class="gms-label">群公告</span>
          <button
            v-if="canEditNotice"
            type="button"
            class="gms-inline-save"
            :disabled="updatingNotice || !isGroupNoticeChanged"
            @click="$emit('save-notice')"
          >
            {{ updatingNotice ? '保存中…' : '保存' }}
          </button>
        </div>
        <textarea
          v-if="canEditNotice"
          :value="noticeDraft"
          class="gms-textarea"
          rows="3"
          maxlength="1000"
          placeholder="请输入群公告"
          :disabled="updatingNotice"
          @input="$emit('update:noticeDraft', ($event.target as HTMLTextAreaElement).value)"
        />
        <p v-else class="gms-notice">{{ groupDetail?.notice?.trim() || '暂无公告' }}</p>
      </div>
      <div class="gms-row gms-row-edit">
        <span class="gms-label">备注</span>
        <input
          :value="groupRemark"
          class="gms-input"
          maxlength="100"
          placeholder="群聊的备注仅自己可见"
          @input="$emit('update:groupRemark', ($event.target as HTMLInputElement).value)"
        />
      </div>
      <div class="gms-row gms-row-edit">
        <span class="gms-label">我在本群的昵称</span>
        <input
          :value="memberCardName"
          class="gms-input"
          maxlength="64"
          placeholder="本群昵称"
          @input="$emit('update:memberCardName', ($event.target as HTMLInputElement).value)"
        />
      </div>
      <button type="button" class="gms-row gms-link" @click="$emit('search-chat')">
        <span class="gms-label">查找聊天内容</span>
        <span class="gms-chevron">›</span>
      </button>
      <label class="gms-row gms-switch">
        <span class="gms-label">消息免打扰</span>
        <input
          type="checkbox"
          class="gms-toggle"
          :checked="notificationMuted"
          @change="$emit('update:notificationMuted', ($event.target as HTMLInputElement).checked)"
        />
      </label>
      <label class="gms-row gms-switch">
        <span class="gms-label">显示群成员昵称</span>
        <input
          type="checkbox"
          class="gms-toggle"
          :checked="showMemberNicknames"
          @change="$emit('update:showMemberNicknames', ($event.target as HTMLInputElement).checked)"
        />
      </label>
      <button type="button" class="gms-save-prefs" @click="$emit('save-preferences')">保存设置</button>
    </div>

    <div class="gms-danger">
      <button v-if="canDissolveGroup" type="button" class="gms-danger-btn" @click="$emit('dissolve-group')">解散群聊</button>
      <button v-if="canDissolveGroup" type="button" class="gms-danger-btn" @click="$emit('transfer-owner')">转让群聊</button>
      <button v-if="!isGroupOwner" type="button" class="gms-danger-btn" @click="$emit('leave-group')">退出群聊</button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ProtectedImage from '../ProtectedImage.vue'
import type { GroupDetail, GroupMember } from '../../types/chat'
import { resolveGroupMemberDisplayName } from '../../utils/chat'

const props = defineProps<{
  visible: boolean
  groupDetail: GroupDetail | null
  groupProfileName: string
  noticeDraft: string
  groupRemark: string
  memberCardName: string
  notificationMuted: boolean
  showMemberNicknames: boolean
  canEditGroupProfile: boolean
  canEditNotice: boolean
  updatingGroupProfile: boolean
  updatingNotice: boolean
  isGroupProfileChanged: boolean
  isGroupNoticeChanged: boolean
  canDissolveGroup: boolean
  isGroupOwner: boolean
  canManageMembers: boolean
}>()

defineEmits<{
  (e: 'close'): void
  (e: 'add-members'): void
  (e: 'update:groupProfileName', v: string): void
  (e: 'update:noticeDraft', v: string): void
  (e: 'update:groupRemark', v: string): void
  (e: 'update:memberCardName', v: string): void
  (e: 'update:notificationMuted', v: boolean): void
  (e: 'update:showMemberNicknames', v: boolean): void
  (e: 'save-group-profile'): void
  (e: 'save-notice'): void
  (e: 'save-preferences'): void
  (e: 'search-chat'): void
  (e: 'dissolve-group'): void
  (e: 'transfer-owner'): void
  (e: 'leave-group'): void
}>()

const memberKeyword = ref('')
const membersExpanded = ref(false)
/** 默认 4×4 网格，最后一格为「添加」 */
const DEFAULT_GRID_CELLS = 16

const filteredMembers = computed(() => {
  const list = props.groupDetail?.members || []
  const k = memberKeyword.value.trim().toLowerCase()
  if (!k) return list
  return list.filter(
    m =>
      m.nickname?.toLowerCase().includes(k) ||
      m.username?.toLowerCase().includes(k) ||
      m.memberCardName?.toLowerCase().includes(k)
  )
})

const showAddSlot = computed(() => props.canManageMembers)

const gridMembers = computed(() => {
  const list = filteredMembers.value
  if (membersExpanded.value) return list
  const memberCap = showAddSlot.value ? DEFAULT_GRID_CELLS - 1 : DEFAULT_GRID_CELLS
  return list.slice(0, memberCap)
})

function displayMemberName(m: GroupMember) {
  return resolveGroupMemberDisplayName(m, props.showMemberNicknames) || '成员'
}
</script>

<style scoped>
.group-manage-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-left: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  max-height: 100%;
}

.gms-header {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 40px;
  border-bottom: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  flex-shrink: 0;
}

.gms-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
}

.gms-close {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--linkx-text-muted);
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.gms-close:hover {
  background: var(--linkx-bg-hover, rgba(0, 0, 0, 0.06));
}

.gms-search {
  padding: 12px;
}

.gms-search-input {
  width: 100%;
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--linkx-border);
  border-radius: 6px;
  font-size: 13px;
  background: var(--linkx-bg);
  color: var(--linkx-text);
}

.gms-members {
  padding: 0 12px 12px;
}

.gms-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 8px;
}

.gms-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.gms-avatar {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  background: var(--linkx-bg-muted, #eee);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.gms-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gms-cell-name {
  font-size: 11px;
  color: var(--linkx-text-muted);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.gms-add {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0;
}

.gms-add-icon {
  width: 44px;
  height: 44px;
  border: 1px dashed var(--linkx-border);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--linkx-text-muted);
}

.gms-more {
  width: 100%;
  margin-top: 10px;
  padding: 8px;
  border: none;
  background: transparent;
  color: var(--linkx-primary);
  font-size: 13px;
  cursor: pointer;
}

.gms-fields {
  border-top: 1px solid var(--linkx-border);
  padding: 8px 0;
}

.gms-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  gap: 12px;
  font-size: 13px;
}

.gms-row-block {
  flex-direction: column;
  align-items: flex-start;
}

.gms-row-edit {
  flex-wrap: wrap;
}

.gms-row-stack {
  flex-direction: column;
  align-items: stretch;
}

.gms-row-head {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.gms-inline-save {
  border: none;
  background: none;
  color: var(--linkx-primary);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.gms-inline-save:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.gms-input-block,
.gms-value-block {
  width: 100%;
}

.gms-textarea {
  width: 100%;
  min-height: 72px;
  padding: 8px;
  border: 1px solid var(--linkx-border);
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  resize: vertical;
  background: var(--linkx-bg);
  color: var(--linkx-text);
  box-sizing: border-box;
}

.gms-link {
  width: 100%;
  border: none;
  background: none;
  cursor: pointer;
  text-align: left;
}

.gms-link:hover {
  background: var(--linkx-bg-hover, rgba(0, 0, 0, 0.04));
}

.gms-label {
  color: var(--linkx-text);
  flex-shrink: 0;
}

.gms-value {
  color: var(--linkx-text-muted);
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gms-notice {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--linkx-text-muted);
  line-height: 1.5;
  white-space: pre-wrap;
}

.gms-input {
  flex: 1;
  min-width: 120px;
  height: 28px;
  padding: 0 8px;
  border: 1px solid var(--linkx-border);
  border-radius: 4px;
  font-size: 12px;
  background: var(--linkx-bg);
  color: var(--linkx-text);
}

.gms-chevron {
  color: var(--linkx-text-muted);
}

.gms-switch {
  cursor: pointer;
}

.gms-toggle {
  width: 40px;
  height: 22px;
  accent-color: var(--linkx-primary, #07c160);
}

.gms-save-prefs {
  margin: 8px 16px;
  width: calc(100% - 32px);
  height: 36px;
  border: none;
  border-radius: 6px;
  background: var(--linkx-primary);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.gms-danger {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid var(--linkx-border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gms-danger-btn {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #e54d42;
  font-size: 14px;
  cursor: pointer;
}

.gms-danger-btn:hover {
  background: rgba(229, 77, 66, 0.08);
}
</style>