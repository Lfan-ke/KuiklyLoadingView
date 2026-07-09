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
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Modal
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.TransitionType
import com.tencent.kuikly.core.views.TransitionView
import com.tencent.kuikly.core.views.View

fun ViewContainer<*, *>.Toast(init: ToastView.() -> Unit) {
    addChild(ToastView(), init)
}

enum class ToastIcon {
    NONE,
    SUCCESS,
    FAIL,
    WARNING,
    LOADING,
}

enum class ToastPosition {
    TOP,
    CENTER,
    BOTTOM,
}

class ToastAttr : ComposeAttr() {
    internal var visible by observable(false)
    internal var message by observable("")
    internal var durationMs by observable(2000)
    internal var icon by observable(ToastIcon.NONE)
    internal var position by observable(ToastPosition.CENTER)
    internal var maskColor by observable(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0f))
    internal var bubbleColor by observable(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.75f))
    internal var textColor by observable(Color(0xFFFFFFFFL))
    internal var fontSize by observable(14f)
    internal var cornerRadius by observable(10f)
    internal var paddingH by observable(20f)
    internal var paddingV by observable(14f)

    fun visible(show: Boolean) { visible = show }
    fun message(msg: String) { message = msg }
    fun durationMs(ms: Int) { durationMs = ms.coerceAtLeast(0) }
    fun icon(i: ToastIcon) { icon = i }
    fun position(p: ToastPosition) { position = p }
    fun maskColor(c: Color) { maskColor = c }
    fun bubbleColor(c: Color) { bubbleColor = c }
    fun textColor(c: Color) { textColor = c }
    fun fontSize(size: Float) { fontSize = size }
    fun cornerRadius(r: Float) { cornerRadius = r }
}

class ToastEvent : ComposeEvent() {
    internal var onDismissHandler: (() -> Unit)? = null

    fun onDismiss(handler: () -> Unit) {
        onDismissHandler = handler
    }
}

class ToastView : ComposeView<ToastAttr, ToastEvent>() {

    private var dismissHandle = ""

    override fun createAttr(): ToastAttr = ToastAttr()
    override fun createEvent(): ToastEvent = ToastEvent()

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopTimer()
    }

    override fun attr(init: ToastAttr.() -> Unit) {
        val wasVisible = attr.visible
        super.attr(init)
        if (attr.visible && !wasVisible) {
            stopTimer()
            if (attr.durationMs > 0) {
                dismissHandle = setTimeout(pagerId, attr.durationMs) {
                    event.onDismissHandler?.invoke()
                }
            }
        } else if (!attr.visible && wasVisible) {
            stopTimer()
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            vif({ ctx.attr.visible }) {
                Modal {
                    attr {
                        when (ctx.attr.position) {
                            ToastPosition.TOP -> {
                                justifyContentFlexStart()
                                alignItemsCenter()
                                paddingTop(80f)
                            }
                            ToastPosition.CENTER -> {
                                justifyContentCenter()
                                alignItemsCenter()
                            }
                            ToastPosition.BOTTOM -> {
                                justifyContentFlexEnd()
                                alignItemsCenter()
                                paddingBottom(80f)
                            }
                        }
                    }
                    // Optional tinted mask - only add the view if non-transparent
                    View {
                        attr {
                            absolutePositionAllZero()
                            backgroundColor(ctx.attr.maskColor)
                        }
                        event { click { } }
                    }
                    TransitionView(type = TransitionType.FADE_IN_OUT) {
                        View {
                            attr {
                                paddingLeft(ctx.attr.paddingH)
                                paddingRight(ctx.attr.paddingH)
                                paddingTop(ctx.attr.paddingV)
                                paddingBottom(ctx.attr.paddingV)
                                borderRadius(ctx.attr.cornerRadius)
                                backgroundColor(ctx.attr.bubbleColor)
                                flexDirectionColumn()
                                justifyContentCenter()
                                alignItemsCenter()
                            }
                            event { click { } }
                            if (ctx.attr.icon != ToastIcon.NONE) {
                                Text {
                                    attr {
                                        fontSize(ctx.attr.fontSize * 1.8f)
                                        color(ctx.attr.textColor)
                                        marginBottom(if (ctx.attr.message.isNotEmpty()) 8f else 0f)
                                        text(when (ctx.attr.icon) {
                                            ToastIcon.SUCCESS -> "✓"
                                            ToastIcon.FAIL    -> "✗"
                                            ToastIcon.WARNING -> "⚠"
                                            ToastIcon.LOADING -> "⟳"
                                            ToastIcon.NONE    -> ""
                                        })
                                    }
                                }
                            }
                            if (ctx.attr.message.isNotEmpty()) {
                                Text {
                                    attr {
                                        fontSize(ctx.attr.fontSize)
                                        color(ctx.attr.textColor)
                                        text(ctx.attr.message)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        if (dismissHandle.isNotEmpty()) {
            clearTimeout(dismissHandle)
            dismissHandle = ""
        }
    }
}
