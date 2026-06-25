<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * ErrorToast 组件，负责当前界面片段的展示与交互。
 */
import { onMounted } from 'vue'  // 行注：引入 onMounted 能力
import { useMessage } from 'naive-ui'  // 行注：引入 useMessage 能力
import { setGlobalErrorHandler } from '../api/client'  // 行注：引入 setGlobalErrorHandler 能力

const message = useMessage()  // 行注：获取全局消息实例

onMounted(() => {  // 行注：注册组件挂载后的初始化逻辑
  setGlobalErrorHandler((msg) => {  // 行注：调用 setGlobalErrorHandler 方法
    message.error(msg)  // 行注：提示错误信息
  })  // 行注：结束当前调用配置
})  // 行注：结束当前调用配置
</script>
