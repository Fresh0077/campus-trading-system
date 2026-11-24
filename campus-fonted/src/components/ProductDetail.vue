<template>
  <div class="product-detail-container">
    <!-- 导航栏 -->
    <div class="navbar">
      <button @click="goBack" class="back-btn">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        返回
      </button>
      <div class="navbar-title">商品详情</div>
      <div class="navbar-actions">
        <button @click="shareProduct" class="icon-btn" title="分享商品">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
            <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
          </svg>
        </button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>
    
    <!-- 商品不存在 -->
    <div v-else-if="!product" class="error-state">
      <div class="error-icon">!</div>
      <h2>商品不存在</h2>
      <p>该商品可能已被删除或不存在</p>
      <button @click="goBack" class="error-back-btn">返回商品列表</button>
    </div>
    
    <!-- 商品详情 -->
    <div v-else class="product-detail">
      <!-- 主要信息区 -->
      <div class="detail-main">
        <!-- 图片展示区 -->
        <div class="image-section">
          <div class="main-image-wrapper">
            <img 
              :src="getImageUrl(currentImage)" 
              :alt="product.title" 
              class="main-image"
              @error="handleImageError" 
            />
            <div class="image-badge" v-if="product.status === 1">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              在售
            </div>
          </div>
          <div class="thumbnail-list" v-if="productImages.length > 1">
            <div 
              v-for="(image, index) in productImages" 
              :key="index"
              class="thumbnail-item"
              :class="{ active: currentImage === image }"
              @click="currentImage = image"
            >
              <img 
                :src="getImageUrl(image)" 
                :alt="`图片 ${index + 1}`"
                @error="handleImageError" 
              />
            </div>
          </div>
        </div>
        
        <!-- 信息展示区 -->
        <div class="info-section">
          <!-- 商品标题 -->
          <h1 class="product-title">{{ product.title }}</h1>
          
          <!-- 价格区 -->
          <div class="price-box">
            <div class="price-main">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ product.price }}</span>
            </div>
            <div class="price-meta">
              <span class="view-count">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                {{ product.viewCount || 0 }} 次浏览
              </span>
            </div>
          </div>
          
          <!-- 商品信息列表 -->
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">商品分类</span>
              <span class="info-value">
                <span class="category-badge">{{ getCategoryName(product.categoryId) }}</span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">商品状态</span>
              <span class="info-value">
                <span class="status-badge" :class="`status-${getStatusClass(product.status)}`">
                  {{ getStatusName(product.status) }}
                </span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间</span>
              <span class="info-value">{{ formatTime(product.createTime) }}</span>
            </div>
          </div>
          
          <!-- 卖家信息卡片 -->
          <div class="seller-card">
            <div class="seller-header">
              <span class="seller-label">卖家信息</span>
            </div>
            <div class="seller-content">
              <img 
                :src="getSellerAvatar()" 
                :alt="product.username" 
                class="seller-avatar"
                @error="handleImageError" 
              />
              <div class="seller-info">
                <div class="seller-name">{{ product.username }}</div>
                <div class="seller-meta">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  校园卖家
                </div>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-section" v-if="product.status === 1">
            <button class="btn-primary" @click="showPurchaseDialog">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="9" cy="21" r="1"/>
                <circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
              </svg>
              立即购买
            </button>
            <div class="action-group">
              <button class="btn-secondary" @click="contactSeller">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                联系卖家
              </button>
              <button class="btn-favorite" @click="toggleFavorite" :class="{ active: isFavorite }">
                <svg width="18" height="18" viewBox="0 0 24 24" :fill="isFavorite ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </button>
            </div>
          </div>
          
          <div v-else class="unavailable-notice">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            该商品暂时不可购买
          </div>
        </div>
      </div>
      
      <!-- 商品描述区 -->
      <div class="description-section">
        <div class="section-header">
          <h2>商品描述</h2>
        </div>
        <div class="description-content">
          <p v-if="product.description" class="description-text">{{ product.description }}</p>
          <div v-else class="no-description">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            <p>暂无商品描述</p>
          </div>
        </div>
      </div>
      
      <!-- 推荐商品区 -->
      <div class="recommendation-section" v-if="relatedProducts.length > 0">
        <div class="section-header">
          <h2>相关推荐</h2>
          <p>为您推荐相似的商品</p>
        </div>
        <div class="recommendation-grid">
          <div 
            v-for="item in relatedProducts" 
            :key="item.id"
            class="recommendation-card"
            @click="viewProduct(item.id)"
          >
            <div class="recommendation-image">
              <img :src="getRecommendationImage(item)" :alt="item.title" />
            </div>
            <div class="recommendation-info">
              <h4 class="recommendation-title">{{ item.title }}</h4>
              <div class="recommendation-price">¥{{ item.price }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 购买对话框 -->
    <div v-if="showPurchase" class="purchase-modal" @click="closePurchaseDialog">
      <div class="purchase-container" @click.stop>
        <div class="purchase-header">
          <h2>{{ currentStep === 'info' ? '确认购买' : currentStep === 'payment' ? '选择支付方式' : '模拟支付' }}</h2>
          <button @click="closePurchaseDialog" class="close-btn">×</button>
        </div>
        
        <!-- 步骤1: 填写购买信息 -->
        <div v-if="currentStep === 'info'" class="purchase-content">
          <div class="product-summary">
            <img :src="getImageUrl(currentImage)" :alt="product.title || product.name" class="summary-image">
            <div class="summary-info">
              <h3>{{ product.title || product.name }}</h3>
              <div class="summary-price">¥{{ product.price }}</div>
            </div>
          </div>
          
          <div class="purchase-form">
            <div class="form-group">
              <label><span class="required">*</span> 联系方式：</label>
              <input v-model="buyerContact" type="text" placeholder="请输入您的手机号或邮箱" />
            </div>
            
            <div class="form-group">
              <label><span class="required">*</span> 收货地址：</label>
              <input v-model="deliveryAddress" type="text" placeholder="请输入详细收货地址" />
            </div>
            
            <div class="form-group">
              <label>备注信息：</label>
              <textarea v-model="orderNotes" placeholder="请输入备注信息（可选）" rows="3"></textarea>
            </div>
            
            <div class="total-price-box">
              <div class="price-detail">
                <span>商品价格</span>
                <span>¥{{ product.price }}</span>
              </div>
              <div class="price-total">
                <span>应付总额</span>
                <span class="amount">¥{{ totalAmount }}</span>
              </div>
            </div>
          </div>
          
          <div class="purchase-actions">
            <button @click="closePurchaseDialog" class="cancel-btn">取消</button>
            <button @click="goToPaymentStep" class="confirm-btn">下一步</button>
          </div>
        </div>
        
        <!-- 步骤2: 选择支付方式 -->
        <div v-if="currentStep === 'payment'" class="purchase-content">
          <div class="payment-amount-display">
            <div class="amount-label">订单金额</div>
            <div class="amount-value">¥{{ totalAmount }}</div>
          </div>
          
          <div class="payment-methods">
            <div 
              class="payment-method-item"
              :class="{ active: selectedPaymentMethod === 'alipay' }"
              @click="selectedPaymentMethod = 'alipay'"
            >
              <div class="payment-icon alipay-icon">
                <svg width="32" height="32" viewBox="0 0 1024 1024">
                  <path fill="#1677FF" d="M1023.795 701.952c-20.258-86.811-120.832-224.819-189.133-302.182l-15.309 15.667c80.794 90.931 152.678 212.992 167.629 286.515-105.216 43.878-243.61 76.749-382.362 91.904 0 0-114.483-184.576-164.557-291.43h199.194V448h-256v-64h199.194v-64H393.6v64h192v64H342.323v54.426h128.717s-43.315 194.867-278.426 296.422c0 0-55.373-37.99-103.834-106.035 0 0 158.72-62.054 184.013-231.987H64v-76.954h202.65V320H64v-64h202.65V128.922S258.099 128 241.92 128c-16.179 0-145.92 11.418-145.92 11.418s134.963-57.907 406.067-53.965c271.104 3.891 433.92 129.946 433.92 129.946-86.195 0-201.37 1.485-201.37 1.485V256H960v64.41H734.771v63.642H960v76.954h-191.13c12.39 15.821 27.955 34.918 42.035 54.067C811.725 516.045 960 640 960 640s-39.219 17.562-80.486 38.4c0.922 0.666 0.922 0.666 1.843 1.28 28.416 20.48 83.507 63.642 141.824 22.118l0.614-0.154z"/>
                </svg>
              </div>
              <div class="payment-info">
                <div class="payment-name">支付宝支付</div>
                <div class="payment-desc">推荐使用支付宝安全支付</div>
              </div>
              <div class="payment-check">
                <svg v-if="selectedPaymentMethod === 'alipay'" width="20" height="20" viewBox="0 0 24 24" fill="#1677FF">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                </svg>
              </div>
            </div>
            
            <div 
              class="payment-method-item"
              :class="{ active: selectedPaymentMethod === 'wechat' }"
              @click="selectedPaymentMethod = 'wechat'"
            >
              <div class="payment-icon wechat-icon">
                <svg width="32" height="32" viewBox="0 0 1024 1024">
                  <path fill="#07C160" d="M664.576 368.64a207.872 207.872 0 0 0-10.24-2.048c-8.192-77.824-92.16-138.24-192.512-138.24-112.64 0-204.288 72.704-204.288 162.816 0 52.224 28.16 95.232 75.264 128.512l-18.944 56.832 66.048-33.28c23.552 4.608 42.496 9.216 66.048 9.216 6.656 0 13.312-0.512 19.456-0.512-4.096-13.824-6.656-28.16-6.656-42.496 0.512-87.552 75.776-158.72 172.544-158.72 6.656 0 13.312 0.512 19.968 0.512l13.312 17.408z m-116.736-56.32c14.336 0 23.552 9.216 23.552 23.04s-9.216 23.04-23.552 23.04c-14.336 0-28.16-9.216-28.16-23.04s13.824-23.04 28.16-23.04z m-146.432 46.08c-14.336 0-28.672-9.216-28.672-23.04s14.336-23.04 28.672-23.04c13.824 0 23.552 9.216 23.552 23.04s-9.728 23.04-23.552 23.04z m473.088 93.696c0-76.8-75.264-139.264-162.304-139.264-92.16 0-162.816 62.464-162.816 139.264 0 77.312 70.656 139.264 162.816 139.264 18.944 0 37.888-4.608 56.832-9.216l51.712 28.16-14.336-46.592c37.376-28.16 61.44-65.536 61.44-111.104l6.656-0.512z m-216.576-23.04c-9.216 0-18.944-9.216-18.944-18.432 0-9.728 9.728-18.944 18.944-18.944 14.336 0 23.552 9.216 23.552 18.944 0 9.216-9.216 18.432-23.552 18.432z m108.544 0c-9.216 0-18.432-9.216-18.432-18.432 0-9.728 9.216-18.944 18.432-18.944 13.824 0 23.552 9.216 23.552 18.944 0 9.216-9.728 18.432-23.552 18.432z"/>
                </svg>
              </div>
              <div class="payment-info">
                <div class="payment-name">微信支付</div>
                <div class="payment-desc">亿万用户的选择，更快更安全</div>
              </div>
              <div class="payment-check">
                <svg v-if="selectedPaymentMethod === 'wechat'" width="20" height="20" viewBox="0 0 24 24" fill="#07C160">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                </svg>
              </div>
            </div>
          </div>
          
          <div class="purchase-actions">
            <button @click="currentStep = 'info'" class="cancel-btn">上一步</button>
            <button @click="goToPaymentProcess" class="confirm-btn" :disabled="!selectedPaymentMethod">
              确认支付
            </button>
          </div>
        </div>
        
        <!-- 步骤3: 模拟支付页面 -->
        <div v-if="currentStep === 'paying'" class="purchase-content payment-process">
          <div class="payment-qrcode-section" v-if="!paymentProcessing">
            <div class="payment-logo">
              <svg v-if="selectedPaymentMethod === 'alipay'" width="64" height="64" viewBox="0 0 1024 1024">
                <path fill="#1677FF" d="M1023.795 701.952c-20.258-86.811-120.832-224.819-189.133-302.182l-15.309 15.667c80.794 90.931 152.678 212.992 167.629 286.515-105.216 43.878-243.61 76.749-382.362 91.904 0 0-114.483-184.576-164.557-291.43h199.194V448h-256v-64h199.194v-64H393.6v64h192v64H342.323v54.426h128.717s-43.315 194.867-278.426 296.422c0 0-55.373-37.99-103.834-106.035 0 0 158.72-62.054 184.013-231.987H64v-76.954h202.65V320H64v-64h202.65V128.922S258.099 128 241.92 128c-16.179 0-145.92 11.418-145.92 11.418s134.963-57.907 406.067-53.965c271.104 3.891 433.92 129.946 433.92 129.946-86.195 0-201.37 1.485-201.37 1.485V256H960v64.41H734.771v63.642H960v76.954h-191.13c12.39 15.821 27.955 34.918 42.035 54.067C811.725 516.045 960 640 960 640s-39.219 17.562-80.486 38.4c0.922 0.666 0.922 0.666 1.843 1.28 28.416 20.48 83.507 63.642 141.824 22.118l0.614-0.154z"/>
              </svg>
              <svg v-else width="64" height="64" viewBox="0 0 1024 1024">
                <path fill="#07C160" d="M664.576 368.64a207.872 207.872 0 0 0-10.24-2.048c-8.192-77.824-92.16-138.24-192.512-138.24-112.64 0-204.288 72.704-204.288 162.816 0 52.224 28.16 95.232 75.264 128.512l-18.944 56.832 66.048-33.28c23.552 4.608 42.496 9.216 66.048 9.216 6.656 0 13.312-0.512 19.456-0.512-4.096-13.824-6.656-28.16-6.656-42.496 0.512-87.552 75.776-158.72 172.544-158.72 6.656 0 13.312 0.512 19.968 0.512l13.312 17.408z m-116.736-56.32c14.336 0 23.552 9.216 23.552 23.04s-9.216 23.04-23.552 23.04c-14.336 0-28.16-9.216-28.16-23.04s13.824-23.04 28.16-23.04z m-146.432 46.08c-14.336 0-28.672-9.216-28.672-23.04s14.336-23.04 28.672-23.04c13.824 0 23.552 9.216 23.552 23.04s-9.728 23.04-23.552 23.04z m473.088 93.696c0-76.8-75.264-139.264-162.304-139.264-92.16 0-162.816 62.464-162.816 139.264 0 77.312 70.656 139.264 162.816 139.264 18.944 0 37.888-4.608 56.832-9.216l51.712 28.16-14.336-46.592c37.376-28.16 61.44-65.536 61.44-111.104l6.656-0.512z m-216.576-23.04c-9.216 0-18.944-9.216-18.944-18.432 0-9.728 9.728-18.944 18.944-18.944 14.336 0 23.552 9.216 23.552 18.944 0 9.216-9.216 18.432-23.552 18.432z m108.544 0c-9.216 0-18.432-9.216-18.432-18.432 0-9.728 9.216-18.944 18.432-18.944 13.824 0 23.552 9.216 23.552 18.944 0 9.216-9.728 18.432-23.552 18.432z"/>
              </svg>
            </div>
            <h3>{{ selectedPaymentMethod === 'alipay' ? '支付宝' : '微信' }}扫码支付</h3>
            <div class="qrcode-box">
              <div class="qrcode-placeholder">
                <svg width="200" height="200" viewBox="0 0 200 200">
                  <rect x="10" y="10" width="40" height="40" fill="#000"/>
                  <rect x="60" y="10" width="10" height="10" fill="#000"/>
                  <rect x="80" y="10" width="10" height="10" fill="#000"/>
                  <rect x="100" y="10" width="10" height="10" fill="#000"/>
                  <rect x="150" y="10" width="40" height="40" fill="#000"/>
                  <rect x="20" y="20" width="20" height="20" fill="#fff"/>
                  <rect x="160" y="20" width="20" height="20" fill="#fff"/>
                  <rect x="10" y="60" width="10" height="10" fill="#000"/>
                  <rect x="30" y="60" width="10" height="10" fill="#000"/>
                  <rect x="50" y="60" width="10" height="10" fill="#000"/>
                  <rect x="70" y="60" width="30" height="10" fill="#000"/>
                  <rect x="110" y="60" width="10" height="10" fill="#000"/>
                  <rect x="130" y="60" width="10" height="10" fill="#000"/>
                  <rect x="150" y="60" width="10" height="10" fill="#000"/>
                  <rect x="170" y="60" width="10" height="10" fill="#000"/>
                  <rect x="190" y="60" width="10" height="10" fill="#000"/>
                  <rect x="10" y="150" width="40" height="40" fill="#000"/>
                  <rect x="20" y="160" width="20" height="20" fill="#fff"/>
                  <rect x="60" y="150" width="10" height="10" fill="#000"/>
                  <rect x="80" y="150" width="10" height="10" fill="#000"/>
                  <rect x="100" y="150" width="30" height="10" fill="#000"/>
                  <rect x="140" y="150" width="10" height="10" fill="#000"/>
                  <rect x="160" y="150" width="20" height="30" fill="#000"/>
                </svg>
              </div>
              <p class="qrcode-tip">请使用{{ selectedPaymentMethod === 'alipay' ? '支付宝' : '微信' }}扫描二维码完成支付</p>
            </div>
            <div class="payment-amount-small">
              <span>支付金额：</span>
              <span class="amount">¥{{ totalAmount }}</span>
            </div>
            <div class="mock-payment-tip">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="#f59e0b" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              这是模拟支付，点击下方按钮模拟支付成功
            </div>
            <button @click="simulatePayment" class="mock-pay-btn">模拟支付成功</button>
          </div>
          
          <div v-else class="payment-processing">
            <div class="processing-icon">
              <div class="spinner-large"></div>
            </div>
            <h3>支付处理中...</h3>
            <p>请稍候，正在创建订单</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del } from '../api/request'
