# 日历应用 (Calendar App)

一个功能完整的 Android 日历应用，支持日程管理、提醒通知、农历显示和多种视图模式。采用现代化的 Kotlin 和 Jetpack Compose 技术栈构建。

## 📱 功能特性

### 📅 日程管理
- **创建日程**：添加标题、描述、开始/结束时间
- **编辑日程**：修改现有日程的所有信息
- **删除日程**：支持删除不需要的日程
- **全天日程**：支持设置全天事件

### 🔔 智能提醒
- **多种提醒时间**：5分钟、10分钟、30分钟、1小时前
- **全天提醒**：前一天晚上8点、9点、10点提醒
- **当日提醒**：当天早上6点、7点、8点提醒
- **开机自启**：设备重启后自动恢复提醒服务

### 🌙 农历支持
- 显示公历和农历日期
- 展示传统节日和节气信息

### 🗓️ 多视图模式
- **月视图**：查看整月日程分布
- **周视图**：以周为单位展示日程
- **日视图**：详细查看当日日程安排
- **日程列表**：以列表形式展示所有日程

## 🛠 技术栈

| 技术 | 用途 |
|------|------|
| **Kotlin** | 主要开发语言 |
| **Jetpack Compose** | 现代声明式 UI 框架 |
| **Material Design 3** | 设计规范和组件库 |
| **Room Database** | 本地数据持久化 |
| **Kotlin Coroutines** | 异步编程和并发处理 |
| **Flow** | 响应式数据流 |
| **Android Architecture Components** | MVVM 架构组件 |
| **AlarmManager** | 定时任务调度 |
| **Foreground Service** | 后台提醒服务 |
| **kotlinx-datetime** | 日期时间处理 |
| **lunar** | 农历日历库 |

## 📁 项目结构

```
Calendar/
├── app/
│   └── src/main/
│       ├── java/com/calendar/
│       │   ├── MainActivity.kt           # 应用入口
│       │   ├── constants/
│       │   │   └── ViewMode.kt           # 视图模式枚举
│       │   ├── db/
│       │   │   ├── AppDatabase.kt        # Room 数据库配置
│       │   │   ├── Converters.kt         # 类型转换器
│       │   │   └── ScheduleDao.kt        # 数据访问对象
│       │   ├── model/
│       │   │   ├── ReminderType.kt       # 提醒类型枚举
│       │   │   └── Schedule.kt           # 日程实体类
│       │   ├── receiver/
│       │   │   ├── AlarmReceiver.kt      # 闹钟广播接收器
│       │   │   └── BootReceiver.kt       # 开机广播接收器
│       │   ├── repository/
│       │   │   └── ScheduleRepository.kt # 数据仓库
│       │   ├── service/
│       │   │   └── ReminderForegroundService.kt # 前台服务
│       │   ├── ui/
│       │   │   ├── calendar/             # 日历视图组件
│       │   │   │   ├── DayView.kt        # 日视图
│       │   │   │   ├── MonthView.kt      # 月视图
│       │   │   │   ├── ScheduleView.kt   # 日程列表视图
│       │   │   │   └── WeekView.kt       # 周视图
│       │   │   ├── components/           # 通用 UI 组件
│       │   │   ├── home/
│       │   │   │   └── HomeScreen.kt     # 主界面
│       │   │   ├── schedules/            # 日程管理界面
│       │   │   ├── settings/
│       │   │   │   └── SettingsScreen.kt # 设置界面
│       │   │   └── theme/                # 主题样式
│       │   ├── util/
│       │   │   ├── AlarmScheduler.kt     # 提醒调度工具
│       │   │   ├── LunarCalendarUtil.kt  # 农历工具
│       │   │   └── PermissionUtil.kt     # 权限工具
│       │   └── viewmodel/
│       │       └── ScheduleViewModel.kt  # 日程 ViewModel
│       └── res/                          # 资源文件
├── build.gradle.kts                      # 项目构建配置
├── settings.gradle.kts                   # 项目设置
└── gradle/
    └── libs.versions.toml                # 依赖版本管理
```

## 🏗 架构设计

本项目采用 **MVVM (Model-View-ViewModel)** 架构模式，结合 **Repository** 模式进行数据访问。

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                     │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    HomeScreen.kt                    │   │
│  │         (协调各视图组件，处理用户交互)                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ↓                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  ViewModel Layer                     │   │
│  │              ScheduleViewModel.kt                    │   │
│  │         (管理 UI 状态，处理业务逻辑)                   │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ↓                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                   Repository Layer                   │   │
│  │              ScheduleRepository.kt                   │   │
│  │          (封装数据访问逻辑，统一接口)                  │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ↓                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    Data Layer                        │   │
│  │    Room Database + DAO + Data Models                │   │
│  │            (本地数据持久化存储)                       │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 21
- Android SDK 24 (Android 7.0) 或更高版本
- Gradle 8.5+

### 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 运行测试
./gradlew test

# 代码检查
./gradlew lint
```

## 📖 使用说明

### 添加日程
1. 在日历视图中点击任意日期
2. 点击添加按钮 (+)
3. 填写日程信息（标题、时间、提醒等）
4. 保存日程

### 设置提醒
1. 创建或编辑日程
2. 在提醒设置中选择提醒时间
3. 保存日程后系统会自动调度提醒

### 切换视图
- 使用顶部工具栏切换月/周/日/日程列表视图
- 点击日期可快速跳转至指定日期

## 🔐 权限说明

本应用需要以下权限以实现完整功能：

| 权限 | 用途 |
|------|------|
| `POST_NOTIFICATIONS` | 发送日程提醒通知 |
| `FOREGROUND_SERVICE` | 运行后台提醒服务 |
| `FOREGROUND_SERVICE_SPECIAL_USE` | 特殊用途前台服务 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启提醒服务 |

## 📦 依赖版本

- **Android Gradle Plugin**: 8.13.0
- **Kotlin**: 1.9.24
- **Compose Compiler**: 1.5.14
- **Compose BOM**: 2024.02.00
- **Room**: 2.6.1
- **Lifecycle**: 2.7.0
- **Coroutines**: 1.8.0