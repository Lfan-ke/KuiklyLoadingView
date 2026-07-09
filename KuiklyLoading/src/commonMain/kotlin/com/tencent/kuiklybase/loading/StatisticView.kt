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

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

class StatisticAttr : ComposeAttr() {

    internal var title by observable("")
    internal var value by observable(0.0)
    internal var prefix by observable("")
    internal var suffix by observable("")
    internal var precision by observable(0)
    internal var thousandSeparator by observable(true)
    internal var animate by observable(true)
    internal var animationDurationMs by observable(800)
    internal var titleColor by observable(Color(0xFF8C8C8CL))
    internal var valueColor by observable(Color(0xFF262626L))
    internal var prefixColor by observable(Color(0xFF262626L))
    internal var suffixColor by observable(Color(0xFF262626L))
    internal var titleFontSize by observable(14f)
    internal var valueFontSize by observable(28f)
    internal var prefixFontSize by observable(16f)
    internal var suffixFontSize by observable(14f)

    fun title(t: String) { title = t }
    fun value(v: Double) { value = v }
    fun value(v: Long) { value = v.toDouble() }
    fun value(v: Int) { value = v.toDouble() }
    fun prefix(p: String) { prefix = p }
    fun suffix(s: String) { suffix = s }
    fun precision(p: Int) { precision = p.coerceIn(0, 6) }
    fun thousandSeparator(enabled: Boolean) { thousandSeparator = enabled }
    fun animate(enabled: Boolean) { animate = enabled }
    fun animationDurationMs(ms: Int) { animationDurationMs = ms.coerceAtLeast(0) }
    fun titleColor(c: Color) { titleColor = c }
    fun valueColor(c: Color) { valueColor = c }
    fun prefixColor(c: Color) { prefixColor = c }
    fun suffixColor(c: Color) { suffixColor = c }
    fun titleFontSize(s: Float) { titleFontSize = s }
    fun valueFontSize(s: Float) { valueFontSize = s }
    fun prefixFontSize(s: Float) { prefixFontSize = s }
    fun suffixFontSize(s: Float) { suffixFontSize = s }
}

class StatisticEvent : ComposeEvent() {
    var onAnimationEnd: (() -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class StatisticView : ComposeView<StatisticAttr, StatisticEvent>() {

    // displayed value during count-up animation
    private var displayValue by observable(0.0)
    private var tickHandle = ""
    private var tickCount = 0
    private val totalTicks = 30

    override fun createAttr(): StatisticAttr = StatisticAttr()
    override fun createEvent(): StatisticEvent = StatisticEvent()

    override fun didInit() {
        super.didInit()
        startAnimation()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopAnimation()
    }

    override fun attr(init: StatisticAttr.() -> Unit) {
        val prevValue = attr.value
        super.attr(init)
        if (attr.value != prevValue) {
            stopAnimation()
            displayValue = 0.0
            tickCount = 0
            startAnimation()
        }
    }

    private fun startAnimation() {
        if (!attr.animate || attr.animationDurationMs <= 0) {
            displayValue = attr.value
            return
        }
        tick()
    }

    private fun tick() {
        val intervalMs = attr.animationDurationMs / totalTicks
        tickHandle = setTimeout(pagerId, intervalMs) {
            tickCount++
            val progress = tickCount.toDouble() / totalTicks
            // ease-out: progress^0.5 gives a deceleration effect
            val eased = Math.sqrt(progress).coerceAtMost(1.0)
            displayValue = attr.value * eased

            if (tickCount >= totalTicks) {
                displayValue = attr.value
                tickHandle = ""
                event.onAnimationEnd?.invoke()
            } else {
                tick()
            }
        }
    }

    private fun stopAnimation() {
        if (tickHandle.isNotEmpty()) {
            clearTimeout(tickHandle)
            tickHandle = ""
        }
    }

    private fun formatValue(v: Double): String {
        val intPart: Long
        val fracPart: String
        if (attr.precision > 0) {
            val factor = Math.pow(10.0, attr.precision.toDouble()).toLong()
            val rounded = (v * factor).toLong()
            intPart = rounded / factor
            val frac = rounded % factor
            fracPart = "." + frac.toString().padStart(attr.precision, '0')
        } else {
            intPart = v.toLong()
            fracPart = ""
        }

        val intStr = if (attr.thousandSeparator) {
            intPart.toString().reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()
        } else {
            intPart.toString()
        }

        return intStr + fracPart
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr { flexDirectionColumn() }

                if (ctx.attr.title.isNotEmpty()) {
                    Text {
                        attr {
                            text(ctx.attr.title)
                            fontSize(ctx.attr.titleFontSize)
                            color(ctx.attr.titleColor)
                            marginBottom(4f)
                        }
                    }
                }

                View {
                    attr {
                        flexDirectionRow()
                        alignItems(FlexAlign.BASELINE)
                    }

                    if (ctx.attr.prefix.isNotEmpty()) {
                        Text {
                            attr {
                                text(ctx.attr.prefix)
                                fontSize(ctx.attr.prefixFontSize)
                                color(ctx.attr.prefixColor)
                                marginRight(4f)
                            }
                        }
                    }

                    Text {
                        attr {
                            text(ctx.formatValue(ctx.displayValue))
                            fontSize(ctx.attr.valueFontSize)
                            color(ctx.attr.valueColor)
                            fontWeightSemiBold()
                        }
                    }

                    if (ctx.attr.suffix.isNotEmpty()) {
                        Text {
                            attr {
                                text(ctx.attr.suffix)
                                fontSize(ctx.attr.suffixFontSize)
                                color(ctx.attr.suffixColor)
                                marginLeft(4f)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.Statistic(init: StatisticView.() -> Unit) {
    addChild(StatisticView(), init)
}