import toast from '../services/toast'
import modal from '../services/modal'
import { useSmartNavigation } from '../utils/navigation'
import { handleImageError, getImageUrl } from '../utils/imageUtils'
import { getServerBaseUrl } from '../config'

const router = useRouter()
const route = useRoute()
const { goBack: smartGoBack } = useSmartNavigation()

// 响应式数据
const product = ref(null)
const loading = ref(false)
const currentImage = ref('')
const isFavorite = ref(false)
const relatedProducts = ref([])
const showPurchase = ref(false)
const buyerContact = ref('')
const deliveryAddress = ref('')
const orderNotes = ref('')
const currentStep = ref('info') // info, payment, paying
const selectedPaymentMethod = ref('') // alipay, wechat
const paymentProcessing = ref(false)

// 计算属性
const productId = computed(() => route.params.id)

// 计算总金额（二手商品交易，每个商品唯一，数量固定为1）
const totalAmount = computed(() => {
  if (!product.value) return '0.00'
  return Number(product.value.price).toFixed(2)
})

// 计算商品图片数组
const productImages = computed(() => {
  if (!product.value) return []
  
  const images = []
  
  // 添加主图片
  if (product.value.image) {
    images.push(product.value.image)
  }
  
  // 添加其他图片
  if (product.value.images && Array.isArray(product.value.images)) {
    product.value.images.forEach(img => {
      if (img && !images.includes(img)) {
        images.push(img)
      }
    })
  }
  
  return images.length > 0 ? images : ['/placeholder.png']
})

