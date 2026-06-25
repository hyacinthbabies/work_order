<template>
  <AppLayout>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
      <h2 style="font-size:20px;font-weight:800">工单列表</h2>
      <el-button type="primary" @click="$router.push('/ticket-create')">+ 创建工单</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchText" placeholder="搜索工单编号、标题、客户名..." prefix-icon="Search" style="width:280px" />
      <el-select v-model="statusFilter" placeholder="全部状态" style="width:140px">
        <el-option label="全部状态" value="" />
        <el-option label="待处理" value="待处理" />
        <el-option label="处理中" value="处理中" />
        <el-option label="待审批" value="待审批" />
        <el-option label="已结案" value="已结案" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="全部类型" style="width:140px">
        <el-option label="全部类型" value="" />
        <el-option label="咨询" value="咨询" />
        <el-option label="投诉" value="投诉" />
        <el-option label="办理" value="办理" />
        <el-option label="故障" value="故障" />
      </el-select>
      <el-select v-model="channelFilter" placeholder="全部渠道" style="width:140px">
        <el-option label="全部渠道" value="" />
        <el-option label="手机银行" value="手机银行" />
        <el-option label="电话银行" value="电话银行" />
        <el-option label="网上银行" value="网上银行" />
        <el-option label="微信银行" value="微信银行" />
        <el-option label="网点" value="网点" />
      </el-select>
      <el-select v-model="departmentFilter" placeholder="全部业务线" style="width:140px">
        <el-option label="全部业务线" value="" />
        <el-option label="零售业务" value="零售业务" />
        <el-option label="对公业务" value="对公业务" />
        <el-option label="信用卡" value="信用卡" />
        <el-option label="风控" value="风控" />
      </el-select>
      <el-button type="default" @click="resetFilters">重置</el-button>
    </div>

    <el-card>
      <el-table :data="ticketList" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="ticketNo" label="工单编号" width="140">
          <template #default="{ row }">
            <span style="color:#1a56db;font-weight:600">{{ row.ticketNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="customerName" label="客户" width="100" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="渠道" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.channel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="urgency" label="紧急度" width="80">
          <template #default="{ row }">
            <el-tag :type="getUrgencyType(row.urgency)" size="small">{{ row.urgency }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="处理部门" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status !== '已结案'" type="primary" size="small" @click="$router.push('/ticket-detail/' + row.id)">处理</el-button>
            <el-button v-else type="text" size="small">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding:12px 0;display:flex;align-items:center;justify-content:space-between;border-top:1px solid #f3f4f6">
        <span style="font-size:12px;color:#6b7280">共 {{ total }} 条记录，第 {{ currentPage }}/{{ totalPages }} 页</span>
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppLayout from '@/components/AppLayout.vue'

const searchText = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const channelFilter = ref('')
const departmentFilter = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(156)

const allTickets = ref([
  { id: 'WO20250528001', ticketNo: 'WO20250528001', title: '转账失败，资金未到账', customerName: '李*明', type: '投诉', channel: '电话银行', urgency: '紧急', department: '零售业务部', status: '处理中', createTime: '2025-05-28 09:15' },
  { id: 'WO20250528015', ticketNo: 'WO20250528015', title: '信用卡账单金额有疑问', customerName: '王*华', type: '咨询', channel: '手机银行', urgency: '一般', department: '信用卡中心', status: '待处理', createTime: '2025-05-28 10:32' },
  { id: 'WO20250528028', ticketNo: 'WO20250528028', title: '企业开户资料补充提交', customerName: '北京***科技有限公司', type: '办理', channel: '网点', urgency: '较低', department: '对公业务部', status: '待处理', createTime: '2025-05-28 11:08' },
  { id: 'WO20250527089', ticketNo: 'WO20250527089', title: '可疑交易报告处理', customerName: '张*军', type: '风控', channel: '电话银行', urgency: '紧急', department: '风控合规部', status: '已结案', createTime: '2025-05-27 14:22' },
  { id: 'WO20250527065', ticketNo: 'WO20250527065', title: '理财产品收益咨询', customerName: '赵*丽', type: '咨询', channel: '微信银行', urgency: '较低', department: '零售业务部', status: '已结案', createTime: '2025-05-27 09:45' }
])

const ticketList = computed(() => {
  return allTickets.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const getTagType = (type) => {
  const types = { '投诉': 'danger', '咨询': 'warning', '办理': 'primary', '故障': 'info', '风控': 'danger' }
  return types[type] || 'info'
}

const getUrgencyType = (urgency) => {
  const types = { '紧急': 'danger', '一般': 'warning', '较低': 'info' }
  return types[urgency] || 'info'
}

const getStatusType = (status) => {
  const types = { '待处理': 'primary', '处理中': 'warning', '待审批': 'info', '已结案': 'success' }
  return types[status] || 'info'
}

const handleSelectionChange = (val) => {
  console.log('selected:', val)
}

const resetFilters = () => {
  searchText.value = ''
  statusFilter.value = ''
  typeFilter.value = ''
  channelFilter.value = ''
  departmentFilter.value = ''
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}
</script>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>