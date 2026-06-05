<template>
  <div class="birthday-profile-container">
    <a-spin :spinning="loading">
      <div class="stat-cards">
        <div class="stat-card card-gift">
          <div class="stat-icon">🎁</div>
          <div class="stat-info">
            <div class="stat-value">{{ profile.totalWishes }}</div>
            <div class="stat-label">历年收到祝福</div>
          </div>
        </div>
        <div class="stat-card card-party">
          <div class="stat-icon">🎉</div>
          <div class="stat-info">
            <div class="stat-value">{{ profile.totalParties }}</div>
            <div class="stat-label">参加生日会</div>
          </div>
        </div>
        <div class="stat-card card-star">
          <div class="stat-icon">⭐</div>
          <div class="stat-info">
            <div class="stat-value">{{ profile.yearWishes ? profile.yearWishes.length : 0 }}</div>
            <div class="stat-label">今年收到祝福</div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-header">
          <span class="section-title">💌 今年收到的祝福</span>
          <span class="section-count">{{ profile.yearWishes ? profile.yearWishes.length : 0 }} 条</span>
        </div>
        <div v-if="!profile.yearWishes || profile.yearWishes.length === 0" class="empty-wrapper">
          <a-empty description="今年还没有收到祝福哦" />
        </div>
        <div v-else class="wish-list">
          <div v-for="wish in profile.yearWishes" :key="wish.id" class="wish-item">
            <div class="wish-avatar">
              <a-avatar v-if="wish.senderAvatar" :src="getAvatarUrl(wish.senderAvatar)" :size="44" />
              <a-avatar v-else :size="44" :style="{ backgroundColor: getAvatarColor(wish.senderName) }">
                {{ wish.senderName ? wish.senderName.charAt(0) : '?' }}
              </a-avatar>
            </div>
            <div class="wish-body">
              <div class="wish-header">
                <span class="wish-sender">{{ wish.senderName }}</span>
                <a-tag v-if="wish.isSystem" color="blue" size="small" class="system-tag">系统</a-tag>
                <span class="wish-time">{{ formatDateTime(wish.createTime) }}</span>
              </div>
              <div class="wish-content">{{ wish.content }}</div>
              <div class="wish-footer">
                <span class="wish-likes">
                  <HeartOutlined :style="{ color: wish.isLiked ? '#ff4d4f' : '#999' }" />
                  <span :class="{ 'liked': wish.isLiked }">{{ wish.likeCount || 0 }}</span>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-header">
          <span class="section-title">🎂 参加过的生日会</span>
          <span class="section-count">{{ profile.parties ? profile.parties.length : 0 }} 场</span>
        </div>
        <div v-if="!profile.parties || profile.parties.length === 0" class="empty-wrapper">
          <a-empty description="还没有参加过生日会" />
        </div>
        <div v-else class="party-list">
          <div
            v-for="party in profile.parties"
            :key="party.id"
            class="party-card"
            @click="goToPartyDetail(party.id)"
          >
            <div class="party-theme">{{ party.theme }}</div>
            <div class="party-meta">
              <div class="party-meta-item">
                <CalendarOutlined />
                <span>{{ formatDateTime(party.eventTime) }}</span>
              </div>
              <div class="party-meta-item">
                <EnvironmentOutlined />
                <span>{{ party.location || '-' }}</span>
              </div>
            </div>
            <div class="party-footer">
              <a-tag :color="getPartyStatusColor(party.status)">{{ getPartyStatusText(party.status) }}</a-tag>
              <RightOutlined class="party-arrow" />
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { HeartOutlined, CalendarOutlined, EnvironmentOutlined, RightOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getProfile } from '@/api/birthday'

const router = useRouter()
const loading = ref(false)
const profile = ref({
  totalWishes: 0,
  totalParties: 0,
  yearWishes: [],
  parties: []
})

const avatarColors = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#eb2f96', '#52c41a', '#1890ff', '#fa541c']

const getAvatarColor = (name) => {
  if (!name) return '#1890ff'
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return avatarColors[Math.abs(hash) % avatarColors.length]
}

const getAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const getPartyStatusColor = (status) => {
  const map = {
    0: 'default',
    1: 'processing',
    2: 'success',
    3: 'warning'
  }
  return map[status] ?? 'default'
}

const getPartyStatusText = (status) => {
  const map = {
    0: '未开始',
    1: '进行中',
    2: '已完成',
    3: '已取消'
  }
  return map[status] ?? '未知'
}

const goToPartyDetail = (id) => {
  router.push(`/emp-birthday-party-detail/${id}`)
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data || { totalWishes: 0, totalParties: 0, yearWishes: [], parties: [] }
  } catch (error) {
    console.error('获取生日档案失败:', error)
    message.error('获取生日档案失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.birthday-profile-container {
  max-width: 960px;
  margin: 0 auto;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  border-radius: 16px;
  padding: 28px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-gift {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
}

.card-party {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.card-star {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
}

.stat-icon {
  font-size: 40px;
  line-height: 1;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-top: 4px;
}

.section {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.section-count {
  font-size: 13px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 10px;
  border-radius: 10px;
}

.empty-wrapper {
  padding: 40px 0;
}

.wish-list {
  display: flex;
  flex-direction: column;
}

.wish-item {
  display: flex;
  gap: 16px;
  padding: 18px 0;
  border-bottom: 1px solid #f5f5f5;
}

.wish-item:last-child {
  border-bottom: none;
}

.wish-avatar {
  flex-shrink: 0;
}

.wish-body {
  flex: 1;
  min-width: 0;
}

.wish-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.wish-sender {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.system-tag {
  font-size: 11px;
  line-height: 1;
  padding: 0 6px;
  margin: 0;
}

.wish-time {
  font-size: 12px;
  color: #bfbfbf;
  margin-left: auto;
}

.wish-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
  margin-bottom: 8px;
  word-break: break-word;
}

.wish-footer {
  display: flex;
  align-items: center;
}

.wish-likes {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  cursor: default;
}

.wish-likes .liked {
  color: #ff4d4f;
}

.party-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.party-card {
  background: linear-gradient(135deg, #fffbf0 0%, #fff5eb 100%);
  border: 1px solid #ffe7ba;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.party-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(250, 173, 20, 0.15);
  border-color: #ffd591;
}

.party-theme {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.party-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
}

.party-meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8c8c8c;
}

.party-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.party-arrow {
  color: #d9d9d9;
  font-size: 12px;
  transition: color 0.3s, transform 0.3s;
}

.party-card:hover .party-arrow {
  color: #fa8c16;
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 26px;
  }

  .party-list {
    grid-template-columns: 1fr;
  }

  .section {
    padding: 20px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .stat-cards {
    gap: 14px;
  }

  .stat-card {
    padding: 22px 18px;
  }

  .stat-value {
    font-size: 28px;
  }
}
</style>
