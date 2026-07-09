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
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Canvas
import kotlin.math.PI

fun ViewContainer<*, *>.CircularProgress(init: CircularProgressView.() -> Unit) {
    addChild(CircularProgressView(), init)
}

/** Pre-built color themes for CircularProgress. */
object CircularProgressTheme {
    data class Colors(val progressColor: Color, val gradientEndColor: Color)

    val Blue   = Colors(Color(0xFF1677FFL), Color(0xFF36CFDBL))
    val Green  = Colors(Color(0xFF52C41AL), Color(0xFF95DE64L))
    val Orange = Colors(Color(0xFFFA8C16L), Color(0xFFFFD666L))
    val Red    = Colors(Color(0xFFFF4D4FL), Color(0xFFFF7875L))
}

enum class CircularProgressStyle { SOLID, GRADIENT, DASHED }

class CircularProgressAttr : ComposeAttr() {
    internal var progress by observable(0f)
    internal var size by observable(80f)
    internal var strokeWidth by observable(8f)
    internal var progressColor by observable(Color(0xFF1677FFL))
    internal var gradientEndColor by observable(Color(0xFF36CFDBL))
    internal var trackColor by observable(Color(0xFFEEEEEEL))
    internal var style by observable(CircularProgressStyle.GRADIENT)
    internal var showPercent by observable(true)
    internal var centerLabel by observable("")
    internal var labelFontSize by observable(0f)
    internal var labelColor by observable(Color(0xFF262626L))
    internal var startAngleDeg by observable(-90f)
    internal var dashCount by observable(40)
    internal var clockwise by observable(true)
    internal var showTrack by observable(true)

    fun progress(v: Float) { progress = v.coerceIn(0f, 1f) }
    fun size(dp: Float) { size = dp.coerceIn(20f, 300f) }
    fun strokeWidth(w: Float) { strokeWidth = w.coerceAtLeast(1f) }
    fun progressColor(c: Color) { progressColor = c }
    fun gradientEndColor(c: Color) { gradientEndColor = c }
    fun trackColor(c: Color) { trackColor = c }
    fun trailColor(c: Color) { trackColor = c }
    fun style(s: CircularProgressStyle) { style = s }
    fun showPercent(show: Boolean) { showPercent = show }
    fun label(text: String) { centerLabel = text }
    fun labelFontSize(s: Float) { labelFontSize = s }
    fun labelColor(c: Color) { labelColor = c }
    fun startAngle(deg: Float) { startAngleDeg = deg }
    fun dashCount(n: Int) { dashCount = n.coerceIn(5, 100) }
    fun clockwise(cw: Boolean) { clockwise = cw }
    fun showTrack(show: Boolean) { showTrack = show }
    fun theme(t: CircularProgressTheme.Colors) {
        progressColor = t.progressColor
        gradientEndColor = t.gradientEndColor
    }
}

class CircularProgressEvent : ComposeEvent() {
    internal var onCompleteHandler: (() -> Unit)? = null
    fun onComplete(handler: () -> Unit) { onCompleteHandler = handler }
}

class CircularProgressView : ComposeView<CircularProgressAttr, CircularProgressEvent>() {
    private var prevProgress = -1f

    override fun createAttr(): CircularProgressAttr = CircularProgressAttr()
    override fun createEvent(): CircularProgressEvent = CircularProgressEvent()

    private fun lerpColor(from: Color, to: Color, t: Float): Color {
        val tc = t.coerceIn(0f, 1f)
        return Color(
            from.red + (to.red - from.red) * tc,
            from.green + (to.green - from.green) * tc,
            from.blue + (to.blue - from.blue) * tc,
            1f,
        )
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            Canvas({ attr { size(ctx.attr.size, ctx.attr.size) } }) { context, w, h ->
                val a = ctx.attr
                val cx = w / 2f
                val cy = h / 2f
                val radius = minOf(w, h) / 2f - a.strokeWidth / 2f
                val startRad = (a.startAngleDeg * PI / 180.0).toFloat()
                val fullCircle = (2.0 * PI).toFloat()
                val dir = if (a.clockwise) 1f else -1f

                if (a.showTrack) {
                    context.beginPath()
                    context.arc(cx, cy, radius, 0f, fullCircle)
                    context.strokeStyle(a.trackColor)
                    context.lineWidth(a.strokeWidth)
                    context.stroke()
                }

                if (a.progress > 0f) {
                    when (a.style) {
                        CircularProgressStyle.DASHED -> {
                            val gap = 0.15f / a.dashCount
                            val seg = (1f - gap * a.dashCount) / a.dashCount
                            val filled = (a.progress * a.dashCount).toInt()
                            for (i in 0 until filled) {
                                val s = startRad + dir * fullCircle * i * (seg + gap)
                                val e = s + dir * fullCircle * seg
                                val (arcS, arcE) = if (a.clockwise) s to e else e to s
                                context.beginPath()
                                context.arc(cx, cy, radius, arcS, arcE)
                                context.strokeStyle(a.progressColor)
                                context.lineWidth(a.strokeWidth)
                                context.stroke()
                            }
                        }
                        CircularProgressStyle.GRADIENT -> {
                            val segments = 36
                            val segLen = fullCircle * a.progress / segments
                            for (i in 0 until segments) {
                                val t = i.toFloat() / segments
                                val s = startRad + dir * fullCircle * a.progress * t
                                val e = s + dir * segLen
                                val (arcS, arcE) = if (a.clockwise) s to e else e to s
                                context.beginPath()
                                context.arc(cx, cy, radius, arcS, arcE)
                                context.strokeStyle(ctx.lerpColor(a.progressColor, a.gradientEndColor, t))
                                context.lineWidth(a.strokeWidth)
                                context.stroke()
                            }
                        }
                        CircularProgressStyle.SOLID -> {
                            val endRad = startRad + dir * fullCircle * a.progress
                            val (arcS, arcE) = if (a.clockwise) startRad to endRad else endRad to startRad
                            context.beginPath()
                            context.arc(cx, cy, radius, arcS, arcE)
                            context.strokeStyle(a.progressColor)
                            context.lineWidth(a.strokeWidth)
                            context.stroke()
                        }
                    }
                }

                val displayText = when {
                    a.centerLabel.isNotEmpty() -> a.centerLabel
                    a.showPercent -> "${(a.progress * 100).toInt()}%"
                    else -> ""
                }
                if (displayText.isNotEmpty()) {
                    val fontSize = if (a.labelFontSize > 0f) a.labelFontSize else a.size * 0.22f
                    context.font(fontSize)
                    context.fillStyle(a.labelColor)
                    context.fillText(displayText, cx - displayText.length * fontSize * 0.3f, cy + fontSize * 0.35f)
                }

                if (a.progress >= 1f && ctx.prevProgress < 1f) {
                    ctx.event.onCompleteHandler?.invoke()
                }
                ctx.prevProgress = a.progress
            }
        }
    }
}
