<template>
  <div class="service-manage">
    <!-- 分类管理 -->
    <el-card style="margin-bottom: 20px">
      <template #header>
        <div class="card-header">
          <span>服务分类</span>
          <el-button type="primary" size="small" @click="handleAddCategory">新增分类</el-button>
        </div>
      </template>
      <el-table :data="categoryList" v-loading="categoryLoading" size="small">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="icon" label="图标" />
        <el-table-column prop="isMedical" label="医疗类" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isMedical === 1 ? 'danger' : 'info'" size="small">
              {{ row.isMedical === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditCategory(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 服务项目管理 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务项目</span>
          <el-button type="primary" @click="handleAddItem">新增服务</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" style="margin-bottom: 16px">
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="服务名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadItems">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="itemList" v-loading="itemLoading">
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="name" label="服务名称" />
        <el-table-column prop="categoryId" label="分类">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80">
          <template #default="{ row }">{{ row.duration }}分钟</template>
        </el-table-column>
        <el-table-column prop="dailyCapacity" label="每日容量" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditItem(row)">编辑</el-button>
            <el-button type="warning" link @click="handleTimeSlots(row)">时间段</el-button>
            <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadItems"
        style="margin-top: 16px"
      />
    </el-card>

    <!-- 分类编辑弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="categoryForm.icon" placeholder="请输入图标标识" />
        </el-form-item>
        <el-form-item label="医疗类">
          <el-switch v-model="categoryForm.isMedical" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 服务项目编辑弹窗 -->
    <el-dialog v-model="itemDialogVisible" :title="itemForm.id ? '编辑服务' : '新增服务'" width="500px">
      <el-form :model="itemForm" label-width="100px">
        <el-form-item label="服务名称">
          <el-input v-model="itemForm.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="itemForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="itemForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="服务时长">
          <el-input-number v-model="itemForm.duration" :min="0" />
          <span style="margin-left: 8px">分钟</span>
        </el-form-item>
        <el-form-item label="每日容量">
          <el-input-number v-model="itemForm.dailyCapacity" :min="1" />
        </el-form-item>
        <el-form-item label="需要服务码">
          <el-switch v-model="itemForm.needServiceCode" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="服务描述">
          <el-input v-model="itemForm.description" type="textarea" :rows="3" placeholder="请输入服务描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem">确定</el-button>
      </template>
    </el-dialog>

    <!-- 时间段管理弹窗 -->
    <el-dialog v-model="slotDialogVisible" title="时间段管理" width="600px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" size="small" @click="handleAddSlot">新增时间段</el-button>
      </div>
      <el-table :data="slotList" size="small">
        <el-table-column prop="startTime" label="开始时间" />
        <el-table-column prop="endTime" label="结束时间" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditSlot(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteSlot(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const categoryLoading = ref(false)
const itemLoading = ref(false)
const categoryList = ref([])
const itemList = ref([])
const total = ref(0)

const query = reactive({ categoryId: null, name: '', page: 1, size: 10 })

const categoryDialogVisible = ref(false)
const categoryForm = reactive({ id: null, name: '', icon: '', isMedical: 0, sort: 0 })

const itemDialogVisible = ref(false)
const itemForm = reactive({ id: null, name: '', categoryId: null, price: 0, duration: 60, dailyCapacity: 10, needServiceCode: 1, description: '' })

const slotDialogVisible = ref(false)
const slotList = ref([])
const currentItemId = ref(null)

const getCategoryName = (id) => categoryList.value.find(c => c.id === id)?.name || '-'

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    categoryList.value = await request.get('/admin/service/categories') || []
  } catch (e) { console.error(e) }
  categoryLoading.value = false
}

const loadItems = async () => {
  itemLoading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.name) params.name = query.name
    const res = await request.get('/admin/service/items', { params })
    itemList.value = res.records || []
    total.value = res.total || 0
  } catch (e) { console.error(e) }
  itemLoading.value = false
}

const handleAddCategory = () => {
  Object.assign(categoryForm, { id: null, name: '', icon: '', isMedical: 0, sort: 0 })
  categoryDialogVisible.value = true
}

const handleEditCategory = (row) => {
  Object.assign(categoryForm, row)
  categoryDialogVisible.value = true
}

const submitCategory = async () => {
  try {
    if (categoryForm.id) {
      await request.put(`/admin/service/categories/${categoryForm.id}`, categoryForm)
    } else {
      await request.post('/admin/service/categories', categoryForm)
    }
    ElMessage.success('保存成功')
    categoryDialogVisible.value = false
    loadCategories()
  } catch (e) { console.error(e) }
}

const handleDeleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
    await request.delete(`/admin/service/categories/${row.id}`)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleAddItem = () => {
  Object.assign(itemForm, { id: null, name: '', categoryId: null, price: 0, duration: 60, dailyCapacity: 10, needServiceCode: 1, description: '' })
  itemDialogVisible.value = true
}

const handleEditItem = (row) => {
  Object.assign(itemForm, row)
  itemDialogVisible.value = true
}

const submitItem = async () => {
  try {
    if (itemForm.id) {
      await request.put(`/admin/service/items/${itemForm.id}`, itemForm)
    } else {
      await request.post('/admin/service/items', itemForm)
    }
    ElMessage.success('保存成功')
    itemDialogVisible.value = false
    loadItems()
  } catch (e) { console.error(e) }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.post(`/admin/service/items/${row.id}/status`, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    loadItems()
  } catch (e) { console.error(e) }
}

const handleTimeSlots = async (row) => {
  currentItemId.value = row.id
  try {
    slotList.value = await request.get(`/admin/service/items/${row.id}/time-slots`) || []
  } catch (e) { console.error(e) }
  slotDialogVisible.value = true
}

const handleAddSlot = () => {
  ElMessageBox.prompt('请输入时间段（格式：08:00-10:00,容量）', '新增时间段', {
    inputPattern: /^\d{2}:\d{2}-\d{2}:\d{2},\d+$/,
    inputErrorMessage: '格式错误，示例：08:00-10:00,5'
  }).then(async ({ value }) => {
    const [times, capacity] = value.split(',')
    const [startTime, endTime] = times.split('-')
    await request.post(`/admin/service/items/${currentItemId.value}/time-slots`, { startTime, endTime, capacity: parseInt(capacity), status: 1 })
    ElMessage.success('添加成功')
    slotList.value = await request.get(`/admin/service/items/${currentItemId.value}/time-slots`) || []
  }).catch(() => {})
}

const handleEditSlot = (row) => {
  ElMessageBox.prompt('请输入容量', '编辑时间段', { inputValue: String(row.capacity) }).then(async ({ value }) => {
    await request.put(`/admin/service/time-slots/${row.id}`, { ...row, capacity: parseInt(value) })
    ElMessage.success('更新成功')
    slotList.value = await request.get(`/admin/service/items/${currentItemId.value}/time-slots`) || []
  }).catch(() => {})
}

const handleDeleteSlot = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该时间段？', '提示', { type: 'warning' })
    await request.delete(`/admin/service/time-slots/${row.id}`)
    ElMessage.success('删除成功')
    slotList.value = await request.get(`/admin/service/items/${currentItemId.value}/time-slots`) || []
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

onMounted(() => {
  loadCategories()
  loadItems()
})
</script>

<style scoped>
.service-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
