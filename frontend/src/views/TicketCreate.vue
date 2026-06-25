<template>
  <AppLayout>
    <div style="margin-bottom:20px">
      <h2 style="font-size:20px;font-weight:800">创建工单</h2>
      <p style="font-size:13px;color:#6b7280;margin-top:4px">填写工单信息，AI将实时为您智能预判问题类型并推荐解决方案</p>
    </div>
    <div class="detail-layout">
      <div class="detail-main">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>基本信息</h3>
              <el-tag type="success">AI智能辅助已开启</el-tag>
            </div>
          </template>
          <el-form :model="form" label-width="100px" style="padding:20px">
            <el-form-item label="工单标题" required>
              <el-input v-model="form.title" placeholder="请输入工单标题，AI将实时分析并智能推荐..." @input="handleTitleInput" />
              <div style="font-size:11px;color:#9ca3af;margin-top:4px">输入标题时，AI将自动分析问题原因、推荐解决方案和相似历史工单</div>
            </el-form-item>
            <div v-if="showAIPanel" class="ai-chat-panel">
              <div class="ai-header">
                <div class="ai-avatar">🤖</div>
                <div class="ai-info">
                  <div class="ai-name">智能助手</div>
                  <div class="ai-status">
                    <span v-if="aiLoading" class="loading-dots">正在分析中...</span>
                    <span v-else class="completed">分析完成</span>
                  </div>
                </div>
                <el-tag v-if="aiData.confidence > 0" size="small" type="success">置信度 {{ (aiData.confidence * 100).toFixed(0) }}%</el-tag>
              </div>
              <div class="ai-content">
                <div class="ai-section">
                  <div class="section-icon">🔍</div>
                  <div class="section-content">
                    <div class="section-title">问题分析</div>
                    <div class="section-body">
                      <div v-if="aiLoading" class="typing-indicator">
                        <span></span><span></span><span></span>
                      </div>
                      <div v-else class="analysis-text">
                        <p v-for="(paragraph, idx) in aiData.problemAnalysis?.split('\n\n')" :key="idx" class="paragraph">{{ paragraph }}</p>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="ai-section">
                  <div class="section-icon">💡</div>
                  <div class="section-content">
                    <div class="section-title">推荐方案</div>
                    <div class="section-body">
                      <div v-if="aiLoading" class="typing-indicator">
                        <span></span><span></span><span></span>
                      </div>
                      <div v-else class="solution-text">
                        <pre>{{ aiData.solution || '暂无推荐方案' }}</pre>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="ai-section">
                  <div class="section-icon">✅</div>
                  <div class="section-content">
                    <div class="section-title">建议操作</div>
                    <div class="section-body">
                      <div v-if="aiLoading" class="typing-indicator">
                        <span></span><span></span><span></span>
                      </div>
                      <div v-else class="actions-list">
                        <div v-for="(action, idx) in aiData.suggestedActions?.split('\n').filter(a => a.trim())" :key="idx" class="action-item">{{ action }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="ai-section">
                  <div class="section-icon">📋</div>
                  <div class="section-content">
                    <div class="section-title">相似工单</div>
                    <div class="section-body">
                      <div v-if="aiLoading" class="typing-indicator">
                        <span></span><span></span><span></span>
                      </div>
                      <div v-else-if="similarTickets.length === 0" class="empty-text">暂无相似工单</div>
                      <div v-else class="similar-tickets-list">
                        <div v-for="(ticket, idx) in similarTickets" :key="idx" class="similar-ticket-item" @click="viewSimilarTicket(ticket)">
                          <div class="ticket-header">
                            <span class="ticket-no">{{ ticket.ticketNo }}</span>
                            <el-tag size="small" :type="getTicketTypeTag(ticket.type)">{{ ticket.type }}</el-tag>
                            <span class="similarity-badge">{{ (ticket.similarity * 100).toFixed(0) }}%相似</span>
                          </div>
                          <div class="ticket-title">{{ ticket.title }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="ai-tags">
                  <el-tag v-if="aiData.categoryPath" type="info" effect="plain">{{ aiData.categoryPath }}</el-tag>
                  <el-tag v-if="aiData.ticketType" type="primary" effect="plain">{{ aiData.ticketType }}</el-tag>
                  <el-tag v-if="aiData.urgency" type="danger" effect="plain">{{ aiData.urgency }}</el-tag>
                  <el-tag v-if="aiData.department" type="success" effect="plain">{{ aiData.department }}</el-tag>
                </div>
                <div class="ai-actions">
                  <el-button type="primary" size="large" @click="applyAll">确认填充工单</el-button>
                  <el-button type="default" size="large" @click="resetAI">重新分析</el-button>
                </div>
              </div>
            </div>
            <el-row :gutter="16" style="margin-top:20px">
              <el-col :span="12">
                <el-form-item label="工单类型" required>
                  <el-select v-model="form.type">
                    <el-option label="投诉" value="投诉" />
                    <el-option label="咨询" value="咨询" />
                    <el-option label="办理" value="办理" />
                    <el-option label="故障" value="故障" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="紧急程度" required>
                  <el-select v-model="form.urgency">
                    <el-option label="紧急" value="紧急" />
                    <el-option label="一般" value="一般" />
                    <el-option label="较低" value="较低" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="来源渠道">
                  <el-select v-model="form.channel">
                    <el-option label="电话银行" value="电话银行" />
                    <el-option label="手机银行" value="手机银行" />
                    <el-option label="网上银行" value="网上银行" />
                    <el-option label="微信银行" value="微信银行" />
                    <el-option label="网点" value="网点" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="处理部门">
                  <el-select v-model="form.department">
                    <el-option label="零售业务部" value="零售业务部" />
                    <el-option label="信用卡中心" value="信用卡中心" />
                    <el-option label="对公业务部" value="对公业务部" />
                    <el-option label="风控合规部" value="风控合规部" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="客户编号">
                  <el-input v-model="form.customerNo" placeholder="输入客户编号自动关联" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户姓名">
                  <el-input v-model="form.customerName" :disabled="true" style="color:#9ca3af" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="联系电话">
                  <el-input v-model="form.phone" :disabled="true" style="color:#9ca3af" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="关联账户">
                  <el-input v-model="form.accountNo" :disabled="true" style="color:#9ca3af" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="问题描述" required>
              <div class="editor-wrapper">
                <QuillEditor
                  ref="editorRef"
                  :content="form.description"
                  contentType="html"
                  theme="snow"
                  :options="editorOptions"
                  style="height: 200px"
                  @update:content="(val) => form.description = val"
                />
              </div>
            </el-form-item>
            <el-form-item label="附件">
              <el-upload
                class="upload-demo"
                drag
                action="https://jsonplaceholder.typicode.com/posts/"
                :on-preview="handlePreview"
                :on-remove="handleRemove"
                :file-list="fileList"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">点击或拖拽上传附件</div>
              </el-upload>
            </el-form-item>
            <el-form-item style="margin-top:24px">
              <el-button type="primary">提交工单</el-button>
              <el-button type="default">保存草稿</el-button>
              <el-button @click="$router.back()">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import AppLayout from '@/components/AppLayout.vue'
import { analyzeTitle } from '@/api/ticket'

const router = useRouter()

const form = reactive({
  title: '',
  type: '',
  urgency: '',
  channel: '电话银行',
  department: '',
  customerNo: '',
  customerName: '',
  phone: '',
  accountNo: '',
  description: ''
})

const showAIPanel = ref(false)
const fileList = ref([])
const aiLoading = ref(false)
let debounceTimer = null

const aiData = reactive({
  categoryPath: '',
  ticketType: '',
  urgency: '',
  department: '',
  confidence: 0,
  solution: '',
  problemAnalysis: '',
  suggestedActions: ''
})

const similarTickets = ref([])
const templateFields = ref([])
const templateValues = ref([])

const editorRef = ref(null)
const editorOptions = {
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      [{ 'list': 'ordered'}, { 'list': 'bullet' }],
      [{ 'color': [] }, { 'background': [] }],
      ['clean']
    ]
  },
  placeholder: '请详细描述问题...'
}

