<template>
  <AdminPageShell
    hint="向所有「启用中」的客户端用户写入系统通知，用户在桌面/Web 侧栏「通知」中查看。"
  >
    <n-card title="发布系统通知" style="max-width: 560px">
      <n-form label-placement="left" label-width="72">
        <n-form-item label="标题" required>
          <n-input v-model:value="form.title" placeholder="例如：系统维护通知" maxlength="120" show-count />
        </n-form-item>
        <n-form-item label="内容" required>
          <n-input
            v-model:value="form.content"
            type="textarea"
            placeholder="通知正文"
            :rows="5"
            maxlength="2000"
            show-count
          />
        </n-form-item>
      </n-form>
      <n-button type="primary" :loading="sending" @click="send">向全体用户发送</n-button>
    </n-card>
    <n-card title="指定用户发送" style="max-width: 560px; margin-top: 16px">
      <n-form label-placement="left" label-width="72">
        <n-form-item label="用户 ID" required>
          <n-input
            v-model:value="targetUserIdsText"
            type="textarea"
            placeholder="多个 ID 用英文逗号或换行分隔"
            :rows="3"
          />
        </n-form-item>
        <n-form-item label="标题" required>
          <n-input v-model:value="targetForm.title" maxlength="120" show-count />
        </n-form-item>
        <n-form-item label="内容" required>
          <n-input v-model:value="targetForm.content" type="textarea" :rows="4" maxlength="2000" show-count />
        </n-form-item>
      </n-form>
      <n-button type="primary" :loading="targetSending" @click="sendTarget">发送给指定用户</n-button>
    </n-card>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { NButton, NCard, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

const message = useMessage()
const sending = ref(false)
const targetSending = ref(false)
const form = reactive({ title: '', content: '' })
const targetForm = reactive({ title: '', content: '' })
const targetUserIdsText = ref('')

async function send() {
  const title = form.title.trim()
  const content = form.content.trim()
  if (!title || !content) {
    message.warning('请填写标题和内容')
    return
  }
  sending.value = true
  try {
    const res = await adminApi.broadcastSystemNotification({ title, content })
    const payload = (res.data as { data?: { sentCount?: number } }).data ?? res.data
    const sent = payload?.sentCount ?? 0
    message.success(`已发送给 ${sent} 位用户`)
    form.title = ''
    form.content = ''
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '发送失败')
  } finally {
    sending.value = false
  }
}

function parseUserIds(text: string): number[] {
  return text
    .split(/[\s,，]+/)
    .map((s) => s.trim())
    .filter(Boolean)
    .map((s) => Number(s))
    .filter((n) => Number.isFinite(n) && n > 0)
}

async function sendTarget() {
  const ids = parseUserIds(targetUserIdsText.value)
  const title = targetForm.title.trim()
  const content = targetForm.content.trim()
  if (!ids.length) {
    message.warning('请填写至少一个有效用户 ID')
    return
  }
  if (!title || !content) {
    message.warning('请填写标题和内容')
    return
  }
  targetSending.value = true
  try {
    const res = await adminApi.targetSystemNotification({ userIds: ids, title, content })
    const payload = (res.data as { data?: { sentCount?: number } }).data ?? res.data
    message.success(`已发送给 ${payload?.sentCount ?? ids.length} 位用户`)
    targetUserIdsText.value = ''
    targetForm.title = ''
    targetForm.content = ''
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '发送失败')
  } finally {
    targetSending.value = false
  }
}
</script>