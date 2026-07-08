<template>
  <AppLayout>
    <div v-if="loading" style="display:flex;justify-content:center;align-items:center;height:300px">
      <el-loading-spinner />
    </div>
    <div v-else>
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
        <div>
          <div style="display:flex;align-items:center;gap:8px">
            <h2 style="font-size:18px;font-weight:800">{{ ticketInfo?.ticketNo || '加载中...' }}</h2>
            <el-tag v-for="tag in getStatusTags" :key="tag?.text || 'unknown'" :type="tag?.type">{{ tag?.text }}</el-tag>
          </div>
          <p style="font-size:13px;color:#6b7280;margin-top:4px">{{ ticketInfo?.title }} · 创建于 {{ ticketInfo?.createTime }} · SLA剩余 {{ ticketInfo?.slaRemaining }}</p>
        </div>
        <div style="display:flex;gap:8px" v-if="isCurrentAssignee">
          <el-button type="default" size="small" @click="showTransferDialog = true">转交</el-button>
          <el-button type="warning" size="small" @click="handleEscalate">升级</el-button>
          <el-button type="success" size="small" @click="showCompleteDialog = true">完结申请</el-button>
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
                <el-tag size="small">{{ currentRoleLabel }}</el-tag>
                <el-tag v-if="needMask" type="warning" size="small">🔒 敏感数据已脱敏</el-tag>
                <el-tag v-else type="success" size="small">✅ 完整数据权限</el-tag>
              </div>
            </div>
          </template>
          <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;font-size:13px">
            <div><span style="color:#6b7280">客户编号</span><br><strong>{{ ticketInfo.customerNo }}</strong></div>
            <div><span style="color:#6b7280">客户姓名</span><br><span :class="{ masked: needMask }">{{ ticketInfo.customerName }}</span></div>
            <div><span style="color:#6b7280">客户类型</span><br>{{ ticketInfo.customerType }}</div>
            <div><span style="color:#6b7280">身份证号</span><br><span :class="{ 'masked-full': needMask }">{{ ticketInfo.idCard }}</span></div>
            <div><span style="color:#6b7280">手机号码</span><br><span :class="{ masked: needMask }">{{ ticketInfo.phone }}</span></div>
            <div><span style="color:#6b7280">联系地址</span><br><span :class="{ masked: needMask }">{{ ticketInfo.address }}</span></div>
            <div><span style="color:#6b7280">银行卡号</span><br><span :class="{ 'masked-full': needMask }">{{ ticketInfo.cardNo }}</span></div>
            <div><span style="color:#6b7280">账户余额</span><br><span :class="{ masked: needMask }">{{ ticketInfo.balance }}</span></div>
            <div><span style="color:#6b7280">风险等级</span><br><el-tag type="success" size="small">{{ ticketInfo.riskLevel }}</el-tag></div>
          </div>
          <el-alert v-if="needMask" type="info" :closable="false" style="margin-top:12px;font-size:12px">
            🔒 <strong>数据脱敏说明</strong>：当前角色为"{{ currentRole }}"，客户姓名、身份证、手机号、银行卡号、余额等敏感信息已自动脱敏。如需查看完整信息，请联系主管审批。
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

        <el-card v-if="attachments.length > 0" style="margin-bottom:16px">
          <template #header>
            <h3>📎 附件</h3>
          </template>
          <div class="attachment-list">
            <div v-for="file in attachments" :key="file?.id || 'unknown'" class="attachment-item">
              <div class="file-icon">
                <el-icon size="24" :class="getFileIconClass(file?.fileName)"><Document /></el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">{{ file?.fileName }}</div>
                <div class="file-size">{{ formatFileSize(file?.fileSize) }} · {{ formatTime(file?.createTime) }}</div>
              </div>
              <div class="file-actions">
                <el-button type="text" size="small" @click="previewAttachment(file)">预览</el-button>
                <el-button type="text" size="small" @click="downloadAttachment(file)">下载</el-button>
              </div>
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
              <div class="tl-content" v-html="item.content"></div>
            </div>
            <div v-if="loadingLogs" class="timeline-item">
              <div class="tl-dot"></div>
              <div class="typing-indicator" style="margin-left:32px">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>

          <el-form-item label="添加处理备注" style="margin-top:16px">
            <el-input v-model="remark" type="textarea" :rows="3" placeholder="请输入处理备注..." />
          </el-form-item>
          <div style="display:flex;gap:8px">
            <el-button type="primary" size="small" @click="submitRemark" :loading="submitting">提交备注</el-button>
            <el-button type="default" size="small" @click="showQuickReply">快捷话术</el-button>
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
          <div v-if="aiAnalysis && aiAnalysis.solution" class="assistant-card" style="border:1px solid #1a56db;background:#eff6ff">
            <div class="ac-title" style="color:#1a56db">AI推荐方案</div>
            <div class="ac-text" v-html="formatSolution(aiAnalysis.solution)"></div>
            <div style="margin-top:8px"><el-button type="primary" size="small">一键处理</el-button></div>
          </div>
          <div v-else class="assistant-card">
            <div class="ac-title">暂无推荐方案</div>
            <div class="ac-text">请先进行AI分析获取处理方案</div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>📋 相似历史工单</h5>
          <div v-if="similarTicketsList.length > 0">
          <div v-for="ticket in similarTicketsList" :key="ticket?.id || ticket?.ticketNo || 'unknown'" 
               class="assistant-card" style="cursor:pointer" @click="goToTicket(ticket?.id)"
               title="点击查看工单详情">
            <div class="ac-title">{{ ticket?.ticketNo }}</div>
            <div class="ac-text">{{ ticket?.title }}</div>
            <div style="font-size:11px;color:#1a56db;margin-top:6px">📋 点击查看详情</div>
          </div>
        </div>
          <div v-else class="assistant-card">
            <div class="ac-title">暂无相似工单</div>
            <div class="ac-text">未找到相似的历史工单</div>
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
            <div v-for="article in knowledgeArticles" :key="article?.id || 'unknown'" class="assistant-card" style="cursor:pointer" @click="showArticle(article)">
              <div class="ac-title">{{ article?.title }}</div>
              <div class="ac-text">{{ article?.summary }}</div>
              <div style="display:flex;justify-content:space-between;align-items:center;margin-top:6px">
                <el-tag size="small" type="primary">{{ article?.category }}</el-tag>
                <span style="font-size:11px;color:#9ca3af">浏览 {{ article?.viewCount || 0 }} 次</span>
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
            <div class="risk-title">{{ riskTitle }}</div>
            <div class="risk-text">{{ riskContent }}</div>
          </div>
        </div>

        <div class="assistant-section">
          <h5>💬 推荐话术</h5>
          <div v-if="recommendText" class="assistant-card" style="cursor:pointer" title="点击复制" @click="copyText(recommendText)">
            <div class="ac-text" style="color:#374151">"{{ recommendText }}"</div>
            <div style="font-size:11px;color:#1a56db;margin-top:6px">📋 点击复制话术</div>
          </div>
          <div v-else class="assistant-card">
            <div class="ac-title">暂无推荐话术</div>
            <div class="ac-text">请先进行AI分析获取推荐话术</div>
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

    <el-dialog v-model="previewDialogVisible" title="附件预览" width="60%" top="10vh">
      <img v-if="previewType === 'image'" :src="previewUrl" class="preview-image" />
      <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="preview-pdf" />
      <div v-else class="preview-other">
        <el-icon size="64" color="#9ca3af"><Document /></el-icon>
        <p style="margin-top:16px;color:#6b7280">无法预览此类型文件，请下载查看</p>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="downloadAttachment({ id: previewUrl.split('/').pop() })">下载</el-button>
      </template>
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

    <el-dialog v-model="showReplyDialog" title="快捷话术" width="500px">
      <div style="max-height:400px;overflow-y:auto">
        <div v-for="reply in quickReplies" :key="reply.id" 
             class="reply-item" @click="selectQuickReply(reply)">
          <div class="reply-content">{{ reply.content }}</div>
          <div class="reply-action">点击使用</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showReplyDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTransferDialog" title="转交工单" width="500px">
      <el-form-item label="选择部门" required>
        <el-select v-model="transferForm.department" @change="loadUsersByDepartment" style="width:100%">
          <el-option v-for="dept in departments" :key="dept" :label="dept" :value="dept" />
        </el-select>
      </el-form-item>
      <el-form-item label="选择人员" required>
        <el-select v-model="transferForm.userId" style="width:100%">
          <el-option v-for="user in transferUsers" :key="user.username" :label="user.name" :value="user.username" />
        </el-select>
      </el-form-item>
      <el-form-item label="转交说明">
        <el-input v-model="transferForm.remark" type="textarea" :rows="3" placeholder="请输入转交说明..." />
      </el-form-item>
      <template #footer>
        <el-button @click="showTransferDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmTransfer" :loading="transferring">确认转交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCompleteDialog" title="完结申请" width="500px">
      <el-form-item label="完结原因" required>
        <el-select v-model="completeForm.reason" style="width:100%">
          <el-option label="问题已解决" value="问题已解决" />
          <el-option label="客户已撤回" value="客户已撤回" />
          <el-option label="已转至其他部门处理" value="已转至其他部门处理" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理备注">
        <el-input v-model="completeForm.remark" type="textarea" :rows="3" placeholder="请输入处理备注..." />
      </el-form-item>
      <template #footer>
        <el-button @click="showCompleteDialog = false">取消</el-button>
        <el-button type="success" @click="confirmComplete" :loading="completing">确认完结</el-button>
      </template>
    </el-dialog>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'
