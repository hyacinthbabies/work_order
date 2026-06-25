<template>
  <AppLayout>
    <div style="margin-bottom:20px">
      <h2 style="font-size:20px;font-weight:800">🔐 权限与脱敏管理</h2>
      <p style="font-size:13px;color:#6b7280;margin-top:4px">管理角色权限、数据脱敏规则和字段访问控制</p>
    </div>

    <div class="tabs">
      <div class="tab" :class="{ active: activeTab === 'roles' }" @click="activeTab = 'roles'">角色权限配置</div>
      <div class="tab" :class="{ active: activeTab === 'mask' }" @click="activeTab = 'mask'">脱敏规则管理</div>
      <div class="tab" :class="{ active: activeTab === 'field' }" @click="activeTab = 'field'">字段权限控制</div>
    </div>

    <div v-show="activeTab === 'roles'">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>角色权限矩阵</h3>
            <el-button type="primary" size="small">+ 新增角色</el-button>
          </div>
        </template>
        <el-table :data="roleData" border class="perm-matrix">
          <el-table-column prop="role" label="角色" width="120" />
          <el-table-column prop="create" label="创建工单" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.create)">{{ getPermSymbol(row.create) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="process" label="处理工单" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.process)">{{ getPermSymbol(row.process) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="approve" label="审批工单" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.approve)">{{ getPermSymbol(row.approve) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="delete" label="删除工单" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.delete)">{{ getPermSymbol(row.delete) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="report" label="查看报表" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.report)">{{ getPermSymbol(row.report) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="export" label="导出数据" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.export)">{{ getPermSymbol(row.export) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="config" label="系统配置" width="80" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.config)">{{ getPermSymbol(row.config) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div v-show="activeTab === 'mask'">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>敏感数据脱敏规则</h3>
            <el-button type="primary" size="small">+ 新增规则</el-button>
          </div>
        </template>
        <el-table :data="maskData" stripe>
          <el-table-column prop="type" label="数据类型" />
          <el-table-column prop="original" label="脱敏前示例" />
          <el-table-column prop="masked" label="脱敏后展示">
            <template #default="{ row }">
              <span class="masked">{{ row.masked }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="rule" label="脱敏规则" />
          <el-table-column prop="roles" label="适用角色" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div v-show="activeTab === 'field'">
      <el-card>
        <template #header>
          <h3>字段级权限控制</h3>
        </template>
        <el-table :data="fieldData" border class="perm-matrix">
          <el-table-column prop="field" label="字段名称" width="120" />
          <el-table-column prop="cs" label="一线客服" width="100" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.cs.type)">{{ row.cs.label }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="css" label="客服主管" width="100" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.css.type)">{{ row.css.label }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="qa" label="质检员" width="100" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.qa.type)">{{ row.qa.label }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="rc" label="风控专员" width="100" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.rc.type)">{{ row.rc.label }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sa" label="系统管理员" width="100" align="center">
            <template #default="{ row }">
              <span :class="getPermClass(row.sa.type)">{{ row.sa.label }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref } from 'vue'
import AppLayout from '@/components/AppLayout.vue'

const activeTab = ref('roles')

const roleData = ref([
  { role: '一线客服', create: 'full', process: 'full', approve: 'none', delete: 'none', report: 'partial', export: 'none', config: 'none' },
  { role: '客服主管', create: 'full', process: 'full', approve: 'full', delete: 'none', report: 'full', export: 'partial', config: 'none' },
  { role: '质检员', create: 'none', process: 'partial', approve: 'none', delete: 'none', report: 'full', export: 'full', config: 'none' },
  { role: '风控专员', create: 'full', process: 'full', approve: 'partial', delete: 'none', report: 'full', export: 'full', config: 'none' },
  { role: '系统管理员', create: 'full', process: 'full', approve: 'full', delete: 'full', report: 'full', export: 'full', config: 'full' }
])

const maskData = ref([
  { type: '客户姓名', original: '张三', masked: '张*', rule: '保留姓氏，其余用*替代', roles: '一线客服、质检员', status: '启用' },
  { type: '身份证号', original: '110101199001011234', masked: '110101**********1234', rule: '保留前6位和后4位', roles: '所有角色（除系统管理员）', status: '启用' },
  { type: '手机号码', original: '13812345678', masked: '138****5678', rule: '保留前3位和后4位', roles: '一线客服、质检员', status: '启用' },
  { type: '银行卡号', original: '6222021234567890123', masked: '6222***********0123', rule: '保留前4位和后4位', roles: '所有角色（除系统管理员）', status: '启用' },
  { type: '账户余额', original: '¥ 125,680.50', masked: '¥ ****.**', rule: '完全隐藏金额', roles: '一线客服', status: '启用' },
  { type: '详细地址', original: '北京市朝阳区xxx路xxx号', masked: '北京市朝阳区***', rule: '保留到区级，其余用*替代', roles: '一线客服', status: '启用' }
])

const fieldData = ref([
  { field: '客户姓名', cs: { type: 'partial', label: '脱敏' }, css: { type: 'full', label: '完整' }, qa: { type: 'partial', label: '脱敏' }, rc: { type: 'full', label: '完整' }, sa: { type: 'full', label: '完整' } },
  { field: '身份证号', cs: { type: 'none', label: '不可见' }, css: { type: 'partial', label: '脱敏' }, qa: { type: 'none', label: '不可见' }, rc: { type: 'partial', label: '脱敏' }, sa: { type: 'full', label: '完整' } },
  { field: '银行卡号', cs: { type: 'partial', label: '脱敏' }, css: { type: 'partial', label: '脱敏' }, qa: { type: 'partial', label: '脱敏' }, rc: { type: 'full', label: '完整' }, sa: { type: 'full', label: '完整' } },
  { field: '账户余额', cs: { type: 'none', label: '不可见' }, css: { type: 'full', label: '完整' }, qa: { type: 'none', label: '不可见' }, rc: { type: 'full', label: '完整' }, sa: { type: 'full', label: '完整' } },
  { field: '手机号码', cs: { type: 'partial', label: '脱敏' }, css: { type: 'full', label: '完整' }, qa: { type: 'partial', label: '脱敏' }, rc: { type: 'full', label: '完整' }, sa: { type: 'full', label: '完整' } },
  { field: '详细地址', cs: { type: 'partial', label: '脱敏' }, css: { type: 'full', label: '完整' }, qa: { type: 'partial', label: '脱敏' }, rc: { type: 'full', label: '完整' }, sa: { type: 'full', label: '完整' } },
  { field: '交易金额', cs: { type: 'partial', label: '脱敏' }, css: { type: 'full', label: '完整' }, qa: { type: 'full', label: '完整' }, rc: { type: 'full', label: '完整' }, sa: { type: 'full', label: '完整' } }
])

const getPermClass = (type) => {
  const classes = {
    full: 'perm-check',
    partial: 'perm-partial',
    none: 'perm-cross'
  }
  return classes[type] || 'perm-cross'
}

const getPermSymbol = (type) => {
  const symbols = {
    full: '✓',
    partial: '◐',
    none: '✗'
  }
  return symbols[type] || '✗'
}
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #e5e7eb;
  margin-bottom: 20px;
}

.tab {
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all .15s;
}

.tab:hover {
  color: #374151;
}

.tab.active {
  color: #1a56db;
  border-bottom-color: #1a56db;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.perm-matrix th, .perm-matrix td {
  text-align: center;
}

.perm-matrix th:first-child, .perm-matrix td:first-child {
  text-align: left;
}

.perm-check {
  color: #059669;
  font-weight: 700;
  font-size: 16px;
}

.perm-cross {
  color: #d1d5db;
  font-size: 16px;
}

.perm-partial {
  color: #d97706;
  font-weight: 700;
  font-size: 16px;
}

.masked {
  color: #9ca3af;
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
  letter-spacing: 1px;
}
</style>