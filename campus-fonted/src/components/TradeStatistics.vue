<template>
  <div class="statistics-container">
    <div class="header">
      <button @click="goBack" class="back-btn">← 返回</button>
      <h1>交易统计</h1>
    </div>

    <div class="content">
      <!-- 统计概览卡片 -->
      <div class="stats-overview">
        <div class="stat-card">
          <div class="stat-icon">📈</div>
          <div class="stat-info">
            <div class="stat-label">成功交易</div>
            <div class="stat-value success">{{ tradeStats.successCount }}次</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">💰</div>
          <div class="stat-info">
            <div class="stat-label">交易总额</div>
            <div class="stat-value amount">
              <span v-if="showAmount">¥{{ tradeStats.totalAmount.toFixed(2) }}</span>
              <span v-else>¥****</span>
              <button @click="toggleAmount" class="toggle-btn" :title="showAmount ? '隐藏金额' : '显示金额'">
                {{ showAmount ? '👁️' : '👁️‍🗨️' }}
              </button>
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📦</div>
          <div class="stat-info">
            <div class="stat-label">已售商品</div>
            <div class="stat-value">{{ tradeStats.soldCount }}件</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🛒</div>
          <div class="stat-info">
            <div class="stat-label">已购商品</div>
            <div class="stat-value">{{ tradeStats.boughtCount }}件</div>
          </div>
        </div>
      </div>

      <!-- 交易记录统计 -->
      <div class="records-section">
        <h2>交易记录统计</h2>
        <div class="records-grid">
          <div class="record-item">
            <div class="record-label">待支付订单</div>
            <div class="record-value pending">{{ orderStats.pending }}</div>
          </div>
          <div class="record-item">
            <div class="record-label">待发货订单</div>
            <div class="record-value processing">{{ orderStats.processing }}</div>
          </div>
          <div class="record-item">
            <div class="record-label">已完成订单</div>
            <div class="record-value completed">{{ orderStats.completed }}</div>
          </div>
          <div class="record-item">
            <div class="record-label">已取消订单</div>
            <div class="record-value cancelled">{{ orderStats.cancelled }}</div>
          </div>
        </div>
      </div>

      <!-- 饼状图区域 -->
      <div class="charts-section">
        <div class="chart-card">
          <h3>订单状态分布</h3>
          <div class="pie-chart">
            <svg viewBox="0 0 200 200" class="pie-svg">
              <path
                v-for="(segment, index) in orderPieSegments"
                :key="index"
                :d="getArcPath(segment)"
                :fill="segment.color"
                class="pie-segment"
              />
              <circle cx="100" cy="100" r="50" fill="white" />
              <text x="100" y="95" text-anchor="middle" class="pie-center-text">总订单</text>
              <text x="100" y="115" text-anchor="middle" class="pie-center-number">{{ totalOrders }}</text>
            </svg>
          </div>
          <div class="pie-legend">
            <div v-for="(item, index) in orderPieLegend" :key="index" class="legend-item">
              <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-value">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <div class="chart-card">
          <h3>商品状态分布</h3>
          <div class="pie-chart">
            <svg viewBox="0 0 200 200" class="pie-svg">
              <path
                v-for="(segment, index) in productPieSegments"
                :key="index"
                :d="getArcPath(segment)"
                :fill="segment.color"
                class="pie-segment"
              />
              <circle cx="100" cy="100" r="50" fill="white" />
              <text x="100" y="95" text-anchor="middle" class="pie-center-text">总商品</text>
              <text x="100" y="115" text-anchor="middle" class="pie-center-number">{{ totalProducts }}</text>
            </svg>
          </div>
          <div class="pie-legend">
            <div v-for="(item, index) in productPieLegend" :key="index" class="legend-item">
              <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-value">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <div class="chart-card">
          <h3>交易类型分布</h3>
          <div class="pie-chart">
            <svg viewBox="0 0 200 200" class="pie-svg">
              <path
                v-for="(segment, index) in tradePieSegments"
                :key="index"
                :d="getArcPath(segment)"
                :fill="segment.color"
                class="pie-segment"
              />
              <circle cx="100" cy="100" r="50" fill="white" />
              <text x="100" y="95" text-anchor="middle" class="pie-center-text">交易数</text>
              <text x="100" y="115" text-anchor="middle" class="pie-center-number">{{ totalTrades }}</text>
            </svg>
          </div>
          <div class="pie-legend">
            <div v-for="(item, index) in tradePieLegend" :key="index" class="legend-item">
              <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '../api/request'
import { useSmartNavigation } from '../utils/navigation'

const router = useRouter()
const { goBack } = useSmartNavigation()
const showAmount = ref(false)

// 交易统计数据
const tradeStats = ref({
  successCount: 0,
  totalAmount: 0,
  soldCount: 0,
  boughtCount: 0
})

// 订单统计数据
const orderStats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  cancelled: 0
})

// 商品统计数据
const productStats = ref({
  onSale: 0,      // 在售
  sold: 0,        // 已售出
  removed: 0      // 已下架
})

