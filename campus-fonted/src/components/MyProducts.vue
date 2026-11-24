<template>
  <div class="my-products">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <button @click="goBack" class="back-btn-header">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h1>我的商品</h1>
      </div>
      <router-link to="/publish" class="publish-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        发布新商品
      </router-link>
    </div>

    <!-- 状态筛选 -->
    <div class="status-tabs">
      <button 
        v-for="option in statusOptions" 
        :key="option.value"
        :class="['status-tab', { active: currentStatus === option.value }]"
        @click="currentStatus = option.value"
      >
        {{ option.label }} ({{ option.count }})
      </button>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-box">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input 
          v-model="searchKeyword" 
          type="text" 
          placeholder="搜索商品名称..."
          @input="handleSearch"
        >
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="products-section">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无商品</h3>
        <p>你还没有发布任何商品</p>
        <router-link to="/publish" class="publish-btn">发布第一个商品</router-link>
      </div>

      <div v-else class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-card">
          <div class="product-image">
            <img 
              :src="getProductImage(product)" 
              :alt="product.title || product.name" 
              @error="handleImageError"
            />
            <div class="product-status" :class="getProductStatus(product.status)">
              {{ getStatusText(getProductStatus(product.status)) }}
            </div>
          </div>
          
          <div class="product-info">
            <h3 class="product-title">{{ product.title || product.name }}</h3>
            <p class="product-price">¥{{ product.price }}</p>
            <div class="product-meta">
              <span class="product-category">{{ getCategoryName(product.categoryId) }}</span>
              <span class="views">浏览 {{ product.views || 0 }}</span>
            </div>
            <div class="product-date">
              <span class="date">{{ formatDate(product.createdAt) }}</span>
            </div>
          </div>
          
          <div class="product-actions">
            <button @click="viewProduct(product.id)" class="action-btn view">查看</button>
            <button @click="editProduct(product.id)" class="action-btn edit">编辑</button>
            <button 
              @click="toggleStatus(product)" 
              class="action-btn toggle"
              :class="{ active: getProductStatus(product.status) === 'active' }"
            >
              {{ getProductStatus(product.status) === 'active' ? '下架' : '上架' }}
            </button>
            <button @click="deleteProduct(product.id)" class="action-btn delete">删除</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="currentPage = currentPage - 1" 
          :disabled="currentPage <= 1"
          class="page-btn"
        >
          上一页
        </button>
        
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            @click="currentPage = page"
            :class="['page-btn', { active: currentPage === page }]"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          @click="currentPage = currentPage + 1" 
          :disabled="currentPage >= totalPages"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal-content" @click.stop>
        <h3>确认删除</h3>
        <p>确定要删除这个商品吗？删除后无法恢复。</p>
        <div class="modal-actions">
          <button @click="showDeleteModal = false" class="btn-cancel">取消</button>
          <button @click="confirmDelete" class="btn-confirm">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { get, patch, del } from '../api/request'
import { getImageUrl, handleImageError } from '../utils/imageUtils'
import modal from '../services/modal'
import { smartGoBack } from '../utils/navigation'

