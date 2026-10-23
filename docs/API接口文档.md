# 错题系统 接口文档

> 版本：v0.1（骨架期约定）
> 技术栈：Vue3 + SpringBoot 3 + MySQL 8，前后端分离，所有接口统一走 `/api` 前缀。
> 本文件是前后端唯一契约：前端 `src/api/modules/*`、mock（`src/mock/index.js`）、后端 `controller/*` 三处必须与本文件一致，改动任何一处都要同步修改另外两处。

---

## 1. 通用约定

### 1.1 统一响应体

所有接口（含文件下载外的全部接口）返回如下 JSON 结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | `0` 表示成功，非 0 为业务错误码 |
| message | string | 成功固定为 `ok`，失败时为可直接展示给用户的中文提示 |
| data | any | 业务数据，失败时为 `null` |

**错误码约定**

| code | 含义 | 前端处理 |
| --- | --- | --- |
| 0 | 成功 | 正常取 `data` |
| 40000 | 通用业务错误（参数不合法、数据不存在、重名等） | 弹出 `message` |
| 40001 | 参数校验失败（`@Valid`） | 弹出 `message` |
| 40100 | 未登录 / 登录已失效 | 清空本地 token，跳转 `/login` |
| 50000 | 服务器内部异常 | 提示「服务异常，请稍后重试」 |
| 50100 | 功能尚未开放（当前用于导出） | 提示 `message` |

前端 `src/api/http.js` 已实现：`code === 0` 时直接返回 `data`；否则抛出携带 `code/message` 的 `ApiError`。因此业务代码拿到的就是 `data` 本身，不需要判 `code`。

### 1.2 鉴权