// 监听路由变化，重新加载商品
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchProduct()
  }
})

// 获取卖家头像
const getSellerAvatar = () => {
  // 优先使用userAvatar字段
  if (product.value?.userAvatar) {
    return getImageUrl(product.value.userAvatar)
  }
  // 备选：seller对象中的avatar
  if (product.value?.seller?.avatar) {
    return getImageUrl(product.value.seller.avatar)
  }
  return '/logo.jpg'
}

// 获取卖家名称
const getSellerName = () => {
  // 优先使用username字段
  return product.value?.username || 
         product.value?.seller?.name || 
         product.value?.sellerName || 
         '未知卖家'
}

// 方法
const fetchProduct = async () => {
  loading.value = true
  try {
    console.log('获取商品详情，ID：', productId.value)
    
    const response = await get(`/products/${productId.value}`)
    
    console.log('商品详情API响应：', response)
    
    if (response && response.code === 200) {
      product.value = response.data
      
      // 设置当前显示的图片
      const images = productImages.value
      currentImage.value = images.length > 0 ? images[0] : ''
      
      console.log('商品详情获取成功：', response.data)
      
      // 获取相关商品
      fetchRelatedProducts()
      
      // 增加浏览次数
      incrementViewCount()
      
      // 检查是否已收藏
      checkFavoriteStatus()
    } else {
      console.log('获取商品详情失败:', response ? response.message : '响应为空')
      product.value = null
    }
  } catch (error) {
    console.log('获取商品详情异常:', error)
    product.value = null
  } finally {
    loading.value = false
  }
}

