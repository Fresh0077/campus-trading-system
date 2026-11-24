<template>
  <div class="admin-dashboard">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <h3 v-if="!sidebarCollapsed">🛡️ 管理后台</h3>
          <h3 v-else>🛡️</h3>
        </div>
        <button @click="toggleSidebar" class="collapse-btn">
          {{ sidebarCollapsed ? '→' : '←' }}
        </button>
      </div>
      
      <nav class="sidebar-nav">
        <div class="nav-item" :class="{ active: activeTab === 'dashboard' }" @click="setActiveTab('dashboard')">
          <span class="nav-icon">📊</span>
          <span v-if="!sidebarCollapsed" class="nav-text">仪表盘</span>
        </div>
        <div class="nav-item" :class="{ active: activeTab === 'users' }" @click="setActiveTab('users')">
          <span class="nav-icon">👥</span>
          <span v-if="!sidebarCollapsed" class="nav-text">用户管理</span>
        </div>
        <div class="nav-item" :class="{ active: activeTab === 'products' }" @click="setActiveTab('products')">
          <span class="nav-icon">📦</span>
          <span v-if="!sidebarCollapsed" class="nav-text">商品管理</span>
        </div>
        <div class="nav-item" :class="{ active: activeTab === 'orders' }" @click="setActiveTab('orders')">
          <span class="nav-icon">📋</span>
          <span v-if="!sidebarCollapsed" class="nav-text">订单管理</span>
        </div>
        <div class="nav-item" :class="{ active: activeTab === 'system' }" @click="setActiveTab('system')">
          <span class="nav-icon">⚙️</span>
          <span v-if="!sidebarCollapsed" class="nav-text">系统管理</span>
        </div>
      </nav>
    </div>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部栏 -->
      <header class="top-header">
        <div class="header-left">
          <h2>{{ getTabTitle() }}</h2>
        </div>
        <div class="header-right">
          <div class="admin-info">
            <span class="admin-name">{{ adminInfo?.realName || adminInfo?.username }}</span>
            <div class="admin-avatar">{{ getAvatarText() }}</div>
            <div class="admin-dropdown">
              <button @click="showAdminMenu = !showAdminMenu" class="dropdown-btn">▼</button>
              <div v-if="showAdminMenu" class="dropdown-menu">
                <div class="dropdown-item" @click="viewProfile">个人信息</div>
                <div class="dropdown-item" @click="logout">退出登录</div>
              </div>
            </div>
          </div>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 仪表盘 -->
        <div v-if="activeTab === 'dashboard'" class="tab-content">
          <AdminDashboardStats />
        </div>
        
        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="tab-content">
          <AdminUserManagement />
        </div>
        
        <!-- 商品管理 -->
        <div v-if="activeTab === 'products'" class="tab-content">
          <AdminProductManagement />
        </div>
        
        <!-- 订单管理 -->
        <div v-if="activeTab === 'orders'" class="tab-content">
          <AdminOrderManagement />
        </div>

        <!-- 系统管理 -->
        <div v-if="activeTab === 'system'" class="tab-content">
          <AdminSystemManagement />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from '../services/toast'
import AdminDashboardStats from './admin/AdminDashboardStats.vue'
import AdminUserManagement from './admin/AdminUserManagement.vue'
import AdminProductManagement from './admin/AdminProductManagement.vue'
import AdminOrderManagement from './admin/AdminOrderManagement.vue'
import AdminSystemManagement from './admin/AdminSystemManagement.vue'

const router = useRouter()

const activeTab = ref('dashboard')
const sidebarCollapsed = ref(false)
const showAdminMenu = ref(false)
const adminInfo = ref(null)

onMounted(() => {
  // 检查登录状态
  const token = localStorage.getItem('adminToken')
  if (!token) {
    router.push('/admin/login')
    return
  }
  
  // 获取管理员信息
  const adminInfoStr = localStorage.getItem('adminInfo')
  if (adminInfoStr) {
    adminInfo.value = JSON.parse(adminInfoStr)
  }
  
  // 监听点击事件，关闭下拉菜单
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const setActiveTab = (tab) => {
  activeTab.value = tab
  showAdminMenu.value = false
}

const getTabTitle = () => {
  const titles = {
    dashboard: '仪表盘',
    users: '用户管理',
    products: '商品管理',
    orders: '订单管理',
    system: '系统管理'
  }
  return titles[activeTab.value] || '管理后台'
}

const getAvatarText = () => {
  if (adminInfo.value?.realName) {
    return adminInfo.value.realName.charAt(0).toUpperCase()
  }
  if (adminInfo.value?.username) {
    return adminInfo.value.username.charAt(0).toUpperCase()
  }
  return 'A'
}

const handleClickOutside = (event) => {
  if (!event || !event.target) return
  if (!event.target.closest('.admin-dropdown')) {
    showAdminMenu.value = false
  }
}

const viewProfile = () => {
  showAdminMenu.value = false
  showToast.show({
    message: '个人信息功能开发中',
    type: 'info'
  })
}

const logout = () => {
  showAdminMenu.value = false
  
  // 清除本地存储
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  localStorage.removeItem('adminPermissions')
  
  showToast.show({
    message: '已退出登录',
    type: 'success'
  })
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 侧边栏样式 */
.sidebar {
  width: 250px;
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  color: white;
  transition: width 0.3s ease;
  position: relative;
  z-index: 100;
}

.sidebar.collapsed {
  width: 70px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.collapse-btn {
  background: none;
  border: none;
  color: white;
  font-size: 16px;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.collapse-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.sidebar-nav {
  padding: 20px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.nav-item.active {
  background-color: rgba(255, 255, 255, 0.15);
  border-left-color: #3498db;
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 20px;
  text-align: center;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-header {
  background: white;
  padding: 0 30px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.header-left h2 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-info {
  display: flex;
  align-items: center;
  position: relative;
}

.admin-name {
  margin-right: 12px;
  color: #2c3e50;
  font-weight: 500;
}

.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #2980b9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  margin-right: 8px;
}

.admin-dropdown {
  position: relative;
}

.dropdown-btn {
  background: none;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.dropdown-btn:hover {
  background-color: #ecf0f1;
  color: #2c3e50;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  z-index: 1000;
  animation: fadeInDown 0.3s ease;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  color: #2c3e50;
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}

.dropdown-item:first-child {
  border-radius: 8px 8px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 8px 8px;
}

/* 内容区域 */
.content-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.tab-content {
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 70px;
  }
  
  .sidebar.collapsed {
    width: 0;
  }
  
  .top-header {
    padding: 0 20px;
  }
  
  .header-left h2 {
    font-size: 20px;
  }
  
  .content-area {
    padding: 20px;
  }
  
  .admin-name {
    display: none;
  }
}
</style>
