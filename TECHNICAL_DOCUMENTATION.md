# Clean Win 技术架构文档 (v1.0 - 稳定版)

## 1. 项目概览
**Clean Win** 是一款基于 Android 平台的智能通知管理与任务提取工具。它通过自适应拦截规则和 AI 语义分析，帮助用户在专注模式下屏蔽干扰，并自动从碎片化的通知中提取关键待办事项。

## 2. 技术栈详细说明

### 2.1 核心框架
*   **UI 框架**: [Jetpack Compose (BOM 2024.04.01)](https://developer.android.com/jetpack/compose) - 全声明式 UI 开发。
*   **编程语言**: Kotlin 1.9+ - 利用协程与 Flow 处理异步流。
*   **架构模式**: MVVM (Model-View-ViewModel) - 配合应用级 `StateFlow` 实现响应式状态驱动。

### 2.2 数据持久化
*   **本地数据库**: [Room Database 2.6.1](https://developer.android.com/training/data-storage/room) - 存储拦截规则与历史消息。
*   **安全存储**: [EncryptedSharedPreferences (Security-Crypto 1.1.0)](https://developer.android.com/jetpack/androidx/releases/security) - 使用 AES256 加密存储 API Key 等敏感配置。

### 2.3 网络与 AI
*   **核心模型**: DeepSeek-V4-Flash - 负责通知语义提取与时间解析。
*   **网络请求**: [Retrofit 2.9.0](https://square.github.io/retrofit/) + [OkHttp 4.12.0](https://square.github.io/okhttp/) - 处理 RESTful API 调用。
*   **序列化**: Gson - 进行 JSON 数据解析与任务对象转换。

### 2.4 系统服务
*   **通知监听**: `NotificationListenerService` - 核心底层能力，实时捕获系统通知栏数据。
*   **前台服务**: 确保拦截逻辑在后台持久运行，防止系统被杀。
*   **权限管理**: 适配 Android 13+ 的动态通知权限申请流程。

## 3. 核心功能模块 (v1.0)

### 3.1 智能拦截引擎
*   **三维匹配**: 支持按“联系人姓名”、“应用系统名”以及“自定义关键词”进行精确/模糊匹配。
*   **动态送达策略**:
    *   **立即送达**: 传统通知模式。
    *   **任务结束送达**: 用户点击“完成任务”按钮后，批量释放拦截的消息。
    *   **定时送达**: 设定特定时间点（如每日 18:00）集中推送。

### 3.2 AI 任务分析
*   **语义提取**: 自动识别通知中的时间、地点与任务内容。
*   **问句保留**: 针对疑问句式进行特殊提示词优化，保留原始语境。
*   **时间分类**: 自动将任务分为“明确时间”与“待确认时间”两类展示。

### 3.3 交互体验 (UX)
*   **视觉风格**: 商业级实色 UI 方案，支持完全不透明的深色/浅色模式自适应。
*   **流畅动效**: 
    *   **胶囊式导航**: 浮动底栏配合阴影层级。
    *   **侧滑删除**: 全站集合、子集、单条消息均支持标准的左滑删除交互。
    *   **长按排序**: 规则管理支持拖拽式优先级调整。

## 4. 目录结构规范
```text
app/src/main/java/com/cleanwin/app/
├── data/
│   ├── local/          # Room 数据库配置、Entity、DAO 及 SecurityManager
│   └── remote/         # Retrofit 接口定义与 DeepSeekRepository
├── service/            # NotificationMonitorService 及 Intent 管理
├── ui/
│   ├── theme/          # 12 色阶品牌主题定义 (Color/Theme/Type)
│   ├── viewmodel/      # MainViewModel 业务逻辑中转
│   └── MainScreen.kt   # 全量 UI 组件与页面声明
└── MainActivity.kt     # 入口 Activity 与权限初始化
```

---
*文档更新日期：2026-06-01 | 版本状态：v1.0 Final Release*