onMounted(() => {
  restoreFormData()
})

const saveFormData = () => {
  const formData = {
    title: form.title,
    type: form.type,
    urgency: form.urgency,
    channel: form.channel,
    department: form.department,
    customerNo: form.customerNo,
    customerName: form.customerName,
    phone: form.phone,
    accountNo: form.accountNo,
    description: form.description,
    showAIPanel: showAIPanel.value,
    aiData: { ...aiData },
    similarTickets: similarTickets.value,
    templateFields: templateFields.value
  }
  sessionStorage.setItem('ticketCreateForm', JSON.stringify(formData))
}

const restoreFormData = () => {
  const saved = sessionStorage.getItem('ticketCreateForm')
  if (saved) {
    try {
      const data = JSON.parse(saved)
      Object.assign(form, {
        title: data.title || '',
        type: data.type || '',
        urgency: data.urgency || '',
        channel: data.channel || '电话银行',
        department: data.department || '',
        customerNo: data.customerNo || '',
        customerName: data.customerName || '',
        phone: data.phone || '',
        accountNo: data.accountNo || '',
        description: data.description || ''
      })
      if (data.showAIPanel) {
        showAIPanel.value = true
        Object.assign(aiData, data.aiData || {})
        similarTickets.value = data.similarTickets || []
        templateFields.value = data.templateFields || []
        templateValues.value = new Array(templateFields.value.length).fill('')
      }
      sessionStorage.removeItem('ticketCreateForm')
    } catch (e) {
      console.error('恢复表单数据失败', e)
    }
  }
}