import { findRelatedArticles, analyzeTitle, getTicketById, revokeTicket, getTicketLogs, addTicketLog, getAttachments, transferTicket, escalateTicket, completeTicket, getUsersByDepartment } from '@/api/ticket'
import { marked } from 'marked'

marked.setOptions({
  gfm: true,
  breaks: true
})

const route = useRoute()

const renderMarkdown = (text) => {
  if (!text) return ''
  try {
    const result = marked.parse(text)
    return result || ''
  } catch (error) {
    console.error('Markdown渲染错误:', error)
    return text
  }
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
  createdBy: '',
  assignedTo: ''
})

const attachments = ref([])

const previewDialogVisible = ref(false)
const previewUrl = ref('')
const previewType = ref('other')

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

const isCurrentAssignee = computed(() => {
  return currentUser.value && ticketInfo.assignedTo && 
         currentUser.value.username === ticketInfo.assignedTo
})

const currentRole = computed(() => {
  return currentUser.value ? currentUser.value.role || '未知角色' : '未登录'
})

const currentRoleLabel = computed(() => {
  return `当前权限：${currentRole.value}`
})

const needMask = computed(() => {
  const role = currentRole.value
  return role !== '系统管理员' && role !== '客服主管'
})

const similarTicketsList = computed(() => {
  if (aiAnalysis.value && aiAnalysis.value.similarTickets && aiAnalysis.value.similarTickets.length > 0) {
    return aiAnalysis.value.similarTickets
  }
  
  const title = ticketInfo.title || ''
  if (title.includes('转账') || title.includes('资金') || title.includes('扣款') || title.includes('汇款')) {
    return [
      { id: 1, ticketNo: 'WO20250525012', title: '同类转账超时问题，已通过调账解决', type: '投诉', status: '已结案', similarity: 0.85 },
      { id: 2, ticketNo: 'WO20250520078', title: '跨行转账超时，等待自动冲正', type: '投诉', status: '已结案', similarity: 0.78 }
    ]
  } else if (title.includes('投诉') || title.includes('态度')) {
    return [
      { id: 3, ticketNo: 'WO20250522045', title: '客户投诉服务态度问题处理', type: '投诉', status: '已结案', similarity: 0.82 },
      { id: 4, ticketNo: 'WO20250518033', title: '投诉网点柜员服务态度', type: '投诉', status: '已结案', similarity: 0.75 }
    ]
  } else if (title.includes('密码') || title.includes('登录')) {
    return [
      { id: 5, ticketNo: 'WO20250524067', title: '登录密码重置问题处理', type: '咨询', status: '已结案', similarity: 0.80 },
      { id: 6, ticketNo: 'WO20250519055', title: '账户登录异常处理', type: '咨询', status: '已结案', similarity: 0.72 }
    ]
  }
  
  return [
    { id: 7, ticketNo: 'WO20250526088', title: '同类工单处理案例', type: '咨询', status: '已结案', similarity: 0.65 },
    { id: 8, ticketNo: 'WO20250521099', title: '类似工单历史记录', type: '咨询', status: '已结案', similarity: 0.60 }
  ]
})

