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
 * Semantic type of an [AlertView] - mirrors Ant Design / Vant / NutUI Alert types.
 *
 * - [SUCCESS] - green, confirms a successful operation
 * - [WARNING] - yellow, warns the user about a potential issue
 * - [ERROR]   - red, signals an error or destructive action
 * - [INFO]    - blue, provides neutral informational context
 */
enum class AlertType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
}

class AlertAttr : ComposeAttr() {

    internal var alertType by observable(AlertType.INFO)
    internal var title by observable("")
    internal var description by observable("")
    internal var showIcon by observable(true)
    internal var closable by observable(false)
    internal var closed by observable(false)
    internal var fontSize by observable(14f)
    internal var descFontSize by observable(12f)
    internal var borderRadius by observable(6f)
    internal var paddingH by observable(12f)
    internal var paddingV by observable(10f)

    fun type(t: AlertType) { alertType = t }
    fun title(t: String) { title = t }
    fun description(d: String) { description = d }
    fun showIcon(show: Boolean) { showIcon = show }
    fun closable(c: Boolean) { closable = c }
    fun fontSize(s: Float) { fontSize = s }
    fun descFontSize(s: Float) { descFontSize = s }
    fun borderRadius(r: Float) { borderRadius = r }
    fun paddingH(p: Float) { paddingH = p }
    fun paddingV(p: Float) { paddingV = p }
}

class AlertEvent : ComposeEvent() {
    var onClose: (() -> Unit)? = null
}

// ---------------------------------------------------------------------------
// View
// ---------------------------------------------------------------------------

class AlertView : ComposeView<AlertAttr, AlertEvent>() {

    override fun createAttr(): AlertAttr = AlertAttr()
    override fun createEvent(): AlertEvent = AlertEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            if (!ctx.attr.closed) {
                val a = ctx.attr
                val (bgColor, borderColor, iconText, iconColor, titleColor) = when (a.alertType) {
                    AlertType.SUCCESS -> listOf(
                        Color(0xFFF6FFEDL), Color(0xFFB7EBB4L), "✓",
                        Color(0xFF52C41AL), Color(0xFF135200L),
                    )
                    AlertType.WARNING -> listOf(
                        Color(0xFFFFFBE6L), Color(0xFFFFE58FL), "⚠",
                        Color(0xFFFAAD14L), Color(0xFF614700L),
                    )
                    AlertType.ERROR -> listOf(
                        Color(0xFFFFF2F0L), Color(0xFFFFCCC7L), "✕",
                        Color(0xFFFF4D4FL), Color(0xFF5C0011L),
                    )
                    AlertType.INFO -> listOf(
                        Color(0xFFE6F4FFL), Color(0xFF91CAFFFL), "ℹ",
                        Color(0xFF1677FFL), Color(0xFF003EB3L),
                    )
                }
                val descColor = Color(0xFF595959L)

                View {
                    attr {
                        flexDirectionRow()
                        alignItems(FlexAlign.FLEX_START)
                        paddingLeft(a.paddingH)
                        paddingRight(a.paddingH)
                        paddingTop(a.paddingV)
                        paddingBottom(a.paddingV)
                        borderRadius(a.borderRadius)
                        backgroundColor(bgColor)
                        border(Border(1f, BorderStyle.SOLID, borderColor))
                        opacity(if (a.closed) 0f else 1f)
                        animate(Animation.easeInOut(0.25f), a.closed)
                    }

                    if (a.showIcon) {
                        Text {
                            attr {
                                text(iconText as String)
                                fontSize(a.fontSize * 1.1f)
                                color(iconColor as Color)
                                marginRight(8f)
                                marginTop(1f)
                            }
                        }
                    }

                    View {
                        attr { flex(1f); flexDirectionColumn() }

                        if (a.title.isNotEmpty()) {
                            Text {
                                attr {
                                    text(a.title)
                                    fontSize(a.fontSize)
                                    color(titleColor as Color)
                                    fontWeightMedium()
                                    if (a.description.isNotEmpty()) marginBottom(4f)
                                }
                            }
                        }

                        if (a.description.isNotEmpty()) {
                            Text {
                                attr {
                                    text(a.description)
                                    fontSize(a.descFontSize)
                                    color(descColor)
                                }
                            }
                        }
                    }

                    if (a.closable) {
                        Text {
                            attr {
                                text("✕")
                                fontSize(a.fontSize * 0.9f)
                                color(Color(0xFF8C8C8CL))
                                marginLeft(8f)
                                marginTop(2f)
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
// DSL entry
// ---------------------------------------------------------------------------

fun ViewContainer<*, *>.Alert(init: AlertView.() -> Unit) {
    addChild(AlertView(), init)
}
