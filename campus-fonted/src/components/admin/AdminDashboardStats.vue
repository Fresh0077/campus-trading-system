<template>
  <div class="dashboard-stats">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon users">👥</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
          <div class="stat-change positive">今日新增: {{ stats.todayNewUsers || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon products">📦</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalProducts || 0 }}</div>
          <div class="stat-label">总商品数</div>
          <div class="stat-change positive">今日新增: {{ stats.todayNewProducts || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orders">📋</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalOrders || 0 }}</div>
          <div class="stat-label">总订单数</div>
          <div class="stat-change positive">今日新增: {{ stats.todayNewOrders || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon revenue">💰</div>
        <div class="stat-info">
          <div class="stat-number">¥{{ formatMoney(stats.todayTransactionAmount) }}</div>
          <div class="stat-label">今日交易额</div>
          <div class="stat-change">已完成订单</div>
        </div>
      </div>
    </div>
    
    <!-- 状态统计 -->
    <div class="status-stats">
      <div class="status-section">
        <h3>用户状态</h3>
        <div class="status-items">
          <div class="status-item">
            <span class="status-label">活跃用户</span>
            <span class="status-value">{{ stats.activeUsers || 0 }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">在线用户</span>
            <span class="status-value online">{{ stats.onlineUsers || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="status-section">
        <h3>商品状态</h3>
        <div class="status-items">
          <div class="status-item">
            <span class="status-label">在售商品</span>
            <span class="status-value">{{ stats.availableProducts || 0 }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">已售商品</span>
            <span class="status-value sold">{{ stats.soldProducts || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="status-section">
        <h3>订单状态</h3>
        <div class="status-items">
          <div class="status-item">
            <span class="status-label">待处理</span>
            <span class="status-value pending">{{ stats.pendingOrders || 0 }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">已完成</span>
            <span class="status-value completed">{{ stats.completedOrders || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
    
<!--    &lt;!&ndash; 图表区域 &ndash;&gt;-->
<!--    <div class="charts-section">-->
<!--      <div class="chart-container">-->
<!--        <h3>用户活跃度趋势</h3>-->
<!--        <div class="chart-placeholder">-->
<!--          <div class="chart-bars">-->
<!--            <div v-for="(item, index) in stats.userActivityStats" :key="index" class="chart-bar">-->
<!--              <div class="bar" :style="{ height: getBarHeight(item.count, maxUserActivity) + '%' }"></div>-->
<!--              <div class="bar-label">{{ item.date }}</div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--      -->
<!--      <div class="chart-container">-->
<!--        <h3>交易金额趋势</h3>-->
<!--        <div class="chart-placeholder">-->
<!--          <div class="chart-bars">-->
<!--            <div v-for="(item, index) in stats.transactionStats" :key="index" class="chart-bar">-->
<!--              <div class="bar transaction" :style="{ height: getBarHeight(item.amount, maxTransactionAmount) + '%' }"></div>-->
<!--              <div class="bar-label">{{ item.date }}</div>-->
<!--            </div>-->
<!--          </div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->
<!--    -->
    <!-- 分类统计 -->
    <div class="category-stats" v-if="stats.categoryStats && stats.categoryStats.length > 0">
      <h3>分类统计</h3>
      <div class="category-list">
        <div v-for="category in stats.categoryStats" :key="category.categoryName" class="category-item">
          <div class="category-name">{{ category.categoryName }}</div>
          <div class="category-data">
            <span>商品: {{ category.productCount }}</span>
            <span>订单: {{ category.orderCount }}</span>
            <span>金额: ¥{{ formatMoney(category.totalAmount) }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 热门商品和最新用户 -->
    <div class="bottom-section">
      <div class="popular-products" v-if="stats.popularProducts && stats.popularProducts.length > 0">
        <h3>热门商品</h3>
        <div class="product-list">
          <div v-for="product in stats.popularProducts.slice(0, 5)" :key="product.id" class="product-item">
            <div class="product-info">
              <div class="product-title">{{ product.title }}</div>
              <div class="product-meta">
                <span class="product-price">¥{{ formatMoney(product.price) }}</span>
                <span class="product-views">{{ product.viewCount }} 浏览</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="recent-users" v-if="stats.recentUsers && stats.recentUsers.length > 0">
        <h3>最新用户</h3>
        <div class="user-list">
          <div v-for="user in stats.recentUsers.slice(0, 5)" :key="user.id" class="user-item">
            <div class="user-avatar">{{ user.realName ? user.realName.charAt(0) : 'U' }}</div>
            <div class="user-info">
              <div class="user-name">{{ user.realName || user.username }}</div>
              <div class="user-meta">
                <span class="user-id">{{ user.studentId }}</span>
                <span class="user-time">{{ formatTime(user.createTime) }}</span>
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
import { getDashboardStats } from '../../api/admin'
import { showToast } from '../../services/toast'

const stats = ref({})
const isLoading = ref(false)

onMounted(() => {
  loadDashboardStats()
})

const loadDashboardStats = async () => {
  isLoading.value = true
  try {
    const response = await getDashboardStats()
    if (response.success && response.data) {
      stats.value = response.data
    } else {
      showToast.show({
        message: response.message || '获取统计数据失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('Load dashboard stats error:', error)
    showToast.show({
      message: '获取统计数据失败',
      type: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

const maxUserActivity = computed(() => {
  if (!stats.value.userActivityStats) return 1
  return Math.max(...stats.value.userActivityStats.map(item => item.count)) || 1
})

const maxTransactionAmount = computed(() => {
  if (!stats.value.transactionStats) return 1
  return Math.max(...stats.value.transactionStats.map(item => parseFloat(item.amount || 0))) || 1
})

const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleDateString()
}

const getBarHeight = (value, max) => {
  if (!value || !max) return 0
  return Math.max((parseFloat(value) / parseFloat(max)) * 100, 5)
}
</script>

<style scoped>
.dashboard-stats {
  padding: 20px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 20px;
}

.stat-icon.users { background: linear-gradient(135deg, #374151, #6b7280); }
.stat-icon.products { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.orders { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-icon.revenue { background: linear-gradient(135deg, #43e97b, #38f9d7); }

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 12px;
  color: #27ae60;
}

.stat-change.positive {
  color: #27ae60;
}

/* 状态统计 */
.status-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.status-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-section h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.status-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ecf0f1;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  color: #7f8c8d;
  font-size: 14px;
}

.status-value {
  font-weight: 600;
  color: #2c3e50;
}

.status-value.online { color: #27ae60; }
.status-value.sold { color: #e74c3c; }
.status-value.pending { color: #f39c12; }
.status-value.completed { color: #27ae60; }

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chart-container h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: end;
  justify-content: center;
}

.chart-bars {
  display: flex;
  align-items: end;
  gap: 12px;
  height: 100%;
  width: 100%;
}

.chart-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar {
  width: 100%;
  background: linear-gradient(180deg, #374151, #6b7280);
  border-radius: 4px 4px 0 0;
  min-height: 10px;
  transition: all 0.3s ease;
}

.bar.transaction {
  background: linear-gradient(180deg, #43e97b, #38f9d7);
}

.bar-label {
  margin-top: 8px;
  font-size: 12px;
  color: #7f8c8d;
  text-align: center;
}

/* 分类统计 */
.category-stats {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.category-stats h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.category-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.category-item {
  padding: 16px;
  border: 1px solid #ecf0f1;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.category-item:hover {
  border-color: #3498db;
  background-color: #f8f9fa;
}

.category-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.category-data {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #7f8c8d;
}

/* 底部区域 */
.bottom-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.popular-products,
.recent-users {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.popular-products h3,
.recent-users h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.product-list,
.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-item,
.user-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ecf0f1;
}

.product-item:last-child,
.user-item:last-child {
  border-bottom: none;
}

.product-info,
.user-info {
  flex: 1;
}

.product-title,
.user-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.product-meta,
.user-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #7f8c8d;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db, #2980b9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  margin-right: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-stats {
    padding: 10px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .status-stats {
    grid-template-columns: 1fr;
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .bottom-section {
    grid-template-columns: 1fr;
  }
  
  .category-list {
    grid-template-columns: 1fr;
  }
}
</style>