// 检查商品是否已收藏
const checkFavoriteStatus = async () => {
  try {
    // 使用后端提供的“我的收藏列表”接口
    const response = await get('/favorites/my')
    if (response && response.code === 200 && Array.isArray(response.data)) {
      isFavorite.value = response.data.some(fav =>
        fav.productId === parseInt(productId.value) ||
        (fav.product && fav.product.id === parseInt(productId.value))
      )
    } else {
      isFavorite.value = false
    }
  } catch (error) {
    console.error('检查收藏状态失败：', error)
    isFavorite.value = false
  }
}

const fetchRelatedProducts = async () => {
  try {
    // 使用后端的推荐商品API
    const response = await get(`/products/${productId.value}/recommendations`, {
      params: { limit: 4 }
    })
    
    console.log('推荐商品API响应：', response)
    
    if (response && response.code === 200 && response.data) {
      // 确保response.data是数组
      if (Array.isArray(response.data)) {
        relatedProducts.value = response.data
      } else {
        console.warn('推荐商品数据不是数组格式')
        relatedProducts.value = []
      }
    }
  } catch (error) {
    console.error('获取推荐商品失败：', error)
    relatedProducts.value = []
  }
}

// 获取推荐商品图片
const getRecommendationImage = (item) => {
  if (!item) return '/placeholder.png'
  
  // 优先使用 images 数组的第一张
  if (item.images && Array.isArray(item.images) && item.images.length > 0) {
    return getImageUrl(item.images[0])
  }
  
  // 其次使用 image 字段
  if (item.image) {
    return getImageUrl(item.image)
  }
  
  return '/placeholder.png'
}

