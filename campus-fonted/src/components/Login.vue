<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <div class="brand-content">
        <!-- 大学Logo -->
        <div class="university-logo">
          <img src="/university-logo.jpg" alt="四川大学" />
        </div>
        
        <!-- 平台标题 -->
        <div class="platform-header">
          <h1 class="platform-title">校园二手交易平台</h1>
          <p class="platform-subtitle">Campus Second-hand Trading Platform</p>
        </div>
        
        <!-- 核心优势 -->
        <div class="advantages-section">
          <div class="advantage-item">
            <div class="advantage-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                <path d="m9 12 2 2 4-4"></path>
              </svg>
            </div>
            <div class="advantage-content">
              <h4>实名认证体系</h4>
              <p>基于学号的身份验证机制</p>
            </div>
          </div>
          
          <div class="advantage-item">
            <div class="advantage-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </div>
            <div class="advantage-content">
              <h4>数据安全保障</h4>
              <p>端到端加密，隐私信息多重防护</p>
            </div>
          </div>
          
          <div class="advantage-item">
            <div class="advantage-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path>
              </svg>
            </div>
            <div class="advantage-content">
              <h4>高效发布流程</h4>
              <p>30秒快速上架，智能推荐算法</p>
            </div>
          </div>
          
        </div>
        
        <!-- 品牌特色标签 -->
        <div class="brand-tags">
          <span class="tag">校园专属</span>
          <span class="tag">安全可靠</span>
          <span class="tag">绿色环保</span>
          <span class="tag">诚信交易</span>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录表单区 -->
    <div class="form-section">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户，开始交易</p>
        </div>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="studentId">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              学号
            </label>
            <input
              type="text"
              id="studentId"
              v-model="form.studentId"
              placeholder="请输入学号"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="password">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              密码
            </label>
            <input
              type="password"
              id="password"
              v-model="form.password"
              placeholder="请输入密码"
              required
            />
          </div>
          
          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="loading">
              <svg class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
              </svg>
              登录中...
            </span>
            <span v-else>登录</span>
          </button>
        </form>
        
        <div class="form-footer">
          <p>还没有账号？<router-link to="/register" class="link">立即注册</router-link></p>
          <div class="admin-login-link">
            <router-link to="/admin/login" class="admin-link">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
                <path d="M2 17l10 5 10-5"></path>
                <path d="M2 12l10 5 10-5"></path>
              </svg>
              管理员登录
            </router-link>
          </div>
        </div>
        
        <div v-if="errorMessage" class="error-message">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
          {{ errorMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'

const router = useRouter()

const form = ref({
  studentId: '',
  password: ''
})

const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!form.value.studentId || !form.value.password) {
    errorMessage.value = '请填写完整的登录信息'
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    const response = await login(form.value)
    console.log('登录响应:', response)
    
    if (response.code === 200) {
      const data = response.data || {}
      
      // 保存token
      if (data.token) {
        localStorage.setItem('token', data.token)
      }
      
      // 标准化并保存用户信息（后端返回的是UserLoginResponse，而不是userInfo）
      const normalizedUser = {
        id: data.userId,
        userId: data.userId,
        studentId: data.studentId,
        username: data.username,
        realName: data.realName,
        phone: data.phone,
        email: data.email,
        avatar: data.avatar,
        loginTime: data.loginTime
      }
      localStorage.setItem('userInfo', JSON.stringify(normalizedUser))
      
      if (data.userId) {
        localStorage.setItem('userId', String(data.userId))
      }
      
      // 确保WebSocket连接使用新token（不阻塞跳转）
      try {
        if (window.notificationService && typeof window.notificationService.reconnect === 'function') {
          window.notificationService.reconnect()
        }
      } catch (wsErr) {
        console.warn('WebSocket重连失败:', wsErr)
      }
      
      console.log('准备跳转到首页...')
      router.push('/home')
        .then(() => console.log('跳转成功'))
        .catch(err => console.error('跳转失败:', err))
    } else {
      errorMessage.value = response.message || '登录失败'
    }
  } catch (error) {
    console.error('登录流程异常:', error)
    errorMessage.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f7fafc;
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 50px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  top: -200px;
  left: -200px;
}

.brand-section::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  bottom: -150px;
  right: -150px;
}

