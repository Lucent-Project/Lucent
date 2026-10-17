package com.lucent.app.network

import com.lucent.app.data.WebSearchEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebSearchClientTest {

    @Test
    fun parseSogouMobileHtml() {
        val html = """
            <h3 class=vr-tit data-click-sugg-title><a class="resultLink" href="./id=30f63ea4-45a4-4f16-bec6-6c9ccd433c25/keyword=kotlin/sec=gGDNvufPMPl4txjwtDlgiQ../entryTime=1792251739138/tc?clk=1&wml=1&url=https%3A%2F%2Fkotlinlang.org%2F&dp=1&rcer=SVkTqoSt&is_per=0&pno=1&vrid=30010247&title=Kotlin%20Programming%20Language&linkid=title" id="sogou_vr_30010247_1"> <em>Kotlin</em> Programming Language </a></h3>
            <h3 class="vr-tit clamp2 " vrcid="title.9e6f38a"><a class="resultLink  " href="./id=30f63ea4-45a4-4f16-bec6-6c9ccd433c25/keyword=kotlin/sec=gGDNvufPMPl4txjwtDlgiQ../entryTime=1792251739138/tc?clk=7&wml=1&url=https%3A%2F%2Fwww.runoob.com%2Fkotlin%2Fkotlin-basic-syntax.html&pcurl=https%3A%2F%2Fwww.runoob.com%2Fkotlin%2Fkotlin-basic-syntax.html&dp=1&rcer=SVkTqoSt&is_per=0&pno=1&vrid=30000000&title=Kotlin%20%E5%9F%BA%E7%A1%80%E8%AF%AD%E6%B3%95%20%7C%20%E8%8F%9C%E9%B8%9F%E6%95%99%E7%A8%8B&linkid=title"> <em>Kotlin</em> 基础语法 | 菜鸟教程 </a></h3>
        """.trimIndent()
        val results = WebSearchClient.parseSogou(html)
        assertEquals(2, results.size)
        assertEquals("Kotlin Programming Language", results[0].title)
        assertEquals("https://kotlinlang.org/", results[0].url)
        assertEquals("Kotlin 基础语法 | 菜鸟教程", results[1].title)
        assertEquals("https://www.runoob.com/kotlin/kotlin-basic-syntax.html", results[1].url)
    }

    @Test
    fun parseBaiduCoscCardHtml() {
        val html = """
            <div class="c-container" data-click-info='{&quot;dataUrl&quot;:&quot;https://kotlinlang.org/&quot;}'>
            <div data-module="title" role="title"><h3 class="cosc-title title_4utjm cos-line-clamp-1 "><span class="cosc-title-a cosc-title-md " href=""><span class="cosc-title-slot"><span class="tts-b-hl "><!--s-text--><em>Kotlin</em> Programming Language<!--/s-text--></span></span></span></h3></div>
            </div>
            <div class="c-container" data-click-info='{&quot;dataUrl&quot;:&quot;https://developer.android.google.cn/kotlin/index.html&quot;}'>
            <div data-module="title" role="title"><h3 class="cosc-title title_4utjm cos-line-clamp-1 "><span class="cosc-title-a cosc-title-md " href=""><span class="cosc-title-slot"><span class="tts-b-hl "><!--s-text-->Kotlin 和 Android | Android Developers<!--/s-text--></span></span></span></h3></div>
            </div>
        """.trimIndent()
        val results = WebSearchClient.parseBaidu(html)
        assertEquals(2, results.size)
        assertEquals("Kotlin Programming Language", results[0].title)
        assertEquals("https://kotlinlang.org/", results[0].url)
        assertEquals("Kotlin 和 Android | Android Developers", results[1].title)
        assertEquals("https://developer.android.google.cn/kotlin/index.html", results[1].url)
    }

    @Test
    fun resolveRedirectSogouUrl() {
        val sogouHref = "./tc?clk=1&wml=1&url=https%3A%2F%2Fkotlinlang.org%2Fdocs%2Fhome.html&dp=1"
        assertEquals("https://kotlinlang.org/docs/home.html", WebSearchClient.resolveRedirect(sogouHref))

        val sogouHrefAmp = "./tc?clk=1&amp;wml=1&amp;url=https%3A%2F%2Fexample.com%2Fpath&amp;dp=1"
        assertEquals("https://example.com/path", WebSearchClient.resolveRedirect(sogouHrefAmp))

        val directQuestion = "/search/tc?url=https%3A%2F%2Fexample.com%2Ftest"
        assertEquals("https://example.com/test", WebSearchClient.resolveRedirect(directQuestion))
    }

    @Test
    fun resolveRedirectStandardFormats() {
        val ddgHref = "/l/?uddg=https%3A%2F%2Fexample.com%2Fddg&rut=1"
        assertEquals("https://example.com/ddg", WebSearchClient.resolveRedirect(ddgHref))

        val googleHref = "/url?q=https%3A%2F%2Fexample.com%2Fgoogle&sa=U"
        assertEquals("https://example.com/google", WebSearchClient.resolveRedirect(googleHref))

        val directHref = "https://example.com/direct"
        assertEquals("https://example.com/direct", WebSearchClient.resolveRedirect(directHref))

        val protocolRelative = "//example.com/relative"
        assertEquals("https://example.com/relative", WebSearchClient.resolveRedirect(protocolRelative))
    }

    @Test
    fun webSearchEngineFromKey() {
        assertEquals(WebSearchEngine.AUTO, WebSearchEngine.fromKey("auto"))
        assertEquals(WebSearchEngine.GOOGLE, WebSearchEngine.fromKey("google"))
        assertEquals(WebSearchEngine.BING, WebSearchEngine.fromKey("bing"))
        assertEquals(WebSearchEngine.DUCKDUCKGO, WebSearchEngine.fromKey("duckduckgo"))
        assertEquals(WebSearchEngine.BRAVE, WebSearchEngine.fromKey("brave"))
        assertEquals(WebSearchEngine.YANDEX, WebSearchEngine.fromKey("yandex"))
        assertEquals(WebSearchEngine.MOJEEK, WebSearchEngine.fromKey("mojeek"))
        assertEquals(WebSearchEngine.BAIDU, WebSearchEngine.fromKey("baidu"))
        assertEquals(WebSearchEngine.SOGOU, WebSearchEngine.fromKey("sogou"))
        assertEquals(WebSearchEngine.SO360, WebSearchEngine.fromKey("so360"))
        assertEquals(WebSearchEngine.WIKIPEDIA, WebSearchEngine.fromKey("wikipedia"))
        assertEquals(WebSearchEngine.AUTO, WebSearchEngine.fromKey("unknown"))
        assertEquals(WebSearchEngine.AUTO, WebSearchEngine.fromKey(null))
        assertEquals(WebSearchEngine.AUTO, WebSearchEngine.fromKey("   "))
    }

    @Test
    fun autoChainOrdering() {
        val latinChain = WebSearchClient.autoChain("kotlin language")
        assertTrue(latinChain.contains(WebSearchEngine.BING))
        assertTrue(latinChain.contains(WebSearchEngine.GOOGLE))
        assertEquals(WebSearchEngine.BING, latinChain.first())

        val cjkChain = WebSearchClient.autoChain("搜索")
        assertTrue(cjkChain.contains(WebSearchEngine.BAIDU))
        assertTrue(cjkChain.contains(WebSearchEngine.SOGOU))
        assertEquals(WebSearchEngine.BING, cjkChain.first())
    }
}
