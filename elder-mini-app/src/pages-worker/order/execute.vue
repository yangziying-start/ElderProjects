<template>
  <view class="container">
    <!-- 订单信息卡片 -->
    <view class="order-info-card">
      <view class="order-header">
        <text class="service-name">{{ order.serviceName }}</text>
        <view class="status-tag" :class="'status-' + order.status">{{ order.statusText }}</view>
      </view>
      <view class="order-detail">
        <text class="detail-item">👴 {{ order.elderlyName }}</text>
        <text class="detail-item">📍 {{ order.address }}</text>
        <text class="detail-item">🕐 {{ order.appointmentTime }}</text>
        <text class="detail-item" v-if="order.remark">📝 {{ order.remark }}</text>
      </view>
    </view>

    <!-- 步骤指示器 -->
    <view class="step-section">
      <view class="step" :class="{ active: step >= 1, done: step > 1 }">
        <text class="step-num">1</text>
        <text class="step-text">验证服务码</text>
      </view>
      <view class="step-line" :class="{ active: step > 1 }"></view>
      <view class="step" :class="{ active: step >= 2, done: step > 2 }">
        <text class="step-num">2</text>
        <text class="step-text">执行服务</text>
      </view>
      <view class="step-line" :class="{ active: step > 2 }"></view>
      <view class="step" :class="{ active: step >= 3 }">
        <text class="step-num">3</text>
        <text class="step-text">上传凭证</text>
      </view>
    </view>

    <!-- 步骤1：验证服务码 -->
    <view class="content-section" v-if="step === 1">
      <view class="section-header">
        <text class="section-title">请输入服务码</text>
        <text class="section-desc">向老人或其家属索要6位数字验证码</text>
      </view>
      <view class="code-input-wrapper" @click="focusInput">
        <view class="code-box" v-for="(item, index) in 6" :key="index">
          <text class="code-char">{{ serviceCode[index] || '' }}</text>
        </view>
        <input class="hidden-input" ref="codeInput" v-model="serviceCode" type="number" maxlength="6" 
          @input="onCodeInput" :focus="inputFocused" />
      </view>
      <view class="tip-box">
        <text class="tip-icon">💡</text>
        <text class="tip-text">服务码每日动态更新，请确保输入当日有效的验证码</text>
      </view>
      <view class="submit-btn" :class="{ disabled: serviceCode.length !== 6 }" @click="verifyCode">
        验证打卡
      </view>
    </view>

    <!-- 步骤2：执行服务 -->
    <view class="content-section" v-if="step === 2">
      <view class="section-header">
        <text class="section-title">服务进行中</text>
        <text class="section-desc">请按照服务要求完成服务</text>
      </view>
      <view class="service-progress">
        <view class="progress-icon">🔧</view>
        <text class="progress-text">正在为 {{ order.elderlyName }} 提供服务</text>
        <text class="progress-service">{{ order.serviceName }}</text>
      </view>
      <view class="service-requirements" v-if="order.remark">
        <text class="req-title">服务要求：</text>
        <text class="req-content">{{ order.remark }}</text>
      </view>
      <view class="submit-btn" @click="goUpload">服务完成，上传凭证</view>
    </view>

    <!-- 步骤3：上传凭证 -->
    <view class="content-section" v-if="step === 3">
      <view class="section-header">
        <text class="section-title">上传服务凭证</text>
        <text class="section-desc">请拍照上传服务完成后的照片作为凭证</text>
      </view>
      <view class="upload-area" @click="chooseImage">
        <image v-if="evidence" :src="getImageUrl(evidence)" class="evidence-img" mode="aspectFill" />
        <view v-else class="upload-placeholder">
          <text class="upload-icon">📷</text>
          <text class="upload-text">点击拍照或选择图片</text>
        </view>
      </view>
      <view class="remark-section">
        <text class="remark-label">服务备注（选填）</text>
        <textarea class="remark-input" v-model="completeRemark" placeholder="请输入服务完成情况说明..." />
      </view>
      <view class="submit-btn" :class="{ disabled: !evidence }" @click="submitComplete">
        提交完成
      </view>
    </view>

    <!-- 联系老人按钮 -->
    <view class="float-btn" @click="callElderly">
      <text class="float-icon">📞</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const step = ref(1)
