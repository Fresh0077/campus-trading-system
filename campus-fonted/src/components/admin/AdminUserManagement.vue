<template>
  <div class="user-management">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-section">
        <input
          v-model="searchKeyword"
          @input="handleSearch"
          type="text"
          placeholder="搜索用户名、姓名或学号..."
          class="search-input"
        />
        <button @click="loadUsers" class="refresh-btn">🔄 刷新</button>
      </div>
      
      <div class="filter-section">
        <select v-model="statusFilter" @change="handleStatusFilter" class="status-filter">
          <option value="">全部状态</option>
          <option value="1">正常</option>
          <option value="0">禁用</option>
        </select>
      </div>
    </div>
    
    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedUsers.length > 0">
      <span class="selected-count">已选择 {{ selectedUsers.length }} 个用户</span>
      <button @click="batchUpdateStatus(1)" class="batch-btn enable">批量启用</button>
      <button @click="batchUpdateStatus(0)" class="batch-btn disable">批量禁用</button>
    </div>
    
    <!-- 用户列表 -->
    <div class="user-table-container">
      <table class="user-table">
        <thead>
          <tr>
            <th>
              <input
                type="checkbox"
                :checked="isAllSelected"
                @change="toggleSelectAll"
              />
            </th>
            <th>头像</th>
            <th>用户信息</th>
            <th>联系方式</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id" class="user-row">
            <td>
              <input
                type="checkbox"
                :value="user.id"
                v-model="selectedUsers"
              />
            </td>
            <td>
              <div class="user-avatar">
                <img v-if="user.avatar" :src="getImageUrl(user.avatar)" :alt="user.username" />
                <div v-else class="avatar-placeholder">
                  {{ user.realName ? user.realName.charAt(0) : user.username.charAt(0) }}
                </div>
              </div>
            </td>
            <td>
              <div class="user-info">
                <div class="username">{{ user.username }}</div>
                <div class="real-name">{{ user.realName }}</div>
                <div class="student-id">学号: {{ user.studentId }}</div>
              </div>
            </td>
            <td>
              <div class="contact-info">
                <div v-if="user.phone" class="phone">📱 {{ user.phone }}</div>
                <div v-if="user.email" class="email">📧 {{ user.email }}</div>
              </div>
            </td>
            <td>
              <span class="status-badge" :class="{ active: user.status === 1, disabled: user.status === 0 }">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="create-time">{{ formatTime(user.createTime) }}</div>
            </td>
            <td>
              <div class="action-buttons">
                <button @click="viewUserDetail(user)" class="action-btn view">详情</button>
                <button
                  @click="toggleUserStatus(user)"
                  class="action-btn"
                  :class="{ enable: user.status === 0, disable: user.status === 1 }"
                >
                  {{ user.status === 1 ? '禁用' : '启用' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!isLoading && users.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <div class="empty-text">暂无用户数据</div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
        @click="changePage(currentPage - 1)"
        :disabled="currentPage <= 0"
        class="page-btn"
      >
        上一页
      </button>
      
      <div class="page-numbers">
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="changePage(page - 1)"
          class="page-btn"
          :class="{ active: page - 1 === currentPage }"
        >
          {{ page }}
        </button>
      </div>
      
      <button
        @click="changePage(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="page-btn"
      >
        下一页
      </button>
    </div>
    
    <!-- 用户详情弹窗 -->
    <div v-if="showUserDetail" class="modal-overlay" @click="closeUserDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>用户详情</h3>
          <button @click="closeUserDetail" class="close-btn">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedUser" class="user-detail">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>用户名:</label>
                  <span>{{ selectedUser.username }}</span>
                </div>
                <div class="detail-item">
                  <label>真实姓名:</label>
                  <span>{{ selectedUser.realName }}</span>
                </div>
                <div class="detail-item">
                  <label>学号:</label>
                  <span>{{ selectedUser.studentId }}</span>
                </div>
                <div class="detail-item">
                  <label>手机号:</label>
                  <span>{{ selectedUser.phone || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <label>邮箱:</label>
                  <span>{{ selectedUser.email || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <label>状态:</label>
                  <span class="status-badge" :class="{ active: selectedUser.status === 1, disabled: selectedUser.status === 0 }">
                    {{ selectedUser.status === 1 ? '正常' : '禁用' }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="detail-section" v-if="userStats">
              <h4>统计信息</h4>
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.productCount || 0 }}</div>
                  <div class="stat-label">发布商品</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.soldProductCount || 0 }}</div>
                  <div class="stat-label">已售商品</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.buyOrderCount || 0 }}</div>
                  <div class="stat-label">购买订单</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStats.sellOrderCount || 0 }}</div>
                  <div class="stat-label">销售订单</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getUserList, updateUserStatus, batchUpdateUserStatus, getUserDetail } from '../../api/admin'
import { showToast } from '../../services/toast'
import { getImageUrl } from '../../utils/imageUtils'

const users = ref([])
const selectedUsers = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const isLoading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)

const showUserDetail = ref(false)
const selectedUser = ref(null)
const userStats = ref(null)

let searchTimeout = null

onMounted(() => {
  loadUsers()
})

