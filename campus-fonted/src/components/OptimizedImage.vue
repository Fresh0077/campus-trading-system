<template>
  <div class="optimized-image" :class="{ 'image-loading': loading }">
    <img
      v-if="shouldLoad"
      :src="currentSrc"
      :alt="alt"
      :class="imageClass"
      :style="imageStyle"
      @load="onImageLoad"
      @error="onImageError"
      loading="lazy"
    />
    <div v-if="loading" class="image-placeholder">
      <div class="skeleton-loader"></div>
    </div>
  </div>
</template>

<script>
import { getImageUrl, handleImageError, preloadImages, generateResponsiveUrl } from '@/utils/imageUtils'

export default {
  name: 'OptimizedImage',
  props: {
    src: {
      type: String,
      required: true
    },
    alt: {
      type: String,
      default: ''
    },
    width: {
      type: [Number, String],
      default: null
    },
    height: {
      type: [Number, String],
      default: null
    },
    quality: {
      type: Number,
      default: 80,
      validator: value => value >= 1 && value <= 100
    },
    format: {
      type: String,
      default: 'auto',
      validator: value => ['auto', 'webp', 'jpg', 'png'].includes(value)
    },
    lazy: {
      type: Boolean,
      default: true
    },
    preload: {
      type: Boolean,
      default: false
    },
    objectFit: {
      type: String,
      default: 'cover',
      validator: value => ['cover', 'contain', 'fill', 'scale-down', 'none'].includes(value)
    },
    rounded: {
      type: Boolean,
      default: false
    },
    className: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      loading: false,
      loaded: false,
      error: false,
      currentSrc: '',
      shouldLoad: !this.lazy
    }
  },
  computed: {
    imageClass() {
      return [
        'optimized-image__img',
        {
          'rounded': this.rounded,
          'loaded': this.loaded,
          'error': this.error
        },
        this.className
      ]
    },
    imageStyle() {
      const style = {}
      
      if (this.width) {
        style.width = typeof this.width === 'number' ? `${this.width}px` : this.width
      }
      
      if (this.height) {
        style.height = typeof this.height === 'number' ? `${this.height}px` : this.height
      }
      
      style.objectFit = this.objectFit
      
      return style
    },
    optimizedSrc() {
      const fullUrl = getImageUrl(this.src)
      
      // 如果需要优化，生成响应式URL
      if (this.width || this.height || this.quality !== 80 || this.format !== 'auto') {
        return generateResponsiveUrl(fullUrl, {
          width: this.width,
          height: this.height,
          quality: this.quality,
          format: this.format
        })
      }
      
      return fullUrl
    }
  },
  watch: {
    src: {
      immediate: true,
      handler(newSrc) {
        if (newSrc) {
          this.loadImage()
        }
      }
    }
  },
  mounted() {
    if (this.lazy && this.src) {
      this.setupLazyLoading()
    } else if (this.src) {
      this.loadImage()
    }
    
    if (this.preload && this.src) {
      this.preloadImage()
    }
  },
  methods: {
    loadImage() {
      if (!this.src) return
      
      this.loading = true
      this.error = false
      this.currentSrc = this.optimizedSrc
    },
    onImageLoad() {
      this.loading = false
      this.loaded = true
      this.$emit('load')
    },
    onImageError(event) {
      this.loading = false
      this.error = true
      handleImageError(event)
      this.$emit('error')
    },
    setupLazyLoading() {
      // 使用Intersection Observer实现懒加载
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach(entry => {
            if (entry.isIntersecting) {
              this.shouldLoad = true
              this.loadImage()
              observer.unobserve(this.$el)
            }
          })
        },
        {
          rootMargin: '50px'
        }
      )
      
      observer.observe(this.$el)
      
      // 组件销毁时清理观察器
      this.$once('hook:beforeDestroy', () => {
        observer.disconnect()
      })
    },
    preloadImage() {
      preloadImages(this.optimizedSrc).catch(err => {
        console.warn('图片预加载失败:', err)
      })
    }
  }
}
</script>

<style scoped>
.optimized-image {
  position: relative;
  overflow: hidden;
}

.optimized-image__img {
  display: block;
  width: 100%;
  height: 100%;
  transition: opacity 0.3s ease;
}

.optimized-image__img:not(.loaded) {
  opacity: 0;
}

.optimized-image__img.loaded {
  opacity: 1;
}

.optimized-image__img.rounded {
  border-radius: 8px;
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skeleton-loader {
  width: 60%;
  height: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.image-loading .optimized-image__img {
  filter: blur(2px);
}

.image-loading .image-placeholder {
  display: flex;
}

.image-loading .skeleton-loader {
  animation: skeleton-loading 1.5s infinite;
}

/* 响应式图片 */
@media (max-width: 768px) {
  .optimized-image__img {
    max-width: 100%;
    height: auto;
  }
}
</style>