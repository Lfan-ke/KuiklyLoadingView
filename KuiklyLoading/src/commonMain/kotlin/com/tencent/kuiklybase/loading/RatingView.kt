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
import com.tencent.kuikly.core.base.Scale
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
 * Icon shape for [RatingView].
 *
 * - [STAR]   - ★/☆ characters
 * - [HEART]  - ♥/♡ characters
 * - [THUMBS] - 👍 (opacity dimmed when inactive)
 */
enum class RatingIcon { STAR, HEART, THUMBS }

class RatingAttr : ComposeAttr() {

    internal var value by observable(0f)
    internal var count by observable(5)
    internal var readOnly by observable(false)
    internal var allowHalf by observable(false)
    internal var icon by observable(RatingIcon.STAR)
    internal var size by observable(24f)
    internal var spacing by observable(6f)
    internal var activeColor by observable(Color(0xFFFFAD14L))
    internal var inactiveColor by observable(Color(0xFFD9D9D9L))
    internal var showCount by observable(false)

    fun value(v: Float) { value = v.coerceIn(0f, count.toFloat()) }
    fun count(n: Int) { count = n.coerceIn(1, 10) }
    fun readOnly(r: Boolean) { readOnly = r }
    fun allowHalf(h: Boolean) { allowHalf = h }
    fun icon(i: RatingIcon) { icon = i }
    fun size(s: Float) { size = s.coerceAtLeast(12f) }
    fun spacing(s: Float) { spacing = s.coerceAtLeast(0f) }
    fun activeColor(c: Color) { activeColor = c }
    fun inactiveColor(c: Color) { inactiveColor = c }
    fun showCount(s: Boolean) { showCount = s }
}

class RatingEvent : ComposeEvent() {
    var onChange: ((Float) -> Unit)? = null
    fun onChange(block: (Float) -> Unit) { onChange = block }
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class RatingView : ComposeView<RatingAttr, RatingEvent>() {

    private var pressIdx by observable(-1)

    override fun createAttr(): RatingAttr = RatingAttr()
    override fun createEvent(): RatingEvent = RatingEvent()

    private fun filledChar(): String = when (attr.icon) {
        RatingIcon.STAR   -> "★"
        RatingIcon.HEART  -> "♥"
        RatingIcon.THUMBS -> "👍"
    }

    private fun emptyChar(): String = when (attr.icon) {
        RatingIcon.STAR   -> "☆"
        RatingIcon.HEART  -> "♡"
        RatingIcon.THUMBS -> "👍"
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr { flexDirectionRow(); alignItems(FlexAlign.CENTER) }

                repeat(ctx.attr.count) { idx ->
                    val starValue = idx + 1f
                    val halfValue = idx + 0.5f
                    val isFilled = ctx.attr.value >= starValue
                    val isHalf = !isFilled && ctx.attr.allowHalf && ctx.attr.value >= halfValue
                    val isActive = isFilled || isHalf
                    val isPressed = ctx.pressIdx == idx

                    View {
                        attr {
                            marginRight(if (idx < ctx.attr.count - 1) ctx.attr.spacing else 0f)
                            allCenter()
                            transform(Scale(if (isPressed) 1.2f else 1f, if (isPressed) 1.2f else 1f))
                            animate(Animation.easeInOut(0.15f), isActive)
                            opacity(if (!isActive && ctx.attr.icon == RatingIcon.THUMBS) 0.35f else 1f)
                        }
                        event {
                            if (!ctx.attr.readOnly) {
                                click {
                                    val newVal = if (ctx.attr.allowHalf && ctx.attr.value == starValue) halfValue else starValue
                                    ctx.attr.value = newVal
                                    ctx.event.onChange?.invoke(newVal)
                                }
                                longPress {
                                    if (ctx.attr.allowHalf) {
                                        ctx.attr.value = halfValue
                                        ctx.event.onChange?.invoke(halfValue)
                                    }
                                }
                            }
                        }
                        Text {
                            attr {
                                text(if (isActive) ctx.filledChar() else ctx.emptyChar())
                                fontSize(ctx.attr.size)
                                color(if (isActive) ctx.attr.activeColor else ctx.attr.inactiveColor)
                            }
                        }
                    }
                }

                if (ctx.attr.showCount) {
                    val displayStr = if (ctx.attr.value == ctx.attr.value.toLong().toFloat()) {
                        ctx.attr.value.toInt().toString()
                    } else {
                        ctx.attr.value.toString()
                    }
                    Text {
                        attr {
                            text("  $displayStr / ${ctx.attr.count}")
                            fontSize(ctx.attr.size * 0.6f)
                            color(ctx.attr.inactiveColor)
                            marginLeft(4f)
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

fun ViewContainer<*, *>.Rating(init: RatingView.() -> Unit) {
    addChild(RatingView(), init)
}
