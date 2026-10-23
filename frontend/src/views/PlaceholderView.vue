<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()

const title = computed(() => route.meta?.title ?? '页面开发中')
const desc = computed(() => route.meta?.desc ?? '这个页面正在开发中。')
const isNotFound = computed(() => route.name === 'not-found')
</script>

<template>
  <div class="container placeholder">
    <div class="placeholder__card card">
      <span v-if="isNotFound" class="placeholder__badge">404</span>
      <h1 class="placeholder__title">{{ title }}</h1>
      <p class="placeholder__desc">{{ desc }}</p>
      <p v-if="isNotFound" class="placeholder__hint">
        检查一下地址，或回到首页重新开始。
      </p>
      <div class="placeholder__actions">
        <RouterLink to="/" class="btn btn--primary">回到首页</RouterLink>
        <RouterLink to="/library" class="btn btn--ghost">看看错题库</RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.placeholder {
  padding: 88px 24px 40px;
  display: flex;
  justify-content: center;
}

.placeholder__card {
  width: 100%;
  max-width: 560px;
  padding: 36px 34px 32px;
  text-align: left;
}

.placeholder__badge {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent);
  font-size: 11.5px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.placeholder__title {
  margin-top: 14px;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.placeholder__desc {
  margin-top: 8px;
  font-size: 14px;
  color: var(--ink-2);
}

.placeholder__hint {
  margin-top: 12px;
  font-size: 12.5px;
  line-height: 1.8;
  color: var(--ink-3);
}

.placeholder__actions {
  display: flex;
  gap: 10px;
  margin-top: 24px;
}
</style>
