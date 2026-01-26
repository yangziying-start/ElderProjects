<template>
  <view class="container">
    <!-- 基本信息卡片 -->
    <view class="info-card">
      <view class="info-header">
        <view class="avatar-wrap">
          <image v-if="userAvatar" class="avatar" :src="userAvatar" mode="aspectFill" />
          <view v-else class="avatar-default">👴</view>
        </view>
        <view class="info-main">
          <text class="name">{{ healthInfo.name || '未填写' }}</text>
          <view class="info-tags">
            <text class="tag" v-if="healthInfo.age">{{ healthInfo.age }}岁</text>
            <text class="tag" v-if="healthInfo.bloodType">{{ healthInfo.bloodType }}型血</text>
          </view>
        </view>
      </view>
      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-num">{{ healthInfo.appointmentCount || 0 }}</text>
          <text class="stat-label">预约次数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ healthInfo.completedCount || 0 }}</text>
          <text class="stat-label">完成巡诊</text>
        </view>
      </view>
    </view>

    <!-- 健康记录列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">医疗档案</text>
        <text class="section-tip">历次医疗预约记录</text>
      </view>
      
      <view class="record-list" v-if="healthRecords.length > 0">
        <view class="record-card" v-for="record in healthRecords" :key="record.id" @click="viewDetail(record)">
          <view class="record-header">
            <view class="record-date-wrap">
              <text class="record-date">{{ formatDate(record.appointmentDate) }}</text>
              <text class="record-type" :class="record.appointmentType === 1 ? 'day' : 'night'">
                {{ record.appointmentTypeName }}
              </text>
            </view>
            <view class="record-status" :class="'status-' + record.status">
              {{ record.statusName }}
            </view>
          </view>
          
          <view class="record-doctor" v-if="record.doctorName">
            <text class="doctor-name">{{ record.doctorName }}</text>
            <text class="doctor-title" v-if="record.doctorTitle">{{ record.doctorTitle }}</text>
          </view>
          
          <view class="record-content">
            <view class="content-item" v-if="record.symptoms">
              <text class="content-label">症状描述</text>
              <text class="content-text">{{ record.symptoms }}</text>
            </view>
            <view class="content-item" v-if="record.diagnosis">
              <text class="content-label">诊断结果</text>
              <text class="content-text diagnosis">{{ record.diagnosis }}</text>
            </view>
          </view>
          
          <view class="record-footer">
            <text class="view-detail">查看详情 ›</text>
          </view>
        </view>
      </view>
      
      <view class="empty" v-else>
        <text class="empty-text">暂无医疗记录</text>
        <text class="empty-tip">预约医疗巡诊后，记录将在这里显示</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const healthInfo = ref({})
const healthRecords = ref([])
const userAvatar = ref('')

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

const viewDetail = (record) => {
  uni.navigateTo({
    url: `/pages-elderly/user/health-detail?id=${record.id}`
  })
}

onShow(async () => {
  try {
    // 如果用户信息为空，先获取用户信息
    if (!userStore.userInfo) {
      await userStore.getUserInfo()
    }
    const elderlyId = userStore.userInfo?.id
    // 获取用户头像用于显示
    userAvatar.value = userStore.userInfo?.avatar || ''
    if (!elderlyId) {
      console.error('未获取到用户信息')
      return
    }
    healthInfo.value = await get('/user/health', { elderlyId }) || {}
    healthRecords.value = await get('/user/health/records', { elderlyId }) || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: linear-gradient(180deg, #e8f4f8 0%, #f5f5f5 300rpx);
  min-height: 100vh;
}

.info-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.info-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.avatar-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 30rpx;
  border: 4rpx solid #e8f4f8;
}

.avatar {
  width: 100%;
  height: 100%;
}

.avatar-default {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  background: #f5f5f5;
}

.info-main {
  flex: 1;
}

.name {
  font-size: 42rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 12rpx;
}

.info-tags {
  display: flex;
  gap: 16rpx;
}

.tag {
  font-size: 26rpx;
  color: #666666;
  background: #f5f5f5;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.stats-row {
  display: flex;
  border-top: 1rpx solid #f0f0f0;
  padding-top: 30rpx;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-num {
  font-size: 48rpx;
  font-weight: bold;
  color: #4a90d9;
  display: block;
}

.stat-label {
  font-size: 26rpx;
  color: #999999;
  margin-top: 8rpx;
}

.section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 38rpx;
  font-weight: bold;
  color: #333333;
}

.section-tip {
  font-size: 26rpx;
  color: #999999;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.record-card {
  background: #fafbfc;
  border-radius: 20rpx;
  padding: 30rpx;
  border: 1rpx solid #f0f0f0;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.record-date-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.record-date {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.record-type {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
}

.record-type.day {
  background: #e6f7ff;
  color: #1890ff;
}

.record-type.night {
  background: #fff7e6;
  color: #fa8c16;
}

.record-status {
  font-size: 26rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-0 {
  background: #fff7e6;
  color: #fa8c16;
}

.status-1 {
  background: #e6f7ff;
  color: #1890ff;
}

.status-2 {
  background: #f6ffed;
  color: #52c41a;
}

.record-doctor {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #ffffff;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.doctor-name {
  font-size: 30rpx;
  color: #333333;
  font-weight: 500;
}

.doctor-title {
  font-size: 26rpx;
  color: #999999;
  margin-left: 12rpx;
}

.record-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.content-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.content-label {
  font-size: 26rpx;
  color: #999999;
}

.content-text {
  font-size: 30rpx;
  color: #333333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-text.diagnosis {
  color: #1890ff;
}

.record-footer {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  text-align: right;
}

.view-detail {
  font-size: 28rpx;
  color: #4a90d9;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
}

.empty-text {
  font-size: 32rpx;
  color: #999999;
  margin-bottom: 12rpx;
}

.empty-tip {
  font-size: 26rpx;
  color: #cccccc;
}
</style>