export default {
  name: 'MyProducts',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(false)
    const products = ref([])
    const currentStatus = ref('all')
    const searchKeyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const totalCount = ref(0)
    const showDeleteModal = ref(false)
    const deleteProductId = ref(null)
    
    // 状态选项
    const statusOptions = ref([
      { label: '全部', value: 'all', count: 0 },
      { label: '在售', value: 'active', count: 0 },
      { label: '已下架', value: 'inactive', count: 0 },
      { label: '草稿', value: 'draft', count: 0 }
    ])
    
    // 计算属性
    const totalPages = computed(() => {
      return Math.ceil(totalCount.value / pageSize.value)
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const start = Math.max(1, currentPage.value - 2)
      const end = Math.min(totalPages.value, start + 4)
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
    })
    
    // 获取商品数据
    const fetchProducts = async () => {
      try {
        loading.value = true
        
        const params = {
          page: currentPage.value - 1, // 后端使用0索引
          size: pageSize.value
        }
        
        // 添加状态筛选
        if (currentStatus.value !== 'all') {
          const statusMap = {
            'active': 1,
            'inactive': 2,
            'sold': 3,
            'draft': 0
          }
          params.status = statusMap[currentStatus.value]
        }
        
        // 添加搜索关键词
        if (searchKeyword.value.trim()) {
          params.title = searchKeyword.value.trim()
        }
        
        const response = await get('/products/my', params)
        
        if (response.code === 200) {
          products.value = response.data.content || []
          totalCount.value = response.data.totalElements || 0
        } else {
          console.error('获取商品失败:', response.message)
          products.value = []
          totalCount.value = 0
        }
      } catch (error) {
        console.error('获取商品失败:', error)
        products.value = []
        totalCount.value = 0
      } finally {
        loading.value = false
      }
    }
    
    // 获取所有商品用于统计（不分页）
    const fetchAllProductsForCount = async () => {
      try {
        console.log('开始获取全部商品统计...')
        
        // 获取大量商品以包含所有记录（设置size为1000）
        const response = await get('/products/my', { 
          page: 0, 
          size: 1000
        })
        
        console.log('全部商品响应:', response)
        
        if (response.code === 200 && response.data && response.data.content) {
          const allProducts = response.data.content
          console.log('加载了所有商品，数量:', allProducts.length)
          
          // 统计各状态数量
          const counts = {
            all: 0,
            active: 0,
            inactive: 0,
            sold: 0,
            draft: 0
          }
          
          allProducts.forEach(product => {
            counts.all++
            const status = getProductStatus(product.status)
            if (counts.hasOwnProperty(status)) {
              counts[status]++
            }
          })
          
          console.log('统计结果:', counts)
          
          // 更新状态选项
          statusOptions.value.forEach(option => {
            option.count = counts[option.value] || 0
          })
          
          console.log('最终状态选项:', statusOptions.value)
        } else {
          console.error('获取商品失败:', response)
          // 重置所有计数为0
          statusOptions.value.forEach(option => {
            option.count = 0
          })
        }
      } catch (error) {
        console.error('获取状态统计失败:', error)
        // 重置所有计数为0
        statusOptions.value.forEach(option => {
          option.count = 0
        })
      }
    }
    
    // 获取商品图片
    const getProductImage = (product) => {
      if (!product) return '/placeholder.png'
      
      // 优先使用 images 数组
      if (product.images) {
        return getImageUrl(product.images, true)
      }
      
      // 其次使用 image 字段
      if (product.image) {
        return getImageUrl(product.image, true)
      }
      
      return '/placeholder.png'
    }
    
    // 获取商品状态
    const getProductStatus = (status) => {
      const statusMap = {
        0: 'draft',     // 已删除/草稿
        1: 'active',    // 上架
        2: 'inactive',  // 下架
        3: 'sold'       // 已售出
      }
      return statusMap[status] || 'draft'
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      fetchProducts()
    }
    
    // 查看商品
    const viewProduct = (id) => {
      router.push(`/product/${id}`)
    }
    
    // 编辑商品
    const editProduct = (id) => {
      router.push(`/publish?id=${id}`)
    }
    
    // 切换商品状态
    const toggleStatus = async (product) => {
      try {
        const currentStatus = getProductStatus(product.status)
        const newStatus = currentStatus === 'active' ? 2 : 1 // 2: 下架, 1: 上架
        const statusText = newStatus === 1 ? '上架' : '下架'
        
        // 确认操作
        const result = await modal.confirm(
          `确定要${statusText}这个商品吗？`,
          `确认${statusText}`
        )
        
        if (result !== 'confirm') {
          return
        }
        
        // 显示加载状态
        modal.loading(`正在${statusText}商品...`, statusText)
        
        // 使用统一的 API 请求方法，第二个参数传 null 确保不发送 body
        const apiResult = await patch(`/products/${product.id}/status?status=${newStatus}`, null)
        
        // 关闭加载弹窗
        modal.clear()
        
        if (apiResult.code === 200) {
          product.status = newStatus
          await modal.success(
            `商品已${statusText}`,
            `${statusText}成功`
          )
          // 刷新列表和状态统计
          fetchProducts()
          fetchAllProductsForCount()
        } else {
          await modal.error(
            apiResult.message || `${statusText}失败，请重试`,
            `${statusText}失败`
          )
        }
      } catch (error) {
        console.error('切换状态失败:', error)
        modal.clear()
        await modal.error(
          '操作失败，请稍后重试',
          '操作失败'
        )
      }
    }
    
    // 删除商品
    const deleteProduct = (id) => {
      deleteProductId.value = id
      showDeleteModal.value = true
    }
    
    // 确认删除
    const confirmDelete = async () => {
      try {
        showDeleteModal.value = false
        
        // 显示加载状态
        modal.loading('正在删除商品...', '删除中')
        
        const response = await del(`/products/${deleteProductId.value}`)
        
        // 关闭加载弹窗
        modal.clear()
        
        if (response.code === 200) {
          deleteProductId.value = null
          await modal.success('商品已成功删除', '删除成功')
          // 重新获取数据和状态统计
          fetchProducts()
          fetchAllProductsForCount()
        } else {
          await modal.error(
            response.message || '删除失败，请重试',
            '删除失败'
          )
        }
      } catch (error) {
        console.error('删除商品失败:', error)
        modal.clear()
        await modal.error('删除失败，请稍后重试', '删除失败')
      }
    }
    
    const getStatusText = (status) => {
      const statusTextMap = {
        'active': '在售',
        'inactive': '已下架',
        'sold': '已售出',
        'draft': '草稿'
      }
      return statusTextMap[status] || '未知'
    }
    
    const getCategoryName = (categoryId) => {
      const categoryMap = {
        1: '电子产品',
        2: '图书教材',
        3: '生活用品',
        4: '服装配饰',
        5: '运动健身',
        6: '其他'
      }
      return categoryMap[categoryId] || '未分类'
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
    
    // 返回首页
    const goBack = () => {
      smartGoBack(router, '/home')
    }
    
    // 监听器
    watch([currentStatus, currentPage], () => {
      fetchProducts()
    })
    
    // 生命周期
    onMounted(() => {
      fetchProducts()
      fetchAllProductsForCount()
    })
    
    return {
      loading,
      products,
      currentStatus,
      searchKeyword,
      currentPage,
      totalPages,
      visiblePages,
      statusOptions,
      showDeleteModal,
      handleSearch,
      viewProduct,
      editProduct,
      toggleStatus,
      deleteProduct,
      confirmDelete,
      getStatusText,
      getCategoryName,
      formatDate,
      fetchProducts,
      getProductImage,
      getProductStatus,
      handleImageError,
      goBack
    }
  }
}
</script>

