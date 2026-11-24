<template>
  <div class="publish-container">
    <div class="publish-header">
      <div class="header-left">
        <button @click="goBack" class="back-btn-header">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h1>{{ isEditMode ? '编辑商品' : '发布商品' }}</h1>
      </div>
    </div>

    <div class="publish-form-container">
      <form @submit.prevent="handleSubmit" class="publish-form">
        <!-- 商品基本信息 -->
        <div class="form-section">
          <div class="section-header">
            <div class="section-icon">📝</div>
            <h2>商品基本信息</h2>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="name">商品名称 <span class="required">*</span></label>
              <input
                  id="name"
                  v-model="form.name"
                  type="text"
                  placeholder="请输入商品名称"
                  maxlength="50"
                  required
              />
              <span class="char-count">{{ form.name.length }}/50</span>
            </div>

            <div class="form-group">
              <label for="category">商品分类 <span class="required">*</span></label>
              <select id="category" v-model="form.category" required>
                <option value="">请选择分类</option>
                <option value="1">电子产品</option>
                <option value="2">图书教材</option>
                <option value="3">生活用品</option>
                <option value="4">服装配饰</option>
                <option value="5">运动健身</option>
                <option value="6">其他</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="price">商品价格 <span class="required">*</span></label>
              <div class="price-input">
                <span class="currency">¥</span>
                <input
                    id="price"
                    v-model="form.price"
                    type="number"
                    placeholder="0.00"
                    min="0"
                    step="0.01"
                    required
                />
              </div>
            </div>

            <div class="form-group">
              <label for="originalPrice">原价（可选）</label>
              <div class="price-input">
                <span class="currency">¥</span>
                <input
                    id="originalPrice"
                    v-model="form.originalPrice"
                    type="number"
                    placeholder="0.00"
                    min="0"
                    step="0.01"
                />
              </div>
            </div>
          </div>

          <div class="form-group full-width">
            <label for="description">商品描述</label>
            <textarea
                id="description"
                v-model="form.description"
                placeholder="请详细描述商品的特点、使用情况等"
                maxlength="500"
                rows="5"
            ></textarea>
            <span class="char-count">{{ form.description.length }}/500</span>
          </div>
        </div>

        <!-- 商品图片 -->
        <div class="form-section">
          <div class="section-header">
            <div class="section-icon">📷</div>
            <h2>商品图片</h2>
            <p class="section-desc">最多上传5张图片，第一张将作为封面图片</p>
          </div>

          <div class="image-upload-area">
            <div class="image-list">
              <div
                  v-for="(image, index) in form.images"
                  :key="index"
                  class="image-item"
              >
                <img :src="image.preview" :alt="`商品图片 ${index + 1}`"/>
                <div class="image-overlay">
                  <button type="button" @click="removeImage(index)" class="remove-btn">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path
                          d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14zM10 11v6M14 11v6"/>
                    </svg>
                  </button>
                  <span v-if="index === 0" class="cover-badge">封面</span>
                </div>
              </div>

              <div
                  v-if="form.images.length < 5"
                  class="upload-placeholder"
                  @click="triggerFileInput"
              >
                <div class="upload-icon">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 5v14M5 12h14"/>
                  </svg>
                </div>
                <span>添加图片</span>
              </div>
            </div>

            <input
                ref="fileInput"
                type="file"
                accept="image/*"
                multiple
                @change="handleImageUpload"
                style="display: none"
            />
          </div>
        </div>

        <!-- 联系方式 -->
        <div class="form-section">
          <div class="section-header">
            <div class="section-icon">📞</div>
            <h2>联系方式</h2>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="contactPhone">联系电话</label>
              <input
                  id="contactPhone"
                  v-model="form.contactPhone"
                  type="tel"
                  placeholder="请输入联系电话"
              />
            </div>

            <div class="form-group">
              <label for="contactEmail">联系邮箱</label>
              <input
                  id="contactEmail"
                  v-model="form.contactEmail"
                  type="email"
                  placeholder="请输入联系邮箱"
              />
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <button v-if="!isEditMode" type="button" @click="saveDraft" class="draft-btn" :disabled="loading">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
              <polyline points="17,21 17,13 7,13 7,21"/>
              <polyline points="7,3 7,8 15,8"/>
            </svg>
            保存草稿
          </button>
          <button type="submit" class="submit-btn" :disabled="loading">
            <svg v-if="!loading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2">
              <path d="m22 2-7 20-4-9-9-4 20-7z"/>
            </svg>
            <div v-else class="loading-spinner"></div>
            {{ loading ? (isEditMode ? '更新中...' : '发布中...') : (isEditMode ? '保存更改' : '立即发布') }}
          </button>
        </div>
      </form>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"/>
        <line x1="15" y1="9" x2="9" y2="15"/>
        <line x1="9" y1="9" x2="15" y2="15"/>
      </svg>
      {{ errorMessage }}
    </div>

    <!-- 成功提示 -->
    <div v-if="successMessage" class="success-message">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
        <polyline points="22,4 12,14.01 9,11.01"/>
      </svg>
      {{ successMessage }}
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {post, get, put} from '../api/request'
import modal from '../services/modal'
import { getImageUrl } from '../utils/imageUtils'
import { useSmartNavigation } from '../utils/navigation'
import { getApiBaseUrl } from '../config'

