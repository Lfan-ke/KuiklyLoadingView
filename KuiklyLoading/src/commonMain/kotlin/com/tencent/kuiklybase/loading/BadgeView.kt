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
 * Badge display variant - mirrors Ant Design / Vant / NutUI Badge props.
 *
 * - [COUNT]   - numeric count or overflowed "99+"
 * - [DOT]     - simple red dot (no number)
 * - [TEXT]    - arbitrary short text label (e.g. "NEW", "HOT")
 * - [RIBBON]  - diagonal corner ribbon label (like Ant Design ribbon)
 */
enum class BadgeType {
    COUNT,
    DOT,
    TEXT,
    RIBBON,
}

/** Color presets for [BadgeView]. */
enum class BadgeColor {
    RED,
    BLUE,
    GREEN,
    ORANGE,
    GRAY,
    PURPLE,
}

class BadgeAttr : ComposeAttr() {

    internal var count by observable(0)
    internal var badgeType by observable(BadgeType.COUNT)
    internal var badgeText by observable("")
    internal var max by observable(99)
    internal var showZero by observable(false)
    internal var offset by observable(Pair(0f, 0f))  // x, y offset from top-right
    internal var badgeColor by observable(Color(0xFFFF4D4FL))
    internal var textColor by observable(Color(0xFFFFFFFFL))
    internal var fontSize by observable(11f)
    internal var dotSize by observable(8f)
    internal var visible by observable(true)

    // content builder
    internal var contentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun count(n: Int) { count = n }
    fun type(t: BadgeType) { badgeType = t }
    fun text(t: String) { badgeText = t; badgeType = BadgeType.TEXT }
    fun max(m: Int) { max = m.coerceAtLeast(1) }
    fun showZero(show: Boolean) { showZero = show }
    fun offset(x: Float, y: Float) { offset = x to y }
    fun color(c: Color) { badgeColor = c }
    fun textColor(c: Color) { textColor = c }
    fun fontSize(s: Float) { fontSize = s }
    fun dotSize(s: Float) { dotSize = s }
    fun visible(v: Boolean) { visible = v }

    fun color(preset: BadgeColor) {
        badgeColor = when (preset) {
            BadgeColor.RED    -> Color(0xFFFF4D4FL)
            BadgeColor.BLUE   -> Color(0xFF1677FFL)
            BadgeColor.GREEN  -> Color(0xFF52C41AL)
            BadgeColor.ORANGE -> Color(0xFFFFA940L)
            BadgeColor.GRAY   -> Color(0xFF8C8C8CL)
            BadgeColor.PURPLE -> Color(0xFF722ED1L)
        }
    }

    /** Wraps the badge around [builder] content. */
    fun content(builder: ViewContainer<*, *>.() -> Unit) { contentBuilder = builder }
}

class BadgeEvent : ComposeEvent()

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class BadgeView : ComposeView<BadgeAttr, BadgeEvent>() {

    private var appeared by observable(false)

    override fun createAttr(): BadgeAttr = BadgeAttr()
    override fun createEvent(): BadgeEvent = BadgeEvent()

    override fun didInit() {
        super.didInit()
        appeared = true
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr { positionRelative() }

                // wrapped content (e.g. icon, avatar)
                ctx.attr.contentBuilder?.invoke(this)

                // badge overlay
                val show = ctx.attr.visible && when (ctx.attr.badgeType) {
                    BadgeType.COUNT -> ctx.attr.count > 0 || ctx.attr.showZero
                    BadgeType.DOT   -> true
                    BadgeType.TEXT  -> ctx.attr.badgeText.isNotEmpty()
                    BadgeType.RIBBON -> ctx.attr.badgeText.isNotEmpty()
                }

                if (show) {
                    when (ctx.attr.badgeType) {
                        BadgeType.DOT    -> ctx.renderDot(this)
                        BadgeType.COUNT  -> ctx.renderCount(this)
                        BadgeType.TEXT   -> ctx.renderText(this)
                        BadgeType.RIBBON -> ctx.renderRibbon(this)
                    }
                }
            }
        }
    }

    private fun renderDot(parent: ViewContainer<*, *>) {
        val ctx = this
        val sz = ctx.attr.dotSize
        parent.View {
            attr {
                positionAbsolute()
                top(-sz / 2f + ctx.attr.offset.second)
                right(-sz / 2f - ctx.attr.offset.first)
                size(sz, sz)
                borderRadius(sz / 2f)
                backgroundColor(ctx.attr.badgeColor)
                opacity(if (ctx.appeared) 1f else 0f)
                animate(Animation.easeOut(0.25f), ctx.appeared)
            }
        }
    }

    private fun renderCount(parent: ViewContainer<*, *>) {
        val ctx = this
        val label = if (ctx.attr.count > ctx.attr.max) "${ctx.attr.max}+" else ctx.attr.count.toString()
        val minW = 18f
        val h = 18f
        parent.View {
            attr {
                positionAbsolute()
                top(-h / 2f + ctx.attr.offset.second)
                right(-minW / 2f - ctx.attr.offset.first)
                minWidth(minW)
                height(h)
                borderRadius(h / 2f)
                backgroundColor(ctx.attr.badgeColor)
                allCenter()
                paddingLeft(5f)
                paddingRight(5f)
                opacity(if (ctx.appeared) 1f else 0f)
                animate(Animation.easeOut(0.25f), ctx.appeared)
            }
            Text {
                attr {
                    text(label)
                    fontSize(ctx.attr.fontSize)
                    fontWeightMedium()
                    color(ctx.attr.textColor)
                }
            }
        }
    }

    private fun renderText(parent: ViewContainer<*, *>) {
        val ctx = this
        val h = 18f
        parent.View {
            attr {
                positionAbsolute()
                top(-h / 2f + ctx.attr.offset.second)
                right(-ctx.attr.offset.first)
                height(h)
                borderRadius(h / 2f)
                backgroundColor(ctx.attr.badgeColor)
                allCenter()
                paddingLeft(6f)
                paddingRight(6f)
                opacity(if (ctx.appeared) 1f else 0f)
                animate(Animation.easeOut(0.25f), ctx.appeared)
            }
            Text {
                attr {
                    text(ctx.attr.badgeText)
                    fontSize(ctx.attr.fontSize)
                    fontWeightMedium()
                    color(ctx.attr.textColor)
                }
            }
        }
    }

    private fun renderRibbon(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.View {
            attr {
                positionAbsolute()
                top(0f)
                right(0f)
                width(64f)
                height(64f)
                overflow(false)
            }
            // Diagonal ribbon approximated with a rotated box
            View {
                attr {
                    positionAbsolute()
                    top(12f)
                    right(-20f)
                    width(80f)
                    height(22f)
                    backgroundColor(ctx.attr.badgeColor)
                    allCenter()
                    transform(com.tencent.kuikly.core.base.Rotate(45f))
                }
                Text {
                    attr {
                        text(ctx.attr.badgeText)
                        fontSize(ctx.attr.fontSize)
                        fontWeightBold()
                        color(ctx.attr.textColor)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.Badge(init: BadgeView.() -> Unit) {
    addChild(BadgeView(), init)
}
