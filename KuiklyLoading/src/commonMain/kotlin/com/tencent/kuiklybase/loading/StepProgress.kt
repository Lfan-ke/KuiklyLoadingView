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
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
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
// Public enums and data types
// ---------------------------------------------------------------------------

/** Status of an individual step - mirrors Ant Design / TDesign / NutUI step states. */
enum class StepStatus {
    /** Not yet reached. */
    WAIT,
    /** Currently active / in-progress. */
    PROCESS,
    /** Completed successfully. */
    FINISH,
    /** Completed with an error. */
    ERROR,
}

/** Layout direction of the step rail. */
enum class StepDirection {
    HORIZONTAL,
    VERTICAL,
}

/**
 * Visual style variant.
 *
 * - [DEFAULT] - numbered circle nodes joined by a line (Ant Design / Element Plus style)
 * - [DOT]     - small dot nodes, compact (NutUI / TDesign mini style)
 * - [NAVIGATION] - full-width arrow-tab bar (Vant / TDesign navigation steps)
 */
enum class StepStyleType {
    DEFAULT,
    DOT,
    NAVIGATION,
}

/**
 * A single step definition.
 *
 * @param title    Primary label shown below/beside the node.
 * @param description  Optional secondary label (shown in DEFAULT + VERTICAL).
 * @param status   Override the step's automatic status derived from [StepProgressAttr.current].
 *                 Pass null to use the automatic value.
 * @param icon     Optional single emoji / unicode glyph shown inside the node (DEFAULT style).
 */
data class StepItem(
    val title: String,
    val description: String = "",
    val status: StepStatus? = null,
    val icon: String = "",
)

// ---------------------------------------------------------------------------
// Attr
// ---------------------------------------------------------------------------

class StepProgressAttr : ComposeAttr() {

    internal var steps by observable(emptyList<StepItem>())
    internal var current by observable(0)
    internal var direction by observable(StepDirection.HORIZONTAL)
    internal var styleType by observable(StepStyleType.DEFAULT)
    internal var clickable by observable(false)

    // colors
    internal var finishColor by observable(Color(0xFF1677FFL))
    internal var processColor by observable(Color(0xFF1677FFL))
    internal var waitColor by observable(Color(0xFFBFBFBFL))
    internal var errorColor by observable(Color(0xFFFF4D4FL))
    internal var finishLineColor by observable(Color(0xFF1677FFL))
    internal var waitLineColor by observable(Color(0xFFE8E8E8L))
    internal var titleColor by observable(Color(0xFF333333L))
    internal var titleActiveColor by observable(Color(0xFF1677FFL))
    internal var descColor by observable(Color(0xFF999999L))

    // sizes
    internal var nodeSize by observable(28f)
    internal var dotSize by observable(8f)
    internal var lineThickness by observable(2f)
    internal var titleFontSize by observable(13f)
    internal var descFontSize by observable(11f)
    internal var stepSpacing by observable(0f)   // 0 = flex distributed evenly

    fun steps(list: List<StepItem>) { steps = list }
    fun steps(vararg item: StepItem) { steps = item.toList() }
    fun current(idx: Int) { current = idx.coerceAtLeast(0) }
    fun direction(d: StepDirection) { direction = d }
    fun styleType(s: StepStyleType) { styleType = s }
    fun clickable(enabled: Boolean) { clickable = enabled }
    fun finishColor(c: Color) { finishColor = c; finishLineColor = c }
    fun processColor(c: Color) { processColor = c }
    fun waitColor(c: Color) { waitColor = c; waitLineColor = c }
    fun errorColor(c: Color) { errorColor = c }
    fun titleFontSize(s: Float) { titleFontSize = s }
    fun descFontSize(s: Float) { descFontSize = s }
    fun nodeSize(s: Float) { nodeSize = s }
    fun dotSize(s: Float) { dotSize = s }
    fun lineThickness(t: Float) { lineThickness = t }
    fun stepSpacing(s: Float) { stepSpacing = s.coerceAtLeast(0f) }
}

