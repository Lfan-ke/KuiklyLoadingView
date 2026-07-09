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
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.clearTimeout
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.ActivityIndicator
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * State machine for the load-more footer - matches Vant / NutUI / TDesign InfiniteScroll states.
 */
enum class LoadMoreState {
    /** Waiting for user to scroll near the bottom. */
    IDLE,
    /** Actively fetching next page. */
    LOADING,
    /** Load succeeded; brief success flash before returning to IDLE. */
    SUCCESS,
    /** Load failed; shows an error label users can tap to retry. */
    ERROR,
    /** No more pages available; shows end-of-list label. */
    NO_MORE,
}

/**
 * Visual appearance preset for [LoadMoreView].
 *
 * - [DEFAULT]  - minimal, text-only labels (like Vant InfiniteScroll)
 * - [ICON]     - spinner + label (like NutUI Infinite / TDesign LoadMore)
 * - [DIVIDER]  - horizontal rule with centred text (like Ant Design List `loadMore`)
 */
enum class LoadMoreStyle {
    DEFAULT,
    ICON,
    DIVIDER,
}

class LoadMoreAttr : ComposeAttr() {

    internal var state by observable(LoadMoreState.IDLE)
    internal var style by observable(LoadMoreStyle.ICON)
    internal var height by observable(56f)
    internal var idleText by observable("上拉加载更多")
    internal var loadingText by observable("正在加载…")
    internal var successText by observable("加载成功")
    internal var errorText by observable("加载失败，点击重试")
    internal var noMoreText by observable("没有更多了")
    internal var textColor by observable(Color(0xFF999999L))
    internal var loadingColor by observable(Color(0xFF1677FFL))
    internal var errorColor by observable(Color(0xFFFF4D4FL))
    internal var dividerColor by observable(Color(0xFFE8E8E8L))
    internal var fontSize by observable(13f)

    fun state(s: LoadMoreState) { state = s }
    fun style(s: LoadMoreStyle) { style = s }
    fun height(h: Float) { height = h.coerceAtLeast(32f) }
    fun idleText(t: String) { idleText = t }
    fun loadingText(t: String) { loadingText = t }
    fun successText(t: String) { successText = t }
    fun errorText(t: String) { errorText = t }
    fun noMoreText(t: String) { noMoreText = t }
    fun textColor(c: Color) { textColor = c }
    fun loadingColor(c: Color) { loadingColor = c }
    fun errorColor(c: Color) { errorColor = c }
    fun dividerColor(c: Color) { dividerColor = c }
    fun fontSize(s: Float) { fontSize = s }
}

class LoadMoreEvent : ComposeEvent() {
    /** Fires when user taps the component in [LoadMoreState.IDLE] or [LoadMoreState.ERROR]. */
    var onLoadMore: (() -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class LoadMoreView : ComposeView<LoadMoreAttr, LoadMoreEvent>() {

    private var spinTick by observable(0)
    private var spinHandle = ""

    override fun createAttr(): LoadMoreAttr = LoadMoreAttr()
    override fun createEvent(): LoadMoreEvent = LoadMoreEvent()

    override fun attr(init: LoadMoreAttr.() -> Unit) {
        val prevState = attr.state
        super.attr(init)
        if (attr.state == LoadMoreState.LOADING && prevState != LoadMoreState.LOADING) startSpin()
        if (attr.state != LoadMoreState.LOADING && prevState == LoadMoreState.LOADING) stopSpin()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopSpin()
    }

    private fun startSpin() {
        spinHandle = setTimeout(pagerId, 80) {
            spinTick = (spinTick + 1) % 12
            startSpin()
        }
    }

    private fun stopSpin() {
        if (spinHandle.isNotEmpty()) {
            clearTimeout(spinHandle)
            spinHandle = ""
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            when (ctx.attr.style) {
                LoadMoreStyle.DEFAULT  -> ctx.renderDefault(this)
                LoadMoreStyle.ICON     -> ctx.renderIcon(this)
                LoadMoreStyle.DIVIDER  -> ctx.renderDivider(this)
            }
        }
    }

    private fun renderDefault(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.View {
            attr {
                height(ctx.attr.height)
                allCenter()
                opacity(if (ctx.attr.state == LoadMoreState.IDLE) 0.6f else 1f)
                animate(Animation.easeInOut(0.2f), ctx.attr.state)
            }
            event {
                click {
                    val s = ctx.attr.state
                    if (s == LoadMoreState.IDLE || s == LoadMoreState.ERROR) ctx.event.onLoadMore?.invoke()
                }
            }
            Text {
                attr {
                    text(ctx.resolveText())
                    fontSize(ctx.attr.fontSize)
                    color(if (ctx.attr.state == LoadMoreState.ERROR) ctx.attr.errorColor else ctx.attr.textColor)
                }
            }
        }
    }

    private fun renderIcon(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.View {
            attr {
                height(ctx.attr.height)
                flexDirectionRow()
                allCenter()
            }
            event {
                click {
                    val s = ctx.attr.state
                    if (s == LoadMoreState.IDLE || s == LoadMoreState.ERROR) ctx.event.onLoadMore?.invoke()
                }
            }
            if (ctx.attr.state == LoadMoreState.LOADING) {
                ActivityIndicator {
                    attr {
                        isGrayStyle(false)
                        marginRight(6f)
                    }
                }
            }
            Text {
                attr {
                    text(ctx.resolveText())
                    fontSize(ctx.attr.fontSize)
                    color(
                        when (ctx.attr.state) {
                            LoadMoreState.ERROR   -> ctx.attr.errorColor
                            LoadMoreState.LOADING -> ctx.attr.loadingColor
                            else -> ctx.attr.textColor
                        }
                    )
                    animate(Animation.easeInOut(0.18f), ctx.attr.state)
                }
            }
        }
    }

    private fun renderDivider(parent: ViewContainer<*, *>) {
        val ctx = this
        parent.View {
            attr {
                height(ctx.attr.height)
                flexDirectionRow()
                alignItems(com.tencent.kuikly.core.layout.FlexAlign.CENTER)
                paddingLeft(16f)
                paddingRight(16f)
            }
            event {
                click {
                    val s = ctx.attr.state
                    if (s == LoadMoreState.IDLE || s == LoadMoreState.ERROR) ctx.event.onLoadMore?.invoke()
                }
            }
            View { attr { flex(1f); height(0.5f); backgroundColor(ctx.attr.dividerColor) } }
            if (ctx.attr.state == LoadMoreState.LOADING) {
                ActivityIndicator {
                    attr { isGrayStyle(true); marginLeft(12f); marginRight(4f) }
                }
            }
            Text {
                attr {
                    text(ctx.resolveText())
                    fontSize(ctx.attr.fontSize)
                    color(
                        when (ctx.attr.state) {
                            LoadMoreState.ERROR   -> ctx.attr.errorColor
                            else -> ctx.attr.textColor
                        }
                    )
                    marginLeft(12f)
                    marginRight(12f)
                }
            }
            View { attr { flex(1f); height(0.5f); backgroundColor(ctx.attr.dividerColor) } }
        }
    }

    private fun resolveText(): String = when (attr.state) {
        LoadMoreState.IDLE    -> attr.idleText
        LoadMoreState.LOADING -> attr.loadingText
        LoadMoreState.SUCCESS -> attr.successText
        LoadMoreState.ERROR   -> attr.errorText
        LoadMoreState.NO_MORE -> attr.noMoreText
    }
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.LoadMore(init: LoadMoreView.() -> Unit) {
    addChild(LoadMoreView(), init)
}