const router = useRouter()
const route = useRoute()
const { goBack: smartGoBack } = useSmartNavigation()
const fileInput = ref(null)

// 编辑模式
const isEditMode = ref(false)
const productId = ref(null)

// 表单数据
const form = reactive({
  name: '',
  category: '',
  price: '',
  originalPrice: '',
  description: '',
  images: [],
  contactPhone: '',
  contactEmail: ''
})

// 状态管理
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// 加载商品数据
const loadProductData = async () => {
  try {
    loading.value = true
    const response = await get(`/products/${productId.value}`)
    
    if (response.code === 200 && response.data) {
      const product = response.data
      
      // 填充表单数据
      form.name = product.title || product.name || ''
      form.category = product.categoryId ? String(product.categoryId) : ''
      form.price = product.price || ''
      form.originalPrice = product.originalPrice || ''
      form.description = product.description || ''
      
      // 处理图片
      if (product.images) {
        const imageUrls = Array.isArray(product.images) ? product.images : [product.images]
        form.images = imageUrls.map((url, index) => ({
          file: null, // 已存在的图片没有 file 对象
          preview: getImageUrl(url, true),
          url: url, // 保存原始URL，用于提交时判断
          name: `image-${index}`,
          isExisting: true // 标记为已存在的图片
        }))
      }
      
      console.log('商品数据加载成功:', product)
    } else {
      await modal.error('无法加载商品数据', '加载失败')
      router.push('/my-products')
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    await modal.error('加载商品数据失败', '错误')
    router.push('/my-products')
  } finally {
    loading.value = false
  }
}

// 方法
const goBack = () => {
  // 根据编辑模式返回不同的页面
  const fallback = isEditMode.value ? '/my-products' : '/products'
  smartGoBack(fallback)
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleImageUpload = (event) => {
  const files = Array.from(event.target.files)
  const remainingSlots = 5 - form.images.length
  const filesToProcess = files.slice(0, remainingSlots)

  filesToProcess.forEach(file => {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      errorMessage.value = '请选择图片文件'
      return
    }

    // 验证文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      errorMessage.value = '图片大小不能超过5MB'
      return
    }

    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      form.images.push({
        file: file,
        preview: e.target.result,
        name: file.name
      })
    }
    reader.readAsDataURL(file)
  })

  // 清空input
  event.target.value = ''
}

const removeImage = (index) => {
  form.images.splice(index, 1)
}

