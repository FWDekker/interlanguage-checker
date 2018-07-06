package com.fwdekker.interwikichecker

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Integration tests for [PageDownloader].
 *
 * Requires an Internet connection.
 */
internal object PageDownloaderIntegrationTest : Spek({
    describe("downloading pages") {
        fun createDownloader() = PageDownloader(MediaWikiBotFactory(mapOf(
            Pair("en", "https://fallout.wikia.com/"),
            Pair("it", "http://it.fallout.wikia.com/")
        )))


        // Sanity test
        it("can connect to the Internet") {
            createDownloader().downloadPage(PageLocation("en", "Fallout_Wiki"))
        }

        describe("downloading pages") {
            it("throws an exception if the wiki does not exist") {
                val downloader = PageDownloader(MediaWikiBotFactory(mapOf(Pair("en", "http://invalid.domain/"))))

                assertThatThrownBy { downloader.downloadPage(PageLocation("en", "Home")) }
                    .isInstanceOf(IllegalStateException::class.java)
            }

            it("returns an empty page if the page does not exist on the wiki") {
                val page = createDownloader().downloadPage(PageLocation("en", "_DoesNotExist"))

                assertThat(page.contents).isEmpty()
            }

            it("returns the contents of a page") {
                val page = createDownloader().downloadPage(PageLocation("en", "Fallout_Wiki"))

                assertThat(page.contents).isNotEmpty()
            }

            it("returns the contents of a foreign page") {
                val page = createDownloader().downloadPage(PageLocation("it", "Fallout_Wiki"))

                assertThat(page.contents).isNotEmpty()
            }
        }
    }
})
