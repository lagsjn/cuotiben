<script setup>
import { RouterLink } from 'vue-router'

const navItems = [
  { to: '/', label: '首页' },
  { to: '/library', label: '错题库' },
  { to: '/review', label: '复习' },
  { to: '/stats', label: '统计' },
  { to: '/export', label: '导出' }
]
</script>

<template>
  <header class="site-header">
    <div class="container header-inner">
      <RouterLink to="/" class="brand" aria-label="错题本首页">
        <span class="brand__mark" aria-hidden="true">
          <svg
            width="17"
            height="17"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
            <path
              d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"
            />
            <path d="m9 8.5 2 2 4-4" />
          </svg>
        </span>
        <span class="brand__text">错题本</span>
      </RouterLink>

      <nav class="nav" aria-label="主导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav__link"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="header-actions">
        <RouterLink to="/login" class="btn btn--text login-link">登录</RouterLink>
        <RouterLink to="/library/new" class="btn btn--primary">
          录入错题
        </RouterLink>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(246, 246, 243, 0.86);
  backdrop-filter: saturate(1.4) blur(12px);
  -webkit-backdrop-filter: saturate(1.4) blur(12px);
  border-bottom: 1px solid var(--line);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 32px;
  height: 64px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.brand__mark {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: var(--ink);
  color: #fff;
}

.brand__text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav__link {
  position: relative;
  padding: 6px 10px;
  border-radius: 7px;
  font-size: 14px;
  color: var(--ink-2);
  transition: color 0.16s ease, background-color 0.16s ease;
}

.nav__link:hover {
  color: var(--ink);
  background: rgba(23, 24, 26, 0.04);
}

.nav__link.router-link-active {
  color: var(--ink);
  font-weight: 500;
}

.nav__link.router-link-active::after {
  content: "";
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: -1px;
  height: 2px;
  border-radius: 2px;
  background: var(--ink);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.login-link {
  color: var(--ink-2);
}

/* 窄屏（手机 / 平板竖屏）：导航不隐藏，换成第二行可横向滑动，
   保证手机上也能进入错题库、复习、统计、导出 */
@media (max-width: 840px) {
  .header-inner {
    flex-wrap: wrap;
    gap: 4px 12px;
    height: auto;
    padding-top: 10px;
    padding-bottom: 8px;
  }

  .nav {
    order: 3;
    flex: 1 0 100%;
    gap: 2px;
    overflow-x: auto;
    scrollbar-width: none;
    margin: 0 -10px;
    padding: 0 10px;
  }

  .nav::-webkit-scrollbar {
    display: none;
  }

  .nav__link {
    padding: 6px 10px;
    white-space: nowrap;
  }

  .nav__link.router-link-active::after {
    bottom: 0;
  }

  .header-actions {
    margin-left: auto;
  }
}

/* 小屏保留登录入口，只压缩间距（隐藏会让手机用户无法登录） */
@media (max-width: 420px) {
  .header-inner {
    column-gap: 8px;
  }

  .login-link {
    padding: 6px 2px;
    font-size: 13px;
  }

  .brand {
    gap: 8px;
  }

  .brand__text {
    font-size: 15px;
  }
}
</style>
