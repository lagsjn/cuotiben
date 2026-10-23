<script setup>
import { computed } from 'vue'
import { MASTERY_STATUS } from '@/constants/enums'

const props = defineProps({
  status: { type: String, default: 'NOT_REVIEWED' }
})

const meta = computed(
  () => MASTERY_STATUS[props.status] ?? { label: '未复习', tone: 'gray' }
)
</script>

<template>
  <span class="pill" :class="`pill--${meta.tone}`">
    <span class="pill__dot" aria-hidden="true"></span>
    {{ meta.label }}
  </span>
</template>

<style scoped>
.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.pill__dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: currentColor;
}

.pill--gray {
  color: #6b7178;
  background: var(--gray-pill);
}

.pill--amber {
  color: var(--amber);
  background: var(--amber-soft);
}

.pill--green {
  color: var(--green);
  background: var(--green-soft);
}
</style>
