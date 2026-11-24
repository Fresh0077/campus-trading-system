<template>
  <div class="home-container">
    <div class="header">
      <h1>校园二手交易平台</h1>
      <div class="user-info">
        <span>欢迎，{{ userInfo?.realName || userInfo?.username || '用户' }}</span>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </div>
    
    <div class="content">
      <h2>欢迎来到校园二手交易平台</h2>
      <p>发现校园好物，分享闲置宝贝，让每一件物品都能找到它的新主人。</p>
      
      <!-- 左右布局容器 -->
      <div class="main-layout">
        <!-- 左侧：个人信息板块 -->
        <div class="user-details" v-if="userInfo">
          <div class="user-card-header">
            <div class="user-avatar">
              <img :src="userAvatarUrl" alt="用户头像" />
            </div>
            <div class="user-title">
              <h3>个人信息</h3>
              <p class="user-subtitle">{{ userInfo.realName || userInfo.username }}</p>
            </div>
            <button @click="openEditModal" class="edit-btn" title="编辑信息">
              ✏️
            </button>
          </div>
          
          <!-- 基本信息 -->
          <div class="user-info-list">
            <div class="info-item" style="animation-delay: 0.1s">
              <div class="info-icon">🎓</div>
              <div class="info-content">
                <span class="info-label">学号</span>
                <span class="info-value student-id">{{ userInfo.studentId }}</span>
              </div>
            </div>
            
            <div class="info-item" style="animation-delay: 0.2s">
              <div class="info-icon">👤</div>
              <div class="info-content">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ userInfo.username }}</span>
              </div>
            </div>
            
            <div class="info-item" style="animation-delay: 0.3s">
              <div class="info-icon">📱</div>
              <div class="info-content">
                <span class="info-label">手机号</span>
                <span class="info-value" :class="{ 'not-set': !userInfo.phone }">
                  {{ userInfo.phone || '未设置' }}
                </span>
              </div>
            </div>

            <div class="info-item" style="animation-delay: 0.4s">
              <div class="info-icon">📧</div>
              <div class="info-content">
                <span class="info-label">邮箱</span>
                <span class="info-value" :class="{ 'not-set': !userInfo.email }">
                  {{ userInfo.email || '未设置' }}
                </span>
              </div>
            </div>
            
            <div class="info-item" style="animation-delay: 0.5s">
              <div class="info-icon">⏰</div>
              <div class="info-content">
                <span class="info-label">注册时长</span>
                <span class="info-value">{{ registrationDuration }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：功能导航卡片 -->
        <div class="feature-cards">
          <router-link to="/products" class="feature-card">
            <div class="card-icon">🛍️</div>
            <h3>浏览商品</h3>
            <p>发现校园里的各种好物，找到你需要的宝贝</p>
          </router-link>
          
          <router-link to="/publish" class="feature-card">
            <div class="card-icon">📝</div>
            <h3>发布商品</h3>
            <p>将你的闲置物品分享给需要的同学</p>
          </router-link>
          
          <router-link to="/my-products" class="feature-card">
            <div class="card-icon">📦</div>
            <h3>我的商品</h3>
            <p>管理你发布的商品，查看交易状态</p>
          </router-link>
          
          <router-link to="/orders" class="feature-card">
            <div class="card-icon">📋</div>
            <h3>订单管理</h3>
            <p>查看和管理你的买入卖出订单</p>
          </router-link>
          
          <router-link to="/favorites" class="feature-card">
            <div class="card-icon">❤️</div>
            <h3>我的收藏</h3>
            <p>查看收藏的商品</p>
          </router-link>
          
          <!-- 交易统计卡片 -->
          <router-link to="/trade-statistics" class="feature-card">
            <div class="card-icon">📊</div>
            <h3>交易统计</h3>
            <p>查看交易数据分析和统计图表</p>
          </router-link>
        </div>
      </div>
    </div>
    
    <!-- 编辑信息模态框 -->
    <div v-if="showEditModal && editForm" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑个人信息</h3>
          <button @click="closeEditModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="updateProfile">
            <!-- 头像上传 -->
            <div class="form-group avatar-upload-group">
              <label>头像</label>
              <div class="avatar-upload-container">
                <div class="avatar-preview">
                  <img :key="avatarKey" :src="editAvatarUrl" alt="头像预览" @error="$event.target.src='/logo.jpg'" />
                </div>
                <div class="upload-controls">
                  <input 
                    type="file" 
                    ref="avatarInput" 
                    @change="handleAvatarChange" 
                    accept="image/*"
                    style="display: none"
                  />
                  <button type="button" @click="$refs.avatarInput.click()" class="upload-btn">
                    📷 选择图片
                  </button>
                  <p class="upload-hint">支持 JPG、PNG、GIF 格式，大小不超过 5MB</p>
                </div>
              </div>
            </div>
            
            <!-- 用户名 -->
            <div class="form-group">
              <label for="username">用户名</label>
              <input 
                type="text" 
                id="username"
                v-model="editForm.username" 
                placeholder="请输入用户名"
                minlength="2"
                maxlength="50"
              />
            </div>
            
            <!-- 真实姓名 -->
            <div class="form-group">
              <label for="realName">真实姓名</label>
              <input 
                type="text" 
                id="realName"
                v-model="editForm.realName" 
                placeholder="请输入真实姓名"
                maxlength="50"
              />
            </div>
            
            <!-- 手机号 -->
            <div class="form-group">
              <label for="phone">手机号</label>
              <input 
                type="tel" 
                id="phone"
                v-model="editForm.phone" 
                placeholder="请输入手机号"
                pattern="^1[3-9]\d{9}$"
              />
            </div>
            
            <!-- 邮箱 -->
            <div class="form-group">
              <label for="email">邮箱</label>
              <input 
                type="email" 
                id="email"
                v-model="editForm.email" 
                placeholder="请输入邮箱"
                maxlength="100"
              />
            </div>
            
            <!-- 学号（只读） -->
            <div class="form-group">
              <label for="studentId">学号</label>
              <input 
                type="text" 
                id="studentId"
                :value="userInfo?.studentId || ''" 
                readonly
                class="readonly-input"
              />
              <p class="field-hint">学号不可修改</p>
            </div>
            
            <!-- 注册时长（只读） -->
            <div class="form-group">
              <label for="registrationDuration">注册时长</label>
              <input 
                type="text" 
                id="registrationDuration"
                :value="registrationDuration || ''" 
                readonly
                class="readonly-input"
              />
              <p class="field-hint">注册时长不可修改</p>
            </div>
            
            <!-- 提交按钮 -->
            <div class="form-actions">
              <button type="button" @click="closeEditModal" class="cancel-btn">取消</button>
              <button type="submit" class="submit-btn" :disabled="isSubmitting">
                {{ isSubmitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, put } from '../api/request'
import toast from '../services/toast'
import { getApiBaseUrl } from '../config'
import { getImageUrl } from '../utils/imageUtils'

const router = useRouter()
const userInfo = ref(null)
const showTradeAmount = ref(false)
const registrationTime = ref(null)
const currentTime = ref(new Date())

// 编辑相关状态
const showEditModal = ref(false)
const isSubmitting = ref(false)
const editForm = ref(null)

// 交易统计数据（从API获取真实数据）
const tradeStats = ref({
  successCount: 0,
  totalAmount: 0
})

// 用户头像URL（处理相对路径和完整路径）
const userAvatarUrl = computed(() => {
  const avatar = userInfo.value?.avatar
  if (!avatar) return '/logo.jpg'

  // 兼容旧数据：如果 avatar 中不包含路径分隔符，仅是文件名，则视为 MinIO 中 avatars 目录下的文件
  let mappedPath = avatar
  if (!avatar.startsWith('http://') && !avatar.startsWith('https://') && !avatar.includes('/')) {
    mappedPath = `avatars/${avatar}`
  }

  const url = getImageUrl(mappedPath)
  console.log('头像路径:', avatar, '映射为:', mappedPath, '-> URL:', url)
  return url || '/logo.jpg'
})

// 编辑表单头像URL
const editAvatarUrl = computed(() => {
  const avatar = editForm.value?.avatar
  if (!avatar) return '/logo.jpg'

  let mappedPath = avatar
  if (!avatar.startsWith('http://') && !avatar.startsWith('https://') && !avatar.includes('/')) {
    mappedPath = `avatars/${avatar}`
  }

  return getImageUrl(mappedPath)
})

// 添加一个时间戳key，强制图片重新加载
const avatarKey = ref(Date.now())

// 计算注册时长（精确到秒）
const registrationDuration = computed(() => {
  if (!registrationTime.value) return '未知'
  
  const now = currentTime.value
  const regTime = new Date(registrationTime.value)
  const diffMs = now - regTime
  
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)
  const diffMonths = Math.floor(diffDays / 30)
  const diffYears = Math.floor(diffDays / 365)
  
  if (diffYears > 0) {
    const remainingMonths = Math.floor((diffDays % 365) / 30)
    const remainingDays = diffDays % 30
    return `${diffYears}年${remainingMonths}个月${remainingDays}天`
  } else if (diffMonths > 0) {
    const remainingDays = diffDays % 30
    const remainingHours = diffHours % 24
    return `${diffMonths}个月${remainingDays}天${remainingHours}小时`
  } else if (diffDays > 0) {
    const remainingHours = diffHours % 24
    const remainingMinutes = diffMinutes % 60
    return `${diffDays}天${remainingHours}小时${remainingMinutes}分钟`
  } else if (diffHours > 0) {
    const remainingMinutes = diffMinutes % 60
    const remainingSeconds = diffSeconds % 60
    return `${diffHours}小时${remainingMinutes}分钟${remainingSeconds}秒`
  } else if (diffMinutes > 0) {
    const remainingSeconds = diffSeconds % 60
    return `${diffMinutes}分钟${remainingSeconds}秒`
  } else if (diffSeconds > 0) {
    return `${diffSeconds}秒`
  } else {
    return '刚刚注册'
  }
})

let timeUpdateInterval = null

onMounted(async () => {
  // 检查是否有token
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  
  // 获取用户完整数据和统计信息
  await fetchUserData()
  
  // 每秒更新一次时间，实现精确到秒的显示
  timeUpdateInterval = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }
})

