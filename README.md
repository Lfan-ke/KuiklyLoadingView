# KuiklyLoadingView

基于 [KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 跨端框架构建的 UI 组件库，涵盖加载状态、反馈提示、数据展示等 20 个常用组件，支持 Android、iOS、鸿蒙三端运行。

## 组件列表

| 类别 | 组件 |
|------|------|
| 加载状态 | Loading（全屏/局部遮罩）、Spin（容器级 Loading）、ProgressBar（进度条）、CircularProgress（环形进度）、StepProgress（步骤条）、LoadMore（加载更多） |
| 反馈提示 | Toast（轻提示）、Alert（内嵌警告条）、Result（结果页）、NoticeBar（通知栏滚动） |
| 数据展示 | Skeleton（骨架屏）、Statistic（统计数值）、CountDown（倒计时）、Timeline（时间轴）、Rate（评分） |
| 标注装饰 | Badge（徽标）、Tag（标签）、Watermark（水印）、Collapse（折叠面板）、FloatButton（悬浮按钮） |

## 接入指南

### 1. 添加 Maven 仓库

在 `settings.gradle.kts` 中添加：

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}
```

### 2. 添加依赖

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuiklybase:KuiklyLoading:1.0.0-2.0.21")
            }
        }
    }
}
```

HarmonyOS 额外配置 `build.ohos.gradle.kts`：

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuiklybase:KuiklyLoading:1.0.0-2.0.21-KBA-010")
            }
        }
    }
}
```

## 使用示例

### Loading（全屏加载遮罩）

```kotlin
private var showLoading by observable(false)

Loading {
    attr {
        visible(ctx.showLoading)
        fullScreen(true)
        loadingText("加载中...")
        maskColor(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.5f))
        timeoutMs(10000)
    }
    event {
        onTimeout { ctx.showLoading = false }
    }
}
```

### Toast（轻提示）

```kotlin
Toast {
    attr {
        visible(ctx.showToast)
        message("操作成功")
        icon(ToastIcon.SUCCESS)
        position(ToastPosition.CENTER)
        durationMs(2000)
    }
    event {
        onDismiss { ctx.showToast = false }
    }
}
```

### Skeleton（骨架屏）

```kotlin
Skeleton {
    attr {
        preset(SkeletonPreset.ARTICLE)
        animation(SkeletonAnimation.SHIMMER)
        theme(SkeletonTheme.LIGHT)
        loading(ctx.isLoading)
    }
    content {
        // 真实内容，loading=false 时展示
        Text { attr { text("文章内容已加载") } }
    }
}
```

### Badge（徽标）

```kotlin
Badge {
    attr {
        type(BadgeType.COUNT)
        count(ctx.messageCount)
        color(BadgeColor.RED)
        max(99)
    }
    content {
        // 被徽标包裹的内容
        Image { attr { src("icon_message.png"); size(24f, 24f) } }
    }
}
```

### Watermark（水印）

```kotlin
Watermark {
    attr {
        text("CONFIDENTIAL")
        color(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.08f))
        rotate(-25f)
        grid(cols = 4, rows = 8)
    }
    content {
        // 受保护的内容区域
        View { attr { allFill() } }
    }
}
```

## 示例

完整示例见 `shared/src/commonMain/kotlin/com/kuikly/kuiklyloading/LoadingViewDemoPage.kt`，
在线效果演示见 [GitHub Pages](https://lfan-ke.github.io/KuiklyLoadingView/)。

## 相关资源

- [Kuikly 官方文档](https://kuikly.tds.qq.com/)
- [KuiklyUI 仓库](https://github.com/Tencent-TDS/KuiklyUI)

## License

[KuiklyUI License](https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE)
