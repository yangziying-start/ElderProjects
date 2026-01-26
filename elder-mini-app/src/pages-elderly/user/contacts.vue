<template>
  <view class="container">
    <view class="contact-list">
      <view class="contact-card" v-for="contact in contacts" :key="contact.id">
        <view class="contact-info">
          <text class="contact-name">{{ contact.name }}</text>
          <text class="contact-relation">{{ contact.relation }}</text>
          <text class="contact-phone">{{ contact.phone }}</text>
        </view>
        <view class="contact-actions">
          <view class="call-btn" @click="callPhone(contact.phone)">
            <text>📞 拨打</text>
          </view>
        </view>
      </view>
      
      <view class="empty" v-if="contacts.length === 0">
        <text>暂无紧急联系人</text>
      </view>
    </view>

    <view class="tip">
      <text>紧急联系人将在您一键呼叫时收到通知</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const contacts = ref([])

onShow(async () => {
  try {
    contacts.value = await get('/user/contacts') || []
  } catch (e) {
    console.error(e)
  }
})

const callPhone = (phone) => {
  uni.makePhoneCall({
    phoneNumber: phone,
    fail: (err) => {
      // 用户取消拨打不提示错误
      if (err.errMsg && err.errMsg.includes('cancel')) return
      console.error('拨打电话失败:', err)
    }
  })
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.contact-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contact-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.contact-relation {
  font-size: 30rpx;
  color: #43a047;
  background: #e8f5e9;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
  display: inline-block;
  margin: 16rpx 0;
}

.contact-phone {
  font-size: 34rpx;
  color: #666666;
  display: block;
}

.call-btn {
  background: #52c41a;
  color: #ffffff;
  padding: 24rpx 40rpx;
  border-radius: 16rpx;
  font-size: 34rpx;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999999;
  font-size: 32rpx;
}

.tip {
  text-align: center;
  padding: 40rpx;
  color: #999999;
  font-size: 28rpx;
}
</style>