const incrementViewCount = async () => {
  try {
    await post(`/products/${productId.value}/view`)
  } catch (error) {
    console.error('增加浏览次数失败：', error)
  }
}

const goBack = () => {
  // 使用智能返回，默认返回商品列表
  smartGoBack('/products')
}

const viewProduct = (id) => {
  router.push(`/product/${id}`)
}

const contactSeller = async () => {
  const sellerId = product.value?.sellerId || product.value?.userId
  const currentUserId = Number(localStorage.getItem('userId') || '0')
  
  console.log('联系卖家调试:', {
    sellerId,
    currentUserId,
    productUserId: product.value?.userId,
    productSellerId: product.value?.sellerId
  })
  
  if (sellerId) {
    // 检查是否是自己的商品
    if (sellerId === currentUserId) {
      await modal.info('这是您自己发布的商品，无法联系自己', '提示')
      return
    }
    
    router.push({
      path: '/messages',
      query: {
        to: String(sellerId),
        productId: String(productId.value || ''),
        name: getSellerName(),
        avatar: getSellerAvatar()
      }
    })
  } else {
    const sellerPhone = product.value?.phone || product.value?.seller?.phone
    const sellerEmail = product.value?.email || product.value?.seller?.email
    if (sellerPhone) {
      await modal.info(`卖家联系方式：${sellerPhone}`, '联系方式')
    } else if (sellerEmail) {
      await modal.info(`卖家邮箱：${sellerEmail}`, '联系方式')
    } else {
      await modal.info('暂无卖家联系方式', '联系方式')
    }
  }
}

const toggleFavorite = async () => {
  try {
    if (isFavorite.value) {
      // 取消收藏
      const response = await del(`/favorites/${productId.value}`)
      if (response.code === 200) {
        isFavorite.value = false
        toast.success('已取消收藏', '操作成功')
      } else {
        toast.error(response.message || '取消收藏失败，请重试', '操作失败')
      }
    } else {
      // 添加收藏
      const response = await post(`/favorites/${productId.value}`, {})
      if (response.code === 200) {
        isFavorite.value = true
        toast.success('收藏成功！可在"我的收藏"中查看', '收藏成功')
      } else {
        toast.error(response.message || '收藏失败，请重试', '操作失败')
      }
    }
  } catch (error) {
    console.error('收藏操作失败：', error)
    toast.error('网络错误，请检查网络连接后重试', '操作失败')
  }
}