const validateForm = () => {
  if (!form.name.trim()) {
    errorMessage.value = '请输入商品名称'
    return false
  }

  if (!form.category) {
    errorMessage.value = '请选择商品分类'
    return false
  }

  if (!form.price || form.price <= 0) {
    errorMessage.value = '请输入有效的商品价格'
    return false
  }

  if (form.originalPrice && form.originalPrice <= form.price) {
    errorMessage.value = '原价应该大于现价'
    return false
  }

  if (form.images.length === 0) {
    errorMessage.value = '请至少上传一张商品图片'
    return false
  }

  return true
}

const uploadImages = async () => {
  const uploadedImages = []

  for (const imageData of form.images) {
    // 如果是已存在的图片（编辑模式下的原图），直接使用原URL
    if (imageData.isExisting && imageData.url) {
      uploadedImages.push(imageData.url)
      continue
    }
    
    // 如果是新上传的图片，需要上传
    if (imageData.file) {
      const formData = new FormData()
      formData.append('file', imageData.file)

      try {
        // 直接使用fetch进行文件上传，避开request.js中的JSON序列化
        const uploadUrl = `${getApiBaseUrl()}/files/upload/image`
        const response = await fetch(uploadUrl, {
          method: 'POST',
          body: formData,
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });

        const result = await response.json();

        if (response.ok && result.code === 200) {
          // 只推入文件路径字符串，而不是整个对象
          uploadedImages.push(result.data.filePath);
        } else {
          throw new Error(result.message || '图片上传失败');
        }
      } catch (error) {
        console.error('上传图片错误:', error);
        throw new Error(`图片上传失败: ${error.message}`);
      }
    }
  }

  return uploadedImages
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!validateForm()) {
    return
  }

  loading.value = true

  // 显示加载状态
  const actionText = isEditMode.value ? '更新' : '发布'
  const loadingModal = modal.loading(`正在${actionText}商品，请稍候...`, `${actionText}中`)

  try {
    console.log(`开始${actionText}商品，表单数据：`, form)

    // 上传图片
    const imageUrls = await uploadImages()

    // 准备提交数据
    const submitData = {
      title: form.name.trim(),
      categoryId: form.category ? parseInt(form.category) : null,
      price: parseFloat(form.price),
      originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
      description: form.description.trim(),
      images: imageUrls
    }

    console.log('提交商品数据：', submitData)

    let response
    if (isEditMode.value) {
      // 编辑模式：使用 PUT 方法更新商品
      response = await put(`/products/${productId.value}`, submitData)
    } else {
      // 新建模式：使用 POST 方法创建商品
      response = await post('/products', submitData)
    }

    // 关闭加载弹窗
    modal.clear()

    if (response.code === 200) {
      await modal.success(`商品${actionText}成功！即将跳转到我的商品页面`, `${actionText}成功`)
      console.log(`商品${actionText}成功：`, response.data)

      // 跳转到我的商品列表
      router.push('/my-products')
    } else {
      await modal.error(response.message || `${actionText}失败，请重试`, `${actionText}失败`)
      console.error(`商品${actionText}失败：`, response.message)
    }
  } catch (error) {
    console.error(`${actionText}商品出错：`, error)
    // 关闭加载弹窗
    modal.clear()
    await modal.error(error.message || '网络错误，请检查网络连接后重试', `${actionText}失败`)
  } finally {
    loading.value = false
  }
}

