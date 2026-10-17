package com.lucent.app.ui

import androidx.compose.ui.text.LinkAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals

class UrlRecognitionTest {

    @Test
    fun testBuildAnnotatedStringWithUrlsRecognizesUrls() {
        val text = "Visit https://lucent.app or http://example.com/path?arg=1#frag for details."
        val annotated = buildAnnotatedStringWithUrls(text)
        assertEquals(text, annotated.text)
        val links = annotated.getLinkAnnotations(0, annotated.length)
        assertEquals(2, links.size)
        val urls = links.map { (it.item as LinkAnnotation.Url).url }
        assertEquals("https://lucent.app", urls[0])
        assertEquals("http://example.com/path?arg=1#frag", urls[1])
    }

    @Test
    fun testBuildAnnotatedStringWithUrlsIgnoresTrailingPunctuation() {
        val text = "Check out https://github.com/Lucent-Project/Lucent, and https://kotlinlang.org."
        val annotated = buildAnnotatedStringWithUrls(text)
        val links = annotated.getLinkAnnotations(0, annotated.length)
        assertEquals(2, links.size)
        val urls = links.map { (it.item as LinkAnnotation.Url).url }
        assertEquals("https://github.com/Lucent-Project/Lucent", urls[0])
        assertEquals("https://kotlinlang.org", urls[1])
    }

    @Test
    fun testBuildAnnotatedStringWithUrlsRecognizesMarkdownLinksAndBareUrls() {
        val text = "See [Lucent](https://lucent.app) and bare https://lucent.app/docs!"
        val annotated = buildAnnotatedStringWithUrls(text)
        val links = annotated.getLinkAnnotations(0, annotated.length)
        assertEquals(2, links.size)
        val urls = links.map { (it.item as LinkAnnotation.Url).url }
        assertEquals("https://lucent.app", urls[0])
        assertEquals("https://lucent.app/docs", urls[1])
    }
}
