<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染 Teleport 组件 -->
  <Teleport to="body">
    <!-- 行注：渲染容器 -->
    <div v-if="visible" class="group-preview-overlay" @click.self="$emit('close')">
      <!-- 行注：渲染容器 -->
      <div class="group-preview-popup">
        <!-- 行注：渲染按钮 -->
        <button class="group-preview-close" @click="$emit('close')">
          <!-- 行注：渲染图标容器 -->
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标线段 -->
            <line x1="18" y1="6" x2="6" y2="18"/>
            <!-- 行注：补充图标线段 -->
            <line x1="6" y1="6" x2="18" y2="18"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染容器 -->
        <div v-if="previewGroupDetail" class="group-preview-body">
          <!-- 行注：渲染容器 -->
          <div class="group-preview-hero">
            <!-- 行注：渲染容器 -->
            <div class="group-preview-avatar">
              <!-- 行注：渲染图片 -->
              <ProtectedImage v-if="previewGroupDetail.groupAvatar" :src="previewGroupDetail.groupAvatar" class="avatar-img" />
              <!-- 行注：渲染文本节点 -->
              <span v-else>{{ previewGroupDetail.groupName?.charAt(0) || '群' }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="group-preview-meta">
              <!-- 行注：渲染容器 -->
              <div class="group-preview-name-row">
                <!-- 行注：渲染容器 -->
                <div class="group-preview-name">{{ previewGroupDetail.groupName }}</div>
                <!-- 行注：渲染文本节点 -->
                <span class="group-role-badge" :class="groupRoleClass(previewGroupDetail.myRole)">
                  <!-- 行注：渲染动态文本 -->
                  {{ groupRoleText(previewGroupDetail.myRole) }}
                <!-- 行注：结束文本节点 -->
                </span>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-preview-subtitle">
                <!-- 行注：渲染动态文本 -->
                {{ previewGroupDetail.memberCount || 0 }} / {{ previewGroupDetail.maxMembers || 0 }} 人
                <!-- 行注：开始定义模板区域 -->
                <template v-if="previewGroupDetail.createTime">
                  <!-- 行注：展示“· 创建于 previewGroup”文案 -->
                  · 创建于 {{ previewGroupDetail.createTime.substring(0, 10) }}
                <!-- 行注：结束模板区域 -->
                </template>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-preview-id-row">
                <!-- 行注：展示“群号：{{ previewGroup”文案 -->
                <span class="group-preview-id">群号：{{ previewGroupDetail.id }}</span>
                <!-- 行注：展示“复制群号”文案 -->
                <button class="group-copy-btn" @click="$emit('copy-group-id', previewGroupDetail.id)">复制群号</button>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="group-preview-section">
            <!-- 行注：展示“群公告”文案 -->
            <div class="group-preview-section-title">群公告</div>
            <!-- 行注：渲染容器 -->
            <div class="group-preview-notice">
              <!-- 行注：渲染动态文本 -->
              {{ previewGroupDetail.notice?.trim() || '暂无群公告' }}
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="group-preview-section">
            <!-- 行注：展示“成员预览”文案 -->
            <div class="group-preview-section-title">成员预览</div>
            <!-- 行注：渲染容器 -->
            <div class="group-preview-members">
              <!-- 行注：渲染容器 -->
              <div
                v-for="member in previewGroupDetail.members.slice(0, 8)"
                :key="member.userId"
                class="group-preview-member"
              >
                <!-- 行注：渲染容器 -->
                <div class="group-preview-member-avatar">
                  <!-- 行注：渲染图片 -->
                  <ProtectedImage v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                  <!-- 行注：渲染文本节点 -->
                  <span v-else>{{ member.nickname?.charAt(0) || member.username?.charAt(0) || '群' }}</span>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div class="group-preview-member-name">{{ member.nickname || member.username }}</div>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="group-preview-actions">
            <!-- 行注：展示“关闭”文案 -->
            <button class="group-preview-secondary" @click="$emit('close')">关闭</button>
            <!-- 行注：渲染按钮 -->
            <button
              v-if="isJoinedGroup(previewGroupDetail.id, previewGroupDetail.myRole)"
              class="group-enter-btn solid"
              @click="$emit('enter-group-chat', previewGroupDetail.id)"
            >
              <!-- 行注：展示“进入群聊”文案 -->
              进入群聊
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染按钮 -->
            <button
              v-else-if="hasPendingJoinRequest(previewGroupDetail.id)"
              class="group-enter-btn solid disabled"
              disabled
            >
              <!-- 行注：展示“申请已提交”文案 -->
              申请已提交
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染按钮 -->
            <button
              v-else
              class="group-enter-btn solid"
              @click="$emit('apply-join-from-preview', previewGroupDetail)"
            >
              <!-- 行注：展示“申请加入”文案 -->
              申请加入
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-else class="group-preview-loading">
          <!-- 行注：渲染容器 -->
          <div class="group-preview-loading-spinner"></div>
          <!-- 行注：展示“加载群资料中...”文案 -->
          <span>加载群资料中...</span>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束Teleport 节点 -->
  </Teleport>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
import ProtectedImage from '../ProtectedImage.vue'

interface GroupPreviewMember {  // 行注：开始当前逻辑块
  userId: string | number  // 行注：设置 userId 配置项
  username?: string  // 行注：补充当前表达式
  nickname?: string  // 行注：补充当前表达式
  avatar?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface GroupPreviewDetail {  // 行注：开始当前逻辑块
  id: string | number  // 行注：设置 id 配置项
  groupName: string  // 行注：设置 groupName 配置项
  groupAvatar?: string  // 行注：补充当前表达式
  notice?: string  // 行注：补充当前表达式
  memberCount?: number  // 行注：补充当前表达式
  maxMembers?: number  // 行注：补充当前表达式
  myRole?: number  // 行注：补充当前表达式
  createTime?: string  // 行注：补充当前表达式
  members: GroupPreviewMember[]  // 行注：设置 members 配置项
}  // 行注：结束当前代码块

defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  previewGroupDetail: GroupPreviewDetail | null  // 行注：设置 previewGroupDetail 配置项
  hasPendingJoinRequest: (groupId: string | number) => boolean  // 行注：设置 hasPendingJoinRequest 配置项
  isJoinedGroup: (groupId: string | number, role?: number) => boolean  // 行注：设置 isJoinedGroup 配置项
  groupRoleText: (role?: number) => string  // 行注：设置 groupRoleText 配置项
  groupRoleClass: (role?: number) => string  // 行注：设置 groupRoleClass 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'close'): void  // 行注：执行当前调用逻辑
  (event: 'copy-group-id', groupId: string | number): void  // 行注：执行当前调用逻辑
  (event: 'enter-group-chat', groupId: string | number): void  // 行注：执行当前调用逻辑
  (event: 'apply-join-from-preview', group: GroupPreviewDetail): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
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

.group-preview-id {  /* 行注：定义 .group-preview-id 样式 */
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

.group-enter-btn:disabled {  /* 行注：定义 .group-enter-btn:disabled 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
  opacity: 0.72;  /* 行注：设置 opacity 样式 */
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

.group-enter-btn:hover {  /* 行注：定义 .group-enter-btn:hover 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-enter-btn.solid {  /* 行注：定义 .group-enter-btn.solid 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-enter-btn.solid:hover {  /* 行注：定义 .group-enter-btn.solid:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.group-enter-btn.solid.disabled {  /* 行注：定义 .group-enter-btn.solid.disabled 样式 */
  background: rgba(0, 214, 143, 0.2);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-overlay {  /* 行注：定义 .group-preview-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  background: rgba(0, 0, 0, 0.56);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  z-index: 1000;  /* 行注：设置 z-index 样式 */
  backdrop-filter: blur(8px);  /* 行注：设置 backdrop-filter 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-popup {  /* 行注：定义 .group-preview-popup 样式 */
  width: min(760px, calc(100vw - 32px));  /* 行注：设置 width 样式 */
  min-height: 300px;  /* 行注：设置 min-height 样式 */
  background:  /* 行注：开始设置 background 样式 */
    radial-gradient(circle at top right, rgba(0, 166, 255, 0.12), transparent 28%),  /* 行注：补充 radial-gradient(circle at top right, rgba(0, 166, 255, 0.12), transparent 28%) 选择器 */
    linear-gradient(180deg, rgba(0, 214, 143, 0.05), rgba(0, 214, 143, 0)),  /* 行注：补充 linear-gradient(180deg, rgba(0, 214, 143, 0.05), rgba(0, 214, 143, 0)) 选择器 */
    var(--linkx-bg-card);  /* 行注：补充 background 的取值 */
  border: 1px solid rgba(0, 166, 255, 0.14);  /* 行注：设置 border 样式 */
  border-radius: 24px;  /* 行注：设置 border-radius 样式 */
  box-shadow: 0 28px 90px rgba(0, 0, 0, 0.26);  /* 行注：设置 box-shadow 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-close {  /* 行注：定义 .group-preview-close 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  top: 14px;  /* 行注：设置 top 样式 */
  right: 14px;  /* 行注：设置 right 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  background: rgba(255, 255, 255, 0.06);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
  z-index: 1;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-close:hover {  /* 行注：定义 .group-preview-close:hover 样式 */
  background: var(--linkx-error);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-body {  /* 行注：定义 .group-preview-body 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 20px;  /* 行注：设置 gap 样式 */
  padding: 28px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-hero {  /* 行注：定义 .group-preview-hero 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 18px;  /* 行注：设置 gap 样式 */
  padding-right: 28px;  /* 行注：设置 padding-right 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-avatar {  /* 行注：定义 .group-preview-avatar 样式 */
  width: 72px;  /* 行注：设置 width 样式 */
  height: 72px;  /* 行注：设置 height 样式 */
  border-radius: 22px;  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  font-size: 28px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  box-shadow: 0 12px 28px rgba(0, 102, 255, 0.24);  /* 行注：设置 box-shadow 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-meta {  /* 行注：定义 .group-preview-meta 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-name-row {  /* 行注：定义 .group-preview-name-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  margin-bottom: 8px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-name {  /* 行注：定义 .group-preview-name 样式 */
  font-size: 24px;  /* 行注：设置 font-size 样式 */
  font-weight: 800;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  letter-spacing: 0.01em;  /* 行注：设置 letter-spacing 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-subtitle {  /* 行注：定义 .group-preview-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-id-row {  /* 行注：定义 .group-preview-id-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  margin-top: 10px;  /* 行注：设置 margin-top 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-section {  /* 行注：定义 .group-preview-section 样式 */
  background: rgba(255, 255, 255, 0.02);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  padding: 18px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-section-title {  /* 行注：定义 .group-preview-section-title 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 800;  /* 行注：设置 font-weight 样式 */
  letter-spacing: 0.08em;  /* 行注：设置 letter-spacing 样式 */
  text-transform: uppercase;  /* 行注：设置 text-transform 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  margin-bottom: 12px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-notice {  /* 行注：定义 .group-preview-notice 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.8;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  white-space: pre-wrap;  /* 行注：设置 white-space 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-members {  /* 行注：定义 .group-preview-members 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: repeat(auto-fill, minmax(84px, 1fr));  /* 行注：设置 grid-template-columns 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-member {  /* 行注：定义 .group-preview-member 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-member-avatar {  /* 行注：定义 .group-preview-member-avatar 样式 */
  width: 52px;  /* 行注：设置 width 样式 */
  height: 52px;  /* 行注：设置 height 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00a6ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  box-shadow: 0 8px 18px rgba(0, 166, 255, 0.14);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-member-name {  /* 行注：定义 .group-preview-member-name 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-actions {  /* 行注：定义 .group-preview-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-secondary {  /* 行注：定义 .group-preview-secondary 样式 */
  min-width: 88px;  /* 行注：设置 min-width 样式 */
  height: 38px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-secondary:hover {  /* 行注：定义 .group-preview-secondary:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-loading {  /* 行注：定义 .group-preview-loading 样式 */
  min-height: 300px;  /* 行注：设置 min-height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-preview-loading-spinner {  /* 行注：定义 .group-preview-loading-spinner 样式 */
  width: 28px;  /* 行注：设置 width 样式 */
  height: 28px;  /* 行注：设置 height 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  border: 3px solid rgba(0, 214, 143, 0.16);  /* 行注：设置 border 样式 */
  border-top-color: var(--linkx-primary);  /* 行注：设置 border-top-color 样式 */
  animation: spin 0.9s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

@keyframes spin {  /* 行注：声明关键帧动画 */
  from {  /* 行注：设置动画起始状态 */
    transform: rotate(0deg);  /* 行注：设置 transform 样式 */
  }  /* 行注：结束当前样式块 */

  to {  /* 行注：设置动画结束状态 */
    transform: rotate(360deg);  /* 行注：设置 transform 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 820px) {  /* 行注：声明响应式样式区块 */
  .group-preview-body {  /* 行注：定义 .group-preview-body 样式 */
    padding: 24px 20px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .group-preview-hero {  /* 行注：定义 .group-preview-hero 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    padding-right: 20px;  /* 行注：设置 padding-right 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .group-preview-popup {  /* 行注：定义 .group-preview-popup 样式 */
    width: calc(100vw - 16px);  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .group-preview-body {  /* 行注：定义 .group-preview-body 样式 */
    padding: 20px 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .group-preview-hero {  /* 行注：定义 .group-preview-hero 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    gap: 14px;  /* 行注：设置 gap 样式 */
    padding-right: 0;  /* 行注：设置 padding-right 样式 */
  }  /* 行注：结束当前样式块 */

  .group-preview-name-row {  /* 行注：定义 .group-preview-name-row 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  }  /* 行注：结束当前样式块 */

  .group-preview-actions {  /* 行注：定义 .group-preview-actions 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  }  /* 行注：结束当前样式块 */

  .group-preview-actions > button {  /* 行注：定义 .group-preview-actions > button 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
