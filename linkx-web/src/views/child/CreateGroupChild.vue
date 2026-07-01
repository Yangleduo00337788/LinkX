<template>
  <ChildWindowFrame :width="718" :height="427">
    <ContactPickerLayout
      v-model:selected-ids="memberIds"
      :friends="friends"
      title="发起群聊"
      submit-label="完成"
      :submit-disabled="creating"
      @submit="submit"
      @cancel="close"
    />
  </ChildWindowFrame>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { friendApi, groupApi } from '../../api/client'
import ContactPickerLayout from '../../components/chat/ContactPickerLayout.vue'
import type { FriendItem } from '../../types/chat'
import { closeLinkxChildWindow } from '../../utils/childWindow'
import ChildWindowFrame from './ChildWindowFrame.vue'

const message = useMessage()
const friends = ref<FriendItem[]>([])
const memberIds = ref<Array<string | number>>([])
const creating = ref(false)

onMounted(() => {
  document.body.classList.add('child-window-page')
  void loadFriends()
})

async function loadFriends() {
  try {
    const res: any = await friendApi.getList()
    friends.value = res.data || []
  } catch {
    friends.value = []
  }
}

async function close() {
  await closeLinkxChildWindow()
  window.close()
}

async function submit() {
  if (!memberIds.value.length) {
    message.warning('请至少选择一位好友')
    return
  }
  creating.value = true
  try {
    const res: any = await groupApi.create({
      groupName: `群聊（${memberIds.value.length + 1}）`,
      memberIds: memberIds.value.map(String)
    })
    message.success('群聊已创建')
    if (window.opener) {
      window.opener.postMessage({ type: 'linkx:group-created', groupId: res.data?.id }, '*')
    }
    await close()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '创建失败')
  } finally {
    creating.value = false
  }
}
</script>