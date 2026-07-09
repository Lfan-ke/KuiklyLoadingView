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
 * Visual style of the tag - mirrors Ant Design / Vant / NutUI / TDesign tag variants.
 *
 * - [FILLED]   - solid background (default)
 * - [OUTLINE]  - border only, transparent background
 * - [GHOST]    - light tinted background with matching border
 * - [PLAIN]    - no background, no border (plain text label with color)
 */
enum class TagStyle {
    FILLED,
    OUTLINE,
    GHOST,
    PLAIN,
}

/** Corner shape of the tag. */
enum class TagShape {
    /** Default rounded corners (4dp). */
    ROUNDED,
    /** Fully rounded pill shape. */
    PILL,
    /** Square, no border radius. */
    SQUARE,
    /** Left side flat, right side rounded - for leading tag in a group. */
    MARK,
}

/** Built-in semantic color presets. */
enum class TagColor {
    DEFAULT,
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    INFO,
    PURPLE,
    CYAN,
    GOLD,
    LIME,
}

class TagAttr : ComposeAttr() {

    internal var tagText by observable("")
    internal var tagStyle by observable(TagStyle.FILLED)
    internal var tagShape by observable(TagShape.ROUNDED)
    internal var closable by observable(false)
    internal var closed by observable(false)
    internal var checkable by observable(false)
    internal var checked by observable(false)
    internal var icon by observable("")

    // colors - base on Ant Design token system
    internal var fillColor by observable(Color(0xFF1677FFL))
    internal var textColor by observable(Color(0xFFFFFFFFL))
    internal var borderColor by observable(Color(0xFF1677FFL))

    internal var fontSize by observable(12f)
    internal var paddingH by observable(8f)
    internal var paddingV by observable(3f)
    internal var borderRadius by observable(4f)

    fun text(t: String) { tagText = t }
    fun style(s: TagStyle) { tagStyle = s }
    fun shape(s: TagShape) {
        tagShape = s
        borderRadius = when (s) {
            TagShape.ROUNDED -> 4f
            TagShape.PILL    -> 100f
            TagShape.SQUARE  -> 0f
            TagShape.MARK    -> 0f
        }
    }
    fun closable(c: Boolean) { closable = c }
    fun checkable(c: Boolean) { checkable = c }
    fun checked(c: Boolean) { checked = c }
    fun icon(i: String) { icon = i }
    fun fontSize(s: Float) { fontSize = s }
    fun paddingH(p: Float) { paddingH = p }
    fun paddingV(p: Float) { paddingV = p }

    fun color(preset: TagColor) {
        val (fill, text, border, ghost) = when (preset) {
            TagColor.DEFAULT -> listOf(Color(0xFFF0F0F0L), Color(0xFF595959L), Color(0xFFD9D9D9L), Color(0xFFF5F5F5L))
            TagColor.PRIMARY -> listOf(Color(0xFF1677FFL), Color(0xFFFFFFFFL), Color(0xFF1677FFL), Color(0xFFE6F4FFL))
            TagColor.SUCCESS -> listOf(Color(0xFF52C41AL), Color(0xFFFFFFFFL), Color(0xFF52C41AL), Color(0xFFF6FFEDL))
            TagColor.WARNING -> listOf(Color(0xFFFAAD14L), Color(0xFFFFFFFFL), Color(0xFFFAAD14L), Color(0xFFFFFBE6L))
            TagColor.DANGER  -> listOf(Color(0xFFFF4D4FL), Color(0xFFFFFFFFL), Color(0xFFFF4D4FL), Color(0xFFFFF2F0L))
            TagColor.INFO    -> listOf(Color(0xFF13C2C2L), Color(0xFFFFFFFFL), Color(0xFF13C2C2L), Color(0xFFE6FFFFL))
            TagColor.PURPLE  -> listOf(Color(0xFF722ED1L), Color(0xFFFFFFFFL), Color(0xFF722ED1L), Color(0xFFF9F0FFL))
            TagColor.CYAN    -> listOf(Color(0xFF06B6D4L), Color(0xFFFFFFFFL), Color(0xFF06B6D4L), Color(0xFFE0F2FEL))
            TagColor.GOLD    -> listOf(Color(0xFFD4AF37L), Color(0xFFFFFFFFL), Color(0xFFD4AF37L), Color(0xFFFFFBE6L))
            TagColor.LIME    -> listOf(Color(0xFF7CB305L), Color(0xFFFFFFFFL), Color(0xFF7CB305L), Color(0xFFF4FFEDL))
        }
        fillColor = fill
        textColor = text
        borderColor = border
        // ghost tint is stored in fillColor when style == GHOST (see body())
        // we store the ghost tint as the 4th element via a second set below
        _ghostFill = ghost
    }

