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
import com.tencent.kuiklybase.loading.CircularProgress
import com.tencent.kuiklybase.loading.CircularProgressStyle
import com.tencent.kuiklybase.loading.CircularProgressTheme
import com.tencent.kuiklybase.loading.Loading
import com.tencent.kuiklybase.loading.ResultType
import com.tencent.kuiklybase.loading.ResultView
import com.tencent.kuiklybase.loading.Skeleton
import com.tencent.kuiklybase.loading.SkeletonAnimation
import com.tencent.kuiklybase.loading.SkeletonPreset
import com.tencent.kuiklybase.loading.SkeletonRow
import com.tencent.kuiklybase.loading.ProgressBar
import com.tencent.kuiklybase.loading.ProgressBarShape
import com.tencent.kuiklybase.loading.SkeletonTheme
import com.tencent.kuiklybase.loading.LoadMore
import com.tencent.kuiklybase.loading.LoadMoreState
import com.tencent.kuiklybase.loading.LoadMoreStyle
import com.tencent.kuiklybase.loading.StepDirection
import com.tencent.kuiklybase.loading.StepItem
import com.tencent.kuiklybase.loading.StepProgress
import com.tencent.kuiklybase.loading.StepProgressView
import com.tencent.kuiklybase.loading.StepStatus
import com.tencent.kuiklybase.loading.StepStyleType
import com.tencent.kuiklybase.loading.Timeline
import com.tencent.kuiklybase.loading.TimelineItem
import com.tencent.kuiklybase.loading.TimelineItemStatus
import com.tencent.kuiklybase.loading.TimelineMode
import com.tencent.kuiklybase.loading.TimelineTheme
import com.tencent.kuiklybase.loading.Badge
import com.tencent.kuiklybase.loading.BadgeColor
import com.tencent.kuiklybase.loading.BadgeType
import com.tencent.kuiklybase.loading.CountDown
import com.tencent.kuiklybase.loading.CountDownFormat
import com.tencent.kuiklybase.loading.CountDownStyle
import com.tencent.kuiklybase.loading.Rate
import com.tencent.kuiklybase.loading.RateShape
import com.tencent.kuiklybase.loading.Tag
import com.tencent.kuiklybase.loading.TagColor
import com.tencent.kuiklybase.loading.TagGroup
import com.tencent.kuiklybase.loading.TagShape
import com.tencent.kuiklybase.loading.TagStyle
import com.tencent.kuiklybase.loading.Toast
import com.tencent.kuiklybase.loading.ToastIcon
import com.tencent.kuiklybase.loading.ToastPosition

@Page("LoadingViewDemoPage")
internal class LoadingViewDemoPage : BasePager() {

    // --- Loading state ---
    private var showFullScreen by observable(false)
    private var showPartial by observable(false)
    private var showWithTimeout by observable(false)
    private var showCustomSize by observable(false)
    private var showCustomContent by observable(false)
    private var showWithDelay by observable(false)

    // --- ProgressBar state ---
    private var progress1 by observable(0.3f)
    private var progress2 by observable(0.7f)
    private var progress3 by observable(0f)

    // --- Toast state ---
    private var toastVisible by observable(false)
    private var toastIcon by observable(ToastIcon.NONE)
    private var toastMsg by observable("")
    private var toastPos by observable(ToastPosition.CENTER)

    // --- CircularProgress state ---
    private var circProgress1 by observable(0.35f)
    private var circProgress2 by observable(0.65f)
    private var circProgress3 by observable(1f)

    // --- StepProgress state ---
    private var demoStep by observable(1)

    // --- LoadMore state ---
    private var loadMoreState by observable(LoadMoreState.IDLE)

    // --- Timeline state ---
    private var timelinePending by observable(true)

    // --- Rate state ---
    private var rateValue1 by observable(3f)
    private var rateValue2 by observable(2.5f)
    private var rateValue3 by observable(4f)

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

