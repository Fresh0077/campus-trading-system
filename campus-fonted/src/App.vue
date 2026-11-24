<template>
  <div id="app">
    <router-view />
    <!-- AI智能客服助手 -->
    <AIAssistant v-if="isLoggedIn" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import AIAssistant from './components/AIAssistant.vue';

// 检查用户是否登录
const isLoggedIn = ref(false);

onMounted(() => {
  // 检查token是否存在
  const token = localStorage.getItem('token');
  isLoggedIn.value = !!token;
  
  // 监听storage变化，实时更新登录状态
  window.addEventListener('storage', () => {
    const token = localStorage.getItem('token');
    isLoggedIn.value = !!token;
  });
});
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  width: 100%;
  height: 100%;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

body {
  background-color: #f5f5f5;
}
</style>
