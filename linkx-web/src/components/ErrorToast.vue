<script setup lang="ts">
import { onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { setGlobalErrorHandler } from '../api/client'

const message = useMessage()

onMounted(() => {
  setGlobalErrorHandler((msg) => {
    message.error(msg)
  })
})
</script>
