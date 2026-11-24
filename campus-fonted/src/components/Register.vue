<template>
  <div class="register-container">
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
    
    <!-- 右侧注册表单区 -->
    <div class="form-section">
      <div class="form-wrapper">
        <button @click="goToLogin" class="back-to-login-btn" title="返回登录">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <div class="form-header">
          <h2>开始使用</h2>
          <p>创建您的账户，开启交易之旅</p>
        </div>
        
        <form @submit.prevent="handleRegister" class="register-form">
          <div class="form-row">
            <div class="form-group">
              <label for="studentId">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
                学号 *
              </label>
              <input
                type="text"
                id="studentId"
                v-model="form.studentId"
                placeholder="8-12位数字"
                required
              />
            </div>
            
            <div class="form-group">
              <label for="username">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"></path>
                </svg>
                用户名 *
              </label>
              <input
                type="text"
                id="username"
                v-model="form.username"
                placeholder="请输入用户名"
                required
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="realName">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                <polyline points="17 21 17 13 7 13 7 21"></polyline>
                <polyline points="7 3 7 8 15 8"></polyline>
              </svg>
              真实姓名 *
            </label>
            <input
              type="text"
              id="realName"
              v-model="form.realName"
              placeholder="请输入真实姓名"
              required
            />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="password">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                密码 *
              </label>
              <input
                type="password"
                id="password"
                v-model="form.password"
                placeholder="至少6位，需含字母和数字"
                required
              />
            </div>
            
            <div class="form-group">
              <label for="confirmPassword">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                确认密码 *
              </label>
              <input
                type="password"
                id="confirmPassword"
                v-model="form.confirmPassword"
                placeholder="再次输入密码"
                required
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="phone">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                </svg>
                手机号
              </label>
              <input
                type="tel"
                id="phone"
                v-model="form.phone"
                placeholder="选填"
              />
            </div>
            
            <div class="form-group">
              <label for="email">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                邮箱
              </label>
              <input
                type="email"
                id="email"
                v-model="form.email"
                placeholder="选填"
              />
            </div>
          </div>
          
          <button type="submit" class="register-btn" :disabled="loading">
            <span v-if="loading">
              <svg class="spinner" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12a9 9 0 1 1-6.219-8.56"></path>
              </svg>
              注册中...
            </span>
            <span v-else>立即注册</span>
          </button>
        </form>
        
        <div class="form-footer">
          <p>已有账号？<router-link to="/login" class="link">立即登录</router-link></p>
        </div>
        
        <div v-if="errorMessage" class="error-message">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
          {{ errorMessage }}
        </div>
        
        <div v-if="successMessage" class="success-message">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          {{ successMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'

const router = useRouter()

// 返回登录页
const goToLogin = () => {
  router.push('/login')
}

const form = ref({
  studentId: '',
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleRegister = async () => {
  // 清理所有输入数据的前后空格
  form.value.studentId = form.value.studentId?.trim() || ''
  form.value.username = form.value.username?.trim() || ''
  form.value.realName = form.value.realName?.trim() || ''
  form.value.password = form.value.password?.trim() || ''
  form.value.confirmPassword = form.value.confirmPassword?.trim() || ''
  form.value.phone = form.value.phone?.trim() || ''
  form.value.email = form.value.email?.trim() || ''
  
  // 表单验证
  if (!form.value.studentId || !form.value.username || !form.value.realName || !form.value.password) {
    errorMessage.value = '请填写完整的必填信息'
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  
  if (form.value.password.length < 6) {
    errorMessage.value = '密码长度至少6位'
    return
  }
  
  // 密码复杂度验证（必须包含字母和数字）
  if (!/^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/.test(form.value.password)) {
    errorMessage.value = '密码必须包含至少一个字母和一个数字'
    return
  }
  
  // 学号格式验证
  if (!/^\d{8,12}$/.test(form.value.studentId)) {
    console.log('学号验证失败:', form.value.studentId, '长度:', form.value.studentId.length)
    errorMessage.value = `学号必须是8-12位数字（当前${form.value.studentId.length}位）`
    return
  }
  
  // 手机号格式验证（如果填写了）
  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    errorMessage.value = '请输入正确的手机号格式'
    return
  }
  
  // 邮箱格式验证（如果填写了）
  if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errorMessage.value = '请输入正确的邮箱格式'
    return
  }
  
  loading.value = true
  errorMessage.value = ''
  
  try {
    // 构建注册数据（不包含confirmPassword）
    const registerData = {
      studentId: form.value.studentId,
      username: form.value.username,
      realName: form.value.realName,
      password: form.value.password,
      phone: form.value.phone || null,
      email: form.value.email || null
    }
    
    console.log('发送注册请求:', registerData)
    const response = await register(registerData)
    console.log('注册响应:', response)
    
    if (response.code === 200) {
      // 注册成功，显示成功消息并跳转到登录页
      successMessage.value = '注册成功！即将跳转到登录页面...'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      // 显示后端返回的具体错误信息
      errorMessage.value = response.message || '注册失败，请稍后重试'
    }
  } catch (error) {
    console.error('注册请求异常:', error)
    errorMessage.value = '网络错误，请检查网络连接后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
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
  flex: 0 0 550px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  overflow-y: auto;
}

.form-wrapper {
  width: 100%;
  max-width: 480px;
  position: relative;
}

.back-to-login-btn {
  position: absolute;
  top: 0;
  left: 0;
  background: rgba(103, 126, 234, 0.1);
  border: 1px solid rgba(103, 126, 234, 0.2);
  color: #374151;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
}

.back-to-login-btn:hover {
  background: #374151;
  color: white;
  transform: translateX(-2px);
  box-shadow: 0 4px 12px rgba(103, 126, 234, 0.3);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
  margin-top: 12px;
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

.register-form {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  color: #2d3748;
  font-weight: 600;
  font-size: 13px;
}

.form-group label svg {
  color: #374151;
  flex-shrink: 0;
}

.form-group input {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  background-color: #f7fafc;
}

.form-group input:focus {
  outline: none;
  border-color: #374151;
  background-color: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.register-btn {
  width: 100%;
  padding: 14px;
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.register-btn:disabled {
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
  margin-top: 20px;
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

.error-message,
.success-message {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.error-message {
  background-color: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.success-message {
  background-color: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.error-message svg,
.success-message svg {
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .register-container {
    flex-direction: column;
  }
  
  .brand-section {
    min-height: 35vh;
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
    min-height: 65vh;
  }
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  .form-section {
    flex: 0 0 auto;
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
  
  .form-group {
    margin-bottom: 16px;
  }
}
</style>
