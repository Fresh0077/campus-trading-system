/**
 * 图片URL处理工具
 */
import { getApiBaseUrl } from '@/config'

const API_BASE_URL = getApiBaseUrl()

/**
 * 获取图片完整URL
 * @param {string|array} images - 图片路径或图片数组
 * @param {boolean} getFirst - 如果是数组，是否只返回第一张
 * @returns {string} 完整的图片URL
 */
export function getImageUrl(images, getFirst = true) {
  // 如果是空值，返回占位图
  if (!images) {
    return '/placeholder.png'
  }
  
  // 如果是数组，处理数组
  if (Array.isArray(images)) {
    if (images.length === 0) {
      return '/placeholder.png'
    }
    
    if (getFirst) {
      return buildImageUrl(images[0])
    }
    
    return images.map(img => buildImageUrl(img))
  }
  
  // 如果是字符串，尝试解析JSON
  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) {
        return getImageUrl(parsed, getFirst)
      }
    } catch (e) {
      // 不是JSON，作为单个路径处理
    }
  }
  
  return buildImageUrl(images)
}

/**
 * 构建单个图片的完整URL
 * @param {string} imagePath - 图片路径
 * @returns {string} 完整的图片URL
 */
function buildImageUrl(imagePath) {
  if (!imagePath) {
    return '/placeholder.png'
  }
  
  // 转换为字符串并去除首尾空格和引号
  let cleanPath = String(imagePath).trim()
  if (cleanPath.startsWith('"') && cleanPath.endsWith('"')) {
    cleanPath = cleanPath.slice(1, -1)
  }
  if (cleanPath.startsWith("'") && cleanPath.endsWith("'")) {
    cleanPath = cleanPath.slice(1, -1)
  }

  // 处理历史遗留的简写头像字段，例如数据库中仅存储了 "avatar"、"default-avatar" 等
  // 统一映射为前端公共资源中的默认头像
  const lower = cleanPath.toLowerCase()
  if (lower === 'avatar' || lower === 'default-avatar' || lower === 'default_avatar') {
    return '/default-avatar.png'
  }

  // 如果是一个没有路径分隔符且不包含文件扩展名的短字符串（很可能是无效占位值），也回退到默认头像
  if (!cleanPath.includes('/') && !cleanPath.includes('.')) {
    return '/default-avatar.png'
  }
  
  // 如果已经是完整的HTTP URL（包括 MinIO URL），直接返回
  if (cleanPath.startsWith('http://') || cleanPath.startsWith('https://')) {
    return cleanPath
  }
  
  // 如果以 products、avatars、categories 等分类开头（MinIO 对象名称格式，如 avatars/xxx.jpg）
  // 通过后端 API 接口访问
  if (cleanPath.match(/^(products|avatars|categories|documents)\//)) {
    return `${API_BASE_URL}/files/view/${cleanPath}`
  }
  
  // 如果以 / 开头且是 public 目录下的文件（兼容旧数据）
  if (cleanPath.startsWith('/') && cleanPath.match(/^\/(avatars|products|categories|documents)\//)) {
    // 去掉开头的 /，然后通过后端 API 访问
    const extracted = cleanPath.substring(1)
    return `${API_BASE_URL}/files/view/${extracted}`
  }
  
  // 如果以 /api 或 /uploads 开头，说明是旧的 API 路径
  if (cleanPath.startsWith('/api/v1/files/view/')) {
    // 从 /api/v1/files/view/avatars/xxx.jpg 提取出 avatars/xxx.jpg
    const extracted = cleanPath.replace('/api/v1/files/view/', '')
    return `${API_BASE_URL}/files/view/${extracted}`
  }
  
  if (cleanPath.startsWith('/uploads/')) {
    // 从 /uploads/avatars/xxx.jpg 提取出 avatars/xxx.jpg
    const extracted = cleanPath.replace('/uploads/', '')
    return `${API_BASE_URL}/files/view/${extracted}`
  }
  
  // 其他情况，作为相对路径处理
  return cleanPath.startsWith('/') ? cleanPath : '/' + cleanPath
}

/**
 * 验证图片文件
 * @param {File} file - 文件对象
 * @returns {object} 验证结果 {valid: boolean, message: string}
 */
export function validateImageFile(file) {
  // 检查文件是否存在
  if (!file) {
    return { valid: false, message: '请选择图片文件' }
  }
  
  // 检查文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    return { valid: false, message: '只支持 JPG、PNG、GIF、WEBP 格式的图片' }
  }
  
  // 检查文件大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    return { valid: false, message: '图片大小不能超过 5MB' }
  }
  
  return { valid: true, message: '' }
}

