<template>
  <div class="meal-manage">
    <el-tabs v-model="activeTab">
      <!-- 菜单库 -->
      <el-tab-pane label="菜单库" name="template">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>菜品模板库</span>
              <el-button type="primary" @click="handleAddTemplate">新增菜品</el-button>
            </div>
          </template>
          <el-form :inline="true" :model="templateQuery" style="margin-bottom: 16px">
            <el-form-item label="分类">
              <el-select v-model="templateQuery.category" clearable placeholder="全部" style="width: 120px">
                <el-option label="主食" :value="1" />
                <el-option label="荤菜" :value="2" />
                <el-option label="素菜" :value="3" />
                <el-option label="汤品" :value="4" />
                <el-option label="点心" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item label="适用餐次">
              <el-select v-model="templateQuery.mealType" clearable placeholder="全部" style="width: 100px">
                <el-option label="早餐" :value="1" />
                <el-option label="午餐" :value="2" />
                <el-option label="晚餐" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="templateQuery.keyword" placeholder="菜品名称" style="width: 150px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTemplates">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="templateList" v-loading="templateLoading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="dishName" label="菜品名称" width="150" />
            <el-table-column prop="category" label="分类" width="80">
              <template #default="{ row }">{{ getCategoryName(row.category) }}</template>
            </el-table-column>
            <el-table-column prop="mealTypes" label="适用餐次" width="120">
              <template #default="{ row }">{{ formatMealTypes(row.mealTypes) }}</template>
            </el-table-column>
            <el-table-column prop="price" label="默认价格" width="90" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditTemplate(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="templateQuery.page" :total="templateTotal" layout="total, prev, pager, next" @current-change="loadTemplates" style="margin-top: 16px" />
        </el-card>
      </el-tab-pane>

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

      <!-- 配送员值班管理 -->
      <el-tab-pane label="配送员值班" name="schedule">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>配送员值班安排</span>
              <div>
                <el-date-picker v-model="scheduleWeek" type="week" format="YYYY 第 ww 周" placeholder="选择周" @change="loadWorkerSchedules" style="margin-right: 10px;" />
                <el-button type="primary" @click="handleAddSchedule">新增值班</el-button>
                <el-button type="success" @click="handleBatchAddSchedule">批量排班</el-button>
              </div>
            </div>
          </template>
          <el-table :data="workerScheduleList" v-loading="scheduleLoading">
            <el-table-column prop="scheduleDate" label="值班日期" width="120" />
            <el-table-column prop="mealType" label="餐次" width="100">
              <template #default="{ row }">{{ row.mealType ? getMealTypeName(row.mealType) : '全天' }}</template>
            </el-table-column>
            <el-table-column prop="workerName" label="配送员" width="120" />
            <el-table-column prop="workerPhone" label="联系电话" width="130" />
            <el-table-column prop="buildings" label="负责楼栋" min-width="150">
              <template #default="{ row }">{{ row.buildings || '全部楼栋' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditSchedule(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteSchedule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
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

    <!-- 菜品模板编辑弹窗 -->
    <el-dialog v-model="templateDialogVisible" :title="templateForm.id ? '编辑菜品' : '新增菜品'" width="500px">
      <el-form :model="templateForm" label-width="100px">
        <el-form-item label="菜品名称">
          <el-input v-model="templateForm.dishName" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="templateForm.category" style="width: 100%">
            <el-option label="主食" :value="1" />
            <el-option label="荤菜" :value="2" />
            <el-option label="素菜" :value="3" />
            <el-option label="汤品" :value="4" />
            <el-option label="点心" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用餐次">
          <el-checkbox-group v-model="templateForm.mealTypeList">
            <el-checkbox :label="1">早餐</el-checkbox>
            <el-checkbox :label="2">午餐</el-checkbox>
            <el-checkbox :label="3">晚餐</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="默认价格">
          <el-input-number v-model="templateForm.price" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="templateForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="营养信息">
          <el-input v-model="templateForm.nutritionInfo" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTemplate">确定</el-button>
      </template>
    </el-dialog>

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
    <el-dialog v-model="dishDialogVisible" title="菜品管理" width="1000px" top="5vh">
      <div style="margin-bottom: 16px; display: flex; gap: 10px;">
        <el-button type="primary" @click="handleAddDish">手动添加</el-button>
        <el-button type="success" @click="showSelectTemplate = true">从菜单库选择</el-button>
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

    <!-- 从菜单库选择弹窗 -->
    <el-dialog v-model="showSelectTemplate" title="从菜单库选择菜品" width="800px">
      <el-form :inline="true" style="margin-bottom: 16px">
        <el-form-item label="日期">
          <el-date-picker v-model="selectTemplateForm.dishDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="selectTemplateForm.mealType" style="width: 100px">
            <el-option label="早餐" :value="1" />
            <el-option label="午餐" :value="2" />
            <el-option label="晚餐" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleAddTemplateInSelect">新增菜品到菜单库</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="availableTemplates" max-height="300" @selection-change="handleTemplateSelect">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="category" label="分类" width="80">
          <template #default="{ row }">{{ getCategoryName(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="80" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditTemplateInSelect(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteTemplateInSelect(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showSelectTemplate = false">取消</el-button>
        <el-button type="primary" @click="addFromTemplates" :disabled="selectedTemplates.length === 0">
          添加选中 ({{ selectedTemplates.length }})
        </el-button>
      </template>
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

    <!-- 值班安排编辑弹窗 -->
    <el-dialog v-model="scheduleDialogVisible" :title="scheduleForm.id ? '编辑值班' : '新增值班'" width="500px">
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="值班日期">
          <el-date-picker v-model="scheduleForm.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="scheduleForm.mealType" clearable placeholder="全天" style="width: 100%;">
            <el-option label="全天" :value="null" />
            <el-option label="早餐" :value="1" />
            <el-option label="午餐" :value="2" />
            <el-option label="晚餐" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="配送员">
          <el-select v-model="scheduleForm.workerId" placeholder="请选择配送员" style="width: 100%;">
            <el-option v-for="w in deliveryWorkers" :key="w.id" :label="`${w.name} (${w.phone})`" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责楼栋">
          <el-input v-model="scheduleForm.buildings" placeholder="多个楼栋用逗号分隔，留空表示全部楼栋" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量排班弹窗 -->
    <el-dialog v-model="batchScheduleDialogVisible" title="批量排班" width="500px">
      <el-form :model="batchScheduleForm" label-width="100px">
        <el-form-item label="日期范围">
          <el-date-picker v-model="batchScheduleForm.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="batchScheduleForm.mealType" clearable placeholder="全天" style="width: 100%;">
            <el-option label="全天" :value="null" />
            <el-option label="早餐" :value="1" />
            <el-option label="午餐" :value="2" />
            <el-option label="晚餐" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="配送员">
          <el-select v-model="batchScheduleForm.workerId" placeholder="请选择配送员" style="width: 100%;">
            <el-option v-for="w in deliveryWorkers" :key="w.id" :label="`${w.name} (${w.phone})`" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责楼栋">
          <el-input v-model="batchScheduleForm.buildings" placeholder="多个楼栋用逗号分隔，留空表示全部楼栋" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchScheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('template')

// 菜品模板
const templateLoading = ref(false)
const templateList = ref([])
const templateTotal = ref(0)
const templateQuery = reactive({ page: 1, size: 20, category: null, mealType: null, keyword: '' })
const templateDialogVisible = ref(false)
const templateForm = reactive({ id: null, dishName: '', category: 1, mealTypeList: [2, 3], price: 10, description: '', nutritionInfo: '' })

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
const currentMenu = ref(null)
const dishForm = reactive({ id: null, weeklyMenuId: null, dishDate: '', mealType: 1, dishName: '', price: 0, description: '', nutritionInfo: '' })

// 从模板选择
const showSelectTemplate = ref(false)
const availableTemplates = ref([])
const selectedTemplates = ref([])
const selectTemplateForm = reactive({ dishDate: '', mealType: 2 })

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

// 值班管理
const scheduleLoading = ref(false)
const workerScheduleList = ref([])
const scheduleWeek = ref(new Date())
const deliveryWorkers = ref([])
const scheduleDialogVisible = ref(false)
const scheduleForm = reactive({ id: null, scheduleDate: '', mealType: null, workerId: null, buildings: '', remark: '' })
const batchScheduleDialogVisible = ref(false)
const batchScheduleForm = reactive({ dateRange: [], mealType: null, workerId: null, buildings: '' })

const getMealTypeName = (type) => ({ 1: '早餐', 2: '午餐', 3: '晚餐' }[type] || '-')
const getCategoryName = (cat) => ({ 1: '主食', 2: '荤菜', 3: '素菜', 4: '汤品', 5: '点心' }[cat] || '-')
const getOrderStatusName = (status) => ({ 0: '待配送', 1: '配送中', 2: '已送达', 3: '已取消' }[status] || '-')
const getOrderStatusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')
const formatMealTypes = (types) => {
  if (!types) return '-'
  return types.split(',').map(t => getMealTypeName(parseInt(t))).join('、')
}

// 菜品模板
const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const params = { page: templateQuery.page, size: templateQuery.size }
    if (templateQuery.category) params.category = templateQuery.category
    if (templateQuery.mealType) params.mealType = templateQuery.mealType
    if (templateQuery.keyword) params.keyword = templateQuery.keyword
    const res = await request.get('/admin/meal/dish-templates', { params })
    templateList.value = res.records || []
    templateTotal.value = res.total || 0
  } catch (e) { console.error(e) }
  templateLoading.value = false
}

