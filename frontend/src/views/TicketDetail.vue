<template>
  <AppLayout>
    <div v-if="loading" style="display:flex;justify-content:center;align-items:center;height:300px">
      <el-loading-spinner />
    </div>
    <div v-else>
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
        <div>
          <div style="display:flex;align-items:center;gap:8px">
            <h2 style="font-size:18px;font-weight:800">{{ ticketInfo.ticketNo }}</h2>
            <el-tag v-for="tag in getStatusTags" :key="tag.text" :type="tag.type">{{ tag.text }}</el-tag>
          </div>
          <p style="font-size:13px;color:#6b7280;margin-top:4px">{{ ticketInfo.title }} · 创建于 {{ ticketInfo.createTime }} · SLA剩余 {{ ticketInfo.slaRemaining }}</p>
        </div>
        <div style="display:flex;gap:8px">
          <el-button type="default" size="small">转交</el-button>
          <el-button type="default" size="small">升级</el-button>
          <el-button type="success" size="small">完结申请</el-button>
          <el-button v-if="canRevoke" type="danger" size="small" @click="showRevokeDialog = true">撤销工单</el-button>
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
            <div class="rich-text-content" v-html="ticketInfo.description"></div>
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
          <div v-if="aiAnalysis" class="assistant-card">
            <div class="ac-title">{{ aiAnalysis.analysisTitle }}</div>
            <div class="ac-text">{{ aiAnalysis.problemAnalysis }}</div>
            <div><span class="ac-tag" style="background:#dbeafe;color:#1d4ed8">{{ aiAnalysis.department }}</span><span class="ac-tag" style="background:#fee2e2;color:#dc2626">{{ aiAnalysis.urgency }}</span></div>
          </div>
          <div v-else class="assistant-card">
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
          <div v-if="loadingArticles" class="assistant-card">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
          <div v-else-if="knowledgeArticles.length > 0">
            <div v-for="article in knowledgeArticles" :key="article.id" class="assistant-card" style="cursor:pointer" @click="showArticle(article)">
              <div class="ac-title">{{ article.title }}</div>
              <div class="ac-text">{{ article.summary }}</div>
              <div style="display:flex;justify-content:space-between;align-items:center;margin-top:6px">
                <el-tag size="small" type="primary">{{ article.category }}</el-tag>
                <span style="font-size:11px;color:#9ca3af">浏览 {{ article.viewCount || 0 }} 次</span>
              </div>
            </div>
          </div>
          <div v-else class="assistant-card" style="cursor:pointer" @click="showDefaultArticle()">
            <div class="ac-title">《跨行转账超时处理操作指引》</div>
            <div class="ac-text">详细说明转账超时的排查步骤、调账流程和客户话术。</div>
            <div style="font-size:11px;color:#1a56db;margin-top:6px">📋 点击查看详情</div>
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

    <el-dialog v-model="articleDialogVisible" :title="currentArticle?.title || '知识库文章'" width="700px">
      <div v-if="currentArticle" style="font-size:14px;line-height:1.8">
        <div style="display:flex;gap:16px;margin-bottom:16px;color:#6b7280;font-size:12px">
          <span>📁 {{ currentArticle.category }}</span>
          <span>👤 {{ currentArticle.author }}</span>
          <span>📊 浏览 {{ currentArticle.viewCount || 0 }} 次</span>
        </div>
        <div style="margin-bottom:16px;padding:12px;background:#f3f4f6;border-radius:8px">
          <strong>摘要：</strong>{{ currentArticle.summary }}
        </div>
        <div class="markdown-content" v-html="renderMarkdown(currentArticle.content)"></div>
        <div style="margin-top:16px;padding-top:16px;border-top:1px solid #e5e7eb">
          <div style="font-size:12px;color:#6b7280">
            <strong>标签：</strong>
            <el-tag v-for="tag in (currentArticle.tags || '').split(',')" :key="tag" size="small" style="margin: 2px" type="info">{{ tag }}</el-tag>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showRevokeDialog" title="撤销工单" width="500px" :close-on-click-modal="false">
      <div style="font-size:14px;color:#6b7280;margin-bottom:16px">
        <p>撤销工单将将工单状态改为"已撤销"，此操作不可恢复。</p>
        <p style="margin-top:8px;color:#dc2626">⚠️ 请确认是否继续撤销此工单？</p>
      </div>
      <el-form-item label="撤销原因" required>
        <el-select v-model="revokeReason" style="width:100%">
          <el-option label="客户主动撤回" value="客户主动撤回" />
          <el-option label="工单重复创建" value="工单重复创建" />
          <el-option label="问题已自行解决" value="问题已自行解决" />
          <el-option label="信息填写错误" value="信息填写错误" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="revokeReason === '其他原因'" label="详细说明">
        <el-input v-model="revokeReasonDetail" type="textarea" :rows="3" placeholder="请输入详细的撤销原因..." />
      </el-form-item>
      <template #footer>
        <el-button @click="showRevokeDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmRevoke" :loading="revoking">确认撤销</el-button>
      </template>
    </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import { findRelatedArticles, analyzeTitle, getTicketById, revokeTicket } from '@/api/ticket'