const showPurchaseDialog = () => {
  // 获取用户信息作为默认联系方式
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo && userInfo !== 'undefined') {
    try {
      const user = JSON.parse(userInfo)
      buyerContact.value = user.phone || user.email || ''
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
  
  showPurchase.value = true
}

const closePurchaseDialog = () => {
  showPurchase.value = false
  // 重置表单
  buyerContact.value = ''
  deliveryAddress.value = ''
  orderNotes.value = ''
  currentStep.value = 'info'
  selectedPaymentMethod.value = ''
  paymentProcessing.value = false
}

// 验证购买信息并进入支付步骤
const goToPaymentStep = () => {
  // 表单验证
  const errors = []
  
  if (!buyerContact.value.trim()) {
    errors.push('请输入联系方式')
  } else {
    // 验证联系方式格式（手机号或邮箱）
    const phoneRegex = /^1[3-9]\d{9}$/
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    
    if (!phoneRegex.test(buyerContact.value) && !emailRegex.test(buyerContact.value)) {
      errors.push('请输入有效的手机号或邮箱地址')
    }
  }
  
  if (!deliveryAddress.value.trim()) {
    errors.push('请输入收货地址')
  } else   if (deliveryAddress.value.trim().length < 5) {
    errors.push('收货地址至少需要5个字符')
  }
  
  // 显示验证错误
  if (errors.length > 0) {
    toast.error(errors.join('；'), '请检查输入信息')
    return
  }
  
  // 切换到支付方式选择步骤
  currentStep.value = 'payment'
}

// 进入支付流程
const goToPaymentProcess = () => {
  if (!selectedPaymentMethod.value) {
    toast.error('请选择支付方式', '提示')
    return
  }
  
  currentStep.value = 'paying'
}

// 模拟支付成功
const simulatePayment = async () => {
  paymentProcessing.value = true
  
  try {
    // 模拟支付延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 创建订单
    // 注意：amount字段仅用于前端显示验证，后端会使用商品的实际价格
    // 这是为了防止恶意用户篡改价格进行低价购买
    const orderData = {
      productId: parseInt(productId.value),
      amount: parseFloat(totalAmount.value), // 后端不使用此字段，而是查询商品实际价格
      remark: `
联系方式: ${buyerContact.value}
收货地址: ${deliveryAddress.value}
支付方式: ${selectedPaymentMethod.value === 'alipay' ? '支付宝' : '微信支付'}
${orderNotes.value ? '备注: ' + orderNotes.value : ''}
      `.trim()
    }
    
    console.log('创建订单数据：', orderData)
    
    const response = await post('/orders', orderData)
    
    paymentProcessing.value = false
    
    if (response.success || response.code === 200) {
      closePurchaseDialog()
      await modal.success(
        `支付成功！订单已创建，请等待卖家确认。\n订单号：${response.data?.orderNo || '系统生成中'}`,
        '支付成功'
      )
      
      // 跳转到订单管理页面
      setTimeout(() => {
        router.push('/orders')
      }, 1000)
    } else {
      await modal.error(response.message || '订单创建失败，请重试', '支付失败')
    }
  } catch (error) {
    console.error('创建订单失败：', error)
    paymentProcessing.value = false
    await modal.error('网络错误，请检查网络连接后重试', '支付失败')
  }
}

// 分享商品
const shareProduct = () => {
  const url = window.location.href
  
  // 尝试使用 Web Share API
  if (navigator.share) {
    navigator.share({
      title: product.value?.title || '商品详情',
      text: `${product.value?.title} - ¥${product.value?.price}`,
      url: url
    }).then(() => {
      toast.success('分享成功', '提示')
    }).catch((error) => {
      if (error.name !== 'AbortError') {
        copyToClipboard(url)
      }
    })
  } else {
    // 备用方案：复制链接到剪贴板
    copyToClipboard(url)
  }
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(() => {
      toast.success('链接已复制到剪贴板', '分享')
    }).catch(() => {
      toast.error('复制失败，请手动复制', '提示')
    })
  } else {
    // 备用方案
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      toast.success('链接已复制到剪贴板', '分享')
    } catch (err) {
      toast.error('复制失败，请手动复制', '提示')
    }
    document.body.removeChild(textArea)
  }
}

const getCategoryName = (categoryId) => {
  const categoryMap = {
    1: '电子产品',
    2: '图书教材', 
    3: '服装配饰',
    4: '运动用品',
    5: '生活用品',
    6: '其他',
    'electronics': '电子产品',
    'books': '图书教材',
    'clothing': '服装配饰',
    'sports': '运动用品',
    'daily': '生活用品',
    'other': '其他'
  }
  return categoryMap[categoryId] || '未知分类'
}

