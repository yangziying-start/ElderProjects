<template>
  <div class="building-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>楼栋配送员管理</span>
          <el-text type="info" size="small">为每个楼栋分配配送员，订餐时将自动分配对应楼栋的配送员</el-text>
        </div>
      </template>

      <!-- 楼栋列表 -->
      <el-table :data="buildingList" v-loading="loading" stripe>
        <el-table-column prop="building" label="楼栋" width="120" />
        <el-table-column prop="workerCount" label="配送员数量" width="120">
          <template #default="{ row }">
            <el-tag :type="row.workerCount > 0 ? 'success' : 'danger'">
              {{ row.workerCount }}人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配送员列表" min-width="400">
          <template #default="{ row }">
            <div class="worker-tags" v-if="row.workers && row.workers.length > 0">
              <el-tag 
                v-for="worker in row.workers" 
                :key="worker.workerId"
                :type="worker.isPrimary === 1 ? 'warning' : 'info'"
                closable
                @close="handleRemoveWorker(row.building, worker.workerId)"
                style="margin-right: 8px; margin-bottom: 4px;"
              >
                {{ worker.workerName }}
                <span v-if="worker.isPrimary === 1">(主)</span>
                <span class="worker-phone">{{ worker.workerPhone }}</span>
              </el-tag>
            </div>
            <el-text type="info" v-else>暂未分配配送员</el-text>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAssign(row)">分配配送员</el-button>
            <el-button type="success" link @click="handleSetPrimary(row)" v-if="row.workerCount > 1">
              设置主负责人
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分配配送员弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配配送员" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="楼栋">
          <el-input :value="assignForm.building" disabled />
        </el-form-item>
        <el-form-item label="选择配送员">
          <el-select v-model="assignForm.workerId" placeholder="请选择配送员" style="width: 100%">
            <el-option 
              v-for="worker in availableWorkers" 
              :key="worker.id" 
              :label="`${worker.name} (${worker.phone})`" 
              :value="worker.id"
              :disabled="isWorkerAssigned(worker.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设为主负责人">
          <el-switch v-model="assignForm.isPrimary" :active-value="1" :inactive-value="0" />
          <el-text type="info" size="small" style="margin-left: 10px;">主负责人将优先接收该楼栋订单</el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设置主负责人弹窗 -->
    <el-dialog v-model="primaryDialogVisible" title="设置主负责人" width="400px">
      <el-form :model="primaryForm" label-width="80px">
        <el-form-item label="楼栋">
          <el-input :value="primaryForm.building" disabled />
        </el-form-item>
        <el-form-item label="主负责人">
          <el-select v-model="primaryForm.workerId" placeholder="请选择" style="width: 100%">
            <el-option 
              v-for="worker in currentBuildingWorkers" 
              :key="worker.workerId" 
              :label="worker.workerName" 
              :value="worker.workerId" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="primaryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSetPrimary" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const buildingList = ref([])
const availableWorkers = ref([])

// 分配配送员
const assignDialogVisible = ref(false)
const assignForm = reactive({
  building: '',
  workerId: null,
  isPrimary: 0
})
const currentBuildingWorkers = ref([])

// 设置主负责人
const primaryDialogVisible = ref(false)
const primaryForm = reactive({
  building: '',
  workerId: null
})

const isWorkerAssigned = (workerId) => {
  const building = buildingList.value.find(b => b.building === assignForm.building)
  if (!building || !building.workers) return false
  return building.workers.some(w => w.workerId === workerId)
}

const loadData = async () => {
  loading.value = true
  try {
    buildingList.value = await request.get('/admin/building/list')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadWorkers = async () => {
  try {
    availableWorkers.value = await request.get('/admin/building/workers')
  } catch (e) {
    console.error(e)
  }
}

const handleAssign = (row) => {
  assignForm.building = row.building
  assignForm.workerId = null
  assignForm.isPrimary = row.workerCount === 0 ? 1 : 0
  currentBuildingWorkers.value = row.workers || []
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  if (!assignForm.workerId) {
    ElMessage.warning('请选择配送员')
    return
  }
  
  submitLoading.value = true
  try {
    await request.post('/admin/building/assign', {
      building: assignForm.building,
      workerId: assignForm.workerId,
      isPrimary: assignForm.isPrimary
    })
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

const handleRemoveWorker = async (building, workerId) => {
  try {
    await ElMessageBox.confirm('确定要移除该配送员吗？', '提示', { type: 'warning' })
    await request.delete('/admin/building/remove', { params: { building, workerId } })
    ElMessage.success('移除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleSetPrimary = (row) => {
  primaryForm.building = row.building
  primaryForm.workerId = row.workers.find(w => w.isPrimary === 1)?.workerId || null
  currentBuildingWorkers.value = row.workers || []
  primaryDialogVisible.value = true
}

const submitSetPrimary = async () => {
  if (!primaryForm.workerId) {
    ElMessage.warning('请选择主负责人')
    return
  }
  
  submitLoading.value = true
  try {
    await request.post('/admin/building/set-primary', {
      building: primaryForm.building,
      workerId: primaryForm.workerId
    })
    ElMessage.success('设置成功')
    primaryDialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadWorkers()
})
</script>

<style scoped>
.building-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.worker-tags {
  display: flex;
  flex-wrap: wrap;
}

.worker-phone {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}
</style>