                // ================================================================
                // ProgressBar Section
                // ================================================================
                sectionHeader("ProgressBar 进度条")
                View {
                    attr { padding(16f); flexDirectionColumn() }
                    Text { attr { fontSize(12f); color(Color(0xFF888888L)); marginBottom(6f); text("蓝青渐变 (30%)") } }
                    ProgressBar {
                        attr {
                            progress(ctx.progress1)
                            showLabel(true)
                            trackHeight(10f)
                        }
                    }
                    View { attr { height(16f) } }
                    Text { attr { fontSize(12f); color(Color(0xFF888888L)); marginBottom(6f); text("绿色圆角 (70%)") } }
                    ProgressBar {
                        attr {
                            progress(ctx.progress2)
                            fillGradient(Color(0xFF52C41AL), Color(0xFF73D13DL))
                            shape(ProgressBarShape.ROUNDED)
                            showLabel(true)
                            trackHeight(8f)
                        }
                    }
                    View { attr { height(16f) } }
                    Text { attr { fontSize(12f); color(Color(0xFF888888L)); marginBottom(6f); text("闪烁条纹 + 按钮控制") } }
                    ProgressBar {
                        attr {
                            progress(ctx.progress3)
                            striped(true)
                            showLabel(true)
                            trackHeight(12f)
                        }
                    }
                    View { attr { height(10f) } }
                    View {
                        attr { flexDirectionRow(); marginBottom(4f) }
                        View {
                            attr {
                                height(36f); paddingLeft(16f); paddingRight(16f)
                                borderRadius(18f); backgroundColor(Color(0xFF888888L))
                                justifyContentCenter(); alignItemsCenter(); marginRight(12f)
                            }
                            event { click { ctx.progress3 = 0f } }
                            Text { attr { fontSize(13f); color(Color.WHITE); text("重置") } }
                        }
                        View {
                            attr {
                                height(36f); paddingLeft(16f); paddingRight(16f)
                                borderRadius(18f); backgroundColor(Color(0xFF1677FFL))
                                justifyContentCenter(); alignItemsCenter()
                            }
                            event { click { ctx.progress3 = (ctx.progress3 + 0.1f).coerceAtMost(1f) } }
                            Text { attr { fontSize(13f); color(Color.WHITE); text("+10%") } }
                        }
                    }
                }

                // ================================================================
                // Toast Section
                // ================================================================
                sectionHeader("Toast 轻提示")
                View {
                    attr { padding(16f); flexDirectionColumn() }

                    fun toastBtn(label: String, i: ToastIcon, msg: String, p: ToastPosition, bg: Color = Color(0xFF1677FFL)) {
                        View {
                            attr {
                                height(44f); backgroundColor(bg); borderRadius(8f)
                                justifyContentCenter(); alignItemsCenter(); marginBottom(10f)
                            }
                            event {
                                click {
                                    ctx.toastIcon = i; ctx.toastMsg = msg
                                    ctx.toastPos = p; ctx.toastVisible = true
                                }
                            }
                            Text { attr { color(Color.WHITE); fontSize(14f); text(label) } }
                        }
                    }

                    toastBtn("成功提示", ToastIcon.SUCCESS, "操作成功", ToastPosition.CENTER, Color(0xFF52C41AL))
                    toastBtn("失败提示", ToastIcon.FAIL, "操作失败", ToastPosition.CENTER, Color(0xFFF5222DL))
                    toastBtn("警告提示 (顶部)", ToastIcon.WARNING, "请注意操作", ToastPosition.TOP, Color(0xFFFA8C16L))
                    toastBtn("纯文字 (底部)", ToastIcon.NONE, "消息已发送", ToastPosition.BOTTOM, Color(0xFF595959L))
                }

                // ================================================================
                // ResultView Section
                // ================================================================
                sectionHeader("ResultView 结果页")

