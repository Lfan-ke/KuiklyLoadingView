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
import com.tencent.kuikly.core.base.Scale
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Visual shape of each rating item.
 *
 * - [STAR]    - ★ character (default)
 * - [HEART]   - ♥ character
 * - [CIRCLE]  - ● character
 * - [CUSTOM]  - use [RateAttr.filledChar] / [RateAttr.emptyChar]
 */
enum class RateShape {
    STAR,
    HEART,
    CIRCLE,
    CUSTOM,
}

class RateAttr : ComposeAttr() {

    internal var value by observable(0f)
    internal var count by observable(5)
    internal var allowHalf by observable(false)
    internal var readonly by observable(false)
    internal var shape by observable(RateShape.STAR)
    internal var filledChar by observable("★")
    internal var emptyChar by observable("☆")
    internal var filledColor by observable(Color(0xFFFAAD14L))
    internal var emptyColor by observable(Color(0xFFD9D9D9L))
    internal var size by observable(24f)
    internal var spacing by observable(4f)
    internal var showScore by observable(false)
    internal var scoreColor by observable(Color(0xFF666666L))
    internal var scoreFontSize by observable(13f)

    fun value(v: Float) { value = v.coerceIn(0f, count.toFloat()) }
    fun count(n: Int) { count = n.coerceIn(1, 20) }
    fun allowHalf(a: Boolean) { allowHalf = a }
    fun readonly(r: Boolean) { readonly = r }
    fun shape(s: RateShape) {
        shape = s
        when (s) {
            RateShape.STAR   -> { filledChar = "★"; emptyChar = "☆" }
            RateShape.HEART  -> { filledChar = "♥"; emptyChar = "♡" }
            RateShape.CIRCLE -> { filledChar = "●"; emptyChar = "○" }
            RateShape.CUSTOM -> Unit
        }
    }
    fun filledChar(c: String) { filledChar = c; shape = RateShape.CUSTOM }
    fun emptyChar(c: String) { emptyChar = c; shape = RateShape.CUSTOM }
    fun filledColor(c: Color) { filledColor = c }
    fun emptyColor(c: Color) { emptyColor = c }
    fun size(s: Float) { size = s }
    fun spacing(s: Float) { spacing = s }
    fun showScore(show: Boolean) { showScore = show }
    fun scoreColor(c: Color) { scoreColor = c }
    fun scoreFontSize(s: Float) { scoreFontSize = s }
}

class RateEvent : ComposeEvent() {
    /** Fires when user taps a star. New value is passed. */
    var onChange: ((Float) -> Unit)? = null
    var onHoverChange: ((Float) -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class RateView : ComposeView<RateAttr, RateEvent>() {

    // hover value - what star is being "pointed at" while touching
    private var hoverValue by observable(-1f)

    override fun createAttr(): RateAttr = RateAttr()
    override fun createEvent(): RateEvent = RateEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr { flexDirectionRow(); alignItems(FlexAlign.CENTER) }

                val displayValue = if (ctx.hoverValue >= 0f) ctx.hoverValue else ctx.attr.value

                repeat(ctx.attr.count) { idx ->
                    val starValue = idx + 1f
                    val halfValue = idx + 0.5f
                    val filled = displayValue >= starValue
                    val halfFilled = !filled && ctx.attr.allowHalf && displayValue >= halfValue

                    val scale = if (filled || halfFilled) 1f else 0.85f

                    View {
                        attr {
                            marginRight(if (idx < ctx.attr.count - 1) ctx.attr.spacing else 0f)
                            allCenter()
                            transform(Scale(scale, scale))
                            animate(Animation.easeOut(0.15f), filled || halfFilled)
                        }
                        event {
                            if (!ctx.attr.readonly) {
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
                                text(if (filled || halfFilled) ctx.attr.filledChar else ctx.attr.emptyChar)
                                fontSize(ctx.attr.size)
                                color(if (filled || halfFilled) ctx.attr.filledColor else ctx.attr.emptyColor)
                            }
                        }
                    }
                }

                if (ctx.attr.showScore) {
                    val displayValue2 = if (ctx.hoverValue >= 0f) ctx.hoverValue else ctx.attr.value
                    val scoreText = if (displayValue2 == displayValue2.toLong().toFloat()) {
                        displayValue2.toInt().toString()
                    } else {
                        displayValue2.toString()
                    }
                    Text {
                        attr {
                            text("  $scoreText 分")
                            fontSize(ctx.attr.scoreFontSize)
                            color(ctx.attr.scoreColor)
                            marginLeft(6f)
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

fun ViewContainer<*, *>.Rate(init: RateView.() -> Unit) {
    addChild(RateView(), init)
}
