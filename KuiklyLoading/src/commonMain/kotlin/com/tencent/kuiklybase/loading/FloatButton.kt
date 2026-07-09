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
import com.tencent.kuikly.core.base.BoxShadow
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

/** Preset color theme for [FloatButton] and [FloatActionItem]. */
enum class FloatButtonColor {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
}

/**
 * A single action item in the expandable group above a [FloatButton].
 *
 * @param icon  Glyph or emoji displayed on the action button.
 * @param label Short text label shown to the left of the button.
 * @param color Color preset for this action button.
 */
data class FloatActionItem(
    val icon: String,
    val label: String = "",
    val color: FloatButtonColor = FloatButtonColor.PRIMARY,
)

class FloatButtonAttr : ComposeAttr() {

    internal var icon by observable("+")
    internal var buttonColor by observable(FloatButtonColor.PRIMARY)
    internal var size by observable(56f)
    internal var actionSize by observable(40f)
    internal var bottom by observable(24f)
    internal var right by observable(24f)
    internal var actions by observable(listOf<FloatActionItem>())
    internal var expanded by observable(false)
    internal var labelFontSize by observable(12f)
    internal var showShadow by observable(true)

    fun icon(i: String) { icon = i }
    fun color(c: FloatButtonColor) { buttonColor = c }
    fun size(s: Float) { size = s }
    fun actionSize(s: Float) { actionSize = s }
    fun bottom(b: Float) { bottom = b }
    fun right(r: Float) { right = r }
    fun actions(vararg items: FloatActionItem) { actions = items.toList() }
    fun actions(items: List<FloatActionItem>) { actions = items }
    fun expanded(e: Boolean) { expanded = e }
    fun labelFontSize(s: Float) { labelFontSize = s }
    fun showShadow(show: Boolean) { showShadow = show }
}

class FloatButtonEvent : ComposeEvent() {
    var onClick: (() -> Unit)? = null
    var onActionClick: ((Int) -> Unit)? = null
    var onExpandChange: ((Boolean) -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class FloatButtonView : ComposeView<FloatButtonAttr, FloatButtonEvent>() {

    private var expanded by observable(false)

    override fun createAttr(): FloatButtonAttr = FloatButtonAttr()
    override fun createEvent(): FloatButtonEvent = FloatButtonEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    positionAbsolute()
                    bottom(ctx.attr.bottom)
                    right(ctx.attr.right)
                    flexDirectionColumn()
                    alignItemsFlexEnd()
                }

                // action group above main FAB
                if (ctx.attr.actions.isNotEmpty()) {
                    val actions = ctx.attr.actions
                    View {
                        attr {
                            flexDirectionColumn()
                            alignItemsFlexEnd()
                            marginBottom(12f)
                            opacity(if (ctx.expanded) 1f else 0f)
                            animate(Animation.easeInOut(0.25f), ctx.expanded)
                        }

                        actions.forEachIndexed { idx, item ->
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    marginBottom(if (idx < actions.size - 1) 12f else 0f)
                                }

                                if (item.label.isNotEmpty()) {
                                    View {
                                        attr {
                                            paddingLeft(8f)
                                            paddingRight(8f)
                                            paddingTop(4f)
                                            paddingBottom(4f)
                                            borderRadius(4f)
                                            backgroundColor(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.6f))
                                            marginRight(8f)
                                        }
                                        Text {
                                            attr {
                                                text(item.label)
                                                fontSize(ctx.attr.labelFontSize)
                                                color(Color(0xFFFFFFFFL))
                                            }
                                        }
                                    }
                                }

                                val actionBg = colorForPreset(item.color)
                                val actionSz = ctx.attr.actionSize
                                View {
                                    attr {
                                        size(actionSz, actionSz)
                                        borderRadius(actionSz / 2f)
                                        backgroundColor(actionBg)
                                        allCenter()
                                        if (ctx.attr.showShadow) {
                                            boxShadow(BoxShadow(0f, 4f, 10f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.2f)))
                                        }
                                        animate(Animation.easeInOut(0.25f), ctx.expanded)
                                    }
                                    event {
                                        click {
                                            ctx.event.onActionClick?.invoke(idx)
                                        }
                                    }
                                    Text {
                                        attr {
                                            text(item.icon)
                                            fontSize(ctx.attr.actionSize * 0.4f)
                                            color(Color(0xFFFFFFFFL))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // main FAB
                val mainBg = colorForPreset(ctx.attr.buttonColor)
                val sz = ctx.attr.size
                View {
                    attr {
                        size(sz, sz)
                        borderRadius(sz / 2f)
                        backgroundColor(mainBg)
                        allCenter()
                        if (ctx.attr.showShadow) {
                            boxShadow(BoxShadow(0f, 6f, 16f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.25f)))
                        }
                        // rotate "+" by 45° when expanded to form an "×"
                        transform(Rotate(if (ctx.expanded) 45f else 0f))
                        animate(Animation.easeInOut(0.3f), ctx.expanded)
                    }
                    event {
                        click {
                            if (ctx.attr.actions.isNotEmpty()) {
                                ctx.expanded = !ctx.expanded
                                ctx.event.onExpandChange?.invoke(ctx.expanded)
                            } else {
                                ctx.event.onClick?.invoke()
                            }
                        }
                    }
                    Text {
                        attr {
                            text(ctx.attr.icon)
                            fontSize(sz * 0.45f)
                            color(Color(0xFFFFFFFFL))
                            fontWeightMedium()
                        }
                    }
                }
            }
        }
    }

    private fun colorForPreset(preset: FloatButtonColor): Color = when (preset) {
        FloatButtonColor.PRIMARY -> Color(0xFF1677FFL)
        FloatButtonColor.SUCCESS -> Color(0xFF52C41AL)
        FloatButtonColor.WARNING -> Color(0xFFFAAD14L)
        FloatButtonColor.DANGER  -> Color(0xFFFF4D4FL)
    }
}

// ---------------------------------------------------------------------------
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.FloatButton(init: FloatButtonView.() -> Unit) {
    addChild(FloatButtonView(), init)
}