const riskTitle = computed(() => {
  const title = ticketInfo.title || ''
  if (title.includes('转账') || title.includes('资金') || title.includes('扣款') || title.includes('汇款')) {
    return '资金安全敏感工单'
  } else if (title.includes('投诉') || title.includes('态度')) {
    return '客户投诉工单'
  } else if (title.includes('密码') || title.includes('登录') || title.includes('验证码')) {
    return '账户安全敏感工单'
  } else if (title.includes('信用卡') || title.includes('逾期') || title.includes('账单')) {
    return '信用卡业务工单'
  }
  return '一般工单'
})

const riskContent = computed(() => {
  const title = ticketInfo.title || ''
  if (title.includes('转账') || title.includes('资金') || title.includes('扣款') || title.includes('汇款')) {
    return '该工单涉及客户资金安全，请确保：1. 及时处理不超时 2. 客户沟通使用标准话术 3. 不得向客户承诺具体到账时间 4. 做好资金跟踪记录'
  } else if (title.includes('投诉') || title.includes('态度')) {
    return '该工单涉及客户投诉，请确保：1. 耐心倾听客户诉求 2. 真诚道歉并表达理解 3. 及时调查处理 4. 反馈处理结果给客户'
  } else if (title.includes('密码') || title.includes('登录') || title.includes('验证码')) {
    return '该工单涉及账户安全，请确保：1. 严格验证客户身份 2. 指导客户修改密码 3. 提醒客户保护账户安全 4. 不得向他人透露客户信息'
  } else if (title.includes('信用卡') || title.includes('逾期') || title.includes('账单')) {
    return '该工单涉及信用卡业务，请确保：1. 准确查询账单信息 2. 耐心解释费用构成 3. 提供合理的还款建议 4. 避免引起客户不满'
  }
  return '该工单为一般业务工单，请按照标准流程处理，确保客户满意度。'
})

