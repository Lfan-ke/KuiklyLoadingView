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
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.layout.FlexAlign
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/** Visual mode of a [CollapsePanel] - mirrors Ant Design / Vant accordion styles. */
enum class CollapseTheme {
    /** White background with dividers (default - Ant Design card accordion). */
    DEFAULT,
    /** No border, flat dividers between panels. */
    BORDERLESS,
    /** Each panel is a card with border-radius and shadow. */
    CARD,
}

class CollapseAttr : ComposeAttr() {

    internal var title by observable("")
    internal var subtitle by observable("")
    internal var expanded by observable(false)
    internal var collapseTheme by observable(CollapseTheme.DEFAULT)
    internal var disabled by observable(false)
    internal var showArrow by observable(true)
    internal var headerHeight by observable(48f)
    internal var fontSize by observable(15f)
    internal var subtitleFontSize by observable(13f)
    internal var accentColor by observable(Color(0xFF1677FFL))
    internal var contentPaddingH by observable(16f)
    internal var contentPaddingV by observable(12f)

    // content builder - arbitrary View content in the panel body
    internal var contentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun title(t: String) { title = t }
    fun subtitle(t: String) { subtitle = t }
    fun expanded(e: Boolean) { expanded = e }
    fun theme(t: CollapseTheme) { collapseTheme = t }
    fun disabled(d: Boolean) { disabled = d }
    fun showArrow(show: Boolean) { showArrow = show }
    fun headerHeight(h: Float) { headerHeight = h.coerceAtLeast(36f) }
    fun fontSize(s: Float) { fontSize = s }
    fun accentColor(c: Color) { accentColor = c }
    fun contentPaddingH(p: Float) { contentPaddingH = p }
    fun contentPaddingV(p: Float) { contentPaddingV = p }

    /** Slot for the panel body content. */
    fun content(builder: ViewContainer<*, *>.() -> Unit) { contentBuilder = builder }
}

class CollapseEvent : ComposeEvent() {
    var onChange: ((expanded: Boolean) -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class CollapseView : ComposeView<CollapseAttr, CollapseEvent>() {

    private var isExpanded by observable(false)

    override fun createAttr(): CollapseAttr = CollapseAttr()
    override fun createEvent(): CollapseEvent = CollapseEvent()

    override fun didInit() {
        super.didInit()
        isExpanded = attr.expanded
    }

    private fun toggle() {
        if (attr.disabled) return
        isExpanded = !isExpanded
        event.onChange?.invoke(isExpanded)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            val a = ctx.attr
            val theme = a.collapseTheme

            val borderColor = Color(0xFFE8E8E8L)
            val headerBg = when (theme) {
                CollapseTheme.DEFAULT    -> Color(0xFFFAFAFAL)
                CollapseTheme.BORDERLESS -> Color(0xFFFFFFFFL)
                CollapseTheme.CARD       -> Color(0xFFFAFAFAL)
            }
            val bodyBg = Color(0xFFFFFFFFL)
            val shadow = when (theme) {
                CollapseTheme.CARD -> com.tencent.kuikly.core.base.BoxShadow(0f, 2f, 8f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.08f))
                else -> null
            }
            val radius = if (theme == CollapseTheme.CARD) 8f else 0f

            View {
                attr {
                    flexDirectionColumn()
                    marginBottom(if (theme == CollapseTheme.CARD) 8f else 0f)
                    borderRadius(radius)
                    if (theme != CollapseTheme.BORDERLESS) {
                        border(Border(1f, BorderStyle.SOLID, borderColor))
                    }
                    if (shadow != null) boxShadow(shadow)
                    overflow(false)
                }

                // Header row
                View {
                    attr {
                        flexDirectionRow()
                        alignItems(FlexAlign.CENTER)
                        height(a.headerHeight)
                        backgroundColor(headerBg)
                        paddingLeft(a.contentPaddingH)
                        paddingRight(a.contentPaddingH)
                        opacity(if (a.disabled) 0.45f else 1f)
                    }
                    event {
                        click { ctx.toggle() }
                    }

                    // Title text
                    View {
                        attr {
                            flex(1f)
                            flexDirectionColumn()
                            justifyContentCenter()
                        }
                        Text {
                            attr {
                                text(a.title)
                                fontSize(a.fontSize)
                                fontWeightMedium()
                                color(Color(0xFF1A1A1AL))
                            }
                        }
                        if (a.subtitle.isNotEmpty()) {
                            Text {
                                attr {
                                    text(a.subtitle)
                                    fontSize(a.subtitleFontSize)
                                    color(Color(0xFF888888L))
                                    marginTop(2f)
                                }
                            }
                        }
                    }

                    // Arrow icon rotates 90° when expanded
                    if (a.showArrow) {
                        Text {
                            attr {
                                text("›")
                                fontSize(18f)
                                color(if (ctx.isExpanded) a.accentColor else Color(0xFFAAAAAFL))
                                transform(Rotate(if (ctx.isExpanded) 90f else 0f))
                                animate(Animation.easeInOut(0.22f), ctx.isExpanded)
                                marginLeft(8f)
                            }
                        }
                    }
                }

                // Divider below header (only when expanded)
                if (ctx.isExpanded) {
                    View {
                        attr {
                            height(0.5f)
                            backgroundColor(borderColor)
                        }
                    }
                }

                // Body content - shown / hidden with opacity + height animation
                View {
                    attr {
                        flexDirectionColumn()
                        backgroundColor(bodyBg)
                        paddingLeft(a.contentPaddingH)
                        paddingRight(a.contentPaddingH)
                        paddingTop(if (ctx.isExpanded) a.contentPaddingV else 0f)
                        paddingBottom(if (ctx.isExpanded) a.contentPaddingV else 0f)
                        opacity(if (ctx.isExpanded) 1f else 0f)
                        // height collapses to 0 when closed (content still rendered for animation)
                        maxHeight(if (ctx.isExpanded) 9999f else 0f)
                        overflow(false)
                        animate(Animation.easeInOut(0.22f), ctx.isExpanded)
                    }

                    if (ctx.isExpanded) {
                        a.contentBuilder?.invoke(this)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DSL entries
// ---------------------------------------------------------------------------

/** Single accordion panel with a header and collapsible body content. */
fun ViewContainer<*, *>.CollapsePanel(init: CollapseView.() -> Unit) {
    addChild(CollapseView(), init)
}

/**
 * Accordion group: wraps multiple [CollapsePanel] items with shared border styling,
 * matching Ant Design / Vant Collapse component.
 *
 * Use [accordion] = true to allow only one panel open at a time (requires
 * each panel to be driven by observable expanded state from the parent page).
 */
fun ViewContainer<*, *>.Collapse(
    theme: CollapseTheme = CollapseTheme.DEFAULT,
    content: ViewContainer<*, *>.() -> Unit,
) {
    View {
        attr {
            flexDirectionColumn()
            when (theme) {
                CollapseTheme.DEFAULT -> {
                    border(Border(1f, BorderStyle.SOLID, Color(0xFFE8E8E8L)))
                    borderRadius(6f)
                    overflow(false)
                }
                CollapseTheme.CARD -> {
                    // individual panels carry their own borders
                }
                CollapseTheme.BORDERLESS -> {
                    // no outer border
                }
            }
        }
        content()
    }
}
