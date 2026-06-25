/**
 * 创建 Vue 应用并挂载路由、状态管理和根组件。
 */
import { createApp } from 'vue'  // 行注：引入 createApp 能力
import { createPinia } from 'pinia'  // 行注：引入 createPinia 能力
import router from './router'  // 行注：引入 router 模块
import App from './App.vue'  // 行注：引入 App 组件
import './styles/wechat.css'
import './styles/auth.css'
import './styles/linkx-skin.css'

const app = createApp(App)  // 行注：创建应用实例
app.use(createPinia())  // 行注：为应用实例注册插件
app.use(router)  // 行注：为应用实例注册插件
app.mount('#app')  // 行注：将应用挂载到页面根节点
