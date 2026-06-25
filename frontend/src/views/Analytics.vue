<template>
  <AppLayout>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
      <h2 style="font-size:20px;font-weight:800">📊 数据分析大屏</h2>
      <div style="display:flex;gap:8px">
        <el-select v-model="timeRange" placeholder="时间范围" style="width:120px">
          <el-option label="今日" value="today" />
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
        </el-select>
        <el-button type="default" size="small">导出报表</el-button>
      </div>
    </div>

    <div class="stat-row">
      <div class="stat-item">
        <div class="stat-icon" style="background:#dbeafe">📈</div>
        <div><div class="stat-num">1,256</div><div class="stat-label">本月工单总量</div></div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background:#d1fae5">⚡</div>
        <div><div class="stat-num">32min</div><div class="stat-label">平均处理时长</div></div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background:#fef3c7">✅</div>
        <div><div class="stat-num">96.2%</div><div class="stat-label">及时完成率</div></div>
      </div>
      <div class="stat-item">
        <div class="stat-icon" style="background:#ede9fe">⭐</div>
        <div><div class="stat-num">4.6</div><div class="stat-label">满意度评分</div></div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div>
        <el-card style="margin-bottom:20px">
          <template #header>
            <h3>工单量趋势（近30天）</h3>
          </template>
          <div class="chart-bar" style="height:180px">
            <div v-for="(height, index) in chartData" :key="index" class="bar" :style="{ height: height + '%' }" :class="{ highlight: index === 13 }"></div>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <h3>各部门工单处理效率</h3>
          </template>
          <el-table :data="departmentData" stripe>
            <el-table-column prop="department" label="部门" />
            <el-table-column prop="pending" label="待处理">
              <template #default="{ row }">
                <span :style="{ color: row.pending > 5 ? '#d97706' : '#1f2937', fontWeight: '600' }">{{ row.pending }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="processed" label="已处理" />
            <el-table-column prop="avgTime" label="平均时长" />
            <el-table-column prop="timelyRate" label="及时率">
              <template #default="{ row }">
                <span :style="{ color: row.timelyRate >= 95 ? '#059669' : '#d97706' }">{{ row.timelyRate }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="satisfaction" label="满意度" />
          </el-table>
        </el-card>
      </div>

      <div>
        <el-card style="margin-bottom:20px">
          <template #header>
            <h3>工单类型分布</h3>
          </template>
          <div v-for="item in typeDistribution" :key="item.type" style="margin-bottom:12px">
            <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px">
              <span>{{ item.icon }} {{ item.type }}</span>
              <span>{{ item.percentage }}%</span>
            </div>
            <div style="height:10px;background:#e5e7eb;border-radius:5px;overflow:hidden">
              <div :style="{ height: '100%', width: item.percentage + '%', background: item.color, borderRadius: '5px' }"></div>
            </div>
          </div>
        </el-card>

        <el-card style="margin-bottom:20px">
          <template #header>
            <h3>渠道接入统计</h3>
          </template>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;font-size:13px">
            <div v-for="item in channelData" :key="item.name" style="text-align:center;padding:12px;background:#f9fafb;border-radius:8px">
              <div style="font-size:20px;font-weight:800;color:#1a56db">{{ item.count }}</div>
              <div style="font-size:11px;color:#6b7280">{{ item.icon }} {{ item.name }}</div>
            </div>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <h3>⚠️ 实时预警</h3>
          </template>
          <el-alert type="danger" :closable="false" style="margin-bottom:8px;font-size:12px">🔴 WO20250528001 即将超时（剩余1h 23min）</el-alert>
          <el-alert type="warning" :closable="false" style="margin-bottom:8px;font-size:12px">🟡 零售业务部工单积压（待处理8件）</el-alert>
          <el-alert type="info" :closable="false" style="font-size:12px">🔵 今日投诉类工单较昨日增长15%</el-alert>
        </el-card>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue'
import AppLayout from '@/components/AppLayout.vue'

const timeRange = ref('month')

const chartData = [40, 55, 45, 70, 60, 80, 50, 65, 75, 90, 85, 70, 55, 95, 60]

const departmentData = ref([
  { department: '零售业务部', pending: 8, processed: 456, avgTime: '28min', timelyRate: 97.1, satisfaction: 4.7 },
  { department: '信用卡中心', pending: 5, processed: 389, avgTime: '35min', timelyRate: 95.8, satisfaction: 4.5 },
  { department: '对公业务部', pending: 6, processed: 234, avgTime: '42min', timelyRate: 94.2, satisfaction: 4.6 },
  { department: '风控合规部', pending: 3, processed: 128, avgTime: '55min', timelyRate: 91.5, satisfaction: 4.4 },
  { department: '客服中心', pending: 1, processed: 567, avgTime: '18min', timelyRate: 98.3, satisfaction: 4.8 }
])

const typeDistribution = ref([
  { type: '咨询类', percentage: 42, color: '#3b82f6', icon: '🔵' },
  { type: '投诉类', percentage: 28, color: '#ef4444', icon: '🔴' },
  { type: '办理类', percentage: 20, color: '#f59e0b', icon: '🟡' },
  { type: '风控类', percentage: 10, color: '#8b5cf6', icon: '🟣' }
])

const channelData = ref([
  { name: '手机银行', count: 456, icon: '📱' },
  { name: '电话银行', count: 312, icon: '📞' },
  { name: '网上银行', count: 234, icon: '🌐' },
  { name: '微信银行', count: 178, icon: '💬' },
  { name: '网点', count: 56, icon: '🏦' },
  { name: '第三方', count: 20, icon: '📋' }
])
</script>

<style scoped>
.stat-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-item .stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.stat-item .stat-num {
  font-size: 20px;
  font-weight: 800;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #6b7280;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.chart-bar {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  padding: 10px 0;
}

.chart-bar .bar {
  flex: 1;
  background: #e8edfb;
  border-radius: 3px 3px 0 0;
  transition: height .3s;
}

.chart-bar .bar.highlight {
  background: #1a56db;
}

@media (max-width: 1200px) {
  .stat-row {
    flex-wrap: wrap;
  }
  .stat-item {
    flex: 1 1 calc(50% - 8px);
  }
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>