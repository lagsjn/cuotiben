# 错题系统（学生端）

面向大学生的错题管理系统：把错题连同「为什么错」记下来，按遗忘节奏提醒复习，最后能一键导出成 Word / PDF 错题集。

课程：软件项目管理 · 第一组
需求来源：`1第一组项目开发基本功能.docx`

---

## 1. 技术栈与架构

| 层 | 选型 | 说明 |
| --- | --- | --- |
| 前端 | Vue 3 + Vite 6 + vue-router 4 | 单页应用，无 UI 组件库（自建设计变量，见 `frontend/src/styles/tokens.css`） |
| 后端 | Spring Boot 3.3.5 + Java 17 + MyBatis-Plus 3.5.7 | 分层：`controller / service / mapper / entity / dto` |
| 数据库 | MySQL 8.0（InnoDB + utf8mb4） | 建表脚本 `backend/sql/schema.sql` |
| 交互 | HTTP + JSON，统一响应体 `{code, message, data}` | 前后端分离，跨域已配置 |

```
软件项目管理/
├── frontend/                  前端工程
│   ├── src/
│   │   ├── api/               接口层：http 封装 + 各业务模块 + mock
│   │   ├── components/        通用组件（头部、底部、统计卡、分布条…）
│   │   ├── constants/         业务枚举（掌握状态 / 错误原因 / 复习结果）
│   │   ├── mock/              mock 数据与路由（默认开启，无需后端即可开发）
│   │   ├── router/            路由表（10 个功能页面均已占位）
│   │   ├── styles/            设计变量与基础样式
│   │   └── views/             页面
│   └── vite.config.js         端口 5175，/api 代理到 8080
├── backend/                   后端工程
│   ├── sql/schema.sql         数据库建表脚本
│   └── src/main/java/com/cuotiben/
│       ├── common/            统一响应、分页、异常、登录上下文
│       ├── config/            跨域、拦截器、MyBatis-Plus、令牌
│       ├── controller/        REST 接口
│       ├── dto/               请求体与视图对象
│       ├── entity/            表实体
│       ├── enums/             业务枚举
│       ├── mapper/            数据访问
│       └── service/           业务逻辑
└── docs/API接口文档.md         前后端接口契约
```

---

## 2. 快速开始

### 2.1 前端（只启动前端即可看到完整首页）

```bash
cd frontend
npm install
npm run dev          # http://localhost:5175
```

默认 `VITE_USE_MOCK=true`，数据来自 `src/mock/`，**不需要启动后端**。

### 2.2 后端（需要 JDK 17 + Maven + MySQL 8）

```bash
# 1) 建库建表（一次即可）
mysql -u root -p --default-character-set=utf8mb4 < backend/sql/schema.sql

# 2) 启动
cd backend
mvn spring-boot:run
# 若数据库账号不是 root/123456，用环境变量覆盖：
#   MYSQL_USER=root  MYSQL_PASSWORD=你的密码
```

启动后自检：

```bash
curl http://localhost:8080/api/health
# {"code":0,"message":"ok","data":{"status":"UP",...}}
```

### 2.3 前后端联调

把 `frontend/.env.development` 里的 `VITE_USE_MOCK` 改为 `false`，重启 `npm run dev`。Vite 已把 `/api` 代理到 `http://localhost:8080`，无需改前端代码。

生产构建：`npm run build`（输出 `frontend/dist`，其中 `VITE_USE_MOCK=false`）。

---

## 3. 功能完成情况

| # | 需求文档功能 | 前端 | 后端 | 说明 |
| --- | --- | --- | --- | --- |
| 1 | 用户注册与登录 | 路由 + 接口就绪 | 已实现 | BCrypt 加密；注册自动初始化 5 个学科 |
| 2 | 错题录入（文字 + 图片） | 路由 + 接口就绪 | 已实现 | 图片上传 `/api/mistakes/images`，单张 ≤ 10MB |
| 3 | 错题编辑与删除 | 路由 + 接口就绪 | 已实现 | 删除级联清理标签关联与复习记录 |
| 4 | 学科 / 知识点 / 标签 | 路由 + 接口就绪 | 已实现 | 学科、知识点二级、标签多对多 |
| 5 | 错误原因记录 | 路由 + 接口就绪 | 已实现 | 8 类原因 + 文字补充 |
| 6 | 错题检索与筛选 | 路由 + 接口就绪 | 已实现 | 关键词 / 学科 / 知识点 / 原因 / 状态 / 标签 / 到期 |
| 7 | 掌握状态管理 | 路由 + 接口就绪 | 已实现 | 未复习 / 复习中 / 已掌握 |
| 8 | 错题复习与待复习 | 路由 + 接口就绪 | 已实现 | 待复习队列、提交复习结果、复习历史 |
| 9 | 错题统计 | 路由 + 接口就绪 | 已实现 | 概览、7 天趋势、原因与学科分布 |
| 10 | 导出 Word / PDF | 路由 + 接口就绪 | **待补** | 接口与文件流已就绪，文档生成逻辑未实现（返回 50100） |
| — | 首页 | **已完成** | 已实现 | 概览四卡 + 待复习队列 + 学科/原因分布 + 最近错题 |

「前端路由 + 接口就绪」表示：路由、页面占位、`src/api/modules/*` 调用函数、mock 数据都已按接口文档写好，页面实现时直接调即可。

---

## 4. 首页设计说明

首页同时承担「落地页」与「今日工作台」两个角色，因此信息按「先共情、再动手、后数据」排布：

1. **Hero**：一句话定位（错过的题，只错一次）+ 两个主行动（开始录入 / 去复习今日 N 道）；
2. **今日待复习**：右上卡片直接列出到期题目，逾期用红色标记，让学生一进来就知道先做哪几题；
3. **错题概览**：总题数、待复习、已掌握率、连续打卡四张统计卡；
4. **分布**：学科分布 + 错误原因分布，回答「我弱在哪」；
5. **最近录入**：最近 8 条错题，作为「刚才记的东西还在」的确认。

设计取向是安静的纸白 + 墨蓝，细边框、无渐变、无大圆角阴影，避免教育类产品常见的花哨感。响应式断点 980 / 720 / 640，手机端（390px）已验证无横向溢出。

---

## 5. 约定与注意事项

- **接口契约**：`docs/API接口文档.md` 是唯一标准。改接口要同步改三处：文档、`src/api/modules/*`、`src/mock/index.js`（如涉及 mock）。
- **枚举一致性**：`frontend/src/constants/enums.js` 与后端 `enums/` 包一一对应，文案也必须一致（后端会直接返回 `reasonLabel`）。
- **登录令牌**：骨架期为内存 `TokenStore` + UUID，服务重启即失效；生产可换 JWT 或 Redis，只需替换 `TokenStore` 实现。
- **图片存储**：本地目录 `backend/uploads`（`cuotiben.upload-dir` 可改），数据库只存 `/uploads/xxx.png` 相对路径，通过静态资源映射访问。
- **统计数据**：首页与统计的聚合目前用「按条件分别 count」，数据量大时建议改为一条 `GROUP BY` SQL。
- **上传目录与构建产物**不要提交到仓库，已在 `.gitignore` 中忽略。

---

## 6. 待办

1. 导出功能：接入 Word（如 poi-tl）/ PDF（如 openpdf）后替换 `ExportService` 内的占位实现；
2. 各功能页面的具体 UI（当前为占位页）：录入表单、错题列表、复习卡片、统计图表；
3. 后端单元测试与接口测试；
4. 部署脚本与 Nginx 配置（可选，课程演示阶段本机运行即可）。
