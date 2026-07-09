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
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

class WatermarkAttr : ComposeAttr() {

    internal var watermarkText by observable("CONFIDENTIAL")
    internal var textColor by observable(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.08f))
    internal var fontSize by observable(14f)
    internal var rotateDeg by observable(-25f)
    internal var gapX by observable(60f)
    internal var gapY by observable(60f)
    internal var tileWidth by observable(120f)
    internal var tileHeight by observable(64f)
    internal var cols by observable(4)
    internal var rows by observable(8)
    internal var zIndexValue by observable(10)

    // content builder - the protected content the watermark overlays
    internal var contentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun text(t: String) { watermarkText = t }
    fun color(c: Color) { textColor = c }
    fun fontSize(s: Float) { fontSize = s }
    fun rotate(deg: Float) { rotateDeg = deg }
    fun gap(x: Float, y: Float) { gapX = x; gapY = y }
    fun tileSize(w: Float, h: Float) { tileWidth = w; tileHeight = h }
    fun grid(cols: Int, rows: Int) { this.cols = cols.coerceIn(1, 12); this.rows = rows.coerceIn(1, 20) }

    /** Slot: the View content that the watermark overlays. */
    fun content(builder: ViewContainer<*, *>.() -> Unit) { contentBuilder = builder }
}

class WatermarkEvent : ComposeEvent()

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

/**
 * Renders [content] and overlays a repeating diagonal text watermark on top,
 * matching Ant Design / TDesign Watermark component behavior.
 *
 * The watermark is a grid of rotated Text tiles rendered in an absolutely-
 * positioned View above the content. Alpha is kept very low (0.08 default)
 * so underlying content remains legible.
 */
class WatermarkView : ComposeView<WatermarkAttr, WatermarkEvent>() {

    override fun createAttr(): WatermarkAttr = WatermarkAttr()
    override fun createEvent(): WatermarkEvent = WatermarkEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    positionRelative()
                    flexDirectionColumn()
                }

                // Protected content
                ctx.attr.contentBuilder?.invoke(this)

                // Watermark overlay - fixed grid covers any container
                View {
                    attr {
                        positionAbsolute()
                        top(0f)
                        left(0f)
                        right(0f)
                        bottom(0f)
                        zIndex(ctx.attr.zIndexValue)
                        overflow(false)
                    }

                    for (row in 0 until ctx.attr.rows) {
                        View {
                            attr {
                                flexDirectionRow()
                                positionAbsolute()
                                top(row.toFloat() * (ctx.attr.tileHeight + ctx.attr.gapY))
                                left(-ctx.attr.tileWidth / 2f)
                                right(0f)
                            }
                            for (col in 0 until ctx.attr.cols) {
                                View {
                                    attr {
                                        width(ctx.attr.tileWidth)
                                        height(ctx.attr.tileHeight)
                                        marginRight(ctx.attr.gapX)
                                        allCenter()
                                        transform(Rotate(ctx.attr.rotateDeg))
                                    }
                                    Text {
                                        attr {
                                            text(ctx.attr.watermarkText)
                                            fontSize(ctx.attr.fontSize)
                                            color(ctx.attr.textColor)
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

fun ViewContainer<*, *>.Watermark(init: WatermarkView.() -> Unit) {
    addChild(WatermarkView(), init)
}
