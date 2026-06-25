import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import TicketList from '@/views/TicketList.vue'
import TicketCreate from '@/views/TicketCreate.vue'
import TicketDetail from '@/views/TicketDetail.vue'
import Analytics from '@/views/Analytics.vue'
import Permissions from '@/views/Permissions.vue'
import KnowledgeBase from '@/views/KnowledgeBase.vue'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/ticket-list', name: 'TicketList', component: TicketList },
  { path: '/ticket-create', name: 'TicketCreate', component: TicketCreate },
  { path: '/ticket-detail/:id?', name: 'TicketDetail', component: TicketDetail },
  { path: '/analytics', name: 'Analytics', component: Analytics },
  { path: '/permissions', name: 'Permissions', component: Permissions },
  { path: '/knowledge-base', name: 'KnowledgeBase', component: KnowledgeBase },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router