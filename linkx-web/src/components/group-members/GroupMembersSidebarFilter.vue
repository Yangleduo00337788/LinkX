<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card filter-card">
    <!-- 行注：展示“筛选成员”文案 -->
    <div class="panel-title">筛选成员</div>
    <!-- 行注：渲染容器 -->
    <div class="search-shell">
      <!-- 行注：渲染图标容器 -->
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <!-- 行注：补充图标圆形路径 -->
        <circle cx="11" cy="11" r="8" />
        <!-- 行注：补充图标路径 -->
        <path d="M21 21l-4.35-4.35" />
      <!-- 行注：结束图标容器 -->
      </svg>
      <!-- 行注：渲染输入框 -->
      <input
        :value="searchText"
        type="text"
        class="search-input"
        placeholder="搜索昵称或用户名"
        @input="$emit('update:search-text', ($event.target as HTMLInputElement).value || '')"
      />
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="role-filter-group">
      <!-- 行注：渲染按钮 -->
      <button
        v-for="option in roleFilters"
        :key="option.value"
        class="role-filter-btn"
        :class="{ active: roleFilter === option.value }"
        @click="$emit('update:role-filter', option.value)"
      >
        <!-- 行注：渲染动态文本 -->
        {{ option.label }}
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="filter-hint">
      <!-- 行注：展示“当前展示 filteredMembe”文案 -->
      当前展示 {{ filteredMemberCount }} / {{ totalMemberCount }} 名成员
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束section 节点 -->
  </section>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupMembersSidebarFilter 组件，负责当前界面片段的展示与交互。
 */
import type { GroupRoleFilter, GroupRoleFilterOption } from '../../types/chat'  // 行注：引入 type { GroupRoleFilter, GroupRoleFilterOption } 模块

defineProps<{  // 行注：开始当前逻辑块
  searchText: string  // 行注：设置 searchText 配置项
  roleFilters: GroupRoleFilterOption[]  // 行注：设置 roleFilters 配置项
  roleFilter: GroupRoleFilter  // 行注：设置 roleFilter 配置项
  filteredMemberCount: number  // 行注：设置 filteredMemberCount 配置项
  totalMemberCount: number  // 行注：设置 totalMemberCount 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'update:search-text', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:role-filter', value: GroupRoleFilter): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.panel-card {  /* 行注：定义 .panel-card 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
  padding: 22px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.panel-title {  /* 行注：定义 .panel-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 17px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.search-shell {  /* 行注：定义 .search-shell 样式 */
  margin-top: 16px;  /* 行注：设置 margin-top 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.search-input {  /* 行注：定义 .search-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.role-filter-group {  /* 行注：定义 .role-filter-group 样式 */
  margin-top: 16px;  /* 行注：设置 margin-top 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.role-filter-btn {  /* 行注：定义 .role-filter-btn 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.role-filter-btn.active {  /* 行注：定义 .role-filter-btn.active 样式 */
  background: rgba(0, 214, 143, 0.14);  /* 行注：设置 background 样式 */
  border-color: rgba(0, 214, 143, 0.28);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.filter-hint {  /* 行注：定义 .filter-hint 样式 */
  margin-top: 14px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */
</style>