.brand-content {
  max-width: 580px;
  width: 100%;
  color: white;
  position: relative;
  z-index: 1;
}

/* 大学Logo */
.university-logo {
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
  background: white;
  border-radius: 50%;
  padding: 12px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.university-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 平台标题 */
.platform-header {
  text-align: center;
  margin-bottom: 35px;
}

.platform-title {
  font-size: 34px;
  font-weight: 700;
  margin: 0 0 10px 0;
  line-height: 1.2;
  letter-spacing: 1px;
}

.platform-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.5px;
}

/* 核心优势列表 */
.advantages-section {
  margin-bottom: 30px;
}

.advantage-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-left: 3px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  margin-bottom: 14px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.advantage-item:hover {
  background: rgba(255, 255, 255, 0.15);
  border-left-color: rgba(255, 255, 255, 0.8);
  transform: translateX(5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.advantage-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.advantage-item:hover .advantage-icon {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.advantage-icon svg {
  color: white;
}

.advantage-content {
  flex: 1;
}

.advantage-content h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px 0;
  color: white;
  line-height: 1.2;
}

.advantage-content p {
  font-size: 13px;
  margin: 0;
  opacity: 0.85;
  line-height: 1.4;
  color: white;
}

/* 品牌标签 */
.brand-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  padding: 20px 0 0;
}

.brand-tags .tag {
  display: inline-block;
  padding: 8px 18px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.brand-tags .tag:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 右侧表单区 */
.form-section {
  flex: 0 0 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  overflow-y: auto;
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 12px 0;
}

.form-header p {
  font-size: 16px;
  color: #718096;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: #2d3748;
  font-weight: 600;
  font-size: 14px;
}

.form-group label svg {
  color: #374151;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 16px;
  transition: all 0.3s ease;
  background-color: #f7fafc;
}

.form-group input:focus {
  outline: none;
  border-color: #374151;
  background-color: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.login-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.form-footer p {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.form-footer .link {
  color: #374151;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.form-footer .link:hover {
  color: #6b7280;
  text-decoration: underline;
}

.admin-login-link {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
  text-align: center;
}

.admin-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  text-decoration: none;
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
}

.admin-link:hover {
  color: #374151;
  background: #edf2f7;
  border-color: #374151;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.admin-link svg {
  flex-shrink: 0;
}

.error-message {
  margin-top: 16px;
  padding: 14px 16px;
  background-color: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
  border-radius: 10px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-message svg {
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
  }
  
  .brand-section {
    min-height: 40vh;
    padding: 40px 20px;
  }
  
  .brand-title {
    font-size: 32px;
  }
  
  .platform-subtitle {
    font-size: 14px;
  }
  
  .advantages-section {
    margin-bottom: 20px;
  }
  
  .advantage-item {
    padding: 12px 16px;
    margin-bottom: 10px;
  }
  
  .advantage-content h4 {
    font-size: 14px;
  }
  
  .advantage-content p {
    font-size: 12px;
  }
  
  .brand-tags {
    padding: 15px 0 0;
  }
  
  .brand-tags .tag {
    font-size: 12px;
    padding: 6px 14px;
  }
  
  .form-section {
    flex: 1;
    min-height: 60vh;
  }
}

@media (max-width: 480px) {
  .brand-section {
    padding: 30px 20px;
  }
  
  .university-logo {
    width: 90px;
    height: 90px;
    margin-bottom: 20px;
    padding: 10px;
  }
  
  .platform-title {
    font-size: 20px;
  }
  
  .platform-subtitle {
    font-size: 11px;
  }
  
  .platform-header {
    margin-bottom: 25px;
  }
  
  .advantage-item {
    padding: 10px 14px;
    margin-bottom: 8px;
  }
  
  .advantage-icon {
    width: 36px;
    height: 36px;
  }
  
  .advantage-icon svg {
    width: 18px;
    height: 18px;
  }
  
  .advantage-content h4 {
    font-size: 13px;
  }
  
  .advantage-content p {
    font-size: 11px;
  }
  
  .brand-tags .tag {
    font-size: 11px;
    padding: 5px 12px;
  }
  
  .form-section {
    padding: 20px;
  }
  
  .form-header h2 {
    font-size: 24px;
  }
  
  .form-header p {
    font-size: 14px;
  }
}
</style>