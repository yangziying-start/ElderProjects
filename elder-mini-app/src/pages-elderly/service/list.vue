<template>
  <view class="container">
    <!-- 加载中 -->
    <view class="loading-tip" v-if="loading">
      <text>加载中...</text>
    </view>
    
    <!-- 服务列表 -->
    <view class="service-list" v-else-if="serviceList.length > 0">
      <view 
        class="service-card" 
        v-for="item in serviceList" 
        :key="item.id"
        @click="goDetail(item)"
      >
        <view class="service-info">
          <text class="service-name">{{ item.name }}</text>
          <text class="service-desc">{{ item.description }}</text>
        </view>
        <view class="service-right">
          <text class="service-price">¥{{ item.price }}</text>
          <view class="book-btn">预约</view>
        </view>
      </view>
    </view>
    
    <!-- 空数据提示 -->
    <view class="empty-tip" v-else>
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无服务项目</text>
      <text class="empty-hint">请稍后再试或联系管理员</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const serviceList = ref([])
const loading = ref(true)

onLoad(async (options) => {
  const categoryId = options.categoryId
  console.log('服务列表页面参数:', options)
  
  try {
    const data = await get('/service/items', { categoryId })
    console.log('服务列表数据:', data)
    serviceList.value = data || []
  } catch (e) {
    console.error('获取服务列表失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
})

const goDetail = (item) => {
  uni.navigateTo({
    url: `/pages-elderly/service/detail?id=${item.id}`
  })
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.loading-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
  color: #999999;
  font-size: 32rpx;
}

.service-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-info {
  flex: 1;
}

.service-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.service-desc {
  font-size: 30rpx;
  color: #999999;
  margin-top: 16rpx;
  display: block;
}

.service-right {
  text-align: right;
}

.service-price {
  font-size: 40rpx;
  color: #ff4d4f;
  font-weight: bold;
  display: block;
}

.book-btn {
  background: #43a047;
  color: #ffffff;
  padding: 20rpx 50rpx;
  border-radius: 16rpx;
  font-size: 34rpx;
  margin-top: 20rpx;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 150rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 36rpx;
  color: #333333;
  margin-bottom: 16rpx;
}

.empty-hint {
  font-size: 28rpx;
  color: #999999;
}
</style>