// 计算总数
const totalOrders = computed(() => {
  return orderStats.value.pending + 
         orderStats.value.processing + 
         orderStats.value.completed + 
         orderStats.value.cancelled
})

const totalProducts = computed(() => {
  return productStats.value.onSale + 
         productStats.value.sold + 
         productStats.value.removed
})

const totalTrades = computed(() => {
  return tradeStats.value.soldCount + tradeStats.value.boughtCount
})

// 订单状态饼图数据
const orderPieSegments = computed(() => {
  const total = totalOrders.value || 1
  const data = [
    { value: orderStats.value.pending, color: '#faad14', label: '待支付' },
    { value: orderStats.value.processing, color: '#1890ff', label: '待发货' },
    { value: orderStats.value.completed, color: '#52c41a', label: '已完成' },
    { value: orderStats.value.cancelled, color: '#f5222d', label: '已取消' }
  ]
  
  let offset = 0
  return data.map(item => {
    const percentage = (item.value / total) * 100
    const segment = {
      ...item,
      percentage,
      offset
    }
    offset += percentage
    return segment
  })
})

const orderPieLegend = computed(() => [
  { label: '待支付', value: orderStats.value.pending, color: '#faad14' },
  { label: '待发货', value: orderStats.value.processing, color: '#1890ff' },
  { label: '已完成', value: orderStats.value.completed, color: '#52c41a' },
  { label: '已取消', value: orderStats.value.cancelled, color: '#f5222d' }
])

// 商品状态饼图数据
const productPieSegments = computed(() => {
  const total = totalProducts.value || 1
  const data = [
    { value: productStats.value.onSale, color: '#1890ff', label: '在售中' },
    { value: productStats.value.sold, color: '#52c41a', label: '已售出' },
    { value: productStats.value.removed, color: '#d9d9d9', label: '已下架' }
  ]
  
  let offset = 0
  return data.map(item => {
    const percentage = (item.value / total) * 100
    const segment = {
      ...item,
      percentage,
      offset
    }
    offset += percentage
    return segment
  })
})

const productPieLegend = computed(() => [
  { label: '在售中', value: productStats.value.onSale, color: '#1890ff' },
  { label: '已售出', value: productStats.value.sold, color: '#52c41a' },
  { label: '已下架', value: productStats.value.removed, color: '#d9d9d9' }
])

// 交易类型饼图数据
const tradePieSegments = computed(() => {
  const total = totalTrades.value || 1
  const data = [
    { value: tradeStats.value.soldCount, color: '#722ed1', label: '已售出' },
    { value: tradeStats.value.boughtCount, color: '#13c2c2', label: '已购买' }
  ]
  
  let offset = 0
  return data.map(item => {
    const percentage = (item.value / total) * 100
    const segment = {
      ...item,
      percentage,
      offset
    }
    offset += percentage
    return segment
  })
})

const tradePieLegend = computed(() => [
  { label: '已售出', value: tradeStats.value.soldCount, color: '#722ed1' },
  { label: '已购买', value: tradeStats.value.boughtCount, color: '#13c2c2' }
])

onMounted(async () => {
  await fetchStatistics()
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    let soldCount = 0
    let boughtCount = 0

    // 获取用户统计数据
    const statsResponse = await get('/auth/stats')
    if (statsResponse && statsResponse.code === 200) {
      const stats = statsResponse.data
      tradeStats.value.successCount = stats.successCount || 0
      tradeStats.value.totalAmount = stats.totalAmount || 0
    }

    // 获取订单统计数据（作为买家的订单）
    // 状态值：1=待确认, 2=进行中, 3=已完成, 4=已取消
    const ordersResponse = await get('/orders/my')
    if (ordersResponse && ordersResponse.code === 200) {
      const orders = ordersResponse.data || []
      
      // 按状态统计所有订单（买家+卖家）
      orderStats.value = {
        pending: orders.filter(o => o.status === 1).length,
        processing: orders.filter(o => o.status === 2).length,
        completed: orders.filter(o => o.status === 3).length,
        cancelled: orders.filter(o => o.status === 4).length
      }

      // 计算已购买商品数量（作为买家的已完成订单）
      boughtCount = orders.filter(o => o.status === 3).length
    }

    // 获取我的商品统计
    // 状态值：1=上架, 2=下架, 3=已售出, 0=已删除
    // 注意：/products/my 返回的是分页数据，需要获取所有商品
    const productsResponse = await get('/products/my?size=1000')
    
    if (productsResponse && productsResponse.code === 200) {
      // 从分页对象中提取商品列表
      const pageData = productsResponse.data || {}
      const products = pageData.content || []
      
      // 按状态统计商品
      productStats.value = {
        onSale: products.filter(p => p.status === 1).length,     // 在售中
        sold: products.filter(p => p.status === 3).length,       // 已售出
        removed: products.filter(p => p.status === 2).length     // 已下架
      }

      // 已售出商品数量
      soldCount = productStats.value.sold
    }

    // 更新交易统计中的售出和购买数量
    tradeStats.value.soldCount = soldCount
    tradeStats.value.boughtCount = boughtCount

  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 设置默认值
    tradeStats.value = {
      successCount: 0,
      totalAmount: 0,
      soldCount: 0,
      boughtCount: 0
    }
    orderStats.value = {
      pending: 0,
      processing: 0,
      completed: 0,
      cancelled: 0
    }
    productStats.value = {
      onSale: 0,
      sold: 0,
      removed: 0
    }
  }
}

