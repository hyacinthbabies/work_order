<template>
  <div class="login-page">
    <div class="login-box">
      <div class="logo">
        <div class="icon">
          <svg viewBox="0 0 24 24"><path d="M20 6h-4V4c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zM10 4h4v2h-4V4zm6 11h-3v3h-2v-3H8v-2h3v-3h2v3h3v2z"/></svg>
        </div>
        <h1>全域智能工单系统</h1>
        <p>银行全域智能工单管理平台 V1.0</p>
      </div>
      <el-form :model="loginForm" ref="loginFormRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入工号或用户名" size="large"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="loading">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="sso-hint">— 或使用统一认证平台登录 —</div>
      <el-button style="width:100%" @click="handleSSOLogin" icon="UserFilled">SSO 统一身份认证登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/ticket'

const router = useRouter()
const loading = ref(false)
const loginFormRef = ref(null)
const loginForm = reactive({
  username: 'zhangsan01',
  password: '123456'
})

const handleLogin = async () => {
  if (!loginFormRef.value.validate()) return
  loading.value = true
  try {
    const res = await login(loginForm)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    router.push('/dashboard')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSSOLogin = () => {
  localStorage.setItem('token', 'sso-token-12345')
  localStorage.setItem('user', JSON.stringify({ username: 'zhangsan01', name: '张三', role: '一线客服', department: '客服中心' }))
  router.push('/dashboard')
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3a8a 0%, #1a56db 50%, #3b82f6 100%);
}

.login-box {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,.3);
}

.logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo .icon {
  width: 56px;
  height: 56px;
  background: #1a56db;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.logo .icon svg {
  width: 32px;
  height: 32px;
  fill: #fff;
}

.logo h1 {
  font-size: 20px;
  color: #1f2937;
  margin-top: 12px;
  font-weight: 700;
}

.logo p {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.sso-hint {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: #9ca3af;
}
</style>