import { marked } from 'marked'

marked.setOptions({
  gfm: true,
  breaks: true
})

const route = useRoute()

const renderMarkdown = (text) => {
  if (!text) return ''
  return marked.parse(text)
}

const loading = ref(true)
const ticketInfo = reactive({
  id: '',
  ticketNo: '',
  title: '',
  createTime: '',
  slaRemaining: '',
  customerNo: '',
  customerName: '',
  customerType: '',
  idCard: '',
  phone: '',
  address: '',
  cardNo: '',
  balance: '',
  riskLevel: '',
  description: '',
  transactionNo: '',
  amount: '',
  receiverAccount: '',
  transactionTime: '',
  type: '',
  status: '',
  urgency: '',
  department: '',
  channel: '',
  createdBy: ''
})

const workflowSteps = ref([
  { label: '工单创建', time: '-', completed: false, active: false },
  { label: '智能分类', time: '-', completed: false, active: false },
  { label: '自动派单', time: '-', completed: false, active: false },
  { label: '处理中', time: '-', completed: false, active: false },
  { label: '待审批', time: '-', completed: false, active: false },
  { label: '待回复', time: '-', completed: false, active: false },
  { label: '已结案', time: '-', completed: false, active: false },
  { label: '已撤销', time: '-', completed: false, active: false }
])

const timelineItems = ref([])

const ticketStatus = computed(() => ticketInfo.status || '待处理')

const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const canRevoke = computed(() => {
  const status = ticketStatus.value
  const statusValid = status !== '已结案' && status !== '已完成' && status !== '已撤销'
  const userValid = currentUser.value && ticketInfo.createdBy && 
    currentUser.value.username === ticketInfo.createdBy
  return statusValid && userValid
})

const showRevokeDialog = ref(false)
const revokeReason = ref('')
const revokeReasonDetail = ref('')
const revoking = ref(false)

const getStatusTags = computed(() => {
  const tags = []
  if (ticketInfo.urgency) {
    tags.push({ type: ticketInfo.urgency === '紧急' ? 'danger' : ticketInfo.urgency === '一般' ? 'warning' : 'info', text: ticketInfo.urgency })
  }
  if (ticketStatus.value) {
    let statusType = 'primary'
    if (ticketStatus.value === '已结案' || ticketStatus.value === '已完成') {
      statusType = 'success'
    } else if (ticketStatus.value === '处理中') {
      statusType = 'warning'
    } else if (ticketStatus.value === '已撤销') {
      statusType = 'danger'
    }
    tags.push({ type: statusType, text: ticketStatus.value })
  }
  if (ticketInfo.channel) {
    tags.push({ type: 'primary', text: ticketInfo.channel })
  }
  return tags
})

