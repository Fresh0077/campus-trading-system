<template>
  <div class="admin-login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">
          <h1>🛡️ 管理后台</h1>
          <p>校园二手交易系统</p>
        </div>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">管理员账号</label>
          <input
            id="username"
            v-model="loginForm.username"
            type="text"
            placeholder="请输入管理员账号"
            required
            :disabled="isLoading"
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            required
            :disabled="isLoading"
          />
        </div>
        
        <button type="submit" class="login-btn" :disabled="isLoading">
          <span v-if="!isLoading">登录</span>
          <span v-else>登录中...</span>
        </button>
      </form>
      
      <div class="login-footer">
        <p>默认管理员账号：admin / admin123456</p>
        <p>管理员拥有所有系统权限</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../api/admin'
import { showToast } from '../services/toast'

const router = useRouter()

const loginForm = ref({
  username: '',
  password: ''
})

const isLoading = ref(false)

onMounted(() => {
  // 检查是否已经登录
  const token = localStorage.getItem('adminToken')
  if (token) {
    router.push('/admin/dashboard')
  }
})

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    showToast.error('请填写完整的登录信息')
    return
  }
  
  if (loginForm.value.password.length < 8) {
    showToast.error('密码长度不能少于8位')
    return
  }
  
  isLoading.value = true
  
  try {
    const response = await adminLogin(loginForm.value)
    console.log('管理员登录响应:', response)
    
    console.log('登录响应详情:', {
      success: response.success,
      data: response.data,
      message: response.message
    })
    
    // 检查登录是否成功
    if (response.success && response.data) {
      // 保存token和管理员信息
      const token = response.data.token
      console.log('保存的token:', token)
      localStorage.setItem('adminToken', token)
      localStorage.setItem('adminInfo', JSON.stringify(response.data.admin))
      localStorage.setItem('adminPermissions', JSON.stringify(response.data.permissions))
      
      // 验证token是否保存成功
      console.log('localStorage中的token:', localStorage.getItem('adminToken'))
      
      // 显示成功消息
      showToast.show({
        message: '登录成功',
        type: 'success',
        duration: 2000
      })
      
      // 跳转到管理后台首页
      setTimeout(() => {
        router.push('/admin/dashboard')
      }, 1000)
    } else {
      console.log('登录失败，响应:', response)
      showToast.error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('Login error:', error)
    showToast.error(error.message || '登录失败，请检查网络连接')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo h1 {
  color: #333;
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.logo p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #374151;
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.login-footer p {
  color: #666;
  font-size: 12px;
  margin: 4px 0;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .admin-login-container {
    padding: 10px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
  
  .logo h1 {
    font-size: 24px;
  }
}
</style>