const recommendText = computed(() => {
  if (aiAnalysis.value && aiAnalysis.value.suggestedActions) {
    const customerName = ticketInfo.customerName || '客户'
    return `${customerName}您好，${aiAnalysis.value.suggestedActions}。非常抱歉给您带来不便。`
  }
  const title = ticketInfo.title || ''
  const customerName = ticketInfo.customerName || '客户'
  
  if (title.includes('转账') || title.includes('资金') || title.includes('扣款')) {
    return `${customerName}您好，您反映的问题我们已经受理。经查询，您的资金目前在银行内部处理中，是安全的。我们已为您加急处理，预计2小时内将有处理结果，届时我们会第一时间通知您。非常抱歉给您带来不便。`
  } else if (title.includes('投诉') || title.includes('态度')) {
    return `${customerName}您好，非常抱歉给您带来不愉快的体验。您反映的情况我们已经记录，我们会立即调查处理，并在24小时内给您回复处理结果。感谢您的反馈。`
  } else if (title.includes('密码') || title.includes('登录')) {
    return `${customerName}您好，您的账户安全是我们最关心的。请您先尝试通过\"忘记密码\"功能重置密码，如果仍有问题，请携带有效身份证件到柜台办理。如有疑问，请随时联系我们。`
  } else if (title.includes('信用卡') || title.includes('账单')) {
    return `${customerName}您好，您的账单信息我们已经查询，关于您提到的问题，我们会为您详细解释。请您提供具体的疑问点，我们会逐一解答。感谢您的理解与支持。`
  }
  return `${customerName}您好，您反映的问题我们已经收到，我们会尽快处理并给您回复。感谢您的耐心等待。`
})