                ResultView {
                    attr {
                        resultType(ResultType.SUCCESS)
                        title("操作成功")
                        description("您的申请已提交，正在等待审核")
                        actionButton("查看详情", Color(0xFF52C41AL)) {}
                    }
                }
                ResultView {
                    attr {
                        resultType(ResultType.ERROR)
                        title("提交失败")
                        description("网络异常，请检查连接后重试")
                        actionButton("重 试") {}
                        secondaryButton("返 回") {}
                    }
                }
                ResultView {
                    attr {
                        resultType(ResultType.EMPTY)
                        title("暂无数据")
                        description("当前列表为空，请稍后再来")
                        padding(32f)
                    }
                }
                ResultView {
                    attr {
                        resultType(ResultType.NOT_FOUND)
                        title("页面不存在")
                        description("您访问的页面已被删除或不存在")
                        actionButton("返回首页") {}
                        padding(32f)
                    }
                }

                // ================================================================
                // CircularProgress Section
                // ================================================================
                sectionHeader("CircularProgress 环形进度")

                // Increment button
                View {
                    attr {
                        height(44f)
                        backgroundColor(Color(0xFF1677FFL))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                        marginLeft(16f)
                        marginRight(16f)
                    }
                    event {
                        click {
                            ctx.circProgress1 = if (ctx.circProgress1 >= 1f) 0f else (ctx.circProgress1 + 0.1f).coerceAtMost(1f)
                        }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(14f)
                            text("模拟加载 (点击递增 +10%)")
                        }
                    }
                }

                View {
                    attr {
                        flexDirectionRow()
                        justifyContentCenter()
                        alignItemsCenter()
                        padding(16f)
                        marginBottom(8f)
                    }
                    CircularProgress {
                        attr {
                            progress(ctx.circProgress1)
                            size(64f)
                            theme(CircularProgressTheme.Blue)
                            style(CircularProgressStyle.GRADIENT)
                            strokeWidth(6f)
                        }
                        event { onComplete { ctx.toastMsg = "加载完成！"; ctx.toastVisible = true } }
                    }
                    View { attr { width(24f) } }
                    CircularProgress {
                        attr {
                            progress(ctx.circProgress2)
                            size(80f)
                            theme(CircularProgressTheme.Green)
                            style(CircularProgressStyle.DASHED)
                            dashCount(36)
                            strokeWidth(7f)
                        }
                    }
                    View { attr { width(24f) } }
                    CircularProgress {
                        attr {
                            progress(ctx.circProgress3)
                            size(96f)
                            theme(CircularProgressTheme.Orange)
                            style(CircularProgressStyle.GRADIENT)
                            label("完成")
                            labelColor(Color(0xFFFA8C16L))
                            showPercent(false)
                            strokeWidth(8f)
                        }
                    }
                }

                // ── StepProgress ────────────────────────────────────────────
                View {
                    attr { height(1f); backgroundColor(Color(0xFFE8E8E8L)); marginVertical(16f) }
                }
                Text {
                    attr {
                        text("步骤条 StepProgress")
                        fontSize(16f)
                        fontWeightBold()
                        color(Color(0xFF333333L))
                        marginBottom(16f)
                        marginLeft(16f)
                    }
                }

                // DEFAULT horizontal
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text {
                        attr { text("默认样式 - 水平"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) }
                    }
                    StepProgress {
                        attr {
                            steps(
                                StepItem("已下单", "2026-07-01"),
                                StepItem("备货中", "等待仓库"),
                                StepItem("运输中"),
                                StepItem("已签收"),
                            )
                            current(ctx.demoStep)
                        }
                        event { onStepClick { idx -> ctx.demoStep = idx } }
                    }
                    View { attr { height(8f) } }
                    // tap buttons to move step
                    View {
                        attr { flexDirectionRow(); marginTop(8f) }
                        View {
                            attr {
                                height(32f)
                                paddingLeft(16f)
                                paddingRight(16f)
                                borderRadius(4f)
                                backgroundColor(Color(0xFF1677FFL))
                                allCenter()
                                marginRight(8f)
                                opacity(if (ctx.demoStep > 0) 1f else 0.4f)
                            }
                            event { click { if (ctx.demoStep > 0) ctx.demoStep-- } }
                            Text { attr { text("上一步"); fontSize(13f); color(Color(0xFFFFFFFFL)) } }
                        }
                        View {
                            attr {
                                height(32f)
                                paddingLeft(16f)
                                paddingRight(16f)
                                borderRadius(4f)
                                backgroundColor(Color(0xFF1677FFL))
                                allCenter()
                                opacity(if (ctx.demoStep < 3) 1f else 0.4f)
                            }
                            event { click { if (ctx.demoStep < 3) ctx.demoStep++ } }
                            Text { attr { text("下一步"); fontSize(13f); color(Color(0xFFFFFFFFL)) } }
                        }
                    }
                }

                // DOT horizontal
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text {
                        attr { text("点状样式 - 水平"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) }
                    }
                    StepProgress {
                        attr {
                            steps(
                                StepItem("购物车"),
                                StepItem("结算"),
                                StepItem("支付"),
                                StepItem("完成"),
                            )
                            current(ctx.demoStep)
                            styleType(StepStyleType.DOT)
                            dotSize(10f)
                        }
                    }
                }

                // NAVIGATION horizontal
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text {
                        attr { text("导航样式"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) }
                    }
                    StepProgress {
                        attr {
                            steps(
                                StepItem("基本信息"),
                                StepItem("认证材料"),
                                StepItem("审核确认"),
                            )
                            current(ctx.demoStep.coerceAtMost(2))
                            styleType(StepStyleType.NAVIGATION)
                            clickable(true)
                        }
                        event { onStepClick { idx -> ctx.demoStep = idx } }
                    }
                }

                // DEFAULT vertical
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text {
                        attr { text("默认样式 - 垂直"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) }
                    }
                    StepProgress {
                        attr {
                            steps(
                                StepItem("注册账号", "填写邮箱和密码"),
                                StepItem("完善资料", "姓名、头像等"),
                                StepItem("实名认证", "需上传证件"),
                                StepItem("开通服务"),
                            )
                            current(ctx.demoStep)
                            direction(StepDirection.VERTICAL)
                            clickable(true)
                        }
                        event { onStepClick { idx -> ctx.demoStep = idx } }
                    }
                }

                // Error state demo
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text {
                        attr { text("错误状态示例"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) }
                    }
                    StepProgress {
                        attr {
                            steps(
                                StepItem("已提交", status = StepStatus.FINISH),
                                StepItem("审核失败", status = StepStatus.ERROR),
                                StepItem("补充材料", status = StepStatus.WAIT),
                                StepItem("完成", status = StepStatus.WAIT),
                            )
                            styleType(StepStyleType.DEFAULT)
                        }
                    }
                }

                // Custom color - DOT vertical
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text {
                        attr { text("自定义颜色 - 点状垂直"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) }
                    }
                    StepProgress {
                        attr {
                            steps(
                                StepItem("设计", "UI/UX 评审通过"),
                                StepItem("开发", "前后端并行"),
                                StepItem("测试"),
                                StepItem("上线"),
                            )
                            current(2)
                            direction(StepDirection.VERTICAL)
                            styleType(StepStyleType.DOT)
                            finishColor(Color(0xFF52C41AL))
                            processColor(Color(0xFF1677FFL))
                            waitColor(Color(0xFFD9D9D9L))
                        }
                    }
                }

                // ── LoadMore ─────────────────────────────────────────────────
                View {
                    attr { height(1f); backgroundColor(Color(0xFFE8E8E8L)); marginVertical(16f) }
                }
                Text {
                    attr {
                        text("加载更多 LoadMore")
                        fontSize(16f)
                        fontWeightBold()
                        color(Color(0xFF333333L))
                        marginBottom(16f)
                        marginLeft(16f)
                    }
                }

                // icon style
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(8f) }
                    Text { attr { text("图标样式"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(4f) } }
                    LoadMore {
                        attr {
                            state(ctx.loadMoreState)
                            style(LoadMoreStyle.ICON)
                        }
                        event {
                            onLoadMore {
                                ctx.loadMoreState = LoadMoreState.LOADING
                                setTimeout(pagerId, 2000) {
                                    ctx.loadMoreState = LoadMoreState.SUCCESS
                                    setTimeout(pagerId, 800) { ctx.loadMoreState = LoadMoreState.IDLE }
                                }
                            }
                        }
                    }
                }

                // divider style
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(8f) }
                    Text { attr { text("分隔线样式"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(4f) } }
                    LoadMore {
                        attr {
                            state(LoadMoreState.NO_MORE)
                            style(LoadMoreStyle.DIVIDER)
                        }
                    }
                }

                // error state
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(8f) }
                    Text { attr { text("错误状态 (点击重试)"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(4f) } }
                    LoadMore {
                        attr {
                            state(LoadMoreState.ERROR)
                            style(LoadMoreStyle.DEFAULT)
                        }
                    }
                }

                // ── Timeline ─────────────────────────────────────────────────
                View {
                    attr { height(1f); backgroundColor(Color(0xFFE8E8E8L)); marginVertical(16f) }
                }
                Text {
                    attr {
                        text("时间轴 Timeline")
                        fontSize(16f)
                        fontWeightBold()
                        color(Color(0xFF333333L))
                        marginBottom(16f)
                        marginLeft(16f)
                    }
                }

                // left mode with time column + pending
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text { attr { text("左侧模式 + pending"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Timeline {
                        attr {
                            items(
                                TimelineItem("10:00", "订单创建", "用户提交订单 #20260701", TimelineItemStatus.SUCCESS),
                                TimelineItem("10:05", "支付成功", "微信支付 ¥299.00", TimelineItemStatus.SUCCESS),
                                TimelineItem("14:30", "仓库备货", "深圳仓库出库"),
                                TimelineItem("次日", "运输中", "已交由顺丰快递", TimelineItemStatus.WARNING),
                            )
                            pending(ctx.timelinePending)
                            pendingText("等待签收…")
                        }
                    }
                }

                // alternate mode
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text { attr { text("交替布局"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Timeline {
                        attr {
                            items(
                                TimelineItem("2024 Q1", "成立公司", "在深圳注册"),
                                TimelineItem("2024 Q2", "产品上线", "第一版发布", TimelineItemStatus.SUCCESS),
                                TimelineItem("2024 Q3", "融资 A 轮", "完成 5000 万", TimelineItemStatus.SUCCESS),
                                TimelineItem("2025 Q1", "海外扩张", "进入东南亚市场"),
                                TimelineItem("2025 Q4", "IPO", "预计年底上市", TimelineItemStatus.PENDING),
                            )
                            mode(TimelineMode.ALTERNATE)
                            showTime(false)
                        }
                    }
                }

                // error state with icon
                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(20f) }
                    Text { attr { text("自定义图标 + 错误状态"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Timeline {
                        attr {
                            items(
                                TimelineItem("", "提交申请", icon = "📝", status = TimelineItemStatus.SUCCESS),
                                TimelineItem("", "初审通过", icon = "✅", status = TimelineItemStatus.SUCCESS),
                                TimelineItem("", "审核失败", icon = "❌", status = TimelineItemStatus.ERROR, description = "材料不完整，请补充"),
                                TimelineItem("", "补充材料", icon = "📎", status = TimelineItemStatus.PENDING),
                            )
                            showTime(false)
                            theme(TimelineTheme.BRAND)
                        }
                    }
                }

                // ── Tag ─────────────────────────────────────────────────────
                View {
                    attr { flexDirectionColumn(); marginBottom(24f) }
                    Text {
                        attr {
                            text("标签 Tag")
                            fontSize(16f)
                            fontWeightSemiBold()
                            color(Color(0xFF1A1A1AL))
                            marginLeft(16f)
                            marginBottom(12f)
                        }
                    }

                    // styles row
                    View {
                        attr { flexDirectionRow(); marginHorizontal(16f); marginBottom(8f) }
                        Tag {
                            attr { text("Filled"); color(TagColor.PRIMARY); marginRight(8f) }
                        }
                        Tag {
                            attr { text("Outline"); color(TagColor.PRIMARY); style(TagStyle.OUTLINE); marginRight(8f) }
                        }
                        Tag {
                            attr { text("Ghost"); color(TagColor.PRIMARY); style(TagStyle.GHOST); marginRight(8f) }
                        }
                        Tag {
                            attr { text("Plain"); color(TagColor.PRIMARY); style(TagStyle.PLAIN) }
                        }
                    }

                    // shape row
                    View {
                        attr { flexDirectionRow(); marginHorizontal(16f); marginBottom(8f) }
                        Tag {
                            attr { text("Rounded"); color(TagColor.SUCCESS); marginRight(8f) }
                        }
                        Tag {
                            attr { text("Pill"); color(TagColor.SUCCESS); shape(TagShape.PILL); marginRight(8f) }
                        }
                        Tag {
                            attr { text("Square"); color(TagColor.SUCCESS); shape(TagShape.SQUARE) }
                        }
                    }

                    // color presets
                    TagGroup(8f) {
                        attr { marginHorizontal(16f); marginBottom(8f) }
                        listOf(TagColor.PRIMARY, TagColor.SUCCESS, TagColor.WARNING, TagColor.DANGER,
                            TagColor.INFO, TagColor.PURPLE, TagColor.CYAN, TagColor.GOLD, TagColor.LIME)
                            .forEach { c ->
                                Tag {
                                    attr {
                                        text(c.name)
                                        color(c)
                                        style(TagStyle.GHOST)
                                        marginRight(8f)
                                        marginBottom(8f)
                                    }
                                }
                            }
                    }

                    // closable + icon
                    View {
                        attr { flexDirectionRow(); marginHorizontal(16f) }
                        Tag {
                            attr { text("可关闭"); icon("🔖"); color(TagColor.WARNING); closable(true); marginRight(8f) }
                        }
                        Tag {
                            attr { text("可选中"); color(TagColor.PURPLE); checkable(true); style(TagStyle.OUTLINE) }
                        }
                    }
                }

                // ── Badge ────────────────────────────────────────────────────
                View {
                    attr { flexDirectionColumn(); marginBottom(24f) }
                    Text {
                        attr {
                            text("徽章 Badge")
                            fontSize(16f)
                            fontWeightSemiBold()
                            color(Color(0xFF1A1A1AL))
                            marginLeft(16f)
                            marginBottom(12f)
                        }
                    }

                    View {
                        attr { flexDirectionRow(); flexWrap(com.tencent.kuikly.core.layout.FlexWrap.WRAP); marginHorizontal(16f); marginBottom(8f) }
                        // count badges
                        listOf(3, 99, 128).forEachIndexed { i, n ->
                            Badge {
                                attr {
                                    type(BadgeType.COUNT)
                                    count(n)
                                    color(BadgeColor.RED)
                                    marginRight(16f)
                                }
                            }
                        }
                        // dot badge
                        Badge {
                            attr { type(BadgeType.DOT); color(BadgeColor.GREEN); marginRight(16f) }
                        }
                        // text badge
                        Badge {
                            attr { type(BadgeType.TEXT); text("NEW"); color(BadgeColor.BLUE); marginRight(16f) }
                        }
                        Badge {
                            attr { type(BadgeType.TEXT); text("HOT"); color(BadgeColor.ORANGE) }
                        }
                    }

                    // ribbon badges on cards
                    View {
                        attr { flexDirectionRow(); marginHorizontal(16f) }
                        listOf(BadgeColor.RED to "推荐", BadgeColor.PURPLE to "新品", BadgeColor.GREEN to "优惠")
                            .forEach { (c, lbl) ->
                                View {
                                    attr {
                                        width(80f); height(60f)
                                        borderRadius(8f)
                                        backgroundColor(Color(0xFFF5F5F5L))
                                        marginRight(12f)
                                        overflow(false)
                                    }
                                    Badge {
                                        attr { type(BadgeType.RIBBON); text(lbl); color(c) }
                                    }
                                }
                            }
                    }
                }

                // ── CountDown ─────────────────────────────────────────────────
                View {
                    attr { height(1f); backgroundColor(Color(0xFFE8E8E8L)); marginVertical(16f) }
                }
                Text {
                    attr {
                        text("倒计时 CountDown")
                        fontSize(16f)
                        fontWeightBold()
                        color(Color(0xFF333333L))
                        marginBottom(16f)
                        marginLeft(16f)
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("纯文本样式"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    CountDown {
                        attr {
                            totalSeconds(3600L)
                            format(CountDownFormat.HH_MM_SS)
                            style(CountDownStyle.PLAIN)
                            fontSize(20f)
                            textColor(Color(0xFF1677FFL))
                        }
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("方块样式"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    CountDown {
                        attr {
                            totalSeconds(86400L)
                            format(CountDownFormat.HH_MM_SS)
                            style(CountDownStyle.BLOCK)
                            blockBg(Color(0xFF1677FFL))
                            blockFontSize(20f)
                        }
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("带冒号的方块样式"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    CountDown {
                        attr {
                            totalSeconds(300L)
                            format(CountDownFormat.MM_SS)
                            style(CountDownStyle.COLON)
                            blockBg(Color(0xFFFF4D4FL))
                            separatorColor(Color(0xFFFF4D4FL))
                            blockFontSize(18f)
                        }
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("毫秒级 + 循环"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    CountDown {
                        attr {
                            totalSeconds(10L)
                            format(CountDownFormat.HH_MM_SS_MS)
                            style(CountDownStyle.PLAIN)
                            loop(true)
                            textColor(Color(0xFF52C41AL))
                            fontSize(16f)
                        }
                    }
                }

                // ── Rate ─────────────────────────────────────────────────────
                View {
                    attr { height(1f); backgroundColor(Color(0xFFE8E8E8L)); marginVertical(16f) }
                }
                Text {
                    attr {
                        text("评分 Rate")
                        fontSize(16f)
                        fontWeightBold()
                        color(Color(0xFF333333L))
                        marginBottom(16f)
                        marginLeft(16f)
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("基础星级"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Rate {
                        attr { value(ctx.rateValue1); showScore(true) }
                        event { onChange { v -> ctx.rateValue1 = v } }
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("半星 + 心形"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Rate {
                        attr {
                            value(ctx.rateValue2)
                            allowHalf(true)
                            shape(RateShape.HEART)
                            filledColor(Color(0xFFFF4D4FL))
                            size(28f)
                            showScore(true)
                        }
                        event { onChange { v -> ctx.rateValue2 = v } }
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("只读 + 大尺寸"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Rate {
                        attr {
                            value(ctx.rateValue3)
                            readonly(true)
                            size(36f)
                            filledColor(Color(0xFFFAAD14L))
                            count(10)
                            shape(RateShape.CIRCLE)
                        }
                    }
                }

                View {
                    attr { flexDirectionColumn(); marginHorizontal(16f); marginBottom(12f) }
                    Text { attr { text("自定义字符"); fontSize(12f); color(Color(0xFF999999L)); marginBottom(8f) } }
                    Rate {
                        attr {
                            value(3f)
                            filledChar("🔥")
                            emptyChar("⬜")
                            size(24f)
                            readonly(true)
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
            Toast {
                attr {
                    visible(ctx.toastVisible)
                    message(ctx.toastMsg)
                    icon(ctx.toastIcon)
                    position(ctx.toastPos)
                    durationMs(2000)
                }
                event { onDismiss { ctx.toastVisible = false } }
            }
        }
    }
}
