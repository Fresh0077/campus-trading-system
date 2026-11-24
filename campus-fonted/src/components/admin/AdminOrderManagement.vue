<template>
  <div class="order-management">
    <div class="page-header">
      <h2>订单管理</h2>
      <p>管理平台上的所有交易订单</p>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          @input="handleSearch"
          placeholder="搜索订单号..."
          class="search-input"
        />
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </div>
      
      <div class="filters">
        <select v-model="statusFilter" @change="loadOrders" class="filter-select">
          <option value="">全部状态</option>
          <option value="1">待确认</option>
          <option value="2">进行中</option>
          <option value="3">已完成</option>
          <option value="4">已取消</option>
        </select>
        
        <select v-model="sortBy" @change="loadOrders" class="filter-select">
          <option value="createTime">创建时间</option>
          <option value="updateTime">更新时间</option>
          <option value="amount">交易金额</option>
        </select>
        
        <select v-model="sortDir" @change="loadOrders" class="filter-select">
          <option value="desc">降序</option>
          <option value="asc">升序</option>
        </select>
      </div>
    </div>
    
    <!-- 订单列表 -->
    <div class="orders-container">
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无订单</h3>
        <p>还没有找到符合条件的订单</p>
      </div>
      
      <div v-else class="orders-table">
        <table>
          <thead>
            <tr>
              <th>订单信息</th>
              <th>商品信息</th>
              <th>买家信息</th>
              <th>卖家信息</th>
              <th>金额</th>
              <th>状态</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id" class="order-row">
              <td class="order-info">
                <div class="order-main">
                  <div class="order-no">{{ order.orderNo }}</div>
                  <div class="order-meta">
                    <span class="confirm-status">
                      买家: {{ order.buyerConfirm ? '已确认' : '未确认' }}
                    </span>
                    <span class="confirm-status">
                      卖家: {{ order.sellerConfirm ? '已确认' : '未确认' }}
                    </span>
                  </div>
                </div>
              </td>
              <td class="product-info">
                <div class="product-main">
                  <img 
                    :src="getProductImage(order)" 
                    :alt="order.productTitle"
                    class="product-image"
                    @error="handleImageError"
                  />
                  <div class="product-details">
                    <h4 class="product-title">{{ order.productTitle || '商品已删除' }}</h4>
                    <p class="product-description">{{ order.productDescription || '暂无描述' }}</p>
                  </div>
                </div>
              </td>
              <td class="user-info">
                <div class="user-details">
                  <div class="username">{{ order.buyerUsername || '未知用户' }}</div>
                  <div class="user-meta">
                    <span v-if="order.buyerStudentId">学号: {{ order.buyerStudentId }}</span>
                    <span v-if="order.buyerPhone">{{ order.buyerPhone }}</span>
                  </div>
                </div>
              </td>
              <td class="user-info">
                <div class="user-details">
                  <div class="username">{{ order.sellerUsername || '未知用户' }}</div>
                  <div class="user-meta">
                    <span v-if="order.sellerStudentId">学号: {{ order.sellerStudentId }}</span>
                    <span v-if="order.sellerPhone">{{ order.sellerPhone }}</span>
                  </div>
                </div>
              </td>
              <td class="amount">
                <span class="price">¥{{ order.amount }}</span>
              </td>
              <td class="status">
                <span :class="['status-badge', getStatusClass(order.status)]">
                  {{ getStatusText(order.status) }}
                </span>
              </td>
              <td class="create-time">{{ formatDate(order.createTime) }}</td>
              <td class="actions">
                <div class="action-buttons">
                  <button @click="viewOrderDetail(order)" class="action-btn view" title="查看详情">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                  <button 
                    v-if="order.status !== 3" 
                    @click="updateOrderStatusAction(order.id, 3)" 
                    class="action-btn complete" 
                    title="标记完成"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 6L9 17l-5-5"></path>
                    </svg>
                  </button>
                  <button 
                    v-if="order.status !== 4" 
                    @click="updateOrderStatusAction(order.id, 4)" 
                    class="action-btn cancel" 
                    title="标记取消"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M18 6L6 18M6 6l12 12"></path>
                    </svg>
                  </button>
                  <button 
                    @click="deleteOrderAction(order.id)" 
                    class="action-btn delete" 
                    title="删除"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage === 0"
        class="page-btn"
      >
        上一页
      </button>
      
      <div class="page-numbers">
        <button 
          v-for="page in visiblePages" 
          :key="page"
          @click="changePage(page - 1)"
          :class="['page-number', { active: page - 1 === currentPage }]"
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
      
      <div class="page-info">
        <span>共 {{ totalElements }} 条记录，第 {{ currentPage + 1 }}/{{ totalPages }} 页</span>
      </div>
    </div>
    
    <!-- 订单详情弹窗 -->
    <div v-if="showOrderDetail" class="modal-overlay" @click="closeOrderDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>订单详情</h3>
          <button @click="closeOrderDetail" class="close-btn">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body" v-if="selectedOrder">
          <div class="order-detail-content">
            <div class="order-basic-info">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>订单号:</label>
                  <span>{{ selectedOrder.orderNo }}</span>
                </div>
                <div class="detail-item">
                  <label>交易金额:</label>
                  <span class="amount">¥{{ selectedOrder.amount }}</span>
                </div>
                <div class="detail-item">
                  <label>订单状态:</label>
                  <span :class="['status-badge', getStatusClass(selectedOrder.status)]">
                    {{ getStatusText(selectedOrder.status) }}
                  </span>
                </div>
                <div class="detail-item">
                  <label>创建时间:</label>
                  <span>{{ formatDate(selectedOrder.createTime) }}</span>
                </div>
                <div class="detail-item" v-if="selectedOrder.completeTime">
                  <label>完成时间:</label>
                  <span>{{ formatDate(selectedOrder.completeTime) }}</span>
                </div>
                <div class="detail-item" v-if="selectedOrder.cancelTime">
                  <label>取消时间:</label>
                  <span>{{ formatDate(selectedOrder.cancelTime) }}</span>
                </div>
              </div>
            </div>
            
            <div class="order-parties-info">
              <div class="party-info">
                <h4>买家信息</h4>
                <div class="party-details">
                  <div class="party-item">
                    <label>用户名:</label>
                    <span>{{ selectedOrder.buyerUsername || '未知' }}</span>
                  </div>
                  <div class="party-item" v-if="selectedOrder.buyerStudentId">
                    <label>学号:</label>
                    <span>{{ selectedOrder.buyerStudentId }}</span>
                  </div>
                  <div class="party-item" v-if="selectedOrder.buyerPhone">
                    <label>联系方式:</label>
                    <span>{{ selectedOrder.buyerPhone }}</span>
                  </div>
                  <div class="party-item">
                    <label>确认状态:</label>
                    <span :class="selectedOrder.buyerConfirm ? 'confirmed' : 'unconfirmed'">
                      {{ selectedOrder.buyerConfirm ? '已确认' : '未确认' }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="party-info">
                <h4>卖家信息</h4>
                <div class="party-details">
                  <div class="party-item">
                    <label>用户名:</label>
                    <span>{{ selectedOrder.sellerUsername || '未知' }}</span>
                  </div>
                  <div class="party-item" v-if="selectedOrder.sellerStudentId">
                    <label>学号:</label>
                    <span>{{ selectedOrder.sellerStudentId }}</span>
                  </div>
                  <div class="party-item" v-if="selectedOrder.sellerPhone">
                    <label>联系方式:</label>
                    <span>{{ selectedOrder.sellerPhone }}</span>
                  </div>
                  <div class="party-item">
                    <label>确认状态:</label>
                    <span :class="selectedOrder.sellerConfirm ? 'confirmed' : 'unconfirmed'">
                      {{ selectedOrder.sellerConfirm ? '已确认' : '未确认' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="order-product-info" v-if="selectedOrder.productTitle">
              <h4>商品信息</h4>
              <div class="product-detail">
                <img 
                  :src="getProductImage(selectedOrder)" 
                  :alt="selectedOrder.productTitle"
                  class="product-detail-image"
                  @error="handleImageError"
                />
                <div class="product-detail-info">
                  <h5>{{ selectedOrder.productTitle }}</h5>
                  <p>{{ selectedOrder.productDescription || '暂无描述' }}</p>
                  <div class="product-price">商品价格: ¥{{ selectedOrder.productPrice }}</div>
                </div>
              </div>
            </div>
            
            <div class="order-notes" v-if="selectedOrder.remark || selectedOrder.cancelReason">
              <h4>备注信息</h4>
              <div v-if="selectedOrder.remark" class="note-item">
                <label>订单备注:</label>
                <p>{{ selectedOrder.remark }}</p>
              </div>
              <div v-if="selectedOrder.cancelReason" class="note-item">
                <label>取消原因:</label>
                <p>{{ selectedOrder.cancelReason }}</p>
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
import { getOrderList, updateOrderStatus, deleteOrder, getOrderDetail } from '../../api/admin'
import { showToast } from '../../services/toast'
import { handleImageError } from '../../utils/imageUtils'

const orders = ref([])
const isLoading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const sortBy = ref('createTime')
const sortDir = ref('desc')
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)
const showOrderDetail = ref(false)
const selectedOrder = ref(null)

let searchTimeout = null

// 加载订单列表
const loadOrders = async () => {
  try {
    isLoading.value = true
    
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDir: sortDir.value
    }
    
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    const response = await getOrderList(params)
    
    if (response.success && response.data) {
      orders.value = response.data.content || []
      totalElements.value = response.data.totalElements || 0
      totalPages.value = response.data.totalPages || 0
    } else {
      orders.value = []
      totalElements.value = 0
      totalPages.value = 0
      showToast.error('获取订单列表失败')
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    orders.value = []
    totalElements.value = 0
    totalPages.value = 0
    showToast.error('获取订单列表失败')
  } finally {
    isLoading.value = false
  }
}

// 搜索处理（防抖）
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadOrders()
  }, 500)
}