const saveDraft = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!form.name.trim()) {
    await modal.warning('请至少输入商品名称', '提示')
    return
  }

  loading.value = true

  // 显示加载状态
  const loadingModal = modal.loading('正在保存草稿，请稍候...', '保存中')

  try {
    // 上传已选择的图片
    let imageUrls = []
    if (form.images.length > 0) {
      imageUrls = await uploadImages()
    }

    const draftData = {
      title: form.name.trim(),
      categoryId: form.category ? parseInt(form.category) : null,
      price: form.price ? parseFloat(form.price) : null,
      originalPrice: form.originalPrice ? parseFloat(form.originalPrice) : null,
      description: form.description.trim(),
      images: imageUrls
    }

    const response = await post('/products/draft', draftData)

    // 关闭加载弹窗
    modal.clear()

    if (response.code === 200) {
      await modal.success('草稿已成功保存！您可以随时继续编辑', '保存成功')
      console.log('草稿保存成功：', response.data)
    } else {
      await modal.error(response.message || '保存草稿失败，请重试', '保存失败')
    }
  } catch (error) {
    console.error('保存草稿出错：', error)
    // 关闭加载弹窗
    modal.clear()
    await modal.error(error.message || '保存草稿失败，请重试', '保存失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时检查是否是编辑模式
onMounted(() => {
  const id = route.query.id
  if (id) {
    isEditMode.value = true
    productId.value = id
    loadProductData()
  }
})
</script>

<style scoped>
.publish-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  padding: 20px;
}

.publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 24px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn-header {
  background: rgba(103, 126, 234, 0.1);
  border: 1px solid rgba(103, 126, 234, 0.3);
  color: #374151;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.back-btn-header:hover {
  background: #374151;
  color: white;
  transform: translateX(-2px);
}

.publish-header h1 {
  color: #333;
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.publish-form-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.publish-form {
  padding: 40px;
}

.form-section {
  margin-bottom: 48px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.section-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.section-header h2 {
  color: #333;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-desc {
  color: #666;
  font-size: 14px;
  margin-left: auto;
  font-style: italic;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.form-group {
  position: relative;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  font-size: 15px;
}

.required {
  color: #ff4757;
  font-weight: 700;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  font-size: 15px;
  color: #333;
  background: white;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #374151;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.price-input {
  display: flex;
  align-items: center;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 12px;
  background: white;
  transition: all 0.3s ease;
}

.price-input:focus-within {
  border-color: #374151;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  transform: translateY(-1px);
}

.currency {
  padding: 14px 16px;
  background: rgba(102, 126, 234, 0.1);
  color: #374151;
  font-weight: 700;
  font-size: 16px;
  border-right: 2px solid rgba(102, 126, 234, 0.2);
}

.price-input input {
  border: none;
  border-radius: 0;
  box-shadow: none;
  transform: none;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: -20px;
  font-size: 12px;
  color: #999;
}

.image-upload-area {
  margin-top: 16px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.image-item:hover {
  transform: scale(1.02);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.remove-btn {
  background: #ff4757;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.remove-btn:hover {
  background: #ff3742;
  transform: scale(1.05);
}

.cover-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 600;
}

.upload-placeholder {
  aspect-ratio: 1;
  border: 2px dashed rgba(102, 126, 234, 0.3);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(102, 126, 234, 0.05);
}

.upload-placeholder:hover {
  border-color: #374151;
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.upload-icon {
  color: #374151;
  margin-bottom: 8px;
}

.upload-placeholder span {
  color: #374151;
  font-size: 14px;
  font-weight: 600;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 2px solid rgba(102, 126, 234, 0.1);
}

.draft-btn,
.submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.draft-btn {
  background: rgba(102, 126, 234, 0.1);
  color: #374151;
  border: 2px solid rgba(102, 126, 234, 0.2);
}

.draft-btn:hover {
  background: rgba(102, 126, 234, 0.15);
  border-color: #374151;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.submit-btn {
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled,
.draft-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-message,
.success-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-radius: 12px;
  margin-top: 20px;
  font-weight: 600;
}

.error-message {
  background: rgba(255, 71, 87, 0.1);
  color: #ff4757;
  border: 2px solid rgba(255, 71, 87, 0.2);
}

.success-message {
  background: rgba(46, 213, 115, 0.1);
  color: #2ed573;
  border: 2px solid rgba(46, 213, 115, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .publish-container {
    padding: 16px;
  }

  .publish-header {
    padding: 20px;
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .publish-header h1 {
    font-size: 24px;
  }

  .publish-form {
    padding: 24px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .form-actions {
    flex-direction: column;
  }

  .image-list {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 12px;
  }
}
</style>