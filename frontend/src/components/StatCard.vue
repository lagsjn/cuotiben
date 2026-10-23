<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  unit: { type: String, default: '' },
  hint: { type: String, default: '' },
  note: { type: String, default: '' },
  tone: { type: String, default: 'default' } // default | warn | good
})
</script>

<template>
  <div class="stat">
    <p class="stat__label">{{ label }}</p>
    <p class="stat__value">
      <span class="stat__num mono">{{ value }}</span>
      <span v-if="unit" class="stat__unit">{{ unit }}</span>
    </p>
    <p v-if="hint || note" class="stat__foot">
      <span v-if="note" class="stat__note" :class="`is-${tone}`">{{ note }}</span>
      <span v-if="hint" class="stat__hint">{{ hint }}</span>
    </p>
  </div>
</template>

<style scoped>
.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 18px 20px 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
}

.stat__label {
  font-size: 12.5px;
  color: var(--ink-3);
  letter-spacing: 0.02em;
}

.stat__value {
  display: flex;
  align-items: baseline;
  gap: 5px;
  margin-top: 4px;
}

.stat__num {
  font-size: 27px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.stat__unit {
  font-size: 13px;
  color: var(--ink-3);
}

.stat__foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  min-height: 18px;
  font-size: 12.5px;
  color: var(--ink-3);
}

.stat__note {
  font-weight: 500;
  white-space: nowrap;
}

.stat__note.is-warn {
  color: var(--amber);
}

.stat__note.is-good {
  color: var(--green);
}

.stat__hint {
  color: var(--ink-4);
}

.stat__hint::before {
  content: "·";
  margin-right: 8px;
  color: var(--ink-4);
}

/* 窄屏：说明与注释分两行，避免词被拆开 */
@media (max-width: 720px) {
  .stat {
    padding: 16px 16px 14px;
  }

  .stat__num {
    font-size: 24px;
  }

  .stat__foot {
    flex-direction: column;
    align-items: flex-start;
    gap: 1px;
  }

  .stat__hint::before {
    content: none;
  }
}
</style>
