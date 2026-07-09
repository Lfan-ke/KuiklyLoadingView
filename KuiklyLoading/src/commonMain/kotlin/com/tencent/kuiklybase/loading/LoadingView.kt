/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
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
import com.tencent.kuikly.core.base.Scale
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.ActivityIndicator
import com.tencent.kuikly.core.views.Modal
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.TransitionType
import com.tencent.kuikly.core.views.TransitionView
import com.tencent.kuikly.core.views.View

fun ViewContainer<*, *>.Loading(init: LoadingView.() -> Unit) {
    addChild(LoadingView(), init)
}

class LoadingView : ComposeView<LoadingAttr, LoadingEvent>() {

    private var timeoutHandle: String = ""
    private var delayHandle: String = ""

    override fun createAttr(): LoadingAttr = LoadingAttr()

    override fun createEvent(): LoadingEvent = LoadingEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // actuallyVisible respects the optional delayMs before showing the card
            vif({ ctx.attr.actuallyVisible }) {
                if (ctx.attr.fullScreen) {
                    Modal {
                        attr {
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        View {
                            attr {
                                absolutePositionAllZero()
                                backgroundColor(ctx.attr.maskColor)
                            }
                            // Intercept touches on the mask so they don't fall through
                            event { click { } }
                        }
                        TransitionView(type = TransitionType.FADE_IN_OUT) {
                            ctx.loadingCard(this)
                        }
                    }
                } else {
                    TransitionView(type = TransitionType.FADE_IN_OUT) {
                        ctx.loadingCard(this)
                    }
                }
            }
        }
    }

    private fun loadingCard(container: ViewContainer<*, *>) {
        val ctx = this
        container.View {
            attr {
                justifyContentCenter()
                alignItemsCenter()
                padding(16f)
                borderRadius(8f)
                backgroundColor(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.7f))
            }
            // Intercept touches on the card so they don't pass through in non-fullscreen mode
            event { click { } }
            val customContent = ctx.attr.customContentBuilder
            if (customContent != null) {
                customContent(this)
            } else {
                ActivityIndicator {
                    val size = ctx.attr.indicatorSize
                    attr {
                        isGrayStyle(false)
                        transform(scale = Scale(size, size))
                    }
                }
                vif({ ctx.attr.loadingText.isNotEmpty() }) {
                    Text {
                        attr {
                            marginTop(12f)
                            fontSize(13f)
                            color(Color.WHITE)
                            text(ctx.attr.loadingText)
                        }
                    }
                }
            }
        }
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        cancelTimeout()
        cancelDelay()
    }

    override fun attr(init: LoadingAttr.() -> Unit) {
        val wasVisible = attr.visible
        val prevTimeoutMs = attr.timeoutMs
        super.attr(init)
        val nowVisible = attr.visible
        if (nowVisible && !wasVisible) {
            val delayMs = attr.delayMs
            cancelDelay()
            if (delayMs > 0) {
                delayHandle = setTimeout(pagerId, delayMs) {
                    attr.actuallyVisible = true
                    scheduleTimeout()
                }
            } else {
                attr.actuallyVisible = true
                scheduleTimeout()
            }
        } else if (!nowVisible && wasVisible) {
            cancelTimeout()
            cancelDelay()
            attr.actuallyVisible = false
        } else if (nowVisible && attr.timeoutMs != prevTimeoutMs) {
            scheduleTimeout()
        }
    }

    private fun scheduleTimeout() {
        val ms = attr.timeoutMs
        if (ms <= 0) return
        cancelTimeout()
        timeoutHandle = setTimeout(pagerId, ms) {
            attr.visible = false
            attr.actuallyVisible = false
            event.onFireEvent("timeout", null)
        }
    }

    private fun cancelTimeout() {
        if (timeoutHandle.isNotEmpty()) {
            clearTimeout(timeoutHandle)
            timeoutHandle = ""
        }
    }

    private fun cancelDelay() {
        if (delayHandle.isNotEmpty()) {
            clearTimeout(delayHandle)
            delayHandle = ""
        }
    }
}

class LoadingAttr : ComposeAttr() {

    internal var visible by observable(false)
    internal var actuallyVisible by observable(false)
    internal var fullScreen by observable(true)
    internal var loadingText by observable("")
    internal var maskColor by observable(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.4f))
    internal var timeoutMs: Int = 0
    internal var indicatorSize: Float = 2f
    internal var delayMs: Int = 0
    internal var customContentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun visible(show: Boolean) {
        visible = show
    }

    fun fullScreen(fullScreen: Boolean) {
        this.fullScreen = fullScreen
    }

    fun loadingText(text: String) {
        loadingText = text
    }

    fun maskColor(color: Color) {
        maskColor = color
    }

    fun timeoutMs(ms: Int) {
        timeoutMs = ms
    }

    /**
     * Scale factor for the default [ActivityIndicator]. Ignored when [customContent] is set.
     * Default: 2.0
     */
    fun indicatorSize(size: Float) {
        indicatorSize = size.coerceAtLeast(0.5f)
    }

    /**
     * Delays showing the card by [ms] milliseconds after [visible] is set to true.
     * If the loading completes before the delay elapses, no card is shown at all —
     * preventing a flash of the indicator for fast operations.
     */
    fun delayMs(ms: Int) {
        delayMs = ms.coerceAtLeast(0)
    }

    /**
     * Replaces the default spinner + text layout with a custom view.
     * The builder receives the card [ViewContainer] as receiver.
     */
    fun customContent(builder: ViewContainer<*, *>.() -> Unit) {
        customContentBuilder = builder
    }
}

class LoadingEvent : ComposeEvent() {

    internal var onTimeoutHandler: (() -> Unit)? = null

    fun onTimeout(handler: () -> Unit) {
        onTimeoutHandler = handler
        register("timeout") { onTimeoutHandler?.invoke() }
    }
}
