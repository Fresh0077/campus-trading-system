<template>
  <div class="category-node">
    <div class="category-item" :class="{ 'selected': isSelected, 'disabled': category.status === 0 }">
      <div class="category-main">
        <div class="category-select">
          <input 
            type="checkbox" 
            :checked="isSelected"
            @change="$emit('select', category.id)"
            :id="`category-${category.id}`"
          >
        </div>
        
        <div class="category-info">
          <div class="category-name">
            <label :for="`category-${category.id}`">{{ category.name }}</label>
            <span v-if="category.status === 0" class="status-badge disabled">已禁用</span>
            <span v-else class="status-badge active">启用</span>
          </div>
          <div class="category-meta">
            <span class="sort-order">排序: {{ category.sortOrder || 0 }}</span>
            <span class="product-count">商品: {{ category.productCount || 0 }}</span>
            <span class="create-time">创建: {{ formatDate(category.createTime) }}</span>
          </div>
        </div>
      </div>
      
      <div class="category-actions">
        <button @click="$emit('edit', category)" class="action-btn edit" title="编辑">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
          </svg>
        </button>
        
        <button 
          @click="$emit('toggle-status', category.id)" 
          :class="['action-btn', category.status === 1 ? 'disable' : 'enable']"
          :title="category.status === 1 ? '禁用' : '启用'"
        >
          <svg v-if="category.status === 1" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <path d="M9 9l6 6M15 9l-6 6"></path>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
        </button>
        
        <button @click="$emit('delete', category.id)" class="action-btn delete" title="删除">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
          </svg>
        </button>
      </div>
    </div>
    
    <!-- 子分类 -->
    <div v-if="category.children && category.children.length > 0" class="category-children">
      <CategoryTreeNode
        v-for="child in category.children"
        :key="child.id"
        :category="child"
        :selected-categories="selectedCategories"
        @select="$emit('select', $event)"
        @edit="$emit('edit', $event)"
        @delete="$emit('delete', $event)"
        @toggle-status="$emit('toggle-status', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  category: {
    type: Object,
    required: true
  },
  selectedCategories: {
    type: Array,
    default: () => []
  }
})

defineEmits(['select', 'edit', 'delete', 'toggle-status'])

const isSelected = computed(() => {
  return props.selectedCategories.includes(props.category.id)
})

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.category-node {
  margin-bottom: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 2px solid #e1e8ed;
  border-radius: 8px;
  background: white;
  transition: all 0.3s ease;
}

.category-item:hover {
  border-color: #3498db;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.1);
}

.category-item.selected {
  border-color: #3498db;
  background: #f8fbff;
}

.category-item.disabled {
  opacity: 0.6;
  background: #f8f9fa;
}

.category-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.category-select input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.category-info {
  flex: 1;
}

.category-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.category-name label {
  font-weight: 600;
  color: #2c3e50;
  cursor: pointer;
  font-size: 16px;
}

.status-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.disabled {
  background: #f8d7da;
  color: #721c24;
}

.category-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #7f8c8d;
}

.category-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8f9fa;
  color: #6c757d;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-btn.edit {
  background: #e3f2fd;
  color: #1976d2;
}

.action-btn.edit:hover {
  background: #bbdefb;
}

.action-btn.enable {
  background: #e8f5e8;
  color: #2e7d32;
}

.action-btn.enable:hover {
  background: #c8e6c9;
}

.action-btn.disable {
  background: #fff3e0;
  color: #f57c00;
}

.action-btn.disable:hover {
  background: #ffe0b2;
}

.action-btn.delete {
  background: #ffebee;
  color: #d32f2f;
}

.action-btn.delete:hover {
  background: #ffcdd2;
}

.category-children {
  margin-left: 32px;
  margin-top: 8px;
  padding-left: 16px;
  border-left: 2px solid #e1e8ed;
}
</style>
