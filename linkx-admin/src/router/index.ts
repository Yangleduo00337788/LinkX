import { createRouter, createWebHistory } from 'vue-router'
import { getAdminToken } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { canAccessRoute } from '../utils/adminPermissions'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      component: () => import('../layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue') },
        { path: 'users', name: 'users', component: () => import('../views/Users.vue') },
        { path: 'users/:id', name: 'user-detail', component: () => import('../views/UserDetail.vue') },
        { path: 'friend-requests', name: 'friend-requests', component: () => import('../views/FriendRequests.vue') },
        { path: 'groups', name: 'groups', component: () => import('../views/Groups.vue') },
        { path: 'group-requests', name: 'group-requests', component: () => import('../views/GroupRequests.vue') },
        { path: 'messages', name: 'messages', component: () => import('../views/Messages.vue') },
        { path: 'files', name: 'files', component: () => import('../views/Files.vue') },
        { path: 'blacklist', name: 'blacklist', component: () => import('../views/Blacklist.vue') },
        { path: 'releases', name: 'releases', component: () => import('../views/Releases.vue') },
        { path: 'audit-logs', name: 'audit-logs', component: () => import('../views/AuditLogs.vue') },
        { path: 'login-logs', name: 'login-logs', component: () => import('../views/LoginLogs.vue') },
        { path: 'reports', name: 'reports', component: () => import('../views/Reports.vue') },
        { path: 'sensitive-words', name: 'sensitive-words', component: () => import('../views/SensitiveWords.vue') },
        { path: 'system-notifications', name: 'system-notifications', component: () => import('../views/SystemNotifications.vue') },
        { path: 'file-hash-blacklist', name: 'file-hash-blacklist', component: () => import('../views/FileHashBlacklist.vue') },
        { path: 'system-settings', name: 'system-settings', component: () => import('../views/SystemSettings.vue') },
        { path: 'admins', name: 'admins', component: () => import('../views/Admins.vue') }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true
  if (!getAdminToken()) return { name: 'login', query: { redirect: to.fullPath } }
  const admin = useAdminStore()
  if (!admin.role) {
    try {
      await admin.fetchProfile()
    } catch {
      return { name: 'login' }
    }
  }
  const routeKey = String(to.name || '')
  if (routeKey && routeKey !== 'login' && !canAccessRoute(admin.role, routeKey)) {
    return { name: 'dashboard' }
  }
  return true
})

export default router