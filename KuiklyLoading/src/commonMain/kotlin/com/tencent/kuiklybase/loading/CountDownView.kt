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
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Display format for [CountDownView] - mirrors Vant CountDown / TDesign Countdown.
 *
 * - [HH_MM_SS]   - "HH:MM:SS"
 * - [MM_SS]       - "MM:SS"
 * - [SS]          - seconds only
 * - [HH_MM_SS_MS] - "HH:MM:SS.ss" (includes centiseconds)
 * - [CUSTOM]      - uses [CountDownAttr.format] pattern string
 *                   Tokens: {D} days, {H} hours, {M} minutes, {S} seconds, {MS} centiseconds
 */
enum class CountDownFormat {
    HH_MM_SS,
    MM_SS,
    SS,
    HH_MM_SS_MS,
    CUSTOM,
}

/**
 * Visual appearance of each time segment.
 *
 * - [PLAIN]   - plain text, segments separated by colons
 * - [BLOCK]   - each segment in a rounded block (Vant default style)
 * - [COLON]   - block segments with colon separators explicitly shown
 */
enum class CountDownStyle {
    PLAIN,
    BLOCK,
    COLON,
}

class CountDownAttr : ComposeAttr() {

    internal var totalMs by observable(60_000L)
    internal var autoStart by observable(true)
    internal var format by observable(CountDownFormat.HH_MM_SS)
    internal var customFormat by observable("{H}:{M}:{S}")
    internal var style by observable(CountDownStyle.PLAIN)
    internal var loop by observable(false)

    // PLAIN style
    internal var textColor by observable(Color(0xFF333333L))
    internal var fontSize by observable(16f)

    // BLOCK style
    internal var blockBg by observable(Color(0xFF1677FFL))
    internal var blockTextColor by observable(Color(0xFFFFFFFFL))
    internal var blockFontSize by observable(16f)
    internal var blockPaddingH by observable(8f)
    internal var blockPaddingV by observable(4f)
    internal var blockRadius by observable(4f)
    internal var separatorColor by observable(Color(0xFF1677FFL))
    internal var separatorFontSize by observable(16f)

    fun totalMs(ms: Long) { totalMs = ms.coerceAtLeast(0L) }
    fun totalSeconds(s: Long) { totalMs = s * 1000L }
    fun totalMinutes(m: Long) { totalMs = m * 60_000L }
    fun autoStart(a: Boolean) { autoStart = a }
    fun format(f: CountDownFormat) { format = f }
    fun customFormat(pattern: String) { customFormat = pattern; format = CountDownFormat.CUSTOM }
    fun style(s: CountDownStyle) { style = s }
    fun loop(l: Boolean) { loop = l }
    fun textColor(c: Color) { textColor = c }
    fun fontSize(s: Float) { fontSize = s }
    fun blockBg(c: Color) { blockBg = c }
    fun blockTextColor(c: Color) { blockTextColor = c }
    fun blockFontSize(s: Float) { blockFontSize = s }
    fun blockRadius(r: Float) { blockRadius = r }
    fun separatorColor(c: Color) { separatorColor = c }
}

