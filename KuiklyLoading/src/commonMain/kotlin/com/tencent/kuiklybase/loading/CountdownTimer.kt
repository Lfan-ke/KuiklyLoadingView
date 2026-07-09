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
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Canvas
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import kotlin.math.PI

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Visual style for [CountdownTimerView].
 *
 * - [TEXT]  - plain formatted string
 * - [COLON] - segmented boxes with colon separators (Vant-style blocks)
 * - [RING]  - Canvas circular arc depleting clockwise from top
 */
enum class CountdownStyle { TEXT, COLON, RING }

/**
 * Time display format for [CountdownTimerView].
 *
 * - [HMS]    - HH:MM:SS
 * - [MS]     - MM:SS
 * - [S]      - seconds only
 * - [CUSTOM] - custom pattern with {H}, {M}, {S} tokens
 */
enum class CountdownFormat { HMS, MS, S, CUSTOM }

class CountdownAttr : ComposeAttr() {

    internal var totalMs by observable(60_000L)
    internal var remainingMs by observable(60_000L)
    internal var running by observable(false)
    internal var style by observable(CountdownStyle.COLON)
    internal var format by observable(CountdownFormat.MS)
    internal var customFormat by observable("{M}:{S}")
    internal var fontSize by observable(20f)
    internal var color by observable(Color(0xFF1A1A1AL))
    internal var accentColor by observable(Color(0xFF1677FFL))
    internal var ringSize by observable(80f)
    internal var ringStrokeWidth by observable(6f)
    internal var autoStart by observable(false)

    fun totalMs(ms: Long) { totalMs = ms; remainingMs = ms }
    fun totalSeconds(s: Int) { totalMs(s * 1000L) }
    fun style(s: CountdownStyle) { style = s }
    fun format(f: CountdownFormat) { format = f }
    fun customFormat(f: String) { format = CountdownFormat.CUSTOM; customFormat = f }
    fun running(r: Boolean) { running = r }
    fun autoStart(a: Boolean) { autoStart = a }
    fun fontSize(s: Float) { fontSize = s }
    fun color(c: Color) { color = c }
    fun accentColor(c: Color) { accentColor = c }
    fun ringSize(s: Float) { ringSize = s }
    fun ringStrokeWidth(w: Float) { ringStrokeWidth = w }
}

