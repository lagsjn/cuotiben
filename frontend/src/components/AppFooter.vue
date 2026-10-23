<script setup>
import { RouterLink } from 'vue-router'

const year = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <div class="container footer-inner">
      <div class="footer-brand">
        <span class="footer-mark" aria-hidden="true">错</span>
        <div>
          <p class="footer-name">错题本</p>
          <p class="footer-desc">面向大学生的错题整理与复习系统</p>
        </div>
      </div>

      <div class="footer-links">
        <RouterLink to="/library">错题库</RouterLink>
        <RouterLink to="/review">复习</RouterLink>
        <RouterLink to="/stats">统计</RouterLink>
        <RouterLink to="/export">导出</RouterLink>
      </div>

      <p class="footer-copy">
        软件项目管理课程项目 · 第一组<br />
        © {{ year }} 错题本
      </p>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  border-top: 1px solid var(--line);
  padding: 32px 0 40px;
}

.footer-inner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-mark {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 9px;
  border: 1px solid var(--line-strong);
  background: var(--surface);
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-2);
}

.footer-name {
  font-size: 13.5px;
  font-weight: 600;
}

.footer-desc {
  font-size: 12.5px;
  color: var(--ink-3);
}

.footer-links {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--ink-2);
}

.footer-links a:hover {
  color: var(--ink);
}

.footer-copy {
  font-size: 12.5px;
  line-height: 1.7;
  color: var(--ink-3);
  text-align: right;
}

@media (max-width: 720px) {
  .footer-inner {
    flex-direction: column;
  }

  .footer-copy {
    text-align: left;
  }
}
</style>