// 更新订单状态
const updateOrderStatusAction = async (orderId, status) => {
  try {
    const statusText = getStatusText(status)
    if (!confirm(`确定要将订单状态更新为"${statusText}"吗？`)) {
      return
    }
    
    await updateOrderStatus(orderId, status)
    showToast.success(`订单状态已更新为"${statusText}"`)
    await loadOrders()
  } catch (error) {
    console.error('更新订单状态失败:', error)
    showToast.error('更新订单状态失败')
  }
}

// 删除订单
const deleteOrderAction = async (orderId) => {
  try {
    if (!confirm('确定要删除这个订单吗？此操作不可恢复！')) {
      return
    }
    
    await deleteOrder(orderId)
    showToast.success('订单删除成功')
    await loadOrders()
  } catch (error) {
    console.error('删除订单失败:', error)
    showToast.error('删除订单失败')
  }
}

// 查看订单详情
const viewOrderDetail = async (order) => {
  try {
    const response = await getOrderDetail(order.id)
    if (response.success && response.data) {
      selectedOrder.value = response.data.order || order
      showOrderDetail.value = true
    } else {
      selectedOrder.value = order
      showOrderDetail.value = true
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    selectedOrder.value = order
    showOrderDetail.value = true
  }
}

// 关闭订单详情
const closeOrderDetail = () => {
  showOrderDetail.value = false
  selectedOrder.value = null
}

// 分页处理
const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadOrders()
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'pending'
    case 2: return 'processing'
    case 3: return 'completed'
    case 4: return 'cancelled'
    default: return 'unknown'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1: return '待确认'
    case 2: return '进行中'
    case 3: return '已完成'
    case 4: return '已取消'
    default: return '未知状态'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取商品图片
const getProductImage = (order) => {
  if (order.productImage) {
    if (order.productImage.startsWith('http')) {
      return order.productImage
    }
    return `${import.meta.env.VITE_API_BASE_URL}/uploads/${order.productImage}`
  }
  return '/default-product.jpg'
}

// 计算可见页码
const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value + 1
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages
})