const loadTicketDetail = async () => {
  const ticketId = route.params.id
  if (!ticketId) return
  
  loading.value = true
  try {
    const res = await getTicketById(ticketId)
    const data = res.data || {}
    
    ticketInfo.id = data.id || ''
    ticketInfo.ticketNo = data.ticketNo || ''
    ticketInfo.title = data.title || ''
    ticketInfo.createTime = data.createTime || ''
    ticketInfo.slaRemaining = '1h 23min'
    ticketInfo.customerNo = data.customerNo || ''
    ticketInfo.customerName = maskName(data.customerName || '')
    ticketInfo.customerType = data.customerType || ''
    ticketInfo.idCard = maskIdCard(data.idCard || '')
    ticketInfo.phone = maskPhone(data.phone || '')
    ticketInfo.address = maskAddress(data.address || '')
    ticketInfo.cardNo = maskCard(data.cardNo || '')
    ticketInfo.balance = '¥ ****.**'
    ticketInfo.riskLevel = '低风险'
    ticketInfo.description = data.description || ''
    ticketInfo.transactionNo = maskTransaction(data.transactionNo || '')
    ticketInfo.amount = '¥ ****.**'
    ticketInfo.receiverAccount = maskCard(data.receiverAccount || '')
    ticketInfo.transactionTime = data.transactionTime || ''
    ticketInfo.type = data.type || ''
    ticketInfo.status = data.status || ''
    ticketInfo.urgency = data.urgency || ''
    ticketInfo.department = data.department || ''
    ticketInfo.channel = data.channel || ''
    ticketInfo.createdBy = data.createdBy || ''
    
    initWorkflow()
    initTimeline(data)
    
    loadKnowledgeArticles()
    loadAIAnalysis()
  } catch (error) {
    console.error('加载工单详情失败', error)
  } finally {
    loading.value = false
  }
}

const maskName = (name) => {
  if (!name || name.length <= 1) return name
  return name[0] + '*' + name.slice(-1)
}

const maskIdCard = (id) => {
  if (!id || id.length < 8) return id
  return id.slice(0, 6) + '**********' + id.slice(-4)
}

const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const maskAddress = (addr) => {
  if (!addr) return addr
  const parts = addr.split('')
  if (parts.length <= 6) return addr
  return addr.slice(0, 6) + '***' + addr.slice(-2)
}

const maskCard = (card) => {
  if (!card) return card
  const cleaned = card.replace(/\s/g, '')
  if (cleaned.length < 8) return card
  return cleaned.slice(0, 4) + ' **** **** ' + cleaned.slice(-4)
}

const maskTransaction = (no) => {
  if (!no || no.length < 8) return no
  return no.slice(0, 8) + '****' + no.slice(-4)
}

const initWorkflow = () => {
  const status = ticketStatus.value
  const steps = [
    { label: '工单创建', time: ticketInfo.createTime.split(' ')[1] || '09:15', completed: true, active: false },
    { label: '智能分类', time: ticketInfo.createTime.split(' ')[1] || '09:15', completed: true, active: false },
    { label: '自动派单', time: ticketInfo.createTime.split(' ')[1] || '09:15', completed: true, active: false },
    { label: '处理中', time: status === '处理中' ? '进行中' : '-', completed: status === '已结案' || status === '待审批' || status === '待回复' || status === '已撤销', active: status === '处理中' },
    { label: '待审批', time: status === '待审批' ? '进行中' : '-', completed: status === '已结案' || status === '待回复' || status === '已撤销', active: status === '待审批' },
    { label: '待回复', time: status === '待回复' ? '进行中' : '-', completed: status === '已结案' || status === '已撤销', active: status === '待回复' },
    { label: '已结案', time: status === '已结案' ? ticketInfo.updateTime || '-' : '-', completed: status === '已结案', active: false },
    { label: '已撤销', time: status === '已撤销' ? ticketInfo.updateTime || '-' : '-', completed: status === '已撤销', active: false }
  ]
  workflowSteps.value = steps
}

