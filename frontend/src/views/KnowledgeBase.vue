<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppLayout from '@/components/AppLayout.vue'
import {
  getKnowledgeKeywords,
  saveKnowledgeKeyword,
  deleteKnowledgeKeyword,
  getKnowledgeSolutions,
  saveKnowledgeSolution,
  deleteKnowledgeSolution
} from '@/api/ticket'

const activeTab = ref('keywords')

const keywords = ref([])
const keywordDialogVisible = ref(false)
const keywordForm = ref({ id: null, category: '', keywordName: '', keywords: '', description: '', sortOrder: 1 })
const keywordSearch = ref('')
const keywordCategoryFilter = ref('')
const keywordCategories = [
  { label: '全部', value: '' },
  { label: '工单类型', value: 'TICKET_TYPE' },
  { label: '紧急程度', value: 'URGENCY' },
  { label: '处理部门', value: 'DEPARTMENT' },
  { label: '来源渠道', value: 'CHANNEL' },
  { label: '关联系统', value: 'SYSTEM' }
]

const solutions = ref([])
const solutionDialogVisible = ref(false)
const solutionForm = ref({ id: null, scenario: '', solution: '', processingTime: '', category: '', description: '', sortOrder: 1 })
const solutionSearch = ref('')
const solutionCategoryFilter = ref('')
const solutionCategories = [
  { label: '全部', value: '' },
  { label: '转账汇款', value: 'TRANSFER' },
  { label: '账户管理', value: 'ACCOUNT' },
  { label: '业务咨询', value: 'CONSULT' },
  { label: '科技支持', value: 'TECH' },
  { label: '信用卡', value: 'CREDIT' },
  { label: '贷款', value: 'LOAN' },
  { label: '理财', value: 'WEALTH' },
  { label: '增值服务', value: 'VALUE_ADDED' },
  { label: '商户收单', value: 'MERCHANT' },
  { label: '外汇', value: 'FOREX' },
  { label: '默认', value: 'DEFAULT' }
]

const filteredKeywords = computed(() => {
  return keywords.value.filter(item => {
    const matchSearch = !keywordSearch.value || 
      item.keywordName.includes(keywordSearch.value) || 
      item.keywords.includes(keywordSearch.value) ||
      item.description.includes(keywordSearch.value)
    const matchCategory = !keywordCategoryFilter.value || item.category === keywordCategoryFilter.value
    return matchSearch && matchCategory
  })
})

const filteredSolutions = computed(() => {
  return solutions.value.filter(item => {
    const matchSearch = !solutionSearch.value || 
      item.scenario.includes(solutionSearch.value) || 
      item.solution.includes(solutionSearch.value) ||
      item.description.includes(solutionSearch.value)
    const matchCategory = !solutionCategoryFilter.value || item.category === solutionCategoryFilter.value
    return matchSearch && matchCategory
  })
})

const loadKeywords = async () => {
  try {
    const res = await getKnowledgeKeywords()
    keywords.value = res.data || []
  } catch (error) {
    ElMessage.error('加载关键词失败')
  }
}

const loadSolutions = async () => {
  try {
    const res = await getKnowledgeSolutions()
    solutions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载解决方案失败')
  }
}

const openKeywordDialog = (row) => {
  if (row) {
    keywordForm.value = { ...row }
  } else {
    keywordForm.value = { id: null, category: '', keywordName: '', keywords: '', description: '', sortOrder: keywords.value.length + 1 }
  }
  keywordDialogVisible.value = true
}

