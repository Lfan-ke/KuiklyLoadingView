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
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Left prefix icon mode for [NoticeBarView].
 *
 * - [NONE]    - no icon
 * - [BELL]    - 🔔 bell icon
 * - [INFO]    - ℹ️ info icon
 * - [WARNING] - ⚠️ warning icon
 * - [CUSTOM]  - use [NoticeBarAttr.icon] to set a custom glyph
 */
enum class NoticeBarIcon {
    NONE,
    BELL,
    INFO,
    WARNING,
    CUSTOM,
}

/**
 * Scroll mode for [NoticeBarView] - mirrors Vant/NutUI NoticeBar `scrollable` prop.
 *
 * - [AUTO]    - scrolls if text overflows, static otherwise
 * - [ALWAYS]  - always scrolls
 * - [NEVER]   - never scrolls; text truncates
 */
enum class NoticeBarScrollMode {
    AUTO,
    ALWAYS,
    NEVER,
}

/**
 * Right action mode.
 *
 * - [NONE]   - no right element
 * - [CLOSE]  - × close button
 * - [LINK]   - ">" arrow indicating a clickable link
 * - [CUSTOM] - caller provides content via [NoticeBarAttr.rightIcon]
 */
enum class NoticeBarAction {
    NONE,
    CLOSE,
    LINK,
    CUSTOM,
}

class NoticeBarAttr : ComposeAttr() {

    internal var text by observable("")
    internal var icon by observable(NoticeBarIcon.BELL)
    internal var customIcon by observable("")
    internal var action by observable(NoticeBarAction.CLOSE)
    internal var rightIcon by observable("")
    internal var scrollMode by observable(NoticeBarScrollMode.ALWAYS)
    internal var scrollSpeed by observable(60f)  // dp per second
    internal var scrollDelay by observable(1000)  // ms before first scroll start
    internal var loop by observable(true)
    internal var visible by observable(true)
    internal var wrapable by observable(false)   // multi-line static mode

    // colors
    internal var backgroundColor by observable(Color(0xFFFFFBE6L))
    internal var textColor by observable(Color(0xFF333333L))
    internal var iconColor by observable(Color(0xFFFAAD14L))
    internal var actionColor by observable(Color(0xFF999999L))

    // sizes
    internal var height by observable(40f)
    internal var fontSize by observable(13f)
    internal var paddingH by observable(12f)

    fun text(t: String) { text = t }
    fun icon(i: NoticeBarIcon) { icon = i }
    fun icon(glyph: String) { customIcon = glyph; icon = NoticeBarIcon.CUSTOM }
    fun action(a: NoticeBarAction) { action = a }
    fun rightIcon(glyph: String) { rightIcon = glyph; action = NoticeBarAction.CUSTOM }
    fun scrollMode(m: NoticeBarScrollMode) { scrollMode = m }
    fun scrollSpeed(dp: Float) { scrollSpeed = dp.coerceIn(10f, 300f) }
    fun scrollDelay(ms: Int) { scrollDelay = ms.coerceAtLeast(0) }
    fun loop(l: Boolean) { loop = l }
    fun visible(v: Boolean) { visible = v }
    fun wrapable(w: Boolean) { wrapable = w }
    fun backgroundColor(c: Color) { backgroundColor = c }
    fun textColor(c: Color) { textColor = c }
    fun iconColor(c: Color) { iconColor = c }
    fun actionColor(c: Color) { actionColor = c }
    fun height(h: Float) { height = h.coerceAtLeast(28f) }
    fun fontSize(s: Float) { fontSize = s }
    fun paddingH(p: Float) { paddingH = p.coerceAtLeast(0f) }
}

