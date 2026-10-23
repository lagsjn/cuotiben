<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { ERROR_REASON_MAP } from '@/constants/enums'
import StatusPill from './StatusPill.vue'

const props = defineProps({
  items: { type: Array, default: () => [] }
})

const total = computed(() => props.items.length)

function reasonLabel(code) {
  return ERROR_REASON_MAP[code] ?? ''
}
</script>

<template>
  <section class="card recent">
    <div class="card__head">
      <div>
        <h2 class="card__title">最近录入</h2>
        <p class="card__sub">按录入时间倒序，显示最新 {{ total }} 道</p>
      </div>
      <RouterLink to="/library" class="recent__more">
        全部错题
        <svg
          width="13"
          height="13"
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

    <ul class="recent__list">
      <li v-for="(item, index) in items" :key="item.id" class="recent__row">
        <span class="recent__index mono">{{ String(index + 1).padStart(2, '0') }}</span>

        <div class="recent__main">
          <p class="recent__title">{{ item.title }}</p>
          <div class="recent__meta">
            <span class="recent__subject">{{ item.subjectName }}</span>
            <span class="recent__sep">·</span>
            <span>{{ item.knowledgePointName }}</span>
            <span class="recent__sep">·</span>
            <span>{{ item.displayDate }}</span>
            <span v-if="reasonLabel(item.reasonCode)" class="recent__reason">
              {{ reasonLabel(item.reasonCode) }}
            </span>
          </div>
          <ul v-if="item.tags?.length" class="recent__tags">
            <li v-for="tag in item.tags" :key="tag" class="recent__tag">
              {{ tag }}
            </li>
          </ul>
        </div>

        <StatusPill :status="item.masteryStatus" class="recent__pill" />
      </li>
    </ul>

    <div v-if="!items.length" class="recent__empty">
      还没有错题，<RouterLink to="/library/new">录入第一道</RouterLink>开始积累。
    </div>
  </section>
</template>

<style scoped>
.recent {
  margin-top: 16px;
}

.recent__more {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--ink-2);
  padding-bottom: 2px;
}

.recent__more:hover {
  color: var(--ink);
}

.recent__list {
  display: flex;
  flex-direction: column;
  padding: 10px 24px 8px;
}

.recent__row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 15px 0;
  border-bottom: 1px solid var(--line);
}

.recent__row:last-child {
  border-bottom: none;
}

.recent__index {
  font-size: 12px;
  color: var(--ink-4);
  padding-top: 3px;
  flex-shrink: 0;
}

.recent__main {
  flex: 1;
  min-width: 0;
}

.recent__title {
  font-size: 14px;
  line-height: 1.55;
  color: var(--ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recent__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
  margin-top: 5px;
  font-size: 12px;
  color: var(--ink-3);
}

.recent__subject {
  color: var(--ink-2);
  font-weight: 500;
}

.recent__sep {
  color: var(--ink-4);
}

.recent__reason {
  margin-left: 2px;
  padding: 0 7px;
  border-radius: 5px;
  background: var(--gray-pill);
  color: var(--ink-2);
  font-size: 11.5px;
  line-height: 19px;
}

.recent__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 7px;
}

.recent__tag {
  padding: 1px 8px;
  border: 1px solid var(--line);
  border-radius: 999px;
  font-size: 11.5px;
  color: var(--ink-3);
}

.recent__pill {
  margin-top: 3px;
}

.recent__empty {
  padding: 34px 24px 40px;
  text-align: center;
  font-size: 13.5px;
  color: var(--ink-3);
}

.recent__empty a {
  color: var(--accent);
  text-decoration: underline;
  text-underline-offset: 3px;
}

@media (max-width: 640px) {
  .recent__list {
    padding: 6px 18px 4px;
  }

  .recent__index {
    display: none;
  }

  .recent__row {
    gap: 12px;
  }
}
</style>