const submitKeyword = async () => {
  try {
    await saveKnowledgeKeyword(keywordForm.value)
    ElMessage.success('保存成功')
    keywordDialogVisible.value = false
    loadKeywords()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeKeyword = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该关键词分类？', '提示', { type: 'warning' })
    await deleteKeyword(id)
    ElMessage.success('删除成功')
    loadKeywords()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const openSolutionDialog = (row) => {
  if (row) {
    solutionForm.value = { ...row }
  } else {
    solutionForm.value = { id: null, scenario: '', solution: '', processingTime: '', category: '', description: '', sortOrder: solutions.value.length + 1 }
  }
  solutionDialogVisible.value = true
}

const submitSolution = async () => {
  try {
    await saveKnowledgeSolution(solutionForm.value)
    ElMessage.success('保存成功')
    solutionDialogVisible.value = false
    loadSolutions()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const removeSolution = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该解决方案？', '提示', { type: 'warning' })
    await deleteSolution(id)
    ElMessage.success('删除成功')
    loadSolutions()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const getCategoryLabel = (val) => {
  const cat = keywordCategories.find(c => c.value === val)
  return cat ? cat.label : val
}

const getSolutionCategoryLabel = (val) => {
  const cat = solutionCategories.find(c => c.value === val)
  return cat ? cat.label : val
}

onMounted(() => {
  loadKeywords()
  loadSolutions()
})
</script>

<template>
  <AppLayout>
    <div class="page-header">
      <h2 class="page-title">AI知识库管理</h2>
      <p class="page-subtitle">维护AI智能分析的关键词分类和解决方案模板</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="关键词分类" name="keywords">
        <div class="toolbar">
          <div class="search-bar">
            <el-input v-model="keywordSearch" placeholder="搜索关键词名称、触发词、描述" style="width: 300px" clearable>
              <template #prefix>
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
              </template>
            </el-input>
            <el-select v-model="keywordCategoryFilter" placeholder="筛选分类" style="width: 150px" clearable>
              <el-option v-for="cat in keywordCategories" :key="cat.value" :label="cat.label" :value="cat.value" />
            </el-select>
          </div>
          <el-button type="primary" @click="openKeywordDialog()">+ 新增关键词分类</el-button>
        </div>
        <el-table :data="filteredKeywords" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="{ row }">
              <el-tag>{{ getCategoryLabel(row.category) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="keywordName" label="分类名称" width="150" />
          <el-table-column prop="keywords" label="触发关键词">
            <template #default="{ row }">
              <el-tag v-for="kw in row.keywords.split(',')" :key="kw" size="small" style="margin: 2px">{{ kw }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="openKeywordDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeKeyword(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="filteredKeywords.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="#cbd5e1"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <p>未找到匹配的关键词分类</p>
        </div>
      </el-tab-pane>

      <el-tab-pane label="解决方案模板" name="solutions">
        <div class="toolbar">
          <div class="search-bar">
            <el-input v-model="solutionSearch" placeholder="搜索场景名称、解决方案、描述" style="width: 300px" clearable>
              <template #prefix>
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
              </template>
            </el-input>
            <el-select v-model="solutionCategoryFilter" placeholder="筛选业务分类" style="width: 150px" clearable>
              <el-option v-for="cat in solutionCategories" :key="cat.value" :label="cat.label" :value="cat.value" />
            </el-select>
          </div>
          <el-button type="primary" @click="openSolutionDialog()">+ 新增解决方案</el-button>
        </div>
        <el-table :data="filteredSolutions" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="scenario" label="场景名称" width="150" />
          <el-table-column prop="category" label="业务分类" width="120">
            <template #default="{ row }">
              <el-tag type="success">{{ getSolutionCategoryLabel(row.category) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="solution" label="解决方案">
            <template #default="{ row }">
              <pre style="margin: 0; white-space: pre-wrap; font-family: inherit; font-size: 13px;">{{ row.solution }}</pre>
            </template>
          </el-table-column>
          <el-table-column prop="processingTime" label="处理时效" width="120" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="openSolutionDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeSolution(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="filteredSolutions.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="#cbd5e1"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <p>未找到匹配的解决方案</p>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 关键词编辑弹窗 -->
    <el-dialog v-model="keywordDialogVisible" :title="keywordForm.id ? '编辑关键词分类' : '新增关键词分类'" width="600px">
      <el-form :model="keywordForm" label-width="100px">
        <el-form-item label="所属分类">
          <el-select v-model="keywordForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="cat in keywordCategories.slice(1)" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="keywordForm.keywordName" placeholder="如：转账失败" />
        </el-form-item>
        <el-form-item label="触发关键词">
          <el-input v-model="keywordForm.keywords" type="textarea" :rows="3" placeholder="多个关键词用英文逗号分隔，如：转账,汇款,失败,超时" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="keywordForm.description" placeholder="可选填" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="keywordForm.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="keywordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitKeyword">保存</el-button>
      </template>
    </el-dialog>

    <!-- 解决方案编辑弹窗 -->
    <el-dialog v-model="solutionDialogVisible" :title="solutionForm.id ? '编辑解决方案' : '新增解决方案'" width="700px">
      <el-form :model="solutionForm" label-width="100px">
        <el-form-item label="场景名称">
          <el-input v-model="solutionForm.scenario" placeholder="如：转账失败" />
        </el-form-item>
        <el-form-item label="业务分类">
          <el-select v-model="solutionForm.category" placeholder="请选择业务分类" style="width: 100%">
            <el-option v-for="cat in solutionCategories.slice(1)" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="解决方案">
          <el-input v-model="solutionForm.solution" type="textarea" :rows="5" placeholder="每行一个步骤，如：
1. 查询核心系统交易状态
2. 如已扣款未到账，发起资金调账
3. 预计处理时效：2小时内" />
        </el-form-item>
        <el-form-item label="处理时效">
          <el-input v-model="solutionForm.processingTime" placeholder="如：2小时内 / 1-3个工作日" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="solutionForm.description" placeholder="可选填" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="solutionForm.sortOrder" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="solutionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSolution">保存</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}
.page-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.search-bar {
  display: flex;
  gap: 10px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #94a3b8;
}
.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}
</style>