// 获取用户完整信息和统计数据
const fetchUserData = async () => {
  try {
    // 获取用户详细信息
    const profileResponse = await get('/auth/profile')
    if (profileResponse && profileResponse.code === 200) {
      userInfo.value = profileResponse.data
      // 设置注册时间
      if (userInfo.value.createTime) {
        registrationTime.value = userInfo.value.createTime
      }
      // 更新localStorage中的用户信息
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    // 获取用户统计数据
    const statsResponse = await get('/auth/stats')
    if (statsResponse && statsResponse.code === 200) {
      const stats = statsResponse.data
      tradeStats.value = {
        successCount: stats.successCount || 0,
        totalAmount: stats.totalAmount || 0
      }
    } else {
      // API调用失败时设置默认值
      tradeStats.value = {
        successCount: 0,
        totalAmount: 0
      }
    }
  } catch (error) {
    console.error('获取用户数据失败:', error)
    // 如果API调用失败，使用localStorage中的数据作为备用
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo && storedUserInfo !== 'undefined') {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
        if (userInfo.value.createTime) {
          registrationTime.value = userInfo.value.createTime
        }
      } catch (parseError) {
        console.error('解析用户信息失败:', parseError)
      }
    }
    
    // 设置默认统计数据
    tradeStats.value = {
      successCount: 0,
      totalAmount: 0
    }
  }
}

