<template>
  <div class="building-manage">
    <!-- 楼栋管理 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>楼栋管理</span>
          <el-button type="primary" @click="handleAddBuilding">新增楼栋</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" style="margin-bottom: 16px;">
        <el-form-item label="楼栋名称">
          <el-input v-model="searchForm.name" placeholder="请输入楼栋名称" clearable @keyup.enter="loadBuildingPage" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBuildingPage">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 楼栋列表 -->
      <el-table :data="buildingPageList" v-loading="buildingLoading" stripe>
        <el-table-column prop="code" label="楼栋编号" width="120" />
        <el-table-column prop="name" label="楼栋名称" width="150" />
        <el-table-column prop="floors" label="楼层数" width="100">
          <template #default="{ row }">{{ row.floors || '-' }}</template>
        </el-table-column>
        <el-table-column prop="units" label="单元数" width="100">
          <template #default="{ row }">{{ row.units || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditBuilding(row)">编辑</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDeleteBuilding(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadBuildingPage"
        @current-change="loadBuildingPage"
      />
    </el-card>

    <!-- 楼栋配送员管理 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>楼栋配送员管理</span>
          <el-text type="info" size="small">为每个楼栋分配配送员，订餐时将自动分配对应楼栋的配送员</el-text>
        </div>
      </template>

      <el-table :data="buildingWorkerList" v-loading="workerLoading" stripe>
        <el-table-column prop="building" label="楼栋" width="120" />
        <el-table-column prop="workerCount" label="配送员数量" width="120">
          <template #default="{ row }">
            <el-tag :type="row.workerCount > 0 ? 'success' : 'danger'">{{ row.workerCount }}人</el-tag>
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
            <el-button type="success" link @click="handleSetPrimary(row)" v-if="row.workerCount > 1">设置主负责人</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑楼栋弹窗 -->
    <el-dialog v-model="buildingDialogVisible" :title="buildingForm.id ? '编辑楼栋' : '新增楼栋'" width="500px">
      <el-form :model="buildingForm" :rules="buildingRules" ref="buildingFormRef" label-width="100px">
        <el-form-item label="楼栋编号" prop="code">
          <el-input v-model="buildingForm.code" placeholder="请输入楼栋编号，如：A、B、1号" />
        </el-form-item>
        <el-form-item label="楼栋名称" prop="name">
          <el-input v-model="buildingForm.name" placeholder="请输入楼栋名称，如：A栋、1号楼" />
        </el-form-item>
        <el-form-item label="楼层数" prop="floors">
          <el-input-number v-model="buildingForm.floors" :min="1" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="单元数" prop="units">
          <el-input-number v-model="buildingForm.units" :min="1" :max="20" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="buildingForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="buildingForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBuilding" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

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
              :disabled="getWorkerDisabledInfo(worker.id).disabled"
            >
              <span>{{ worker.name }} ({{ worker.phone }})</span>
              <span v-if="getWorkerDisabledInfo(worker.id).disabled" style="color: #909399; margin-left: 8px; font-size: 12px;">
                - {{ getWorkerDisabledInfo(worker.id).tip }}
              </span>
            </el-option>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// ==================== 楼栋管理 ====================
const buildingLoading = ref(false)
const buildingPageList = ref([])
const searchForm = reactive({ name: '', status: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const submitLoading = ref(false)

const buildingDialogVisible = ref(false)
const buildingFormRef = ref(null)
const buildingForm = reactive({
  id: null, code: '', name: '', floors: null, units: null, status: 1, remark: ''
})
const buildingRules = {
  name: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }]
}