class CountdownEvent : ComposeEvent() {
    var onTick: ((remainingMs: Long) -> Unit)? = null
    var onFinish: (() -> Unit)? = null
    fun onTick(block: (Long) -> Unit) { onTick = block }
    fun onFinish(block: () -> Unit) { onFinish = block }
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class CountdownTimerView : ComposeView<CountdownAttr, CountdownEvent>() {

    private var remainingMs by observable(0L)
    private var tickHandle = ""

    override fun createAttr(): CountdownAttr = CountdownAttr()
    override fun createEvent(): CountdownEvent = CountdownEvent()

    override fun didInit() {
        super.didInit()
        remainingMs = attr.totalMs
        if (attr.autoStart) start()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stop()
    }

    fun start() {
        if (attr.running) return
        attr.running = true
        tick()
    }

    fun pause() {
        attr.running = false
        stop()
    }

    fun reset() {
        stop()
        attr.running = false
        remainingMs = attr.totalMs
    }

    private fun tick() {
        tickHandle = setTimeout(pagerId, 100) {
            if (!attr.running) return@setTimeout
            remainingMs = (remainingMs - 100L).coerceAtLeast(0L)
            event.onTick?.invoke(remainingMs)
            if (remainingMs <= 0L) {
                attr.running = false
                event.onFinish?.invoke()
            } else {
                tick()
            }
        }
    }

    private fun stop() {
        if (tickHandle.isNotEmpty()) {
            clearTimeout(tickHandle)
            tickHandle = ""
        }
    }

    private fun formatTime(ms: Long): String {
        val s = (ms / 1000).toInt()
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return when (attr.format) {
            CountdownFormat.HMS    -> "${h.pad()}:${m.pad()}:${sec.pad()}"
            CountdownFormat.MS     -> "${m.pad()}:${sec.pad()}"
            CountdownFormat.S      -> sec.toString()
            CountdownFormat.CUSTOM -> attr.customFormat
                .replace("{H}", h.toString())
                .replace("{M}", m.pad())
                .replace("{S}", sec.pad())
        }
    }

    private fun Int.pad() = toString().padStart(2, '0')

    private fun buildSegments(ms: Long): List<String> {
        val s = (ms / 1000).toInt()
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return when (attr.format) {
            CountdownFormat.HMS    -> listOf(h.pad(), m.pad(), sec.pad())
            CountdownFormat.MS     -> listOf(m.pad(), sec.pad())
            CountdownFormat.S      -> listOf(sec.toString())
            CountdownFormat.CUSTOM -> listOf(formatTime(ms))
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            when (ctx.attr.style) {
                CountdownStyle.TEXT  -> ctx.renderText(this)
                CountdownStyle.COLON -> ctx.renderColon(this)
                CountdownStyle.RING  -> ctx.renderRing(this)
            }
        }
    }

    private fun renderText(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.Text {
            attr {
                text(ctx.formatTime(ctx.remainingMs))
                fontSize(ctx.attr.fontSize)
                color(ctx.attr.color)
                fontWeightMedium()
            }
        }
    }

    private fun renderColon(parent: ViewContainer<*, *>) {
        val ctx = this
        val segments = ctx.buildSegments(ctx.remainingMs)
        parent.View {
            attr { flexDirectionRow(); alignItems(FlexAlign.CENTER) }
            segments.forEachIndexed { idx, seg ->
                if (idx > 0) {
                    Text {
                        attr {
                            text(":")
                            fontSize(ctx.attr.fontSize)
                            color(ctx.attr.accentColor)
                            fontWeightBold()
                            paddingLeft(4f)
                            paddingRight(4f)
                        }
                    }
                }
                View {
                    attr {
                        minWidth(ctx.attr.fontSize * 1.8f)
                        paddingLeft(ctx.attr.fontSize * 0.4f)
                        paddingRight(ctx.attr.fontSize * 0.4f)
                        paddingTop(ctx.attr.fontSize * 0.2f)
                        paddingBottom(ctx.attr.fontSize * 0.2f)
                        borderRadius(6f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(ctx.attr.accentColor, 0f),
                            ColorStop(
                                Color(
                                    red255 = (ctx.attr.accentColor.red * 255 * 0.75f).toInt(),
                                    green255 = (ctx.attr.accentColor.green * 255 * 0.75f).toInt(),
                                    blue255 = (ctx.attr.accentColor.blue * 255 + 40).coerceAtMost(255).toInt(),
                                    alpha01 = 1f,
                                ),
                                1f,
                            ),
                        )
                        allCenter()
                    }
                    Text {
                        attr {
                            text(seg)
                            fontSize(ctx.attr.fontSize)
                            color(Color(0xFFFFFFFFL))
                            fontWeightBold()
                        }
                    }
                }
            }
        }
    }

    private fun renderRing(parent: ViewContainer<*, *>) {
        val ctx = this
        val fraction = if (ctx.attr.totalMs > 0L) {
            ctx.remainingMs.toFloat() / ctx.attr.totalMs.toFloat()
        } else 0f

        parent.Canvas({ attr { size(ctx.attr.ringSize, ctx.attr.ringSize) } }) { context, w, h ->
            val cx = w / 2f
            val cy = h / 2f
            val radius = minOf(w, h) / 2f - ctx.attr.ringStrokeWidth / 2f
            val fullCircle = (2.0 * PI).toFloat()
            val startRad = (-PI / 2.0).toFloat()

            // track ring
            context.beginPath()
            context.arc(cx, cy, radius, 0f, fullCircle)
            context.strokeStyle(Color(0xFFEEEEEEL))
            context.lineWidth(ctx.attr.ringStrokeWidth)
            context.stroke()

            // depleting arc (clockwise from top, representing remaining time)
            if (fraction > 0f) {
                val endRad = startRad + fullCircle * fraction
                context.beginPath()
                context.arc(cx, cy, radius, startRad, endRad)
                context.strokeStyle(ctx.attr.accentColor)
                context.lineWidth(ctx.attr.ringStrokeWidth)
                context.stroke()
            }

            // center time text
            val timeStr = ctx.formatTime(ctx.remainingMs)
            val fontSize = ctx.attr.ringSize * 0.18f
            context.font(fontSize)
            context.fillStyle(ctx.attr.color)
            context.fillText(timeStr, cx - timeStr.length * fontSize * 0.3f, cy + fontSize * 0.35f)
        }
    }
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.Countdown(init: CountdownTimerView.() -> Unit) {
    addChild(CountdownTimerView(), init)
}
