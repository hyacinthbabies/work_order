<template>
  <AppLayout>
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
      <div>
        <div style="display:flex;align-items:center;gap:8px">
          <h2 style="font-size:18px;font-weight:800">{{ ticketInfo.ticketNo }}</h2>
          <el-tag type="danger">紧急</el-tag>
          <el-tag type="warning">处理中</el-tag>
          <el-tag type="primary">电话银行</el-tag>
        </div>
        <p style="font-size:13px;color:#6b7280;margin-top:4px">{{ ticketInfo.title }} · 创建于 {{ ticketInfo.createTime }} · SLA剩余 {{ ticketInfo.slaRemaining }}</p>
      </div>
      <div style="display:flex;gap:8px">
        <el-button type="default" size="small">转交</el-button>
        <el-button type="default" size="small">升级</el-button>
        <el-button type="success" size="small">完结申请</el-button>
      </div>
    </div>

    <el-card style="margin-bottom:16px">
      <div class="workflow-steps">
        <div v-for="(step, index) in workflowSteps" :key="index" class="workflow-step" :class="{ completed: step.completed, active: step.active }">
          <div class="step-circle">{{ step.completed ? '✓' : index + 1 }}</div>
          <div class="step-label">{{ step.label }}</div>
          <div class="step-time">{{ step.time }}</div>
        </div>
      </div>
    </el-card>

    <div class="detail-layout">
      <div class="detail-main">
        <el-card style="margin-bottom:16px">
          <template #header>
            <div class="card-header">
              <h3>👤 客户信息</h3>
              <div style="display:flex;gap:6px">
                <el-tag size="small">当前权限：一线客服</el-tag>
                <el-tag type="warning" size="small">🔒 敏感数据已脱敏</el-tag>
              </div>
            </div>
          </template>
          <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;font-size:13px">
            <div><span style="color:#6b7280">客户编号</span><br><strong>{{ ticketInfo.customerNo }}</strong></div>
            <div><span style="color:#6b7280">客户姓名</span><br><span class="masked">{{ ticketInfo.customerName }}</span></div>
            <div><span style="color:#6b7280">客户类型</span><br>{{ ticketInfo.customerType }}</div>
            <div><span style="color:#6b7280">身份证号</span><br><span class="masked-full">{{ ticketInfo.idCard }}</span></div>
            <div><span style="color:#6b7280">手机号码</span><br><span class="masked">{{ ticketInfo.phone }}</span></div>
            <div><span style="color:#6b7280">联系地址</span><br><span class="masked">{{ ticketInfo.address }}</span></div>
            <div><span style="color:#6b7280">银行卡号</span><br><span class="masked-full">{{ ticketInfo.cardNo }}</span></div>
            <div><span style="color:#6b7280">账户余额</span><br><span class="masked">{{ ticketInfo.balance }}</span></div>
            <div><span style="color:#6b7280">风险等级</span><br><el-tag type="success" size="small">{{ ticketInfo.riskLevel }}</el-tag></div>
          </div>
          <el-alert type="info" :closable="false" style="margin-top:12px;font-size:12px">
            🔒 <strong>数据脱敏说明</strong>：当前角色为"一线客服"，客户姓名、身份证、手机号、银行卡号、余额等敏感信息已自动脱敏。如需查看完整信息，请联系主管审批。
          </el-alert>
        </el-card>

        <el-card style="margin-bottom:16px">
          <template #header>
            <h3>📋 工单内容</h3>
          </template>
          <div style="font-size:13px;line-height:1.8">
            <p><strong>问题描述：</strong></p>
            <p style="color:#4b5563;margin-bottom:12px">{{ ticketInfo.description }}</p>
            <p><strong>交易信息：</strong></p>
            <div style="display:grid;grid-template-columns:repeat(2,1fr);gap:8px;margin-top:6px;font-size:12px">
              <div>交易流水号：<span class="masked-full">{{ ticketInfo.transactionNo }}</span></div>
              <div>交易金额：<span class="masked">{{ ticketInfo.amount }}</span></div>
              <div>收款账号：<span class="masked-full">{{ ticketInfo.receiverAccount }}</span></div>
              <div>交易时间：{{ ticketInfo.transactionTime }}</div>
            </div>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <h3>📝 处理记录</h3>
          </template>
          <div class="timeline">
            <div v-for="(item, index) in timelineItems" :key="index" class="timeline-item">
              <div class="tl-dot" :class="{ blue: item.type === 'system', green: item.type === 'user' }"></div>
              <div class="tl-time">{{ item.time }}</div>
              <div class="tl-user">{{ item.user }}</div>
              <div class="tl-content">{{ item.content }}</div>
            </div>
          </div>

          <el-form-item label="添加处理备注" style="margin-top:16px">
            <el-input v-model="remark" type="textarea" :rows="3" placeholder="请输入处理备注..." />
          </el-form-item>
          <div style="display:flex;gap:8px">
            <el-button type="primary" size="small">提交备注</el-button>
            <el-button type="default" size="small">快捷话术</el-button>
          </div>
        </el-card>
      </div>

      <div class="assistant-panel">
        <div class="panel-header">
          <div class="dot"></div>
          <h4>Trae AI 智能助手</h4>
        </div>

        <div class="assistant-section">
          <h5>🔍 问题分析</h5>
          <div class="assistant-card">
            <div class="ac-title">转账超时但已扣款</div>
            <div class="ac-text">核心系统交易状态：处理中<br>资金位置：清算过渡户<br>原因分析：跨行转账通道超时</div>
            <div><span class="ac-tag" style="background:#dbeafe;color:#1d4ed8">零售业务</span><span class="ac-tag" style="background:#fee2e2;color:#dc2626">资金安全</span></div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>💡 推荐处理方案</h5>
          <div class="assistant-card" style="border:1px solid #1a56db;background:#eff6ff">
            <div class="ac-title" style="color:#1a56db">方案一：发起调账（推荐）</div>
            <div class="ac-text">提交内部调账申请，将过渡户资金退回客户账户。预计2小时完成。</div>
            <div style="margin-top:8px"><el-button type="primary" size="small">一键发起调账</el-button></div>
          </div>
          <div class="assistant-card">
            <div class="ac-title">方案二：等待自动冲正</div>
            <div class="ac-text">如交易最终失败，系统将自动冲正退回。但等待时间不确定（最长24小时）。</div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>📋 相似历史工单</h5>
          <div class="assistant-card">
            <div class="ac-title">WO20250525012</div>
            <div class="ac-text">同类转账超时问题，已通过调账解决。处理时长：1.5小时</div>
          </div>
          <div class="assistant-card">
            <div class="ac-title">WO20250520078</div>
            <div class="ac-text">跨行转账超时，等待24小时后自动冲正。客户满意度较低。</div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>📚 知识库推荐</h5>
          <div class="assistant-card">
            <div class="ac-title">《跨行转账超时处理操作指引》</div>
            <div class="ac-text">详细说明转账超时的排查步骤、调账流程和客户话术。</div>
          </div>
          <div class="assistant-card">
            <div class="ac-title">《资金调账申请操作手册》</div>
            <div class="ac-text">调账申请的填写规范、审批流程和时效要求。</div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>⚠️ 合规风险提示</h5>
          <div class="risk-card">
            <div class="risk-title">资金安全敏感工单</div>
            <div class="risk-text">该工单涉及客户资金安全，请确保：1. 及时处理不超时 2. 客户沟通使用标准话术 3. 不得向客户承诺具体到账时间</div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>💬 推荐话术</h5>
          <div class="assistant-card" style="cursor:pointer" title="点击复制" @click="copyText">
            <div class="ac-text" style="color:#374151">"李先生您好，您反映的转账问题我们已经受理。经查询，您的资金目前在银行内部处理中，是安全的。我们已为您加急处理，预计2小时内资金将退回您的账户，届时我们会第一时间通知您。非常抱歉给您带来不便。"</div>
            <div style="font-size:11px;color:#1a56db;margin-top:6px">📋 点击复制话术</div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import AppLayout from '@/components/AppLayout.vue'

