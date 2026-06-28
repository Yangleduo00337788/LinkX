<template>
  <AdminPageShell hint="系统通知会出现在客户端侧栏「通知」中；指定用户发送会即时推送未读角标（WebSocket）。">
    <n-tabs type="line" animated>
      <n-tab-pane name="broadcast" tab="全体广播">
        <n-card title="向全体启用用户发送" style="max-width: 560px">
          <n-form label-placement="left" label-width="72">
            <n-form-item label="标题" required>
              <n-input v-model:value="broadcastForm.title" maxlength="120" show-count />
            </n-form-item>
            <n-form-item label="内容" required>
              <n-input v-model:value="broadcastForm.content" type="textarea" :rows="5" maxlength="2000" show-count />
            </n-form-item>
          </n-form>
          <n-button type="primary" :loading="sendingBroadcast" :disabled="!canSend" @click="sendBroadcast">
            发送
          </n-button>
        </n-card>
      </n-tab-pane>
      <n-tab-pane name="users" tab="指定用户">
        <n-card title="按用户 ID 发送（逗号或换行分隔）" style="max-width: 560px">
          <n-form label-placement="left" label-width="88">
            <n-form-item label="用户 ID" required>
              <n-input
                v-model:value="usersForm.userIdsText"
                type="textarea"
                placeholder="例如：1001, 1002"
                :rows="3"
              />
            </n-form-item>
            <n-form-item label="标题" required>
              <n-input v-model:value="usersForm.title" maxlength="120" show-count />
            </n-form-item>
            <n-form-item label="内容" required>
              <n-input v-model:value="usersForm.content" type="textarea" :rows="5" maxlength="2000" show-count />
            </n-form-item>
          </n-form>
          <n-button type="primary" :loading="sendingUsers" :disabled="!canSend" @click="sendUsers">发送</n-button>
        </n-card>
      </n-tab-pane>
    </n-tabs>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { NButton, NCard, NForm, NFormItem, NInput, NTabPane, NTabs, useMessage } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { canWriteOps } from '../utils/adminPermissions'

const admin = useAdminStore()
const message = useMessage()
const canSend = computed(() => canWriteOps(admin.role))

const broadcastForm = reactive({ title: '', content: '' })
const usersForm = reactive({ userIdsText: '', title: '', content: '' })
const sendingBroadcast = ref(false)
const sendingUsers = ref(false)

function parseUserIds(text: string): number[] {
  return text
    .split(/[\s,，;；]+/)
    .map((s) => s.trim())
    .filter(Boolean)
    .map((s) => Number(s))
    .filter((n) => Number.isFinite(n) && n > 0)
}

async function sendBroadcast() {
  const title = broadcastForm.title.trim()
  const content = broadcastForm.content.trim()
  if (!title || !content) {
    message.warning('请填写标题和内容')
    return
  }
  sendingBroadcast.value = true
  try {
    const res = await adminApi.broadcastSystemNotification({ title, content })
    const sent = res.data.data?.sentCount ?? 0
    message.success(`已发送给 ${sent} 位用户`)
    broadcastForm.title = ''
    broadcastForm.content = ''
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '发送失败')
  } finally {
    sendingBroadcast.value = false
  }
}

async function sendUsers() {
  const ids = parseUserIds(usersForm.userIdsText)
  const title = usersForm.title.trim()
  const content = usersForm.content.trim()
  if (!ids.length) {
    message.warning('请填写有效的用户 ID')
    return
  }
  if (!title || !content) {
    message.warning('请填写标题和内容')
    return
  }
  sendingUsers.value = true
  try {
    const res = await adminApi.notifyUsers({ userIds: ids, title, content })
    const sent = res.data.data?.sentCount ?? 0
    message.success(`已发送给 ${sent} 位用户`)
    usersForm.userIdsText = ''
    usersForm.title = ''
    usersForm.content = ''
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '发送失败')
  } finally {
    sendingUsers.value = false
  }
}
</script>