const toggleAmount = () => {
  showAmount.value = !showAmount.value
}

// 绘制饼图扇形的路径
const getArcPath = (segment) => {
  if (!segment || segment.percentage === 0) {
    return ''
  }
  
  const cx = 100  // 中心x坐标
  const cy = 100  // 中心y坐标
  const r = 80    // 外半径
  const innerR = 50  // 内半径（做成环形图）
  
  // 如果是完整的圆（100%），特殊处理
  if (segment.percentage >= 99.9) {
    return `
      M ${cx} ${cy - r}
      A ${r} ${r} 0 1 1 ${cx} ${cy + r}
      A ${r} ${r} 0 1 1 ${cx} ${cy - r}
      M ${cx} ${cy - innerR}
      A ${innerR} ${innerR} 0 1 0 ${cx} ${cy + innerR}
      A ${innerR} ${innerR} 0 1 0 ${cx} ${cy - innerR}
      Z
    `
  }
  
  const startAngle = (segment.offset / 100) * 360
  const endAngle = ((segment.offset + segment.percentage) / 100) * 360
  
  // 转换为弧度
  const startRad = (startAngle - 90) * Math.PI / 180
  const endRad = (endAngle - 90) * Math.PI / 180
  
  // 计算外圆弧的起点和终点
  const x1 = cx + r * Math.cos(startRad)
  const y1 = cy + r * Math.sin(startRad)
  const x2 = cx + r * Math.cos(endRad)
  const y2 = cy + r * Math.sin(endRad)
  
  // 计算内圆弧的起点和终点
  const x3 = cx + innerR * Math.cos(endRad)
  const y3 = cy + innerR * Math.sin(endRad)
  const x4 = cx + innerR * Math.cos(startRad)
  const y4 = cy + innerR * Math.sin(startRad)
  
  // 判断是否为大弧
  const largeArc = segment.percentage > 50 ? 1 : 0
  
  // 构建路径
  const pathData = [
    `M ${x1} ${y1}`,  // 移动到外圆弧起点
    `A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2}`,  // 画外圆弧
    `L ${x3} ${y3}`,  // 连接到内圆弧终点
    `A ${innerR} ${innerR} 0 ${largeArc} 0 ${x4} ${y4}`,  // 画内圆弧（反向）
    'Z'  // 闭合路径
  ].join(' ')
  
  return pathData
}
</script>

<style scoped>
.statistics-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px 40px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.back-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 16px;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
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

.content {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 统计概览卡片 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 48px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 16px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-value.success {
  color: #52c41a;
}

.stat-value.amount {
  color: #fa8c16;
}

.toggle-btn {
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-size: 16px;
  margin-left: 8px;
}

.toggle-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.4);
  transform: scale(1.05);
}

/* 交易记录统计区域 */
.records-section {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 32px;
}

.records-section h2 {
  color: #333;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

/* 交易记录统计 */
.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.record-item {
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  text-align: center;
  transition: all 0.3s ease;
}

.record-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.record-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  font-weight: 500;
}

.record-value {
  font-size: 32px;
  font-weight: 700;
}

.record-value.pending {
  color: #faad14;
}

.record-value.processing {
  color: #1890ff;
}

.record-value.completed {
  color: #52c41a;
}

.record-value.cancelled {
  color: #f5222d;
}

/* 饼状图区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  margin-top: 32px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.chart-card h3 {
  color: #333;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
}

.pie-chart {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.pie-svg {
  width: 200px;
  height: 200px;
}

.pie-segment {
  transition: all 0.3s ease;
  cursor: pointer;
}

.pie-segment:hover {
  opacity: 0.8;
  filter: brightness(1.1);
  transform: scale(1.02);
  transform-origin: center;
}

.pie-center-text {
  font-size: 14px;
  fill: #666;
  font-weight: 500;
}

.pie-center-number {
  font-size: 24px;
  fill: #333;
  font-weight: 700;
}

.pie-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.legend-item:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateX(4px);
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.legend-value {
  font-size: 16px;
  color: #333;
  font-weight: 700;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
  }

  .header h1 {
    font-size: 24px;
  }

  .content {
    padding: 24px 20px;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    font-size: 32px;
  }

  .stat-value {
    font-size: 24px;
  }

  .records-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .records-grid {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .chart-card {
    padding: 20px;
  }

  .pie-svg {
    width: 160px;
    height: 160px;
  }
}
</style>

