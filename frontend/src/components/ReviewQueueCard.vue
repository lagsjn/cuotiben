<script setup>
import { RouterLink } from 'vue-router'
import StatusPill from './StatusPill.vue'

defineProps({
  items: { type: Array, default: () => [] },
  dueCount: { type: Number, default: 0 },
  overdueCount: { type: Number, default: 0 }
})
</script>

<template>
  <aside class="card queue">
    <div class="queue__head">
      <div class="queue__heading">
        <span class="queue__label">今日待复习</span>
        <span class="queue__count mono">{{ dueCount }}</span>
      </div>
      <p class="queue__sub">
        <template v-if="overdueCount">
          <span class="queue__overdue">{{ overdueCount }} 道已逾期</span>
          <span class="queue__dot">·</span>
        </template>
        先做题，再展开答案
      </p>
    </div>

    <ul v-if="items.length" class="queue__list">
      <li v-for="item in items" :key="item.id">
        <RouterLink
          :to="{ name: 'review', query: { id: item.id } }"
          class="queue__item"
        >
          <div class="queue__top">
            <span class="queue__subject">{{ item.subjectName }}</span>
            <span class="queue__due" :class="{ 'is-overdue': item.overdue }">
              <span class="queue__due-dot" aria-hidden="true"></span>
              {{ item.dueLabel }}
            </span>
          </div>
          <p class="queue__title">{{ item.title }}</p>
          <div class="queue__foot-row">
            <span class="queue__kp">{{ item.knowledgePointName }}</span>
            <StatusPill :status="item.masteryStatus" />
          </div>
        </RouterLink>
      </li>
    </ul>

    <div v-else class="queue__empty">
      <p class="queue__empty-title">今天没有到期错题</p>
      <p class="queue__empty-desc">可以去错题库挑几道没把握的题，加入待复习。</p>
    </div>

    <RouterLink to="/review" class="queue__cta">
      开始复习
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

    <p class="queue__tip">复习完可设置下次日期，或标记为已掌握</p>
  </aside>
</template>

<style scoped>
.queue {
  display: flex;
  flex-direction: column;
  padding: 22px 22px 18px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.queue__head {
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.queue__heading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.queue__label {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.queue__count {
  display: inline-grid;
  place-items: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 6px;
  background: var(--accent-soft);
  color: var(--accent);
  font-size: 12.5px;
  font-weight: 600;
}

.queue__sub {
  margin-top: 3px;
  font-size: 12.5px;
  color: var(--ink-3);
}

.queue__overdue {
  color: var(--amber);
}

.queue__dot {
  margin: 0 5px;
  color: var(--ink-4);
}

.queue__list {
  display: flex;
  flex-direction: column;
}

.queue__item {
  display: block;
  padding: 14px 10px;
  margin: 0 -10px;
  border-radius: 10px;
  transition: background-color 0.16s ease;
}

.queue__item:hover {
  background: var(--surface-2);
}

.queue__list li + li .queue__item {
  border-top: 1px solid var(--line);
  border-radius: 0;
}

.queue__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 5px;
}

.queue__subject {
  font-size: 11.5px;
  font-weight: 500;
  color: var(--ink-2);
  letter-spacing: 0.02em;
}

.queue__due {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--ink-3);
}

.queue__due-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--line-strong);
}

.queue__due.is-overdue {
  color: var(--amber);
}

.queue__due.is-overdue .queue__due-dot {
  background: var(--amber);
}

.queue__title {
  font-size: 13.5px;
  line-height: 1.5;
  color: var(--ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.queue__foot-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 6px;
}

.queue__kp {
  font-size: 12px;
  color: var(--ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.queue__empty {
  padding: 26px 4px 20px;
  text-align: center;
}

.queue__empty-title {
  font-size: 13.5px;
  font-weight: 500;
}

.queue__empty-desc {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--ink-3);
}

.queue__cta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 40px;
  margin-top: 12px;
  border-radius: 9px;
  background: var(--ink);
  color: #fff;
  font-size: 13.5px;
  font-weight: 500;
  transition: background-color 0.18s ease;
}

.queue__cta:hover {
  background: #000;
}

.queue__tip {
  margin-top: 10px;
  font-size: 11.5px;
  color: var(--ink-4);
  text-align: center;
}
</style>
