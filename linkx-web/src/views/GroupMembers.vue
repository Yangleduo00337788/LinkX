<template>
  <div class="group-members-page">
    <input ref="groupAvatarInputRef" type="file" accept="image/*" class="hidden-file-input" @change="handleGroupAvatarSelected" />

    <div class="page-shell">
      <div class="page-header">
        <div class="page-header-main">
          <button class="back-btn" @click="openGroupChat">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6" />
            </svg>
            返回群聊
          </button>
          <div>
            <div class="page-title">群成员</div>
            <div class="page-subtitle">独立浏览群成员，并在这里直接完成成员权限管理</div>
          </div>
        </div>
        <div class="page-header-actions">
          <button class="secondary-btn" @click="copyGroupId">复制群号</button>
          <button class="primary-btn" :disabled="pageLoading" @click="loadGroupDetail(true)">
            {{ pageLoading ? '刷新中...' : '刷新成员' }}
          </button>
        </div>
      </div>

      <div v-if="groupDetail" class="page-content">
        <aside class="side-column">
          <section class="group-hero-card">
            <div class="group-hero-top">
              <div class="group-avatar">
                <img v-if="groupDetail.groupAvatar" :src="groupDetail.groupAvatar" class="avatar-img" />
                <span v-else>{{ groupDetail.groupName?.charAt(0) || '群' }}</span>
              </div>
              <div class="group-hero-main">
                <div class="group-name-row">
                  <h1>{{ groupDetail.groupName }}</h1>
                  <span class="role-chip self-role" :class="roleClass(groupDetail.myRole)">我是{{ roleText(groupDetail.myRole) }}</span>
                </div>
                <div class="group-meta-row">
                  <span>群号 {{ groupDetail.id }}</span>
                  <span>{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</span>
                </div>
                <div class="group-notice">
                  {{ groupDetail.notice?.trim() || '暂无群公告' }}
                </div>
              </div>
            </div>
            <div class="group-stat-row">
              <div class="stat-pill">
                <span class="stat-label">群主</span>
                <strong>{{ ownerCount }}</strong>
              </div>
              <div class="stat-pill">
                <span class="stat-label">管理员</span>
                <strong>{{ adminCount }}</strong>
              </div>
              <div class="stat-pill">
                <span class="stat-label">普通成员</span>
                <strong>{{ memberCount }}</strong>
              </div>
              <div class="stat-pill warn">
                <span class="stat-label">已禁言</span>
                <strong>{{ mutedCount }}</strong>
              </div>
            </div>
          </section>

          <section class="panel-card profile-card">
            <div class="panel-title-row">
              <div>
                <div class="panel-title">群资料</div>
                <div class="panel-note">{{ canEditGroupProfile ? '支持直接编辑群名称、头像与公告' : '仅群主和管理员可编辑群资料' }}</div>
              </div>
            </div>

            <div class="profile-editor">
              <button
                class="profile-avatar-editor"
                :class="{ editable: canEditGroupProfile }"
                :disabled="!canEditGroupProfile || updatingGroupProfile"
                @click="triggerGroupAvatarUpload"
              >
                <img
                  v-if="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
                  :src="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
                  class="avatar-img"
                />
                <span v-else>{{ groupProfileDraft.groupName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
                <span v-if="canEditGroupProfile" class="profile-avatar-mask">更换头像</span>
              </button>

              <div class="profile-fields">
                <label class="field-label" for="groupNameInput">群名称</label>
                <input
                  id="groupNameInput"
                  v-model="groupProfileDraft.groupName"
                  type="text"
                  class="text-input filled-input"
                  maxlength="30"
                  :disabled="!canEditGroupProfile || updatingGroupProfile"
                  placeholder="请输入群名称"
                />

                <label class="field-label" for="groupNoticeInput">群公告</label>
                <textarea
                  id="groupNoticeInput"
                  v-model="noticeDraft"
                  class="text-area"
                  rows="4"
                  maxlength="255"
                  :disabled="!canEditNotice || updatingNotice"
                  :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
                ></textarea>
              </div>
            </div>

            <div class="profile-action-row">
              <button
                class="secondary-btn"
                :disabled="(!isGroupProfileChanged && !isGroupNoticeChanged) || updatingGroupProfile || updatingNotice"
                @click="discardGroupProfileDrafts"
              >
                还原修改
              </button>
              <button
                v-if="canEditNotice"
                class="secondary-btn"
                :disabled="!isGroupNoticeChanged || updatingNotice"
                @click="submitUpdateNotice"
              >
                {{ updatingNotice ? '保存公告中...' : '保存公告' }}
              </button>
              <button
                v-if="canEditGroupProfile"
                class="primary-btn"
                :disabled="!isGroupProfileChanged || updatingGroupProfile"
                @click="submitUpdateGroupProfile"
              >
                {{ updatingGroupProfile ? '保存资料中...' : '保存资料' }}
              </button>
            </div>
          </section>

          <section class="panel-card filter-card">
            <div class="panel-title">筛选成员</div>
            <div class="search-shell">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <input
                v-model="searchText"
                type="text"
                class="search-input"
                placeholder="搜索昵称或用户名"
              />
            </div>
            <div class="role-filter-group">
              <button
                v-for="option in roleFilters"
                :key="option.value"
                class="role-filter-btn"
                :class="{ active: roleFilter === option.value }"
                @click="roleFilter = option.value"
              >
                {{ option.label }}
              </button>
            </div>
            <div class="filter-hint">
              当前展示 {{ filteredMembers.length }} / {{ groupDetail.members.length }} 名成员
            </div>
          </section>

          <section class="panel-card manage-card">
            <div class="panel-title">群管理</div>
            <div class="manage-subtitle">把常用群操作集中到这里处理，减少在聊天页和成员页之间来回切换。</div>

            <div class="manage-summary-grid">
              <div class="manage-summary-item">
                <span class="manage-summary-label">我的权限</span>
                <strong>{{ roleText(currentGroupRole) }}</strong>
              </div>
              <div class="manage-summary-item">
                <span class="manage-summary-label">可邀请好友</span>
                <strong>{{ availableFriendsForCurrentGroup.length }}</strong>
              </div>
            </div>

            <div class="manage-action-list">
              <button v-if="canManageMembers" class="secondary-btn action-btn" @click="openAddMembersModal">
                邀请成员
              </button>
              <button v-if="isGroupOwner" class="secondary-btn action-btn" @click="openTransferOwnerModal">
                转让群主
              </button>
              <button v-if="!isGroupOwner" class="secondary-btn action-btn warn" @click="handleLeaveGroup">
                退出群聊
              </button>
              <button v-if="canDissolveGroup" class="secondary-btn action-btn danger" @click="handleDissolveGroup">
                解散群聊
              </button>
            </div>

            <div class="manage-tip">
              <span v-if="canManageMembers">你可以直接邀请好友或处理群管理动作。</span>
              <span v-else>当前仅支持浏览成员信息，如需更多操作请联系群管理员。</span>
            </div>
          </section>
        </aside>

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
                    <span v-if="String(member.userId) === String(userStore.userId)" class="self-tag">我</span>
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
                    @click="toggleAdminRole(member)"
                  >
                    {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
                  </button>
                  <button
                    class="mini-btn"
                    :disabled="actionLoading"
                    @click="toggleMuteMember(member)"
                  >
                    {{ member.muted ? '解除禁言' : '禁言成员' }}
                  </button>
                  <button
                    class="mini-btn danger"
                    :disabled="actionLoading"
                    @click="handleRemoveMember(member)"
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
      </div>

      <div v-else class="loading-panel">
        <div class="loading-spinner"></div>
        <div class="loading-text">正在加载群成员...</div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showAddMembersModal" class="modal-overlay" @click.self="closeAddMembersModal">
        <div class="modal-card picker-modal">
          <div class="modal-header">
            <div>
              <div class="modal-title">邀请成员</div>
              <div class="modal-subtitle">向好友发送入群邀请，待对方确认后加入</div>
            </div>
            <button class="modal-close" @click="closeAddMembersModal">×</button>
          </div>

          <div v-if="availableFriendsForCurrentGroup.length > 0" class="member-picker-list">
            <label v-for="friend in availableFriendsForCurrentGroup" :key="friend.friendId" class="member-picker-item">
              <input v-model="addMembersSelection" :value="friend.friendId" type="checkbox" />
              <div class="member-picker-avatar">
                <img v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
                <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
              </div>
              <div class="member-picker-info">
                <span class="member-picker-name">{{ friend.friendNickname }}</span>
                <span class="member-picker-meta">@{{ friend.friendUsername }}</span>
              </div>
            </label>
          </div>
          <div v-else class="picker-empty-state">所有好友都已在群内，暂无可邀请成员。</div>

          <div class="modal-body">
            <label class="form-label" for="addMembersMessage">邀请说明</label>
            <textarea
              id="addMembersMessage"
              v-model="addMembersMessage"
              class="text-area"
              rows="3"
              maxlength="255"
              placeholder="选填，邀请说明会出现在对方的群通知中"
            ></textarea>
          </div>

          <div class="modal-actions">
            <button class="secondary-btn" :disabled="addingMembers" @click="closeAddMembersModal">取消</button>
            <button class="primary-btn" :disabled="addingMembers || addMembersSelection.length === 0" @click="submitAddMembers">
              {{ addingMembers ? '邀请中...' : '确认邀请' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="showTransferOwnerModal" class="modal-overlay" @click.self="closeTransferOwnerModal">
        <div class="modal-card picker-modal">
          <div class="modal-header">
            <div>
              <div class="modal-title">转让群主</div>
              <div class="modal-subtitle">选择一位成员成为新的群主</div>
            </div>
            <button class="modal-close" @click="closeTransferOwnerModal">×</button>
          </div>

          <div v-if="transferableMembers.length > 0" class="member-picker-list">
            <label v-for="member in transferableMembers" :key="member.userId" class="member-picker-item">
              <input v-model="transferOwnerSelection" :value="member.userId" type="radio" name="transferOwner" />
              <div class="member-picker-avatar">
                <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                <span v-else>{{ member.nickname?.charAt(0) || member.username?.charAt(0) || '群' }}</span>
              </div>
              <div class="member-picker-info">
                <span class="member-picker-name">{{ member.nickname || member.username }}</span>
                <span class="member-picker-meta">@{{ member.username }} · {{ roleText(member.role) }}</span>
              </div>
            </label>
          </div>
          <div v-else class="picker-empty-state">群内没有其他成员可转让。</div>

          <div class="modal-actions">
            <button class="secondary-btn" :disabled="transferringOwner" @click="closeTransferOwnerModal">取消</button>
            <button class="primary-btn" :disabled="!transferOwnerSelection || transferringOwner" @click="submitTransferOwner">
              {{ transferringOwner ? '转让中...' : '确认转让' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="showMuteMemberModal" class="modal-overlay" @click.self="closeMuteMemberModal">
        <div class="modal-card mute-modal">
          <div class="modal-header">
            <div>
              <div class="modal-title">设置成员禁言</div>
              <div class="modal-subtitle">{{ muteTargetMember?.nickname || muteTargetMember?.username || '成员' }}</div>
            </div>
            <button class="modal-close" @click="closeMuteMemberModal">×</button>
          </div>
          <div class="modal-body">
            <label class="form-label" for="muteMinutes">禁言时长（分钟）</label>
            <input
              id="muteMinutes"
              v-model="muteMinutesInput"
              type="number"
              min="1"
              max="43200"
              class="text-input"
              placeholder="请输入 1-43200 之间的整数"
            />
          </div>
          <div class="modal-actions">
            <button class="secondary-btn" :disabled="actionLoading" @click="closeMuteMemberModal">取消</button>
            <button class="primary-btn" :disabled="actionLoading" @click="submitMuteMember">
              {{ actionLoading ? '提交中...' : '确认禁言' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <ConfirmDialog
      v-model:visible="confirmDialog.visible"
      :title="confirmDialog.title"
      :subtitle="confirmDialog.subtitle"
      :description="confirmDialog.description"
      :cancel-text="confirmDialog.cancelText"
      :confirm-text="confirmDialog.confirmText"
      :confirm-type="confirmDialog.confirmType"
      @cancel="cancelConfirmDialog"
      @confirm="confirmConfirmDialog"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { fileApi, friendApi, groupApi } from '../api/client'
import { useUserStore } from '../stores/user'
import { useConfirmDialog } from '../hooks/useConfirmDialog'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import { parseDateTime } from '../utils/datetime'

const GROUP_ROLE_MEMBER = 0
const GROUP_ROLE_ADMIN = 1
const GROUP_ROLE_OWNER = 2

interface GroupMember {
  userId: string | number
  username: string
  nickname: string
  avatar?: string
  role: number
  muted?: boolean
  muteTime?: string
}

interface FriendItem {
  friendId: string | number
  friendNickname: string
  friendAvatar?: string
  friendUsername: string
}

interface GroupDetail {
  id: string | number
  groupName: string
  groupAvatar?: string
  notice?: string
  ownerId: string | number
  maxMembers: number
  memberCount: number
  myRole: number
  members: GroupMember[]
}

type RoleFilter = 'all' | 'owner' | 'admin' | 'member' | 'muted'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const { confirmDialog, openConfirmDialog, cancelConfirmDialog, confirmConfirmDialog } = useConfirmDialog()

const groupAvatarInputRef = ref<HTMLInputElement>()
const groupDetail = ref<GroupDetail | null>(null)
const friends = ref<FriendItem[]>([])
const pageLoading = ref(false)
const actionLoading = ref(false)
const searchText = ref('')
const roleFilter = ref<RoleFilter>('all')
const updatingGroupProfile = ref(false)
const updatingNotice = ref(false)
const noticeDraft = ref('')
const showAddMembersModal = ref(false)
const addMembersSelection = ref<Array<string | number>>([])
const addMembersMessage = ref('')
const addingMembers = ref(false)
const showTransferOwnerModal = ref(false)
const transferOwnerSelection = ref<string | number | null>(null)
const transferringOwner = ref(false)
const showMuteMemberModal = ref(false)
const muteTargetMember = ref<GroupMember | null>(null)
const muteMinutesInput = ref('10')

const groupProfileDraft = reactive({
  groupName: '',
  avatarPreview: '',
  avatarFile: null as File | null
})

const roleFilters = [
  { label: '全部', value: 'all' as RoleFilter },
  { label: '群主', value: 'owner' as RoleFilter },
  { label: '管理员', value: 'admin' as RoleFilter },
  { label: '普通成员', value: 'member' as RoleFilter },
  { label: '已禁言', value: 'muted' as RoleFilter }
]

const groupId = computed(() => String(route.params.groupId || ''))
const currentGroupRole = computed(() => groupDetail.value?.myRole ?? GROUP_ROLE_MEMBER)
const canManageMembers = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditGroupProfile = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
const canEditNotice = computed(() => currentGroupRole.value >= GROUP_ROLE_ADMIN)
const isGroupOwner = computed(() => currentGroupRole.value === GROUP_ROLE_OWNER)
const canDissolveGroup = computed(() => isGroupOwner.value)

const ownerCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_OWNER).length)
const adminCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_ADMIN).length)
const mutedCount = computed(() => (groupDetail.value?.members || []).filter(member => member.muted).length)
const memberCount = computed(() => (groupDetail.value?.members || []).filter(member => member.role === GROUP_ROLE_MEMBER).length)
const activeRoleFilterLabel = computed(() => roleFilters.find(option => option.value === roleFilter.value)?.label || '全部')
const manageableMemberCount = computed(() => (groupDetail.value?.members || []).filter(member => canOperateMember(member)).length)
const isGroupProfileChanged = computed(() => {
  if (!groupDetail.value) {
    return false
  }
  const normalizedName = groupProfileDraft.groupName.trim()
  const currentName = groupDetail.value.groupName?.trim() || ''
  const currentAvatar = groupDetail.value.groupAvatar || ''
  return normalizedName !== currentName || Boolean(groupProfileDraft.avatarFile) || groupProfileDraft.avatarPreview !== currentAvatar
})
const isGroupNoticeChanged = computed(() => {
  if (!groupDetail.value) {
    return false
  }
  return noticeDraft.value.trim() !== (groupDetail.value.notice || '').trim()
})
const availableFriendsForCurrentGroup = computed(() => {
  const memberIds = new Set((groupDetail.value?.members || []).map(member => String(member.userId)))
  return friends.value.filter(friend => !memberIds.has(String(friend.friendId)))
})
const transferableMembers = computed(() => (groupDetail.value?.members || []).filter(member => {
  return String(member.userId) !== String(userStore.userId) && member.role !== GROUP_ROLE_OWNER
}))

const filteredMembers = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  const members = [...(groupDetail.value?.members || [])].sort((left, right) => {
    if (left.role !== right.role) {
      return right.role - left.role
    }
    return String(left.userId).localeCompare(String(right.userId))
  })
  return members.filter(member => {
    if (roleFilter.value === 'owner' && member.role !== GROUP_ROLE_OWNER) {
      return false
    }
    if (roleFilter.value === 'admin' && member.role !== GROUP_ROLE_ADMIN) {
      return false
    }
    if (roleFilter.value === 'member' && member.role !== GROUP_ROLE_MEMBER) {
      return false
    }
    if (roleFilter.value === 'muted' && !member.muted) {
      return false
    }
    if (!keyword) {
      return true
    }
    const searchableText = `${member.nickname || ''} ${member.username || ''}`.toLowerCase()
    return searchableText.includes(keyword)
  })
})

function roleText(role?: number) {
  if (role === GROUP_ROLE_OWNER) {
    return '群主'
  }
  if (role === GROUP_ROLE_ADMIN) {
    return '管理员'
  }
  return '成员'
}

function roleClass(role?: number) {
  if (role === GROUP_ROLE_OWNER) {
    return 'owner'
  }
  if (role === GROUP_ROLE_ADMIN) {
    return 'admin'
  }
  return 'member'
}

function formatDateTime(time?: string) {
  if (!time) {
    return ''
  }
  const date = parseDateTime(time)
  if (!date) {
    return ''
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

function canOperateMember(member: GroupMember) {
  const myId = String(userStore.userId)
  if (!canManageMembers.value || String(member.userId) === myId) {
    return false
  }
  if (currentGroupRole.value === GROUP_ROLE_OWNER) {
    return member.role !== GROUP_ROLE_OWNER
  }
  return member.role === GROUP_ROLE_MEMBER
}

function canToggleAdmin(member: GroupMember) {
  return currentGroupRole.value === GROUP_ROLE_OWNER
    && String(member.userId) !== String(userStore.userId)
    && member.role !== GROUP_ROLE_OWNER
}

async function loadGroupDetail(showSuccess = false) {
  if (!groupId.value) {
    return
  }
  pageLoading.value = true
  try {
    const response: any = await groupApi.detail(groupId.value)
    applyGroupDetail(response.data || null, true)
    if (showSuccess) {
      message.success('群成员已刷新')
    }
  } catch (error: any) {
    console.error('loadGroupDetail error:', error)
    message.error(error.response?.data?.message || '加载群成员失败')
  } finally {
    pageLoading.value = false
  }
}

function resetGroupProfileDraft() {
  if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(groupProfileDraft.avatarPreview)
  }
  groupProfileDraft.groupName = ''
  groupProfileDraft.avatarPreview = ''
  groupProfileDraft.avatarFile = null
  if (groupAvatarInputRef.value) {
    groupAvatarInputRef.value.value = ''
  }
}

function syncGroupProfileDraft(detail?: GroupDetail | null) {
  if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(groupProfileDraft.avatarPreview)
  }
  groupProfileDraft.groupName = detail?.groupName || ''
  groupProfileDraft.avatarPreview = detail?.groupAvatar || ''
  groupProfileDraft.avatarFile = null
  if (groupAvatarInputRef.value) {
    groupAvatarInputRef.value.value = ''
  }
}

function discardGroupProfileDrafts() {
  noticeDraft.value = groupDetail.value?.notice || ''
  syncGroupProfileDraft(groupDetail.value)
}

function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {
  groupDetail.value = detail
  if (syncDraft || !isGroupNoticeChanged.value) {
    noticeDraft.value = detail?.notice || ''
  }
  if (syncDraft || !isGroupProfileChanged.value) {
    syncGroupProfileDraft(detail)
  }
}

async function loadFriends() {
  try {
    const response: any = await friendApi.getList()
    friends.value = response.data || []
  } catch (error) {
    console.error('loadFriends error:', error)
  }
}

async function copyGroupId() {
  if (!groupDetail.value) {
    return
  }
  try {
    await navigator.clipboard.writeText(String(groupDetail.value.id))
    message.success(`群号 ${groupDetail.value.id} 已复制`)
  } catch (error) {
    message.error('复制群号失败')
  }
}

function openGroupChat() {
  if (!groupId.value) {
    router.push('/chat')
    return
  }
  router.push({
    path: `/chat/${groupId.value}`,
    query: { sessionType: '2' }
  })
}

function triggerGroupAvatarUpload() {
  if (!canEditGroupProfile.value) {
    return
  }
  groupAvatarInputRef.value?.click()
}

function handleGroupAvatarSelected(event: Event) {
  if (!canEditGroupProfile.value) {
    return
  }
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  const previewUrl = URL.createObjectURL(file)
  if (groupProfileDraft.avatarPreview.startsWith('blob:')) {
    URL.revokeObjectURL(groupProfileDraft.avatarPreview)
  }
  groupProfileDraft.avatarFile = file
  groupProfileDraft.avatarPreview = previewUrl
}

async function submitUpdateNotice() {
  if (!groupId.value || !canEditNotice.value) {
    return
  }
  updatingNotice.value = true
  try {
    await groupApi.updateNotice(groupId.value, noticeDraft.value.trim())
    if (groupDetail.value) {
      applyGroupDetail({
        ...groupDetail.value,
        notice: noticeDraft.value.trim()
      }, false)
    }
    message.success('群公告已更新')
  } catch (error: any) {
    console.error('submitUpdateNotice error:', error)
    message.error(error.response?.data?.message || '更新群公告失败')
  } finally {
    updatingNotice.value = false
  }
}

async function submitUpdateGroupProfile() {
  if (!groupId.value || !groupDetail.value || !canEditGroupProfile.value) {
    return
  }
  const nextGroupName = groupProfileDraft.groupName.trim()
  if (!nextGroupName) {
    message.warning('请输入群名称')
    return
  }

  updatingGroupProfile.value = true
  try {
    let groupAvatar = groupDetail.value.groupAvatar || ''
    if (groupProfileDraft.avatarFile) {
      const uploadResponse: any = await fileApi.uploadImage(groupProfileDraft.avatarFile)
      groupAvatar = uploadResponse.data?.fileUrl || ''
    }
    await groupApi.updateProfile(groupId.value, {
      groupName: nextGroupName,
      groupAvatar
    })
    if (groupDetail.value) {
      applyGroupDetail({
        ...groupDetail.value,
        groupName: nextGroupName,
        groupAvatar
      }, true)
    }
    message.success('群资料已更新')
  } catch (error: any) {
    console.error('submitUpdateGroupProfile error:', error)
    message.error(error.response?.data?.message || '更新群资料失败')
  } finally {
    updatingGroupProfile.value = false
  }
}

function openAddMembersModal() {
  if (!canManageMembers.value) {
    return
  }
  addMembersSelection.value = []
  addMembersMessage.value = ''
  showAddMembersModal.value = true
}

function closeAddMembersModal() {
  if (addingMembers.value) {
    return
  }
  showAddMembersModal.value = false
  addMembersSelection.value = []
  addMembersMessage.value = ''
}

function resetAddMembersModal() {
  showAddMembersModal.value = false
  addMembersSelection.value = []
  addMembersMessage.value = ''
}

async function submitAddMembers() {
  if (!groupId.value || addMembersSelection.value.length === 0) {
    return
  }
  addingMembers.value = true
  try {
    await groupApi.inviteMembers(groupId.value, addMembersSelection.value, addMembersMessage.value.trim())
    resetAddMembersModal()
    message.success('邀请已发送')
  } catch (error: any) {
    console.error('submitAddMembers error:', error)
    message.error(error.response?.data?.message || '邀请成员失败')
  } finally {
    addingMembers.value = false
  }
}

function openTransferOwnerModal() {
  if (!isGroupOwner.value) {
    return
  }
  transferOwnerSelection.value = null
  showTransferOwnerModal.value = true
}

function closeTransferOwnerModal() {
  if (transferringOwner.value) {
    return
  }
  showTransferOwnerModal.value = false
  transferOwnerSelection.value = null
}

function resetTransferOwnerModal() {
  showTransferOwnerModal.value = false
  transferOwnerSelection.value = null
}

async function submitTransferOwner() {
  if (!groupId.value || !transferOwnerSelection.value) {
    return
  }
  const member = groupDetail.value?.members.find(item => String(item.userId) === String(transferOwnerSelection.value))
  const memberName = member?.nickname || member?.username || '该成员'
  const confirmed = await openConfirmDialog({
    title: '确认转让群主',
    subtitle: memberName,
    description: `确认将群主转让给 ${memberName} 吗？转让后你将变为普通成员。`,
    confirmText: '确认转让',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }

  transferringOwner.value = true
  try {
    await groupApi.transferOwner(groupId.value, transferOwnerSelection.value)
    resetTransferOwnerModal()
    await loadGroupDetail()
    message.success('群主已转让')
  } catch (error: any) {
    console.error('submitTransferOwner error:', error)
    message.error(error.response?.data?.message || '转让群主失败')
  } finally {
    transferringOwner.value = false
  }
}

async function toggleAdminRole(memberItem: GroupMember) {
  if (!groupDetail.value || !canToggleAdmin(memberItem)) {
    return
  }
  const nextActionText = memberItem.role === GROUP_ROLE_ADMIN ? '取消该成员的管理员身份' : '将该成员设为管理员'
  const confirmed = await openConfirmDialog({
    title: '确认成员权限变更',
    subtitle: nextActionText,
    description: `确认${nextActionText}吗？`,
    confirmText: '确认操作'
  })
  if (!confirmed) {
    return
  }

  actionLoading.value = true
  try {
    if (memberItem.role === GROUP_ROLE_ADMIN) {
      await groupApi.removeAdmin(groupDetail.value.id, memberItem.userId)
      message.success('已取消管理员')
    } else {
      await groupApi.setAdmin(groupDetail.value.id, memberItem.userId)
      message.success('已设为管理员')
    }
    await loadGroupDetail()
  } catch (error: any) {
    console.error('toggleAdminRole error:', error)
    message.error(error.response?.data?.message || '管理员操作失败')
  } finally {
    actionLoading.value = false
  }
}

function openMuteMemberModal(memberItem: GroupMember) {
  muteTargetMember.value = memberItem
  muteMinutesInput.value = '10'
  showMuteMemberModal.value = true
}

function closeMuteMemberModal() {
  if (actionLoading.value) {
    return
  }
  muteTargetMember.value = null
  muteMinutesInput.value = '10'
  showMuteMemberModal.value = false
}

async function toggleMuteMember(memberItem: GroupMember) {
  if (!groupDetail.value) {
    return
  }
  if (memberItem.muted) {
    actionLoading.value = true
    try {
      await groupApi.unmuteMember(groupDetail.value.id, memberItem.userId)
      message.success('已解除禁言')
      await loadGroupDetail()
    } catch (error: any) {
      console.error('toggleMuteMember error:', error)
      message.error(error.response?.data?.message || '操作失败')
    } finally {
      actionLoading.value = false
    }
    return
  }
  openMuteMemberModal(memberItem)
}

async function submitMuteMember() {
  if (!groupDetail.value || !muteTargetMember.value) {
    return
  }
  const muteMinutes = Number(muteMinutesInput.value)
  if (!Number.isInteger(muteMinutes) || muteMinutes <= 0 || muteMinutes > 43200) {
    message.warning('请输入 1 到 43200 之间的整数分钟数')
    return
  }

  actionLoading.value = true
  try {
    await groupApi.muteMember(groupDetail.value.id, muteTargetMember.value.userId, muteMinutes)
    message.success('已设置禁言')
    closeMuteMemberModal()
    await loadGroupDetail()
  } catch (error: any) {
    console.error('submitMuteMember error:', error)
    message.error(error.response?.data?.message || '设置禁言失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleRemoveMember(memberItem: GroupMember) {
  if (!groupDetail.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认移出成员',
    subtitle: memberItem.nickname || memberItem.username || '该成员',
    description: `确认将 ${memberItem.nickname || memberItem.username} 移出群聊吗？`,
    confirmText: '确认移出',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }

  actionLoading.value = true
  try {
    await groupApi.removeMember(groupDetail.value.id, memberItem.userId)
    message.success('成员已移出群聊')
    await loadGroupDetail()
  } catch (error: any) {
    console.error('handleRemoveMember error:', error)
    message.error(error.response?.data?.message || '移除成员失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleDissolveGroup() {
  if (!groupId.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认解散群聊',
    subtitle: '该操作不可恢复',
    description: '解散后，当前群聊与会话记录入口将被移除。',
    confirmText: '确认解散',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }

  try {
    await groupApi.dissolve(groupId.value)
    message.success('群聊已解散')
    await router.replace('/chat')
  } catch (error: any) {
    console.error('handleDissolveGroup error:', error)
    message.error(error.response?.data?.message || '解散群聊失败')
  }
}

async function handleLeaveGroup() {
  if (!groupId.value) {
    return
  }
  const confirmed = await openConfirmDialog({
    title: '确认退出群聊',
    subtitle: groupDetail.value?.groupName || '当前群聊',
    description: '退出后你将不再接收该群消息，需要重新被邀请才能再次加入。',
    confirmText: '确认退出',
    confirmType: 'danger'
  })
  if (!confirmed) {
    return
  }

  try {
    await groupApi.leaveGroup(groupId.value)
    message.success('已退出群聊')
    await router.replace('/chat')
  } catch (error: any) {
    console.error('handleLeaveGroup error:', error)
    message.error(error.response?.data?.message || '退出群聊失败')
  }
}

watch(() => route.params.groupId, () => {
  searchText.value = ''
  roleFilter.value = 'all'
  void loadGroupDetail()
})

onMounted(() => {
  void loadFriends()
  void loadGroupDetail()
})

onUnmounted(() => {
  resetGroupProfileDraft()
})
</script>

<style scoped>
.group-members-page {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 214, 143, 0.08), transparent 28%),
    radial-gradient(circle at left center, rgba(80, 145, 255, 0.08), transparent 24%),
    var(--linkx-bg);
}

.hidden-file-input {
  display: none;
}

.page-shell {
  width: min(1180px, 74vw, 100%);
  margin: 0 auto;
}

.page-header,
.group-hero-card,
.panel-card,
.member-card,
.modal-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-md);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--linkx-radius-lg);
}

.page-header-main,
.group-hero-top,
.member-card-main,
.member-name-row,
.group-meta-row,
.page-header-actions,
.group-stat-row,
.member-actions,
.modal-actions {
  display: flex;
  align-items: center;
}

.page-header-main {
  gap: 16px;
}

.page-header-actions,
.group-stat-row,
.member-actions,
.modal-actions {
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--linkx-text);
}

.page-subtitle,
.group-meta-row,
.member-username,
.member-status,
.filter-hint,
.panel-subtitle,
.modal-subtitle {
  color: var(--linkx-text-muted);
}

.page-subtitle {
  margin-top: 6px;
  font-size: 13px;
}

.back-btn,
.primary-btn,
.secondary-btn,
.mini-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  font-size: 13px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.back-btn,
.secondary-btn,
.mini-btn {
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
}

.primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.back-btn:hover,
.secondary-btn:hover,
.mini-btn:hover {
  background: var(--linkx-bg-hover);
}

.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.page-content {
  margin-top: 20px;
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  align-items: start;
  gap: 24px;
}

.side-column {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.group-hero-card,
.panel-card {
  border-radius: var(--linkx-radius-lg);
  padding: 22px;
}

.group-hero-card {
  position: relative;
  overflow: hidden;
}

.group-hero-card::after {
  content: '';
  position: absolute;
  inset: auto -60px -60px auto;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 214, 143, 0.14), transparent 68%);
  pointer-events: none;
}

.group-hero-top {
  align-items: flex-start;
  gap: 18px;
}

.group-avatar,
.member-avatar {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 24px;
  font-weight: 700;
}

.member-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  font-size: 18px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-hero-main,
.member-info {
  min-width: 0;
  flex: 1;
}

.group-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.group-name-row h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  color: var(--linkx-text);
}

.group-meta-row {
  gap: 14px;
  margin-top: 10px;
  font-size: 13px;
  flex-wrap: wrap;
}

.group-notice {
  margin-top: 14px;
  max-width: 680px;
  color: var(--linkx-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.stat-pill,
.role-chip,
.self-tag,
.mute-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-weight: 600;
}

.group-stat-row {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-pill {
  min-width: 0;
  padding: 14px 16px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  border-radius: 18px;
}

.stat-pill strong {
  color: var(--linkx-text);
  font-size: 20px;
}

.stat-pill.warn strong {
  color: #f0a020;
}

.stat-label {
  font-size: 12px;
}

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
}

.panel-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.panel-note {
  margin-top: 6px;
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-editor {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: start;
  gap: 16px;
}

.profile-avatar-editor {
  position: relative;
  width: 96px;
  height: 96px;
  border: 1px solid var(--linkx-border);
  border-radius: 28px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 30px;
  font-weight: 700;
  cursor: default;
}

.profile-avatar-editor.editable {
  cursor: pointer;
}

.profile-avatar-mask {
  position: absolute;
  inset: auto 0 0;
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.48);
  color: white;
  font-size: 12px;
  text-align: center;
}

.profile-fields {
  min-width: 0;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.field-label + .filled-input,
.field-label + .text-area {
  margin-bottom: 14px;
}

.filled-input {
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
}

.profile-action-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
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

.search-input,
.text-input {
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
  font-size: 12px;
}

.manage-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.manage-subtitle {
  color: var(--linkx-text-muted);
  font-size: 13px;
  line-height: 1.7;
}

.manage-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.manage-summary-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
}

.manage-summary-label {
  display: block;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.manage-summary-item strong {
  display: block;
  margin-top: 8px;
  color: var(--linkx-text);
  font-size: 18px;
}

.manage-action-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.action-btn {
  width: 100%;
  justify-content: center;
}

.action-btn.warn {
  color: #d88914;
}

.action-btn.danger {
  color: #ff6b6b;
}

.manage-tip {
  color: var(--linkx-text-secondary);
  font-size: 12px;
  line-height: 1.7;
  padding: 12px 14px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--linkx-bg-hover) 68%, transparent);
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
  color: var(--linkx-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
}

.member-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
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
  align-items: flex-start;
  gap: 14px;
}

.member-name-row {
  gap: 8px;
  flex-wrap: wrap;
}

.member-name {
  color: var(--linkx-text);
  font-size: 15px;
  font-weight: 700;
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
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.4;
}

.role-chip,
.self-tag,
.mute-tag {
  height: 24px;
  padding: 0 10px;
  font-size: 12px;
}

.role-chip.owner,
.self-role.owner {
  background: rgba(255, 187, 80, 0.18);
  color: #d88914;
}

.role-chip.admin,
.self-role.admin {
  background: rgba(80, 145, 255, 0.16);
  color: #4f86ff;
}

.role-chip.member,
.self-role.member,
.self-tag {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
}

.mute-tag {
  background: rgba(240, 160, 32, 0.16);
  color: #d88914;
}

.member-actions {
  margin-top: 0;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.mini-btn.danger {
  color: #ff6b6b;
}

.empty-state,
.loading-panel {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-title,
.loading-text {
  color: var(--linkx-text);
  font-size: 16px;
  font-weight: 700;
}

.empty-subtitle {
  margin-top: 8px;
  color: var(--linkx-text-muted);
  font-size: 13px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
  animation: spin 0.8s linear infinite;
}

.loading-text {
  margin-top: 14px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 2100;
}

.mute-modal {
  width: min(420px, 100%);
  padding: 22px;
  border-radius: var(--linkx-radius-lg);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.modal-title {
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
}

.modal-subtitle {
  margin-top: 4px;
  font-size: 13px;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: var(--linkx-radius-md);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 18px;
  line-height: 1;
}

.modal-body {
  margin: 20px 0 18px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.text-input {
  height: 42px;
  padding: 0 14px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
}

.picker-modal {
  width: min(560px, 100%);
  padding: 22px;
  border-radius: var(--linkx-radius-lg);
}

.member-picker-list {
  display: grid;
  gap: 12px;
  max-height: 340px;
  margin-top: 18px;
  padding-right: 4px;
  overflow-y: auto;
}

.member-picker-item {
  display: grid;
  grid-template-columns: auto 48px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);
  cursor: pointer;
}

.member-picker-item input {
  margin: 0;
}

.member-picker-avatar {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.member-picker-info {
  min-width: 0;
}

.member-picker-name,
.member-picker-meta {
  display: block;
}

.member-picker-name {
  color: var(--linkx-text);
  font-size: 14px;
  font-weight: 700;
}

.member-picker-meta {
  margin-top: 4px;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.picker-empty-state {
  margin-top: 18px;
  padding: 18px;
  border-radius: 16px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-muted);
  font-size: 13px;
  line-height: 1.7;
  text-align: center;
}

.text-area {
  width: 100%;
  min-height: 92px;
  padding: 12px 14px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  resize: vertical;
  outline: none;
  font-size: 14px;
  line-height: 1.6;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1400px) {
  .page-shell {
    width: min(1120px, 84vw, 100%);
  }
}

@media (max-width: 1100px) {
  .page-shell {
    width: min(920px, 92vw, 100%);
  }

  .page-content {
    grid-template-columns: 1fr;
  }

  .side-column {
    position: static;
  }

  .member-panel-head,
  .member-panel-toolbar,
  .member-card {
    grid-template-columns: 1fr;
  }

  .member-panel-head,
  .member-panel-toolbar {
    display: grid;
  }

  .member-panel-overview {
    width: 100%;
  }

  .manage-summary-grid {
    grid-template-columns: 1fr;
  }

  .profile-editor {
    grid-template-columns: 1fr;
  }

  .member-card-side {
    align-items: flex-start;
  }

  .member-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .group-members-page {
    padding: 16px;
  }

  .page-shell {
    width: 100%;
  }

  .page-header,
  .page-header-main,
  .page-header-actions,
  .group-hero-top,
  .member-card-main,
  .member-panel-toolbar,
  .profile-action-row {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header-actions,
  .member-actions,
  .modal-actions {
    width: 100%;
  }

  .back-btn,
  .primary-btn,
  .secondary-btn,
  .mini-btn {
    width: 100%;
    justify-content: center;
  }

  .group-stat-row {
    grid-template-columns: 1fr;
  }

  .member-picker-item {
    grid-template-columns: auto 40px minmax(0, 1fr);
  }

  .member-picker-avatar {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    font-size: 16px;
  }

  .member-panel-overview {
    grid-template-columns: 1fr;
  }

  .member-actions {
    justify-content: stretch;
  }
}
</style>
