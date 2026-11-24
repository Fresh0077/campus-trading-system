<template>
  <div class="system-management">
    <div class="page-header">
      <h2>系统管理</h2>
      <p>系统配置、日志管理和维护工具</p>
    </div>

    <!-- 功能选项卡 -->
    <div class="tab-navigation">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="setActiveTab(tab.key)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-text">{{ tab.label }}</span>
      </button>
    </div>

    <!-- 缓存管理 -->
    <div v-if="activeTab === 'cache'" class="tab-content">
      <div class="section-header">
        <h3>🗄️ 缓存管理</h3>
        <button @click="loadCacheInfo" class="refresh-btn" :disabled="isLoading">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M23 4v6h-6M1 20v-6h6M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"></path>
          </svg>
          刷新
        </button>
      </div>

      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载缓存信息中...</p>
      </div>

      <div v-else class="cache-section">
        <!-- 缓存概览 -->
        <div class="cache-overview">
          <div class="cache-card">
            <div class="cache-info">
              <h4>总缓存数量</h4>
              <div class="cache-value">{{ cacheInfo.totalCaches || 0 }}</div>
            </div>
            <div class="cache-icon">📊</div>
          </div>
          <div class="cache-card">
            <div class="cache-info">
              <h4>缓存大小</h4>
              <div class="cache-value">{{ formatSize(cacheInfo.totalSize) }}</div>
            </div>
            <div class="cache-icon">💾</div>
          </div>
          <div class="cache-card">
            <div class="cache-info">
              <h4>命中率</h4>
              <div class="cache-value">{{ cacheInfo.hitRate || '0%' }}</div>
            </div>
            <div class="cache-icon">🎯</div>
          </div>
        </div>

        <!-- 缓存操作 -->
        <div class="cache-actions">
          <button @click="clearAllCache" class="action-btn danger" :disabled="isClearing">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6M14 11v6"></path>
            </svg>
            {{ isClearing ? '清理中...' : '清理所有缓存' }}
          </button>
          <button @click="clearExpiredCache" class="action-btn warning" :disabled="isClearing">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12,6 12,12 16,14"></polyline>
            </svg>
            清理过期缓存
          </button>
        </div>

        <!-- 缓存列表 -->
        <div v-if="cacheInfo.cacheDetails && cacheInfo.cacheDetails.length > 0" class="cache-list">
          <h4>缓存详情</h4>
          <div class="cache-table">
            <div class="cache-table-header">
              <div class="cache-col">缓存名称</div>
              <div class="cache-col">大小</div>
              <div class="cache-col">条目数</div>
              <div class="cache-col">操作</div>
            </div>
            <div v-for="cache in cacheInfo.cacheDetails" :key="cache.name" class="cache-table-row">
              <div class="cache-col">{{ cache.name }}</div>
              <div class="cache-col">{{ formatSize(cache.size) }}</div>
              <div class="cache-col">{{ cache.entries || 0 }}</div>
              <div class="cache-col">
                <button @click="clearSpecificCache(cache.name)" class="mini-btn danger">清理</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统日志 -->
    <div v-if="activeTab === 'logs'" class="tab-content">
      <div class="section-header">
        <h3>📋 系统日志</h3>
        <div class="log-controls">
          <select v-model="logLevel" @change="loadSystemLogs">
            <option value="">所有级别</option>
            <option value="ERROR">错误</option>
            <option value="WARN">警告</option>
            <option value="INFO">信息</option>
            <option value="DEBUG">调试</option>
          </select>
          <input 
            v-model="logKeyword" 
            @input="handleLogSearch"
            placeholder="搜索日志内容..."
            class="log-search"
          >
          <button @click="loadSystemLogs" class="refresh-btn" :disabled="isLoading">刷新</button>
        </div>
      </div>

      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载系统日志中...</p>
      </div>

      <div v-else class="logs-section">
        <div v-if="systemLogs.length === 0" class="no-data">
          <span>暂无日志数据</span>
        </div>
        <div v-else class="logs-list">
          <div v-for="log in systemLogs" :key="log.id" :class="['log-item', `log-${log.level?.toLowerCase()}`]">
            <div class="log-header">
              <span class="log-time">{{ formatTime(log.timestamp) }}</span>
              <span :class="['log-level', `level-${log.level?.toLowerCase()}`]">{{ log.level }}</span>
              <span class="log-source">{{ log.source }}</span>
            </div>
            <div class="log-message">{{ log.message }}</div>
            <div v-if="log.details" class="log-details">
              <pre>{{ log.details }}</pre>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="logTotalPages > 1" class="pagination">
          <button 
            @click="loadSystemLogs(logCurrentPage - 1)" 
            :disabled="logCurrentPage <= 1"
            class="page-btn"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ logCurrentPage }} 页，共 {{ logTotalPages }} 页
          </span>
          <button 
            @click="loadSystemLogs(logCurrentPage + 1)" 
            :disabled="logCurrentPage >= logTotalPages"
            class="page-btn"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 管理员管理 -->
    <div v-if="activeTab === 'admins'" class="tab-content">
      <div class="section-header">
        <h3>👥 管理员管理</h3>
        <button @click="showCreateAdmin = true" class="action-btn primary">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="16"></line>
            <line x1="8" y1="12" x2="16" y2="12"></line>
          </svg>
          添加管理员
        </button>
      </div>

      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载管理员列表中...</p>
      </div>

      <div v-else class="admins-section">
        <div v-if="adminList.length === 0" class="no-data">
          <span>暂无管理员数据</span>
        </div>
        <div v-else class="admins-table">
          <div class="table-header">
            <div class="table-col">用户名</div>
            <div class="table-col">真实姓名</div>
            <div class="table-col">邮箱</div>
            <div class="table-col">状态</div>
            <div class="table-col">创建时间</div>
            <div class="table-col">操作</div>
          </div>
          <div v-for="admin in adminList" :key="admin.id" class="table-row">
            <div class="table-col">{{ admin.username }}</div>
            <div class="table-col">{{ admin.realName || '-' }}</div>
            <div class="table-col">{{ admin.email || '-' }}</div>
            <div class="table-col">
              <span :class="['status-badge', admin.status === 1 ? 'active' : 'inactive']">
                {{ admin.status === 1 ? '正常' : '禁用' }}
              </span>
            </div>
            <div class="table-col">{{ formatTime(admin.createTime) }}</div>
            <div class="table-col">
              <button 
                @click="toggleAdminStatus(admin)" 
                :class="['mini-btn', admin.status === 1 ? 'warning' : 'success']"
              >
                {{ admin.status === 1 ? '禁用' : '启用' }}
              </button>
              <button 
                @click="deleteAdminAccount(admin)" 
                class="mini-btn danger"
                v-if="admin.id !== currentAdminId"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统维护 -->
    <div v-if="activeTab === 'maintenance'" class="tab-content">
      <div class="section-header">
        <h3>🔧 系统维护</h3>
      </div>

      <div class="maintenance-section">
        <div class="maintenance-card">
          <div class="maintenance-info">
            <h4>数据清理</h4>
            <p>清理超过指定天数的临时数据和日志</p>
          </div>
          <div class="maintenance-actions">
            <select v-model="cleanupDays" class="cleanup-select">
              <option value="7">7天前</option>
              <option value="30">30天前</option>
              <option value="90">90天前</option>
            </select>
            <button @click="performCleanup" class="action-btn warning" :disabled="isCleaning">
              {{ isCleaning ? '清理中...' : '开始清理' }}
            </button>
          </div>
        </div>

        <div class="maintenance-card">
          <div class="maintenance-info">
            <h4>系统信息</h4>
            <p>查看系统运行状态和基本信息</p>
          </div>
          <div class="maintenance-actions">
            <button @click="loadSystemInfo" class="action-btn primary">查看详情</button>
          </div>
        </div>
      </div>

      <!-- 系统信息显示 -->
      <div v-if="systemInfo" class="system-info">
        <h4>系统信息</h4>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">系统版本:</span>
            <span class="info-value">{{ systemInfo.version || 'v1.0.0' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">运行时间:</span>
            <span class="info-value">{{ systemInfo.uptime || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Java版本:</span>
            <span class="info-value">{{ systemInfo.javaVersion || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">内存使用:</span>
            <span class="info-value">{{ systemInfo.memoryUsage || '-' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建管理员弹窗 -->
    <div v-if="showCreateAdmin" class="modal-overlay" @click="closeCreateAdmin">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>添加管理员</h3>
          <button @click="closeCreateAdmin" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="createNewAdmin">
            <div class="form-group">
              <label>用户名 *</label>
              <input v-model="newAdmin.username" type="text" required placeholder="请输入用户名">
            </div>
            <div class="form-group">
              <label>密码 *</label>
              <input v-model="newAdmin.password" type="password" required placeholder="请输入密码">
            </div>
            <div class="form-group">
              <label>真实姓名</label>
              <input v-model="newAdmin.realName" type="text" placeholder="请输入真实姓名">
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input v-model="newAdmin.email" type="email" placeholder="请输入邮箱">
            </div>
            <div class="form-actions">
              <button type="button" @click="closeCreateAdmin" class="btn secondary">取消</button>
              <button type="submit" class="btn primary" :disabled="isCreating">
                {{ isCreating ? '创建中...' : '创建' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { 
  getCacheInfo, 
  clearCache, 
  getSystemLogs, 
  cleanupSystemData,
  getAdminList,
  createAdmin,
  updateAdminStatus,
  deleteAdmin
} from '../../api/admin'
import { showToast } from '../../services/toast'

// 响应式数据
const activeTab = ref('cache')
const isLoading = ref(false)
const isClearing = ref(false)
const isCleaning = ref(false)
const isCreating = ref(false)

// 缓存相关
const cacheInfo = ref({})

// 日志相关
const systemLogs = ref([])
const logLevel = ref('')
const logKeyword = ref('')
const logCurrentPage = ref(1)
const logTotalPages = ref(1)
let logSearchTimeout = null

// 管理员相关
const adminList = ref([])
const showCreateAdmin = ref(false)
const newAdmin = ref({
  username: '',
  password: '',
  realName: '',
  email: ''
})

// 系统维护相关
const cleanupDays = ref(30)
const systemInfo = ref(null)

// 当前管理员ID
const currentAdminId = computed(() => {
  const adminInfo = localStorage.getItem('adminInfo')
  if (adminInfo) {
    return JSON.parse(adminInfo).id
  }
  return null
})

// 选项卡配置
const tabs = [
  { key: 'cache', label: '缓存管理', icon: '🗄️' },
  { key: 'logs', label: '系统日志', icon: '📋' },
  { key: 'admins', label: '管理员管理', icon: '👥' },
  { key: 'maintenance', label: '系统维护', icon: '🔧' }
]

// 方法
const setActiveTab = (tab) => {
  activeTab.value = tab
  
  // 根据选项卡加载对应数据
  switch (tab) {
    case 'cache':
      loadCacheInfo()
      break
    case 'logs':
      loadSystemLogs()
      break
    case 'admins':
      loadAdminList()
      break
    case 'maintenance':
      // 维护页面不需要自动加载数据
      break
  }
}

// 缓存管理方法
const loadCacheInfo = async () => {
  try {
    isLoading.value = true
    const response = await getCacheInfo()
    if (response.success) {
      cacheInfo.value = response.data || {}
    } else {
      showToast.show({
        message: response.message || '获取缓存信息失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Load cache info error:', error)
    showToast.show({
      message: '获取缓存信息失败',
      type: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

const clearAllCache = async () => {
  if (!confirm('确定要清理所有缓存吗？此操作不可撤销。')) {
    return
  }
  
  try {
    isClearing.value = true
    const response = await clearCache([])
    if (response.success) {
      showToast.show({
        message: '缓存清理成功',
        type: 'success'
      })
      loadCacheInfo()
    } else {
      showToast.show({
        message: response.message || '缓存清理失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Clear cache error:', error)
    showToast.show({
      message: '缓存清理失败',
      type: 'error'
    })
  } finally {
    isClearing.value = false
  }
}

const clearExpiredCache = async () => {
  try {
    isClearing.value = true
    const response = await clearCache(['expired'])
    if (response.success) {
      showToast.show({
        message: '过期缓存清理成功',
        type: 'success'
      })
      loadCacheInfo()
    } else {
      showToast.show({
        message: response.message || '过期缓存清理失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Clear expired cache error:', error)
    showToast.show({
      message: '过期缓存清理失败',
      type: 'error'
    })
  } finally {
    isClearing.value = false
  }
}

const clearSpecificCache = async (cacheName) => {
  if (!confirm(`确定要清理缓存 "${cacheName}" 吗？`)) {
    return
  }
  
  try {
    const response = await clearCache([cacheName])
    if (response.success) {
      showToast.show({
        message: `缓存 "${cacheName}" 清理成功`,
        type: 'success'
      })
      loadCacheInfo()
    } else {
      showToast.show({
        message: response.message || '缓存清理失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Clear specific cache error:', error)
    showToast.show({
      message: '缓存清理失败',
      type: 'error'
    })
  }
}

// 系统日志方法
const loadSystemLogs = async (page = 1) => {
  try {
    isLoading.value = true
    logCurrentPage.value = page
    
    const params = {
      page: page - 1,
      size: 20,
      level: logLevel.value || undefined,
      keyword: logKeyword.value || undefined
    }
    
    const response = await getSystemLogs(params)
    if (response.success && response.data) {
      systemLogs.value = response.data.content || []
      logTotalPages.value = response.data.totalPages || 1
    } else {
      showToast.show({
        message: response.message || '获取系统日志失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Load system logs error:', error)
    showToast.show({
      message: '获取系统日志失败',
      type: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

const handleLogSearch = () => {
  if (logSearchTimeout) {
    clearTimeout(logSearchTimeout)
  }
  logSearchTimeout = setTimeout(() => {
    loadSystemLogs(1)
  }, 500)
}

// 管理员管理方法
const loadAdminList = async () => {
  try {
    isLoading.value = true
    const response = await getAdminList()
    if (response.success && response.data) {
      adminList.value = response.data || []
    } else {
      showToast.show({
        message: response.message || '获取管理员列表失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Load admin list error:', error)
    showToast.show({
      message: '获取管理员列表失败',
      type: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

const createNewAdmin = async () => {
  if (!newAdmin.value.username || !newAdmin.value.password) {
    showToast.show({
      message: '请填写必填字段',
      type: 'warning'
    })
    return
  }
  
  try {
    isCreating.value = true
    const response = await createAdmin(newAdmin.value)
    if (response.success) {
      showToast.show({
        message: '管理员创建成功',
        type: 'success'
      })
      closeCreateAdmin()
      loadAdminList()
    } else {
      showToast.show({
        message: response.message || '管理员创建失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Create admin error:', error)
    showToast.show({
      message: '管理员创建失败',
      type: 'error'
    })
  } finally {
    isCreating.value = false
  }
}

const toggleAdminStatus = async (admin) => {
  const newStatus = admin.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  if (!confirm(`确定要${action}管理员 "${admin.username}" 吗？`)) {
    return
  }
  
  try {
    const response = await updateAdminStatus(admin.id, newStatus)
    if (response.success) {
      showToast.show({
        message: `${action}成功`,
        type: 'success'
      })
      loadAdminList()
    } else {
      showToast.show({
        message: response.message || `${action}失败`,
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Toggle admin status error:', error)
    showToast.show({
      message: `${action}失败`,
      type: 'error'
    })
  }
}

const deleteAdminAccount = async (admin) => {
  if (!confirm(`确定要删除管理员 "${admin.username}" 吗？此操作不可撤销。`)) {
    return
  }
  
  try {
    const response = await deleteAdmin(admin.id)
    if (response.success) {
      showToast.show({
        message: '管理员删除成功',
        type: 'success'
      })
      loadAdminList()
    } else {
      showToast.show({
        message: response.message || '管理员删除失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Delete admin error:', error)
    showToast.show({
      message: '管理员删除失败',
      type: 'error'
    })
  }
}

const closeCreateAdmin = () => {
  showCreateAdmin.value = false
  newAdmin.value = {
    username: '',
    password: '',
    realName: '',
    email: ''
  }
}

// 系统维护方法
const performCleanup = async () => {
  if (!confirm(`确定要清理 ${cleanupDays.value} 天前的数据吗？此操作不可撤销。`)) {
    return
  }
  
  try {
    isCleaning.value = true
    const response = await cleanupSystemData(cleanupDays.value)
    if (response.success) {
      showToast.show({
        message: '数据清理成功',
        type: 'success'
      })
    } else {
      showToast.show({
        message: response.message || '数据清理失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Cleanup system data error:', error)
    showToast.show({
      message: '数据清理失败',
      type: 'error'
    })
  } finally {
    isCleaning.value = false
  }
}

const loadSystemInfo = () => {
  // 模拟系统信息
  systemInfo.value = {
    version: 'v1.0.0',
    uptime: '7天 12小时 30分钟',
    javaVersion: 'OpenJDK 17.0.2',
    memoryUsage: '512MB / 2GB (25%)'
  }
  
  showToast.show({
    message: '系统信息加载成功',
    type: 'success'
  })
}

// 工具方法
const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时加载缓存信息
onMounted(() => {
  loadCacheInfo()
})
</script>

<style scoped>
.system-management {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

/* 选项卡导航 */
.tab-navigation {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 2px solid #e1e8ed;
  padding-bottom: 16px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #f8f9fa;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
}

.tab-btn:hover {
  background: #e9ecef;
  color: #495057;
}

.tab-btn.active {
  background: #3498db;
  color: white;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.3);
}

.tab-icon {
  font-size: 16px;
}

/* 通用样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e1e8ed;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.refresh-btn, .action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn {
  background: #6c757d;
  color: white;
}

.refresh-btn:hover:not(:disabled) {
  background: #5a6268;
}

.action-btn.primary {
  background: #3498db;
  color: white;
}

.action-btn.primary:hover:not(:disabled) {
  background: #2980b9;
}

.action-btn.danger {
  background: #e74c3c;
  color: white;
}

.action-btn.danger:hover:not(:disabled) {
  background: #c0392b;
}

.action-btn.warning {
  background: #f39c12;
  color: white;
}

.action-btn.warning:hover:not(:disabled) {
  background: #e67e22;
}

.action-btn:disabled, .refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载状态 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #7f8c8d;
  font-size: 16px;
}

/* 缓存管理样式 */
.cache-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.cache-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border-left: 4px solid #3498db;
}

.cache-info h4 {
  margin: 0 0 8px 0;
  color: #6c757d;
  font-size: 14px;
  font-weight: 500;
}

.cache-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
}

.cache-icon {
  font-size: 32px;
  opacity: 0.7;
}

.cache-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.cache-list h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 18px;
}

.cache-table {
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  overflow: hidden;
}

.cache-table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.cache-table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  border-top: 1px solid #e1e8ed;
}

.cache-col {
  padding: 12px 16px;
  display: flex;
  align-items: center;
}

/* 日志管理样式 */
.log-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.log-controls select, .log-search {
  padding: 8px 12px;
  border: 2px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
}

.log-search {
  min-width: 200px;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  padding: 16px;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  border-left: 4px solid #6c757d;
}

.log-item.log-error {
  border-left-color: #e74c3c;
  background: rgba(231, 76, 60, 0.05);
}

.log-item.log-warn {
  border-left-color: #f39c12;
  background: rgba(243, 156, 18, 0.05);
}

.log-item.log-info {
  border-left-color: #3498db;
  background: rgba(52, 152, 219, 0.05);
}

.log-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
}

.log-time {
  color: #6c757d;
}

.log-level {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  text-transform: uppercase;
}

.level-error {
  background: #e74c3c;
  color: white;
}

.level-warn {
  background: #f39c12;
  color: white;
}

.level-info {
  background: #3498db;
  color: white;
}

.level-debug {
  background: #6c757d;
  color: white;
}

.log-source {
  color: #495057;
  font-weight: 500;
}

.log-message {
  color: #2c3e50;
  font-size: 14px;
  line-height: 1.5;
}

.log-details {
  margin-top: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
}

.log-details pre {
  margin: 0;
  font-size: 12px;
  color: #495057;
  white-space: pre-wrap;
}

/* 管理员管理样式 */
.admins-table {
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr 80px 120px 120px;
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1.5fr 80px 120px 120px;
  border-top: 1px solid #e1e8ed;
}

.table-col {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.mini-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  margin-right: 4px;
  transition: all 0.3s ease;
}

.mini-btn.success {
  background: #28a745;
  color: white;
}

.mini-btn.warning {
  background: #ffc107;
  color: #212529;
}

.mini-btn.danger {
  background: #dc3545;
  color: white;
}

/* 系统维护样式 */
.maintenance-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.maintenance-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  background: #f8f9fa;
}

.maintenance-info h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
}

.maintenance-info p {
  margin: 0;
  color: #6c757d;
  font-size: 14px;
}

.maintenance-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.cleanup-select {
  padding: 8px 12px;
  border: 2px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
}

.system-info {
  margin-top: 24px;
  padding: 20px;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  background: #f8f9fa;
}

.system-info h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #dee2e6;
}

.info-label {
  font-weight: 500;
  color: #495057;
}

.info-value {
  color: #2c3e50;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e1e8ed;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #6c757d;
  font-size: 14px;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e1e8ed;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #495057;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #2c3e50;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #3498db;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn.primary {
  background: #3498db;
  color: white;
}

.btn.primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn.secondary {
  background: #6c757d;
  color: white;
}

.btn.secondary:hover {
  background: #5a6268;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tab-navigation {
    flex-wrap: wrap;
  }
  
  .cache-overview {
    grid-template-columns: 1fr;
  }
  
  .log-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .table-header, .table-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .table-col {
    padding: 8px 12px;
  }
  
  .maintenance-card {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 24px;
}

.page-header p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

.coming-soon {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.coming-soon h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 20px;
}

.coming-soon p {
  margin: 0 0 30px 0;
  font-size: 16px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 300px;
  margin: 0 auto;
}

.feature-item {
  text-align: left;
  color: #27ae60;
  font-size: 14px;
}
</style>