const handleAddTemplate = () => {
  Object.assign(templateForm, { id: null, dishName: '', category: 1, mealTypeList: [2, 3], price: 10, description: '', nutritionInfo: '' })
  templateDialogVisible.value = true
}

const handleEditTemplate = (row) => {
  Object.assign(templateForm, {
    ...row,
    mealTypeList: row.mealTypes ? row.mealTypes.split(',').map(Number) : []
  })
  templateDialogVisible.value = true
}

const submitTemplate = async () => {
  try {
    const data = { ...templateForm, mealTypes: templateForm.mealTypeList.join(',') }
    if (templateForm.id) {
      await request.put(`/admin/meal/dish-templates/${templateForm.id}`, data)
    } else {
      await request.post('/admin/meal/dish-templates', data)
    }
    ElMessage.success('保存成功')
    templateDialogVisible.value = false
    loadTemplates()
    // 如果选择弹窗打开着，也刷新它的列表
    if (showSelectTemplate.value) {
      availableTemplates.value = await request.get('/admin/meal/dish-templates/all', { params: { mealType: selectTemplateForm.mealType } }) || []
    }
  } catch (e) { console.error(e) }
}

const handleDeleteTemplate = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该菜品模板？', '提示', { type: 'warning' })
    await request.delete(`/admin/meal/dish-templates/${row.id}`)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

