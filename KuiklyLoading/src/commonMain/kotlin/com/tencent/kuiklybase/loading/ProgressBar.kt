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
import com.tencent.kuikly.core.base.ColorStop
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.Direction
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import kotlin.math.abs

fun ViewContainer<*, *>.ProgressBar(init: ProgressBarView.() -> Unit) {
    addChild(ProgressBarView(), init)
}

enum class ProgressBarShape {
    ROUNDED,
    SQUARE,
}

class ProgressBarAttr : ComposeAttr() {
    internal var progress by observable(0f)
    internal var trackColor by observable(Color(0xFFEEEEEEL))
    internal var fillColorStart by observable(Color(0xFF1677FFL))
    internal var fillColorEnd by observable(Color(0xFF36CFDBL))
    internal var trackHeight by observable(8f)
    internal var shape by observable(ProgressBarShape.ROUNDED)
    internal var showLabel by observable(false)
    internal var labelColor by observable(Color(0xFF1677FFL))
    internal var labelFontSize by observable(12f)
    internal var striped by observable(false)
    internal var animated by observable(true)
    internal var animStepMs by observable(16)

    fun progress(v: Float) { progress = v.coerceIn(0f, 1f) }
    fun trackColor(c: Color) { trackColor = c }
    fun fillColor(c: Color) { fillColorStart = c; fillColorEnd = c }
    fun fillGradient(start: Color, end: Color) { fillColorStart = start; fillColorEnd = end }
    fun trackHeight(h: Float) { trackHeight = h.coerceAtLeast(2f) }
    fun shape(s: ProgressBarShape) { shape = s }
    fun showLabel(show: Boolean) { showLabel = show }
    fun labelColor(c: Color) { labelColor = c }
    fun labelFontSize(size: Float) { labelFontSize = size }
    fun striped(enable: Boolean) { striped = enable }
    fun animated(enable: Boolean) { animated = enable }
}

class ProgressBarView : ComposeView<ProgressBarAttr, ComposeEvent>() {

    private var displayProgress by observable(0f)
    private var animHandle = ""
    private var stripeTick by observable(false)
    private var stripeHandle = ""

    override fun createAttr(): ProgressBarAttr = ProgressBarAttr()

    override fun didInit() {
        super.didInit()
        displayProgress = attr.progress
        if (attr.striped) startStripe()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopAnim()
        stopStripe()
    }

    override fun attr(init: ProgressBarAttr.() -> Unit) {
        val prevProgress = attr.progress
        val prevStriped = attr.striped
        super.attr(init)
        if (attr.progress != prevProgress) {
            if (attr.animated) {
                stopAnim()
                animateTo(attr.progress)
            } else {
                displayProgress = attr.progress
            }
        }
        if (attr.striped != prevStriped) {
            stopStripe()
            if (attr.striped) startStripe()
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flexDirectionRow()
                    alignItemsCenter()
                }
                View {
                    attr {
                        flex(1f)
                        height(ctx.attr.trackHeight)
                        val r = if (ctx.attr.shape == ProgressBarShape.ROUNDED) ctx.attr.trackHeight / 2f else 0f
                        borderRadius(r)
                        backgroundColor(ctx.attr.trackColor)
                        flexDirectionRow()
                    }
                    val pct = ctx.displayProgress.coerceIn(0f, 1f)
                    if (pct > 0f) {
                        View {
                            attr {
                                height(ctx.attr.trackHeight)
                                val r = if (ctx.attr.shape == ProgressBarShape.ROUNDED) ctx.attr.trackHeight / 2f else 0f
                                borderRadius(r)
                                flex(pct)
                                val startColor = if (ctx.attr.striped && ctx.stripeTick)
                                    Color(red255 = 22, green255 = 119, blue255 = 255, alpha01 = 0.75f)
                                else ctx.attr.fillColorStart
                                val endColor = if (ctx.attr.striped && ctx.stripeTick)
                                    Color(red255 = 54, green255 = 207, blue255 = 219, alpha01 = 0.75f)
                                else ctx.attr.fillColorEnd
                                backgroundLinearGradient(
                                    Direction.TO_RIGHT,
                                    ColorStop(startColor, 0f),
                                    ColorStop(endColor, 1f),
                                )
                            }
                        }
                    }
                    val remaining = 1f - pct
                    if (remaining > 0.001f) {
                        View { attr { flex(remaining) } }
                    }
                }
                if (ctx.attr.showLabel) {
                    Text {
                        attr {
                            fontSize(ctx.attr.labelFontSize)
                            color(ctx.attr.labelColor)
                            marginLeft(8f)
                            text("${(ctx.displayProgress * 100).toInt()}%")
                        }
                    }
                }
            }
        }
    }

    private fun animateTo(target: Float) {
        val step = if (target > displayProgress) 0.04f else -0.04f
        val next = (displayProgress + step).coerceIn(0f, 1f)
        displayProgress = next
        if (abs(next - target) > 0.005f) {
            animHandle = setTimeout(pagerId, attr.animStepMs) { animateTo(target) }
        } else {
            displayProgress = target
        }
    }

    private fun stopAnim() {
        if (animHandle.isNotEmpty()) {
            clearTimeout(animHandle)
            animHandle = ""
        }
    }

    private fun startStripe() {
        stripeHandle = setTimeout(pagerId, 600) {
            stripeTick = !stripeTick
            startStripe()
        }
    }

    private fun stopStripe() {
        if (stripeHandle.isNotEmpty()) {
            clearTimeout(stripeHandle)
            stripeHandle = ""
        }
    }
}
