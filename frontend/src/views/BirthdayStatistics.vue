<template>
  <div class="birthday-statistics-container">
    <a-row :gutter="[16, 16]">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card-wish">
          <div class="stat-item">
            <div class="stat-icon">
              <HeartOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.yearWishCount ?? 0 }}</div>
              <div class="stat-label">今年祝福总数</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card-party">
          <div class="stat-item">
            <div class="stat-icon">
              <GiftOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.yearPartyCount ?? 0 }}</div>
              <div class="stat-label">今年生日会</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card-rate">
          <div class="stat-item">
            <div class="stat-icon">
              <TeamOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ participationRate }}</div>
              <div class="stat-label">平均参与率</div>
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card stat-card-dist">
          <div class="stat-item">
            <div class="stat-icon">
              <CalendarOutlined />
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalBirthdayEmployees }}</div>
              <div class="stat-label">生日员工分布</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :span="12">
        <a-card :bordered="false" class="chart-card">
          <template #title>生日月份分布</template>
          <div ref="barChartRef" class="chart"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card :bordered="false" class="chart-card">
          <template #title>部门生日分布</template>
          <div ref="pieChartRef" class="chart"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :span="12">
        <a-card :bordered="false" class="rank-card">
          <template #title>最活跃祝福者TOP10</template>
          <div class="rank-list">
            <div v-for="(item, index) in statistics.activeWishers" :key="item.id" class="rank-item">
              <span class="rank-number" :class="{ 'rank-top': index < 3 }">{{ index + 1 }}</span>
              <a-avatar :src="item.avatar" :size="36" class="rank-avatar">
                {{ item.name?.charAt(0) }}
              </a-avatar>
              <div class="rank-info">
                <div class="rank-name">{{ item.name }}</div>
                <a-progress :percent="wisherPercent(item.count)" :show-info="false" :stroke-color="warmColors[index % warmColors.length]" size="small" />
              </div>
              <div class="rank-count">{{ item.count }}次</div>
            </div>
            <a-empty v-if="!statistics.activeWishers?.length" description="暂无数据" />
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card :bordered="false" class="rank-card">
          <template #title>最受欢迎寿星TOP10</template>
          <div class="rank-list">
            <div v-for="(item, index) in statistics.popularStars" :key="item.id" class="rank-item">
              <span class="rank-number" :class="{ 'rank-top': index < 3 }">{{ index + 1 }}</span>
              <a-avatar :src="item.avatar" :size="36" class="rank-avatar">
                {{ item.name?.charAt(0) }}
              </a-avatar>
              <div class="rank-info">
                <div class="rank-name">{{ item.name }}</div>
                <a-progress :percent="starPercent(item.count)" :show-info="false" :stroke-color="warmColors[index % warmColors.length]" size="small" />
              </div>
              <div class="rank-count">{{ item.count }}次</div>
            </div>
            <a-empty v-if="!statistics.popularStars?.length" description="暂无数据" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :span="12">
        <a-card :bordered="false" class="export-card">
          <template #title>导出数据</template>
          <div class="export-actions">
            <div class="export-item">
              <span class="export-label">导出本月生日员工名单</span>
              <a-select v-model:value="exportMonth" style="width: 100px" :options="monthOptions" />
              <a-button type="primary" :loading="exportEmployeeLoading" @click="handleExportEmployees">
                <template #icon><DownloadOutlined /></template>
                导出
              </a-button>
            </div>
            <div class="export-item">
              <span class="export-label">导出祝福互动报表</span>
              <a-select v-model:value="exportYear" style="width: 100px" :options="yearOptions" />
              <a-button type="primary" :loading="exportWishLoading" @click="handleExportWishReport">
                <template #icon><DownloadOutlined /></template>
                导出
              </a-button>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import {
  HeartOutlined, GiftOutlined, TeamOutlined, CalendarOutlined, DownloadOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'
import { getStatistics, exportBirthdayEmployees, exportWishReport } from '@/api/birthday'

const warmColors = [
  '#FF6B6B', '#FF8E53', '#FFC857', '#4ECDC4', '#45B7D1',
  '#96CEB4', '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F',
  '#BB8FCE', '#85C1E9'
]

const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

const statistics = ref({
  birthdayDistribution: [],
  deptDistribution: [],
  yearWishCount: 0,
  activeWishers: [],
  popularStars: [],
  yearPartyCount: 0,
  avgParticipationRate: 0
})

const exportMonth = ref(new Date().getMonth() + 1)
const exportYear = ref(new Date().getFullYear())
const exportEmployeeLoading = ref(false)
const exportWishLoading = ref(false)

const monthOptions = Array.from({ length: 12 }, (_, i) => ({
  value: i + 1,
  label: `${i + 1}月`
}))

const yearOptions = Array.from({ length: 5 }, (_, i) => {
  const y = new Date().getFullYear() - i
  return { value: y, label: `${y}年` }
})

const totalBirthdayEmployees = computed(() =>
  statistics.value.birthdayDistribution?.reduce((sum, item) => sum + (item.count || 0), 0) || 0
)

const participationRate = computed(() => {
  const rate = statistics.value.avgParticipationRate
  if (rate == null) return '0%'
  return (rate * 100).toFixed(1) + '%'
})

const wisherPercent = (count) => {
  const max = Math.max(...(statistics.value.activeWishers?.map(i => i.count) || [1]), 1)
  return Math.round((count / max) * 100)
}

const starPercent = (count) => {
  const max = Math.max(...(statistics.value.popularStars?.map(i => i.count) || [1]), 1)
  return Math.round((count / max) * 100)
}

const fetchData = async () => {
  try {
    const res = await getStatistics()
    statistics.value = res.data || {}
    await nextTick()
    renderCharts()
  } catch (error) {
    console.error('获取生日统计数据失败:', error)
  }
}

const renderCharts = () => {
  renderBarChart()
  renderPieChart()
}

const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }

  const dist = statistics.value.birthdayDistribution || []
  const monthData = Array.from({ length: 12 }, (_, i) => {
    const found = dist.find(d => d.month === i + 1)
    return found ? found.count : 0
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 16,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 12 }, (_, i) => `${i + 1}月`),
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { type: 'dashed', color: '#e8e8e8' } }
    },
    series: [
      {
        name: '生日人数',
        type: 'bar',
        data: monthData,
        barWidth: 24,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: (params) => {
            return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: warmColors[params.dataIndex % warmColors.length] },
              { offset: 1, color: warmColors[params.dataIndex % warmColors.length] + '88' }
            ])
          }
        }
      }
    ]
  }

  barChart.setOption(option)
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  const deptData = (statistics.value.deptDistribution || [])
    .filter(d => d.count > 0)
    .map(d => ({ name: d.department, value: d.count }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    color: warmColors,
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: deptData
      }
    ]
  }

  pieChart.setOption(option)
}

const downloadBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

const handleExportEmployees = async () => {
  exportEmployeeLoading.value = true
  try {
    const res = await exportBirthdayEmployees(exportMonth.value)
    downloadBlob(res.data, `生日员工名单_${exportMonth.value}月.xlsx`)
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exportEmployeeLoading.value = false
  }
}

const handleExportWishReport = async () => {
  exportWishLoading.value = true
  try {
    const res = await exportWishReport(exportYear.value)
    downloadBlob(res.data, `祝福互动报表_${exportYear.value}年.xlsx`)
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  } finally {
    exportWishLoading.value = false
  }
}

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.birthday-statistics-container {
  padding: 0;
}

.stat-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.stat-card-wish .stat-icon {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.stat-card-party .stat-icon {
  background: linear-gradient(135deg, #FFC857 0%, #FF8E53 100%);
}

.stat-card-rate .stat-icon {
  background: linear-gradient(135deg, #4ECDC4 0%, #45B7D1 100%);
}

.stat-card-dist .stat-icon {
  background: linear-gradient(135deg, #DDA0DD 0%, #BB8FCE 100%);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.chart-card,
.rank-card,
.export-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.chart {
  width: 100%;
  height: 300px;
}

.rank-list {
  max-height: 420px;
  overflow-y: auto;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-number {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  flex-shrink: 0;
}

.rank-number.rank-top {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: #fff;
}

.rank-avatar {
  flex-shrink: 0;
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: 14px;
  color: #1f2937;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-count {
  font-size: 14px;
  font-weight: 600;
  color: #FF6B6B;
  flex-shrink: 0;
  min-width: 42px;
  text-align: right;
}

.export-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.export-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.export-label {
  font-size: 14px;
  color: #374151;
  min-width: 180px;
}
</style>
