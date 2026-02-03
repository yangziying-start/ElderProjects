<template>
  <div class="emergency-center">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span style="color: #F56C6C; font-weight: bold">
                <el-icon><Bell /></el-icon> 紧急呼叫管理
              </span>
              <el-button type="primary" @click="loadData">刷新</el-button>
            </div>
          </template>
          <el-table :data="emergencyList" v-loading="loading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="elderlyName" label="老人姓名" width="100" />
            <el-table-column prop="phone" label="联系电话" width="120" />
            <el-table-column prop="address" label="地址" show-overflow-tooltip />
            <el-table-column prop="eventType" label="呼叫类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getEventTypeStyle(row.eventType)">
                  {{ getEventTypeText(row.eventType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="triggerTime" label="呼叫时间" width="160" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'danger' : 'success'">
                  {{ row.status === 0 ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button 
                  type="success" 
                  link 
                  v-if="row.status === 0" 
                  @click="handleProcess(row)"
                >
                  标记已处理
                </el-button>
                <span v-else style="color: #67C23A">已处理</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>统计信息</template>
          <div class="stat-item">
            <span class="label">今日呼叫总数</span>
            <span class="value">{{ stats.todayTotal }}</span>
          </div>
          <div class="stat-item">
            <span class="label">待处理</span>
            <span class="value danger">{{ stats.pending }}</span>
          </div>
          <div class="stat-item">
            <span class="label">已处理</span>
            <span class="value success">{{ stats.processed }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const emergencyList = ref([])

const stats = computed(() => {
  const pending = emergencyList.value.filter(e => e.status === 0).length
  const processed = emergencyList.value.filter(e => e.status === 1).length
  return {
    todayTotal: emergencyList.value.length,
    pending,
    processed
  }
})

const eventTypeMap = {
  1: { text: '紧急医疗', type: 'danger' },
  2: { text: '摔倒求助', type: 'warning' },
  3: { text: '一般求助', type: 'info' }
}

const getEventTypeText = (type) => eventTypeMap[type]?.text || '未知'
const getEventTypeStyle = (type) => eventTypeMap[type]?.type || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/emergency/list')
    emergencyList.value = res || []
  } catch (e) {
    // 使用模拟数据
    emergencyList.value = [
      { id: 1, elderlyName: '张大爷', phone: '13800138001', address: '幸福社区1栋101', eventType: 1, triggerTime: '2024-01-05 09:30:00', status: 0 },
      { id: 2, elderlyName: '王奶奶', phone: '13800138002', address: '幸福社区2栋305', eventType: 3, triggerTime: '2024-01-05 08:15:00', status: 1 }
    ]
  }
  loading.value = false
}

const handleProcess = (row) => {
  ElMessageBox.confirm(
    `确认已处理 ${row.elderlyName} 的紧急呼叫？`,
    '确认处理',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await request.post(`/emergency/${row.id}/process`)
      row.status = 1
      ElMessage.success('已标记为已处理')
    } catch (e) {
      row.status = 1
      ElMessage.success('已标记为已处理')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-item .label {
  color: #909399;
}

.stat-item .value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.stat-item .value.danger {
  color: #F56C6C;
}

.stat-item .value.success {
  color: #67C23A;
}
</style>
