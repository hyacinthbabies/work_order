<template>
  <AppLayout>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
      <div>
        <h2 style="font-size:20px;font-weight:800">工作台概览</h2>
        <p style="font-size:13px;color:#6b7280;margin-top:4px">欢迎回来，{{ user.name }}。今日待处理工单 <strong style="color:#1a56db">12</strong> 件</p>
      </div>
      <div style="display:flex;gap:8px">
        <el-button type="primary" @click="$router.push('/ticket-create')">+ 创建工单</el-button>
      </div>
    </div>

    <el-alert type="warning" title="⚠️ SLA预警：您有 3 笔工单即将超时（剩余时间不足2小时），请优先处理！" show-icon />

    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#dbeafe">📋</div>
        <div class="kpi-label">今日新增工单</div>
        <div class="kpi-value">156</div>
        <div class="kpi-change up">↑ 12.3% 较昨日</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#fef3c7">⏳</div>
        <div class="kpi-label">待处理工单</div>
        <div class="kpi-value" style="color:#d97706">23</div>
        <div class="kpi-change down">↑ 5件 较昨日</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#d1fae5">✅</div>
        <div class="kpi-label">今日已处理</div>
        <div class="kpi-value">89</div>
        <div class="kpi-change up">↑ 8.5% 较昨日</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon" style="background:#ede9fe">⭐</div>
        <div class="kpi-label">客户满意度</div>
        <div class="kpi-value">4.6</div>
        <div class="kpi-change up">↑ 0.2 较上周</div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div>
        <el-card style="margin-bottom:20px">
          <template #header>
            <div class="card-header">
              <h3>我的待办工单</h3>
              <el-button type="text" size="small" @click="$router.push('/ticket-list')">查看全部 →</el-button>
            </div>
          </template>
          <el-table :data="pendingTickets" stripe>
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
                <el-tag :type="getUrgencyType(row.urgency)">{{ row.urgency }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remainingTime" label="剩余时间">
              <template #default="{ row }">
                <span :style="{ color: row.urgency === '紧急' ? '#dc2626' : '#6b7280', fontWeight: row.urgency === '紧急' ? '600' : 'normal' }">{{ row.remainingTime }}</span>
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
            <div v-for="(height, index) in chartData" :key="index" class="bar" :style="{ height: height + '%' }" :class="{ highlight: index === 4 }"></div>
          </div>
          <div style="display:flex;justify-content:space-between;font-size:11px;color:#9ca3af;padding:0 4px">
            <span>5/22</span><span>5/23</span><span>5/24</span><span>5/25</span><span>5/26</span><span>5/27</span><span>5/28</span>
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
              <span>AI自动派单准确率</span><strong style="color:#059669">92.3%</strong>
            </div>
            <el-progress :percentage="92.3" color="#059669" :stroke-width="8" />
          </div>
          <div style="margin-bottom:16px">
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px">
              <span>智能分类准确率</span><strong style="color:#1a56db">94.7%</strong>
            </div>
            <el-progress :percentage="94.7" color="#1a56db" :stroke-width="8" />
          </div>
          <div>
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:6px">
              <span>智能处理占比</span><strong style="color:#7c3aed">63.8%</strong>
            </div>
            <el-progress :percentage="63.8" color="#7c3aed" :stroke-width="8" />
          </div>
        </el-card>

        <el-card>
          <template #header>
            <h3>热点问题 TOP 5</h3>
          </template>
          <div v-for="(item, index) in hotIssues" :key="index" style="display:flex;align-items:center;gap:10px;padding:8px 0;border-bottom:1px solid #f3f4f6">
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
import { computed, ref } from 'vue'
import AppLayout from '@/components/AppLayout.vue'

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : { name: '张三' }
})

const pendingTickets = ref([
  { id: 'WO20250528001', ticketNo: 'WO20250528001', title: '转账失败，资金未到账', type: '投诉', channel: '电话银行', urgency: '紧急', remainingTime: '1h 23min' },
  { id: 'WO20250528015', ticketNo: 'WO20250528015', title: '信用卡账单金额有疑问', type: '咨询', channel: '手机银行', urgency: '一般', remainingTime: '3h 45min' },
  { id: 'WO20250528028', ticketNo: 'WO20250528028', title: '企业开户资料补充提交', type: '办理', channel: '网点', urgency: '较低', remainingTime: '6h 12min' },
  { id: 'WO20250528033', ticketNo: 'WO20250528033', title: '贷款提前还款咨询', type: '咨询', channel: '微信银行', urgency: '一般', remainingTime: '8h 00min' }
])

const chartData = [60, 75, 55, 90, 100, 70, 45]

const hotIssues = ref([
  { title: '转账延迟/失败', count: 32 },
  { title: '信用卡账单疑问', count: 28 },
  { title: '账户余额查询异常', count: 21 },
  { title: '贷款还款咨询', count: 18 },
  { title: '理财产品赎回问题', count: 15 }
])

const getTagType = (type) => {
  const types = { '投诉': 'danger', '咨询': 'warning', '办理': 'primary', '故障': 'info' }
  return types[type] || 'info'
}

const getUrgencyType = (urgency) => {
  const types = { '紧急': 'danger', '一般': 'warning', '较低': 'info' }
  return types[urgency] || 'info'
}

const getRankColor = (index) => {
  const colors = ['#dc2626', '#d97706', '#d97706', '#9ca3af', '#9ca3af']
  return colors[index] || '#9ca3af'
}
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