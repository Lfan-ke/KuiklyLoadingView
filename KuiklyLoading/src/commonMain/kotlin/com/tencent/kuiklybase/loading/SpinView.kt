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
import com.tencent.kuikly.core.views.ActivityIndicator
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Tip position relative to the spinner - mirrors Ant Design Spin `indicator` layout.
 *
 * - [BOTTOM]  - tip label below the spinner (default)
 * - [RIGHT]   - tip label to the right of the spinner
 */
enum class SpinTipPosition {
    BOTTOM,
    RIGHT,
}

/**
 * Container-level loading overlay - wraps arbitrary content and overlays a
 * spinner when [SpinAttr.spinning] is true. Mirrors Ant Design `<Spin>`,
 * Arco Design `<Spin>`, and Element Plus `v-loading`.
 *
 * Usage:
 * ```kotlin
 * Spin {
 *     attr { spinning(true); tip("正在加载…") }
 *     content {
 *         // your content here
 *         Text { attr { text("Hello") } }
 *     }
 * }
 * ```
 */
class SpinAttr : ComposeAttr() {

    internal var spinning by observable(false)
    internal var tip by observable("")
    internal var tipPosition by observable(SpinTipPosition.BOTTOM)
    internal var delay by observable(0)
    internal var maskColor by observable(Color(red255 = 255, green255 = 255, blue255 = 255, alpha01 = 0.75f))
    internal var tipColor by observable(Color(0xFF1677FFL))
    internal var tipFontSize by observable(13f)
    internal var spinnerSize by observable(32f)
    internal var isGraySpinner by observable(false)
    internal var contentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun spinning(s: Boolean) { spinning = s }
    fun tip(t: String) { tip = t }
    fun tipPosition(p: SpinTipPosition) { tipPosition = p }
    fun delay(ms: Int) { delay = ms.coerceAtLeast(0) }
    fun maskColor(c: Color) { maskColor = c }
    fun tipColor(c: Color) { tipColor = c }
    fun tipFontSize(s: Float) { tipFontSize = s }
    fun spinnerSize(s: Float) { spinnerSize = s.coerceAtLeast(16f) }
    fun graySpinner(g: Boolean) { isGraySpinner = g }
    fun content(builder: ViewContainer<*, *>.() -> Unit) { contentBuilder = builder }
}

class SpinEvent : ComposeEvent()

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class SpinView : ComposeView<SpinAttr, SpinEvent>() {

    private var actuallySpinning by observable(false)
    private var delayHandle = ""

    override fun createAttr(): SpinAttr = SpinAttr()
    override fun createEvent(): SpinEvent = SpinEvent()

    override fun attr(init: SpinAttr.() -> Unit) {
        val wasSpinning = attr.spinning
        super.attr(init)
        if (attr.spinning && !wasSpinning) {
            val d = attr.delay
            if (d > 0) {
                delayHandle = com.tencent.kuikly.core.timer.setTimeout(pagerId, d) {
                    if (attr.spinning) actuallySpinning = true
                }
            } else {
                actuallySpinning = true
            }
        } else if (!attr.spinning && wasSpinning) {
            if (delayHandle.isNotEmpty()) {
                com.tencent.kuikly.core.timer.clearTimeout(delayHandle)
                delayHandle = ""
            }
            actuallySpinning = false
        }
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        if (delayHandle.isNotEmpty()) {
            com.tencent.kuikly.core.timer.clearTimeout(delayHandle)
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr { positionRelative() }

                // underlying content
                ctx.attr.contentBuilder?.invoke(this)

                // overlay when spinning
                if (ctx.actuallySpinning) {
                    View {
                        attr {
                            positionAbsolute()
                            top(0f)
                            left(0f)
                            right(0f)
                            bottom(0f)
                            backgroundColor(ctx.attr.maskColor)
                            allCenter()
                            opacity(if (ctx.actuallySpinning) 1f else 0f)
                            animate(Animation.easeInOut(0.2f), ctx.actuallySpinning)
                        }

                        if (ctx.attr.tipPosition == SpinTipPosition.RIGHT) {
                            View {
                                attr { flexDirectionRow(); alignItems(FlexAlign.CENTER) }
                                ActivityIndicator { attr { isGrayStyle(ctx.attr.isGraySpinner) } }
                                if (ctx.attr.tip.isNotEmpty()) {
                                    Text {
                                        attr {
                                            text(ctx.attr.tip)
                                            fontSize(ctx.attr.tipFontSize)
                                            color(ctx.attr.tipColor)
                                            marginLeft(8f)
                                        }
                                    }
                                }
                            }
                        } else {
                            View {
                                attr { flexDirectionColumn(); alignItems(FlexAlign.CENTER) }
                                ActivityIndicator { attr { isGrayStyle(ctx.attr.isGraySpinner) } }
                                if (ctx.attr.tip.isNotEmpty()) {
                                    Text {
                                        attr {
                                            text(ctx.attr.tip)
                                            fontSize(ctx.attr.tipFontSize)
                                            color(ctx.attr.tipColor)
                                            marginTop(8f)
                                        }
                                    }
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

fun ViewContainer<*, *>.Spin(init: SpinView.() -> Unit) {
    addChild(SpinView(), init)
}
