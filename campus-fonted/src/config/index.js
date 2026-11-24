/**
 * 应用配置 - 智能适配开发和生产环境
 * 支持域名和IP访问
 */

// 获取API基础URL
export const getApiBaseUrl = () => {
  const hostname = window.location.hostname
  const protocol = window.location.protocol
  const port = window.location.port
  
  // 开发环境
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8081/api/v1'
  }
  
  // 生产环境 - 使用相对路径，自动适配当前访问的域名或IP
  return '/api/v1'
}

// 获取完整的服务器地址（用于图片等资源）
export const getServerBaseUrl = () => {
  const hostname = window.location.hostname
  
  // 开发环境
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8081'
  }
  
  // 生产环境 - 使用当前协议和主机
  return `${window.location.protocol}//${window.location.host}`
}

// 获取WebSocket URL
export const getWebSocketUrl = () => {
  const hostname = window.location.hostname
  
  // 开发环境
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'ws://localhost:8081/api/v1/ws'
  }
  
  // 生产环境 - 强制使用 ws:// 协议（不使用 wss://）
  const host = window.location.host
  return `ws://${host}/api/v1/ws`
}

// 导出配置对象
export default {
  apiBaseUrl: getApiBaseUrl(),
  serverBaseUrl: getServerBaseUrl(),
  wsBaseUrl: getWebSocketUrl()
}