// 切换交易金额显示状态
const toggleTradeAmount = () => {
  showTradeAmount.value = !showTradeAmount.value
}

// 刷新统计数据
const refreshStats = async () => {
  try {
    const statsResponse = await get('/auth/stats')
    if (statsResponse && statsResponse.code === 200) {
      const stats = statsResponse.data
      tradeStats.value = {
        successCount: stats.successCount || 0,
        totalAmount: stats.totalAmount || 0
      }
    }
  } catch (error) {
    console.error('刷新统计数据失败:', error)
  }
}



const logout = () => {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  
  // 跳转到登录页
  router.push('/login')
}

// 打开编辑模态框
const openEditModal = () => {
  if (!userInfo.value) {
    toast.warning('用户信息加载中，请稍后再试')
    return
  }
  
  try {
    // 初始化表单数据
    editForm.value = {
      username: userInfo.value?.username || '',
      realName: userInfo.value?.realName || '',
      phone: userInfo.value?.phone || '',
      email: userInfo.value?.email || '',
      avatar: userInfo.value?.avatar || ''
    }
    // 重置avatarKey
    avatarKey.value = Date.now()
    showEditModal.value = true
  } catch (error) {
    console.error('打开编辑模态框失败:', error)
    toast.error('打开编辑窗口失败，请刷新页面重试')
  }
}