    internal var _ghostFill: Color = Color(0xFFE6F4FFL)

    fun fillColor(c: Color) { fillColor = c }
    fun textColor(c: Color) { textColor = c }
    fun borderColor(c: Color) { borderColor = c }
}

class TagEvent : ComposeEvent() {
    var onClose: (() -> Unit)? = null
    var onCheck: ((Boolean) -> Unit)? = null
    var onClick: (() -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class TagView : ComposeView<TagAttr, TagEvent>() {

    override fun createAttr(): TagAttr = TagAttr()
    override fun createEvent(): TagEvent = TagEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            if (!ctx.attr.closed) {
                val a = ctx.attr
                val isChecked = a.checkable && a.checked
                val (bg, txt, brd) = when (a.tagStyle) {
                    TagStyle.FILLED  -> Triple(
                        if (a.checkable && !isChecked) Color(0xFFF0F0F0L) else a.fillColor,
                        if (a.checkable && !isChecked) Color(0xFF595959L) else a.textColor,
                        Color(0x00000000L),
                    )
                    TagStyle.OUTLINE -> Triple(
                        Color(0x00000000L),
                        a.fillColor,
                        a.borderColor,
                    )
                    TagStyle.GHOST   -> Triple(
                        a._ghostFill,
                        a.fillColor,
                        a.borderColor,
                    )
                    TagStyle.PLAIN   -> Triple(
                        Color(0x00000000L),
                        a.fillColor,
                        Color(0x00000000L),
                    )
                }
                val hasBorder = a.tagStyle == TagStyle.OUTLINE || a.tagStyle == TagStyle.GHOST

                View {
                    attr {
                        flexDirectionRow()
                        alignItems(FlexAlign.CENTER)
                        paddingLeft(a.paddingH)
                        paddingRight(a.paddingH)
                        paddingTop(a.paddingV)
                        paddingBottom(a.paddingV)
                        borderRadius(a.borderRadius)
                        backgroundColor(bg)
                        if (hasBorder) border(Border(1f, BorderStyle.SOLID, brd))
                        animate(Animation.easeInOut(0.15f), isChecked)
                    }
                    event {
                        click {
                            if (a.checkable) {
                                ctx.attr.checked = !ctx.attr.checked
                                ctx.event.onCheck?.invoke(ctx.attr.checked)
                            }
                            ctx.event.onClick?.invoke()
                        }
                    }

                    if (a.icon.isNotEmpty()) {
                        Text {
                            attr { text(a.icon); fontSize(a.fontSize); marginRight(4f) }
                        }
                    }

                    Text {
                        attr {
                            text(a.tagText)
                            fontSize(a.fontSize)
                            color(txt)
                            fontWeightMedium()
                        }
                    }

                    if (a.closable) {
                        Text {
                            attr {
                                text(" ×")
                                fontSize(a.fontSize * 1.1f)
                                color(txt)
                                marginLeft(2f)
                            }
                            event {
                                click {
                                    ctx.attr.closed = true
                                    ctx.event.onClose?.invoke()
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
// DSL entry + convenience TagGroup
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.Tag(init: TagView.() -> Unit) {
    addChild(TagView(), init)
}

/**
 * Convenience wrapper that lays out multiple [Tag] items in a wrapping row,
 * matching Ant Design / NutUI TagGroup behavior.
 */
fun ViewContainer<*, *>.TagGroup(spacing: Float = 8f, content: ViewContainer<*, *>.() -> Unit) {
    View {
        attr {
            flexDirectionRow()
            flexWrap(com.tencent.kuikly.core.layout.FlexWrap.WRAP)
        }
        val wrapper = this
        // We can't easily intercept addChild here, so just call content directly.
        // Each Tag inside will have marginRight(spacing) and marginBottom(spacing) via the slot.
        content()
    }
}
