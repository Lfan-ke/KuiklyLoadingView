# KuiklyLoadingView

基于 [KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 跨端框架构建的加载框组件，支持 Android、iOS、鸿蒙多端运行。

## 功能特性

- **全屏/局部加载**：默认使用 `ActivityIndicator` 作为加载指示器，支持全屏遮罩和局部区域两种模式
- **加载文字**：可选在 spinner 下方显示提示文字
- **超时自动关闭**：支持通过 `timeoutMs` 设置超时时长，超时后自动隐藏并回调
- **自定义遮罩色**：支持自定义全屏遮罩的背景颜色和透明度
- **简洁 DSL 语法**：声明式接入，一行代码即可集成

## 接入指南

### 1. 添加 Maven 仓库

在 `settings.gradle.kts` 中添加仓库地址：

```kotlin
dependencyResolutionManagement {
    repositories {
        // ... 其他仓库
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}
```

### 2. 添加依赖

在模块的 `build.gradle.kts` 中添加：

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

在 `build.ohos.gradle.kts` 中添加：

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

---

## API 文档

### Loading（入口函数）

`ViewContainer` 的扩展函数，用于在 DSL 中声明加载框组件：

```kotlin
fun ViewContainer<*, *>.Loading(init: LoadingView.() -> Unit)
```

---

### LoadingAttr（属性配置）

| 方法 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `visible(Boolean)` | `Boolean` | `false` | 是否显示加载框 |
| `fullScreen(Boolean)` | `Boolean` | `true` | 是否全屏显示（false 为局部内嵌模式） |
| `loadingText(String)` | `String` | `""` | 加载提示文字（空字符串时不显示） |
| `maskColor(Color)` | `Color` | `Color(0,0,0,0.4f)` | 全屏遮罩颜色（仅 fullScreen=true 时生效） |
| `timeoutMs(Int)` | `Int` | `0` | 超时时长（毫秒），`0` 表示不超时 |

---

### LoadingEvent（事件回调）

| 方法 | 说明 |
|------|------|
| `onTimeout(() -> Unit)` | 超时后回调，通常在此将 `visible` 设为 `false` |

---

## 使用示例

### 示例 1：全屏加载

```kotlin
private var showLoading by observable(false)

// 触发显示
Button {
    event { click { ctx.showLoading = true } }
}

// 加载框（放在页面层级以覆盖全屏）
Loading {
    attr {
        visible(ctx.showLoading)
        fullScreen(true)
        loadingText("加载中…")
        maskColor(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.5f))
    }
}
```

### 示例 2：局部内嵌加载

```kotlin
View {
    attr { size(200f, 200f) }

    Loading {
        attr {
            visible(ctx.showLoading)
            fullScreen(false)
            loadingText("请稍候")
        }
    }
}
```

### 示例 3：超时自动关闭

```kotlin
private var showLoading by observable(false)

Loading {
    attr {
        visible(ctx.showLoading)
        fullScreen(true)
        loadingText("3 秒后自动关闭")
        timeoutMs(3000)
    }
    event {
        onTimeout { ctx.showLoading = false }
    }
}
```

---

## License

MIT License
