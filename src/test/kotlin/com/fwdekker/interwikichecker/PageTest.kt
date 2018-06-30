package com.fwdekker.interwikichecker

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import net.sourceforge.jwbf.core.contentRep.Article
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Unit tests for [Page].
 */
internal object PageTest : Spek({
    val location = PageLocation("language", "page name")

    fun mockArticle(contents: String) = mock<Article> { on { it.text } doReturn contents }

    fun createPage(contents: String) = Page(location, mockArticle(contents))


    it("returns the contents of the article it wraps around") {
        assertThat(Page(location, mockArticle("Nabla Randem Scones")).contents)
            .isEqualTo("Nabla Randem Scones")
    }

    describe("extracting interwiki links") {
        context("bad-weather cases") {
            it("does not find any interwiki links in an empty document") {
                assertThat(createPage("").getInterwikiLinks()).isEmpty()
            }

            it("does not find any interwiki links in a document without any links") {
                assertThat(createPage("Ride Picking Avolate").getInterwikiLinks()).isEmpty()
            }

            it("does not think a regular link is an interwiki link") {
                assertThat(createPage("[[Comptrol]]").getInterwikiLinks()).isEmpty()
            }

            it("does not think a two-letter link is an interwiki link") {
                assertThat(createPage("[[it]]").getInterwikiLinks()).isEmpty()
            }

            it("does not think that a namespace identifies an interwiki link") {
                assertThat(createPage("[[Beer:Hoss]]").getInterwikiLinks()).isEmpty()
            }

            it("does not think that a nested two-letter namespace identifies an interwiki link") {
                assertThat(createPage("[[Relend:ty:Gemot]]").getInterwikiLinks()).isEmpty()
            }

            it("does not think that an interwiki link without square braces is an interwiki link") {
                assertThat(createPage("en:Untripe").getInterwikiLinks()).isEmpty()
            }
        }

        context("good-weather cases") {
            it("finds a regular interwiki link") {
                assertThat(createPage("[[kG:Sandyish]]").getInterwikiLinks())
                    .containsExactly(InterwikiLink(location, PageLocation("kG", "Sandyish")))
            }

            it("finds a regular interwiki link that contains a namespace") {
                assertThat(createPage("[[fr:Yachters:Martite]]").getInterwikiLinks())
                    .containsExactly(InterwikiLink(location, PageLocation("fr", "Yachters:Martite")))
            }

            it("finds a regular interwiki link that contains a two-letter namespace") {
                assertThat(createPage("[[yu:di:Nomina]]").getInterwikiLinks())
                    .containsExactly(InterwikiLink(location, PageLocation("yu", "di:Nomina")))
            }

            it("finds an interwiki link even if the page name is empty") {
                assertThat(createPage("[[aa:]]").getInterwikiLinks())
                    .containsExactly(InterwikiLink(location, PageLocation("aa", "")))
            }
        }
    }
})