const serviceCode = ref('')
const evidence = ref('')
const completeRemark = ref('')
const order = ref({})
const orderId = ref('')
const orderType = ref('cleaning')
const inputFocused = ref(true)

const focusInput = () => {
  inputFocused.value = false
  setTimeout(() => {
    inputFocused.value = true
  }, 100)
}

onLoad(async (options) => {
  orderId.value = options.id
  orderType.value = options.type || 'cleaning'
  try {
    order.value = await get(`/worker/order/${orderId.value}?type=${orderType.value}`) || {}
    // 根据订单状态设置当前步骤
    // 保洁: 0-待服务 1-服务中 2-待确认
    // 餐饮: 0-待配送 1-配送中 2-已送达
    // 医疗: 0-排队中 1-巡诊中 2-已完成
    if (order.value.status === 1) {
      step.value = 2
    } else if (order.value.status >= 2) {
      step.value = 3
    }
  } catch (e) {
    console.error(e)
  }
})


const onCodeInput = (e) => {
  serviceCode.value = e.detail.value.replace(/\D/g, '').substring(0, 6)
}

const verifyCode = async () => {
  if (serviceCode.value.length !== 6) {
    uni.showToast({ title: '请输入6位服务码', icon: 'none' })
    return
  }
  uni.showLoading({ title: '验证中...' })
  try {
    await post('/worker/verify-code', {
      orderId: orderId.value,
      serviceCode: serviceCode.value,
      type: orderType.value
    })
    uni.hideLoading()
    uni.showToast({ title: '验证成功，开始服务', icon: 'success' })
    order.value.status = 1
    order.value.statusText = '服务中'
    step.value = 2
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}

const goUpload = () => { step.value = 3 }

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera', 'album'],
    success: (res) => { 
      uploadImage(res.tempFilePaths[0])
    }
  })
}

const uploadImage = (filePath) => {
  uni.showLoading({ title: '上传中...' })
  const token = uni.getStorageSync('token')
  uni.uploadFile({
    url: 'http://localhost:8080/api/file/upload',
    filePath: filePath,
    name: 'file',
    header: {
      'Authorization': token ? `Bearer ${token}` : ''
    },
    success: (res) => {
      uni.hideLoading()
      if (res.statusCode === 200) {
        const data = JSON.parse(res.data)
        if (data.code === 200) {
          // 存储相对路径，显示时拼接完整URL
          evidence.value = data.data
          uni.showToast({ title: '上传成功', icon: 'success' })
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
        }
      } else {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({ title: '上传失败', icon: 'none' })
    }
  })
}

// 获取图片完整URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  return `http://localhost:8080/api/file/view/${path}`
}