const ticketInfo = reactive({
  ticketNo: 'WO20250528001',
  title: '转账失败，资金未到账',
  createTime: '2025-05-28 09:15',
  slaRemaining: '1h 23min',
  customerNo: 'C20230045678',
  customerName: '李*明',
  customerType: '个人客户（金卡）',
  idCard: '110101**********1234',
  phone: '138****5678',
  address: '北京市朝阳区***路***号',
  cardNo: '6222 0123 **** **** 0123',
  balance: '¥ ****.**',
  riskLevel: '低风险',
  description: '客户李先生反映5月28日上午9:10通过手机银行向尾号5678的账户转账5000元，操作显示交易超时，但查询银行卡余额发现5000元已被扣减，收款方表示未收到款项。客户情绪较为激动，要求尽快查明原因并退回资金。',
  transactionNo: 'TXN20250528****5678',
  amount: '¥ ****.**',
  receiverAccount: '6222 **** **** 5678',
  transactionTime: '2025-05-28 09:10:23'
})

const workflowSteps = ref([
  { label: '工单创建', time: '09:15', completed: true, active: false },
  { label: '智能分类', time: '09:15', completed: true, active: false },
  { label: '自动派单', time: '09:15', completed: true, active: false },
  { label: '处理中', time: '进行中', completed: false, active: true },
  { label: '待审批', time: '-', completed: false, active: false },
  { label: '待回复', time: '-', completed: false, active: false },
  { label: '已结案', time: '-', completed: false, active: false }
])

