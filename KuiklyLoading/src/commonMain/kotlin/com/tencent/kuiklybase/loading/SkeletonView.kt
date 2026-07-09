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
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

fun ViewContainer<*, *>.Skeleton(init: SkeletonView.() -> Unit) {
    addChild(SkeletonView(), init)
}

/**
 * Built-in color themes for [SkeletonView].
 * Pass to [SkeletonAttr.theme] to apply light or dark skeleton colors without
 * manually specifying [SkeletonAttr.baseColor] and [SkeletonAttr.highlightColor].
 */
enum class SkeletonTheme {
    /** Default light theme - gray blocks on white background (Material/Ant Design style). */
    LIGHT,
    /** Dark theme - deep gray blocks for dark-mode layouts (NutUI/Varlet dark style). */
    DARK,
    /** Subtle blue-tinted theme matching Ant Design brand palette. */
    BLUE,
}

/** Animation style for skeleton placeholder elements. */
enum class SkeletonAnimation {
    /** Static gray blocks, no animation. */
    NONE,
    /** Alternates opacity to create a breathing pulse effect (battery-friendly). */
    PULSE,
    /** Rapid color flash simulating a highlight sweep (like CSS shimmer). */
    SHIMMER,
}

/**
 * A single placeholder row in a [SkeletonView].
 *
 * @param widthFraction Fraction of available width (0.0–1.0). Ignored when [isCircle] is true.
 * @param height Row height in dp.
 * @param cornerRadius Border radius applied to rectangular rows.
 * @param isCircle When true, renders a circle using [height] as the diameter.
 * @param marginBottom Space below this row before the next.
 */
data class SkeletonRow(
    val widthFraction: Float = 1f,
    val height: Float = 14f,
    val cornerRadius: Float = 3f,
    val isCircle: Boolean = false,
    val marginBottom: Float = -1f, // -1 = use SkeletonAttr.rowSpacing
)

/**
 * Built-in skeleton layout presets matching common UI patterns.
 * Pass any of these to [SkeletonAttr.preset].
 */
object SkeletonPreset {
    /** Full article: large image banner + title + three text lines. */
    val article: List<SkeletonRow> = listOf(
        SkeletonRow(1f, 160f, 6f),
        SkeletonRow(0.75f, 20f, 4f),
        SkeletonRow(1f, 14f),
        SkeletonRow(1f, 14f),
        SkeletonRow(0.55f, 14f),
    )

    /** Card tile: medium image + title + two text lines. */
    val card: List<SkeletonRow> = listOf(
        SkeletonRow(1f, 120f, 6f),
        SkeletonRow(0.7f, 18f, 4f),
        SkeletonRow(1f, 13f),
        SkeletonRow(0.6f, 13f),
    )

    /** User profile: circular avatar + name + short subtitle. */
    val profile: List<SkeletonRow> = listOf(
        SkeletonRow(height = 56f, isCircle = true),
        SkeletonRow(0.5f, 18f, 4f),
        SkeletonRow(0.35f, 13f),
    )

    /** List-item row: small avatar + two lines of text. */
    val listItem: List<SkeletonRow> = listOf(
        SkeletonRow(height = 44f, isCircle = true),
        SkeletonRow(0.55f, 16f, 3f),
        SkeletonRow(0.75f, 13f),
    )

    /** Plain text paragraph: title + three lines (mixed widths). */
    val paragraph: List<SkeletonRow> = listOf(
        SkeletonRow(0.55f, 20f, 4f),
        SkeletonRow(1f, 14f),
        SkeletonRow(1f, 14f),
        SkeletonRow(0.8f, 14f),
    )

    /** Button placeholder strip. */
    val button: List<SkeletonRow> = listOf(
        SkeletonRow(1f, 40f, 6f),
    )
}

class SkeletonAttr : ComposeAttr() {

    internal var rows by observable(SkeletonPreset.paragraph)
    internal var animation by observable(SkeletonAnimation.SHIMMER)
    internal var loading by observable(true)
    internal var repeatCount by observable(1)
    internal var rowSpacing by observable(10f)
    internal var repeatSpacing by observable(20f)
    internal var contentPadding by observable(16f)
    internal var baseColor by observable(Color(0xFFE8E8E8L))
    internal var highlightColor by observable(Color(0xFFF2F2F2L))
    internal var round by observable(false)
    internal var throttleMs by observable(0)
    internal var contentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    /** Replace default paragraph preset with a custom row list. */
    fun rows(list: List<SkeletonRow>) { rows = list }

    /** Convenience vararg overload. */
    fun rows(vararg row: SkeletonRow) { rows = row.toList() }

    /** Apply one of the built-in [SkeletonPreset] layouts. */
    fun preset(layout: List<SkeletonRow>) { rows = layout }

    /** Animation style. Defaults to [SkeletonAnimation.SHIMMER]. */
    fun animation(anim: SkeletonAnimation) { animation = anim }

    /** Shorthand to enable or disable animation. */
    fun animated(enable: Boolean) {
        animation = if (enable) SkeletonAnimation.SHIMMER else SkeletonAnimation.NONE
    }

    /**
     * Convenience setter that applies a pre-configured [SkeletonTheme].
     * Overrides [baseColor] and [highlightColor] with theme defaults.
     */
    fun theme(t: SkeletonTheme) {
        when (t) {
            SkeletonTheme.LIGHT -> {
                baseColor = Color(0xFFE8E8E8L)
                highlightColor = Color(0xFFF2F2F2L)
            }
            SkeletonTheme.DARK -> {
                baseColor = Color(0xFF3A3A3AL)
                highlightColor = Color(0xFF4E4E4EL)
            }
            SkeletonTheme.BLUE -> {
                baseColor = Color(0xFFD6E4FFL)
                highlightColor = Color(0xFFE8F0FEL)
            }
        }
    }

