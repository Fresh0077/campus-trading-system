<template>
  <div class="product-management">
    <div class="page-header">
      <h2>商品管理</h2>
      <p>管理平台上的所有商品信息</p>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          @input="handleSearch"
          placeholder="搜索商品标题..."
          class="search-input"
        />
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </div>
      
      <div class="filters">
        <select v-model="statusFilter" @change="loadProducts" class="filter-select">
          <option value="">全部状态</option>
          <option value="0">已删除</option>
          <option value="1">上架中</option>
          <option value="2">已下架</option>
          <option value="3">已售出</option>
        </select>
        
        <select v-model="sortBy" @change="loadProducts" class="filter-select">
          <option value="createTime">创建时间</option>
          <option value="updateTime">更新时间</option>
          <option value="price">价格</option>
          <option value="viewCount">浏览次数</option>
        </select>
        
        <select v-model="sortDir" @change="loadProducts" class="filter-select">
          <option value="desc">降序</option>
          <option value="asc">升序</option>
        </select>
      </div>
    </div>
    
    <!-- 批量操作 -->
    <div class="batch-actions" v-if="selectedProducts.length > 0">
      <span class="selected-count">已选择 {{ selectedProducts.length }} 个商品</span>
      <div class="batch-buttons">
        <button @click="batchUpdateStatus(1)" class="batch-btn approve">批量上架</button>
        <button @click="batchUpdateStatus(2)" class="batch-btn reject">批量下架</button>
        <button @click="selectedProducts = []" class="batch-btn cancel">取消选择</button>
      </div>
    </div>
    
    <!-- 商品列表 -->
    <div class="products-container">
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无商品</h3>
        <p>还没有找到符合条件的商品</p>
      </div>
      
      <div v-else class="products-table">
        <table>
          <thead>
            <tr>
              <th>
                <input 
                  type="checkbox" 
                  :checked="isAllSelected" 
                  @change="toggleSelectAll"
                />
              </th>
              <th>商品信息</th>
              <th>价格</th>
              <th>状态</th>
              <th>浏览次数</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.id" class="product-row">
              <td>
                <input 
                  type="checkbox" 
                  :value="product.id" 
                  v-model="selectedProducts"
                />
              </td>
              <td class="product-info">
                <div class="product-main">
                  <img 
                    :src="getProductImage(product)" 
                    :alt="product.title"
                    class="product-image"
                    @error="handleImageError"
                  />
                  <div class="product-details">
                    <h4 class="product-title">{{ product.title }}</h4>
                    <p class="product-description">{{ product.description || '暂无描述' }}</p>
                    <div class="product-meta">
                      <span class="seller">卖家: {{ product.username || '未知' }}</span>
                      <span class="category">分类: {{ product.categoryName || '未分类' }}</span>
                    </div>
                  </div>
                </div>
              </td>
              <td class="price">
                <div class="price-info">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
                </div>
              </td>
              <td class="status">
                <span :class="['status-badge', getStatusClass(product.status)]">
                  {{ getStatusText(product.status) }}
                </span>
              </td>
              <td class="view-count">{{ product.viewCount || 0 }}</td>
              <td class="create-time">{{ formatDate(product.createTime) }}</td>
              <td class="actions">
                <div class="action-buttons">
                  <button @click="viewProductDetail(product)" class="action-btn view" title="查看详情">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                  <button 
                    v-if="product.status !== 1" 
                    @click="updateProductStatusAction(product.id, 1)" 
                    class="action-btn approve" 
                    title="上架"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 6L9 17l-5-5"></path>
                    </svg>
                  </button>
                  <button 
                    v-if="product.status === 1" 
                    @click="updateProductStatusAction(product.id, 2)" 
                    class="action-btn reject" 
                    title="下架"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M18 6L6 18M6 6l12 12"></path>
                    </svg>
                  </button>
                  <button 
                    @click="deleteProduct(product.id)" 
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
    
    <!-- 商品详情弹窗 -->
    <div v-if="showProductDetail" class="modal-overlay" @click="closeProductDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>商品详情</h3>
          <button @click="closeProductDetail" class="close-btn">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body" v-if="selectedProduct">
          <div class="product-detail-content">
            <div class="product-images">
              <img 
                :src="getProductImage(selectedProduct)" 
                :alt="selectedProduct.title"
                class="main-image"
                @error="handleImageError"
              />
            </div>
            <div class="product-info-detail">
              <h4>{{ selectedProduct.title }}</h4>
              <p class="description">{{ selectedProduct.description }}</p>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>价格:</label>
                  <span>¥{{ selectedProduct.price }}</span>
                </div>
                <div class="detail-item" v-if="selectedProduct.originalPrice">
                  <label>原价:</label>
                  <span>¥{{ selectedProduct.originalPrice }}</span>
                </div>
                <div class="detail-item">
                  <label>状态:</label>
                  <span :class="['status-badge', getStatusClass(selectedProduct.status)]">
                    {{ getStatusText(selectedProduct.status) }}
                  </span>
                </div>
                <div class="detail-item">
                  <label>浏览次数:</label>
                  <span>{{ selectedProduct.viewCount || 0 }}</span>
                </div>
                <div class="detail-item">
                  <label>卖家:</label>
                  <span>{{ selectedProduct.username || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>分类:</label>
                  <span>{{ selectedProduct.categoryName || '未分类' }}</span>
                </div>
                <div class="detail-item">
                  <label>发布时间:</label>
                  <span>{{ formatDate(selectedProduct.createTime) }}</span>
                </div>
                <div class="detail-item">
                  <label>更新时间:</label>
                  <span>{{ formatDate(selectedProduct.updateTime) }}</span>
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
import { getProductList, updateProductStatus, forceDeleteProduct, batchUpdateProductStatus, getProductDetail } from '../../api/admin'
import { showToast } from '../../services/toast'
import { handleImageError } from '../../utils/imageUtils'

const products = ref([])
const selectedProducts = ref([])
const isLoading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const sortBy = ref('createTime')
const sortDir = ref('desc')
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)
const totalPages = ref(0)
const showProductDetail = ref(false)
const selectedProduct = ref(null)

let searchTimeout = null

// 计算属性
const isAllSelected = computed(() => {
  return products.value.length > 0 && selectedProducts.value.length === products.value.length
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

// 方法
const loadProducts = async () => {
  try {
    isLoading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDir: sortDir.value,
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value || undefined
    }
    
    const response = await getProductList(params)
    if (response.success && response.data) {
      products.value = response.data.content || []
      totalElements.value = response.data.totalElements || 0
      totalPages.value = response.data.totalPages || 0
    } else {
      showToast.error(response.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    showToast.error('获取商品列表失败')
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
    loadProducts()
  }, 500)
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedProducts.value = []
  } else {
    selectedProducts.value = products.value.map(p => p.id)
  }
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadProducts()
  }
}

const updateProductStatusAction = async (productId, status) => {
  try {
    const response = await updateProductStatus(productId, status)
    if (response.success) {
      showToast.success('商品状态更新成功')
      loadProducts()
    } else {
      showToast.error(response.message || '更新商品状态失败')
    }
  } catch (error) {
    console.error('更新商品状态失败:', error)
    showToast.error('更新商品状态失败')
  }
}

const deleteProduct = async (productId) => {
  if (!confirm('确定要删除这个商品吗？此操作不可恢复！')) {
    return
  }
  
  try {
    const response = await forceDeleteProduct(productId)
    if (response.success) {
      showToast.success('商品删除成功')
      loadProducts()
    } else {
      showToast.error(response.message || '删除商品失败')
    }
  } catch (error) {
    console.error('删除商品失败:', error)
    showToast.error('删除商品失败')
  }
}

const batchUpdateStatus = async (status) => {
  if (selectedProducts.value.length === 0) {
    showToast.warning('请先选择要操作的商品')
    return
  }
  
  const action = status === 1 ? '上架' : '下架'
  if (!confirm(`确定要批量${action}选中的 ${selectedProducts.value.length} 个商品吗？`)) {
    return
  }
  
  try {
    const response = await batchUpdateProductStatus(selectedProducts.value, status)
    if (response.success) {
      showToast.success(`批量${action}成功`)
      selectedProducts.value = []
      loadProducts()
    } else {
      showToast.error(response.message || `批量${action}失败`)
    }
  } catch (error) {
    console.error(`批量${action}失败:`, error)
    showToast.error(`批量${action}失败`)
  }
}

const viewProductDetail = async (product) => {
  try {
    const response = await getProductDetail(product.id)
    if (response.success && response.data) {
      selectedProduct.value = response.data.product
      showProductDetail.value = true
    } else {
      showToast.error(response.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    showToast.error('获取商品详情失败')
  }
}

const closeProductDetail = () => {
  showProductDetail.value = false
  selectedProduct.value = null
}

const getProductImage = (product) => {
  if (product.images && product.images.length > 0) {
    return product.images[0]
  }
  return '/placeholder.png'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '已删除',
    1: '上架中',
    2: '已下架',
    3: '已售出'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status) => {
  const classMap = {
    0: 'deleted',
    1: 'active',
    2: 'inactive',
    3: 'sold'
  }
  return classMap[status] || 'unknown'
}

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

// 生命周期
onMounted(() => {
  loadProducts()
})</script>

<style scoped>
.product-management {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 100%;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
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
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 40px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #374151;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  padding: 12px 16px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #374151;
}

/* 批量操作 */
.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 20px;
}

.selected-count {
  font-size: 14px;
  color: #4a5568;
  font-weight: 500;
}

.batch-buttons {
  display: flex;
  gap: 8px;
}

.batch-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.batch-btn.approve {
  background: #10b981;
  color: white;
}

.batch-btn.approve:hover {
  background: #059669;
}

.batch-btn.reject {
  background: #f59e0b;
  color: white;
}

.batch-btn.reject:hover {
  background: #d97706;
}

.batch-btn.cancel {
  background: #6b7280;
  color: white;
}

.batch-btn.cancel:hover {
  background: #4b5563;
}

/* 加载和空状态 */
.loading {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #374151;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #374151;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 商品表格 */
.products-table {
  overflow-x: auto;
}

.products-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.products-table th,
.products-table td {
  padding: 16px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.products-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.product-row:hover {
  background: #f9fafb;
}

.product-info {
  min-width: 300px;
}

.product-main {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
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
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-description {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.current-price {
  font-size: 16px;
  font-weight: 600;
  color: #dc2626;
}

.original-price {
  font-size: 12px;
  color: #9ca3af;
  text-decoration: line-through;
}

/* 状态标签 */
.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #dcfce7;
  color: #166534;
}

.status-badge.inactive {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.sold {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.deleted {
  background: #fee2e2;
  color: #991b1b;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn.view {
  background: #e0e7ff;
  color: #3730a3;
}

.action-btn.view:hover {
  background: #c7d2fe;
}

.action-btn.approve {
  background: #dcfce7;
  color: #166534;
}

.action-btn.approve:hover {
  background: #bbf7d0;
}

.action-btn.reject {
  background: #fef3c7;
  color: #92400e;
}

.action-btn.reject:hover {
  background: #fde68a;
}

.action-btn.delete {
  background: #fee2e2;
  color: #991b1b;
}

.action-btn.delete:hover {
  background: #fecaca;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding: 20px 0;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #d1d5db;
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
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-number:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.page-number.active {
  background: #374151;
  color: white;
  border-color: #374151;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
}

/* 弹窗 */
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
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  padding: 8px;
  border: none;
  background: none;
  cursor: pointer;
  color: #6b7280;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  max-height: calc(90vh - 80px);
}

.product-detail-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
}

.product-images {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.main-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.product-info-detail h4 {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.description {
  margin: 0 0 20px 0;
  color: #6b7280;
  line-height: 1.6;
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
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.detail-item span {
  font-size: 14px;
  color: #374151;
}

@media (max-width: 768px) {
  .search-filters {
    flex-direction: column;
  }
  
  .search-box {
    min-width: auto;
  }
  
  .batch-actions {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .batch-buttons {
    justify-content: center;
  }
  
  .products-table {
    font-size: 12px;
  }
  
  .product-main {
    flex-direction: column;
    gap: 8px;
  }
  
  .product-image {
    width: 100%;
    height: 120px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .product-detail-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
