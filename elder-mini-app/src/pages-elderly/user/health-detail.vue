<template>
  <view class="container">
    <view class="detail-card">
      <!-- 头部状态 -->
      <view class="header" :class="'status-bg-' + detail.status">
        <view class="status-info">
          <text class="status-text">{{ detail.statusName }}</text>
          <text class="type-text">{{ detail.appointmentTypeName }}</text>
        </view>
        <text class="date-text">{{ detail.appointmentDate }}</text>
      </view>

      <!-- 医生信息 -->
      <view class="section" v-if="detail.doctorName">
        <view class="section-title">
          <text class="title-icon">👨‍⚕️</text>
          <text>接诊医生</text>
        </view>
        <view class="doctor-info">
          <text class="doctor-name">{{ detail.doctorName }}</text>
          <text class="doctor-title" v-if="detail.doctorTitle">{{ detail.doctorTitle }}</text>
        </view>
        <text class="doctor-specialty" v-if="detail.doctorSpecialty">专长：{{ detail.doctorSpecialty }}</text>
      </view>

      <!-- 症状描述 -->
      <view class="section" v-if="detail.symptoms">
        <view class="section-title">
          <text class="title-icon">📋</text>
          <text>症状描述</text>
        </view>
        <text class="section-content">{{ detail.symptoms }}</text>
      </view>

      <!-- 诊断结果 -->
      <view class="section highlight" v-if="detail.diagnosis">
        <view class="section-title">
          <text class="title-icon">🔍</text>
          <text>诊断结果</text>
        </view>
        <text class="section-content diagnosis">{{ detail.diagnosis }}</text>
      </view>

      <!-- 处方建议 -->
      <view class="section highlight" v-if="detail.prescription">
        <view class="section-title">
          <text class="title-icon">💊</text>
          <text>处方建议</text>
        </view>
        <text class="section-content prescription">{{ detail.prescription }}</text>
      </view>

      <!-- 其他信息 -->
      <view class="section">
        <view class="section-title">
          <text class="title-icon">📝</text>
          <text>预约信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">订单号</text>
            <text class="info-value">{{ detail.orderNo }}</text>
          </view>
          <view class="info-item" v-if="detail.queueNumber">
            <text class="info-label">排队号</text>
            <text class="info-value">{{ detail.queueNumber }}号</text>
          </view>
          <view class="info-item" v-if="detail.address">
            <text class="info-label">巡诊地址</text>
            <text class="info-value">{{ detail.address }}</text>
          </view>
          <view class="info-item" v-if="detail.visitTime">
            <text class="info-label">巡诊时间</text>
            <text class="info-value">{{ detail.visitTime }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">预约时间</text>
            <text class="info-value">{{ detail.createTime }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const detail = ref({})

onLoad(async (options) => {
  if (options.id) {
    try {
      detail.value = await get(`/user/health/records/${options.id}`) || {}
    } catch (e) {
      console.error(e)
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  }
})
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.detail-card {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.header {
  padding: 40rpx;
  color: #ffffff;
}

.status-bg-0 {
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
}

.status-bg-1 {
  background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
}

.status-bg-2 {
  background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
}

.status-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.status-text {
  font-size: 40rpx;
  font-weight: bold;
}

.type-text {
  font-size: 26rpx;
  background: rgba(255, 255, 255, 0.3);
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
}

.date-text {
  font-size: 30rpx;
  opacity: 0.9;
}

.section {
  padding: 30rpx 40rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.section:last-child {
  border-bottom: none;
}

.section.highlight {
  background: #fafbfc;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.title-icon {
  font-size: 32rpx;
}

.section-content {
  font-size: 30rpx;
  color: #666666;
  line-height: 1.6;
}

.section-content.diagnosis {
  color: #1890ff;
  font-weight: 500;
}

.section-content.prescription {
  color: #52c41a;
  font-weight: 500;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.doctor-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333333;
}

.doctor-title {
  font-size: 26rpx;
  color: #999999;
  background: #f5f5f5;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
}

.doctor-specialty {
  font-size: 28rpx;
  color: #666666;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 28rpx;
  color: #999999;
}

.info-value {
  font-size: 28rpx;
  color: #333333;
}
</style>
