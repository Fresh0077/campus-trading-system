<template>
  <div class="order-management">
    <!-- 顶部栏 -->
    <div class="page-header">
      <div class="header-left">
        <button @click="goBack" class="back-btn-header">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h1>订单管理</h1>
      </div>
      <div class="header-stats">
        <span class="stat-item">总订单 <strong>{{ totalOrders }}</strong></span>
        <span class="stat-item">待处理 <strong>{{ pendingOrders }}</strong></span>
        <span class="stat-item">已完成 <strong>{{ completedOrders }}</strong></span>
      </div>
    </div>
    
    <!-- Tab和筛选整合 -->
    <div class="control-bar">
      <div class="order-tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'all' }]"
          @click="switchTab('all')"
        >
          全部 ({{ totalOrders }})
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'buy' }]"
          @click="switchTab('buy')"
        >
          买入 ({{ buyOrders.length }})
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'sell' }]"
          @click="switchTab('sell')"
        >
          卖出 ({{ sellOrders.length }})
        </button>
      </div>
      
      <div class="filter-bar">
        <select v-model="statusFilter" @change="filterOrders" class="filter-select">
          <option value="">全部状态</option>
          <option value="pending">待确认</option>
          <option value="confirmed">已确认</option>
          <option value="shipping">配送中</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
        
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <path d="m21 21-4.35-4.35"></path>
          </svg>
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索订单..."
            @input="filterOrders"
          >
        </div>
      </div>
    </div>
    
    <div class="order-list">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载订单中...</p>
      </div>
      
      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <h3>暂无订单</h3>
        <p>{{ getEmptyMessage() }}</p>
        <router-link to="/products" class="browse-btn">去浏览商品</router-link>
      </div>
      
      <div v-else class="orders">
        <div 
          v-for="order in paginatedOrders" 
          :key="order.id"
          class="order-card"
        >
          <div class="order-main">
            <!-- 商品信息 -->
            <div class="product-section">
              <img 
                :src="getOrderProductImage(order.productImage)" 
                :alt="order.productName" 
                class="product-image"
                @error="handleImageError"
              >
              <div class="product-info">
                <h4 class="product-name">{{ order.productName }}</h4>
                <div class="order-meta">
                  <span class="order-number">{{ order.orderNumber }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 状态和价格 -->
            <div class="status-section">
              <span :class="['order-type', order.type]">
                {{ order.type === 'buy' ? '买入' : '卖出' }}
              </span>
              <span :class="['order-status', order.status]">
                {{ getStatusText(order.status) }}
              </span>
            </div>
            
            <div class="price-section">
              <div class="price-label">总价</div>
              <div class="price-value">¥{{ order.totalPrice.toFixed(2) }}</div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="action-section">
              <button 
                v-if="canConfirm(order)" 
                @click="confirmOrder(order)"
                class="action-btn confirm"
              >
                确认
              </button>
              <button 
                v-if="canCancel(order)" 
                @click="cancelOrder(order)"
                class="action-btn cancel"
              >
                取消
              </button>
              <button 
                v-if="canComplete(order)" 
                @click="completeOrder(order)"
                class="action-btn complete"
              >
                收货
              </button>
              <button 
                @click="viewOrderDetail(order)"
                class="action-btn detail"
              >
                详情
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="page-btn"
        >
          上一页
        </button>
        
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages"
            :key="page"
            @click="changePage(page)"
            :class="['page-btn', { active: page === currentPage }]"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          @click="changePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>
    
    <!-- 订单详情弹窗 -->
    <div v-if="showOrderDetail" class="order-detail-modal" @click="closeOrderDetail">
      <div class="detail-container" @click.stop>
        <div class="detail-header">
          <h2>订单详情</h2>
          <button @click="closeOrderDetail" class="close-btn">×</button>
        </div>
        
        <div class="detail-content" v-if="selectedOrder">
          <div class="detail-section">
            <h3>基本信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>订单号:</label>
                <span>{{ selectedOrder.orderNumber }}</span>
              </div>
              <div class="detail-item">
                <label>订单类型:</label>
                <span>{{ selectedOrder.type === 'buy' ? '买入' : '卖出' }}</span>
              </div>
              <div class="detail-item">
                <label>订单状态:</label>
                <span>{{ getStatusText(selectedOrder.status) }}</span>
              </div>
              <div class="detail-item">
                <label>创建时间:</label>
                <span>{{ formatTime(selectedOrder.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>商品信息</h3>
            <div class="product-detail">
              <img 
                :src="selectedOrder.productImage" 
                :alt="selectedOrder.productName"
                @error="handleImageError"
              >
              <div class="product-info">
                <h4>{{ selectedOrder.productName }}</h4>
                <p v-if="selectedOrder.productDescription">{{ selectedOrder.productDescription }}</p>
                <div class="product-meta">
                  <span v-if="selectedOrder.productCategory">分类: {{ selectedOrder.productCategory }}</span>
                  <span>数量: {{ selectedOrder.quantity }}</span>
                  <span>单价: ¥{{ selectedOrder.unitPrice.toFixed(2) }}</span>
                  <span>总价: ¥{{ selectedOrder.totalPrice.toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>联系信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>{{ selectedOrder.type === 'buy' ? '卖家姓名:' : '买家姓名:' }}</label>
                <span>{{ selectedOrder.contactName }}</span>
              </div>
              <div class="detail-item">
                <label>联系电话:</label>
                <span>{{ selectedOrder.contactPhone }}</span>
              </div>
              <div class="detail-item" v-if="selectedOrder.deliveryAddress">
                <label>配送地址:</label>
                <span>{{ selectedOrder.deliveryAddress }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedOrder.statusHistory && selectedOrder.statusHistory.length > 0">
            <h3>状态历史</h3>
            <div class="status-timeline">
              <div 
                v-for="(history, index) in selectedOrder.statusHistory" 
                :key="index"
                class="timeline-item"
              >
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-status">{{ getStatusText(history.status) }}</div>
                  <div class="timeline-time">{{ formatTime(history.timestamp) }}</div>
                  <div class="timeline-note" v-if="history.note">{{ history.note }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="detail-actions">
          <button 
            v-if="selectedOrder && canConfirm(selectedOrder)" 
            @click="confirmOrder(selectedOrder)"
            class="action-btn confirm"
          >
            确认订单
          </button>
          <button 
            v-if="selectedOrder && canCancel(selectedOrder)" 
            @click="cancelOrder(selectedOrder)"
            class="action-btn cancel"
          >
            取消订单
          </button>
          <button 
            v-if="selectedOrder && canComplete(selectedOrder)" 
            @click="completeOrder(selectedOrder)"
            class="action-btn complete"
          >
            确认收货
          </button>
          <button @click="closeOrderDetail" class="action-btn">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, put, patch } from '../api/request'
import modal from '../services/modal'
import { smartGoBack } from '../utils/navigation'
import { getImageUrl, handleImageError } from '../utils/imageUtils'

export default {
  name: 'OrderManagement',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const loading = ref(false)
    const orders = ref([])
    const activeTab = ref('all')
    const statusFilter = ref('')
    const timeFilter = ref('')
    const searchKeyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const showOrderDetail = ref(false)
    const selectedOrder = ref(null)
    
    // 计算属性
    const buyOrders = computed(() => orders.value.filter(order => order.type === 'buy'))
    const sellOrders = computed(() => orders.value.filter(order => order.type === 'sell'))
    const totalOrders = computed(() => orders.value.length)
    const completedOrders = computed(() => orders.value.filter(order => order.status === 'completed').length)
    const pendingOrders = computed(() => orders.value.filter(order => ['pending', 'confirmed'].includes(order.status)).length)
    const totalAmount = computed(() => orders.value.reduce((sum, order) => sum + order.totalPrice, 0))
    
    const filteredOrders = computed(() => {
      let result = orders.value
      
      // 按标签页筛选
      if (activeTab.value === 'buy') {
        result = result.filter(order => order.type === 'buy')
      } else if (activeTab.value === 'sell') {
        result = result.filter(order => order.type === 'sell')
      }
      
      // 按状态筛选
      if (statusFilter.value) {
        result = result.filter(order => order.status === statusFilter.value)
      }
      
      // 按时间筛选
      if (timeFilter.value) {
        const now = new Date()
        const filterDate = new Date()
        
        switch (timeFilter.value) {
          case 'today':
            filterDate.setHours(0, 0, 0, 0)
            break
          case 'week':
            filterDate.setDate(now.getDate() - 7)
            break
          case 'month':
            filterDate.setMonth(now.getMonth() - 1)
            break
          case 'quarter':
            filterDate.setMonth(now.getMonth() - 3)
            break
        }
        
        result = result.filter(order => new Date(order.createdAt) >= filterDate)
      }
      
      // 按关键词搜索
      if (searchKeyword.value.trim()) {
        const keyword = searchKeyword.value.toLowerCase()
        result = result.filter(order => 
          order.productName.toLowerCase().includes(keyword) ||
          order.orderNumber.toLowerCase().includes(keyword) ||
          order.contactName.toLowerCase().includes(keyword)
        )
      }
      
      return result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    })
    
    const totalPages = computed(() => Math.ceil(filteredOrders.value.length / pageSize.value))
    
    const paginatedOrders = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredOrders.value.slice(start, end)
    })
    
    const visiblePages = computed(() => {
      const pages = []
      const total = totalPages.value
      const current = currentPage.value
      
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
    
    // 辅助函数 - 处理商品图片URL（使用统一的工具函数）
    const getProductImage = (imageData) => {
      return getImageUrl(imageData, true)
    }
    
    // 获取订单商品图片
    const getOrderProductImage = (imageData) => {
      return getImageUrl(imageData, true)
    }

    const getOrderStatus = (status) => {
      // 【修复】后端订单状态：1=待确认, 2=进行中, 3=已完成, 4=已取消
      const statusMap = {
        1: 'pending',      // 待确认
        2: 'confirmed',    // 进行中
        3: 'completed',    // 已完成
        4: 'cancelled'     // 已取消
      }
      return statusMap[status] || 'pending'
    }
    
    // 生成订单状态历史
    const generateStatusHistory = (order) => {
      const history = []
      const createTime = order.createTime
      const updateTime = order.updateTime
      const cancelTime = order.cancelTime
      const completeTime = order.completeTime
      
      // 1. 订单创建
      history.push({
        status: 'pending',
        timestamp: createTime,
        note: '订单已创建，等待双方确认'
      })
      
      // 2. 根据确认状态添加记录
      if (order.buyerConfirm === 1 || order.sellerConfirm === 1) {
        let confirmNote = ''
        if (order.buyerConfirm === 1 && order.sellerConfirm === 1) {
          confirmNote = '买家和卖家均已确认'
        } else if (order.buyerConfirm === 1) {
          confirmNote = '买家已确认，等待卖家确认'
        } else if (order.sellerConfirm === 1) {
          confirmNote = '卖家已确认，等待买家确认'
        }
        
        history.push({
          status: 'confirmed',
          timestamp: updateTime,
          note: confirmNote
        })
      }
      
      // 3. 订单完成
      if (order.status === 3 && completeTime) {
        history.push({
          status: 'completed',
          timestamp: completeTime,
          note: '订单已完成，交易成功'
        })
      }
      
      // 4. 订单取消
      if (order.status === 4) {
        history.push({
          status: 'cancelled',
          timestamp: cancelTime || updateTime,
          note: order.cancelReason || '订单已取消'
        })
      }
      
      return history
    }

    // 方法
    const fetchOrders = async () => {
      try {
        loading.value = true
        
        // 并行获取买家和卖家订单
        const [buyerResponse, sellerResponse] = await Promise.all([
          get('/orders/buyer'),
          get('/orders/seller')
        ])
        
        const allOrders = []
        
        // 处理买家订单（【修复】使用正确的字段映射）
        if (buyerResponse.code === 200 && buyerResponse.data) {
          const buyOrders = buyerResponse.data.map(order => {
            const statusHistory = generateStatusHistory(order)
            return {
              id: order.id,
              orderNumber: order.orderNo,
              type: 'buy',
              status: getOrderStatus(order.status),
              productId: order.productId,
              productName: order.productTitle || '商品名称',
              productDescription: order.productDescription || '',
              productImage: getProductImage(order.productImage),
              productCategory: order.productCategory || '未分类',
              quantity: 1, // 二手交易固定为1
              unitPrice: order.amount || 0,
              totalPrice: order.amount || 0,
              contactName: order.sellerNickname || order.sellerUsername || '卖家',
              contactPhone: order.sellerPhone || '未提供',
              deliveryAddress: order.remark || '',
              notes: order.remark || '',
              buyerConfirm: order.buyerConfirm,
              sellerConfirm: order.sellerConfirm,
              createdAt: order.createTime,
              updatedAt: order.updateTime,
              statusHistory: statusHistory
            }
          })
          allOrders.push(...buyOrders)
        }
        
        // 处理卖家订单（【修复】使用正确的字段映射）
        if (sellerResponse.code === 200 && sellerResponse.data) {
          const sellOrders = sellerResponse.data.map(order => {
            const statusHistory = generateStatusHistory(order)
            return {
              id: order.id,
              orderNumber: order.orderNo,
              type: 'sell',
              status: getOrderStatus(order.status),
              productId: order.productId,
              productName: order.productTitle || '商品名称',
              productDescription: order.productDescription || '',
              productImage: getProductImage(order.productImage),
              productCategory: order.productCategory || '未分类',
              quantity: 1, // 二手交易固定为1
              unitPrice: order.amount || 0,
              totalPrice: order.amount || 0,
              contactName: order.buyerNickname || order.buyerUsername || '买家',
              contactPhone: order.buyerPhone || '未提供',
              deliveryAddress: order.remark || '',
              notes: order.remark || '',
              buyerConfirm: order.buyerConfirm,
              sellerConfirm: order.sellerConfirm,
              createdAt: order.createTime,
              updatedAt: order.updateTime,
              statusHistory: statusHistory
            }
          })
          allOrders.push(...sellOrders)
        }
        
        // 按创建时间排序
        orders.value = allOrders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        
      } catch (error) {
        console.error('获取订单失败:', error)
        orders.value = []
      } finally {
        loading.value = false
      }
    }
    

    
    const switchTab = (tab) => {
      activeTab.value = tab
      currentPage.value = 1
    }
    
    const filterOrders = () => {
      currentPage.value = 1
    }
    
    const resetFilters = () => {
      statusFilter.value = ''
      timeFilter.value = ''
      searchKeyword.value = ''
      currentPage.value = 1
    }
    
    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }
    
    const getStatusText = (status) => {
      const statusMap = {
        pending: '待确认',
        confirmed: '已确认',
        shipping: '配送中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || status
    }
    
    const getEmptyMessage = () => {
      if (activeTab.value === 'buy') {
        return '你还没有买入任何商品'
      } else if (activeTab.value === 'sell') {
        return '你还没有卖出任何商品'
      } else {
        return '你还没有任何订单'
      }
    }
    
    // 【修复】判断是否可以确认（只有待确认或进行中且自己未确认的订单才能确认）
    const canConfirm = (order) => {
      if (!['pending', 'confirmed'].includes(order.status)) return false
      
      // 买家：自己未确认时可以确认
      if (order.type === 'buy') {
        return order.buyerConfirm === 0
      }
      // 卖家：自己未确认时可以确认
      if (order.type === 'sell') {
        return order.sellerConfirm === 0
      }
      return false
    }
    
    const canCancel = (order) => {
      // 待确认或进行中的订单可以取消
      return ['pending', 'confirmed'].includes(order.status)
    }
    
    const canComplete = (order) => {
      // 已完成的订单不需要任何操作
      return false
    }
    
    // 【修复】确认订单（使用正确的API路径和HTTP方法）
    const confirmOrder = async (order) => {
      try {
        // 显示加载状态
        modal.loading('正在确认订单...', '处理中')
        
        let response
        
        // 根据订单类型调用不同的确认接口
        if (order.type === 'buy') {
          // 买家确认订单
          response = await patch(`/orders/${order.id}/confirm/buyer`)
        } else {
          // 卖家确认订单
          response = await patch(`/orders/${order.id}/confirm/seller`)
        }
        
        // 关闭加载弹窗
        modal.clear()
        
        if (response.code === 200) {
          // 重新加载订单列表以获取最新状态
          await fetchOrders()
          
          // 如果当前在详情弹窗中，确认成功后自动关闭弹窗
          if (showOrderDetail.value) {
            closeOrderDetail()
          }
          
          await modal.success('订单已成功确认！', '确认成功')
        } else {
          await modal.error(response.message || '确认订单失败，请重试', '确认失败')
        }
      } catch (error) {
        console.error('确认订单失败:', error)
        modal.clear()
        await modal.error('确认订单失败，请重试', '操作失败')
      }
    }
    
    // 【修复】取消订单（使用正确的API路径）
    const cancelOrder = async (order) => {
      // 确认取消操作
      const result = await modal.confirm(
        '确定要取消这个订单吗？取消后商品将重新上架。',
        '确认取消订单'
      )
      
      if (result !== 'confirm') return
      
      try {
        // 显示加载状态
        modal.loading('正在取消订单...', '处理中')
        
        const response = await patch(`/orders/${order.id}/cancel`, { reason: '用户取消' })
        
        // 关闭加载弹窗
        modal.clear()
        
        if (response.code === 200) {
          // 重新加载订单列表以获取最新状态
          await fetchOrders()
          
          await modal.success('订单已成功取消！商品已重新上架。', '取消成功')
        } else {
          await modal.error(response.message || '取消订单失败，请重试', '取消失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        modal.clear()
        await modal.error('取消订单失败，请重试', '操作失败')
      }
    }
    
    const completeOrder = async (order) => {
      // 确认完成操作
      const result = await modal.confirm(
        '确认已收到商品吗？完成后订单将标记为已完成。',
        '确认完成订单'
      )
      
      if (result !== 'confirm') return
      
      try {
        // 显示加载状态
        modal.loading('正在完成订单...', '处理中')
        
        const response = await put(`/api/orders/${order.id}/complete`)
        
        // 关闭加载弹窗
        modal.clear()
        
        if (response.code === 200) {
          // 更新本地订单状态
          order.status = 'completed'
          order.updatedAt = new Date().toISOString()
          
          // 添加状态历史
          if (!order.statusHistory) order.statusHistory = []
          order.statusHistory.push({
            status: 'completed',
            timestamp: new Date().toISOString(),
            note: '买家确认收货，订单完成'
          })
          
          await modal.success('订单已成功完成！感谢您的交易。', '完成成功')
        } else {
          await modal.error(response.message || '完成订单失败，请重试', '完成失败')
        }
      } catch (error) {
        console.error('完成订单失败:', error)
        modal.clear()
        await modal.error('完成订单失败，请重试', '操作失败')
      }
    }
    
    const viewOrderDetail = (order) => {
      selectedOrder.value = order
      showOrderDetail.value = true
    }
    
    const closeOrderDetail = () => {
      showOrderDetail.value = false
      selectedOrder.value = null
    }
    
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)
      return date.toLocaleString('zh-CN')
    }
    
    // 返回首页
    const goBack = () => {
      smartGoBack(router, '/home')
    }
    
    // 生命周期
    onMounted(() => {
      fetchOrders()
    })
    
    return {
      // 基础状态
      loading,
      orders,
      activeTab,
      statusFilter,
      timeFilter,
      searchKeyword,
      currentPage,
      pageSize,
      showOrderDetail,
      selectedOrder,
      buyOrders,
      sellOrders,
      totalOrders,
      completedOrders,
      pendingOrders,
      totalAmount,
      filteredOrders,
      totalPages,
      paginatedOrders,
      visiblePages,
      // 工具方法
      getOrderProductImage,
      fetchOrders,
      switchTab,
      filterOrders,
      resetFilters,
      changePage,
      getStatusText,
      getEmptyMessage,
      canConfirm,
      canCancel,
      canComplete,
      confirmOrder,
      cancelOrder,
      completeOrder,
      viewOrderDetail,
      closeOrderDetail,
      formatTime,
      handleImageError, // 从imageUtils导入
      goBack
    }
  }
}
</script>

<style scoped>
.order-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 20px 30px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
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
  background: linear-gradient(135deg, #374151, #6b7280);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-stats {
  display: flex;
  gap: 30px;
  align-items: center;
}

.stat-item {
  font-size: 14px;
  color: #666;
}

.stat-item strong {
  font-size: 20px;
  color: #374151;
  margin-left: 8px;
  font-weight: 700;
}

/* 控制栏 */
.control-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px 20px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.order-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab-btn:hover:not(.active) {
  background: rgba(102, 126, 234, 0.1);
  color: #374151;
}

.tab-btn.active {
  background: linear-gradient(135deg, #374151, #6b7280);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  padding: 8px 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  font-size: 14px;
  background: white;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #374151;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box svg {
  position: absolute;
  left: 12px;
  color: #999;
  pointer-events: none;
}

.search-box input {
  padding: 8px 16px 8px 36px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  font-size: 14px;
  background: white;
  width: 200px;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #374151;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
  width: 250px;
}

/* 订单列表 */
.order-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px;
  color: #7f8c8d;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(102, 126, 234, 0.2);
  border-top: 4px solid #374151;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
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
  padding: 80px;
  color: #7f8c8d;
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-state h3 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.empty-state p {
  color: #7f8c8d;
  font-size: 16px;
  margin-bottom: 24px;
}

.browse-btn {
  margin-top: 20px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #374151, #6b7280);
  color: white;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.browse-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

/* 订单卡片 */
.orders {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.order-main {
  display: grid;
  grid-template-columns: 2fr auto auto auto;
  gap: 20px;
  align-items: center;
}

/* 商品信息区域 */
.product-section {
  display: flex;
  gap: 15px;
  align-items: center;
}

.product-image {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.product-info {
  flex: 1;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.order-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.order-number {
  color: #374151;
}

.order-time {
  color: #999;
}

/* 状态区域 */
.status-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.order-type {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.order-type.buy {
  background: #e8f5e8;
  color: #2e7d32;
}

.order-type.sell {
  background: #fff3e0;
  color: #f57c00;
}

.order-status {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.order-status.pending {
  background: #fff3e0;
  color: #f57c00;
}

.order-status.confirmed {
  background: #e3f2fd;
  color: #1976d2;
}

.order-status.shipping {
  background: #f3e5f5;
  color: #7b1fa2;
}

.order-status.completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.order-status.cancelled {
  background: #ffebee;
  color: #d32f2f;
}

/* 价格区域 */
.price-section {
  text-align: right;
}

.price-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.price-value {
  font-size: 20px;
  font-weight: 700;
  color: #ff6b6b;
}

/* 操作按钮区域 */
.action-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
  min-width: 70px;
  text-align: center;
}

.action-btn.confirm {
  background: #4caf50;
  color: white;
}

.action-btn.confirm:hover {
  background: #45a049;
  transform: translateY(-1px);
}

.action-btn.cancel {
  background: #f44336;
  color: white;
}

.action-btn.cancel:hover {
  background: #da190b;
  transform: translateY(-1px);
}

.action-btn.complete {
  background: #2196f3;
  color: white;
}

.action-btn.complete:hover {
  background: #0b7dda;
  transform: translateY(-1px);
}

.action-btn.detail {
  background: #374151;
  color: white;
}

.action-btn.detail:hover {
  background: #5568d3;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .order-main {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .status-section,
  .price-section {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
  }
  
  .action-section {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .action-btn {
    flex: 1;
    min-width: 80px;
  }
}


/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  gap: 8px;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
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

/* 订单详情弹窗 */
.order-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
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

.detail-container {
  background: white;
  border-radius: 20px;
  max-width: 900px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
}

.detail-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: white;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.detail-content {
  padding: 32px;
  overflow-y: auto;
  flex: 1;
}

.detail-section {
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.detail-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
}

.detail-section h3 {
  margin: 0 0 20px 0;
  color: #374151;
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-section h3:before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  border-radius: 2px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.detail-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.detail-item label {
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.detail-item span {
  color: #333;
  font-weight: 500;
  text-align: right;
}

.product-detail {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.product-detail img {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.product-detail img:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.product-detail .product-info {
  flex: 1;
}

.product-detail .product-info h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 18px;
  font-weight: 700;
}

.product-detail .product-info p {
  margin: 0 0 16px 0;
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}

.product-detail .product-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}

.product-detail .product-meta span {
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 8px;
  color: #374151;
  font-weight: 500;
}

.status-timeline {
  position: relative;
  padding-left: 32px;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
}

.timeline-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: -24px;
  top: 18px;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #374151 0%, rgba(55, 65, 81, 0.2) 100%);
  border-radius: 2px;
}

.timeline-dot {
  position: absolute;
  left: -30px;
  top: 6px;
  width: 14px;
  height: 14px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  border-radius: 50%;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 2px 16px rgba(102, 126, 234, 0.8);
  }
}

.timeline-content {
  background: white;
  padding: 12px 16px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.timeline-content:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.timeline-status {
  font-weight: 700;
  color: #374151;
  font-size: 15px;
}

.timeline-time {
  color: #999;
  font-size: 13px;
  margin-top: 4px;
}

.timeline-note {
  color: #666;
  font-size: 13px;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px 32px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(118, 75, 162, 0.03) 100%);
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    padding: 15px 20px;
  }
  
  .header-stats {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
    width: 100%;
  }
  
  .control-bar {
    flex-direction: column;
    gap: 12px;
    padding: 15px;
  }
  
  .order-tabs {
    width: 100%;
    justify-content: space-between;
  }
  
  .tab-btn {
    flex: 1;
    padding: 8px 12px;
    font-size: 13px;
  }
  
  .filter-bar {
    width: 100%;
    flex-direction: column;
  }
  
  .filter-select,
  .search-box input {
    width: 100%;
  }
  
  .search-box input:focus {
    width: 100%;
  }
  
  .orders {
    padding: 15px;
  }
  
  .order-card {
    padding: 15px;
  }
  
  .product-section {
    flex-direction: column;
  }
  
  .product-image {
    width: 100%;
    height: 150px;
  }
  
  .detail-container {
    width: 95%;
    max-height: 95vh;
    margin: auto;
  }
  
  .detail-header {
    padding: 20px;
  }
  
  .detail-header h2 {
    font-size: 20px;
  }
  
  .detail-content {
    padding: 20px;
  }
  
  .detail-section {
    padding: 16px;
    margin-bottom: 20px;
  }
  
  .detail-section h3 {
    font-size: 16px;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .product-detail {
    flex-direction: column;
    padding: 16px;
  }
  
  .product-detail img {
    width: 100%;
    height: 200px;
  }
  
  .detail-actions {
    padding: 16px 20px;
    flex-wrap: wrap;
  }
  
  .detail-actions .action-btn {
    flex: 1;
    min-width: calc(50% - 6px);
  }
}
</style>