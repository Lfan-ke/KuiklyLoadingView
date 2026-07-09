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

import com.tencent.kuikly.core.base.BoxShadow
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

fun ViewContainer<*, *>.ResultView(init: ResultComposeView.() -> Unit) {
    addChild(ResultComposeView(), init)
}

/** Result state variants inspired by Vant/NutUI/Ant Design Mobile. */
enum class ResultType {
    SUCCESS,
    ERROR,
    WARNING,
    EMPTY,
    NETWORK_ERROR,
    FORBIDDEN,
    NOT_FOUND,
    CUSTOM,
}

class ResultAttr : ComposeAttr() {
    internal var resultType by observable(ResultType.EMPTY)
    internal var title by observable("")
    internal var description by observable("")
    internal var iconSize by observable(80f)
    internal var titleFontSize by observable(18f)
    internal var descriptionFontSize by observable(14f)
    internal var actionLabel by observable("")
    internal var actionColor by observable(Color(0xFF1677FFL))
    internal var actionHandler: (() -> Unit)? = null
    internal var secondaryActionLabel by observable("")
    internal var secondaryActionHandler: (() -> Unit)? = null
    internal var paddingVertical by observable(48f)
    internal var customIconText by observable("")
    internal var customIconColor by observable(Color(0xFF1677FFL))
    internal var iconBackgroundColorOverride: Color? = null
    internal var titleColorOverride: Color? = null
    internal var cardBackgroundColor by observable(Color(0x00000000L))  // transparent by default

    fun resultType(type: ResultType) { resultType = type }
    fun title(t: String) { title = t }
    fun description(desc: String) { description = desc }
    fun iconSize(size: Float) { iconSize = size.coerceIn(32f, 160f) }
    fun titleFontSize(size: Float) { titleFontSize = size }
    fun descriptionFontSize(size: Float) { descriptionFontSize = size }
    fun actionButton(label: String, color: Color = Color(0xFF1677FFL), onClick: () -> Unit) {
        actionLabel = label
        actionColor = color
        actionHandler = onClick
    }
    fun secondaryButton(label: String, onClick: () -> Unit) {
        secondaryActionLabel = label
        secondaryActionHandler = onClick
    }
    fun padding(vertical: Float) { paddingVertical = vertical }
    fun customIcon(text: String, color: Color = Color(0xFF1677FFL)) {
        resultType = ResultType.CUSTOM
        customIconText = text
        customIconColor = color
    }
    fun iconBackgroundColor(c: Color) { iconBackgroundColorOverride = c }
    fun titleColor(c: Color) { titleColorOverride = c }
    fun cardBackgroundColor(c: Color) { cardBackgroundColor = c }
}

class ResultEvent : ComposeEvent() {
    internal var onActionHandler: (() -> Unit)? = null
    fun onAction(handler: () -> Unit) { onActionHandler = handler }
}

class ResultComposeView : ComposeView<ResultAttr, ResultEvent>() {
    override fun createAttr(): ResultAttr = ResultAttr()
    override fun createEvent(): ResultEvent = ResultEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flexDirectionColumn()
                    alignItemsCenter()
                    justifyContentCenter()
                    paddingTop(ctx.attr.paddingVertical)
                    paddingBottom(ctx.attr.paddingVertical)
                    paddingLeft(24f)
                    paddingRight(24f)
                    backgroundColor(ctx.attr.cardBackgroundColor)
                }

                val (iconEmoji, iconBg, iconFg) = when (ctx.attr.resultType) {
                    ResultType.SUCCESS       -> Triple("✓",  Color(0xFFE8F7EEL), Color(0xFF52C41AL))
                    ResultType.ERROR        -> Triple("✕",  Color(0xFFFFF1F0L), Color(0xFFFF4D4FL))
                    ResultType.WARNING      -> Triple("!",  Color(0xFFFFFBE6L), Color(0xFFFA8C16L))
                    ResultType.EMPTY        -> Triple("○",  Color(0xFFF5F5F5L), Color(0xFFBBBBBBL))
                    ResultType.NETWORK_ERROR -> Triple("⚡", Color(0xFFF0F5FFL), Color(0xFF4096FFL))
                    ResultType.FORBIDDEN    -> Triple("🔒", Color(0xFFFFF7E6L), Color(0xFFFA8C16L))
                    ResultType.NOT_FOUND    -> Triple("?",  Color(0xFFF5F5F5L), Color(0xFF8C8C8CL))
                    ResultType.CUSTOM       -> Triple(ctx.attr.customIconText, Color(0xFFF0F5FFL), ctx.attr.customIconColor)
                }

                val circleSize = ctx.attr.iconSize * 1.4f
                val effectiveIconBg = ctx.attr.iconBackgroundColorOverride ?: iconBg
                View {
                    attr {
                        size(circleSize, circleSize)
                        borderRadius(circleSize / 2f)
                        backgroundColor(effectiveIconBg)
                        allCenter()
                        marginBottom(24f)
                        boxShadow(BoxShadow(0f, 4f, 20f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.08f)))
                    }
                    Text {
                        attr {
                            text(iconEmoji)
                            fontSize(ctx.attr.iconSize * 0.5f)
                            color(iconFg)
                            fontWeightBold()
                            textAlignCenter()
                        }
                    }
                }

                if (ctx.attr.title.isNotEmpty()) {
                    Text {
                        attr {
                            text(ctx.attr.title)
                            fontSize(ctx.attr.titleFontSize)
                            fontWeightSemiBold()
                            color(ctx.attr.titleColorOverride ?: Color(0xFF262626L))
                            textAlignCenter()
                            marginBottom(8f)
                        }
                    }
                }

                if (ctx.attr.description.isNotEmpty()) {
                    Text {
                        attr {
                            text(ctx.attr.description)
                            fontSize(ctx.attr.descriptionFontSize)
                            color(Color(0xFF8C8C8CL))
                            textAlignCenter()
                            lineHeight(ctx.attr.descriptionFontSize * 1.6f)
                            marginBottom(32f)
                        }
                    }
                }

                if (ctx.attr.actionLabel.isNotEmpty()) {
                    View {
                        attr {
                            height(44f)
                            minWidth(160f)
                            paddingLeft(24f)
                            paddingRight(24f)
                            borderRadius(22f)
                            backgroundColor(ctx.attr.actionColor)
                            allCenter()
                            marginBottom(12f)
                            boxShadow(BoxShadow(0f, 4f, 12f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.15f)))
                        }
                        event {
                            click {
                                ctx.attr.actionHandler?.invoke()
                                ctx.event.onActionHandler?.invoke()
                            }
                        }
                        Text {
                            attr {
                                text(ctx.attr.actionLabel)
                                fontSize(15f)
                                fontWeightMedium()
                                color(Color(0xFFFFFFFFL))
                            }
                        }
                    }
                }

                if (ctx.attr.secondaryActionLabel.isNotEmpty()) {
                    View {
                        attr {
                            height(44f)
                            minWidth(160f)
                            paddingLeft(24f)
                            paddingRight(24f)
                            borderRadius(22f)
                            backgroundColor(Color(0xFFF5F5F5L))
                            allCenter()
                        }
                        event { click { ctx.attr.secondaryActionHandler?.invoke() } }
                        Text {
                            attr {
                                text(ctx.attr.secondaryActionLabel)
                                fontSize(15f)
                                fontWeightMedium()
                                color(Color(0xFF595959L))
                            }
                        }
                    }
                }
            }
        }
    }
}