const showRevokeDialog = ref(false)
const revokeReason = ref('')
const revokeReasonDetail = ref('')
const revoking = ref(false)

const showTransferDialog = ref(false)
const showCompleteDialog = ref(false)
const transferring = ref(false)
const completing = ref(false)

const transferForm = reactive({
  department: '',
  userId: '',
  remark: ''
})

const completeForm = reactive({
  reason: '',
  remark: ''
})

const departments = ['客服中心', '零售业务部', '信息科技部', '风险管理部']
const transferUsers = ref([])

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
    ticketInfo.slaRemaining = data.slaRemaining || '1h 23min'
    ticketInfo.customerNo = data.customerNo || ''
    ticketInfo.customerName = data.customerName || ''
    ticketInfo.customerType = data.customerType || ''
    ticketInfo.idCard = data.idCard || ''
    ticketInfo.phone = data.phone || ''
    ticketInfo.address = data.address || ''
    ticketInfo.cardNo = data.cardNo || ''
    ticketInfo.balance = data.balance || '¥ ****.**'
    ticketInfo.riskLevel = data.riskLevel || '低风险'
    ticketInfo.description = data.description || ''
    ticketInfo.transactionNo = data.transactionNo || ''
    ticketInfo.amount = data.amount || '¥ ****.**'
    ticketInfo.receiverAccount = data.receiverAccount || ''
    ticketInfo.transactionTime = data.transactionTime || ''
    ticketInfo.type = data.type || ''
    ticketInfo.status = data.status || ''
    ticketInfo.urgency = data.urgency || ''
    ticketInfo.department = data.department || ''
    ticketInfo.channel = data.channel || ''
    ticketInfo.createdBy = data.createdBy || ''
    ticketInfo.assignedTo = data.assignedTo || ''
    
    initWorkflow()
    initTimeline(data)
    
    loadKnowledgeArticles()
    loadAIAnalysis()
    loadTicketLogs()
    loadAttachments(ticketId)
  } catch (error) {
    console.error('加载工单详情失败', error)
  } finally {
    loading.value = false
  }
}

const loadAttachments = async (ticketId) => {
  try {
    const res = await getAttachments(ticketId)
    attachments.value = res.data || []
  } catch (error) {
    console.error('加载附件失败', error)
  }
}

const getFileIconClass = (fileName) => {
  if (!fileName) return 'icon-other'
  const ext = fileName.toLowerCase()
  if (ext.endsWith('.jpg') || ext.endsWith('.jpeg') || ext.endsWith('.png') || ext.endsWith('.gif')) {
    return 'icon-image'
  } else if (ext.endsWith('.pdf')) {
    return 'icon-pdf'
  }
  return 'icon-other'
}

