/**
 * 前端路由表与登录态前置守卫。
 */
import { createRouter, createWebHashHistory } from 'vue-router'  // 行注：引入 createRouter, createWebHashHistory 能力

const routes = [  // 行注：初始化 routes 变量
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/ForgotPassword.vue') },
  { path: '/reset-password', name: 'ResetPassword', component: () => import('../views/ResetPassword.vue') },
  { path: '/child/create-group', name: 'ChildCreateGroup', component: () => import('../views/child/CreateGroupChild.vue') },
  { path: '/child/add-friend', name: 'ChildAddFriend', component: () => import('../views/child/AddFriendChild.vue') },
  {
    path: '/child/add-group-members',
    name: 'ChildAddGroupMembers',
    component: () => import('../views/child/AddGroupMembersChild.vue')
  },
  {  // 行注：开始当前代码块
    path: '/',  // 行注：设置 path 配置项
    component: () => import('../views/Layout.vue'),  // 行注：传入 component 回调
    redirect: '/chat',  // 行注：设置 redirect 配置项
    children: [  // 行注：设置 children 配置项
      { path: 'chat', name: 'Chat', component: () => import('../views/Chat.vue') },  // 行注：执行当前调用逻辑
      { path: 'chat/:targetId', name: 'ChatRoom', component: () => import('../views/Chat.vue') },  // 行注：执行当前调用逻辑
      { path: 'groups/:groupId/members', name: 'GroupMembers', component: () => import('../views/GroupMembers.vue') },  // 行注：执行当前调用逻辑
      { path: 'friends', name: 'Friends', component: () => import('../views/Friends.vue') },  // 行注：执行当前调用逻辑
      { path: 'files', name: 'Files', component: () => import('../views/Files.vue') },  // 行注：执行当前调用逻辑
      { path: 'blacklist', name: 'Blacklist', component: () => import('../views/Blacklist.vue') },
      { path: 'settings', name: 'Settings', component: () => import('../views/Settings.vue') },
      { path: 'settings/devices', name: 'LoginDevices', component: () => import('../views/LoginDevices.vue') },
      { path: 'releases', redirect: { name: 'Settings' } },
      { path: 'reports', name: 'Reports', component: () => import('../views/Reports.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
    ]  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
]  // 行注：补充当前表达式

const router = createRouter({  // 行注：开始解构当前返回值
  history: createWebHashHistory(),  // 行注：设置 history 配置项
  routes  // 行注：传入 routes 参数
})  // 行注：结束当前调用配置

// 进入受保护页面前先检查本地 token，未登录统一跳转到登录页。
router.beforeEach((to, _from, next) => {  // 行注：调用 beforeEach 方法
  const token = localStorage.getItem('token')  // 行注：初始化 token 变量
  const publicNames = new Set(['Login', 'Register', 'ForgotPassword', 'ResetPassword'])
  if (!publicNames.has(String(to.name)) && !token) {
    next('/login')  // 行注：调用 next 方法
  } else {  // 行注：开始当前逻辑块
    next()  // 行注：调用 next 方法
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置

export default router  // 行注：导出默认组件或配置
