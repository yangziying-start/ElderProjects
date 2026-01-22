<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-label">订单总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayOrders }}</div>
          <div class="stat-label">今日订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card emergency">
          <div class="stat-value">{{ stats.pendingEmergency }}</div>
          <div class="stat-label">待处理呼叫</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card>
          <template #header>用户分布</template>
          <div class="user-stats">
            <div class="user-stat-item">
              <span class="label">老人</span>
              <span class="value">{{ stats.elderlyCount }}</span>
            </div>
            <div class="user-stat-item">
              <span class="label">子女</span>
              <span class="value">{{ stats.childCount }}</span>
            </div>
            <div class="user-stat-item">
              <span class="label">服务人员</span>
              <span class="value">{{ stats.workerCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>订单状态</template>
          <div class="order-stats">
            <div class="order-stat-item">
              <span class="label">待接单</span>
              <span class="value warning">{{ stats.pendingOrders }}</span>
            </div>
            <div class="order-stat-item">
              <span class="label">已完成</span>
              <span class="value success">{{ stats.completedOrders }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>今日应急</template>
          <div class="emergency-stats">
            <div class="emergency-stat-item">
              <span class="label">今日呼叫</span>
              <span class="value">{{ stats.todayEmergency }}</span>
            </div>
            <div class="emergency-stat-item">
              <span class="label">待处理</span>
              <span class="value danger">{{ stats.pendingEmergency }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>最近订单</template>
          <el-table :data="recentOrders" size="small">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="serviceName" label="服务项目" />
            <el-table-column prop="elderlyName" label="老人" width="100" />
            <el-table-column prop="amount" label="积分" width="80">
              <template #default="{ row }">{{ row.amount || '-' }}</template>
            </el-table-column>
            <el-table-column prop="statusText" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref({
  userCount: 0,
  elderlyCount: 0,
  childCount: 0,
  workerCount: 0,
  orderCount: 0,
  todayOrders: 0,
  pendingOrders: 0,
  completedOrders: 0,
  pendingEmergency: 0,
  todayEmergency: 0
})

const recentOrders = ref([])

const getStatusType = (status) => {
  if (status === 0) return 'info'
  if (status === 1) return 'warning'
  if (status === 2) return 'warning'
  if (status === 3) return 'success'
  if (status === 4) return 'danger'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const loadData = async () => {
  try {
    stats.value = await request.get('/admin/dashboard/stats') || stats.value
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
  
  try {
    recentOrders.value = await request.get('/admin/dashboard/recent-orders') || []
  } catch (e) {
    console.error('加载最近订单失败:', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.stat-card.emergency .stat-value {
  color: #F56C6C;
}

.stat-label {
  color: #909399;
  margin-top: 10px;
}

.user-stats, .order-stats, .emergency-stats {
  padding: 10px 0;
}

.user-stat-item, .order-stat-item, .emergency-stat-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.user-stat-item:last-child, .order-stat-item:last-child, .emergency-stat-item:last-child {
  border-bottom: none;
}

.label {
  color: #909399;
}

.value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.value.warning {
  color: #E6A23C;
}

.value.success {
  color: #67C23A;
}

.value.danger {
  color: #F56C6C;
}
</style>
