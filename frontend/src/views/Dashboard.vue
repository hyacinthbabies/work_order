<template>
  <AppLayout>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
      <div>
        <h2 style="font-size:20px;font-weight:800">工作台概览</h2>
        <p style="font-size:13px;color:#6b7280;margin-top:4px">欢迎回来，{{ user.name }}。今日待处理工单 <strong style="color:#1a56db">{{ dashboardData.pendingTickets || 0 }}</strong> 件</p>
      </div>
      <div style="display:flex;gap:8px">
        <el-button type="primary" @click="$router.push('/ticket-create')">+ 创建工单</el-button>
      </div>
    </div>

    <el-alert v-if="dashboardData.slaAlerts && dashboardData.slaAlerts.length > 0" type="warning" :title="slaAlertTitle" show-icon />

    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#dbeafe">📋</div>
        <div class="kpi-label">今日新增工单</div>
        <div class="kpi-value">{{ dashboardData.todayNewTickets || 0 }}</div>
        <!-- <div class="kpi-change up">↑ 12.3% 较昨日</div> -->
      </div>
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#fef3c7">⏳</div>
        <div class="kpi-label">待处理工单</div>
        <div class="kpi-value" style="color:#d97706">{{ dashboardData.pendingTickets || 0 }}</div>
        <!-- <div class="kpi-change down">↑ 5件 较昨日</div> -->
      </div>
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#d1fae5">✅</div>
        <div class="kpi-label">今日已处理</div>
        <div class="kpi-value">{{ dashboardData.todayProcessedTickets || 0 }}</div>
        <!-- <div class="kpi-change up">↑ 8.5% 较昨日</div> -->
      </div>
      <!-- <div class="kpi-card">
        <div class="kpi-icon" style="background:#ede9fe">⭐</div>
        <div class="kpi-label">客户满意度</div>
        <div class="kpi-value">4.6</div>
        <div class="kpi-change up">↑ 0.2 较上周</div>
      </div> -->
    </div>

    <div class="dashboard-grid">
      <div>
        <el-card style="margin-bottom:20px">
          <template #header>
            <div class="card-header">
              <h3>我的待办工单</h3>
              <el-button v-if="hasMoreTasks" type="text" size="small" @click="$router.push('/ticket-list')">查看全部 →</el-button>
            </div>
          </template>
          <el-table :data="dashboardData.myTasks || []" stripe>
            <el-table-column prop="ticketNo" label="工单编号" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="type" label="类型">
              <template #default="{ row }">
                <el-tag :type="getTagType(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="channel" label="渠道">
              <template #default="{ row }">
                <el-tag size="small">{{ row.channel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="urgency" label="紧急度">
              <template #default="{ row }">
                <el-tag :type="getUrgencyType(row.urgency)">{{ getUrgencyText(row.urgency) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="slaRemaining" label="剩余时间">
              <template #default="{ row }">
                <span :style="{ color: row.slaRemaining && row.slaRemaining.includes('已超时') ? '#dc2626' : '#6b7280', fontWeight: row.slaRemaining && row.slaRemaining.includes('已超时') ? '600' : 'normal' }">{{ row.slaRemaining || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="$router.push('/ticket-detail/' + row.id)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card>
          <template #header>
            <h3>近7日工单趋势</h3>
          </template>
          <div class="mini-chart">
            <div v-for="(item, index) in dailyChartData" :key="index" class="bar" :style="{ height: item.height + '%' }" :class="{ highlight: index === dailyChartData.length - 1 }"></div>
          </div>
          <div style="display:flex;justify-content:space-between;font-size:11px;color:#9ca3af;padding:0 4px">
            <span v-for="(item, index) in dashboardData.dailyStats || []" :key="index">{{ item.date }}</span>
          </div>
        </el-card>
      </div>

      <div>
        <el-card style="margin-bottom:20px">
          <template #header>
            <h3>智能派单统计</h3>
          </template>
          <div style="margin-bottom:16px">
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px">
              <span>AI自动派单准确率</span><strong style="color:#059669">{{ dashboardData.aiAssignAccuracy || 0 }}%</strong>
            </div>
            <el-progress :percentage="dashboardData.aiAssignAccuracy || 0" color="#059669" :stroke-width="8" />
          </div>
          <div style="margin-bottom:16px">
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px">
              <span>智能分类准确率</span><strong style="color:#1a56db">{{ dashboardData.aiClassifyAccuracy || 0 }}%</strong>
            </div>
            <el-progress :percentage="dashboardData.aiClassifyAccuracy || 0" color="#1a56db" :stroke-width="8" />
          </div>
          <div>
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px">
              <span>智能处理占比</span><strong style="color:#7c3aed">{{ dashboardData.aiHandleRate || 0 }}%</strong>
            </div>
            <el-progress :percentage="dashboardData.aiHandleRate || 0" color="#7c3aed" :stroke-width="8" />
          </div>
        </el-card>

        <el-card>
          <template #header>
            <h3>热点问题 TOP 5</h3>
          </template>
          <div v-for="(item, index) in dashboardData.hotIssues || []" :key="index" style="display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid #f3f4f6">
            <span :style="{ background: getRankColor(index), color: '#fff', width: '20px', height: '20px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: '700' }">{{ index + 1 }}</span>
            <span style="font-size:13px;flex:1">{{ item.title }}</span>
            <span style="font-size:12px;color:#9ca3af">{{ item.count }}件</span>
          </div>
        </el-card>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import AppLayout from '@/components/AppLayout.vue'
import { getDashboard } from '@/api/ticket.js'

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : { name: '张三' }
})

const dashboardData = ref({})

const dailyChartData = computed(() => {
  const stats = dashboardData.value.dailyStats || []
  if (stats.length === 0) {
    return [{ height: 60 }, { height: 75 }, { height: 55 }, { height: 90 }, { height: 100 }, { height: 70 }, { height: 45 }]
  }
  const maxCount = Math.max(...stats.map(s => s.count), 1)
  return stats.map(s => ({ height: (s.count / maxCount) * 100 }))
})

const slaAlertTitle = computed(() => {
  const alertCount = dashboardData.value.slaAlerts ? dashboardData.value.slaAlerts.length : 0
  return `⚠️ SLA预警：您有 ${alertCount} 笔工单即将超时（剩余时间不足2小时），请优先处理！`
})

const hasMoreTasks = computed(() => {
  const count = dashboardData.value.myTasksCount || 0
  const listLength = dashboardData.value.myTasks ? dashboardData.value.myTasks.length : 0
  return count > listLength
})

const getTagType = (type) => {
  const types = { '投诉类': 'danger', '咨询类': 'warning', '办理类': 'primary', '风控类': 'info', '投诉': 'danger', '咨询': 'warning', '办理': 'primary', '故障': 'info' }
  return types[type] || 'info'
}

const getUrgencyType = (urgency) => {
  const types = { 'high': 'danger', '紧急': 'danger', 'medium': 'warning', '一般': 'warning', 'low': 'info', '较低': 'info' }
  return types[urgency] || 'info'
}

const getUrgencyText = (urgency) => {
  const texts = { 'high': '紧急', 'medium': '一般', 'low': '较低' }
  return texts[urgency] || urgency
}

const getRankColor = (index) => {
  const colors = ['#dc2626', '#d97706', '#d97706', '#9ca3af', '#9ca3af']
  return colors[index] || '#9ca3af'
}

const fetchDashboardData = async () => {
  try {
    const response = await getDashboard()
    if (response.data) {
      dashboardData.value = response.data
    }
  } catch (error) {
    console.error('获取工作台数据失败:', error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 20px;
}

.kpi-card .kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  float: right;
  font-size: 20px;
}

.kpi-card .kpi-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.kpi-card .kpi-value {
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
}

.kpi-card .kpi-change {
  font-size: 12px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.kpi-card .kpi-change.up {
  color: #059669;
}

.kpi-card .kpi-change.down {
  color: #dc2626;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  width: 100%;
  overflow-x: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mini-chart {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 120px;
  padding: 10px 0;
}

.mini-chart .bar {
  flex: 1;
  background: #e8edfb;
  border-radius: 3px 3px 0 0;
  transition: height .3s;
}

.mini-chart .bar.highlight {
  background: #1a56db;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
