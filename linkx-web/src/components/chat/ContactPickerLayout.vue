<template>
  <div class="picker-shell" :class="{ compact: compact }">
    <aside class="picker-left">
      <div class="picker-search">
        <svg class="picker-search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input v-model="keyword" type="text" class="picker-search-input" placeholder="搜索" />
      </div>
      <div class="picker-section-title">联系人</div>
      <div class="picker-list">
        <button
          v-for="friend in filteredFriends"
          :key="friend.friendId"
          type="button"
          class="picker-row"
          :class="{ selected: isSelected(friend.friendId) }"
          @click="toggle(friend.friendId)"
        >
          <span class="picker-check" :class="{ on: isSelected(friend.friendId) }">
            <svg v-if="isSelected(friend.friendId)" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3">
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
          <div class="picker-avatar">
            <ProtectedImage v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
            <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
          </div>
          <span class="picker-name">{{ friend.friendNickname }}</span>
        </button>
        <div v-if="!filteredFriends.length" class="picker-empty">暂无联系人</div>
      </div>
    </aside>
    <section class="picker-right">
      <header class="picker-right-head">
        <span class="picker-right-title">{{ title }}</span>
        <span v-if="multiple" class="picker-right-count">已选择{{ selectedIds.length }}个联系人</span>
      </header>
      <div class="picker-selected-list">
        <div v-for="id in selectedIds" :key="String(id)" class="picker-selected-chip">
          <div class="picker-avatar small">
            <ProtectedImage v-if="friendById(id)?.friendAvatar" :src="friendById(id)!.friendAvatar!" class="avatar-img" />
            <span v-else>{{ friendById(id)?.friendNickname?.charAt(0) || '友' }}</span>
          </div>
          <span class="picker-selected-name">{{ friendById(id)?.friendNickname || id }}</span>
          <button type="button" class="picker-remove" aria-label="移除" @click="remove(id)">×</button>
        </div>
      </div>
      <footer class="picker-footer">
        <button type="button" class="picker-btn primary" :disabled="submitDisabled || (requireSelection && !selectedIds.length)" @click="$emit('submit')">
          {{ submitLabel }}
        </button>
        <button type="button" class="picker-btn" @click="$emit('cancel')">取消</button>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ProtectedImage from '../ProtectedImage.vue'
import type { FriendItem } from '../../types/chat'

const props = withDefaults(
  defineProps<{
    friends: FriendItem[]
    selectedIds: Array<string | number>
    title?: string
    multiple?: boolean
    submitLabel?: string
    submitDisabled?: boolean
    requireSelection?: boolean
    compact?: boolean
  }>(),
  {
    title: '发起群聊',
    multiple: true,
    submitLabel: '完成',
    submitDisabled: false,
    requireSelection: true,
    compact: false
  }
)

const emit = defineEmits<{
  (e: 'update:selectedIds', value: Array<string | number>): void
  (e: 'submit'): void
  (e: 'cancel'): void
}>()

const keyword = ref('')

const filteredFriends = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return props.friends
  return props.friends.filter(
    f =>
      f.friendNickname?.toLowerCase().includes(k) ||
      f.friendUsername?.toLowerCase().includes(k)
  )
})

function friendById(id: string | number) {
  return props.friends.find(f => String(f.friendId) === String(id))
}

function isSelected(id: string | number) {
  return props.selectedIds.some(x => String(x) === String(id))
}

function toggle(id: string | number) {
  if (props.multiple) {
    const next = isSelected(id)
      ? props.selectedIds.filter(x => String(x) !== String(id))
      : [...props.selectedIds, id]
    emit('update:selectedIds', next)
  } else {
    emit('update:selectedIds', isSelected(id) ? [] : [id])
  }
}

function remove(id: string | number) {
  emit(
    'update:selectedIds',
    props.selectedIds.filter(x => String(x) !== String(id))
  )
}
</script>

<style scoped>
.picker-shell {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 360px;
  background: #f5f5f5;
  color: #1a1a1a;
  font-size: 13px;
}

.picker-left {
  width: 280px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.picker-search {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px;
  padding: 0 10px;
  height: 32px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.picker-search-icon {
  color: #999;
  flex-shrink: 0;
}

.picker-search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  background: transparent;
}

.picker-section-title {
  padding: 4px 16px 8px;
  font-size: 12px;
  color: #888;
}

.picker-list {
  flex: 1;
  overflow-y: auto;
}

.picker-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 16px;
  border: none;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.picker-row:hover,
.picker-row.selected {
  background: #e8e8e8;
}

.picker-check {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.picker-check.on {
  background: #07c160;
  border-color: #07c160;
}

.picker-avatar {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  overflow: hidden;
  background: #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.picker-avatar.small {
  width: 32px;
  height: 32px;
}

.picker-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.picker-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picker-empty {
  padding: 24px;
  text-align: center;
  color: #999;
}

.picker-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.picker-right-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.picker-right-title {
  font-size: 14px;
  font-weight: 600;
}

.picker-right-count {
  font-size: 12px;
  color: #888;
}

.picker-selected-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}

.picker-selected-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.picker-selected-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.picker-remove {
  border: none;
  background: none;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
}

.picker-footer {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.picker-btn {
  flex: 1;
  height: 36px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  background: #f0f0f0;
  color: #333;
}

.picker-btn.primary {
  background: #07c160;
  color: #fff;
}

.picker-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.picker-shell.compact {
  min-height: 0;
}
</style>