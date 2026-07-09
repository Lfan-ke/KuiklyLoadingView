/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kuikly.kuiklyloading

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.ActivityIndicator
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuiklybase.loading.Loading
import com.tencent.kuiklybase.loading.Skeleton
import com.tencent.kuiklybase.loading.SkeletonAnimation
import com.tencent.kuiklybase.loading.SkeletonPreset
import com.tencent.kuiklybase.loading.SkeletonRow
import com.tencent.kuiklybase.loading.SkeletonTheme

@Page("LoadingViewDemoPage")
internal class LoadingViewDemoPage : BasePager() {

    // --- Loading state ---
    private var showFullScreen by observable(false)
    private var showPartial by observable(false)
    private var showWithTimeout by observable(false)
    private var showCustomSize by observable(false)
    private var showCustomContent by observable(false)
    private var showWithDelay by observable(false)

    // --- Skeleton state ---
    private var skArticleLoading by observable(true)
    private var skProfileLoading by observable(true)
    private var skListLoading by observable(true)
    private var skPulseLoading by observable(true)
    private var skRoundLoading by observable(true)
    private var skCustomLoading by observable(true)
    private var skDarkLoading by observable(true)
    private var skBlueLoading by observable(true)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Scroller {
                attr {
                    flex(1f)
                    flexDirectionColumn()
                    backgroundColor(Color(0xFFF5F5F5L))
                }

                // ---- Section header helper ----
                fun sectionHeader(title: String) {
                    View {
                        attr {
                            height(40f)
                            backgroundColor(Color(0xFFEEEEEEL))
                            justifyContentCenter()
                            paddingLeft(16f)
                            marginTop(8f)
                        }
                        Text {
                            attr {
                                fontSize(13f)
                                color(Color(0xFF555555L))
                                text(title)
                            }
                        }
                    }
                }

                fun demoButton(label: String, bgColor: Color, onClick: () -> Unit) {
                    View {
                        attr {
                            height(44f)
                            backgroundColor(bgColor)
                            borderRadius(8f)
                            justifyContentCenter()
                            alignItemsCenter()
                            marginBottom(10f)
                            marginLeft(16f)
                            marginRight(16f)
                        }
                        event { click { onClick() } }
                        Text {
                            attr {
                                color(Color.WHITE)
                                fontSize(14f)
                                text(label)
                            }
                        }
                    }
                }

                fun toggleButton(label: String, isLoading: Boolean, color: Color, onClick: () -> Unit) {
                    View {
                        attr {
                            height(32f)
                            paddingLeft(14f)
                            paddingRight(14f)
                            borderRadius(16f)
                            backgroundColor(color)
                            justifyContentCenter()
                            alignItemsCenter()
                            marginBottom(8f)
                            marginLeft(16f)
                        }
                        event { click { onClick() } }
                        Text {
                            attr {
                                fontSize(12f)
                                color(Color.WHITE)
                                text(if (isLoading) "切换显示真实内容" else "切换回骨架屏")
                            }
                        }
                    }
                }

                // ================================================================
                // Loading Section
                // ================================================================
                sectionHeader("Loading 组件")

                View {
                    attr {
                        padding(16f)
                        flexDirectionColumn()
                    }
                    demoButton("全屏加载（点击显示）", Color(0xFF1976D2L)) { ctx.showFullScreen = true }
                    demoButton("局部加载（点击显示）", Color(0xFF388E3CL)) { ctx.showPartial = true }
                    demoButton("3 秒超时自动关闭", Color(0xFFF57C00L)) { ctx.showWithTimeout = true }
                    demoButton("自定义指示器尺寸 indicatorSize=3", Color(0xFF7B1FA2L)) { ctx.showCustomSize = true }
                    demoButton("自定义加载内容 customContent", Color(0xFF00796BL)) { ctx.showCustomContent = true }
                    demoButton("延迟 800ms 显示（快速操作不闪烁）", Color(0xFF37474FL)) { ctx.showWithDelay = true }

                    Loading {
                        attr {
                            visible(ctx.showPartial)
                            fullScreen(false)
                            loadingText("局部加载中…")
                        }
                        event { onTimeout { ctx.showPartial = false } }
                    }
                }

                // ================================================================
                // Skeleton Section
                // ================================================================
                sectionHeader("Skeleton 骨架屏 - 文章 (shimmer)")
                toggleButton("切换", ctx.skArticleLoading, Color(0xFF1677FFL)) { ctx.skArticleLoading = !ctx.skArticleLoading }
                Skeleton {
                    attr {
                        loading(ctx.skArticleLoading)
                        preset(SkeletonPreset.article)
                        animation(SkeletonAnimation.SHIMMER)
                        contentPadding(16f)
                        content {
                            View {
                                attr {
                                    padding(16f)
                                    flexDirectionColumn()
                                }
                                View {
                                    attr {
                                        height(160f)
                                        backgroundColor(Color(0xFFBBDEFBL))
                                        borderRadius(6f)
                                        marginBottom(12f)
                                        justifyContentCenter()
                                        alignItemsCenter()
                                    }
                                    Text { attr { fontSize(14f); color(Color(0xFF1976D2L)); text("图片已加载") } }
                                }
                                Text { attr { fontSize(16f); color(Color(0xFF212121L)); text("文章标题已加载") } }
                                Text { attr { fontSize(13f); color(Color(0xFF757575L)); marginTop(8f); text("正文内容第一行已加载完成，内容丰富。") } }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 用户资料 (pulse)")
                toggleButton("切换", ctx.skProfileLoading, Color(0xFF52C41AL)) { ctx.skProfileLoading = !ctx.skProfileLoading }
                Skeleton {
                    attr {
                        loading(ctx.skProfileLoading)
                        preset(SkeletonPreset.profile)
                        animation(SkeletonAnimation.PULSE)
                        contentPadding(16f)
                        content {
                            View {
                                attr { padding(16f); flexDirectionRow(); alignItemsCenter() }
                                View {
                                    attr {
                                        width(56f); height(56f); borderRadius(28f)
                                        backgroundColor(Color(0xFF4CAF50L))
                                        justifyContentCenter(); alignItemsCenter()
                                        marginRight(12f)
                                    }
                                    Text { attr { fontSize(20f); color(Color.WHITE); text("A") } }
                                }
                                View {
                                    attr { flexDirectionColumn() }
                                    Text { attr { fontSize(16f); color(Color(0xFF212121L)); text("Alice Chen") } }
                                    Text { attr { fontSize(13f); color(Color(0xFF757575L)); marginTop(4f); text("@alice · 前端工程师") } }
                                }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 列表 ×3 (shimmer)")
                toggleButton("切换", ctx.skListLoading, Color(0xFFFF9800L)) { ctx.skListLoading = !ctx.skListLoading }
                Skeleton {
                    attr {
                        loading(ctx.skListLoading)
                        preset(SkeletonPreset.listItem)
                        animation(SkeletonAnimation.SHIMMER)
                        repeatCount(3)
                        repeatSpacing(16f)
                        contentPadding(16f)
                        content {
                            View {
                                attr { padding(16f); flexDirectionColumn() }
                                for (name in listOf("Alice", "Bob", "Carol")) {
                                    View {
                                        attr { flexDirectionRow(); alignItemsCenter(); marginBottom(16f) }
                                        View {
                                            attr {
                                                width(44f); height(44f); borderRadius(22f)
                                                backgroundColor(Color(0xFF1677FFL))
                                                justifyContentCenter(); alignItemsCenter(); marginRight(12f)
                                            }
                                            Text { attr { fontSize(16f); color(Color.WHITE); text(name[0].toString()) } }
                                        }
                                        View {
                                            attr { flexDirectionColumn() }
                                            Text { attr { fontSize(15f); color(Color(0xFF212121L)); text(name) } }
                                            Text { attr { fontSize(12f); color(Color(0xFF9E9E9EL)); marginTop(4f); text("内容已加载完成") } }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 圆角样式 round=true (pulse)")
                toggleButton("切换", ctx.skRoundLoading, Color(0xFF9C27B0L)) { ctx.skRoundLoading = !ctx.skRoundLoading }
                Skeleton {
                    attr {
                        loading(ctx.skRoundLoading)
                        preset(SkeletonPreset.paragraph)
                        animation(SkeletonAnimation.PULSE)
                        round(true)
                        contentPadding(16f)
                        content {
                            View {
                                attr { padding(16f) }
                                Text { attr { fontSize(14f); color(Color(0xFF212121L)); text("段落内容已加载，显示为真实文字。") } }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 静态无动画 (none)")
                toggleButton("切换", ctx.skPulseLoading, Color(0xFF607D8BL)) { ctx.skPulseLoading = !ctx.skPulseLoading }
                Skeleton {
                    attr {
                        loading(ctx.skPulseLoading)
                        preset(SkeletonPreset.card)
                        animation(SkeletonAnimation.NONE)
                        contentPadding(16f)
                        content {
                            View {
                                attr { padding(16f); flexDirectionColumn() }
                                View {
                                    attr {
                                        height(120f)
                                        backgroundColor(Color(0xFFE8F5E9L))
                                        borderRadius(6f)
                                        marginBottom(10f)
                                        justifyContentCenter()
                                        alignItemsCenter()
                                    }
                                    Text { attr { fontSize(13f); color(Color(0xFF388E3CL)); text("卡片图片") } }
                                }
                                Text { attr { fontSize(15f); color(Color(0xFF212121L)); text("卡片标题已加载") } }
                                Text { attr { fontSize(13f); color(Color(0xFF757575L)); marginTop(6f); text("卡片描述文字。") } }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 自定义行配置")
                toggleButton("切换", ctx.skCustomLoading, Color(0xFF00BCD4L)) { ctx.skCustomLoading = !ctx.skCustomLoading }
                Skeleton {
                    attr {
                        loading(ctx.skCustomLoading)
                        rows(
                            SkeletonRow(height = 60f, isCircle = true),
                            SkeletonRow(0.45f, 20f, 4f),
                            SkeletonRow(0.30f, 13f),
                            SkeletonRow(1f, 1f, 0f, marginBottom = 12f), // thin divider line
                            SkeletonRow(1f, 80f, 6f),
                            SkeletonRow(0.9f, 14f),
                            SkeletonRow(0.65f, 14f),
                        )
                        animation(SkeletonAnimation.SHIMMER)
                        baseColor(Color(0xFFE3F2FDL))
                        highlightColor(Color(0xFFBBDEFBL))
                        contentPadding(16f)
                        content {
                            View {
                                attr { padding(16f) }
                                Text { attr { fontSize(14f); color(Color(0xFF1976D2L)); text("自定义骨架屏内容已加载完成。") } }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 暗色主题 SkeletonTheme.DARK (shimmer)")
                toggleButton("切换", ctx.skDarkLoading, Color(0xFF212121L)) { ctx.skDarkLoading = !ctx.skDarkLoading }
                View {
                    attr {
                        backgroundColor(Color(0xFF1A1A1AL))
                        padding(4f)
                    }
                    Skeleton {
                        attr {
                            loading(ctx.skDarkLoading)
                            preset(SkeletonPreset.article)
                            animation(SkeletonAnimation.SHIMMER)
                            theme(SkeletonTheme.DARK)
                            contentPadding(16f)
                            content {
                                View {
                                    attr { padding(16f); flexDirectionColumn() }
                                    Text { attr { fontSize(15f); color(Color(0xFFEEEEEEL)); text("暗色主题内容已加载") } }
                                    Text { attr { fontSize(13f); color(Color(0xFFAAAAAL)); marginTop(8f); text("Dark theme content ready.") } }
                                }
                            }
                        }
                    }
                }

                sectionHeader("Skeleton 骨架屏 - 蓝色主题 SkeletonTheme.BLUE (pulse)")
                toggleButton("切换", ctx.skBlueLoading, Color(0xFF1677FFL)) { ctx.skBlueLoading = !ctx.skBlueLoading }
                Skeleton {
                    attr {
                        loading(ctx.skBlueLoading)
                        preset(SkeletonPreset.card)
                        animation(SkeletonAnimation.PULSE)
                        theme(SkeletonTheme.BLUE)
                        contentPadding(16f)
                        content {
                            View {
                                attr { padding(16f) }
                                Text { attr { fontSize(14f); color(Color(0xFF1677FFL)); text("蓝色主题内容已加载完成。") } }
                            }
                        }
                    }
                }

                // Bottom padding
                View { attr { height(32f) } }
            }

            // Full-screen Loading overlays (placed outside Scroller to render on top)
            Loading {
                attr {
                    visible(ctx.showFullScreen)
                    fullScreen(true)
                    loadingText("全屏加载中…")
                    maskColor(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.5f))
                    timeoutMs(3000)
                }
                event { onTimeout { ctx.showFullScreen = false } }
            }
            Loading {
                attr {
                    visible(ctx.showWithTimeout)
                    fullScreen(true)
                    loadingText("3s 后自动关闭")
                    timeoutMs(3000)
                }
                event { onTimeout { ctx.showWithTimeout = false } }
            }
            Loading {
                attr {
                    visible(ctx.showCustomSize)
                    fullScreen(true)
                    loadingText("自定义尺寸")
                    indicatorSize(3f)
                    timeoutMs(3000)
                }
                event { onTimeout { ctx.showCustomSize = false } }
            }
            Loading {
                attr {
                    visible(ctx.showCustomContent)
                    fullScreen(true)
                    timeoutMs(3000)
                    customContent {
                        ActivityIndicator {
                            attr { isGrayStyle(true) }
                        }
                        Text {
                            attr {
                                marginTop(8f)
                                fontSize(12f)
                                color(Color.WHITE)
                                text("自定义内容区域")
                            }
                        }
                    }
                }
                event { onTimeout { ctx.showCustomContent = false } }
            }
            Loading {
                attr {
                    visible(ctx.showWithDelay)
                    fullScreen(true)
                    loadingText("延迟后显示")
                    delayMs(800)
                    timeoutMs(3000)
                }
                event { onTimeout { ctx.showWithDelay = false } }
            }
        }
    }
}