class CountDownEvent : ComposeEvent() {
    var onChange: ((remainingMs: Long) -> Unit)? = null
    var onFinish: (() -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class CountDownView : ComposeView<CountDownAttr, CountDownEvent>() {

    private var remainingMs by observable(0L)
    private var running by observable(false)
    private var tickHandle = ""
    // animation tick for block style - alternates to trigger flip animation
    private var flipTick by observable(false)
    private var lastFlipSecond = -1L

    override fun createAttr(): CountDownAttr = CountDownAttr()
    override fun createEvent(): CountDownEvent = CountDownEvent()

    override fun didInit() {
        super.didInit()
        remainingMs = attr.totalMs
        if (attr.autoStart) start()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopTick()
    }

    fun start() {
        if (running) return
        running = true
        tick()
    }

    fun pause() {
        running = false
        stopTick()
    }

    fun reset(restartImmediately: Boolean = false) {
        stopTick()
        running = false
        remainingMs = attr.totalMs
        lastFlipSecond = -1L
        if (restartImmediately) start()
    }

    private fun tick() {
        tickHandle = setTimeout(pagerId, 100) {
            if (!running) return@setTimeout
            remainingMs = (remainingMs - 100L).coerceAtLeast(0L)
            event.onChange?.invoke(remainingMs)

            val currentSecond = remainingMs / 1000L
            if (currentSecond != lastFlipSecond) {
                lastFlipSecond = currentSecond
                flipTick = !flipTick
            }

            if (remainingMs <= 0L) {
                running = false
                event.onFinish?.invoke()
                if (attr.loop) {
                    remainingMs = attr.totalMs
                    lastFlipSecond = -1L
                    start()
                }
            } else {
                tick()
            }
        }
    }

    private fun stopTick() {
        if (tickHandle.isNotEmpty()) {
            clearTimeout(tickHandle)
            tickHandle = ""
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            when (ctx.attr.style) {
                CountDownStyle.PLAIN  -> ctx.renderPlain(this)
                CountDownStyle.BLOCK  -> ctx.renderBlock(this, showColon = false)
                CountDownStyle.COLON  -> ctx.renderBlock(this, showColon = true)
            }
        }
    }

    private fun renderPlain(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.Text {
            attr {
                text(ctx.formatTime(ctx.remainingMs))
                fontSize(ctx.attr.fontSize)
                color(ctx.attr.textColor)
                fontWeightMedium()
            }
        }
    }

    private fun renderBlock(parent: ViewContainer<*, *>, showColon: Boolean) {
        val ctx = this
        val segments = ctx.buildSegments(ctx.remainingMs)
        parent.View {
            attr { flexDirectionRow(); alignItems(FlexAlign.CENTER) }
            segments.forEachIndexed { idx, seg ->
                // separator
                if (idx > 0) {
                    Text {
                        attr {
                            text(if (showColon) ":" else " ")
                            fontSize(ctx.attr.separatorFontSize)
                            color(ctx.attr.separatorColor)
                            fontWeightBold()
                            paddingLeft(4f)
                            paddingRight(4f)
                        }
                    }
                }
                View {
                    attr {
                        minWidth(ctx.attr.blockFontSize * 1.8f)
                        paddingLeft(ctx.attr.blockPaddingH)
                        paddingRight(ctx.attr.blockPaddingH)
                        paddingTop(ctx.attr.blockPaddingV)
                        paddingBottom(ctx.attr.blockPaddingV)
                        borderRadius(ctx.attr.blockRadius)
                        backgroundColor(ctx.attr.blockBg)
                        allCenter()
                        animate(Animation.easeInOut(0.12f), ctx.flipTick)
                    }
                    Text {
                        attr {
                            text(seg)
                            fontSize(ctx.attr.blockFontSize)
                            color(ctx.attr.blockTextColor)
                            fontWeightBold()
                        }
                    }
                }
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSecs = ms / 1000L
        val days = totalSecs / 86400L
        val hours = (totalSecs % 86400L) / 3600L
        val minutes = (totalSecs % 3600L) / 60L
        val seconds = totalSecs % 60L
        val centi = (ms % 1000L) / 10L

        return when (attr.format) {
            CountDownFormat.SS          -> seconds.pad()
            CountDownFormat.MM_SS       -> "${(minutes + hours * 60L + days * 1440L).pad()}:${seconds.pad()}"
            CountDownFormat.HH_MM_SS    -> "${(hours + days * 24L).pad()}:${minutes.pad()}:${seconds.pad()}"
            CountDownFormat.HH_MM_SS_MS -> "${(hours + days * 24L).pad()}:${minutes.pad()}:${seconds.pad()}.${centi.pad()}"
            CountDownFormat.CUSTOM      -> attr.customFormat
                .replace("{D}", days.toString())
                .replace("{H}", hours.pad())
                .replace("{M}", minutes.pad())
                .replace("{S}", seconds.pad())
                .replace("{MS}", centi.pad())
        }
    }

    private fun buildSegments(ms: Long): List<String> {
        val totalSecs = ms / 1000L
        val days = totalSecs / 86400L
        val hours = (totalSecs % 86400L) / 3600L
        val minutes = (totalSecs % 3600L) / 60L
        val seconds = totalSecs % 60L
        val centi = (ms % 1000L) / 10L
        return when (attr.format) {
            CountDownFormat.SS          -> listOf(seconds.pad())
            CountDownFormat.MM_SS       -> listOf((minutes + hours * 60L + days * 1440L).pad(), seconds.pad())
            CountDownFormat.HH_MM_SS    -> listOf((hours + days * 24L).pad(), minutes.pad(), seconds.pad())
            CountDownFormat.HH_MM_SS_MS -> listOf((hours + days * 24L).pad(), minutes.pad(), seconds.pad(), centi.pad())
            CountDownFormat.CUSTOM      -> listOf(formatTime(ms))
        }
    }

    private fun Long.pad(): String = toString().padStart(2, '0')
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.CountDown(init: CountDownView.() -> Unit) {
    addChild(CountDownView(), init)
}
