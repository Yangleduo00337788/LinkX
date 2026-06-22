<template>
  <section class="panel-card filter-card">
    <div class="panel-title">筛选成员</div>
    <div class="search-shell">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="11" cy="11" r="8" />
        <path d="M21 21l-4.35-4.35" />
      </svg>
      <input
        :value="searchText"
        type="text"
        class="search-input"
        placeholder="搜索昵称或用户名"
        @input="$emit('update:search-text', ($event.target as HTMLInputElement).value || '')"
      />
    </div>
    <div class="role-filter-group">
      <button
        v-for="option in roleFilters"
        :key="option.value"
        class="role-filter-btn"
        :class="{ active: roleFilter === option.value }"
        @click="$emit('update:role-filter', option.value)"
      >
        {{ option.label }}
      </button>
    </div>
    <div class="filter-hint">
      当前展示 {{ filteredMemberCount }} / {{ totalMemberCount }} 名成员
    </div>
  </section>
</template>

<script setup lang="ts">
import type { GroupRoleFilter, GroupRoleFilterOption } from '../../types/chat'

defineProps<{
  searchText: string
  roleFilters: GroupRoleFilterOption[]
  roleFilter: GroupRoleFilter
  filteredMemberCount: number
  totalMemberCount: number
}>()

defineEmits<{
  (event: 'update:search-text', value: string): void
  (event: 'update:role-filter', value: GroupRoleFilter): void
}>()
</script>

<style scoped>
.panel-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-md);
  padding: 22px;
}

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
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

.search-input {
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
  color: var(--linkx-text-muted);
  font-size: 12px;
}
</style>
