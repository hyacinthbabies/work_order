<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="icon">
          <svg viewBox="0 0 24 24"><path d="M20 6h-4V4c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zM10 4h4v2h-4V4zm6 11h-3v3h-2v-3H8v-2h3v-3h2v3h3v2z"/></svg>
        </div>
        <span>智能工单</span>
      </div>
      <nav>
        <div class="nav-group">
          <div class="nav-group-title">工作台</div>
          <div class="nav-item" :class="{ active: currentRoute === 'Dashboard' }" @click="navigate('Dashboard')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
            工作台概览
          </div>
          <div class="nav-item" :class="{ active: currentRoute === 'TicketList' }" @click="navigate('TicketList')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>
            工单列表
            <span class="badge">23</span>
          </div>
          <div class="nav-item" :class="{ active: currentRoute === 'TicketCreate' }" @click="navigate('TicketCreate')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
            创建工单
          </div>
        </div>
        <div class="nav-group">
          <div class="nav-group-title">智能中心</div>
          <div class="nav-item" :class="{ active: currentRoute === 'TicketDetail' }" @click="navigate('TicketDetail')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/></svg>
            工单处理（含AI助手）
          </div>
          <div class="nav-item" :class="{ active: currentRoute === 'Analytics' }" @click="navigate('Analytics')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>
            数据分析大屏
          </div>
          <div v-if="user.role === '系统管理员'" class="nav-item" :class="{ active: currentRoute === 'KnowledgeBase' }" @click="navigate('KnowledgeBase')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/></svg>
            AI知识库管理
          </div>
        </div>
        <div class="nav-group">
          <div class="nav-group-title">系统管理</div>
          <div class="nav-item" :class="{ active: currentRoute === 'Permissions' }" @click="navigate('Permissions')">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg>
            权限与脱敏管理
          </div>
        </div>
      </nav>
    </aside>
    <div class="main-area">
      <header class="top-header">
        <div class="breadcrumb">
          <span>智能工单</span>
          <span>/</span>
          <span class="current">{{ breadcrumbName }}</span>
        </div>
        <div class="user-info">
          <div style="text-align: right">
            <div class="name">{{ user.name }}</div>
            <div class="role">{{ user.department }} · {{ user.role }}</div>
          </div>
          <div class="avatar">{{ user.name.charAt(0) }}</div>
        </div>
      </header>
      <div class="content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : { name: '张三', role: '一线客服', department: '客服中心' }
})

const currentRoute = computed(() => route.name)

const breadcrumbNames = {
  Dashboard: '工作台概览',
  TicketList: '工单列表',
  TicketCreate: '创建工单',
  TicketDetail: '工单处理（含AI助手）',
  Analytics: '数据分析大屏',
  Permissions: '权限与脱敏管理',
  KnowledgeBase: 'AI知识库管理'
}

const breadcrumbName = computed(() => breadcrumbNames[currentRoute.value] || '')

const navigate = (name) => {
  router.push({ name })
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 220px;
  background: #111827;
  color: #fff;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.sidebar .brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,.1);
}

.sidebar .brand .icon {
  width: 32px;
  height: 32px;
  background: #1a56db;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar .brand .icon svg {
  width: 18px;
  height: 18px;
  fill: #fff;
}

.sidebar .brand span {
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.sidebar nav {
  flex: 1;
  padding: 12px 0;
  overflow-y: auto;
}

.sidebar nav .nav-group {
  margin-bottom: 4px;
}

.sidebar nav .nav-group-title {
  padding: 8px 20px;
  font-size: 11px;
  color: rgba(255,255,255,.4);
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

.sidebar nav .nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  color: rgba(255,255,255,.7);
  cursor: pointer;
  transition: all .15s;
  font-size: 13px;
  border-left: 3px solid transparent;
}

.sidebar nav .nav-item:hover {
  background: rgba(255,255,255,.08);
  color: #fff;
}

.sidebar nav .nav-item.active {
  background: rgba(26,86,219,.3);
  color: #fff;
  border-left-color: #1a56db;
}

.sidebar nav .nav-item svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  opacity: .7;
}

.sidebar nav .nav-item.active svg {
  opacity: 1;
}

.sidebar nav .nav-item .badge {
  margin-left: auto;
  background: #dc2626;
  color: #fff;
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 600;
}

.main-area {
  margin-left: 220px;
  flex: 1;
  min-height: 100vh;
}

.top-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.top-header .breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.top-header .breadcrumb .current {
  color: #1f2937;
  font-weight: 600;
}

.top-header .user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.top-header .user-info .avatar {
  width: 32px;
  height: 32px;
  background: #e8edfb;
  color: #1a56db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
}

.top-header .user-info .name {
  font-size: 13px;
  font-weight: 600;
}

.top-header .user-info .role {
  font-size: 11px;
  color: #9ca3af;
}

.content {
  padding: 24px;
}
</style>