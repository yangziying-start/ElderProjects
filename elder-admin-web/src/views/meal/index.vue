<template>
  <div class="meal-manage">
    <el-tabs v-model="activeTab">
      <!-- 周食谱管理 -->
      <el-tab-pane label="周食谱管理" name="menu">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>周食谱列表</span>
              <el-button type="primary" @click="handleAddMenu">新增周食谱</el-button>
            </div>
          </template>
          <el-table :data="menuList" v-loading="menuLoading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="weekStartDate" label="周起始日期" width="120" />
            <el-table-column prop="weekEndDate" label="周结束日期" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="160" />
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleManageDishes(row)">管理菜品</el-button>
                <el-button type="success" link @click="handlePublishMenu(row)" v-if="row.status === 0">发布</el-button>
                <el-button type="warning" link @click="handleEditMenu(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteMenu(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 配送时间配置 -->
      <el-tab-pane label="配送时间配置" name="config">
        <el-card>
          <template #header><span>配送时间配置</span></template>
          <el-table :data="deliveryConfigs" v-loading="configLoading">
            <el-table-column prop="mealType" label="餐次" width="100">
              <template #default="{ row }">{{ getMealTypeName(row.mealType) }}</template>
            </el-table-column>
            <el-table-column prop="deliveryStartTime" label="送餐开始时间" width="140" />
            <el-table-column prop="deliveryEndTime" label="送餐结束时间" width="140" />
            <el-table-column prop="bookingDeadlineMinutes" label="预约截止(分钟)" width="140" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditConfig(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 16px; color: #909399; font-size: 13px;">
            <p>预约窗口规则：上一餐送餐结束1.5小时后 至 本餐送餐前1小时</p>
            <p>取消规则：送餐前>1小时免费取消；1小时~10分钟扣5%；<10分钟扣10%</p>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 餐饮订单 -->
      <el-tab-pane label="餐饮订单" name="order">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>餐饮订单</span>
              <div class="stats">
                <el-tag>今日订单: {{ orderStats.todayCount || 0 }}</el-tag>
                <el-tag type="warning">待配送: {{ orderStats.pendingCount || 0 }}</el-tag>
                <el-tag type="success">配送中: {{ orderStats.deliveringCount || 0 }}</el-tag>
              </div>
            </div>
          </template>
          <el-form :inline="true" :model="orderQuery" style="margin-bottom: 16px">
            <el-form-item label="日期">
              <el-date-picker v-model="orderQuery.dishDate" type="date" value-format="YYYY-MM-DD" clearable />
            </el-form-item>
            <el-form-item label="餐次">
              <el-select v-model="orderQuery.mealType" clearable style="width: 100px">
                <el-option label="早餐" :value="1" />
                <el-option label="午餐" :value="2" />
                <el-option label="晚餐" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="orderQuery.status" clearable style="width: 100px">
                <el-option label="待配送" :value="0" />
                <el-option label="配送中" :value="1" />
                <el-option label="已送达" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadOrders">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="orderList" v-loading="orderLoading">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="dishDate" label="用餐日期" width="110" />
            <el-table-column prop="mealType" label="餐次" width="70">
              <template #default="{ row }">{{ getMealTypeName(row.mealType) }}</template>
            </el-table-column>
            <el-table-column label="菜品" width="120">
              <template #default="{ row }">{{ row.dish?.dishName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="金额(积分)" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)" size="small">{{ getOrderStatusName(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="160" />
          </el-table>
          <el-pagination v-model:current-page="orderQuery.page" :total="orderTotal" layout="total, prev, pager, next" @current-change="loadOrders" style="margin-top: 16px" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 周食谱编辑弹窗 -->
    <el-dialog v-model="menuDialogVisible" :title="menuForm.id ? '编辑周食谱' : '新增周食谱'" width="400px">
      <el-form :model="menuForm" label-width="100px">
        <el-form-item label="周起始日期">
          <el-date-picker v-model="menuForm.weekStartDate" type="date" value-format="YYYY-MM-DD" :disabled="!!menuForm.id" />
        </el-form-item>
        <el-form-item label="周结束日期">
          <el-date-picker v-model="menuForm.weekEndDate" type="date" value-format="YYYY-MM-DD" :disabled="!!menuForm.id" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMenu">确定</el-button>
      </template>
    </el-dialog>

    <!-- 菜品管理弹窗 -->
    <el-dialog v-model="dishDialogVisible" title="菜品管理" width="900px" top="5vh">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAddDish">新增菜品</el-button>
      </div>
      <el-table :data="dishList" v-loading="dishLoading" max-height="400">
        <el-table-column prop="dishDate" label="日期" width="110" />
        <el-table-column prop="mealType" label="餐次" width="70">
          <template #default="{ row }">{{ getMealTypeName(row.mealType) }}</template>
        </el-table-column>
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="price" label="价格(积分)" width="100" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditDish(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteDish(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 菜品编辑弹窗 -->
    <el-dialog v-model="dishEditDialogVisible" :title="dishForm.id ? '编辑菜品' : '新增菜品'" width="500px">
      <el-form :model="dishForm" label-width="100px">
        <el-form-item label="日期">
          <el-date-picker v-model="dishForm.dishDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="dishForm.mealType" style="width: 100%">
            <el-option label="早餐" :value="1" />
            <el-option label="午餐" :value="2" />
            <el-option label="晚餐" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品名称">
          <el-input v-model="dishForm.dishName" />
        </el-form-item>
        <el-form-item label="价格(积分)">
          <el-input-number v-model="dishForm.price" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dishForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="营养信息">
          <el-input v-model="dishForm.nutritionInfo" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dishEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDish">确定</el-button>
      </template>
    </el-dialog>

    <!-- 配送配置编辑弹窗 -->
    <el-dialog v-model="configDialogVisible" title="编辑配送配置" width="400px">
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="餐次">
          <span>{{ getMealTypeName(configForm.mealType) }}</span>
        </el-form-item>
        <el-form-item label="送餐开始时间">
          <el-time-picker v-model="configForm.deliveryStartTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="送餐结束时间">
          <el-time-picker v-model="configForm.deliveryEndTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="预约截止(分钟)">
          <el-input-number v-model="configForm.bookingDeadlineMinutes" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfig">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('menu')

// 周食谱
const menuLoading = ref(false)
const menuList = ref([])
const menuDialogVisible = ref(false)
const menuForm = reactive({ id: null, weekStartDate: '', weekEndDate: '' })

// 菜品
const dishDialogVisible = ref(false)
const dishEditDialogVisible = ref(false)
const dishLoading = ref(false)
const dishList = ref([])
const currentMenuId = ref(null)
const dishForm = reactive({ id: null, weeklyMenuId: null, dishDate: '', mealType: 1, dishName: '', price: 0, description: '', nutritionInfo: '' })

// 配送配置
const configLoading = ref(false)
const deliveryConfigs = ref([])
const configDialogVisible = ref(false)
const configForm = reactive({ id: null, mealType: 1, deliveryStartTime: '', deliveryEndTime: '', bookingDeadlineMinutes: 60 })

// 订单
const orderLoading = ref(false)
const orderList = ref([])
const orderTotal = ref(0)
const orderStats = ref({})
const orderQuery = reactive({ page: 1, size: 10, dishDate: null, mealType: null, status: null })

const getMealTypeName = (type) => ({ 1: '早餐', 2: '午餐', 3: '晚餐' }[type] || '-')
const getOrderStatusName = (status) => ({ 0: '待配送', 1: '配送中', 2: '已送达', 3: '已取消' }[status] || '-')
const getOrderStatusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')

const loadMenus = async () => {
  menuLoading.value = true
  try {
    const res = await request.get('/admin/meal/weekly-menus')
    menuList.value = res.records || []
  } catch (e) { console.error(e) }
  menuLoading.value = false
}

const handleAddMenu = () => {
  Object.assign(menuForm, { id: null, weekStartDate: '', weekEndDate: '' })
  menuDialogVisible.value = true
}

const handleEditMenu = (row) => {
  Object.assign(menuForm, row)
  menuDialogVisible.value = true
}

const submitMenu = async () => {
  try {
    if (menuForm.id) {
      await request.put(`/admin/meal/weekly-menus/${menuForm.id}`, menuForm)
    } else {
      await request.post('/admin/meal/weekly-menus', menuForm)
    }
    ElMessage.success('保存成功')
    menuDialogVisible.value = false
    loadMenus()
  } catch (e) { console.error(e) }
}

const handlePublishMenu = async (row) => {
  try {
    await ElMessageBox.confirm('确定发布该周食谱？', '提示')
    await request.post(`/admin/meal/weekly-menus/${row.id}/publish`)
    ElMessage.success('发布成功')
    loadMenus()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleDeleteMenu = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该周食谱？', '提示', { type: 'warning' })
    await request.delete(`/admin/meal/weekly-menus/${row.id}`)
    ElMessage.success('删除成功')
    loadMenus()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleManageDishes = async (row) => {
  currentMenuId.value = row.id
  dishLoading.value = true
  dishDialogVisible.value = true
  try {
    dishList.value = await request.get(`/admin/meal/weekly-menus/${row.id}/dishes`) || []
  } catch (e) { console.error(e) }
  dishLoading.value = false
}

const handleAddDish = () => {
  Object.assign(dishForm, { id: null, weeklyMenuId: currentMenuId.value, dishDate: '', mealType: 1, dishName: '', price: 0, description: '', nutritionInfo: '' })
  dishEditDialogVisible.value = true
}

const handleEditDish = (row) => {
  Object.assign(dishForm, row)
  dishEditDialogVisible.value = true
}

const submitDish = async () => {
  try {
    dishForm.weeklyMenuId = currentMenuId.value
    if (dishForm.id) {
      await request.put(`/admin/meal/dishes/${dishForm.id}`, dishForm)
    } else {
      await request.post('/admin/meal/dishes', dishForm)
    }
    ElMessage.success('保存成功')
    dishEditDialogVisible.value = false
    dishList.value = await request.get(`/admin/meal/weekly-menus/${currentMenuId.value}/dishes`) || []
  } catch (e) { console.error(e) }
}

const handleDeleteDish = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该菜品？', '提示', { type: 'warning' })
    await request.delete(`/admin/meal/dishes/${row.id}`)
    ElMessage.success('删除成功')
    dishList.value = await request.get(`/admin/meal/weekly-menus/${currentMenuId.value}/dishes`) || []
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const loadDeliveryConfigs = async () => {
  configLoading.value = true
  try {
    deliveryConfigs.value = await request.get('/admin/meal/delivery-configs') || []
  } catch (e) { console.error(e) }
  configLoading.value = false
}

const handleEditConfig = (row) => {
  Object.assign(configForm, row)
  configDialogVisible.value = true
}

const submitConfig = async () => {
  try {
    await request.put(`/admin/meal/delivery-configs/${configForm.id}`, configForm)
    ElMessage.success('保存成功')
    configDialogVisible.value = false
    loadDeliveryConfigs()
  } catch (e) { console.error(e) }
}

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const params = { page: orderQuery.page, size: orderQuery.size }
    if (orderQuery.dishDate) params.dishDate = orderQuery.dishDate
    if (orderQuery.mealType != null) params.mealType = orderQuery.mealType
    if (orderQuery.status != null) params.status = orderQuery.status
    const res = await request.get('/admin/meal/orders', { params })
    orderList.value = res.records || []
    orderTotal.value = res.total || 0
  } catch (e) { console.error(e) }
  orderLoading.value = false
}

const loadOrderStats = async () => {
  try {
    orderStats.value = await request.get('/admin/meal/orders/stats') || {}
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadMenus()
  loadDeliveryConfigs()
  loadOrders()
  loadOrderStats()
})
</script>

<style scoped>
.meal-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats { display: flex; gap: 10px; }
</style>