- 登录成功后返回 `token`，前端存 `localStorage`，后续请求带请求头：`Authorization: Bearer <token>`。
- 令牌由后端 `TokenStore` 签发（骨架期为内存 `Map` + UUID，默认有效期 72 小时，见 `cuotiben.token-expire-hours`），服务重启后失效，前端会自动跳登录页。
- 免登录接口：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/health`；`GET /api/home/overview` 未登录时返回空概览（首页同时作为落地页）。
- 其余接口内部调用 `UserContext.require()`，未登录统一返回 `40100`。

### 1.3 分页

分页接口统一返回：

```json
{ "code": 0, "message": "ok", "data": { "list": [], "total": 0, "page": 1, "size": 20 } }
```

请求参数：`page`（从 1 开始，默认 1）、`size`（默认 20，后端限制 1–100）。

### 1.4 枚举

**掌握状态 `masteryStatus`**（功能 7）

| 值 | 含义 |
| --- | --- |
| `NOT_REVIEWED` | 未复习 |
| `REVIEWING` | 复习中 |
| `MASTERED` | 已掌握 |

**错误原因 `reasonCode`**（功能 5）

| 值 | 含义 |
| --- | --- |
| `KNOWLEDGE` | 知识点不会 |
| `THINKING` | 思路错误 |
| `FORMULA` | 公式记错 |
| `CALCULATION` | 计算错误 |
| `READING` | 审题错误 |
| `CARELESS` | 粗心 |
| `TIME_SHORTAGE` | 时间不足 |
| `OTHER` | 其他 |

**复习结果 `result`**（功能 8）：`CORRECT`（做对了）、`WRONG`（又做错）、`UNSURE`（没把握）。

**导出格式 `format`**（功能 10）：`WORD`、`PDF`。

日期字段统一用字符串：日期 `yyyy-MM-dd`，时间 `yyyy-MM-dd HH:mm:ss`。

### 1.5 功能与接口对照

| 需求文档中的功能 | 接口 |
| --- | --- |
| 1. 用户注册与登录 | `/api/auth/*` |
| 2. 错题录入（文字 + 图片） | `POST /api/mistakes`、`POST /api/mistakes/images` |
| 3. 错题编辑与删除 | `PUT /api/mistakes/{id}`、`DELETE /api/mistakes/{id}` |
| 4. 学科 / 知识点 / 标签分类 | `/api/subjects`、`/api/knowledge-points`、`/api/tags` |
| 5. 错误原因记录 | 录入时带 `reasonCode`，另有 `PUT /api/mistakes/{id}/error-reason` |
| 6. 错题检索与筛选 | `GET /api/mistakes` |
| 7. 掌握状态管理 | `PUT /api/mistakes/{id}/mastery` |
| 8. 错题复习与待复习 | `GET /api/reviews/queue`、`POST /api/reviews`、`PUT /api/mistakes/{id}/schedule` |
| 9. 错题统计 | `/api/stats/*` |
| 10. 错题集导出 | `POST /api/export` |

---

## 2. 认证

### 2.1 注册

`POST /api/auth/register`

```json
{ "username": "linjx", "email": "lin@example.com", "password": "123456", "nickname": "林同学" }
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| username | 是 | 4–20 位，唯一 |
| email | 是 | 邮箱格式，唯一 |
| password | 是 | 6–32 位，后端 BCrypt 加密存储 |
| nickname | 否 | 昵称，默认取 username |

响应 `data`：

```json
{
  "token": "0f4c...",
  "user": { "id": 1, "username": "linjx", "email": "lin@example.com", "nickname": "林同学", "identity": null, "goal": null }
}
```

注册成功后自动为新用户初始化 5 个常用学科（高等数学、线性代数、大学物理、大学英语、数据结构），对应需求「开箱即用」。

### 2.2 登录

`POST /api/auth/login`

```json
{ "account": "linjx", "password": "123456" }
```

`account` 可为用户名或邮箱。响应结构同注册。

### 2.3 注销

`POST /api/auth/logout` —— 注销当前请求头里的令牌，`data` 为 `null`。

### 2.4 当前用户

`GET /api/auth/me` → `data` 为 `user` 对象（结构同上）。

---

## 3. 首页

### 3.1 首页概览

`GET /api/home/overview`

一次返回首屏所需全部数据，前端只发这一个请求（`src/api/modules/home.js`）。

```json
{
  "profile": { "nickname": "林同学", "identity": "大学生", "goal": "期末复习" },
  "stats": {
    "totalCount": 128, "dueTodayCount": 3, "overdueCount": 1,
    "masteredCount": 59, "masteredRate": 46,
    "newThisWeekCount": 12, "newGrowth": 4, "streakDays": 6
  },
  "reviewQueue": [
    { "id": 121, "title": "求 ∫x·e^x dx", "subjectName": "高等数学", "knowledgePointName": "分部积分",
      "masteryStatus": "REVIEWING", "dueLabel": "今天", "overdue": false }
  ],
  "subjectDistribution": [ { "name": "高等数学", "count": 42, "color": "#2c4bd6" } ],
  "reasonDistribution": [ { "code": "KNOWLEDGE", "name": "知识点不会", "count": 43, "percent": 34 } ],
  "recentMistakes": [
    { "id": 128, "title": "极限存在判定的反例", "subjectName": "高等数学", "knowledgePointName": "极限",
      "tags": ["易错题"], "masteryStatus": "NOT_REVIEWED", "reasonCode": "READING",
      "reasonLabel": "审题错误", "displayDate": "今天" }
  ]
}
```

| 字段 | 说明 |
| --- | --- |
| stats.dueTodayCount | 待复习且下次复习日期 ≤ 今天 的错题数 |
| stats.overdueCount | 其中下次复习日期 < 今天（已逾期）的数量 |
| stats.masteredRate | 整数百分比（0–100） |
| stats.newGrowth | 本周新增相对上周的增量 |
| stats.streakDays | 连续复习打卡天数 |
| reviewQueue | 今日待复习队列，最多 5 条（首页卡片展示用；完整队列用 `/api/reviews/queue`） |
| dueLabel | 展示用文案：`已逾期`、`今天`、`明天`、`N 天后` |

未登录访问时 `stats` 全为 0、各数组为空，`profile.nickname` 为「同学」，前端据此展示落地页文案。

---

## 4. 错题（功能 2 / 3 / 5 / 6 / 7）

### 4.1 分页检索筛选

`GET /api/mistakes`

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| keyword | string | 模糊匹配题干 / 答案 / 解析 / 来源 |
| subjectId | long | 学科筛选 |
| knowledgePointId | long | 知识点筛选 |
| reasonCode | string | 错误原因筛选 |
| masteryStatus | string | 掌握状态筛选 |
| tagIds | long[] | 标签筛选，重复传参：`?tagIds=1&tagIds=2`（同时含全部标签） |
| dueOnly | bool | `true` 只返回进入复习队列且到期/逾期的错题 |
| page / size | int | 分页 |

响应 `data` 为分页结构，`list` 元素为错题摘要：

```json
{ "id": 128, "title": "极限存在判定的反例", "subjectName": "高等数学", "knowledgePointName": "极限",
  "tags": ["易错题"], "masteryStatus": "NOT_REVIEWED", "reasonCode": "READING",
  "reasonLabel": "审题错误", "displayDate": "今天" }
```

> `displayDate` 为列表展示用相对日期（今天 / 昨天 / N 天前 / yyyy-MM-dd），由后端算好，避免前端各页面重复实现。

### 4.2 错题详情

`GET /api/mistakes/{id}`

```json
{
  "id": 128, "subjectId": 1, "subjectName": "高等数学", "knowledgePointId": 3, "knowledgePointName": "极限",
  "title": "极限存在判定的反例", "answer": "...", "myAnswer": "...", "analysis": "...",
  "source": "2024 期末卷第 12 题", "imageUrls": ["/uploads/abc.png"],
  "reasonCode": "READING", "reasonLabel": "审题错误", "reasonNote": "没看清「不」字",
  "masteryStatus": "NOT_REVIEWED", "nextReviewDate": "2025-01-05",
  "inReviewQueue": true, "tags": ["易错题"], "tagIds": [2],
  "createdAt": "2024-12-30 10:00:00", "updatedAt": "2024-12-30 10:00:00"
}
```

不属于当前用户的错题返回 `40000`「错题不存在」。

### 4.3 新增错题

`POST /api/mistakes`

```json
{
  "subjectId": 1, "knowledgePointId": 3, "title": "极限存在判定的反例",
  "answer": "...", "myAnswer": "...", "analysis": "...", "source": "2024 期末卷第 12 题",
  "imageUrls": ["/uploads/abc.png"], "reasonCode": "READING", "reasonNote": "没看清「不」字",
  "tags": ["易错题", "重点题"], "masteryStatus": "NOT_REVIEWED", "nextReviewDate": "2025-01-05"
}
```

仅 `subjectId`、`title` 必填。未传 `masteryStatus` 时默认 `NOT_REVIEWED`，未传 `nextReviewDate` 时默认「明天」，并自动进入待复习队列。`tags` 传标签名数组，后端按需创建（同名复用）。

响应：`{ "id": 128 }`。

### 4.4 编辑错题

`PUT /api/mistakes/{id}` —— 请求体同新增，整体覆盖；`tags` 传空数组表示清空标签。

### 4.5 删除错题

`DELETE /api/mistakes/{id}` —— 同时删除其标签关联与复习记录（数据库外键级联）。

### 4.6 修改掌握状态

`PUT /api/mistakes/{id}/mastery`

```json
{ "masteryStatus": "MASTERED" }
```

### 4.7 修改错误原因

`PUT /api/mistakes/{id}/error-reason`

```json
{ "reasonCode": "CALCULATION", "reasonNote": "符号写错" }
```

### 4.8 设置下次复习日期

`PUT /api/mistakes/{id}/schedule`

```json
{ "nextReviewDate": "2025-01-10" }
```

传 `null` 表示移出待复习队列。

### 4.9 上传题目图片

`POST /api/mistakes/images`，`multipart/form-data`，字段名 `file`。

- 仅接受图片（`image/*`），单张 ≤ 10MB；
- 返回 `{ "url": "/uploads/xxx.png", "path": "/uploads/xxx.png" }`，前端把 `url` 放进 `imageUrls` 数组；
- 文件保存在 `cuotiben.upload-dir`（默认 `./uploads`），数据库只存相对路径；静态映射：`GET /uploads/xxx.png`。

### 4.10 某题的复习历史

`GET /api/mistakes/{id}/reviews` → `data` 为复习记录数组：

```json
[ { "id": 9, "mistakeId": 128, "userId": 1, "result": "WRONG", "masteryStatus": "REVIEWING",
    "reasonCode": "READING", "reasonNote": "", "nextReviewDate": "2025-01-02",
    "reviewedAt": "2025-01-01 20:15:00" } ]
```

---

## 5. 分类（功能 4）

### 5.1 学科

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/subjects` | 学科列表（含错题数） |
| POST | `/api/subjects` | 新增，body `{ "name": "概率论", "color": "#2c4bd6" }` |
| PUT | `/api/subjects/{id}` | 改名 / 改色 |
| DELETE | `/api/subjects/{id}` | 删除；该学科下仍有错题时返回 `40000` 拒绝 |

`GET /api/subjects` 响应：

```json
[ { "id": 1, "name": "高等数学", "color": "#2c4bd6", "sortNo": 1, "mistakeCount": 42 } ]
```

### 5.2 知识点

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/subjects/{id}/knowledge-points` | 某学科的知识点（含错题数） |
| GET | `/api/knowledge-points?subjectId=1` | 同上，`subjectId` 可省略表示全部 |
| POST | `/api/knowledge-points` | 新增，body `{ "subjectId": 1, "parentId": null, "name": "洛必达法则" }` |

`parentId` 支持二级知识点。响应：

```json
[ { "id": 3, "subjectId": 1, "parentId": null, "name": "极限", "sortNo": 1, "mistakeCount": 12 } ]
```

### 5.3 标签

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/tags` | 标签列表（含使用次数） |
| POST | `/api/tags` | 新增，body `{ "name": "易错题" }`，重名返回 `40000` |
| DELETE | `/api/tags/{id}` | 删除标签并解除所有错题关联 |

```json
[ { "id": 2, "name": "易错题", "mistakeCount": 8 } ]
```

---

## 6. 复习（功能 8）

### 6.1 待复习队列

`GET /api/reviews/queue` → 与首页 `reviewQueue` 元素结构一致，最多 50 条，按「逾期优先 + 日期升序」排序：

```json
[ { "id": 121, "title": "求 ∫x·e^x dx", "subjectName": "高等数学", "knowledgePointName": "分部积分",
    "masteryStatus": "REVIEWING", "dueLabel": "已逾期", "overdue": true } ]
```

### 6.2 提交复习结果

`POST /api/reviews`

```json
{ "mistakeId": 121, "result": "CORRECT", "masteryStatus": "MASTERED",
  "reasonCode": null, "reasonNote": null, "nextReviewDate": "2025-01-08" }
```

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| mistakeId | 是 | 错题 id |
| result | 是 | `CORRECT` / `WRONG` / `UNSURE` |
| masteryStatus | 否 | 不传时后端按结果推断：CORRECT→`MASTERED`，WRONG→`NOT_REVIEWED`，UNSURE→`REVIEWING` |
| reasonCode / reasonNote | 否 | 传了则同时更新错题的错误原因（「又做错」时常用） |
| nextReviewDate | 否 | 不传时后端按掌握状态推荐下次复习日期 |

副作用：写入一条复习记录、更新错题的掌握状态 / 下次复习日期 / 错误原因，并据此把该题加入或移出待复习队列。

---

## 7. 统计（功能 9）

### 7.1 概览

`GET /api/stats/summary`

```json
{
  "totalCount": 128, "masteredCount": 59, "reviewingCount": 43, "notReviewedCount": 26,
  "dueTodayCount": 3, "masteredRate": 46,
  "byMastery": [ { "status": "NOT_REVIEWED", "label": "未复习", "count": 26 } ],
  "bySubject": [ { "name": "高等数学", "count": 42, "color": "#2c4bd6" } ],
  "byReason": [ { "code": "KNOWLEDGE", "name": "知识点不会", "count": 43, "percent": 34 } ]
}
```

### 7.2 趋势

`GET /api/stats/trend?days=7`（`days` 1–90，默认 7）

```json
[ { "date": "2025-01-01", "added": 3, "reviewed": 5 } ]
```

`added` 为当天新增错题数，`reviewed` 为当天复习次数。

### 7.3 错误原因分布

`GET /api/stats/reasons` → 同 `byReason`：`[ { "code", "name", "count", "percent" } ]`，`percent` 以「已标注错误原因的错题数」为分母，合计约 100。

---

## 8. 导出（功能 10）

`POST /api/export`

```json
{ "format": "WORD", "mistakeIds": [128, 121], "subjectId": null, "masteryStatus": null }
```

- `mistakeIds` 为空表示按 `subjectId` / `masteryStatus` 条件导出当前筛选结果；
- 成功时**不返回 JSON**，而是文件流：`Content-Type: application/octet-stream`、`Content-Disposition: attachment; filename="错题集.docx"`；
- 前端 `exportMistakes` 已按 `responseType: 'blob'` 处理，拿到 `Blob` 后触发浏览器下载。

> 当前状态：路由、参数、文件流响应已就绪，文档生成逻辑待补充，接口先返回 `50100`「导出功能开发中，敬请期待」，前端据此弹提示。这是唯一未完成的接口。

---

## 9. 健康检查

`GET /api/health`（免登录）

```json
{ "code": 0, "message": "ok", "data": { "status": "UP", "time": "2025-01-01 10:00:00", "service": "cuotiben-backend" } }
```

前端可用它探测后端是否已启动；本机默认地址 `http://localhost:8080/api/health`。

---

## 10. 本地 mock 说明

前端默认 **不依赖后端**：`.env.development` 中 `VITE_USE_MOCK=true` 时，`src/api/http.js` 会把请求转给 `src/mock/index.js`（按正则匹配路径，模拟 240ms 网络延迟，内存 CRUD）。因此：

- 只启动前端 → 整套页面可点、可跳转、首页数据完整；
- 想联调真后端 → `.env.development` 改成 `VITE_USE_MOCK=false`，重启 `npm run dev`，并保证后端在 `localhost:8080` 运行（Vite 已配置 `/api` 代理）。

mock 与真后端返回结构完全一致，切换时页面代码无需改动。
