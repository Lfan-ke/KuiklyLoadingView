/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2026 Tencent. All rights reserved.
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

package com.tencent.kuiklybase.loading

import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Status attached to a single timeline event.
 * Controls node color, mirroring Ant Design Timeline / TDesign Timeline.
 */
enum class TimelineItemStatus {
    DEFAULT,
    SUCCESS,
    WARNING,
    ERROR,
    PENDING,
}

/**
 * A single event entry in a [TimelineView].
 *
 * @param time       Timestamp string displayed in the left column.
 * @param title      Primary event label.
 * @param description Secondary detail text (optional).
 * @param status     Node color indicator.
 * @param icon       Optional emoji/glyph rendered inside the node dot.
 */
data class TimelineItem(
    val time: String = "",
    val title: String,
    val description: String = "",
    val status: TimelineItemStatus = TimelineItemStatus.DEFAULT,
    val icon: String = "",
)

/**
 * Visual layout of timeline nodes.
 *
 * - [LEFT]       - node on the left, content on the right (classic Ant Design style)
 * - [ALTERNATE]  - nodes alternate sides (Ant Design `mode="alternate"`)
 * - [RIGHT]      - node on the right, content on the left (Ant Design `mode="right"`)
 */
enum class TimelineMode {
    LEFT,
    ALTERNATE,
    RIGHT,
}

/** Built-in color themes. */
enum class TimelineTheme {
    DEFAULT,
    DARK,
    BRAND,
}

// ---------------------------------------------------------------------------
// Attr
// ---------------------------------------------------------------------------

class TimelineAttr : ComposeAttr() {

    internal var items by observable(emptyList<TimelineItem>())
    internal var mode by observable(TimelineMode.LEFT)
    internal var showTime by observable(true)
    internal var pending by observable(false)
    internal var pendingText by observable("Loading…")

    // sizes
    internal var dotSize by observable(10f)
    internal var lineWidth by observable(2f)
    internal var timeFontSize by observable(11f)
    internal var titleFontSize by observable(14f)
    internal var descFontSize by observable(12f)
    internal var timeColumnWidth by observable(72f)

    // colors
    internal var defaultDotColor by observable(Color(0xFF1677FFL))
    internal var successDotColor by observable(Color(0xFF52C41AL))
    internal var warningDotColor by observable(Color(0xFFFAAD14L))
    internal var errorDotColor by observable(Color(0xFFFF4D4FL))
    internal var pendingDotColor by observable(Color(0xFFBFBFBFL))
    internal var lineColor by observable(Color(0xFFE8E8E8L))
    internal var timeColor by observable(Color(0xFF999999L))
    internal var titleColor by observable(Color(0xFF333333L))
    internal var descColor by observable(Color(0xFF999999L))

    fun items(list: List<TimelineItem>) { items = list }
    fun items(vararg item: TimelineItem) { items = item.toList() }
    fun mode(m: TimelineMode) { mode = m }
    fun showTime(show: Boolean) { showTime = show }
    fun pending(show: Boolean) { pending = show }
    fun pendingText(t: String) { pendingText = t }
    fun dotSize(s: Float) { dotSize = s }
    fun lineWidth(w: Float) { lineWidth = w }
    fun timeFontSize(s: Float) { timeFontSize = s }
    fun titleFontSize(s: Float) { titleFontSize = s }
    fun descFontSize(s: Float) { descFontSize = s }
    fun timeColumnWidth(w: Float) { timeColumnWidth = w }
    fun defaultDotColor(c: Color) { defaultDotColor = c }

    fun theme(t: TimelineTheme) {
        when (t) {
            TimelineTheme.DEFAULT -> {
                defaultDotColor = Color(0xFF1677FFL)
                lineColor = Color(0xFFE8E8E8L)
                titleColor = Color(0xFF333333L)
                timeColor = Color(0xFF999999L)
            }
            TimelineTheme.DARK -> {
                defaultDotColor = Color(0xFF4096FFL)
                lineColor = Color(0xFF3A3A3AL)
                titleColor = Color(0xFFEAEAEAL)
                timeColor = Color(0xFF888888L)
            }
            TimelineTheme.BRAND -> {
                defaultDotColor = Color(0xFF1677FFL)
                successDotColor = Color(0xFF00B96BL)
                lineColor = Color(0xFFD6E4FFL)
                titleColor = Color(0xFF0958D9L)
                timeColor = Color(0xFF4096FFL)
            }
        }
    }
}

