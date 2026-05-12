import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/BaseLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', roles: ['super_admin'] }
      },
      {
        path: 'scripts',
        name: 'Scripts',
        component: () => import('../views/script/ScriptList.vue'),
        meta: { title: '脚本管理' }
      },
      {
        path: 'scripts/view/:id',
        name: 'ScriptView',
        component: () => import('../views/script/ScriptView.vue'),
        meta: { title: '查看脚本', hidden: true }
      },
      {
        path: 'scripts/edit/:id',
        name: 'ScriptEdit',
        component: () => import('../views/script/ScriptEdit.vue'),
        meta: { title: '编辑脚本', hidden: true, roles: ['super_admin', 'admin'] }
      },
      {
        path: 'scripts/new',
        name: 'ScriptNew',
        component: () => import('../views/script/ScriptEdit.vue'),
        meta: { title: '新增脚本', hidden: true, roles: ['super_admin', 'admin'] }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: () => import('../views/audit/AuditList.vue'),
        meta: { title: '审计日志' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') return next()
  if (!userStore.isLogin) return next('/login')
  const needRoles = to.meta?.roles
  if (needRoles && !needRoles.includes(userStore.role)) return next('/dashboard')
  next()
})

export default router
