<template>
  <view class="container">
    <view class="status-banner" :class="'status-' + task.status">
      <text class="status-text">{{ getStatusText(task.status) }}</text>
      <text class="status-desc">{{ getStatusDesc(task.status) }}</text>
    </view>

    <view class="info-section">
      <view class="section-title">服务信息</view>
      <view class="info-item">
        <text class="label">服务项目</text>
        <text class="value">{{ task.serviceName }}</text>
      </view>
      <view class="info-item">
        <text class="label">服务金额</text>
        <text class="value price">{{ task.amount }}积分</text>
      </view>
      <view class="info-item">
        <text class="label">预约时间</text>
        <text class="value">{{ task.appointmentTime }}</text>
      </view>
      <view class="info-item" v-if="task.remark">
        <text class="label">服务要求</text>
        <text class="value">{{ task.remark }}</text>
      </view>
    </view>

    <view class="info-section">
      <view class="section-title">老人信息</view>
      <view class="info-item">
        <text class="label">老人姓名</text>
        <text class="value">{{ task.elderlyName }}</text>
      </view>
      <view class="info-item">
        <text class="label">联系电话</text>
        <text class="value link" @click="callElderly">{{ task.elderlyPhone }} 📞</text>
      </view>
      <view class="info-item">
        <text class="label">服务地址</text>
        <text class="value">{{ task.address }}</text>
      </view>
    </view>

    <!-- 服务凭证（已完成时显示） -->
    <view class="info-section" v-if="task.evidence && task.status >= 2">
      <view class="section-title">服务凭证</view>
      <image :src="getImageUrl(task.evidence)" class="evidence-image" mode="aspectFill" @click="previewEvidence" />
    </view>

    <view class="action-section">
      <view class="action-btn orange" v-if="task.status === 0 && !task.serviceCode" @click="acceptOrder">接单</view>
      <view class="action-btn green" v-if="task.status === 0 && task.serviceCode" @click="goExecute">开始服务</view>
      <view class="action-btn blue" v-if="task.status === 1" @click="goExecute">继续服务</view>
      <view class="action-btn call" @click="callElderly">📞 联系老人</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const task = ref({})
const orderType = ref('cleaning')

const statusMap = {
  0: '待服务',
  1: '服务中',
  2: '待确认',
  3: '已完成',
  4: '已取消'
}

const statusDescMap = {
  0: '请按时上门提供服务',
  1: '服务进行中，完成后请上传凭证',
  2: '等待老人确认服务完成',
  3: '服务已完成',
  4: '订单已取消'
}

const getStatusText = (status) => task.value.statusText || statusMap[status] || '未知'
const getStatusDesc = (status) => statusDescMap[status] || ''

onLoad(async (options) => {
  const id = options.id
  orderType.value = options.type || 'cleaning'
  try {
    task.value = await get(`/worker/order/${id}?type=${orderType.value}`) || {}
  } catch (e) {
    console.error(e)
  }
})

const goExecute = () => {
  uni.navigateTo({
    url: `/pages-worker/order/execute?id=${task.value.id}&type=${orderType.value}`
  })
}

const acceptOrder = async () => {
  try {
    const result = await post('/worker/accept', {
      orderId: task.value.id,
      type: orderType.value
    })
    uni.showToast({ title: '接单成功', icon: 'success' })
    // 更新本地数据
    task.value.serviceCode = result.serviceCode
  } catch (e) {
    console.error('接单失败:', e)
  }
}

const callElderly = () => {
  if (task.value.elderlyPhone) {
    uni.makePhoneCall({ 
      phoneNumber: task.value.elderlyPhone, 
      fail: (err) => {
        // 用户取消拨打不提示错误
        if (err.errMsg && err.errMsg.includes('cancel')) return
        console.error('拨打电话失败:', err)
      }
    })
  } else {
    uni.showToast({ title: '暂无联系方式', icon: 'none' })
  }
}

const previewEvidence = () => {
  if (task.value.evidence) {
    uni.previewImage({
      urls: [getImageUrl(task.value.evidence)]
    })
  }
}

// 获取图片完整URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080/api/file/view/${path}`
}
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; padding-bottom: 280rpx; }
.status-banner { padding: 60rpx; text-align: center; }
.status-0 { background: linear-gradient(135deg, #43a047, #66bb6a); }
.status-1 { background: linear-gradient(135deg, #fa8c16, #ffc53d); }
.status-2 { background: linear-gradient(135deg, #1890ff, #69c0ff); }
.status-3 { background: linear-gradient(135deg, #52c41a, #95de64); }
.status-4 { background: linear-gradient(135deg, #52c41a, #95de64); }
.status-5 { background: linear-gradient(135deg, #999, #bbb); }
.status-text { font-size: 48rpx; font-weight: bold; color: #ffffff; display: block; }
.status-desc { font-size: 28rpx; color: rgba(255,255,255,0.8); margin-top: 12rpx; display: block; }
.info-section { background: #ffffff; margin: 30rpx; border-radius: 24rpx; padding: 30rpx 40rpx; }
.section-title { font-size: 34rpx; font-weight: bold; color: #333333; margin-bottom: 20rpx; }
.info-item { display: flex; justify-content: space-between; padding: 24rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.info-item:last-child { border-bottom: none; }
.label { font-size: 32rpx; color: #999999; }
.value { font-size: 32rpx; color: #333333; max-width: 60%; text-align: right; }
.value.price { color: #ff4d4f; font-weight: bold; }
.value.link { color: #1890ff; }
.evidence-image { width: 100%; height: 400rpx; border-radius: 16rpx; }
.action-section { position: fixed; bottom: 0; left: 0; right: 0; padding: 30rpx; background: #ffffff; padding-bottom: calc(30rpx + env(safe-area-inset-bottom)); }
.action-btn { text-align: center; padding: 32rpx; border-radius: 16rpx; font-size: 36rpx; font-weight: bold; margin-bottom: 20rpx; background: #43a047; color: #ffffff; }
.action-btn.green { background: #52c41a; }
.action-btn.orange { background: #fa8c16; }
.action-btn.blue { background: #1890ff; }
.action-btn.call { background: #ffffff; color: #333333; border: 2rpx solid #d9d9d9; }
</style>