const loadBuildingPage = async () => {
  buildingLoading.value = true
  try {
    const res = await request.get('/admin/building/page', {
      params: { pageNum: pagination.pageNum, pageSize: pagination.pageSize, name: searchForm.name || undefined, status: searchForm.status }
    })
    buildingPageList.value = res.records
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { buildingLoading.value = false }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = null
  pagination.pageNum = 1
  loadBuildingPage()
}

const handleAddBuilding = () => {
  Object.assign(buildingForm, { id: null, code: '', name: '', floors: null, units: null, status: 1, remark: '' })
  buildingDialogVisible.value = true
}

const handleEditBuilding = (row) => {
  Object.assign(buildingForm, row)
  buildingDialogVisible.value = true
}

const submitBuilding = async () => {
  const valid = await buildingFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (buildingForm.id) {
      await request.put(`/admin/building/${buildingForm.id}`, buildingForm)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/building', buildingForm)
      ElMessage.success('新增成功')
    }
    buildingDialogVisible.value = false
    loadBuildingPage()
    loadWorkerData()
  } catch (e) { console.error(e) } finally { submitLoading.value = false }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确定要${action}该楼栋吗？`, '提示', { type: 'warning' })
    await request.put(`/admin/building/${row.id}/status`, null, { params: { status: newStatus } })
    ElMessage.success(`${action}成功`)
    loadBuildingPage()
    loadWorkerData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleDeleteBuilding = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该楼栋吗？删除后不可恢复', '提示', { type: 'warning' })
    await request.delete(`/admin/building/${row.id}`)
    ElMessage.success('删除成功')
    loadBuildingPage()
    loadWorkerData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

// ==================== 楼栋配送员管理 ====================
const workerLoading = ref(false)
const buildingWorkerList = ref([])
const availableWorkers = ref([])
const assignDialogVisible = ref(false)
const assignForm = reactive({ building: '', workerId: null, isPrimary: 0 })
const currentBuildingWorkers = ref([])
const primaryDialogVisible = ref(false)
const primaryForm = reactive({ building: '', workerId: null })

const isWorkerAssignedToCurrentBuilding = (workerId) => {
  const building = buildingWorkerList.value.find(b => b.building === assignForm.building)
  return building?.workers?.some(w => w.workerId === workerId) || false
}

const isWorkerAssignedToOtherBuilding = (workerId) => {
  for (const building of buildingWorkerList.value) {
    if (building.building === assignForm.building) continue
    if (building.workers?.some(w => w.workerId === workerId)) return building.building
  }
  return null
}

const getWorkerDisabledInfo = (workerId) => {
  if (isWorkerAssignedToCurrentBuilding(workerId)) return { disabled: true, tip: '已分配给当前楼栋' }
  const otherBuilding = isWorkerAssignedToOtherBuilding(workerId)
  if (otherBuilding) return { disabled: true, tip: `已分配给${otherBuilding}` }
  return { disabled: false, tip: '' }
}

const loadWorkerData = async () => {
  workerLoading.value = true
  try { buildingWorkerList.value = await request.get('/admin/building/list') }
  catch (e) { console.error(e) } finally { workerLoading.value = false }
}

const loadWorkers = async () => {
  try { availableWorkers.value = await request.get('/admin/building/workers') }
  catch (e) { console.error(e) }
}

const handleAssign = (row) => {
  assignForm.building = row.building
  assignForm.workerId = null
  assignForm.isPrimary = row.workerCount === 0 ? 1 : 0
  currentBuildingWorkers.value = row.workers || []
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  if (!assignForm.workerId) { ElMessage.warning('请选择配送员'); return }
  submitLoading.value = true
  try {
    await request.post('/admin/building/assign', { building: assignForm.building, workerId: assignForm.workerId, isPrimary: assignForm.isPrimary })
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    loadWorkerData()
  } catch (e) { console.error(e) } finally { submitLoading.value = false }
}

const handleRemoveWorker = async (building, workerId) => {
  try {
    await ElMessageBox.confirm('确定要移除该配送员吗？', '提示', { type: 'warning' })
    await request.delete('/admin/building/remove', { params: { building, workerId } })
    ElMessage.success('移除成功')
    loadWorkerData()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleSetPrimary = (row) => {
  primaryForm.building = row.building
  primaryForm.workerId = row.workers.find(w => w.isPrimary === 1)?.workerId || null
  currentBuildingWorkers.value = row.workers || []
  primaryDialogVisible.value = true
}

const submitSetPrimary = async () => {
  if (!primaryForm.workerId) { ElMessage.warning('请选择主负责人'); return }
  submitLoading.value = true
  try {
    await request.post('/admin/building/set-primary', { building: primaryForm.building, workerId: primaryForm.workerId })
    ElMessage.success('设置成功')
    primaryDialogVisible.value = false
    loadWorkerData()
  } catch (e) { console.error(e) } finally { submitLoading.value = false }
}

onMounted(() => { loadBuildingPage(); loadWorkerData(); loadWorkers() })
</script>

<style scoped>
.building-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.worker-tags { display: flex; flex-wrap: wrap; }
.worker-phone { font-size: 12px; color: #909399; margin-left: 4px; }
</style>