/**
 * 图片加载错误处理
 * @param {Event} event - 图片加载错误事件
 */
export function handleImageError(event) {
  const target = event.target
  
  // 如果已经是占位图，不再处理，防止无限循环
  if (target.src.endsWith('/placeholder.png') || target.src.includes('placeholder.png')) {
    target.onerror = null
    return
  }
  
  // 标记图片已经失败，避免重复处理
  if (target.dataset.failed === 'true') {
    return
  }
  
  target.dataset.failed = 'true'
  target.src = '/placeholder.png'
  target.onerror = null // 移除错误处理，防止无限循环
  
  // 添加一个class用于样式标识
  target.classList.add('image-fallback')
}

/**
 * 创建图片懒加载观察器
 * @param {string} selector - 图片选择器，默认为 'img[data-src]'
 * @param {Object} options - IntersectionObserver选项
 * @returns {IntersectionObserver} 观察器实例
 */
export function createLazyImageObserver(selector = 'img[data-src]', options = {}) {
  const defaultOptions = {
    rootMargin: '50px', // 提前50px开始加载
    threshold: 0.1
  }
  
  const finalOptions = { ...defaultOptions, ...options }
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target
        const src = img.dataset.src
        
        if (src) {
          // 开始加载图片
          img.src = src
          img.onload = () => {
            img.classList.add('loaded')
            img.removeAttribute('data-src')
          }
          img.onerror = () => {
            handleImageError({ target: img })
          }
          
          // 停止观察已加载的图片
          observer.unobserve(img)
        }
      }
    })
  }, finalOptions)
  
  // 开始观察所有符合条件的图片
  document.querySelectorAll(selector).forEach(img => {
    observer.observe(img)
  })
  
  return observer
}

/**
 * 预加载图片
 * @param {string|array} urls - 图片URL或URL数组
 * @returns {Promise} 预加载Promise
 */
export function preloadImages(urls) {
  const urlArray = Array.isArray(urls) ? urls : [urls]
  
  const promises = urlArray.map(url => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => resolve(url)
      img.onerror = () => reject(new Error(`Failed to preload: ${url}`))
      img.src = url
    })
  })
  
  return Promise.allSettled(promises)
}

/**
 * 生成响应式图片URL
 * @param {string} originalUrl - 原始图片URL
 * @param {Object} options - 尺寸选项
 * @returns {string} 处理后的URL
 */
export function generateResponsiveUrl(originalUrl, options = {}) {
  const { width, height, quality = 80, format = 'auto' } = options
  
  if (!originalUrl || originalUrl.startsWith('data:')) {
    return originalUrl
  }
  
  // 如果是MinIO URL，可以添加参数进行动态调整
  if (originalUrl.includes('minio') || originalUrl.includes('/files/view/')) {
    const params = new URLSearchParams()
    
    if (width) params.append('w', width)
    if (height) params.append('h', height)
    if (quality !== 80) params.append('q', quality)
    if (format !== 'auto') params.append('f', format)
    
    const paramString = params.toString()
    return paramString ? `${originalUrl}?${paramString}` : originalUrl
  }
  
  return originalUrl
}

