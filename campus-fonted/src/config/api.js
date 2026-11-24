/**
 * API配置文件
 */

// 获取API基础URL
const getApiBaseUrl = () => {
  const hostname = window.location.hostname
  
  // 开发环境
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8081/api/v1'
  }
  
  // 生产环境 - 使用当前协议和主机
  return `${window.location.protocol}//${window.location.host}/api/v1`
}

export const API_BASE_URL = getApiBaseUrl()

export default {
  API_BASE_URL
}
