<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchHomeOverview } from '@/api'
import StatCard from '@/components/StatCard.vue'
import ReviewQueueCard from '@/components/ReviewQueueCard.vue'
import DistributionBars from '@/components/DistributionBars.vue'
import RecentMistakes from '@/components/RecentMistakes.vue'

const loading = ref(true)
const error = ref('')
const overview = ref(null)

const stats = computed(() => overview.value?.stats ?? {})
const profile = computed(() => overview.value?.profile ?? null)
const reviewQueue = computed(() => overview.value?.reviewQueue ?? [])
const subjectDistribution = computed(
  () => overview.value?.subjectDistribution ?? []
)
const reasonDistribution = computed(
  () => overview.value?.reasonDistribution ?? []
)
const recentMistakes = computed(() => overview.value?.recentMistakes ?? [])
const totalCount = computed(() => stats.value.totalCount ?? 0)

async function loadOverview() {
  loading.value = true
  error.value = ''
  try {
    overview.value = await fetchHomeOverview()
  } catch (err) {
    error.value = err?.message || '数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<template>
  <div class="home">
    <!-- 首屏：一句话说清“为谁、解决什么” + 今日待复习 -->
    <section class="hero">
      <div class="container hero__inner">
        <div class="hero__copy">
          <p class="eyebrow">面向大学生的错题复习系统</p>
          <h1 class="hero__title">
            错过的题<br />
            只错一次。
          </h1>
          <p class="hero__lead">
            把试卷、练习册和网课里的错题录进来：按学科和知识点归档，写清错在哪一步，到期提醒你重做一遍。期末复习时，不用再翻那本抄得自己都看不懂的错题本。
          </p>

          <div class="hero__actions">
            <RouterLink to="/library/new" class="btn btn--primary btn--lg">
              开始整理错题
            </RouterLink>
            <RouterLink to="/review" class="btn btn--ghost btn--lg">
              <template v-if="stats.dueTodayCount">
                复习 {{ stats.dueTodayCount }} 道到期题
              </template>
              <template v-else>看看今天要复习什么</template>
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path d="M5 12h14" />
                <path d="m12 5 7 7-7 7" />
              </svg>
            </RouterLink>
          </div>

          <ul class="hero__meta">
            <li>文字、拍照都能录</li>
            <li>按学科 · 知识点归类</li>
            <li>到期提醒，不用自己记</li>
          </ul>
        </div>

        <div class="hero__aside">
          <ReviewQueueCard
            v-if="!loading && !error"
            :items="reviewQueue"
            :due-count="stats.dueTodayCount ?? 0"
            :overdue-count="stats.overdueCount ?? 0"
          />

          <div v-else-if="loading" class="card queue-skeleton" aria-hidden="true">
            <div class="skeleton" style="width: 40%; height: 15px"></div>
            <div class="skeleton" style="width: 68%; height: 12px"></div>
            <div class="queue-skeleton__rows">
              <div v-for="i in 3" :key="i" class="queue-skeleton__row">
                <div class="skeleton" style="width: 26%; height: 11px"></div>
                <div class="skeleton" style="width: 100%; height: 13px"></div>
                <div class="skeleton" style="width: 74%; height: 13px"></div>
              </div>
            </div>
            <div class="skeleton" style="width: 100%; height: 40px; margin-top: 16px"></div>
          </div>

          <div v-else class="card queue-error">
            <p class="queue-error__title">待复习数据没加载出来</p>
            <p class="queue-error__desc">{{ error }}</p>
            <button class="btn btn--ghost" @click="loadOverview">重新加载</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 概览：数字、分布、最近录入 -->
    <section class="container overview" aria-label="我的错题概览">
      <div class="overview__head">
        <h2 class="overview__title">错题概览</h2>
        <p class="overview__note">
          <template v-if="profile">{{ profile.nickname }} · {{ profile.goal }}</template>
        </p>
      </div>

      <div v-if="loading" class="stat-grid" aria-hidden="true">
        <div v-for="i in 4" :key="i" class="stat-skeleton card">
          <div class="skeleton" style="width: 48%; height: 11px"></div>
          <div class="skeleton" style="width: 62%; height: 25px; margin-top: 12px"></div>
          <div class="skeleton" style="width: 42%; height: 10px; margin-top: 10px"></div>
        </div>
      </div>

      <div v-else-if="!error" class="stat-grid">
        <StatCard
          label="错题总数"
          :value="stats.totalCount ?? 0"
          unit="道"
          :note="`+${stats.newThisWeekCount ?? 0} 本周`"
          hint="累计录入"
        />
        <StatCard
          label="今日待复习"
          :value="stats.dueTodayCount ?? 0"
          unit="道"
          :note="stats.overdueCount ? `${stats.overdueCount} 道已逾期` : '没有逾期'"
          :tone="stats.overdueCount ? 'warn' : 'good'"
          hint="按到期日期"
        />
        <StatCard
          label="已掌握"
          :value="stats.masteredCount ?? 0"
          unit="道"
          :note="`${stats.masteredRate ?? 0}%`"
          tone="good"
          hint="占全部错题"
        />
        <StatCard
          label="本周新增"
          :value="stats.newThisWeekCount ?? 0"
          unit="道"
          :note="`较上周 +${stats.newGrowth ?? 0}`"
          :tone="(stats.newGrowth ?? 0) > 0 ? 'warn' : 'good'"
          hint="新录入的错题"
        />
      </div>

      <template v-if="!loading && !error">
        <div class="split">
          <DistributionBars
            title="学科分布"
            :subtitle="`共 ${totalCount} 道错题，按学科归档`"
            :items="subjectDistribution"
            unit="道"
          />
          <DistributionBars
            title="错误原因"
            subtitle="每道题按当前主要错误原因统计"
            :items="reasonDistribution"
            variant="reason"
          />
        </div>

        <RecentMistakes :items="recentMistakes" />
      </template>
    </section>
  </div>
</template>

<style scoped>
/* ---------- Hero ---------- */
.hero {
  padding: 64px 0 44px;
}

.hero__inner {
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(0, 0.98fr);
  gap: 56px;
  align-items: start;
}

.hero__copy {
  padding-top: 8px;
  animation: rise 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.hero__title {
  margin-top: 14px;
  font-size: clamp(34px, 4.6vw, 54px);
  font-weight: 600;
  line-height: 1.16;
  letter-spacing: -0.035em;
  color: var(--ink);
}

.hero__lead {
  max-width: 44ch;
  margin-top: 20px;
  font-size: 15px;
  line-height: 1.85;
  color: var(--ink-2);
}

.hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 28px;
}

.hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 22px;
  margin-top: 26px;
  font-size: 12.5px;
  color: var(--ink-3);
}

.hero__meta li {
  position: relative;
  padding-left: 14px;
}

.hero__meta li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0.62em;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--line-strong);
}

