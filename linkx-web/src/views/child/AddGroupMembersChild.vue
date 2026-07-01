<template>
  <ChildWindowFrame :width="718" :height="427">
    <ContactPickerLayout
      v-model:selected-ids="memberIds"
      :friends="friends"
      title="添加群成员"
      submit-label="添加"
      :submit-disabled="submitting"
      @submit="submit"
      @cancel="close"
    />
  </ChildWindowFrame>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { friendApi, groupApi } from '../../api/client'
import ContactPickerLayout from '../../components/chat/ContactPickerLayout.vue'
import type { FriendItem } from '../../types/chat'
import { closeLinkxChildWindow } from '../../utils/childWindow'
import ChildWindowFrame from './ChildWindowFrame.vue'

const route = useRoute()
const message = useMessage()
const friends = ref<FriendItem[]>([])
const memberIds = ref<Array<string | number>>([])
const submitting = ref(false)
const groupId = computed(() => String(route.query.groupId || ''))

onMounted(() => {
  document.body.classList.add('child-window-page')
  void loadFriends()
})

async function loadFriends() {
  try {
    const [friendRes, detailRes]: any[] = await Promise.all([
      friendApi.getList(),
      groupId.value ? groupApi.detail(groupId.value) : Promise.resolve({ data: null })
    ])
    const memberIdsSet = new Set(
      (detailRes?.data?.members || []).map((m: { userId: string | number }) => String(m.userId))
    )
    friends.value = (friendRes.data || []).filter((f: FriendItem) => !memberIdsSet.has(String(f.friendId)))
  } catch {
    friends.value = []
  }
}

async function close() {
  await closeLinkxChildWindow()
  window.close()
}

async function submit() {
  if (!groupId.value) {
    message.error('缺少群 ID')
    return
  }
  if (!memberIds.value.length) {
    message.warning('请选择联系人')
    return
  }
  submitting.value = true
  try {
    await groupApi.inviteMembers(groupId.value, memberIds.value.map(String), '')
    message.success('邀请已发送')
    if (window.opener) {
      window.opener.postMessage({ type: 'linkx:group-members-invited', groupId: groupId.value }, '*')
    }
    await close()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '添加失败')
  } finally {
    submitting.value = false
  }
}
</script>