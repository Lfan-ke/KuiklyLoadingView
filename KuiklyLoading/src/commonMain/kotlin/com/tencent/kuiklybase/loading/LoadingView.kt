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
import com.tencent.kuikly.core.views.View

fun ViewContainer<*, *>.Loading(init: LoadingView.() -> Unit) {
    addChild(LoadingView(), init)
}

class LoadingView : ComposeView<LoadingAttr, LoadingEvent>() {

    private var timeoutHandle: String = ""

    override fun createAttr(): LoadingAttr = LoadingAttr()

    override fun createEvent(): LoadingEvent = LoadingEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            vif({ ctx.attr.visible }) {
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
                        }
                        ctx.loadingCard(this)
                    }
                } else {
                    ctx.loadingCard(this)
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
            ActivityIndicator {
                attr {
                    isGrayStyle(false)
                    transform(scale = Scale(2f, 2f))
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

    override fun viewWillUnload() {
        super.viewWillUnload()
        cancelTimeout()
    }

    override fun attr(init: LoadingAttr.() -> Unit) {
        val wasVisible = attr.visible
        val prevTimeoutMs = attr.timeoutMs
        super.attr(init)
        if (attr.visible && !wasVisible) {
            scheduleTimeout()
        } else if (!attr.visible && wasVisible) {
            cancelTimeout()
        } else if (attr.visible && attr.timeoutMs != prevTimeoutMs) {
            scheduleTimeout()
        }
    }

    private fun scheduleTimeout() {
        val ms = attr.timeoutMs
        if (ms <= 0) return
        cancelTimeout()
        timeoutHandle = setTimeout(pagerId, ms) {
            attr.visible = false
            event.onFireEvent("timeout", null)
        }
    }

    private fun cancelTimeout() {
        if (timeoutHandle.isNotEmpty()) {
            clearTimeout(timeoutHandle)
            timeoutHandle = ""
        }
    }
}

class LoadingAttr : ComposeAttr() {

    internal var visible by observable(false)
    internal var fullScreen by observable(true)
    internal var loadingText by observable("")
    internal var maskColor by observable(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.4f))
    internal var timeoutMs: Int = 0

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
}

class LoadingEvent : ComposeEvent() {

    internal var onTimeoutHandler: (() -> Unit)? = null

    fun onTimeout(handler: () -> Unit) {
        onTimeoutHandler = handler
        register("timeout") { onTimeoutHandler?.invoke() }
    }
}