// 关闭编辑模态框
const closeEditModal = () => {
  showEditModal.value = false
  isSubmitting.value = false
}

// 处理头像文件选择
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  if (!editForm.value) {
    toast.error('表单数据错误')
    return
  }
  
  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    toast.error('请选择 JPG、PNG 或 GIF 格式的图片')
    return
  }
  
  // 验证文件大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    toast.error('图片大小不能超过 5MB')
    return
  }
  
  // 上传图片
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('category', 'avatars')
    
    const token = localStorage.getItem('token')
    const uploadUrl = `${getApiBaseUrl()}/files/upload/image`
    const response = await fetch(uploadUrl, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })
    
    const result = await response.json()
    
    if (result.code === 200 && result.data) {
      // 保存 MinIO 对象名称（filePath），而不是完整 URL
      // 数据库应该存储对象名称，如 "avatars/xxx.jpg"
      editForm.value.avatar = result.data.filePath || result.data.url
      console.log('上传成功，MinIO 对象名称:', result.data.filePath)
      console.log('完整 URL:', result.data.url)
      
      // 更新avatarKey强制图片重新渲染
      avatarKey.value = Date.now()
      
      toast.success('头像上传成功')
    } else {
      console.error('上传失败响应:', result)
      toast.error(result.message || '头像上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    toast.error('头像上传失败: ' + (error.message || '网络错误'))
  }
}