class TimelineEvent : ComposeEvent() {
    var onItemClick: ((index: Int) -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class TimelineView : ComposeView<TimelineAttr, TimelineEvent>() {

    private var pendingTick by observable(false)
    private var pendingHandle = ""

    override fun createAttr(): TimelineAttr = TimelineAttr()
    override fun createEvent(): TimelineEvent = TimelineEvent()

    override fun didInit() {
        super.didInit()
        if (attr.pending) startPendingBlink()
    }

    override fun attr(init: TimelineAttr.() -> Unit) {
        val wasPending = attr.pending
        super.attr(init)
        if (attr.pending && !wasPending) startPendingBlink()
        if (!attr.pending && wasPending) stopPendingBlink()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopPendingBlink()
    }

    private fun startPendingBlink() {
        pendingHandle = com.tencent.kuikly.core.timer.setTimeout(pagerId, 700) {
            pendingTick = !pendingTick
            startPendingBlink()
        }
    }

    private fun stopPendingBlink() {
        if (pendingHandle.isNotEmpty()) {
            com.tencent.kuikly.core.timer.clearTimeout(pendingHandle)
            pendingHandle = ""
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr { flexDirectionColumn() }

                ctx.attr.items.forEachIndexed { idx, item ->
                    val isLast = idx == ctx.attr.items.lastIndex && !ctx.attr.pending
                    when (ctx.attr.mode) {
                        TimelineMode.LEFT      -> ctx.renderLeftItem(this, idx, item, isLast)
                        TimelineMode.RIGHT     -> ctx.renderRightItem(this, idx, item, isLast)
                        TimelineMode.ALTERNATE -> {
                            if (idx % 2 == 0) ctx.renderLeftItem(this, idx, item, isLast)
                            else              ctx.renderRightItem(this, idx, item, isLast)
                        }
                    }
                }

                // pending indicator at the bottom
                if (ctx.attr.pending) {
                    ctx.renderPendingItem(this)
                }
            }
        }
    }

    private fun renderLeftItem(
        parent: ViewContainer<*, *>,
        idx: Int,
        item: TimelineItem,
        isLast: Boolean,
    ) {
        val ctx = this
        parent.View {
            attr { flexDirectionRow(); alignItems(FlexAlign.FLEX_START) }
            event { click { ctx.event.onItemClick?.invoke(idx) } }

            // optional time column
            if (ctx.attr.showTime && item.time.isNotEmpty()) {
                View {
                    attr { width(ctx.attr.timeColumnWidth); paddingTop(2f) }
                    Text {
                        attr {
                            text(item.time)
                            fontSize(ctx.attr.timeFontSize)
                            color(ctx.attr.timeColor)
                        }
                    }
                }
            }

            // node + connector
            ctx.renderNodeColumn(parent = this, item = item, isLast = isLast)

            // content
            View {
                attr { flex(1f); flexDirectionColumn(); paddingLeft(12f); paddingBottom(if (isLast) 0f else 20f) }
                ctx.renderContent(this, item)
            }
        }
    }

    private fun renderRightItem(
        parent: ViewContainer<*, *>,
        idx: Int,
        item: TimelineItem,
        isLast: Boolean,
    ) {
        val ctx = this
        parent.View {
            attr { flexDirectionRow(); alignItems(FlexAlign.FLEX_START) }
            event { click { ctx.event.onItemClick?.invoke(idx) } }

            // content on the left
            View {
                attr { flex(1f); flexDirectionColumn(); paddingRight(12f); paddingBottom(if (isLast) 0f else 20f); alignItems(FlexAlign.FLEX_END) }
                ctx.renderContent(this, item)
            }

            // node + connector
            ctx.renderNodeColumn(parent = this, item = item, isLast = isLast)

            // optional time column
            if (ctx.attr.showTime && item.time.isNotEmpty()) {
                View {
                    attr { width(ctx.attr.timeColumnWidth); paddingTop(2f); alignItems(FlexAlign.FLEX_END) }
                    Text {
                        attr {
                            text(item.time)
                            fontSize(ctx.attr.timeFontSize)
                            color(ctx.attr.timeColor)
                        }
                    }
                }
            }
        }
    }

    private fun renderPendingItem(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.View {
            attr { flexDirectionRow(); alignItems(FlexAlign.FLEX_START) }

            if (ctx.attr.showTime) {
                View { attr { width(ctx.attr.timeColumnWidth) } }
            }

            // blinking pending dot
            View {
                attr { flexDirectionColumn(); alignItems(FlexAlign.CENTER) }
                val dotSize = ctx.attr.dotSize
                View {
                    attr {
                        size(dotSize, dotSize)
                        borderRadius(dotSize / 2f)
                        backgroundColor(ctx.attr.pendingDotColor)
                        opacity(if (ctx.pendingTick) 0.3f else 1f)
                        animate(Animation.easeInOut(0.5f), ctx.pendingTick)
                    }
                }
            }

            View {
                attr { flex(1f); paddingLeft(12f) }
                Text {
                    attr {
                        text(ctx.attr.pendingText)
                        fontSize(ctx.attr.titleFontSize)
                        color(ctx.attr.descColor)
                    }
                }
            }
        }
    }

    private fun renderNodeColumn(
        parent: ViewContainer<*, *>,
        item: TimelineItem,
        isLast: Boolean,
    ) {
        val ctx = this
        parent.View {
            attr { flexDirectionColumn(); alignItems(FlexAlign.CENTER) }

            val dotSize = ctx.attr.dotSize
            val dotColor = ctx.statusColor(item.status)

            if (item.icon.isNotEmpty()) {
                View {
                    attr {
                        size(dotSize * 2f, dotSize * 2f)
                        borderRadius(dotSize)
                        backgroundColor(dotColor)
                        allCenter()
                    }
                    Text {
                        attr { text(item.icon); fontSize(dotSize * 0.9f) }
                    }
                }
            } else {
                View {
                    attr {
                        size(dotSize, dotSize)
                        borderRadius(dotSize / 2f)
                        backgroundColor(dotColor)
                    }
                }
            }

            if (!isLast) {
                View {
                    attr {
                        flex(1f)
                        width(ctx.attr.lineWidth)
                        minHeight(24f)
                        marginTop(4f)
                        marginBottom(4f)
                        backgroundColor(ctx.attr.lineColor)
                    }
                }
            }
        }
    }

    private fun renderContent(parent: ViewContainer<*, *>, item: TimelineItem) {
        val ctx = this
        parent.View {
            attr { flexDirectionColumn() }
            Text {
                attr {
                    text(item.title)
                    fontSize(ctx.attr.titleFontSize)
                    fontWeightMedium()
                    color(ctx.attr.titleColor)
                }
            }
            if (item.description.isNotEmpty()) {
                Text {
                    attr {
                        text(item.description)
                        fontSize(ctx.attr.descFontSize)
                        color(ctx.attr.descColor)
                        marginTop(3f)
                    }
                }
            }
        }
    }

    private fun statusColor(status: TimelineItemStatus): Color = when (status) {
        TimelineItemStatus.DEFAULT -> attr.defaultDotColor
        TimelineItemStatus.SUCCESS -> attr.successDotColor
        TimelineItemStatus.WARNING -> attr.warningDotColor
        TimelineItemStatus.ERROR   -> attr.errorDotColor
        TimelineItemStatus.PENDING -> attr.pendingDotColor
    }
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.Timeline(init: TimelineView.() -> Unit) {
    addChild(TimelineView(), init)
}