const initTimeline = (data) => {
  const items = []
  
  items.push({
    time: data.createTime || '',
    user: '系统（AI自动）',
    content: `🤖 <strong>智能分类</strong>：${data.department || '零售业务'} → ${data.type || '交易异常'} | 置信度 96.8%`,
    type: 'system'
  })
  
  items.push({
    time: data.createTime || '',
    user: '系统（AI自动）',
    content: `🤖 <strong>智能派单</strong>：自动派发至 ${data.department || '零售业务部'} → 张三（技能匹配度 95%，当前负载低）`,
    type: 'system'
  })
  
  if (data.status === '处理中') {
    items.push({
      time: new Date().toLocaleString('zh-CN'),
      user: '张三（一线客服）',
      content: '已接单，正在查询核心系统交易状态。已联系客户安抚情绪。',
      type: 'user'
    })
  }
  
  if (data.status === '已结案') {
    items.push({
      time: new Date().toLocaleString('zh-CN'),
      user: '张三（一线客服）',
      content: '工单已处理完成，客户反馈满意。',
      type: 'user'
    })
  }
  
  timelineItems.value = items
}

const remark = ref('')

const knowledgeArticles = ref([])
const aiAnalysis = ref(null)
const loadingArticles = ref(false)

const loadKnowledgeArticles = async () => {
  loadingArticles.value = true
  try {
    const res = await findRelatedArticles(ticketInfo.title)
    knowledgeArticles.value = res.data || []
  } catch (error) {
    console.error('加载知识库文章失败', error)
  } finally {
    loadingArticles.value = false
  }
}

const loadAIAnalysis = async () => {
  try {
    const res = await analyzeTitle(ticketInfo.title)
    aiAnalysis.value = res.data || null
  } catch (error) {
    console.error('AI分析失败', error)
  }
}

const copyText = () => {
  const text = '李先生您好，您反映的转账问题我们已经受理。经查询，您的资金目前在银行内部处理中，是安全的。我们已为您加急处理，预计2小时内资金将退回您的账户，届时我们会第一时间通知您。非常抱歉给您带来不便。'
  navigator.clipboard.writeText(text)
}

const currentArticle = ref(null)
const articleDialogVisible = ref(false)

const showArticle = (article) => {
  currentArticle.value = article
  articleDialogVisible.value = true
}

const showDefaultArticle = () => {
  currentArticle.value = {
    title: '跨行转账超时处理操作指引',
    category: '转账汇款',
    author: '信息科技部',
    viewCount: 1258,
    summary: '详细说明跨行转账超时的排查步骤、调账流程和客户沟通话术',
    tags: '转账,汇款,超时,调账,跨行',
    content: '## 跨行转账超时处理操作指引\n\n### 一、问题定位\n1. 登录核心银行系统查询交易状态\n2. 确认资金当前位置（过渡户/已扣款/未扣款）\n3. 检查通道状态和清算进度\n\n### 二、处理方案\n\n#### 方案一：发起调账（推荐）\n- 适用场景：资金已扣款但未到账\n- 操作步骤：\n  1. 在调账系统提交调账申请\n  2. 选择\"跨行转账超时\"调账类型\n  3. 填写交易流水号和金额\n  4. 提交审批\n- 预计时效：2小时内完成\n\n#### 方案二：等待自动冲正\n- 适用场景：系统正在处理中\n- 说明：如交易最终失败，系统会自动冲正\n- 等待时间：最长24小时\n\n### 三、客户沟通话术\n\"XX先生/女士您好，您反映的转账问题我们已经受理。经查询，您的资金目前在银行内部处理中，是安全的。我们已为您加急处理，预计2小时内资金将退回您的账户，届时我们会第一时间通知您。\"\n\n### 四、注意事项\n1. 及时处理，避免超时\n2. 使用标准话术，不承诺具体到账时间\n3. 做好记录，便于后续跟进'
  }
  articleDialogVisible.value = true
}