<style scoped>
.my-products {
  min-height: 100vh;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn-header {
  background: rgba(103, 126, 234, 0.1);
  border: 1px solid rgba(103, 126, 234, 0.3);
  color: #374151;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.back-btn-header:hover {
  background: #374151;
  color: white;
  transform: translateX(-2px);
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: #2196f3;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  transition: background-color 0.3s ease;
}

.publish-btn:hover {
  background: #1976d2;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.status-tab {
  padding: 12px 20px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s ease;
}

.status-tab.active {
  background: #2196f3;
  color: white;
}

.status-tab:hover:not(.active) {
  background: #f5f5f5;
}

.search-section {
  margin-bottom: 20px;
}

.search-box {
  position: relative;
  max-width: 400px;
}

.search-box svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.search-box input {
  width: 100%;
  padding: 12px 12px 12px 40px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.search-box input:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.products-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #666;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #2196f3;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #666;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: white;
}

.product-status.active {
  background: #4caf50;
}

.product-status.inactive {
  background: #ff9800;
}

.product-status.sold {
  background: #9e9e9e;
}

.product-status.draft {
  background: #f44336;
}

.product-info {
  padding: 16px;
}

.product-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
  line-height: 1.4;
}

.product-price {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #f44336;
  font-weight: 600;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  margin-bottom: 8px;
}

.product-category {
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.views {
  color: #999;
}

.product-date {
  display: flex;
  font-size: 12px;
}

.date {
  color: #999;
}

.product-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.action-btn.view {
  color: #2196f3;
  border-color: #2196f3;
}

.action-btn.view:hover {
  background: #2196f3;
  color: white;
}

.action-btn.edit {
  color: #ff9800;
  border-color: #ff9800;
}

.action-btn.edit:hover {
  background: #ff9800;
  color: white;
}

.action-btn.toggle {
  color: #4caf50;
  border-color: #4caf50;
}

.action-btn.toggle:hover {
  background: #4caf50;
  color: white;
}

.action-btn.toggle.active {
  color: #ff9800;
  border-color: #ff9800;
}

.action-btn.toggle.active:hover {
  background: #ff9800;
  color: white;
}

.action-btn.delete {
  color: #f44336;
  border-color: #f44336;
}

.action-btn.delete:hover {
  background: #f44336;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 8px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.page-btn:hover:not(:disabled) {
  background: #f5f5f5;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn.active {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

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
  padding: 24px;
  max-width: 400px;
  width: 90%;
}

.modal-content h3 {
  margin: 0 0 12px 0;
  color: #333;
}

.modal-content p {
  margin: 0 0 20px 0;
  color: #666;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-confirm {
  padding: 8px 16px;
  border: 1px solid #f44336;
  background: #f44336;
  color: white;
  border-radius: 6px;
  cursor: pointer;
}

.btn-confirm:hover {
  background: #d32f2f;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .status-tabs {
    flex-wrap: wrap;
  }
  
  .products-grid {
    grid-template-columns: 1fr;
  }
  
  .product-actions {
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
  
  .modal-actions {
    flex-wrap: wrap;
  }
  
  .action-btn {
    min-width: 0;
    flex: 1 1 calc(50% - 4px);
  }
}
</style>