const handleTitleInput = async (val) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (val.length < 5) {
    showAIPanel.value = false
    resetAIData()
    return
  }
  debounceTimer = setTimeout(async () => {
    aiLoading.value = true
    showAIPanel.value = true
    resetAIData()
    try {
      const res = await analyzeTitle(val)
      const data = res.data
      if (data) {
        aiData.categoryPath = data.categoryPath || ''
        aiData.ticketType = data.ticketType || ''
        aiData.urgency = data.urgency || ''
        aiData.department = data.department || ''
        aiData.confidence = data.confidence || 0
        aiData.problemAnalysis = data.problemAnalysis || ''
        aiData.suggestedActions = data.suggestedActions || ''
        aiData.solution = data.solution || ''
        
        if (data.similarTickets && Array.isArray(data.similarTickets)) {
          similarTickets.value = data.similarTickets.map(t => ({
            id: t.id,
            ticketNo: t.ticketNo,
            title: t.title,
            similarity: t.similarity || 0,
            type: t.type
          }))
        }
        
        if (data.templateFields && Array.isArray(data.templateFields)) {
          templateFields.value = data.templateFields.map(f => ({
            fieldName: f.fieldName,
            placeholder: f.placeholder,
            required: f.required || false
          }))
          templateValues.value = new Array(templateFields.value.length).fill('')
        }
      }
    } catch (error) {
      console.error('AI分析失败:', error)
      ElMessage.warning('AI分析服务暂不可用')
    } finally {
      aiLoading.value = false
      if (templateFields.value.length > 0) {
        fillTemplateToEditor()
      }
    }
  }, 500)
}

const resetAIData = () => {
  aiData.categoryPath = ''
  aiData.ticketType = ''
  aiData.urgency = ''
  aiData.department = ''
  aiData.confidence = 0
  aiData.solution = ''
  aiData.problemAnalysis = ''
  aiData.suggestedActions = ''
  similarTickets.value = []
  templateFields.value = []
  templateValues.value = []
}

