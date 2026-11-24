/**
 * 智能导航工具
 * 提供更好的返回逻辑和用户体验
 */

import { useRouter } from 'vue-router'

/**
 * 页面路由关系配置
 * 定义每个页面的默认返回路径
 */
const PAGE_FALLBACKS = {
  // 商品相关
  '/products': '/home',
  '/product/:id': '/products',
  '/publish': '/products',
  '/my-products': '/home',
  
  // 订单和收藏
  '/orders': '/home',
  '/favorites': '/home',
  
  // 消息中心
  '/messages': '/home',
  
  // 统计页面
  '/trade-statistics': '/home',
  
  // 默认
  'default': '/home'
}

/**
 * 获取当前路由的默认返回路径
 */
function getDefaultFallback(currentPath) {
  // 处理动态路由（如 /product/123）
  if (currentPath.startsWith('/product/')) {
    return PAGE_FALLBACKS['/product/:id']
  }
  
  // 查找精确匹配
  if (PAGE_FALLBACKS[currentPath]) {
    return PAGE_FALLBACKS[currentPath]
  }
  
  // 返回默认值
  return PAGE_FALLBACKS['default']
}

/**
 * 智能返回逻辑
 * @param {Object} router - Vue Router 实例
 * @param {String} fallback - 可选的自定义返回路径
 * @param {Object} options - 其他选项
 */
export function smartGoBack(router, fallback = null, options = {}) {
  const { 
    replace = false,  // 是否使用 replace 而非 push
    force = false     // 是否强制使用 fallback
  } = options
  
  // 获取当前路由路径
  const currentPath = router.currentRoute.value.path
  
  // 如果强制使用 fallback
  if (force && fallback) {
    if (replace) {
      router.replace(fallback)
    } else {
      router.push(fallback)
    }
    return
  }
  
  // 检查是否有历史记录
  // 注意：window.history.length > 1 表示有历史记录
  // 但这不够准确，因为包含了整个浏览器历史
  // 更好的方式是检查 from 路由（在路由守卫中）
  
  // 尝试获取来源路由（如果有的话）
  const from = router.options.history.state.back
  
  if (from && from !== null && from !== currentPath) {
    // 有历史记录且不是当前页面，使用 back
    router.back()
  } else {
    // 没有历史记录，使用 fallback 或默认路径
    const targetPath = fallback || getDefaultFallback(currentPath)
    
    if (replace) {
      router.replace(targetPath)
    } else {
      router.push(targetPath)
    }
  }
}

/**
 * Vue Composable - 智能导航钩子
 * 在组件中使用
 */
export function useSmartNavigation() {
  const router = useRouter()
  
  /**
   * 智能返回
   * @param {String} fallback - 可选的自定义返回路径
   * @param {Object} options - 其他选项
   */
  const goBack = (fallback = null, options = {}) => {
    smartGoBack(router, fallback, options)
  }
  
  /**
   * 返回首页
   */
  const goHome = () => {
    router.push('/home')
  }
  
  /**
   * 返回商品列表
   */
  const goProducts = () => {
    router.push('/products')
  }
  
  /**
   * 返回上一页（强制使用 router.back）
   */
  const goBackForce = () => {
    router.back()
  }
  
  /**
   * 检查是否有历史记录可以返回
   */
  const canGoBack = () => {
    const from = router.options.history.state.back
    const currentPath = router.currentRoute.value.path
    return from && from !== null && from !== currentPath
  }
  
  /**
   * 导航到指定路径，如果是当前路径则返回上一页
   */
  const navigateOrBack = (path) => {
    const currentPath = router.currentRoute.value.path
    if (currentPath === path) {
      goBack()
    } else {
      router.push(path)
    }
  }
  
  return {
    goBack,
    goHome,
    goProducts,
    goBackForce,
    canGoBack,
    navigateOrBack
  }
}

/**
 * 面包屑导航辅助函数
 * 根据当前路由生成面包屑
 */
export function getBreadcrumbs(currentRoute) {
  const breadcrumbs = [
    { name: '首页', path: '/home' }
  ]
  
  const path = currentRoute.path
  const name = currentRoute.name
  
  // 根据路由添加面包屑
  if (path.startsWith('/product')) {
    breadcrumbs.push({ name: '商品列表', path: '/products' })
    if (path !== '/products') {
      breadcrumbs.push({ name: '商品详情', path })
    }
  } else if (path === '/publish') {
    breadcrumbs.push({ name: '发布商品', path })
  } else if (path === '/my-products') {
    breadcrumbs.push({ name: '我的商品', path })
  } else if (path === '/orders') {
    breadcrumbs.push({ name: '订单管理', path })
  } else if (path === '/favorites') {
    breadcrumbs.push({ name: '我的收藏', path })
  } else if (path === '/messages') {
    breadcrumbs.push({ name: '消息中心', path })
  } else if (path === '/trade-statistics') {
    breadcrumbs.push({ name: '交易统计', path })
  }
  
  return breadcrumbs
}

export default {
  smartGoBack,
  useSmartNavigation,
  getBreadcrumbs
}