// 周食谱
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
    await ElMessageBox.confirm('确定发布该周食谱？发布后用户可以预约', '提示')
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

// 菜品管理
const handleManageDishes = async (row) => {
  currentMenuId.value = row.id
  currentMenu.value = row
  dishLoading.value = true
  dishDialogVisible.value = true
  try {
    dishList.value = await request.get(`/admin/meal/weekly-menus/${row.id}/dishes`) || []
  } catch (e) { console.error(e) }
  dishLoading.value = false
}

const handleAddDish = () => {
  Object.assign(dishForm, { id: null, weeklyMenuId: currentMenuId.value, dishDate: currentMenu.value?.weekStartDate || '', mealType: 1, dishName: '', price: 0, description: '', nutritionInfo: '' })
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

// 从模板选择
watch(showSelectTemplate, async (val) => {
  if (val) {
    selectTemplateForm.dishDate = currentMenu.value?.weekStartDate || ''
    try {
      availableTemplates.value = await request.get('/admin/meal/dish-templates/all', { params: { mealType: selectTemplateForm.mealType } }) || []
    } catch (e) { console.error(e) }
  }
})

watch(() => selectTemplateForm.mealType, async (val) => {
  if (showSelectTemplate.value) {
    try {
      availableTemplates.value = await request.get('/admin/meal/dish-templates/all', { params: { mealType: val } }) || []
    } catch (e) { console.error(e) }
  }
})

const handleTemplateSelect = (rows) => {
  selectedTemplates.value = rows
}

// 在选择弹窗中管理菜单库
const handleAddTemplateInSelect = () => {
  Object.assign(templateForm, { id: null, dishName: '', category: 1, mealTypeList: [selectTemplateForm.mealType], price: 10, description: '', nutritionInfo: '' })
  templateDialogVisible.value = true
}

const handleEditTemplateInSelect = (row) => {
  Object.assign(templateForm, {
    ...row,
    mealTypeList: row.mealTypes ? row.mealTypes.split(',').map(Number) : []
  })
  templateDialogVisible.value = true
}

const handleDeleteTemplateInSelect = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该菜品模板？', '提示', { type: 'warning' })
    await request.delete(`/admin/meal/dish-templates/${row.id}`)
    ElMessage.success('删除成功')
    // 刷新选择列表
    availableTemplates.value = await request.get('/admin/meal/dish-templates/all', { params: { mealType: selectTemplateForm.mealType } }) || []
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const addFromTemplates = async () => {
  if (!selectTemplateForm.dishDate) {
    ElMessage.warning('请选择日期')
    return
  }
  try {
    for (const tpl of selectedTemplates.value) {
      await request.post(`/admin/meal/weekly-menus/${currentMenuId.value}/add-from-template`, {
        templateId: tpl.id,
        dishDate: selectTemplateForm.dishDate,
        mealType: selectTemplateForm.mealType
      })
    }
    ElMessage.success(`成功添加 ${selectedTemplates.value.length} 个菜品`)
    showSelectTemplate.value = false
    selectedTemplates.value = []
    dishList.value = await request.get(`/admin/meal/weekly-menus/${currentMenuId.value}/dishes`) || []
  } catch (e) { console.error(e) }
}

// 配送配置
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

// 订单
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

// 值班管理
const loadDeliveryWorkers = async () => {
  try {
    deliveryWorkers.value = await request.get('/admin/meal/delivery-workers') || []
  } catch (e) { console.error(e) }
}

const loadWorkerSchedules = async () => {
  scheduleLoading.value = true
  try {
    const weekDate = new Date(scheduleWeek.value)
    const day = weekDate.getDay() || 7
    const startDate = new Date(weekDate)
    startDate.setDate(weekDate.getDate() - day + 1)
    const endDate = new Date(startDate)
    endDate.setDate(startDate.getDate() + 6)
    
    const formatDate = (d) => d.toISOString().split('T')[0]
    workerScheduleList.value = await request.get('/admin/meal/worker-schedules', {
      params: { startDate: formatDate(startDate), endDate: formatDate(endDate) }
    }) || []
  } catch (e) { console.error(e) }
  scheduleLoading.value = false
}

const handleAddSchedule = () => {
  Object.assign(scheduleForm, { id: null, scheduleDate: '', mealType: null, workerId: null, buildings: '', remark: '' })
  scheduleDialogVisible.value = true
}

const handleEditSchedule = (row) => {
  Object.assign(scheduleForm, row)
  scheduleDialogVisible.value = true
}

const submitSchedule = async () => {
  if (!scheduleForm.scheduleDate || !scheduleForm.workerId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (scheduleForm.id) {
      await request.put(`/admin/meal/worker-schedules/${scheduleForm.id}`, scheduleForm)
    } else {
      await request.post('/admin/meal/worker-schedules', scheduleForm)
    }
    ElMessage.success('保存成功')
    scheduleDialogVisible.value = false
    loadWorkerSchedules()
  } catch (e) { console.error(e) }
}

const handleDeleteSchedule = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该值班安排？', '提示', { type: 'warning' })
    await request.delete(`/admin/meal/worker-schedules/${row.id}`)
    ElMessage.success('删除成功')
    loadWorkerSchedules()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleBatchAddSchedule = () => {
  Object.assign(batchScheduleForm, { dateRange: [], mealType: null, workerId: null, buildings: '' })
  batchScheduleDialogVisible.value = true
}

const submitBatchSchedule = async () => {
  if (!batchScheduleForm.dateRange || batchScheduleForm.dateRange.length !== 2 || !batchScheduleForm.workerId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await request.post('/admin/meal/worker-schedules/batch', {
      startDate: batchScheduleForm.dateRange[0],
      endDate: batchScheduleForm.dateRange[1],
      mealType: batchScheduleForm.mealType,
      workerId: batchScheduleForm.workerId,
      buildings: batchScheduleForm.buildings
    })
    ElMessage.success('批量排班成功')
    batchScheduleDialogVisible.value = false
    loadWorkerSchedules()
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadTemplates()
  loadMenus()
  loadDeliveryConfigs()
  loadOrders()
  loadOrderStats()
  loadDeliveryWorkers()
  loadWorkerSchedules()
})
</script>

<style scoped>
.meal-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats { display: flex; gap: 10px; }
</style>
