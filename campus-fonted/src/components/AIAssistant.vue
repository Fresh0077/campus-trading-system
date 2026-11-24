<template>
  <div class="ai-assistant-container">
    <!-- 聊天窗口 -->
    <div v-if="isOpen" class="chat-window">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-left">
          <div class="ai-avatar">🤖</div>
          <div class="ai-info">
            <h3>{{ assistantInfo.name || 'AI助手' }}</h3>
            <span class="status">在线</span>
          </div>
        </div>
        <div class="header-actions">
          <button @click="clearHistory" class="icon-btn" title="清除对话">
            🗑️
          </button>
          <button @click="toggleWindow" class="icon-btn" title="最小化">
            ➖
          </button>
          <button @click="closeWindow" class="icon-btn" title="关闭">
            ✖️
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="ai-avatar-large">🤖</div>
          <h4>你好！我是{{ assistantInfo.name }}</h4>
          <p>{{ assistantInfo.description }}</p>
          <div class="quick-questions">
            <button 
              v-for="(q, index) in quickQuestions" 
              :key="index"
              @click="sendQuickQuestion(q)"
              class="quick-question-btn"
            >
              {{ q }}
            </button>
          </div>
        </div>

        <!-- 对话消息 -->
        <div 
          v-for="(msg, index) in messages" 
          :key="index"
          :class="['message', msg.role]"
        >
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="isLoading" class="message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input">
        <textarea
          v-model="inputMessage"
          @keydown.enter.exact.prevent="sendMessage"
          @keydown.enter.shift.exact="inputMessage += '\n'"
          placeholder="输入消息..."
          rows="1"
          ref="messageInput"
        ></textarea>
        <button 
          @click="sendMessage" 
          :disabled="!inputMessage.trim() || isLoading"
          class="send-btn"
          title="Enter发送，Shift+Enter换行"
        >
          <span v-if="!isLoading">发送</span>
          <span v-else>...</span>
        </button>
      </div>
    </div>

    <!-- 浮动按钮 -->
    <button 
      v-if="!isOpen" 
      @click="openWindow" 
      class="float-btn"
      :class="{ 'has-notification': hasNewMessage }"
    >
      <span class="float-icon">💬</span>
      <span class="float-text">AI助手</span>
      <span v-if="hasNewMessage" class="notification-dot"></span>
    </button>
  </div>
</template>

<script>
import { get, post, del } from '../api/request';

