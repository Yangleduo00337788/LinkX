<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染aside 节点 -->
  <aside class="side-column">
    <!-- 行注：渲染section 节点 -->
    <section class="group-hero-card">
      <!-- 行注：渲染容器 -->
      <div class="group-hero-top">
        <!-- 行注：渲染容器 -->
        <div class="group-avatar">
          <!-- 行注：渲染图片 -->
          <ProtectedImage v-if="groupDetail.groupAvatar" :src="groupDetail.groupAvatar" class="avatar-img" />
          <!-- 行注：渲染文本节点 -->
          <span v-else>{{ groupDetail.groupName?.charAt(0) || '群' }}</span>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="group-hero-main">
          <!-- 行注：渲染容器 -->
          <div class="group-name-row">
            <!-- 行注：渲染一级标题 -->
            <h1>{{ groupDisplayName }}</h1>
            <!-- 行注：展示“我是{{ roleText(grou”文案 -->
            <span class="role-chip self-role" :class="roleClass(groupDetail.myRole)">我是{{ roleText(groupDetail.myRole) }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：展示“原群名：{{ groupDetail”文案 -->
          <div v-if="groupDetail.groupRemark" class="group-alias-hint">原群名：{{ groupDetail.groupName }}</div>
          <!-- 行注：渲染容器 -->
          <div class="group-meta-row">
            <!-- 行注：展示“群号 {{ groupDetail.”文案 -->
            <span>群号 {{ groupDetail.id }}</span>
            <!-- 行注：渲染文本节点 -->
            <span>{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="group-notice">
            <!-- 行注：渲染动态文本 -->
            {{ groupDetail.notice?.trim() || '暂无群公告' }}
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="group-stat-row">
        <!-- 行注：渲染容器 -->
        <div class="stat-pill">
          <!-- 行注：展示“群主”文案 -->
          <span class="stat-label">群主</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ ownerCount }}</strong>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="stat-pill">
          <!-- 行注：展示“管理员”文案 -->
          <span class="stat-label">管理员</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ adminCount }}</strong>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="stat-pill">
          <!-- 行注：展示“普通成员”文案 -->
          <span class="stat-label">普通成员</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ memberCount }}</strong>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="stat-pill warn">
          <!-- 行注：展示“已禁言”文案 -->
          <span class="stat-label">已禁言</span>
          <!-- 行注：渲染strong 节点 -->
          <strong>{{ mutedCount }}</strong>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束section 节点 -->
    </section>
    <!-- 行注：渲染 GroupMembersSidebarProfile 组件 -->
    <GroupMembersSidebarProfile
      :group-detail="groupDetail"
      :can-edit-group-profile="canEditGroupProfile"
      :can-edit-notice="canEditNotice"
      :updating-group-profile="updatingGroupProfile"
      :updating-notice="updatingNotice"
      :group-profile-draft="groupProfileDraft"
      :notice-draft="noticeDraft"
      :is-group-profile-changed="isGroupProfileChanged"
      :is-group-notice-changed="isGroupNoticeChanged"
      @trigger-group-avatar-upload="$emit('trigger-group-avatar-upload')"
      @update:group-profile-name="$emit('update:group-profile-name', $event)"
      @update:notice-draft="$emit('update:notice-draft', $event)"
      @discard-group-profile-drafts="$emit('discard-group-profile-drafts')"
      @submit-update-notice="$emit('submit-update-notice')"
      @submit-update-group-profile="$emit('submit-update-group-profile')"
    />
    <!-- 行注：渲染 GroupMembersSidebarPreferences 组件 -->
    <GroupMembersSidebarPreferences
      :group-preference-draft="groupPreferenceDraft"
      :is-group-preference-changed="isGroupPreferenceChanged"
      :saving-group-preferences="savingGroupPreferences"
      @update:group-remark="$emit('update:group-remark', $event)"
      @update:group-notification-muted="$emit('update:group-notification-muted', $event)"
      @restore-group-preferences="$emit('restore-group-preferences')"
      @submit-group-preferences="$emit('submit-group-preferences')"
    />
    <!-- 行注：渲染 GroupMembersSidebarFilter 组件 -->
    <GroupMembersSidebarFilter
      :search-text="searchText"
      :role-filters="roleFilters"
      :role-filter="roleFilter"
      :filtered-member-count="filteredMemberCount"
      :total-member-count="groupDetail.members.length"
      @update:search-text="$emit('update:search-text', $event)"
      @update:role-filter="$emit('update:role-filter', $event)"
    />
    <!-- 行注：渲染 GroupMembersSidebarManage 组件 -->
    <GroupMembersSidebarManage
      :current-group-role="currentGroupRole"
      :available-friends-count="availableFriendsCount"
      :can-manage-members="canManageMembers"
      :is-group-owner="isGroupOwner"
      :can-dissolve-group="canDissolveGroup"
      @open-add-members-modal="$emit('open-add-members-modal')"
      @open-transfer-owner-modal="$emit('open-transfer-owner-modal')"
      @leave-group="$emit('leave-group')"
      @dissolve-group="$emit('dissolve-group')"
    />
  <!-- 行注：结束aside 节点 -->
  </aside>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupMembersSidebar 组件，负责当前界面片段的展示与交互。
 */
import ProtectedImage from '../ProtectedImage.vue'
import type { GroupDetail, GroupPreferenceDraftState, GroupRoleFilter, GroupRoleFilterOption } from '../../types/chat'
import type { GroupProfileDraftState } from '../../utils/group-draft'  // 行注：引入 type { GroupProfileDraftState } 模块
import { roleClass, roleText } from '../../utils/chat'  // 行注：引入 roleClass, roleText 能力
import GroupMembersSidebarFilter from './GroupMembersSidebarFilter.vue'  // 行注：引入 GroupMembersSidebarFilter 组件
import GroupMembersSidebarManage from './GroupMembersSidebarManage.vue'  // 行注：引入 GroupMembersSidebarManage 组件
import GroupMembersSidebarPreferences from './GroupMembersSidebarPreferences.vue'  // 行注：引入 GroupMembersSidebarPreferences 组件
import GroupMembersSidebarProfile from './GroupMembersSidebarProfile.vue'  // 行注：引入 GroupMembersSidebarProfile 组件

defineProps<{  // 行注：开始当前逻辑块
  groupDetail: GroupDetail  // 行注：设置 groupDetail 配置项
  groupDisplayName: string  // 行注：设置 groupDisplayName 配置项
  ownerCount: number  // 行注：设置 ownerCount 配置项
  adminCount: number  // 行注：设置 adminCount 配置项
  memberCount: number  // 行注：设置 memberCount 配置项
  mutedCount: number  // 行注：设置 mutedCount 配置项
  canEditGroupProfile: boolean  // 行注：设置 canEditGroupProfile 配置项
  canEditNotice: boolean  // 行注：设置 canEditNotice 配置项
  updatingGroupProfile: boolean  // 行注：设置 updatingGroupProfile 配置项
  updatingNotice: boolean  // 行注：设置 updatingNotice 配置项
  groupProfileDraft: GroupProfileDraftState  // 行注：设置 groupProfileDraft 配置项
  noticeDraft: string  // 行注：设置 noticeDraft 配置项
  isGroupProfileChanged: boolean  // 行注：设置 isGroupProfileChanged 配置项
  isGroupNoticeChanged: boolean  // 行注：设置 isGroupNoticeChanged 配置项
  groupPreferenceDraft: GroupPreferenceDraftState  // 行注：设置 groupPreferenceDraft 配置项
  isGroupPreferenceChanged: boolean  // 行注：设置 isGroupPreferenceChanged 配置项
  savingGroupPreferences: boolean  // 行注：设置 savingGroupPreferences 配置项
  searchText: string  // 行注：设置 searchText 配置项
  roleFilters: GroupRoleFilterOption[]  // 行注：设置 roleFilters 配置项
  roleFilter: GroupRoleFilter  // 行注：设置 roleFilter 配置项
  filteredMemberCount: number  // 行注：设置 filteredMemberCount 配置项
  currentGroupRole: number  // 行注：设置 currentGroupRole 配置项
  availableFriendsCount: number  // 行注：设置 availableFriendsCount 配置项
  canManageMembers: boolean  // 行注：设置 canManageMembers 配置项
  isGroupOwner: boolean  // 行注：设置 isGroupOwner 配置项
  canDissolveGroup: boolean  // 行注：设置 canDissolveGroup 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'trigger-group-avatar-upload'): void  // 行注：执行当前调用逻辑
  (event: 'update:group-profile-name', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:notice-draft', value: string): void  // 行注：执行当前调用逻辑
  (event: 'discard-group-profile-drafts'): void  // 行注：执行当前调用逻辑
  (event: 'submit-update-notice'): void  // 行注：执行当前调用逻辑
  (event: 'submit-update-group-profile'): void  // 行注：执行当前调用逻辑
  (event: 'update:group-remark', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:group-notification-muted', value: boolean): void  // 行注：执行当前调用逻辑
  (event: 'restore-group-preferences'): void  // 行注：执行当前调用逻辑
  (event: 'submit-group-preferences'): void  // 行注：执行当前调用逻辑
  (event: 'update:search-text', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:role-filter', value: GroupRoleFilter): void  // 行注：执行当前调用逻辑
  (event: 'open-add-members-modal'): void  // 行注：执行当前调用逻辑
  (event: 'open-transfer-owner-modal'): void  // 行注：执行当前调用逻辑
  (event: 'leave-group'): void  // 行注：执行当前调用逻辑
  (event: 'dissolve-group'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.side-column {  /* 行注：定义 .side-column 样式 */
  position: sticky;  /* 行注：设置 position 样式 */
  top: 0;  /* 行注：设置 top 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 20px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-hero-card,  /* 行注：补充 .group-hero-card 选择器 */
.panel-card {  /* 行注：定义 .panel-card 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
  padding: 22px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.group-hero-card {  /* 行注：定义 .group-hero-card 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-hero-card::after {  /* 行注：定义 .group-hero-card::after 样式 */
  content: '';  /* 行注：设置 content 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: auto -60px -60px auto;  /* 行注：设置 inset 样式 */
  width: 180px;  /* 行注：设置 width 样式 */
  height: 180px;  /* 行注：设置 height 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  background: radial-gradient(circle, rgba(0, 214, 143, 0.14), transparent 68%);  /* 行注：设置 background 样式 */
  pointer-events: none;  /* 行注：设置 pointer-events 样式 */
}  /* 行注：结束当前样式块 */

.group-hero-top,  /* 行注：补充 .group-hero-top 选择器 */
.group-meta-row,  /* 行注：补充 .group-meta-row 选择器 */
.group-stat-row,  /* 行注：补充 .group-stat-row 选择器 */
.panel-title-row,  /* 行注：补充 .panel-title-row 选择器 */
.profile-action-row {  /* 行注：定义 .profile-action-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.group-hero-top {  /* 行注：定义 .group-hero-top 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  gap: 18px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-avatar {  /* 行注：定义 .group-avatar 样式 */
  width: 68px;  /* 行注：设置 width 样式 */
  height: 68px;  /* 行注：设置 height 样式 */
  border-radius: 20px;  /* 行注：设置 border-radius 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 24px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.group-hero-main,  /* 行注：补充 .group-hero-main 选择器 */
.profile-fields {  /* 行注：定义 .profile-fields 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
}  /* 行注：结束当前样式块 */

.group-name-row {  /* 行注：定义 .group-name-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.group-name-row h1 {  /* 行注：定义 .group-name-row h1 样式 */
  margin: 0;  /* 行注：设置 margin 样式 */
  font-size: 28px;  /* 行注：设置 font-size 样式 */
  line-height: 1.2;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-meta-row {  /* 行注：定义 .group-meta-row 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  margin-top: 10px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.group-alias-hint {  /* 行注：定义 .group-alias-hint 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.group-notice {  /* 行注：定义 .group-notice 样式 */
  margin-top: 14px;  /* 行注：设置 margin-top 样式 */
  max-width: 680px;  /* 行注：设置 max-width 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.7;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.stat-pill,  /* 行注：补充 .stat-pill 选择器 */
.role-chip {  /* 行注：定义 .role-chip 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.group-stat-row {  /* 行注：定义 .group-stat-row 样式 */
  margin-top: 22px;  /* 行注：设置 margin-top 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: repeat(2, minmax(0, 1fr));  /* 行注：设置 grid-template-columns 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.stat-pill {  /* 行注：定义 .stat-pill 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  padding: 14px 16px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.stat-pill strong {  /* 行注：定义 .stat-pill strong 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 20px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.stat-pill.warn strong {  /* 行注：定义 .stat-pill.warn strong 样式 */
  color: #f0a020;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.stat-label {  /* 行注：定义 .stat-label 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.role-chip {  /* 行注：定义 .role-chip 样式 */
  height: 24px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.role-chip.owner,  /* 行注：补充 .role-chip.owner 选择器 */
.self-role.owner {  /* 行注：定义 .self-role.owner 样式 */
  background: rgba(255, 187, 80, 0.18);  /* 行注：设置 background 样式 */
  color: #d88914;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.role-chip.admin,  /* 行注：补充 .role-chip.admin 选择器 */
.self-role.admin {  /* 行注：定义 .self-role.admin 样式 */
  background: rgba(80, 145, 255, 0.16);  /* 行注：设置 background 样式 */
  color: #4f86ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.role-chip.member,  /* 行注：补充 .role-chip.member 选择器 */
.self-role.member {  /* 行注：定义 .self-role.member 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .side-column {  /* 行注：定义 .side-column 样式 */
    position: static;  /* 行注：设置 position 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 768px) {  /* 行注：声明响应式样式区块 */
  .group-hero-top {  /* 行注：定义 .group-hero-top 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .group-stat-row {  /* 行注：定义 .group-stat-row 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