.hero__aside {
  padding-top: 20px;
  animation: rise 0.5s 0.06s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

/* ---------- 队列骨架 / 错误态 ---------- */
.queue-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 24px 22px;
}

.queue-skeleton__rows {
  margin-top: 6px;
}

.queue-skeleton__row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 0;
  border-top: 1px solid var(--line);
}

.queue-error {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 26px 22px;
}

.queue-error__title {
  font-size: 14px;
  font-weight: 600;
}

.queue-error__desc {
  font-size: 12.5px;
  color: var(--ink-3);
  margin-bottom: 6px;
}

/* ---------- 概览 ---------- */
.overview {
  padding-top: 8px;
}

.overview__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.overview__title {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.overview__note {
  font-size: 12px;
  color: var(--ink-4);
  text-align: right;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.stat-skeleton {
  display: flex;
  flex-direction: column;
  padding: 18px 20px 20px;
  border-radius: var(--radius);
}

.split {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 0.92fr);
  gap: 16px;
  margin-top: 16px;
}

/* ---------- 响应式 ---------- */
@media (max-width: 980px) {
  .hero__inner {
    grid-template-columns: 1fr;
    gap: 36px;
  }

  .hero {
    padding-top: 48px;
  }

  .hero__lead {
    max-width: 56ch;
  }

  .hero__aside {
    max-width: 560px;
  }

  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .split {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: 36px 0 32px;
  }

  .hero__title {
    margin-top: 10px;
  }

  .hero__lead {
    margin-top: 16px;
    font-size: 14.5px;
  }

  .hero__actions .btn {
    flex: 1 1 auto;
  }

  .overview__head {
    flex-direction: column;
    align-items: flex-start;
    gap: 3px;
  }

  .overview__note {
    text-align: left;
  }

  .stat-grid {
    gap: 10px;
  }
}
</style>