const timelineItems = ref([
  { time: '2025-05-28 09:15', user: '系统（AI自动）', content: '🤖 <strong>智能分类</strong>：零售业务 → 转账汇款 → 交易异常 | 置信度 96.8%', type: 'system' },
  { time: '2025-05-28 09:15', user: '系统（AI自动）', content: '🤖 <strong>智能派单</strong>：自动派发至零售业务部 → 张三（技能匹配度 95%，当前负载低）', type: 'system' },
  { time: '2025-05-28 09:20', user: '张三（一线客服）', content: '已接单，正在查询核心系统交易状态。已联系客户安抚情绪。', type: 'user' },
  { time: '2025-05-28 09:35', user: '张三（一线客服）', content: '经查询核心系统，交易状态为"处理中"，资金暂挂在清算过渡户。已提交内部调账申请，预计2小时内完成。', type: 'user' }
])

const remark = ref('')

const copyText = () => {
  const text = '李先生您好，您反映的转账问题我们已经受理。经查询，您的资金目前在银行内部处理中，是安全的。我们已为您加急处理，预计2小时内资金将退回您的账户，届时我们会第一时间通知您。非常抱歉给您带来不便。'
  navigator.clipboard.writeText(text)
}
</script>

<style scoped>
.detail-layout {
  display: flex;
  gap: 20px;
}

.detail-main {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.workflow-steps {
  display: flex;
  align-items: center;
  gap: 0;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}

.workflow-step .step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  border: 2px solid #d1d5db;
  color: #9ca3af;
  background: #fff;
  z-index: 1;
}

.workflow-step.completed .step-circle {
  background: #059669;
  border-color: #059669;
  color: #fff;
}

.workflow-step.active .step-circle {
  background: #1a56db;
  border-color: #1a56db;
  color: #fff;
  animation: pulse 2s infinite;
}

.workflow-step .step-label {
  font-size: 11px;
  color: #6b7280;
  margin-top: 6px;
  font-weight: 600;
}

.workflow-step.completed .step-label {
  color: #059669;
}

.workflow-step.active .step-label {
  color: #1a56db;
}

.workflow-step .step-time {
  font-size: 10px;
  color: #9ca3af;
  margin-top: 2px;
}

.workflow-step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 18px;
  left: calc(50% + 20px);
  width: calc(100% - 40px);
  height: 2px;
  background: #e5e7eb;
}

.workflow-step.completed:not(:last-child)::after {
  background: #059669;
}

.masked {
  color: #9ca3af;
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
  letter-spacing: 1px;
}

.masked-full {
  color: #9ca3af;
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
  letter-spacing: 1px;
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e5e7eb;
}

.timeline-item {
  position: relative;
  margin-bottom: 16px;
}

.timeline-item .tl-dot {
  position: absolute;
  left: -20px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  background: #fff;
}

.timeline-item .tl-dot.blue {
  border-color: #1a56db;
  background: #1a56db;
}

.timeline-item .tl-dot.green {
  border-color: #059669;
  background: #059669;
}

.timeline-item .tl-time {
  font-size: 11px;
  color: #9ca3af;
}

.timeline-item .tl-content {
  font-size: 13px;
  color: #4b5563;
  margin-top: 2px;
}

.timeline-item .tl-user {
  font-size: 12px;
  color: #1a56db;
  font-weight: 600;
}

.assistant-panel {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  height: calc(100vh - 130px);
  position: sticky;
  top: 80px;
  overflow-y: auto;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-header .dot {
  width: 8px;
  height: 8px;
  background: #059669;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.panel-header h4 {
  font-size: 14px;
  font-weight: 700;
}

.assistant-section {
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.assistant-section h5 {
  font-size: 12px;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: .5px;
  margin-bottom: 10px;
}

.assistant-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
}

.assistant-card .ac-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.assistant-card .ac-text {
  font-size: 12px;
  color: #4b5563;
  line-height: 1.6;
}

.assistant-card .ac-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-top: 6px;
  margin-right: 4px;
}

.risk-card {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 12px;
}

.risk-card .risk-title {
  font-size: 13px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 4px;
}

.risk-card .risk-text {
  font-size: 12px;
  color: #991b1b;
  line-height: 1.6;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .5; }
}

@media (max-width: 1200px) {
  .detail-layout {
    flex-direction: column;
  }
  .assistant-panel {
    width: 100%;
    height: auto;
    position: static;
    border-left: none;
    border-top: 1px solid #e5e7eb;
  }
}
</style>