class NoticeBarEvent : ComposeEvent() {
    var onClose: (() -> Unit)? = null
    var onLinkClick: (() -> Unit)? = null
    var onReplay: (() -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class NoticeBarView : ComposeView<NoticeBarAttr, NoticeBarEvent>() {

    // offset in dp - drives the marquee text position
    private var scrollOffset by observable(0f)
    private var contentWidth by observable(0f)
    private var containerWidth by observable(0f)
    private var scrollHandle = ""
    private var delayHandle = ""
    private var dismissed by observable(false)

    // tick every 16ms (~60fps) advancing by speed * 0.016
    private val tickMs = 16

    override fun createAttr(): NoticeBarAttr = NoticeBarAttr()
    override fun createEvent(): NoticeBarEvent = NoticeBarEvent()

    override fun didInit() {
        super.didInit()
        scheduleStart()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopScroll()
        clearTimeout(delayHandle)
    }

    override fun attr(init: NoticeBarAttr.() -> Unit) {
        val prevText = attr.text
        super.attr(init)
        if (attr.text != prevText) {
            stopScroll()
            scrollOffset = 0f
            scheduleStart()
        }
    }

    private fun scheduleStart() {
        val mode = attr.scrollMode
        if (mode == NoticeBarScrollMode.NEVER) return
        delayHandle = setTimeout(pagerId, attr.scrollDelay) { startScroll() }
    }

    private fun startScroll() {
        if (scrollHandle.isNotEmpty()) return
        scroll()
    }

    private fun scroll() {
        scrollHandle = setTimeout(pagerId, tickMs) {
            val dpPerTick = attr.scrollSpeed * tickMs / 1000f
            scrollOffset += dpPerTick

            // content has fully scrolled off; decide to loop or stop
            val totalTravel = contentWidth + containerWidth
            if (scrollOffset >= totalTravel) {
                if (attr.loop) {
                    scrollOffset = 0f
                    event.onReplay?.invoke()
                } else {
                    scrollHandle = ""
                    return@setTimeout
                }
            }
            scroll()
        }
    }

    private fun stopScroll() {
        if (scrollHandle.isNotEmpty()) {
            clearTimeout(scrollHandle)
            scrollHandle = ""
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            if (!ctx.dismissed && ctx.attr.visible) {
                View {
                    attr {
                        flexDirectionRow()
                        alignItems(FlexAlign.CENTER)
                        height(ctx.attr.height)
                        backgroundColor(ctx.attr.backgroundColor)
                        paddingLeft(ctx.attr.paddingH)
                        paddingRight(ctx.attr.paddingH)
                        opacity(if (ctx.dismissed) 0f else 1f)
                        animate(Animation.easeInOut(0.2f), ctx.dismissed)
                    }

                    // left icon
                    val iconGlyph = when (ctx.attr.icon) {
                        NoticeBarIcon.NONE    -> ""
                        NoticeBarIcon.BELL    -> "🔔"
                        NoticeBarIcon.INFO    -> "ℹ"
                        NoticeBarIcon.WARNING -> "⚠"
                        NoticeBarIcon.CUSTOM  -> ctx.attr.customIcon
                    }
                    if (iconGlyph.isNotEmpty()) {
                        Text {
                            attr {
                                text(iconGlyph)
                                fontSize(ctx.attr.fontSize * 1.15f)
                                color(ctx.attr.iconColor)
                                marginRight(6f)
                            }
                        }
                    }

                    // scrolling text area
                    View {
                        attr {
                            flex(1f)
                            overflow(false)
                        }
                        if (ctx.attr.wrapable || ctx.attr.scrollMode == NoticeBarScrollMode.NEVER) {
                            Text {
                                attr {
                                    text(ctx.attr.text)
                                    fontSize(ctx.attr.fontSize)
                                    color(ctx.attr.textColor)
                                }
                            }
                        } else {
                            // marquee: translate text by -scrollOffset
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItems(FlexAlign.CENTER)
                                    positionAbsolute()
                                    left(-ctx.scrollOffset)
                                    top(0f)
                                    bottom(0f)
                                }
                                Text {
                                    attr {
                                        text(ctx.attr.text)
                                        fontSize(ctx.attr.fontSize)
                                        color(ctx.attr.textColor)
                                    }
                                }
                            }
                        }
                    }

                    // right action
                    when (ctx.attr.action) {
                        NoticeBarAction.NONE -> Unit
                        NoticeBarAction.CLOSE -> {
                            Text {
                                attr {
                                    text("✕")
                                    fontSize(ctx.attr.fontSize * 1.1f)
                                    color(ctx.attr.actionColor)
                                    marginLeft(8f)
                                }
                                event {
                                    click {
                                        ctx.dismissed = true
                                        ctx.event.onClose?.invoke()
                                    }
                                }
                            }
                        }
                        NoticeBarAction.LINK -> {
                            Text {
                                attr {
                                    text("›")
                                    fontSize(ctx.attr.fontSize * 1.3f)
                                    color(ctx.attr.actionColor)
                                    marginLeft(8f)
                                }
                                event { click { ctx.event.onLinkClick?.invoke() } }
                            }
                        }
                        NoticeBarAction.CUSTOM -> {
                            if (ctx.attr.rightIcon.isNotEmpty()) {
                                Text {
                                    attr {
                                        text(ctx.attr.rightIcon)
                                        fontSize(ctx.attr.fontSize * 1.1f)
                                        color(ctx.attr.actionColor)
                                        marginLeft(8f)
                                    }
                                    event { click { ctx.event.onLinkClick?.invoke() } }
                                }
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

fun ViewContainer<*, *>.NoticeBar(init: NoticeBarView.() -> Unit) {
    addChild(NoticeBarView(), init)
}