// 组件挂载时加载数据
onMounted(() => {
  loadOrders()
})</script>

<style scoped>
.order-management {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
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

/* 搜索和筛选 */
.search-filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #7f8c8d;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  padding: 10px 12px;
  border: 2px solid #e1e8ed;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #3498db;
}

/* 订单容器 */
.orders-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 20px;
}

/* 订单表格 */
.orders-table {
  overflow-x: auto;
}

.orders-table table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.orders-table th {
  background: #f8f9fa;
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e1e8ed;
  white-space: nowrap;
}

.orders-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #e1e8ed;
  vertical-align: top;
}

.order-row:hover {
  background: #f8f9fa;
}

/* 订单信息 */
.order-info .order-no {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.confirm-status {
  font-size: 12px;
  color: #7f8c8d;
}

/* 商品信息 */
.product-main {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-description {
  margin: 0;
  font-size: 12px;
  color: #7f8c8d;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 用户信息 */
.user-details .username {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-meta span {
  font-size: 12px;
  color: #7f8c8d;
}

/* 金额 */
.amount .price {
  font-weight: 600;
  color: #e74c3c;
  font-size: 16px;
}

/* 状态徽章 */
.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  min-width: 60px;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-badge.processing {
  background: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.status-badge.completed {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8f9fa;
  color: #6c757d;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-btn.view {
  background: #e3f2fd;
  color: #1976d2;
}

.action-btn.view:hover {
  background: #bbdefb;
}

.action-btn.complete {
  background: #e8f5e8;
  color: #2e7d32;
}

.action-btn.complete:hover {
  background: #c8e6c9;
}

.action-btn.cancel {
  background: #fff3e0;
  color: #f57c00;
}

.action-btn.cancel:hover {
  background: #ffe0b2;
}

.action-btn.delete {
  background: #ffebee;
  color: #d32f2f;
}

.action-btn.delete:hover {
  background: #ffcdd2;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding: 20px;
}

.page-btn {
  padding: 8px 16px;
  border: 2px solid #e1e8ed;
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

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-number {
  width: 36px;
  height: 36px;
  border: 2px solid #e1e8ed;
  background: white;
  color: #2c3e50;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.page-number:hover {
  border-color: #3498db;
  color: #3498db;
}

.page-number.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

.page-info {
  font-size: 14px;
  color: #7f8c8d;
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
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e1e8ed;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  color: #6c757d;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #e9ecef;
  color: #495057;
}

.modal-body {
  padding: 24px;
}

/* 订单详情内容 */
.order-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-basic-info h4,
.order-parties-info h4,
.order-product-info h4,
.order-notes h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #e1e8ed;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: #7f8c8d;
  font-weight: 500;
}

.detail-item span {
  font-size: 14px;
  color: #2c3e50;
}

.detail-item .amount {
  font-weight: 600;
  color: #e74c3c;
  font-size: 16px;
}

/* 买卖双方信息 */
.order-parties-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.party-info {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.party-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.party-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.party-item label {
  font-size: 12px;
  color: #7f8c8d;
  font-weight: 500;
}

.party-item span {
  font-size: 14px;
  color: #2c3e50;
}

.confirmed {
  color: #27ae60;
  font-weight: 600;
}

.unconfirmed {
  color: #e74c3c;
  font-weight: 600;
}

/* 商品详情 */
.product-detail {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.product-detail-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.product-detail-info {
  flex: 1;
}

.product-detail-info h5 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.product-detail-info p {
  margin: 0 0 12px 0;
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.5;
}

.product-price {
  font-weight: 600;
  color: #e74c3c;
  font-size: 16px;
}

/* 备注信息 */
.note-item {
  margin-bottom: 16px;
}

.note-item label {
  display: block;
  font-size: 12px;
  color: #7f8c8d;
  font-weight: 500;
  margin-bottom: 4px;
}

.note-item p {
  margin: 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 14px;
  color: #2c3e50;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .filters {
    flex-wrap: wrap;
  }
  
  .orders-table {
    font-size: 12px;
  }
  
  .orders-table th,
  .orders-table td {
    padding: 8px 6px;
  }
  
  .product-main {
    flex-direction: column;
    gap: 8px;
  }
  
  .product-image {
    width: 100%;
    height: 120px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .order-parties-info {
    grid-template-columns: 1fr;
  }
  
  .product-detail {
    flex-direction: column;
  }
  
  .product-detail-image {
    width: 100%;
    height: 200px;
  }
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