const getStatusName = (status) => {
  const statusMap = {
    1: '在售',
    0: '已下架',
    2: '已售出',
    3: '已预订',
    'available': '在售',
    'sold': '已售出',
    'reserved': '已预订',
    'offline': '已下架'
  }
  return statusMap[status] || '未知状态'
}

const getStatusClass = (status) => {
  if (status === 1 || status === 'available') return 'available'
  if (status === 0 || status === 'offline') return 'offline'
  if (status === 2 || status === 'sold') return 'sold'
  if (status === 3 || status === 'reserved') return 'reserved'
  return 'unknown'
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
/* ========== 容器样式 ========== */
.product-detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* ========== 导航栏 ========== */
.navbar {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: #f9fafb;
  border-color: #374151;
  color: #374151;
}

.navbar-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  flex: 1;
}

.navbar-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  background: #f3f4f6;
  border-color: #374151;
  color: #374151;
}

/* ========== 加载状态 ========== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 16px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e5e7eb;
  border-top-color: #374151;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: #6b7280;
  font-size: 14px;
}

/* ========== 错误状态 ========== */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px;
  text-align: center;
}

.error-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #fee;
  color: #ef4444;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 24px;
}

.error-state h2 {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.error-state p {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 24px;
}

.error-back-btn {
  padding: 10px 24px;
  background: #374151;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.error-back-btn:hover {
  background: #5568d3;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* ========== 商品详情 ========== */
.product-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ========== 主要信息区 ========== */
.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* ========== 图片展示区 ========== */
.image-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
  height: 480px;
  background: #f9fafb;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.main-image:hover {
  transform: scale(1.02);
}

.image-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(34, 197, 94, 0.95);
  color: white;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);
}

.thumbnail-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 0;
}

.thumbnail-list::-webkit-scrollbar {
  height: 6px;
}

.thumbnail-list::-webkit-scrollbar-track {
  background: #f3f4f6;
  border-radius: 3px;
}

.thumbnail-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.thumbnail-list::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

.thumbnail-item {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  background: #f9fafb;
}

.thumbnail-item:hover {
  border-color: #374151;
  transform: translateY(-2px);
}

.thumbnail-item.active {
  border-color: #374151;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ========== 信息展示区 ========== */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #111827;
  line-height: 1.3;
  margin: 0;
}

/* 价格区 */
.price-box {
  padding: 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  border: 1px solid #fbbf24;
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.price-symbol {
  font-size: 20px;
  font-weight: 600;
  color: #dc2626;
}

.price-value {
  font-size: 36px;
  font-weight: 700;
  color: #dc2626;
  line-height: 1;
}

.price-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #92400e;
  font-weight: 500;
}

/* 商品信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.category-badge {
  display: inline-block;
  padding: 6px 12px;
  background: #ede9fe;
  color: #7c3aed;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.status-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.status-available {
  background: #d1fae5;
  color: #065f46;
}

.status-sold {
  background: #fee2e2;
  color: #991b1b;
}

.status-reserved {
  background: #fef3c7;
  color: #92400e;
}

.status-offline {
  background: #f3f4f6;
  color: #6b7280;
}

/* 卖家信息卡片 */
.seller-card {
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.seller-header {
  margin-bottom: 16px;
}

.seller-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.seller-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.seller-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.seller-info {
  flex: 1;
}

.seller-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
}

.seller-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

/* 操作按钮 */
.action-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px 24px;
  background: #374151;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  background: #5568d3;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.action-group {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.btn-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  background: #f9fafb;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  transform: translateY(-1px);
}

.btn-favorite {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: #f9fafb;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 48px;
}

.btn-favorite:hover {
  background: #fef2f2;
  border-color: #fca5a5;
  color: #ef4444;
}

.btn-favorite.active {
  background: #fef2f2;
  border-color: #ef4444;
  color: #ef4444;
}

.unavailable-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  background: #fef2f2;
  color: #991b1b;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #fee2e2;
}

/* ========== 商品描述区 ========== */
.description-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.section-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.description-content {
  color: #374151;
  line-height: 1.7;
  font-size: 15px;
}

.description-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.no-description {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #9ca3af;
  text-align: center;
}

.no-description svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-description p {
  margin: 0;
  font-size: 14px;
}

/* ========== 推荐商品区 ========== */
.recommendation-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.recommendation-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.recommendation-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border-color: #374151;
}

.recommendation-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f9fafb;
}

.recommendation-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.recommendation-card:hover .recommendation-image img {
  transform: scale(1.08);
}

.recommendation-info {
  padding: 16px;
}

.recommendation-title {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommendation-price {
  font-size: 18px;
  font-weight: 700;
  color: #dc2626;
}

/* ========== 购买对话框 ========== */
.purchase-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  padding: 20px;
}

.purchase-container {
  background: white;
  border-radius: 16px;
  max-width: 540px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
}

.purchase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.purchase-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #9ca3af;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
  line-height: 1;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.purchase-content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.product-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.summary-image {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.summary-info {
  flex: 1;
  min-width: 0;
}

.summary-info h3 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #111827;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-price {
  color: #dc2626;
  font-weight: 700;
  font-size: 18px;
}