// 更新用户信息
const updateProfile = async () => {
  if (isSubmitting.value) return
  
  if (!editForm.value) {
    toast.error('表单数据错误')
    return
  }
  
  // 验证表单
  if (!editForm.value.username || editForm.value.username.trim().length < 2) {
    toast.error('用户名至少需要2个字符')
    return
  }
  
  if (editForm.value.phone && !/^1[3-9]\d{9}$/.test(editForm.value.phone)) {
    toast.error('手机号格式不正确')
    return
  }
  
  if (editForm.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.value.email)) {
    toast.error('邮箱格式不正确')
    return
  }
  
  isSubmitting.value = true
  
  try {
    const response = await put('/auth/profile', {
      username: editForm.value.username,
      realName: editForm.value.realName || null,
      phone: editForm.value.phone || null,
      email: editForm.value.email || null,
      avatar: editForm.value.avatar || null
    })
    
    if (response && response.code === 200) {
      toast.success('个人信息更新成功')
      
      // 更新本地用户信息
      userInfo.value = response.data
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      // 强制刷新头像（添加时间戳）
      avatarKey.value = Date.now()
      
      // 关闭模态框
      closeEditModal()
    } else {
      toast.error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    toast.error('更新失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  padding: 0;
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header h1 {
  color: #333;
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info span {
  color: #333;
  font-weight: 500;
  font-size: 16px;
}

.logout-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.content {
  padding: 60px 40px;
  max-width: 1300px;
  margin: 0 auto;
}

.content h2 {
  color: white;
  text-align: center;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.content p {
  color: rgba(255, 255, 255, 0.9);
  text-align: center;
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 40px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* 左右布局容器 */
.main-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.user-details {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s ease-out;
  flex: 0 0 300px;
  max-width: 300px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.user-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
  position: relative;
}

.edit-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.edit-btn:hover {
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.5);
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
  position: relative;
}

.user-avatar:hover {
  transform: scale(1.05);
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.user-avatar::before {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.user-avatar:hover::before {
  opacity: 0.2;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-title h3 {
  color: #333;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px 0;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-subtitle {
  color: #666;
  background:  rgba(102, 126, 234, 0.4);
  font-size: 14px;
  margin: 0;
  font-weight: 500;
}

.user-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  animation: slideInLeft 0.6s ease-out both;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
}

.info-icon {
  font-size: 18px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  color: #666;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

.info-value.not-set {
  color: #999;
  font-style: italic;
  font-weight: 400;
}

.student-id {
  font-family: 'Courier New', monospace;
  font-size: 15px;
  font-weight: 700;
  color: #374151;
  letter-spacing: 1px;
}


/* 功能导航卡片样式 */
.feature-cards {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  align-content: start;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px 16px;
  border-radius: 16px;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 1);
}

.card-icon {
  font-size: 40px;
  margin-bottom: 12px;
  display: block;
}

.feature-card h3 {
  color: #333;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.feature-card p {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
  margin: 0;
  text-shadow: none;
}

.message-card {
  position: relative;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .feature-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1024px) {
  .main-layout {
    flex-direction: column;
  }
  
  .user-details {
    flex: 1;
    max-width: 100%;
  }
  
  .user-info-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .header h1 {
    font-size: 24px;
  }
  
  .content {
    padding: 40px 20px;
  }
  
  .content h2 {
    font-size: 28px;
  }
  
  .content p {
    font-size: 16px;
  }
  
  .main-layout {
    gap: 24px;
  }
  
  .user-details {
    padding: 24px 20px;
  }
  
  .user-details h3 {
    font-size: 20px;
  }
  
  .feature-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 12px 16px;
  }
  
  .header h1 {
    font-size: 20px;
  }
  
  .user-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .content {
    padding: 32px 16px;
  }
  
  .content h2 {
    font-size: 24px;
  }
  
  .user-details {
    padding: 20px 16px;
  }
  
  .user-card-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
    margin-bottom: 20px;
    padding-bottom: 16px;
  }
  
  .user-avatar {
    width: 60px;
    height: 60px;
    border-width: 3px;
  }
  
  .user-title h3 {
    font-size: 22px;
  }
  
  .user-subtitle {
    font-size: 14px;
  }
  
  .user-info-list {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .info-item {
    padding: 14px;
    gap: 12px;
  }
  
  .info-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .info-label {
    font-size: 12px;
  }
  
  .info-value {
    font-size: 15px;
  }
  
  .student-id {
    font-size: 16px;
  }
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.modal-header h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.close-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(102, 126, 234, 0.1);
  color: #374151;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: rotate(90deg);
}

.modal-body {
  padding: 32px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #374151;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-group input.readonly-input {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
  font-style: italic;
}

/* 头像上传样式 */
.avatar-upload-group {
  margin-bottom: 32px;
}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-controls {
  flex: 1;
}

.upload-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.upload-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.upload-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  margin: 8px 0 0 0;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.cancel-btn {
  padding: 12px 32px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.submit-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 20px 24px;
  }
  
  .modal-body {
    padding: 24px;
  }
  
  .avatar-upload-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}
</style>