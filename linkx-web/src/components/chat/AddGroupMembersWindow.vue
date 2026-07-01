<template>
  <DesktopToolWindow
    :visible="visible"
    title="添加群成员"
    :width="718"
    :height="427"
    :show-title-bar="false"
    @close="$emit('close')"
  >
    <div class="agm-root">
      <aside class="agm-left">
        <div class="agm-search">
          <svg class="agm-search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input v-model="keyword" type="text" placeholder="搜索" class="agm-search-input" />
        </div>
        <div class="agm-section-label">联系人</div>
        <div class="agm-list">
          <template v-for="group in groupedFriends" :key="group.letter">
            <div class="agm-letter">{{ group.letter }}</div>
            <button
              v-for="f in group.items"
              :key="f.friendId"
              type="button"
              class="agm-row"
              :class="{ active: isSelected(f.friendId) }"
              @click="toggle(f.friendId)"
            >
              <span class="agm-radio" :class="{ checked: isSelected(f.friendId) }" />
              <div class="agm-avatar">
                <ProtectedImage v-if="f.friendAvatar" :src="f.friendAvatar" class="avatar-img" />
                <span v-else>{{ f.friendNickname?.charAt(0) || '友' }}</span>
              </div>
              <span class="agm-name">{{ f.friendNickname }}</span>
            </button>
          </template>
          <div v-if="!filteredFriends.length" class="agm-empty">暂无可邀请的好友</div>
        </div>
      </aside>
      <section class="agm-right">
        <div class="agm-right-head">
          <span class="agm-right-title">添加群成员</span>
          <span class="agm-count">已选择{{ selectedIds.length }}个联系人</span>
        </div>
        <div class="agm-selected">
          <div v-for="id in selectedIds" :key="String(id)" class="agm-chip">
            <div class="agm-chip-avatar">
              <ProtectedImage v-if="friendById(id)?.friendAvatar" :src="friendById(id)!.friendAvatar!" class="avatar-img" />
              <span v-else>{{ friendById(id)?.friendNickname?.charAt(0) || '友' }}</span>
            </div>
            <span class="agm-chip-name">{{ friendById(id)?.friendNickname }}</span>
            <button type="button" class="agm-chip-remove" @click="remove(id)">×</button>
          </div>
        </div>
        <div class="agm-actions">
          <button type="button" class="agm-btn primary" :disabled="!selectedIds.length || adding" @click="$emit('submit')">
            {{ adding ? '添加中…' : '添加' }}
          </button>
          <button type="button" class="agm-btn" @click="$emit('close')">取消</button>
        </div>
      </section>
    </div>
  </DesktopToolWindow>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import ProtectedImage from '../ProtectedImage.vue'
import DesktopToolWindow from './DesktopToolWindow.vue'
import type { FriendItem } from '../../types/chat'

const props = defineProps<{
  visible: boolean
  friends: FriendItem[]
  selectedIds: Array<string | number>
  adding?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit'): void
  (e: 'update:selectedIds', v: Array<string | number>): void
}>()

const keyword = ref('')

watch(
  () => props.visible,
  v => {
    if (!v) keyword.value = ''
  }
)

const filteredFriends = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return props.friends
  return props.friends.filter(
    f =>
      f.friendNickname?.toLowerCase().includes(k) ||
      f.friendUsername?.toLowerCase().includes(k)
  )
})

const groupedFriends = computed(() => {
  const map = new Map<string, FriendItem[]>()
  for (const f of filteredFriends.value) {
    const letter = (f.friendNickname?.charAt(0) || '#').toUpperCase()
    const key = /[A-Z]/.test(letter) ? letter : '#'
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(f)
  }
  return [...map.entries()]
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([letter, items]) => ({ letter, items }))
})

function friendById(id: string | number) {
  return props.friends.find(f => String(f.friendId) === String(id))
}

function isSelected(id: string | number) {
  return props.selectedIds.some(x => String(x) === String(id))
}

function toggle(id: string | number) {
  const next = isSelected(id)
    ? props.selectedIds.filter(x => String(x) !== String(id))
    : [...props.selectedIds, id]
  emit('update:selectedIds', next)
}

function remove(id: string | number) {
  emit(
    'update:selectedIds',
    props.selectedIds.filter(x => String(x) !== String(id))
  )
}
</script>

<style scoped>
.agm-root {
  display: flex;
  height: 100%;
  min-height: 0;
}

.agm-left {
  width: 50%;
  border-right: 1px solid #e7e7e7;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: #fafafa;
}

.agm-search {
  margin: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  height: 32px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
}

.agm-search-icon {
  color: #999;
  flex-shrink: 0;
}

.agm-search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
  background: transparent;
}

.agm-section-label {
  padding: 4px 16px 8px;
  font-size: 12px;
  color: #888;
}

.agm-list {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 8px;
}

.agm-letter {
  padding: 4px 16px;
  font-size: 11px;
  color: #aaa;
}

.agm-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.agm-row:hover,
.agm-row.active {
  background: #ececec;
}

.agm-radio {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #ccc;
  flex-shrink: 0;
}

.agm-radio.checked {
  border-color: #07c160;
  background: #07c160;
  box-shadow: inset 0 0 0 3px #fff;
}

.agm-avatar {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  overflow: hidden;
  background: #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}

.agm-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.agm-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agm-empty {
  padding: 24px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.agm-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
}

.agm-right-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.agm-right-title {
  font-size: 14px;
  color: #333;
}

.agm-count {
  font-size: 12px;
  color: #888;
}

.agm-selected {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.agm-chip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.agm-chip-avatar {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  overflow: hidden;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.agm-chip-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.agm-chip-name {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agm-chip-remove {
  border: none;
  background: transparent;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  padding: 0 4px;
}

.agm-actions {
  padding: 12px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.agm-btn {
  height: 36px;
  border: none;
  border-radius: 6px;
  background: #f0f0f0;
  color: #333;
  font-size: 14px;
  cursor: pointer;
}

.agm-btn.primary {
  background: #07c160;
  color: #fff;
}

.agm-btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>