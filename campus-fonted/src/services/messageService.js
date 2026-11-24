// 仅保留系统通知功能的简化消息服务
import { Client } from '@stomp/stompjs'
import { getWebSocketUrl } from '../config'

class NotificationService {
  constructor() {
    this.client = null
    this.isConnected = false
    this.notificationHandlers = []
    this.connectionHandlers = []
  }

  // 初始化WebSocket连接（仅用于系统通知）
  async initialize() {
    try {
      console.log('初始化WebSocket连接（系统通知）...')

      // 获取token
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('未找到认证token，无法建立WebSocket连接')
        return false
      }

      // 计算后端 WS 地址
      const baseWsUrl = getWebSocketUrl()
      const wsUrl = `${baseWsUrl}?token=${encodeURIComponent(token)}`
      console.log('连接WebSocket地址:', wsUrl)

      // 创建STOMP客户端
      this.client = new Client({
        brokerURL: wsUrl,
        connectHeaders: {
          'Authorization': `Bearer ${token}`
        },
        reconnectDelay: 0,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        debug: (str) => {
          console.log('WebSocket Debug:', str)
        },
        onConnect: (frame) => {
          console.log('WebSocket连接成功')
          this.isConnected = true
          this.onConnected()
          this.connectionHandlers.forEach(handler => {
            try { handler(true) } catch (e) { console.error('连接回调错误:', e) }
          })
        },
        onDisconnect: () => {
          console.log('WebSocket连接断开')
          this.isConnected = false
          this.connectionHandlers.forEach(handler => {
            try { handler(false) } catch (e) { console.error('连接回调错误:', e) }
          })
        },
        onStompError: (frame) => {
          console.error('WebSocket错误:', frame)
        }
      })

      this.client.activate()
      return true
    } catch (error) {
      console.error('WebSocket初始化失败:', error)
      return false
    }
  }

  // 连接成功后订阅系统通知
  onConnected() {
    try {
      // 只订阅系统通知，不订阅私聊消息
      this.client.subscribe('/topic/notifications', (message) => {
        console.log('收到系统通知:', message.body)
        try {
          const payload = JSON.parse(message.body)
          this.handleNotification(payload)
        } catch (e) {
          console.error('解析系统通知失败:', e)
        }
      })

      console.log('系统通知订阅完成')
    } catch (error) {
      console.error('订阅系统通知失败:', error)
    }
  }

  // 处理系统通知
  handleNotification(data) {
    console.log('处理系统通知:', data)
    try {
      const mapped = this.mapNotification(data)
      this.notificationHandlers.forEach(handler => {
        try { handler(mapped) } catch (error) { console.error('通知处理器错误:', error) }
      })
    } catch (err) {
      console.error('处理系统通知失败:', err)
    }
  }

  // 订阅系统通知
  onNotification(handler) {
    this.notificationHandlers.push(handler)
    return () => {
      const index = this.notificationHandlers.indexOf(handler)
      if (index > -1) this.notificationHandlers.splice(index, 1)
    }
  }

  // 连接状态订阅
  onConnectionChange(handler) {
    this.connectionHandlers.push(handler)
    try { handler(this.isConnected) } catch (e) { console.error('连接回调错误:', e) }
    return () => {
      const index = this.connectionHandlers.indexOf(handler)
      if (index > -1) this.connectionHandlers.splice(index, 1)
    }
  }

  // 映射通知格式
  mapNotification(notification) {
    if (!notification || typeof notification !== 'object') return notification

    return {
      id: notification.id,
      type: notification.type || 'SYSTEM',
      title: notification.title,
      content: notification.content || notification.message,
      timestamp: notification.timestamp || Date.now(),
      read: notification.read || false,
      level: notification.level || 'info', // info, success, warning, error
      category: notification.category, // order, product, user, system
      relatedId: notification.relatedId,
      actionUrl: notification.actionUrl,
      actionText: notification.actionText
    }
  }

  // 断开连接
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.isConnected = false
      console.log('WebSocket连接已断开')
    }
  }

  // 重连
  async reconnect() {
    try {
      console.log('准备重连WebSocket...')
      if (this.client) {
        this.client.deactivate()
        this.client = null
        this.isConnected = false
      }
      return await this.initialize()
    } catch (e) {
      console.error('重连失败:', e)
      return false
    }
  }

  // 获取连接状态
  getConnectionStatus() {
    return this.isConnected
  }
}

// 创建单例
const notificationService = new NotificationService()

export default notificationService