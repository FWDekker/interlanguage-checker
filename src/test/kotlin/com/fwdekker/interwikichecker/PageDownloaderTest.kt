package com.fwdekker.interwikichecker

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import net.sourceforge.jwbf.core.contentRep.Article
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.net.MalformedURLException
import java.net.URL

/**
 * Unit tests for [PageDownloader].
 */
internal object PageDownloaderTest : Spek({
    describe("page downloader") {
        val article = mock<Article> {
            on { it.text } doReturn "contentz"
        }
        val bot = mock<MediaWikiBot> {
            on { it.getArticle(any()) } doReturn article
        }
        val factory = mock<MediaWikiBotFactory> {
            on { it.createMediaWikiBot(any()) } doReturn bot
        }
        val downloader = PageDownloader(factory, mapOf(
            Pair("en", "http://example.com/")
        ))


        it("downloads the article") {
            val page = downloader.downloadPage(PageLocation("en", "Walkings"))

            verify(factory).createMediaWikiBot(URL("http://example.com/"))
            verify(bot).getArticle("Walkings")
            assertThat(page.contents).isEqualTo("contentz")
        }

        it("does not create a new bot if the same language is reused") {
            downloader.downloadPage(PageLocation("en", "Wandered"))
            downloader.downloadPage(PageLocation("en", "Syncopic"))

            verify(factory).createMediaWikiBot(URL("http://example.com/"))
        }

        it("throws an NPE if the language is not recognized") {
            assertThatThrownBy { downloader.downloadPage(PageLocation("im", "Guzzlers")) }
                .isInstanceOf(MalformedURLException::class.java)
        }
    }
})