const confirmRevoke = async () => {
  if (!revokeReason.value) {
    ElMessage.warning('请选择撤销原因')
    return
  }
  
  let reason = revokeReason.value
  if (revokeReason.value === '其他原因' && revokeReasonDetail.value) {
    reason += ' - ' + revokeReasonDetail.value
  }
  
  revoking.value = true
  
  try {
    const res = await revokeTicket(ticketInfo.id, { reason })
    if (res.data) {
      ElMessage.success('工单已撤销')
      showRevokeDialog.value = false
      ticketInfo.status = '已撤销'
      initWorkflow()
      timelineItems.value.push({
        time: new Date().toLocaleString('zh-CN'),
        user: '系统',
        content: `工单已撤销，撤销原因：${reason}`,
        type: 'system'
      })
    }
  } catch (error) {
    console.error('撤销工单失败', error)
    ElMessage.error(error.response?.data?.message || '撤销工单失败')
  } finally {
    revoking.value = false
  }
}

onMounted(() => {
  loadTicketDetail()
})
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

.markdown-content {
  line-height: 1.8;
  color: #374151;
}

.markdown-content h1 {
  font-size: 20px;
  font-weight: 700;
  margin: 20px 0 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #1a56db;
  color: #1a56db;
}

.markdown-content h2 {
  font-size: 16px;
  font-weight: 700;
  margin: 18px 0 10px;
  color: #1f2937;
}

.markdown-content h3 {
  font-size: 14px;
  font-weight: 600;
  margin: 14px 0 8px;
  color: #374151;
}

.markdown-content p {
  margin: 8px 0;
}

.markdown-content ul, .markdown-content ol {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-content li {
  margin: 4px 0;
}

.markdown-content strong {
  font-weight: 600;
  color: #1f2937;
}

.markdown-content em {
  font-style: italic;
}

.markdown-content blockquote {
  border-left: 4px solid #1a56db;
  padding: 8px 16px;
  background: #eff6ff;
  margin: 12px 0;
  border-radius: 0 8px 8px 0;
}

.markdown-content code {
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: monospace;
}

.markdown-content pre {
  background: #1f2937;
  color: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}

.markdown-content pre code {
  background: transparent;
  color: inherit;
  padding: 0;
}

.markdown-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.markdown-content th, .markdown-content td {
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content th {
  background: #f9fafb;
  font-weight: 600;
}

.markdown-content hr {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 16px 0;
}

.markdown-content a {
  color: #1a56db;
  text-decoration: underline;
}

.markdown-content img {
  max-width: 100%;
  border-radius: 8px;
}

.rich-text-content {
  font-size: 13px;
  line-height: 1.8;
  color: #4b5563;
  margin-bottom: 12px;
}

.rich-text-content p {
  margin: 6px 0;
}

.rich-text-content strong {
  font-weight: 600;
  color: #1f2937;
}

.rich-text-content ul, .rich-text-content ol {
  margin: 8px 0;
  padding-left: 24px;
}

.rich-text-content li {
  margin: 4px 0;
}

.rich-text-content h1, .rich-text-content h2, .rich-text-content h3 {
  font-weight: 600;
  margin: 12px 0 6px;
}

.rich-text-content h1 {
  font-size: 18px;
  border-bottom: 2px solid #1a56db;
  padding-bottom: 4px;
}

.rich-text-content h2 {
  font-size: 15px;
}

.rich-text-content h3 {
  font-size: 14px;
}

.rich-text-content blockquote {
  border-left: 4px solid #1a56db;
  padding: 8px 16px;
  background: #eff6ff;
  margin: 12px 0;
  border-radius: 0 8px 8px 0;
}

.rich-text-content a {
  color: #1a56db;
  text-decoration: underline;
}

.rich-text-content img {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}
</style>