export default {
  name: 'AIAssistant',
  data() {
    return {
      isOpen: false,
      messages: [],
      inputMessage: '',
      isLoading: false,
      sessionId: null,
      assistantInfo: {
        name: 'AI助手',
        description: '我可以帮您解答关于平台使用、商品交易、订单处理等各类问题。'
      },
      hasNewMessage: false,
      quickQuestions: [
        '如何发布商品？',
        '如何购买商品？',
        '订单如何确认？',
        '如何联系卖家？'
      ]
    };
  },
  mounted() {
    this.loadAssistantInfo();
    // 从localStorage恢复会话
    const savedSession = localStorage.getItem('ai_session_id');
    if (savedSession) {
      this.sessionId = savedSession;
    }
  },
  methods: {
    async loadAssistantInfo() {
      try {
        const response = await get('/ai-assistant/info');
        if (response.code === 200) {
          this.assistantInfo = response.data;
        }
      } catch (error) {
        console.error('加载AI助手信息失败:', error);
      }
    },

    openWindow() {
      this.isOpen = true;
      this.hasNewMessage = false;
      this.$nextTick(() => {
        this.$refs.messageInput?.focus();
      });
    },

    closeWindow() {
      this.isOpen = false;
    },

    toggleWindow() {
      this.isOpen = !this.isOpen;
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isLoading) return;

      const userMessage = this.inputMessage.trim();
      this.inputMessage = '';

      // 添加用户消息
      this.messages.push({
        role: 'user',
        content: userMessage,
        timestamp: Date.now()
      });

      this.scrollToBottom();
      this.isLoading = true;

      try {
        const response = await post('/ai-assistant/chat', {
          message: userMessage,
          sessionId: this.sessionId,
          productId: this.$route?.params?.productId || null,
          orderId: this.$route?.params?.orderId || null
        });

        if (response.code === 200) {
          const aiResponse = response.data;
          
          // 保存会话ID
          if (aiResponse.sessionId) {
            this.sessionId = aiResponse.sessionId;
            localStorage.setItem('ai_session_id', aiResponse.sessionId);
          }

          // 添加AI回复
          this.messages.push({
            role: 'assistant',
            content: aiResponse.reply,
            timestamp: aiResponse.timestamp || Date.now()
          });

          this.scrollToBottom();
        } else {
          this.showError(response.message || '发送失败');
        }
      } catch (error) {
        console.error('发送消息失败:', error);
        // 更详细的错误信息
        let errorMsg = '网络错误，请检查：\n';
        errorMsg += '1. 后端服务是否启动\n';
        errorMsg += '2. 网络连接是否正常\n';
        errorMsg += '3. API地址是否正确';
        this.showError(errorMsg);
      } finally {
        this.isLoading = false;
      }
    },

    sendQuickQuestion(question) {
      this.inputMessage = question;
      this.sendMessage();
    },

    async clearHistory() {
      if (!confirm('确定要清除对话历史吗？')) return;

      if (this.sessionId) {
        try {
          await del(`/ai-assistant/conversation/${this.sessionId}`);
        } catch (error) {
          console.error('清除对话历史失败:', error);
        }
      }

      this.messages = [];
      this.sessionId = null;
      localStorage.removeItem('ai_session_id');
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    },

    formatMessage(content) {
      // 简单的Markdown格式化
      return content
        .replace(/\n/g, '<br>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`(.*?)`/g, '<code>$1</code>');
    },

    formatTime(timestamp) {
      const date = new Date(timestamp);
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${hours}:${minutes}`;
    },

    showError(message) {
      this.messages.push({
        role: 'assistant',
        content: `❌ ${message}`,
        timestamp: Date.now()
      });
      this.scrollToBottom();
    }
  }
};
</script>

<style scoped>
.ai-assistant-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

/* 浮动按钮 */
.float-btn {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  animation: float 3s ease-in-out infinite, glow 2s ease-in-out infinite;
}

.float-btn:hover {
  transform: scale(1.15);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.7);
}

.float-icon {
  font-size: 32px;
  margin-bottom: 2px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.float-text {
  font-size: 11px;
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.notification-dot {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 12px;
  height: 12px;
  background: #ff4757;
  border-radius: 50%;
  border: 2px solid white;
  animation: pulse 2s infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes glow {
  0%, 100% { 
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  }
  50% { 
    box-shadow: 0 6px 30px rgba(102, 126, 234, 0.8), 0 0 20px rgba(102, 126, 234, 0.4);
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

/* 聊天窗口 */
.chat-window {
  width: 600px;
  height: 800px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 头部 */
.chat-header {
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.ai-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.status {
  font-size: 12px;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 14px;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7f8fc;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
}

.ai-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin: 0 auto 16px;
}

.welcome-message h4 {
  margin: 0 0 8px 0;
  color: #2d3748;
  font-size: 18px;
}

.welcome-message p {
  color: #718096;
  font-size: 14px;
  margin: 0 0 20px 0;
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 20px;
}

.quick-question-btn {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: #4a5568;
  font-size: 14px;
  text-align: left;
}

.quick-question-btn:hover {
  background: #f7fafc;
  border-color: #374151;
  color: #374151;
  transform: translateX(4px);
}

/* 消息气泡 */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #374151;
}

.message-content {
  flex: 1;
  max-width: 75%;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  color: #2d3748;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message.user .message-text {
  background: #374151;
  color: white;
}

.message-time {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 4px;
  padding: 0 4px;
}

/* 输入打字动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e0;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
}

/* 输入框 */
.chat-input {
  padding: 18px;
  background: white;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
}

.chat-input textarea {
  flex: 1;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  font-size: 14px;
  resize: none;
  font-family: inherit;
  min-height: 44px;
  max-height: 120px;
  transition: border-color 0.2s;
}

.chat-input textarea:focus {
  outline: none;
  border-color: #374151;
}

.send-btn {
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  padding: 0 28px;
  min-height: 44px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-window {
    width: calc(100vw - 40px);
    height: calc(100vh - 100px);
  }
}
</style>