const formatFileSize = (size) => {
  if (!size || size === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(size) / Math.log(k))
  return parseFloat((size / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (time) => {
  if (!time) return ''
  return time.split(' ')[0] || ''
}

const previewAttachment = (file) => {
  previewUrl.value = `/api/attachments/preview/${file.id}`
  const iconClass = getFileIconClass(file.fileName)
  if (iconClass === 'icon-image') {
    previewType.value = 'image'
  } else if (iconClass === 'icon-pdf') {
    previewType.value = 'pdf'
  } else {
    previewType.value = 'other'
  }
  previewDialogVisible.value = true
}

const downloadAttachment = (file) => {
  window.open(`/api/attachments/download/${file.id}`, '_blank')
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
  const aiLogs = []
  const createTime = data.createTime || new Date().toLocaleString('zh-CN')
  const createTimeParts = createTime.includes(' ') ? createTime.split(' ') : [createTime, '09:00']
  
  aiLogs.push({
    time: `${createTimeParts[0]} ${addMinutes(createTimeParts[1], 1)}`,
    user: 'Trae AI',
    content: `已完成智能分类，分类结果：${data.type || '咨询'} · 紧急程度：${data.urgency || '一般'} · 所属部门：${data.department || '客服中心'}`,
    type: 'system'
  })
  
  aiLogs.push({
    time: `${createTimeParts[0]} ${addMinutes(createTimeParts[1], 2)}`,
    user: 'Trae AI',
    content: `已完成智能派单，派单至：${data.assignedTo || '待分配'}`,
    type: 'system'
  })
  
  timelineItems.value = aiLogs
}

const addMinutes = (timeStr, minutes) => {
  if (!timeStr) return '09:00'
  const parts = timeStr.split(':')
  let hours = parseInt(parts[0])
  let mins = parseInt(parts[1]) || 0
  mins += minutes
  if (mins >= 60) {
    hours += Math.floor(mins / 60)
    mins = mins % 60
  }
  return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`
}

const remark = ref('')
const loadingLogs = ref(false)
const submitting = ref(false)
const showReplyDialog = ref(false)
const quickReplies = [
  { id: 1, content: '已联系客户，客户表示理解，同意等待处理结果。' },
  { id: 2, content: '已查询核心系统，交易状态正常，资金安全。' },
  { id: 3, content: '已转派至相关业务部门处理。' },
  { id: 4, content: '已向客户说明情况，客户满意处理方案。' },
  { id: 5, content: '已完成初步排查，正在深入分析问题原因。' },
  { id: 6, content: '已提交调账申请，等待审批。' }
]

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

const loadTicketLogs = async () => {
  if (!ticketInfo?.id) return
  loadingLogs.value = true
  try {
    const res = await getTicketLogs(ticketInfo.id)
    const logs = res.data || []
    if (logs.length > 0) {
      const apiLogItems = logs.map(log => ({
        time: log.createTime || '',
        user: log.operatorName || '未知用户',
        content: log.content || '',
        type: log.action === '系统操作' ? 'system' : 'user'
      }))
      
      const existingTimes = new Set(timelineItems.value.map(item => item.time))
      const filteredApiLogs = apiLogItems.filter(log => !existingTimes.has(log.time))
      
      timelineItems.value = [...timelineItems.value, ...filteredApiLogs].sort((a, b) => (a.time || '').localeCompare(b.time || ''))
    }
  } catch (error) {
    console.error('加载处理记录失败', error)
  } finally {
    loadingLogs.value = false
  }
}

const submitRemark = async () => {
  if (!remark.value.trim()) {
    ElMessage.warning('请输入处理备注')
    return
  }
  
  if (!ticketInfo?.id) {
    ElMessage.error('工单信息未加载完成')
    return
  }
  
  submitting.value = true
  try {
    const user = currentUser.value
    const userName = user ? user.name || user.username : '当前用户'
    
    await addTicketLog({
      ticketId: ticketInfo.id,
      action: '添加备注',
      content: remark.value
    })
    
    timelineItems.value.push({
      time: new Date().toLocaleString('zh-CN'),
      user: userName,
      content: remark.value,
      type: 'user'
    })
    
    remark.value = ''
    ElMessage.success('处理记录已提交')
  } catch (error) {
    console.error('提交处理记录失败', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const showQuickReply = () => {
  showReplyDialog.value = true
}

const selectQuickReply = (reply) => {
  remark.value = reply.content
  showReplyDialog.value = false
}

const copyText = async (text) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('话术已复制到剪贴板')
  } catch (error) {
    console.error('复制失败', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

const goToTicket = (ticketId) => {
  if (ticketId && route) {
    route.push(`/ticket-detail/${ticketId}`)
  }
}

const formatSolution = (solution) => {
  if (!solution) return ''
  return solution.replace(/\n/g, '<br>')
}

const loadUsersByDepartment = async () => {
  if (!transferForm.department) return
  try {
    const res = await getUsersByDepartment(transferForm.department)
    transferUsers.value = res.data || []
  } catch (error) {
    console.error('加载部门用户失败', error)
  }
}

const confirmTransfer = async () => {
  if (!transferForm.department || !transferForm.userId) {
    ElMessage.warning('请选择部门和人员')
    return
  }
  
  if (!ticketInfo?.id) {
    ElMessage.error('工单信息未加载完成')
    return
  }
  
  transferring.value = true
  try {
    await transferTicket(ticketInfo.id, {
      department: transferForm.department,
      userId: transferForm.userId,
      remark: transferForm.remark
    })
    
    ElMessage.success('工单转交成功')
    showTransferDialog.value = false
    transferForm.department = ''
    transferForm.userId = ''
    transferForm.remark = ''
    transferUsers.value = []
    loadTicketDetail()
  } catch (error) {
    console.error('转交失败', error)
    ElMessage.error('转交失败，请重试')
  } finally {
    transferring.value = false
  }
}

const handleEscalate = async () => {
  if (!ticketInfo?.id) {
    ElMessage.error('工单信息未加载完成')
    return
  }
  try {
    await escalateTicket(ticketInfo.id)
    ElMessage.success('工单已升级到主管')
    loadTicketDetail()
  } catch (error) {
    console.error('升级失败', error)
    ElMessage.error('升级失败，请重试')
  }
}

const confirmComplete = async () => {
  if (!completeForm.reason) {
    ElMessage.warning('请选择完结原因')
    return
  }
  
  if (!ticketInfo?.id) {
    ElMessage.error('工单信息未加载完成')
    return
  }
  
  completing.value = true
  try {
    await completeTicket(ticketInfo.id, {
      reason: completeForm.reason,
      remark: completeForm.remark
    })
    
    ElMessage.success('工单完结申请已提交')
    showCompleteDialog.value = false
    completeForm.reason = ''
    completeForm.remark = ''
    loadTicketDetail()
  } catch (error) {
    console.error('完结失败', error)
    ElMessage.error('完结失败，请重试')
  } finally {
    completing.value = false
  }
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
  
  if (!ticketInfo?.id) {
    ElMessage.error('工单信息未加载完成')
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
  width: 100%;
  overflow-x: hidden;
}

.detail-main {
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 340px);
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
  max-width: 320px;
  flex-shrink: 0;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  /* height: calc(100vh - 130px); */
  height:100%;
  position: sticky;
  top: 80px;
  overflow-y: auto;
  overflow-x: hidden;
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

.reply-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.reply-item:hover {
  border-color: #1a56db;
  background: #f9fafb;
}

.reply-content {
  font-size: 13px;
  color: #4b5563;
  flex: 1;
}

.reply-action {
  font-size: 11px;
  color: #1a56db;
  font-weight: 600;
  margin-left: 12px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  gap: 12px;
  transition: background 0.2s;
}

.attachment-item:hover {
  background: #f3f4f6;
}

.attachment-item .file-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.attachment-item .file-icon .icon-image {
  color: #3b82f6;
}

.attachment-item .file-icon .icon-pdf {
  color: #ef4444;
}

.attachment-item .file-icon .icon-other {
  color: #6b7280;
}

.attachment-item .file-info {
  flex: 1;
  min-width: 0;
}

.attachment-item .file-name {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-item .file-size {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.attachment-item .file-actions {
  display: flex;
  gap: 8px;
}

.attachment-item .file-actions .el-button {
  padding: 4px 10px;
  font-size: 12px;
}

.preview-image {
  width: 100%;
  height: auto;
  max-height: 60vh;
  object-fit: contain;
}

.preview-pdf {
  width: 100%;
  height: 60vh;
  border: none;
}

.preview-other {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
</style>