import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import messageService from './services/messageService'

const app = createApp(App)
app.use(router)
app.mount('#app')

// 将通知服务挂载到全局，方便 Login.vue 登录成功后调用重连
window.notificationService = messageService
