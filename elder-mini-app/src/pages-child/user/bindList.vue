<template>
  <view class="container">
    <view class="elderly-list" v-if="elderlyList.length > 0">
      <view class="elderly-card" v-for="item in elderlyList" :key="item.id">
        <view class="elderly-info">
          <view class="avatar">👴</view>
          <view class="info">
            <text class="name">{{ item.name || '未设置姓名' }}</text>
            <text class="phone">{{ item.phone }}</text>
            <text class="bindTime">绑定时间：{{ item.bindTime || '-' }}</text>
          </view>
        </view>
        <view class="actions">
          <view class="action-btn call" @click="callElderly(item)">📞 拨打</view>
          <view class="action-btn unbind" @click="unbindElderly(item)">解绑</view>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">👴</text>
      <text class="empty-text">暂未绑定老人</text>
      <text class="empty-tip">点击下方按钮绑定老人</text>
    </view>

    <view class="add-btn" @click="showBindDialog">
      <text>+ 绑定老人</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const elderlyList = ref([])

onShow(async () => {
  await loadElderlyList()
})

const loadElderlyList = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    elderlyList.value = await get('/user/bindElderly') || []
    uni.hideLoading()
  } catch (e) {
    uni.hideLoading()
    console.error(e)
    elderlyList.value = []
  }
}

const showBindDialog = () => {
  uni.showModal({
    title: '绑定老人',
    editable: true,
    placeholderText: '请输入老人手机号',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          uni.showLoading({ title: '绑定中...' })
          await post('/user/bindElderly', { phone: res.content })
          uni.hideLoading()
          uni.showToast({ title: '绑定成功', icon: 'success' })
          await loadElderlyList()
        } catch (e) {
          uni.hideLoading()
          uni.showToast({ title: e.message || '绑定失败', icon: 'none' })
        }
      }
    }
  })
}

const unbindElderly = (item) => {
  uni.showModal({
    title: '确认解绑',
    content: `确定要解绑 ${item.name || item.phone} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '解绑中...' })
          await post('/user/unbindElderly', { elderlyId: item.id })
          uni.hideLoading()
          // 从列表中移除该项
          elderlyList.value = elderlyList.value.filter(e => e.id !== item.id)
          uni.showToast({ title: '解绑成功', icon: 'success' })
        } catch (e) {
          uni.hideLoading()
          uni.showToast({ title: e.message || '解绑失败', icon: 'none' })
        }
      }
    }
  })
}

const callElderly = (item) => {
  if (item.phone) {
    uni.makePhoneCall({ 
      phoneNumber: item.phone, 
      fail: (err) => {
        // 用户取消拨打不提示错误
        if (err.errMsg && err.errMsg.includes('cancel')) return
        console.error('拨打电话失败:', err)
      }
    })
  }
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 150rpx;
}

.elderly-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
}

.elderly-info {
  display: flex;
  align-items: center;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  background: #e8f5e9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50rpx;
  margin-right: 24rpx;
}

.info {
  flex: 1;
}

.name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.phone {
  font-size: 30rpx;
  color: #666666;
  margin-top: 8rpx;
  display: block;
}

.bindTime {
  font-size: 26rpx;
  color: #999999;
  margin-top: 8rpx;
  display: block;
}

.actions {
  display: flex;
  gap: 20rpx;
  margin-top: 24rpx;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 30rpx;
}

.action-btn.call {
  background: #e8f5e9;
  color: #43a047;
}

.action-btn.unbind {
  background: #fff1f0;
  color: #ff4d4f;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  background: #ffffff;
  border-radius: 24rpx;
}

.empty-icon {
  font-size: 100rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 36rpx;
  color: #333333;
  display: block;
}

.empty-tip {
  font-size: 28rpx;
  color: #999999;
  margin-top: 12rpx;
  display: block;
}

.add-btn {
  position: fixed;
  bottom: 50rpx;
  left: 30rpx;
  right: 30rpx;
  height: 100rpx;
  background: #43a047;
  color: #ffffff;
  border-radius: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: bold;
  box-shadow: 0 8rpx 24rpx rgba(67, 160, 71, 0.3);
}
</style>