const applyAll = () => {
  if (aiData.ticketType) form.type = aiData.ticketType
  if (aiData.urgency) form.urgency = aiData.urgency
  if (aiData.department) form.department = aiData.department
  if (templateFields.value.length > 0) {
    fillTemplateToEditor()
  } else if (!form.description) {
    form.description = generateDefaultDescription()
  }
  ElMessage.success('已填充AI推荐的工单信息')
}

const generateDefaultDescription = () => {
  let description = '<p><strong>问题描述</strong></p>'
  description += `<p>问题类型：${aiData.ticketType || '未知'}</p>`
  description += `<p>问题标题：${form.title}</p>`
  description += '<p>问题详情：请详细描述问题发生的时间、经过和具体表现...</p>'
  description += '<p>期望结果：请描述您期望的解决方案...</p>'
  return description
}

const fillTemplateToEditor = () => {
  if (templateFields.value.length === 0) return
  let description = '<p><strong>AI智能分析生成的问题模版</strong></p>'
  description += '<p>请根据以下提示填写对应信息：</p>'
  description += '<p> </p>'
  templateFields.value.forEach((field) => {
    const requiredTag = field.required ? '<span style="color:#ef4444">*</span>' : '<span style="color:#6b7280">(选填)</span>'
    description += `<p><strong>${field.fieldName}：</strong>${requiredTag}</p>`
    description += `<p><span style="color:#9ca3af">${field.placeholder}</span></p>`
    description += '<p> </p>'
  })
  form.description = description
}

const resetAI = () => {
  resetAIData()
  if (form.title.length >= 5) {
    handleTitleInput(form.title)
  } else {
    showAIPanel.value = false
  }
}

const viewSimilarTicket = (ticket) => {
  saveFormData()
  router.push({ name: 'TicketDetail', params: { id: ticket.id } })
}

const getTicketTypeTag = (type) => {
  const typeMap = {
    '投诉': 'danger',
    '咨询': 'primary',
    '办理': 'success',
    '故障': 'warning'
  }
  return typeMap[type] || 'info'
}

const handleRemove = (file) => {
  const index = fileList.value.indexOf(file)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

const handlePreview = (file) => {
  console.log(file)
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

.ai-chat-panel {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px;
  margin-top: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.ai-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.ai-info {
  flex: 1;
}

.ai-name {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.ai-status {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.loading-dots::after {
  content: '.';
  animation: dots 1.5s steps(5, end) infinite;
}

@keyframes dots {
  0%, 20% { content: '.'; }
  40% { content: '..'; }
  60% { content: '...'; }
  80%, 100% { content: ''; }
}

.completed {
  color: #10b981;
}

.ai-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-section {
  display: flex;
  gap: 12px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
}

.section-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0fdf4 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.section-content {
  flex: 1;
  min-width: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 8px;
}

.section-body {
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #94a3b8;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.analysis-text .paragraph {
  margin-bottom: 12px;
  text-align: justify;
}

.solution-text pre {
  margin: 0;
  font-family: inherit;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.actions-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.similar-tickets-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.similar-ticket-item {
  padding: 10px;
  background: #ffffff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e2e8f0;
}

.similar-ticket-item:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.ticket-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.ticket-no {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
}

.similarity-badge {
  margin-left: auto;
  font-size: 11px;
  color: #059669;
  font-weight: 600;
}

.ticket-title {
  font-size: 13px;
  color: #334155;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.empty-text {
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  padding: 16px 0;
}

.ai-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 8px;
}

.ai-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #e2e8f0;
}

.ai-actions .el-button {
  padding: 10px 28px;
  font-size: 14px;
  font-weight: 500;
}

.editor-wrapper {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.ql-container) {
  font-size: 14px;
}

:deep(.ql-toolbar) {
  border: none;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

:deep(.ql-editor) {
  min-height: 150px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.ai-chat-panel {
  animation: fadeIn 0.3s ease;
}
</style>
