import { API_BASE_URL } from '../config/api'

// 获取管理员token
const getAdminToken = () => {
  return localStorage.getItem('adminToken')
}

// 管理员请求头
const getAdminHeaders = () => {
  const token = getAdminToken()
  console.log('获取管理员token:', token)
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': token ? `Bearer ${token}` : ''
  }
  console.log('管理员请求头:', headers)
  return headers
}

// 通用请求函数
const adminRequest = async (url, options = {}) => {
  const config = {
    headers: getAdminHeaders(),
    ...options
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}${url}`, config)
    
    if (response.status === 401) {
      // Token过期，跳转到登录页
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminInfo')
      localStorage.removeItem('adminPermissions')
      window.location.href = '/admin/login'
      return { success: false, message: '登录已过期，请重新登录' }
    }
    
    const data = await response.json()
    
    // 转换ApiResponse格式为前端期望的格式
    if (data.code !== undefined) {
      return {
        success: data.code === 200,
        message: data.message,
        data: data.data,
        timestamp: data.timestamp
      }
    }
    
    return data
  } catch (error) {
    console.error('Admin API request error:', error)
    return {
      success: false,
      message: error.message || '网络请求失败'
    }
  }
}

// ==================== 管理员认证相关 ====================

/**
 * 管理员登录
 */
export const adminLogin = async (loginData) => {
  return await adminRequest('/admin/login', {
    method: 'POST',
    body: JSON.stringify(loginData)
  })
}

/**
 * 获取管理员信息
 */
export const getAdminInfo = async () => {
  return await adminRequest('/admin/info')
}

/**
 * 获取仪表盘统计数据
 */
export const getDashboardStats = async () => {
  return await adminRequest('/admin/dashboard/stats')
}

/**
 * 管理员登出
 */
export const adminLogout = async () => {
  return await adminRequest('/admin/logout', {
    method: 'POST'
  })
}

// ==================== 用户管理相关 ====================

/**
 * 获取用户列表
 */
export const getUserList = async (params = {}) => {
  const queryString = new URLSearchParams()
  
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      queryString.append(key, params[key])
    }
  })
  
  const url = `/admin/management/users${queryString.toString() ? '?' + queryString.toString() : ''}`
  return await adminRequest(url)
}

/**
 * 获取用户详情
 */
export const getUserDetail = async (userId) => {
  return await adminRequest(`/admin/management/users/${userId}`)
}

/**
 * 更新用户状态
 */
export const updateUserStatus = async (userId, status) => {
  return await adminRequest(`/admin/management/users/${userId}/status?status=${status}`, {
    method: 'PUT'
  })
}

/**
 * 批量更新用户状态
 */
export const batchUpdateUserStatus = async (userIds, status) => {
  return await adminRequest(`/admin/management/users/batch/status?status=${status}`, {
    method: 'PUT',
    body: JSON.stringify(userIds)
  })
}

// ==================== 商品管理相关 ====================

/**
 * 获取商品列表
 */
export const getProductList = async (params = {}) => {
  const queryString = new URLSearchParams()
  
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      queryString.append(key, params[key])
    }
  })
  
  const url = `/admin/management/products${queryString.toString() ? '?' + queryString.toString() : ''}`
  return await adminRequest(url)
}

/**
 * 获取商品详情
 */
export const getProductDetail = async (productId) => {
  return await adminRequest(`/admin/management/products/${productId}`)
}

/**
 * 更新商品状态
 */
export const updateProductStatus = async (productId, status) => {
  return await adminRequest(`/admin/management/products/${productId}/status?status=${status}`, {
    method: 'PUT'
  })
}

/**
 * 强制删除商品
 */
export const forceDeleteProduct = async (productId) => {
  return await adminRequest(`/admin/management/products/${productId}`, {
    method: 'DELETE'
  })
}

/**
 * 批量更新商品状态
 */
export const batchUpdateProductStatus = async (productIds, status) => {
  return await adminRequest(`/admin/management/products/batch/status?status=${status}`, {
    method: 'PUT',
    body: JSON.stringify(productIds)
  })
}

// ==================== 订单管理相关 ====================

/**
 * 获取订单列表
 */
export const getOrderList = async (params = {}) => {
  const queryString = new URLSearchParams()
  
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      queryString.append(key, params[key])
    }
  })
  
  const url = `/admin/management/orders${queryString.toString() ? '?' + queryString.toString() : ''}`
  return await adminRequest(url)
}

/**
 * 获取订单详情
 */
export const getOrderDetail = async (orderId) => {
  return await adminRequest(`/admin/management/orders/${orderId}`)
}

/**
 * 更新订单状态
 */
export const updateOrderStatus = async (orderId, status) => {
  return await adminRequest(`/admin/management/orders/${orderId}/status?status=${status}`, {
    method: 'PUT'
  })
}

/**
 * 删除订单
 */
export const deleteOrder = async (orderId) => {
  return await adminRequest(`/admin/management/orders/${orderId}`, {
    method: 'DELETE'
  })
}

// ==================== 分类管理相关 ====================


// ==================== 统计分析相关 ====================

/**
 * 获取系统统计信息
 */
export const getSystemStatistics = async () => {
  return await adminRequest('/admin/management/statistics/system')
}

/**
 * 获取用户活跃度统计
 */
export const getUserActivityStatistics = async (days = 7) => {
  return await adminRequest(`/admin/management/statistics/users?days=${days}`)
}


/**
 * 获取交易统计
 */
export const getTradeStatistics = async (days = 7) => {
  return await adminRequest(`/admin/management/statistics/trades?days=${days}`)
}

// ==================== 系统管理相关 ====================

/**
 * 获取系统日志
 */
export const getSystemLogs = async (params = {}) => {
  const queryString = new URLSearchParams()
  
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      queryString.append(key, params[key])
    }
  })
  
  const url = `/admin/management/logs${queryString.toString() ? '?' + queryString.toString() : ''}`
  return await adminRequest(url)
}

/**
 * 清理系统数据
 */
export const cleanupSystemData = async (days = 30) => {
  return await adminRequest(`/admin/management/cleanup?days=${days}`, {
    method: 'POST'
  })
}

/**
 * 获取缓存信息
 */
export const getCacheInfo = async () => {
  return await adminRequest('/cache/info')
}

/**
 * 清理缓存
 */
export const clearCache = async (cacheNames = []) => {
  return await adminRequest('/cache/clear', {
    method: 'POST',
    body: JSON.stringify(cacheNames)
  })
}

// ==================== 管理员管理相关 ====================

/**
 * 创建管理员
 */
export const createAdmin = async (adminData) => {
  return await adminRequest('/admin/create', {
    method: 'POST',
    body: JSON.stringify(adminData)
  })
}

/**
 * 获取管理员列表
 */
export const getAdminList = async (params = {}) => {
  const queryString = new URLSearchParams()
  
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      queryString.append(key, params[key])
    }
  })
  
  const url = `/admin/list${queryString.toString() ? '?' + queryString.toString() : ''}`
  return await adminRequest(url)
}

/**
 * 更新管理员状态
 */
export const updateAdminStatus = async (adminId, status) => {
  return await adminRequest(`/admin/${adminId}/status?status=${status}`, {
    method: 'PUT'
  })
}

/**
 * 删除管理员
 */
export const deleteAdmin = async (adminId) => {
  return await adminRequest(`/admin/${adminId}`, {
    method: 'DELETE'
  })
}
