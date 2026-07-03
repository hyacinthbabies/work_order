<template>
  <AppLayout>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
      <h2 style="font-size:20px;font-weight:800">工单列表</h2>
      <el-button type="primary" @click="$router.push('/ticket-create')">+ 创建工单</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="searchText" placeholder="搜索工单编号、标题、客户名..." prefix-icon="Search" style="width:280px" clearable />
      <el-select v-model="statusFilter" placeholder="全部状态" style="width:140px" clearable>
        <el-option label="全部状态" value="" />
        <el-option label="待处理" value="待处理" />
        <el-option label="处理中" value="处理中" />
        <el-option label="待审批" value="待审批" />
        <el-option label="已结案" value="已结案" />
        <el-option label="已撤销" value="已撤销" />
      </el-select>
      <el-select v-model="typeFilter" placeholder="全部类型" style="width:140px" clearable>
        <el-option label="全部类型" value="" />
        <el-option label="咨询" value="咨询" />
        <el-option label="投诉" value="投诉" />
        <el-option label="办理" value="办理" />
        <el-option label="故障" value="故障" />
      </el-select>
      <el-select v-model="channelFilter" placeholder="全部渠道" style="width:140px" clearable>
        <el-option label="全部渠道" value="" />
        <el-option label="手机银行" value="手机银行" />
        <el-option label="电话银行" value="电话银行" />
        <el-option label="网上银行" value="网上银行" />
        <el-option label="微信银行" value="微信银行" />
        <el-option label="网点" value="网点" />
      </el-select>
      <el-select v-model="departmentFilter" placeholder="全部业务线" style="width:140px" clearable>
        <el-option label="全部业务线" value="" />
        <el-option label="零售业务" value="零售业务" />
        <el-option label="对公业务" value="对公业务" />
        <el-option label="信用卡" value="信用卡" />
        <el-option label="风控" value="风控" />
      </el-select>
      <el-button type="default" @click="resetFilters">重置</el-button>
    </div>

    <el-card>
      <el-table :data="ticketList" stripe @selection-change="handleSelectionChange" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="ticketNo" label="工单编号" width="140">
          <template #default="{ row }">
            <span style="color:#1a56db;font-weight:600;cursor:pointer" @click="viewDetail(row.id)">{{ row.ticketNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <span style="cursor:pointer;color:#374151" @click="viewDetail(row.id)">{{ row.title }}</span>
          </template>
        </el-table-column>
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
            <el-tag :type="getUrgencyType(row.urgency)" size="small">{{ getUrgencyText(row.urgency) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="处理部门" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="140" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row.id)">查看</el-button>
            <el-button v-if="row.status !== '已结案' && row.status !== '已完成' && row.status !== '已撤销'" type="default" size="small" @click="handleTicket(row.id)">处理</el-button>
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
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { getTicketList } from '@/api/ticket'

const searchText = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const channelFilter = ref('')
const departmentFilter = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const ticketList = ref([])
const loading = ref(false)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const loadTickets = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (searchText.value) params.keyword = searchText.value
    if (statusFilter.value) params.status = statusFilter.value
    if (typeFilter.value) params.type = typeFilter.value
    if (channelFilter.value) params.channel = channelFilter.value
    if (departmentFilter.value) params.department = departmentFilter.value
    
    const res = await getTicketList(params)
    if (res.data && res.data.content) {
      ticketList.value = res.data.content
      total.value = res.data.totalElements || res.data.total || 0
    } else {
      ticketList.value = res.data || []
      total.value = ticketList.value.length
    }
  } catch (error) {
    console.error('加载工单列表失败', error)
    ticketList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  if (id) {
    window.open(`/ticket-detail/${id}`, '_blank')
  }
}

const handleTicket = (id) => {
  if (id) {
    window.open(`/ticket-detail/${id}`, '_blank')
  }
}

const getTagType = (type) => {
  const types = { '投诉': 'danger', '咨询': 'warning', '办理': 'primary', '故障': 'info', '风控': 'danger' }
  return types[type] || 'info'
}

const getUrgencyText = (urgency) => {
  const texts = { 'high': '紧急', 'medium': '一般', 'low': '较低', '紧急': '紧急', '一般': '一般', '较低': '较低' }
  return texts[urgency] || urgency || '未知'
}

const getUrgencyType = (urgency) => {
  const types = { 'high': 'danger', 'medium': 'warning', 'low': 'info', '紧急': 'danger', '一般': 'warning', '较低': 'info' }
  return types[urgency] || 'info'
}

const getStatusType = (status) => {
  const types = { '待处理': 'primary', '处理中': 'warning', '待审批': 'info', '已结案': 'success', '已完成': 'success', '已撤销': 'danger' }
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
  currentPage.value = 1
  loadTickets()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadTickets()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadTickets()
}

onMounted(() => {
  loadTickets()
})
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
