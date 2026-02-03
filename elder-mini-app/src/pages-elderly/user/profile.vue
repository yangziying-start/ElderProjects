<template>
  <view class="container">
    <view class="form-card">
      <view class="form-item">
        <text class="label">姓名</text>
        <input class="input" v-model="form.name" placeholder="请输入姓名" maxlength="20" />
      </view>
      
      <view class="form-item">
        <text class="label">手机号</text>
        <input class="input" v-model="form.phone" disabled placeholder="手机号不可修改" />
      </view>
      
      <view class="form-item">
        <text class="label">身份证号</text>
        <input class="input" v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
      </view>
      
      <view class="form-item">
        <text class="label">地址</text>
        <textarea class="textarea" v-model="form.address" placeholder="请输入详细地址" maxlength="200" />
      </view>
      
      <view class="submit-btn" @click="handleSubmit">保存修改</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, put } from '@/utils/request'

const form = ref({
  name: '',
  phone: '',
  idCard: '',
  address: ''
})

onMounted(async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const userInfo = await get('/user/info')
    form.value = {
      name: userInfo.name || '',
      phone: userInfo.phone || '',
      idCard: userInfo.idCard || '',
      address: userInfo.address || ''
    }
    uni.hideLoading()
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
})

const handleSubmit = async () => {
  if (!form.value.name) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '保存中...' })
    await put('/user/info', {
      name: form.value.name,
      idCard: form.value.idCard,
      address: form.value.address
    })
    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.form-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 32rpx;
  color: #333333;
  margin-bottom: 16rpx;
  display: block;
}

.input {
  width: 100%;
  height: 100rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 0 30rpx;
  font-size: 34rpx;
  box-sizing: border-box;
}

.input[disabled] {
  color: #999999;
}

.textarea {
  width: 100%;
  height: 200rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 20rpx 30rpx;
  font-size: 34rpx;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  height: 100rpx;
  background: #43a047;
  color: #ffffff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: bold;
  margin-top: 40rpx;
}
</style>
