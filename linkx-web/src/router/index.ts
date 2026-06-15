import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/chat',
    children: [
      { path: 'chat', name: 'Chat', component: () => import('../views/Chat.vue') },
      { path: 'chat/:targetId', name: 'ChatRoom', component: () => import('../views/Chat.vue') },
      { path: 'friends', name: 'Friends', component: () => import('../views/Friends.vue') },
      { path: 'files', name: 'Files', component: () => import('../views/Files.vue') },
      { path: 'blacklist', name: 'Blacklist', component: () => import('../views/Blacklist.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && to.name !== 'Register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
