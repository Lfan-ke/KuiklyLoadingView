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

package com.kuikly.kuiklyloading

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuiklybase.loading.Loading
import com.tencent.kuikly.core.views.ActivityIndicator

@Page("LoadingViewDemoPage")
internal class LoadingViewDemoPage : BasePager() {

    private var showFullScreen by observable(false)
    private var showPartial by observable(false)
    private var showWithTimeout by observable(false)
    private var showCustomSize by observable(false)
    private var showCustomContent by observable(false)
    private var showWithDelay by observable(false)

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flex(1f)
                    padding(16f)
                    flexDirectionColumn()
                }
                View {
                    attr {
                        height(48f)
                        backgroundColor(Color(0xFF1976D2L))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                    }
                    event {
                        click { ctx.showFullScreen = true }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(15f)
                            text("全屏加载（点击显示）")
                        }
                    }
                }
                View {
                    attr {
                        height(48f)
                        backgroundColor(Color(0xFF388E3CL))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                    }
                    event {
                        click { ctx.showPartial = true }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(15f)
                            text("局部加载（点击显示）")
                        }
                    }
                }
                View {
                    attr {
                        height(48f)
                        backgroundColor(Color(0xFFF57C00L))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                    }
                    event {
                        click { ctx.showWithTimeout = true }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(15f)
                            text("3 秒超时自动关闭")
                        }
                    }
                }
                View {
                    attr {
                        height(48f)
                        backgroundColor(Color(0xFF7B1FA2L))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                    }
                    event {
                        click { ctx.showCustomSize = true }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(15f)
                            text("自定义指示器尺寸（indicatorSize=3）")
                        }
                    }
                }
                View {
                    attr {
                        height(48f)
                        backgroundColor(Color(0xFF00796BL))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                    }
                    event {
                        click { ctx.showCustomContent = true }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(15f)
                            text("自定义加载内容（customContent）")
                        }
                    }
                }
                View {
                    attr {
                        height(48f)
                        backgroundColor(Color(0xFF37474FL))
                        borderRadius(8f)
                        justifyContentCenter()
                        alignItemsCenter()
                        marginBottom(12f)
                    }
                    event {
                        click { ctx.showWithDelay = true }
                    }
                    Text {
                        attr {
                            color(Color.WHITE)
                            fontSize(15f)
                            text("延迟 800ms 显示（快速操作不闪烁）")
                        }
                    }
                }
                Loading {
                    attr {
                        visible(ctx.showPartial)
                        fullScreen(false)
                        loadingText("局部加载中…")
                    }
                    event {
                        onTimeout { ctx.showPartial = false }
                    }
                }
            }
            Loading {
                attr {
                    visible(ctx.showFullScreen)
                    fullScreen(true)
                    loadingText("全屏加载中…")
                    maskColor(Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.5f))
                    timeoutMs(3000)
                }
                event {
                    onTimeout { ctx.showFullScreen = false }
                }
            }
            Loading {
                attr {
                    visible(ctx.showWithTimeout)
                    fullScreen(true)
                    loadingText("3s 后自动关闭")
                    timeoutMs(3000)
                }
                event {
                    onTimeout { ctx.showWithTimeout = false }
                }
            }
            Loading {
                attr {
                    visible(ctx.showCustomSize)
                    fullScreen(true)
                    loadingText("自定义尺寸")
                    indicatorSize(3f)
                    timeoutMs(3000)
                }
                event {
                    onTimeout { ctx.showCustomSize = false }
                }
            }
            Loading {
                attr {
                    visible(ctx.showCustomContent)
                    fullScreen(true)
                    timeoutMs(3000)
                    customContent {
                        ActivityIndicator {
                            attr { isGrayStyle(true) }
                        }
                        Text {
                            attr {
                                marginTop(8f)
                                fontSize(12f)
                                color(Color.WHITE)
                                text("自定义内容区域")
                            }
                        }
                    }
                }
                event {
                    onTimeout { ctx.showCustomContent = false }
                }
            }
            Loading {
                attr {
                    visible(ctx.showWithDelay)
                    fullScreen(true)
                    loadingText("延迟后显示")
                    delayMs(800)
                    timeoutMs(3000)
                }
                event {
                    onTimeout { ctx.showWithDelay = false }
                }
            }
        }
    }
}