const isAllSelected = computed(() => {
  return users.value.length > 0 && selectedUsers.value.length === users.value.length
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 3)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

const loadUsers = async () => {
  isLoading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value || undefined
    }
    
    const response = await getUserList(params)
    if (response.success && response.data) {
      users.value = response.data.content || []
      totalElements.value = response.data.totalElements || 0
      totalPages.value = response.data.totalPages || 0
    } else {
      showToast.show({
        message: response.message || '获取用户列表失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Load users error:', error)
    showToast.show({
      message: '获取用户列表失败',
      type: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadUsers()
  }, 500)
}

const handleStatusFilter = () => {
  currentPage.value = 0
  loadUsers()
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedUsers.value = []
  } else {
    selectedUsers.value = users.value.map(user => user.id)
  }
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  if (!confirm(`确定要${action}用户 "${user.username}" 吗？`)) {
    return
  }
  
  try {
    const response = await updateUserStatus(user.id, newStatus)
    if (response.success) {
      showToast.show({
        message: `${action}成功`,
        type: 'success'
      })
      loadUsers()
    } else {
      showToast.show({
        message: response.message || `${action}失败`,
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Update user status error:', error)
    showToast.show({
      message: `${action}失败`,
      type: 'error'
    })
  }
}

const batchUpdateStatus = async (status) => {
  if (selectedUsers.value.length === 0) {
    showToast.show({
      message: '请选择要操作的用户',
      type: 'warning'
    })
    return
  }
  
  const action = status === 1 ? '启用' : '禁用'
  if (!confirm(`确定要${action} ${selectedUsers.value.length} 个用户吗？`)) {
    return
  }
  
  try {
    const response = await batchUpdateUserStatus(selectedUsers.value, status)
    if (response.success) {
      showToast.show({
        message: `批量${action}成功`,
        type: 'success'
      })
      selectedUsers.value = []
      loadUsers()
    } else {
      showToast.show({
        message: response.message || `批量${action}失败`,
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Batch update user status error:', error)
    showToast.show({
      message: `批量${action}失败`,
      type: 'error'
    })
  }
}

const viewUserDetail = async (user) => {
  selectedUser.value = user
  showUserDetail.value = true
  
  try {
    const response = await getUserDetail(user.id)
    if (response.success) {
      userStats.value = response.data.statistics
    }
  } catch (error) {
    console.error('Get user detail error:', error)
  }
}

const closeUserDetail = () => {
  showUserDetail.value = false
  selectedUser.value = null
  userStats.value = null
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadUsers()
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}
</script>

<style scoped>
.user-management {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.search-section {
  display: flex;
  gap: 12px;
  flex: 1;
}

.search-input {
  flex: 1;
  max-width: 300px;
  padding: 10px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
}

.refresh-btn {
  padding: 10px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.refresh-btn:hover {
  background: #2980b9;
}

.filter-section {
  display: flex;
  gap: 12px;
}

.status-filter {
  padding: 10px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

/* 批量操作 */
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.selected-count {
  color: #2c3e50;
  font-weight: 500;
}

.batch-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.batch-btn.enable {
  background: #27ae60;
  color: white;
}

.batch-btn.enable:hover {
  background: #219a52;
}

.batch-btn.disable {
  background: #e74c3c;
  color: white;
}

.batch-btn.disable:hover {
  background: #c0392b;
}

/* 用户表格 */
.user-table-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.user-table th,
.user-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e1e5e9;
}

.user-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.user-row:hover {
  background: #f8f9fa;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3498db, #2980b9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.user-info {
  min-width: 150px;
}

.username {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.real-name {
  color: #7f8c8d;
  font-size: 13px;
  margin-bottom: 2px;
}

.student-id {
  color: #95a5a6;
  font-size: 12px;
}

.contact-info {
  min-width: 120px;
}

.phone,
.email {
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 2px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.disabled {
  background: #fadbd8;
  color: #e74c3c;
}

.create-time {
  color: #7f8c8d;
  font-size: 13px;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.action-btn.view {
  background: #3498db;
  color: white;
}

.action-btn.view:hover {
  background: #2980b9;
}

.action-btn.enable {
  background: #27ae60;
  color: white;
}

.action-btn.enable:hover {
  background: #219a52;
}

.action-btn.disable {
  background: #e74c3c;
  color: white;
}

.action-btn.disable:hover {
  background: #c0392b;
}

/* 加载和空状态 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #7f8c8d;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e1e5e9;
  border-top: 2px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #e1e5e9;
  background: white;
  color: #2c3e50;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: #3498db;
  color: #3498db;
}

.page-btn.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 弹窗样式 */
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
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #7f8c8d;
  padding: 4px;
}

.close-btn:hover {
  color: #2c3e50;
}

.modal-body {
  padding: 24px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-weight: 500;
  color: #7f8c8d;
  font-size: 14px;
}

.detail-item span {
  color: #2c3e50;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #7f8c8d;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section {
    flex-direction: column;
  }
  
  .search-input {
    max-width: none;
  }
  
  .user-table {
    font-size: 12px;
  }
  
  .user-table th,
  .user-table td {
    padding: 8px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