    /**
     * Toggle between skeleton and real content.
     * When set to false, [content] lambda is rendered instead.
     */
    fun loading(show: Boolean) { loading = show }

    /** Repeat the skeleton block N times — useful for list placeholders. */
    fun repeatCount(n: Int) { repeatCount = n.coerceIn(1, 20) }

    /** Vertical gap between skeleton rows. */
    fun rowSpacing(sp: Float) { rowSpacing = sp.coerceAtLeast(0f) }

    /** Vertical gap between repeated skeleton blocks. */
    fun repeatSpacing(sp: Float) { repeatSpacing = sp.coerceAtLeast(0f) }

    /** Inner padding around the skeleton content area. */
    fun contentPadding(p: Float) { contentPadding = p.coerceAtLeast(0f) }

    /** Base (darker) color of the placeholder blocks. */
    fun baseColor(color: Color) { baseColor = color }

    /** Highlight (lighter) color used during animation alternation. */
    fun highlightColor(color: Color) { highlightColor = color }

    /**
     * When true, text-line rows use pill-shaped (fully rounded) corners —
     * matching the Ant Design `round` prop style.
     */
    fun round(enabled: Boolean) { round = enabled }

    /**
     * Delay in ms before the skeleton appears after [loading] becomes true.
     * If loading finishes before the delay, skeleton is never shown —
     * preventing a flash for fast requests (like Element Plus `throttle`).
     */
    fun throttleMs(ms: Int) { throttleMs = ms.coerceAtLeast(0) }

    /**
     * Content to display when [loading] is false.
     * The builder receives the parent container as receiver.
     */
    fun content(builder: ViewContainer<*, *>.() -> Unit) { contentBuilder = builder }

    fun size(w: Float, h: Float) {
        if (!w.isNaN()) width(w)
        if (!h.isNaN()) height(h)
    }
}

class SkeletonView : ComposeView<SkeletonAttr, ComposeEvent>() {

    private var animTick by observable(false)
    private var actuallyVisible by observable(true)
    private var animHandle = ""
    private var throttleHandle = ""

    override fun createAttr() = SkeletonAttr()

    override fun didInit() {
        super.didInit()
        if (attr.animation != SkeletonAnimation.NONE) startAnim()
    }

    override fun viewWillUnload() {
        super.viewWillUnload()
        stopAnim()
        stopThrottle()
    }

    override fun attr(init: SkeletonAttr.() -> Unit) {
        val prevAnim = attr.animation
        val prevLoading = attr.loading
        super.attr(init)

        if (attr.animation != prevAnim) {
            stopAnim()
            if (attr.animation != SkeletonAnimation.NONE) startAnim()
        }

        if (attr.loading && !prevLoading) {
            val ms = attr.throttleMs
            stopThrottle()
            if (ms > 0) {
                actuallyVisible = false
                throttleHandle = setTimeout(pagerId, ms) {
                    if (attr.loading) actuallyVisible = true
                }
            } else {
                actuallyVisible = true
            }
        } else if (!attr.loading && prevLoading) {
            stopThrottle()
            actuallyVisible = false
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            vif({ ctx.attr.loading && ctx.actuallyVisible }) {
                View {
                    attr {
                        flexDirectionColumn()
                        padding(ctx.attr.contentPadding)
                    }
                    val color = if (ctx.animTick) ctx.attr.highlightColor else ctx.attr.baseColor
                    repeat(ctx.attr.repeatCount) { idx ->
                        ctx.renderBlock(this, color)
                        if (idx < ctx.attr.repeatCount - 1) {
                            View { attr { height(ctx.attr.repeatSpacing) } }
                        }
                    }
                }
            }
            vif({ !ctx.attr.loading }) {
                ctx.attr.contentBuilder?.invoke(this)
            }
        }
    }

    private fun renderBlock(container: ViewContainer<*, *>, color: Color) {
        val ctx = this
        container.apply {
            ctx.attr.rows.forEachIndexed { rowIdx, row ->
                val spacing = if (row.marginBottom >= 0f) row.marginBottom else ctx.attr.rowSpacing
                if (row.isCircle) {
                    View {
                        attr {
                            val d = row.height
                            width(d)
                            height(d)
                            borderRadius(d / 2f)
                            backgroundColor(color)
                            marginBottom(spacing)
                        }
                    }
                } else {
                    val radius = if (ctx.attr.round) row.height / 2f else row.cornerRadius
                    View {
                        attr {
                            flexDirectionRow()
                            marginBottom(spacing)
                        }
                        View {
                            attr {
                                height(row.height)
                                flex(row.widthFraction.coerceIn(0.01f, 1f))
                                borderRadius(radius)
                                backgroundColor(color)
                            }
                        }
                        if (row.widthFraction < 0.99f) {
                            View { attr { flex((1f - row.widthFraction).coerceAtLeast(0.01f)) } }
                        }
                    }
                }
            }
        }
    }

    private fun startAnim() {
        val intervalMs = when (attr.animation) {
            SkeletonAnimation.SHIMMER -> 450
            SkeletonAnimation.PULSE -> 750
            SkeletonAnimation.NONE -> return
        }
        animHandle = setTimeout(pagerId, intervalMs) {
            animTick = !animTick
            startAnim()
        }
    }

    private fun stopAnim() {
        if (animHandle.isNotEmpty()) {
            clearTimeout(animHandle)
            animHandle = ""
        }
    }

    private fun stopThrottle() {
        if (throttleHandle.isNotEmpty()) {
            clearTimeout(throttleHandle)
            throttleHandle = ""
        }
    }
}