.purchase-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.form-group input,
.form-group textarea {
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #374151;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 12px;
  width: fit-content;
}

.quantity-control button {
  width: 36px;
  height: 36px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  transition: all 0.2s ease;
}

.quantity-control button:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.quantity-control button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quantity-control input {
  width: 72px;
  text-align: center;
  margin: 0;
  font-weight: 600;
}

.total-price {
  text-align: right;
  font-size: 20px;
  font-weight: 700;
  color: #dc2626;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.total-price-box {
  background: #f9fafb;
  border-radius: 10px;
  padding: 16px;
  margin-top: 20px;
}

.price-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #6b7280;
}

.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0 0;
  margin-top: 8px;
  border-top: 1px solid #e5e7eb;
  font-size: 16px;
  font-weight: 600;
}

.price-total .amount {
  font-size: 24px;
  font-weight: 700;
  color: #dc2626;
}

.required {
  color: #ef4444;
  margin-right: 4px;
}

.purchase-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.cancel-btn,
.confirm-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
}

.cancel-btn:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.confirm-btn {
  background: #374151;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.confirm-btn:hover:not(:disabled) {
  background: #5568d3;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ========== 响应式设计 ========== */
@media (max-width: 1024px) {
  .product-detail {
    padding: 20px;
  }
  
  .detail-main {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .product-detail {
    padding: 16px;
  }
  
  .detail-main {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 20px;
  }
  
  .main-image-wrapper {
    height: 320px;
  }
  
  .product-title {
    font-size: 24px;
  }
  
  .price-value {
    font-size: 32px;
  }
  
  .info-list {
    gap: 12px;
  }
  
  .action-section {
    gap: 10px;
  }
  
  .action-group {
    grid-template-columns: 1fr;
  }
  
  .description-section,
  .recommendation-section {
    padding: 24px;
  }
  
  .recommendation-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
  }
  
  .recommendation-image {
    height: 140px;
  }
}

@media (max-width: 480px) {
  .navbar {
    padding: 12px 16px;
  }
  
  .product-detail {
    padding: 12px;
  }
  
  .detail-main {
    padding: 16px;
    gap: 20px;
  }
  
  .main-image-wrapper {
    height: 280px;
  }
  
  .thumbnail-item {
    width: 72px;
    height: 72px;
  }
  
  .product-title {
    font-size: 20px;
  }
  
  .price-value {
    font-size: 28px;
  }
  
  .btn-primary {
    padding: 12px 20px;
    font-size: 15px;
  }
  
  .btn-secondary,
  .btn-favorite {
    padding: 10px 16px;
    font-size: 14px;
  }
  
  .description-section,
  .recommendation-section {
    padding: 20px 16px;
  }
  
  .recommendation-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .purchase-modal {
    padding: 12px;
  }
  
  .purchase-container {
    border-radius: 12px;
  }
  
  .purchase-header,
  .purchase-content,
  .purchase-actions {
    padding: 16px;
  }
}

/* ========== 支付相关样式 ========== */
.payment-amount-display {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  border-radius: 12px;
  margin-bottom: 24px;
}

.amount-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin-bottom: 8px;
}

.amount-value {
  color: white;
  font-size: 36px;
  font-weight: 700;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.payment-method-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.payment-method-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.payment-method-item.active {
  background: #ffffff;
  border-color: #374151;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.payment-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.alipay-icon {
  background: #e6f4ff;
}

.wechat-icon {
  background: #e6f9f0;
}

.payment-info {
  flex: 1;
}

.payment-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.payment-desc {
  font-size: 13px;
  color: #6b7280;
}

.payment-check {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 支付处理页面 */
.payment-process {
  text-align: center;
}

.payment-qrcode-section {
  padding: 20px;
}

.payment-logo {
  margin: 0 auto 16px;
  width: 64px;
  height: 64px;
}

.payment-qrcode-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 24px 0;
}

.qrcode-box {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 24px;
  margin: 0 auto 16px;
  max-width: 260px;
}

.qrcode-placeholder {
  width: 200px;
  height: 200px;
  margin: 0 auto;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-tip {
  margin: 16px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.payment-amount-small {
  font-size: 16px;
  color: #374151;
  margin: 16px 0;
}

.payment-amount-small .amount {
  font-size: 20px;
  font-weight: 700;
  color: #dc2626;
  margin-left: 4px;
}

.mock-payment-tip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fef3c7;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  color: #92400e;
  font-size: 13px;
  margin: 16px 0;
}

.mock-pay-btn {
  width: 100%;
  max-width: 300px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #374151 0%, #6b7280 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  margin-top: 16px;
}

.mock-pay-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.payment-processing {
  padding: 60px 20px;
}

.processing-icon {
  margin: 0 auto 24px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner-large {
  width: 64px;
  height: 64px;
  border: 4px solid #e5e7eb;
  border-top-color: #374151;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.payment-processing h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.payment-processing p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}
</style>