<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <aside v-if="visible" class="group-drawer" :class="{ 'group-drawer--inline': inline }">
    <div class="group-drawer-inner">
      <div class="drawer-header">
        <div>
          <div class="drawer-title">群资料</div>
          <div class="drawer-subtitle">{{ groupDetail?.groupName || '加载中…' }}</div>
        </div>
        <button type="button" class="modal-close" aria-label="关闭" @click="$emit('close')">×</button>
      </div>
      <div v-if="groupDetail" class="drawer-tabs">
        <button
          type="button"
          class="drawer-tab"
          :class="{ active: activeTab === 'info' }"
          @click="$emit('update:activeTab', 'info')"
        >
          资料
        </button>
        <button
          type="button"
          class="drawer-tab"
          :class="{ active: activeTab === 'members' }"
          @click="$emit('update:activeTab', 'members')"
        >
          成员
        </button>
        <button
          type="button"
          class="drawer-tab"
          :class="{ active: activeTab === 'files' }"
          @click="$emit('update:activeTab', 'files')"
        >
          群文件
        </button>
        <button
          type="button"
          class="drawer-tab"
          :class="{ active: activeTab === 'manage' }"
          @click="$emit('update:activeTab', 'manage')"
        >
          我的设置
        </button>
      </div>
      <template v-if="groupDetail">
        <div class="drawer-body">
          <template v-if="activeTab === 'info'">
          <!-- 行注：渲染容器 -->
          <div class="group-summary-card">
            <!-- 行注：渲染容器 -->
            <div
              class="group-summary-avatar"
              :class="{ editable: canEditGroupProfile }"
              @click="canEditGroupProfile && $emit('trigger-group-profile-avatar-upload')"
            >
              <!-- 行注：渲染图片 -->
              <ProtectedImage
                v-if="groupProfileAvatarPreview || groupDetail.groupAvatar"
                :src="groupProfileAvatarPreview || groupDetail.groupAvatar"
                class="avatar-img"
              />
              <!-- 行注：渲染文本节点 -->
              <span v-else>{{ groupProfileName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
              <!-- 行注：展示“更换”文案 -->
              <div v-if="canEditGroupProfile" class="group-summary-avatar-mask">更换</div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="group-summary-info">
              <!-- 行注：渲染容器 -->
              <div class="group-profile-name-row">
                <!-- 行注：渲染容器 -->
                <div class="group-profile-name-shell">
                  <!-- 行注：渲染输入框 -->
                  <input
                    :value="groupProfileName"
                    class="group-profile-name-input"
                    :disabled="!canEditGroupProfile || updatingGroupProfile"
                    maxlength="100"
                    placeholder="请输入群名称"
                    @input="handleGroupNameInput"
                  />
                  <!-- 行注：渲染容器 -->
                  <div class="group-profile-hint">
                    <!-- 行注：渲染动态文本 -->
                    {{ canEditGroupProfile ? '点击头像可更换群头像' : '仅群主和管理员可编辑群资料' }}
                  <!-- 行注：结束容器 -->
                  </div>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染按钮 -->
                <button
                  v-if="canEditGroupProfile"
                  class="text-btn group-profile-save-btn"
                  :disabled="updatingGroupProfile || !isGroupProfileChanged"
                  :title="!isGroupProfileChanged ? '修改群名称或头像后才能保存' : '保存群资料'"
                  @click="$emit('save-group-profile')"
                >
                  <!-- 行注：渲染动态文本 -->
                  {{ updatingGroupProfile ? '保存中...' : '保存资料' }}
                <!-- 行注：结束按钮 -->
                </button>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-summary-badges">
                <!-- 行注：渲染容器 -->
                <div class="group-summary-pill">{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</div>
                <!-- 行注：展示“我的角色：{{ roleText(g”文案 -->
                <div class="group-summary-pill role">我的角色：{{ roleText(groupDetail.myRole) }}</div>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-summary-id-row">
                <!-- 行注：展示“群号：{{ groupDetail.”文案 -->
                <span class="group-summary-id">群号：{{ groupDetail.id }}</span>
                <!-- 行注：展示“复制群号”文案 -->
                <button class="text-btn group-id-copy-btn" @click="$emit('copy-group-id', groupDetail.id)">复制群号</button>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="drawer-section">
            <!-- 行注：渲染容器 -->
            <div class="section-title-row">
              <!-- 行注：展示“群公告”文案 -->
              <span class="drawer-section-title">群公告</span>
              <!-- 行注：渲染按钮 -->
              <button
                v-if="canEditNotice"
                class="text-btn"
                :disabled="updatingNotice || !isGroupNoticeChanged"
                :title="!isGroupNoticeChanged ? '修改群公告后才能保存' : '保存群公告'"
                @click="$emit('save-notice')"
              >
                <!-- 行注：渲染动态文本 -->
                {{ updatingNotice ? '保存中...' : '保存公告' }}
              <!-- 行注：结束按钮 -->
              </button>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染文本域 -->
            <textarea
              :value="noticeDraft"
              class="drawer-textarea"
              rows="4"
              :disabled="!canEditNotice"
              :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
              @input="handleNoticeInput"
            ></textarea>
            <!-- 行注：展示“最近更新：{{ formatDate”文案 -->
            <div v-if="groupDetail.noticeUpdateTime" class="section-hint">最近更新：{{ formatDateTime(groupDetail.noticeUpdateTime) }}</div>
          <!-- 行注：结束容器 -->
          </div>
          </template>

          <template v-else-if="activeTab === 'members'">
          <div class="drawer-section">
            <div class="section-title-row">
              <span class="drawer-section-title">成员管理</span>
              <div class="section-actions">
                <button v-if="canManageMembers" class="text-btn" @click="$emit('open-add-members-modal')">邀请进群</button>
              </div>
            </div>
            <div class="member-manage-list">
              <!-- 行注：渲染容器 -->
              <div v-for="member in groupDetail.members" :key="member.userId" class="member-row">
                <!-- 行注：渲染容器 -->
                <div class="member-main">
                  <!-- 行注：渲染容器 -->
                  <div class="member-avatar">
                    <!-- 行注：渲染图片 -->
                    <ProtectedImage v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                    <!-- 行注：渲染文本节点 -->
                    <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
                  <!-- 行注：结束容器 -->
                  </div>
                  <!-- 行注：渲染容器 -->
                  <div class="member-info">
                    <!-- 行注：渲染容器 -->
                    <div class="member-name-row">
                      <!-- 行注：渲染文本节点 -->
                      <span class="member-name">{{ member.nickname || member.username }}</span>
                      <!-- 行注：渲染容器 -->
                      <div class="member-badges">
                        <!-- 行注：渲染文本节点 -->
                        <span class="member-role-tag" :class="roleClass(member.role)">{{ roleText(member.role) }}</span>
                        <!-- 行注：展示“我”文案 -->
                        <span v-if="String(member.userId) === currentUserId" class="member-self-tag">我</span>
                      <!-- 行注：结束容器 -->
                      </div>
                    <!-- 行注：结束容器 -->
                    </div>
                    <!-- 行注：渲染容器 -->
                    <div class="member-meta">
                      <!-- 行注：展示“@{{ member.usernam”文案 -->
                      <span class="member-username">@{{ member.username }}</span>
                      <!-- 行注：渲染文本节点 -->
                      <span v-if="member.muted && member.muteTime" class="member-mute-text">
                        <!-- 行注：展示“已禁言至 formatDateTim”文案 -->
                        已禁言至 {{ formatDateTime(member.muteTime) }}
                      <!-- 行注：结束文本节点 -->
                      </span>
                    <!-- 行注：结束容器 -->
                    </div>
                  <!-- 行注：结束容器 -->
                  </div>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div v-if="canOperateMember(member)" class="member-actions">
                  <!-- 行注：渲染按钮 -->
                  <button v-if="canToggleAdmin(member)" class="mini-btn" @click="$emit('toggle-admin-role', member)">
                    <!-- 行注：渲染动态文本 -->
                    {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
                  <!-- 行注：结束按钮 -->
                  </button>
                  <!-- 行注：渲染按钮 -->
                  <button class="mini-btn" @click="$emit('toggle-mute-member', member)">{{ member.muted ? '解除禁言' : '禁言' }}</button>
                  <!-- 行注：展示“踢出”文案 -->
                  <button class="mini-btn danger" @click="$emit('remove-member', member)">踢出</button>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          </div>
          <div v-if="canDissolveGroup || !isGroupOwner" class="drawer-section danger-zone">
            <!-- 行注：渲染容器 -->
            <div class="section-title-row">
              <!-- 行注：展示“危险操作”文案 -->
              <span class="drawer-section-title">危险操作</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="danger-actions">
              <!-- 行注：展示“解散群聊”文案 -->
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="$emit('dissolve-group')">解散群聊</button>
              <!-- 行注：展示“转让群主”文案 -->
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="$emit('open-transfer-owner-modal')">转让群主</button>
              <!-- 行注：展示“退出群聊”文案 -->
              <button v-if="!isGroupOwner" class="danger-action-btn" @click="$emit('leave-group')">退出群聊</button>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          </template>

          <template v-else-if="activeTab === 'album'">
            <div class="drawer-section">
              <div class="section-title-row">
                <span class="drawer-section-title">群相册</span>
                <button type="button" class="text-btn" :disabled="mediaLoading" @click="loadGroupMedia('image')">
                  {{ mediaLoading ? '加载中…' : '刷新' }}
                </button>
              </div>
              <div v-if="mediaLoading && !albumItems.length" class="drawer-placeholder">加载中…</div>
              <div v-else-if="!albumItems.length" class="drawer-placeholder">暂无图片</div>
              <div v-else class="group-album-grid">
                <button
                  v-for="item in albumItems"
                  :key="item.id"
                  type="button"
                  class="group-album-cell"
                  @click="onMediaPreview(item)"
                >
                  <ProtectedImage v-if="item.content" :src="item.content" class="group-album-img" alt="" />
                </button>
              </div>
            </div>
          </template>

          <template v-else-if="activeTab === 'highlights'">
            <div class="drawer-section">
              <div class="section-title-row">
                <span class="drawer-section-title">群精华</span>
              </div>
              <div class="drawer-placeholder">
                在聊天中长按消息可设为精华（功能开发中）。当前可先使用群文件查看重要附件。
              </div>
            </div>
          </template>

          <template v-else-if="activeTab === 'manage'">
            <div class="drawer-section preference-drawer-section">
              <div class="drawer-section-title">我的群设置</div>
              <p class="section-hint">仅对你自己生效：群备注、免打扰、本群昵称等。</p>
              <label class="field-label">群备注</label>
              <input
                :value="groupPreferenceDraft?.groupRemark ?? ''"
                type="text"
                class="drawer-text-input"
                maxlength="100"
                placeholder="给本群设置备注名"
                @input="$emit('update:group-remark', ($event.target as HTMLInputElement).value)"
              />
              <label class="field-label">我在本群的昵称</label>
              <input
                :value="memberCardDraft"
                type="text"
                class="drawer-text-input"
                maxlength="64"
                placeholder="本群内显示的昵称"
                @input="$emit('update:member-card-name', ($event.target as HTMLInputElement).value)"
              />
              <label class="preference-switch-row">
                <input
                  type="checkbox"
                  :checked="groupPreferenceDraft?.notificationMuted ?? false"
                  @change="$emit('update:group-notification-muted', ($event.target as HTMLInputElement).checked)"
                />
                <span>消息免打扰</span>
              </label>
              <button type="button" class="text-btn group-profile-save-btn" style="margin-top: 8px" @click="$emit('save-group-preferences')">
                保存我的设置
              </button>
              <button type="button" class="text-btn" style="margin-top: 12px" @click="$emit('open-full-manage')">
                进入完整群管理页 →
              </button>
            </div>
          </template>

          <template v-else-if="activeTab === 'files'">
            <div class="drawer-section">
              <div class="section-title-row">
                <span class="drawer-section-title">群文件</span>
                <button type="button" class="text-btn" :disabled="mediaLoading" @click="loadGroupMedia">
                  {{ mediaLoading ? '加载中…' : '刷新' }}
                </button>
              </div>
              <div v-if="mediaLoading && !mediaItems.length" class="drawer-placeholder">加载中…</div>
              <div v-else-if="!mediaItems.length" class="drawer-placeholder">暂无群文件或图片</div>
              <ul v-else class="group-media-list">
                <li v-for="item in mediaItems" :key="item.id" class="group-media-item">
                  <div class="group-media-thumb" @click="onMediaPreview(item)">
                    <ProtectedImage
                      v-if="item.msgType === MESSAGE_TYPE_IMAGE && item.content"
                      :src="item.content"
                      class="group-media-thumb-img"
                      alt=""
                    />
                    <span v-else class="group-media-thumb-file" aria-hidden="true">
                      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                        <polyline points="14 2 14 8 20 8" />
                      </svg>
                    </span>
                  </div>
                  <div class="group-media-main">
                    <button type="button" class="group-media-name" :title="mediaDisplayName(item)" @click="onMediaPreview(item)">
                      {{ mediaDisplayName(item) }}
                    </button>
                    <span v-if="item.createTime" class="group-media-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                  <div class="group-media-actions">
                    <button
                      v-if="item.msgType === MESSAGE_TYPE_IMAGE && item.content"
                      type="button"
                      class="mini-btn"
                      @click="onMediaPreview(item)"
                    >
                      预览
                    </button>
                    <button
                      v-if="item.content"
                      type="button"
                      class="mini-btn"
                      @click="onMediaDownload(item)"
                    >
                      下载
                    </button>
                  </div>
                </li>
              </ul>
            </div>
          </template>
        </div>
      </template>
      <div v-else class="panel-placeholder drawer-placeholder">正在加载群详情...</div>
    </div>
  </aside>
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupDetailPanel 组件，负责当前界面片段的展示与交互。
 */
import { computed, ref, watch } from 'vue'
import ProtectedImage from '../ProtectedImage.vue'
import { groupApi } from '../../api/client'
import {
  GROUP_ROLE_ADMIN,
  MESSAGE_TYPE_FILE,
  MESSAGE_TYPE_IMAGE,
  SESSION_TYPE_GROUP,
  type DisplayMessage,
  type GroupDetail,
  type GroupMember
} from '../../types/chat'
import { formatDateTime, getFileName, roleClass, roleText } from '../../utils/chat'

export interface GroupMediaMessage {
  id: string | number
  content: string
  msgType: number
  fileName?: string
  fileSize?: number
  createTime?: string
  fromNickname?: string
}

const props = defineProps<{
  visible: boolean
  inline?: boolean
  activeTab?: 'info' | 'members' | 'files' | 'album' | 'highlights' | 'manage'
  groupDetail: GroupDetail | null
  groupPreferenceDraft?: { groupRemark: string; notificationMuted: boolean }
  memberCardDraft?: string
  groupProfileName: string  // 行注：设置 groupProfileName 配置项
  groupProfileAvatarPreview: string  // 行注：设置 groupProfileAvatarPreview 配置项
  noticeDraft: string  // 行注：设置 noticeDraft 配置项
  currentUserId: string  // 行注：设置 currentUserId 配置项
  canEditGroupProfile: boolean  // 行注：设置 canEditGroupProfile 配置项
  updatingGroupProfile: boolean  // 行注：设置 updatingGroupProfile 配置项
  isGroupProfileChanged: boolean  // 行注：设置 isGroupProfileChanged 配置项
  canEditNotice: boolean  // 行注：设置 canEditNotice 配置项
  updatingNotice: boolean  // 行注：设置 updatingNotice 配置项
  isGroupNoticeChanged: boolean  // 行注：设置 isGroupNoticeChanged 配置项
  canManageMembers: boolean  // 行注：设置 canManageMembers 配置项
  canDissolveGroup: boolean  // 行注：设置 canDissolveGroup 配置项
  isGroupOwner: boolean  // 行注：设置 isGroupOwner 配置项
  canOperateMember: (member: GroupMember) => boolean  // 行注：设置 canOperateMember 配置项
  canToggleAdmin: (member: GroupMember) => boolean  // 行注：设置 canToggleAdmin 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:activeTab', tab: 'info' | 'members' | 'files' | 'album' | 'highlights' | 'manage'): void
  (event: 'trigger-group-profile-avatar-upload'): void
  (event: 'update:groupProfileName', value: string): void
  (event: 'update:noticeDraft', value: string): void
  (event: 'save-group-profile'): void
  (event: 'copy-group-id', value: string | number): void
  (event: 'save-notice'): void
  (event: 'open-add-members-modal'): void
  (event: 'toggle-admin-role', member: GroupMember): void
  (event: 'toggle-mute-member', member: GroupMember): void
  (event: 'remove-member', member: GroupMember): void
  (event: 'dissolve-group'): void
  (event: 'open-transfer-owner-modal'): void
  (event: 'leave-group'): void
  (event: 'preview-media', message: DisplayMessage): void
  (event: 'download-media', message: DisplayMessage): void
  (event: 'update:group-remark', value: string): void
  (event: 'update:member-card-name', value: string): void
  (event: 'update:group-notification-muted', value: boolean): void
  (event: 'save-group-preferences'): void
  (event: 'open-full-manage'): void
}>()

const activeTab = computed(() => props.activeTab ?? 'info')
const memberCardDraft = computed(() => props.memberCardDraft ?? '')

const mediaItems = ref<GroupMediaMessage[]>([])
const albumItems = computed(() => mediaItems.value.filter(i => i.msgType === MESSAGE_TYPE_IMAGE))
const mediaLoading = ref(false)

function normalizeMediaRows(raw: unknown): GroupMediaMessage[] {
  const list = Array.isArray(raw) ? raw : (raw as { records?: unknown[] })?.records ?? []
  return list
    .map((row: Record<string, unknown>) => {
      const content = String(row.content ?? row.fileUrl ?? '').trim()
      if (!content) return null
      const msgType = Number(row.msgType ?? row.messageType ?? MESSAGE_TYPE_FILE)
      const id = row.id ?? row.messageId ?? content
      const createTime = row.createTime != null ? String(row.createTime) : undefined
      return {
        id,
        content,
        msgType,
        fileName: row.fileName != null ? String(row.fileName) : undefined,
        fileSize: row.fileSize != null ? Number(row.fileSize) : undefined,
        createTime,
        fromNickname: row.fromNickname != null ? String(row.fromNickname) : undefined
      } satisfies GroupMediaMessage
    })
    .filter((x): x is GroupMediaMessage => x != null)
}

function mediaDisplayName(item: GroupMediaMessage) {
  return item.fileName || getFileName(item.content) || (item.msgType === MESSAGE_TYPE_IMAGE ? '图片' : '文件')
}

function toDisplayFromMedia(item: GroupMediaMessage): DisplayMessage {
  const gid = String(props.groupDetail?.id ?? '')
  const time = item.createTime ? formatDateTime(item.createTime) : ''
  return {
    id: item.id,
    localId: `group-media-${item.id}`,
    isMe: false,
    isSystem: false,
    name: item.fromNickname || '',
    content: item.content,
    msgType: item.msgType,
    status: 0,
    createTime: item.createTime || '',
    time,
    readStatus: '',
    deliveryStatus: 'sent',
    fileName: item.fileName,
    fileSize: item.fileSize,
    sessionType: SESSION_TYPE_GROUP,
    targetId: gid,
    mentionAll: false,
    mentionUserIds: [],
    mentionDisplayNames: [],
    mentionedMe: false
  }
}

function onMediaPreview(item: GroupMediaMessage) {
  if (!item.content) return
  if (item.msgType === MESSAGE_TYPE_IMAGE) {
    emit('preview-media', toDisplayFromMedia(item))
    return
  }
  emit('download-media', toDisplayFromMedia(item))
}

function onMediaDownload(item: GroupMediaMessage) {
  if (!item.content) return
  emit('download-media', toDisplayFromMedia(item))
}

function handleGroupNameInput(event: Event) {  // 行注：定义 handleGroupNameInput 方法
  const target = event.target as HTMLInputElement  // 行注：初始化 target 变量
  emit('update:groupProfileName', target.value || '')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块

function handleNoticeInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:noticeDraft', target.value || '')
}

async function loadGroupMedia(mode: 'all' | 'image' = 'all') {
  const gid = props.groupDetail?.id
  if (!gid) return
  mediaLoading.value = true
  try {
    const res: any = await groupApi.getMedia(gid, {
      size: 50,
      mediaType: mode === 'image' ? 'image' : 'all'
    })
    const data = res.data?.data ?? res.data
    mediaItems.value = normalizeMediaRows(data)
  } catch {
    mediaItems.value = []
  } finally {
    mediaLoading.value = false
  }
}

watch(
  () => [props.visible, props.activeTab, props.groupDetail?.id] as const,
  ([vis, tab, gid]) => {
    if (!vis || !gid) return
    if (tab === 'files') void loadGroupMedia('all')
    if (tab === 'album') void loadGroupMedia('image')
  },
  { immediate: true }
)
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.drawer-overlay {  /* 行注：定义 .drawer-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: rgba(0, 0, 0, 0.5);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
  z-index: 1800;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.group-drawer {
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--linkx-bg-card);
  border-left: 1px solid var(--linkx-border);
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.06);
}

.group-drawer--inline {
  width: clamp(300px, 38vw, 420px);
  min-width: 280px;
  flex-shrink: 0;
  height: 100%;
  container-type: inline-size;
  container-name: group-drawer;
}

.group-drawer-inner {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.drawer-tabs {
  display: flex;
  gap: 4px;
  padding: 0 16px 12px;
  border-bottom: 1px solid var(--linkx-border);
}

.drawer-tab {
  flex: 1;
  padding: 8px 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  color: var(--linkx-text-muted);
  cursor: pointer;
}

.drawer-tab.active {
  background: var(--linkx-bg-hover);
  color: var(--linkx-primary);
  font-weight: 600;
}

.group-media-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.group-media-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--linkx-border);
  font-size: 13px;
  min-width: 0;
}

.group-media-thumb {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.group-media-thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-media-thumb-file {
  color: var(--linkx-text-muted);
  display: flex;
}

.group-media-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.group-media-name {
  border: none;
  background: none;
  padding: 0;
  text-align: left;
  color: var(--linkx-text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.group-media-name:hover {
  color: var(--linkx-primary);
}

.group-media-time {
  font-size: 11px;
  color: var(--linkx-text-muted);
}

.group-media-actions {
  display: flex;
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: flex-end;
}

.group-album-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.group-album-cell {
  aspect-ratio: 1;
  padding: 0;
  border: none;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: var(--linkx-bg);
}

.group-album-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.drawer-text-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  margin-bottom: 10px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  font-size: 14px;
}

.field-label {
  display: block;
  font-size: 12px;
  color: var(--linkx-text-secondary);
  margin-bottom: 4px;
}

.preference-switch-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-top: 8px;
  cursor: pointer;
}

.header-menu-anchor {
  position: relative;
}

.drawer-placeholder {
  padding: 24px;
  text-align: center;
  color: var(--linkx-text-muted);
  font-size: 13px;
}

.drawer-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--linkx-border);
  min-width: 0;
}

.drawer-header > div:first-child {
  flex: 1;
  min-width: 0;
}

.section-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  min-width: 0;
}

.section-title-row .drawer-section-title {
  flex: 1 1 auto;
  min-width: 0;
}

.section-title-row .text-btn,
.section-title-row .section-actions {
  flex-shrink: 0;
}

.drawer-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--linkx-text);
  line-height: 1.3;
}

.drawer-subtitle {
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.drawer-subtitle,  /* 行注：补充 .drawer-subtitle 选择器 */
.section-hint,  /* 行注：补充 .section-hint 选择器 */
.member-meta {  /* 行注：定义 .member-meta 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.drawer-subtitle {
  font-size: 13px;
}

.drawer-body {
  flex: 1;
  padding: 16px;
  overflow: auto;
  overflow-x: hidden;
  min-width: 0;
}

.modal-close,  /* 行注：补充 .modal-close 选择器 */
.text-btn,  /* 行注：补充 .text-btn 选择器 */
.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.modal-close {  /* 行注：定义 .modal-close 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.modal-close:hover,  /* 行注：补充 .modal-close:hover 选择器 */
.text-btn:hover,  /* 行注：补充 .text-btn:hover 选择器 */
.mini-btn:hover {  /* 行注：定义 .mini-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  margin-bottom: 16px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  min-width: 0;
}

.group-summary-avatar,  /* 行注：补充 .group-summary-avatar 选择器 */
.member-avatar {  /* 行注：定义 .member-avatar 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar {  /* 行注：定义 .group-summary-avatar 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  width: 72px;  /* 行注：设置 width 样式 */
  height: 72px;  /* 行注：设置 height 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 28px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  border: 1px solid rgba(255, 255, 255, 0.06);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar.editable {  /* 行注：定义 .group-summary-avatar.editable 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar-mask {  /* 行注：定义 .group-summary-avatar-mask 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: rgba(15, 23, 42, 0.52);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  opacity: 0;  /* 行注：设置 opacity 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar.editable:hover .group-summary-avatar-mask {  /* 行注：定义 .group-summary-avatar.editable:hover .group-summary-avatar-mask 样式 */
  opacity: 1;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-info,  /* 行注：补充 .group-summary-info 选择器 */
.member-info {  /* 行注：定义 .member-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-info {  /* 行注：定义 .group-summary-info 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  width: 100%;
  gap: 8px 10px;
  min-width: 0;
}

.group-profile-name-shell {  /* 行注：定义 .group-profile-name-shell 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-input {
  width: 100%;
  min-width: 0;
  min-height: 40px;
  height: auto;
  padding: 8px 12px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-md);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: clamp(15px, 4cqi, 18px);
  font-weight: 700;
  line-height: 1.35;
  word-break: break-word;
}

.group-profile-name-input:disabled {  /* 行注：定义 .group-profile-name-input:disabled 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border-color: transparent;  /* 行注：设置 border-color 样式 */
  cursor: default;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-input:focus,  /* 行注：补充 .group-profile-name-input:focus 选择器 */
.drawer-textarea:focus {  /* 行注：定义 .drawer-textarea:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-hint {  /* 行注：定义 .group-profile-hint 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.4;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.text-btn {
  padding: 8px 10px;
  border-radius: var(--linkx-radius);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  font-size: 13px;
  line-height: 1.3;
  white-space: nowrap;
}

.group-profile-save-btn {
  flex: 0 0 auto;
  min-width: 72px;
  min-height: 40px;
  height: auto;
  padding: 8px 12px;
  white-space: nowrap;
  font-size: 13px;
  line-height: 1.3;
  border: 1px solid rgba(77, 107, 255, 0.12);
  background: rgba(77, 107, 255, 0.06);
  color: var(--linkx-primary);
}

.group-profile-save-btn:hover {  /* 行注：定义 .group-profile-save-btn:hover 样式 */
  background: rgba(77, 107, 255, 0.12);  /* 行注：设置 background 样式 */
  color: #a8b8ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-badges {  /* 行注：定义 .group-summary-badges 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-pill,  /* 行注：补充 .group-summary-pill 选择器 */
.member-role-tag,  /* 行注：补充 .member-role-tag 选择器 */
.member-self-tag {  /* 行注：定义 .member-self-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-pill {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.06);
  color: var(--linkx-text-secondary);
  max-width: 100%;
  line-height: 1.35;
  word-break: break-word;
  text-align: center;
}

.group-summary-pill.role {  /* 行注：定义 .group-summary-pill.role 样式 */
  background: rgba(77, 107, 255, 0.14);  /* 行注：设置 background 样式 */
  color: #9cb0ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-id-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 10px;
  min-width: 0;
}

.member-main,
.member-name-row,
.member-badges {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.member-name-row {
  flex-wrap: wrap;
}

.group-summary-id {
  flex: 1 1 auto;
  min-width: 0;
  color: var(--linkx-text-secondary);
  font-size: 12px;
  line-height: 1.45;
  word-break: break-all;
}

.group-id-copy-btn {
  flex-shrink: 0;
  padding: 4px 8px;
  font-size: 12px;
  white-space: nowrap;
}

.drawer-section {  /* 行注：定义 .drawer-section 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  margin-bottom: 18px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.drawer-section-title {  /* 行注：定义 .drawer-section-title 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.drawer-textarea {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 12px 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  resize: vertical;
  outline: none;
  transition: var(--linkx-transition-fast);
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.section-hint {
  font-size: 12px;
  line-height: 1.45;
  word-break: break-word;
}

@container group-drawer (max-width: 320px) {
  .group-profile-name-row {
    flex-direction: column;
    align-items: stretch;
  }

  .group-profile-save-btn {
    width: 100%;
    justify-content: center;
  }

  .section-title-row {
    flex-direction: column;
    align-items: stretch;
  }

  .section-title-row .text-btn {
    align-self: flex-start;
  }

  .group-summary-avatar {
    width: 64px;
    height: 64px;
    font-size: 24px;
  }
}

.section-actions,  /* 行注：补充 .section-actions 选择器 */
.danger-actions,  /* 行注：补充 .danger-actions 选择器 */
.member-actions {  /* 行注：定义 .member-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.member-manage-list {  /* 行注：定义 .member-manage-list 样式 */
  overflow: auto;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.member-row {  /* 行注：定义 .member-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.member-row:not(:last-child) {  /* 行注：定义 .member-row:not(:last-child) 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.member-avatar {  /* 行注：定义 .member-avatar 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.member-info {  /* 行注：定义 .member-info 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.member-name {  /* 行注：定义 .member-name 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.35;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag,  /* 行注：补充 .member-role-tag 选择器 */
.member-self-tag {  /* 行注：定义 .member-self-tag 样式 */
  min-height: 20px;  /* 行注：设置 min-height 样式 */
  padding: 0 8px;  /* 行注：设置 padding 样式 */
  line-height: 20px;  /* 行注：设置 line-height 样式 */
  letter-spacing: 0.01em;  /* 行注：设置 letter-spacing 样式 */
  border: 1px solid transparent;  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag.owner {  /* 行注：定义 .member-role-tag.owner 样式 */
  background: rgba(77, 107, 255, 0.14);  /* 行注：设置 background 样式 */
  border-color: rgba(144, 167, 255, 0.16);  /* 行注：设置 border-color 样式 */
  color: #9cb0ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag.admin {  /* 行注：定义 .member-role-tag.admin 样式 */
  background: rgba(0, 214, 143, 0.1);  /* 行注：设置 background 样式 */
  border-color: rgba(55, 216, 170, 0.14);  /* 行注：设置 border-color 样式 */
  color: #43ddb1;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag.member,  /* 行注：补充 .member-role-tag.member 选择器 */
.member-self-tag {  /* 行注：定义 .member-self-tag 样式 */
  background: rgba(255, 255, 255, 0.06);  /* 行注：设置 background 样式 */
  border-color: rgba(255, 255, 255, 0.08);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.member-meta {  /* 行注：定义 .member-meta 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-actions {  /* 行注：定义 .member-actions 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  padding: 6px 10px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn.danger,  /* 行注：补充 .mini-btn.danger 选择器 */
.danger-action-btn {  /* 行注：定义 .danger-action-btn 样式 */
  color: #ff8b8b;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.danger-zone {  /* 行注：定义 .danger-zone 样式 */
  padding-top: 4px;  /* 行注：设置 padding-top 样式 */
}  /* 行注：结束当前样式块 */

.danger-action-btn {  /* 行注：定义 .danger-action-btn 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 18px;  /* 行注：设置 padding 样式 */
  border: 1px solid rgba(255, 107, 107, 0.18);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: rgba(255, 107, 107, 0.08);  /* 行注：设置 background 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.danger-action-btn:hover {  /* 行注：定义 .danger-action-btn:hover 样式 */
  background: rgba(255, 107, 107, 0.14);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.panel-placeholder.drawer-placeholder {  /* 行注：定义 .panel-placeholder.drawer-placeholder 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */
</style>
