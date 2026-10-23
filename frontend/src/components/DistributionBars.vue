<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  items: { type: Array, default: () => [] },
  unit: { type: String, default: '道' },
  variant: { type: String, default: 'subject' } // subject | reason
})

const total = computed(() =>
  props.items.reduce((sum, item) => sum + (Number(item.count) || 0), 0)
)

const maxCount = computed(() =>
  props.items.reduce((max, item) => Math.max(max, Number(item.count) || 0), 0)
)

function percentOf(item) {
  if (item.percent !== undefined && item.percent !== null) return item.percent
  if (!total.value) return 0
  return Math.round(((Number(item.count) || 0) / total.value) * 100)
}

function widthOf(item) {
  if (!maxCount.value) return '0%'
  return `${Math.max(4, ((Number(item.count) || 0) / maxCount.value) * 100)}%`
}
</script>

<template>
  <section class="card dist">
    <div class="card__head">
      <div>
        <h2 class="card__title">{{ title }}</h2>
        <p v-if="subtitle" class="card__sub">{{ subtitle }}</p>
      </div>
      <slot name="action"></slot>
    </div>

    <ul class="dist__list">
      <li v-for="item in items" :key="item.name" class="dist__row">
        <div class="dist__meta">
          <span class="dist__name">
            <span
              v-if="variant === 'subject' && item.color"
              class="dist__swatch"
              :style="{ background: item.color }"
              aria-hidden="true"
            ></span>
            {{ item.name }}
          </span>
          <span class="dist__value mono">
            {{ item.count }} {{ unit }}
            <span v-if="variant === 'reason'" class="dist__percent">
              · {{ percentOf(item) }}%
            </span>
          </span>
        </div>
        <div class="dist__track">
          <span class="dist__fill" :style="{ width: widthOf(item) }"></span>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.dist {
  padding-bottom: 22px;
}

.dist__list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 18px 24px 0;
}

.dist__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 7px;
}

.dist__name {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  color: var(--ink);
}

.dist__swatch {
  width: 7px;
  height: 7px;
  border-radius: 2px;
  flex-shrink: 0;
}

.dist__value {
  font-size: 12.5px;
  color: var(--ink-3);
  white-space: nowrap;
}

.dist__percent {
  color: var(--ink-4);
}

.dist__track {
  height: 5px;
  border-radius: 999px;
  background: #f0f0ec;
  overflow: hidden;
}

.dist__fill {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--accent);
  opacity: 0.85;
  transition: width 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}
</style>