// ---------------------------------------------------------------------------
// Event
// ---------------------------------------------------------------------------

class StepProgressEvent : ComposeEvent() {
    var onStepClick: ((index: Int) -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class StepProgressView : ComposeView<StepProgressAttr, StepProgressEvent>() {

    // pulse tick for the current PROCESS node
    private var pulseTick by observable(false)

    override fun createAttr(): StepProgressAttr = StepProgressAttr()
    override fun createEvent(): StepProgressEvent = StepProgressEvent()

    override fun didInit() {
        super.didInit()
        startPulse()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        clearPulse()
    }

    private var pulseHandle = ""

    private fun startPulse() {
        pulseHandle = com.tencent.kuikly.core.timer.setTimeout(pagerId, 800) {
            pulseTick = !pulseTick
            startPulse()
        }
    }

    private fun clearPulse() {
        if (pulseHandle.isNotEmpty()) {
            com.tencent.kuikly.core.timer.clearTimeout(pulseHandle)
            pulseHandle = ""
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            when (ctx.attr.direction) {
                StepDirection.HORIZONTAL -> ctx.renderHorizontal(this)
                StepDirection.VERTICAL   -> ctx.renderVertical(this)
            }
        }
    }

    // -----------------------------------------------------------------------
    // Horizontal layout
    // -----------------------------------------------------------------------

    private fun renderHorizontal(container: ViewContainer<*, *>) {
        val ctx = this
        container.View {
            attr {
                flexDirectionRow()
                alignItems(FlexAlign.FLEX_START)
            }
            ctx.attr.steps.forEachIndexed { idx, step ->
                val status = ctx.resolveStatus(step, idx)
                when (ctx.attr.styleType) {
                    StepStyleType.DEFAULT    -> ctx.renderHDefaultStep(this, idx, step, status)
                    StepStyleType.DOT        -> ctx.renderHDotStep(this, idx, step, status)
                    StepStyleType.NAVIGATION -> ctx.renderNavStep(this, idx, step, status)
                }
            }
        }
    }

    private fun renderHDefaultStep(
        parent: ViewContainer<*, *>,
        idx: Int,
        step: StepItem,
        status: StepStatus,
    ) {
        val ctx = this
        val isLast = idx == ctx.attr.steps.lastIndex
        parent.View {
            attr {
                flex(1f)
                flexDirectionColumn()
                alignItems(FlexAlign.CENTER)
                if (ctx.attr.stepSpacing > 0f) paddingLeft(ctx.attr.stepSpacing / 2f).also { paddingRight(ctx.attr.stepSpacing / 2f) }
            }
            if (ctx.attr.clickable) event { click { ctx.event.onStepClick?.invoke(idx) } }

            // Row: left-line + node + right-line
            View {
                attr {
                    flexDirectionRow()
                    alignItems(FlexAlign.CENTER)
                    width(Float.MAX_VALUE) // fill parent flex
                }
                // left connector line
                if (idx > 0) {
                    View {
                        attr {
                            flex(1f)
                            height(ctx.attr.lineThickness)
                            backgroundColor(if (status == StepStatus.FINISH) ctx.attr.finishLineColor else ctx.attr.waitLineColor)
                        }
                    }
                } else {
                    View { attr { flex(1f) } }
                }

                ctx.renderDefaultNode(this, idx, step, status)

                // right connector line
                if (!isLast) {
                    View {
                        attr {
                            flex(1f)
                            height(ctx.attr.lineThickness)
                            backgroundColor(
                                if (status == StepStatus.FINISH) ctx.attr.finishLineColor
                                else ctx.attr.waitLineColor
                            )
                        }
                    }
                } else {
                    View { attr { flex(1f) } }
                }
            }

            // Title + description
            ctx.renderStepLabel(this, step, status, align = "center")
        }
    }

    private fun renderHDotStep(
        parent: ViewContainer<*, *>,
        idx: Int,
        step: StepItem,
        status: StepStatus,
    ) {
        val ctx = this
        val isLast = idx == ctx.attr.steps.lastIndex
        parent.View {
            attr {
                flex(1f)
                flexDirectionColumn()
                alignItems(FlexAlign.CENTER)
            }
            if (ctx.attr.clickable) event { click { ctx.event.onStepClick?.invoke(idx) } }

            View {
                attr { flexDirectionRow(); alignItems(FlexAlign.CENTER) }
                if (idx > 0) {
                    View {
                        attr {
                            flex(1f)
                            height(ctx.attr.lineThickness)
                            backgroundColor(if (status == StepStatus.FINISH) ctx.attr.finishLineColor else ctx.attr.waitLineColor)
                        }
                    }
                } else {
                    View { attr { flex(1f) } }
                }
                ctx.renderDotNode(this, status)
                if (!isLast) {
                    View {
                        attr {
                            flex(1f)
                            height(ctx.attr.lineThickness)
                            backgroundColor(if (status == StepStatus.FINISH) ctx.attr.finishLineColor else ctx.attr.waitLineColor)
                        }
                    }
                } else {
                    View { attr { flex(1f) } }
                }
            }
            ctx.renderStepLabel(this, step, status, align = "center")
        }
    }

    private fun renderNavStep(
        parent: ViewContainer<*, *>,
        idx: Int,
        step: StepItem,
        status: StepStatus,
    ) {
        val ctx = this
        val isLast = idx == ctx.attr.steps.lastIndex
        val bgColor = when (status) {
            StepStatus.FINISH  -> ctx.attr.finishColor
            StepStatus.PROCESS -> ctx.attr.processColor
            StepStatus.ERROR   -> ctx.attr.errorColor
            StepStatus.WAIT    -> ctx.attr.waitLineColor
        }
        parent.View {
            attr {
                flex(1f)
                height(40f)
                backgroundColor(bgColor)
                allCenter()
                if (!isLast) marginRight(1f)
                animate(Animation.easeInOut(0.2f), status)
            }
            if (ctx.attr.clickable) event { click { ctx.event.onStepClick?.invoke(idx) } }
            Text {
                attr {
                    text(step.title)
                    fontSize(ctx.attr.titleFontSize)
                    fontWeightMedium()
                    color(if (status == StepStatus.WAIT) Color(0xFF999999L) else Color(0xFFFFFFFFL))
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Vertical layout (DEFAULT and DOT only - navigation is inherently horizontal)
    // -----------------------------------------------------------------------

    private fun renderVertical(container: ViewContainer<*, *>) {
        val ctx = this
        container.View {
            attr { flexDirectionColumn() }
            ctx.attr.steps.forEachIndexed { idx, step ->
                val status = ctx.resolveStatus(step, idx)
                val isLast = idx == ctx.attr.steps.lastIndex
                View {
                    attr {
                        flexDirectionRow()
                        alignItems(FlexAlign.FLEX_START)
                        if (ctx.attr.stepSpacing > 0f) marginBottom(ctx.attr.stepSpacing)
                    }
                    if (ctx.attr.clickable) event { click { ctx.event.onStepClick?.invoke(idx) } }

                    // Left column: node + vertical connector
                    View {
                        attr { flexDirectionColumn(); alignItems(FlexAlign.CENTER) }
                        when (ctx.attr.styleType) {
                            StepStyleType.DOT -> ctx.renderDotNode(this, status)
                            else              -> ctx.renderDefaultNode(this, idx, step, status)
                        }
                        if (!isLast) {
                            View {
                                attr {
                                    width(ctx.attr.lineThickness)
                                    flex(1f)
                                    minHeight(32f)
                                    marginTop(4f)
                                    marginBottom(4f)
                                    backgroundColor(if (status == StepStatus.FINISH) ctx.attr.finishLineColor else ctx.attr.waitLineColor)
                                }
                            }
                        }
                    }

                    // Right column: title + description
                    View {
                        attr {
                            flex(1f)
                            flexDirectionColumn()
                            marginLeft(12f)
                            paddingBottom(if (isLast) 0f else 16f)
                        }
                        ctx.renderStepLabel(this, step, status, align = "left")
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Shared node renderers
    // -----------------------------------------------------------------------

    private fun renderDefaultNode(
        parent: ViewContainer<*, *>,
        idx: Int,
        step: StepItem,
        status: StepStatus,
    ) {
        val ctx = this
        val (bgColor, borderColor) = when (status) {
            StepStatus.FINISH  -> ctx.attr.finishColor to ctx.attr.finishColor
            StepStatus.PROCESS -> ctx.attr.processColor to ctx.attr.processColor
            StepStatus.ERROR   -> ctx.attr.errorColor to ctx.attr.errorColor
            StepStatus.WAIT    -> Color(0xFFFFFFFFL) to ctx.attr.waitColor
        }
        val nodeSize = ctx.attr.nodeSize
        val isProcess = status == StepStatus.PROCESS

        parent.View {
            attr {
                size(nodeSize, nodeSize)
                borderRadius(nodeSize / 2f)
                backgroundColor(bgColor)
                border(Border(if (status == StepStatus.WAIT) 1.5f else 0f, BorderStyle.SOLID, borderColor))
                allCenter()
                animate(Animation.easeInOut(0.25f), status)
                if (isProcess) opacity(if (ctx.pulseTick) 0.75f else 1f)
                if (isProcess) animate(Animation.easeInOut(0.6f), ctx.pulseTick)
            }

            val nodeText = when (status) {
                StepStatus.FINISH -> if (step.icon.isNotEmpty()) step.icon else "✓"
                StepStatus.ERROR  -> if (step.icon.isNotEmpty()) step.icon else "✕"
                else              -> if (step.icon.isNotEmpty()) step.icon else (idx + 1).toString()
            }
            Text {
                attr {
                    text(nodeText)
                    fontSize(nodeSize * 0.43f)
                    fontWeightSemiBold()
                    color(if (status == StepStatus.WAIT) ctx.attr.waitColor else Color(0xFFFFFFFFL))
                }
            }
        }
    }

    private fun renderDotNode(parent: ViewContainer<*, *>, status: StepStatus) {
        val ctx = this
        val color = when (status) {
            StepStatus.FINISH  -> ctx.attr.finishColor
            StepStatus.PROCESS -> ctx.attr.processColor
            StepStatus.ERROR   -> ctx.attr.errorColor
            StepStatus.WAIT    -> ctx.attr.waitColor
        }
        val size = if (status == StepStatus.PROCESS) ctx.attr.dotSize * 1.5f else ctx.attr.dotSize
        parent.View {
            attr {
                size(size, size)
                borderRadius(size / 2f)
                backgroundColor(color)
                animate(Animation.easeInOut(0.25f), status)
            }
        }
    }

    private fun renderStepLabel(
        parent: ViewContainer<*, *>,
        step: StepItem,
        status: StepStatus,
        align: String,
    ) {
        val ctx = this
        val isActive = status == StepStatus.PROCESS || status == StepStatus.FINISH
        parent.View {
            attr {
                flexDirectionColumn()
                alignItems(if (align == "center") FlexAlign.CENTER else FlexAlign.FLEX_START)
                marginTop(6f)
            }
            Text {
                attr {
                    text(step.title)
                    fontSize(ctx.attr.titleFontSize)
                    fontWeightMedium()
                    color(if (isActive) ctx.attr.titleActiveColor else ctx.attr.titleColor)
                    animate(Animation.easeInOut(0.2f), isActive)
                }
            }
            if (step.description.isNotEmpty()) {
                Text {
                    attr {
                        text(step.description)
                        fontSize(ctx.attr.descFontSize)
                        color(ctx.attr.descColor)
                        marginTop(2f)
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private fun resolveStatus(step: StepItem, idx: Int): StepStatus {
        step.status?.let { return it }
        return when {
            idx < attr.current  -> StepStatus.FINISH
            idx == attr.current -> StepStatus.PROCESS
            else                -> StepStatus.WAIT
        }
    }
}

// ---------------------------------------------------------------------------
// DSL entry point
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.StepProgress(init: StepProgressView.() -> Unit) {
    addChild(StepProgressView(), init)
}