const submitComplete = async () => {
  if (!evidence.value) {
    uni.showToast({ title: '请上传服务凭证', icon: 'none' })
    return
  }
  uni.showLoading({ title: '提交中...' })
  try {
    await post('/worker/complete', {
      orderId: orderId.value,
      evidence: evidence.value,
      remark: completeRemark.value,
      type: orderType.value
    })
    uni.hideLoading()
    uni.showModal({
      title: '提交成功',
      content: '服务已完成，等待老人确认',
      showCancel: false,
      success: () => {
        uni.navigateBack()
      }
    })
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}

const callElderly = () => {
  if (order.value.elderlyPhone) {
    uni.makePhoneCall({ phoneNumber: order.value.elderlyPhone })
  } else {
    uni.showToast({ title: '暂无联系方式', icon: 'none' })
  }
}
</script>

<style scoped>
.container { padding: 30rpx; background: #f5f5f5; min-height: 100vh; padding-bottom: 150rpx; }
.order-info-card { background: linear-gradient(135deg, #43a047, #66bb6a); border-radius: 24rpx; padding: 40rpx; margin-bottom: 30rpx; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.service-name { font-size: 40rpx; font-weight: bold; color: #fff; }
.status-tag { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 8rpx; background: rgba(255,255,255,0.2); color: #fff; }
.detail-item { font-size: 28rpx; color: rgba(255,255,255,0.9); display: block; margin-bottom: 12rpx; }
.step-section { display: flex; align-items: center; background: #fff; border-radius: 20rpx; padding: 40rpx 30rpx; margin-bottom: 30rpx; }
.step { display: flex; flex-direction: column; align-items: center; }
.step-num { width: 56rpx; height: 56rpx; border-radius: 50%; background: #e0e0e0; color: #999; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: bold; }
.step.active .step-num { background: #43a047; color: #fff; }
.step.done .step-num { background: #52c41a; }
.step-text { font-size: 24rpx; color: #999; margin-top: 12rpx; white-space: nowrap; }
.step.active .step-text { color: #333; font-weight: bold; }
.step-line { flex: 1; height: 4rpx; background: #e0e0e0; margin: 0 16rpx; margin-bottom: 36rpx; }
.step-line.active { background: #52c41a; }
.content-section { background: #fff; border-radius: 20rpx; padding: 40rpx; }
.section-header { margin-bottom: 40rpx; }
.section-title { font-size: 40rpx; font-weight: bold; color: #333; display: block; }
.section-desc { font-size: 28rpx; color: #999; margin-top: 12rpx; display: block; }
.code-input-wrapper { display: flex; justify-content: center; gap: 16rpx; position: relative; margin-bottom: 40rpx; }
.code-box { width: 80rpx; height: 100rpx; border: 2rpx solid #d9d9d9; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; background: #fafafa; }
.code-char { font-size: 48rpx; font-weight: bold; color: #333; }
.hidden-input { position: absolute; top: 0; left: 0; width: 100%; height: 100%; opacity: 0; z-index: 1; }
.tip-box { display: flex; align-items: flex-start; background: #fff7e6; border-radius: 12rpx; padding: 24rpx; margin-bottom: 40rpx; }
.tip-icon { font-size: 32rpx; margin-right: 16rpx; }
.tip-text { font-size: 26rpx; color: #fa8c16; flex: 1; line-height: 1.5; }
.submit-btn { background: #43a047; color: #fff; text-align: center; padding: 32rpx; border-radius: 16rpx; font-size: 36rpx; font-weight: bold; }
.submit-btn.disabled { background: #d9d9d9; color: #999; }
.service-progress { text-align: center; padding: 60rpx 0; }
.progress-icon { font-size: 100rpx; margin-bottom: 30rpx; }
.progress-text { font-size: 32rpx; color: #666; display: block; margin-bottom: 16rpx; }
.progress-service { font-size: 40rpx; font-weight: bold; color: #43a047; }
.service-requirements { background: #f5f5f5; border-radius: 12rpx; padding: 24rpx; margin-bottom: 40rpx; }
.req-title { font-size: 28rpx; color: #999; display: block; margin-bottom: 12rpx; }
.req-content { font-size: 30rpx; color: #333; line-height: 1.6; }
.upload-area { border: 2rpx dashed #d9d9d9; border-radius: 16rpx; height: 400rpx; display: flex; align-items: center; justify-content: center; margin-bottom: 30rpx; overflow: hidden; }
.upload-placeholder { text-align: center; }
.upload-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.upload-text { font-size: 30rpx; color: #999; }
.evidence-img { width: 100%; height: 100%; }
.remark-section { margin-bottom: 40rpx; }
.remark-label { font-size: 28rpx; color: #666; display: block; margin-bottom: 16rpx; }
.remark-input { width: 100%; height: 200rpx; border: 2rpx solid #d9d9d9; border-radius: 12rpx; padding: 20rpx; font-size: 30rpx; box-sizing: border-box; }
.float-btn { position: fixed; right: 40rpx; bottom: 200rpx; width: 100rpx; height: 100rpx; background: #43a047; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 8rpx 24rpx rgba(67, 160, 71, 0.4); }
.float-icon { font-size: 48rpx; }
</style>