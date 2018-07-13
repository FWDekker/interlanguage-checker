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

    fun interwikiMap(vararg languages: String) = languages.map { Pair(it, "") }.toMap()


    it("returns the contents of the article it wraps around") {
        assertThat(Page(location, mockArticle("Nabla Randem Scones")).contents)
            .isEqualTo("Nabla Randem Scones")
    }

    describe("extracting interwiki links") {
        context("bad-weather cases") {
            it("does not find any interwiki links in an empty document") {
                assertThat(createPage("").getInterwikiLinks(interwikiMap("Gr"))).isEmpty()
            }

            it("does not find any interwiki links in a document without any links") {
                assertThat(createPage("Ride Picking Avolate").getInterwikiLinks(interwikiMap("cr"))).isEmpty()
            }

            it("does not think a regular link is an interwiki link") {
                assertThat(createPage("[[Comptrol]]").getInterwikiLinks(interwikiMap("dH"))).isEmpty()
            }

            it("does not think a two-letter link is an interwiki link") {
                assertThat(createPage("[[it]]").getInterwikiLinks(interwikiMap("it"))).isEmpty()
            }

            it("does not think that a namespace identifies an interwiki link") {
                assertThat(createPage("[[Beer:Hoss]]").getInterwikiLinks(interwikiMap("Curried"))).isEmpty()
            }

            it("does not think that a nested two-letter namespace identifies an interwiki link") {
                assertThat(createPage("[[Relend:ty:Gemot]]").getInterwikiLinks(interwikiMap("ty"))).isEmpty()
            }

            it("does not think that an interwiki link without square braces is an interwiki link") {
                assertThat(createPage("en:Untripe").getInterwikiLinks(interwikiMap("en"))).isEmpty()
            }
        }

        context("good-weather cases") {
            it("finds a regular interwiki link") {
                assertThat(createPage("[[kG:Sandyish]]").getInterwikiLinks(interwikiMap("kG")))
                    .containsExactly(InterwikiLink(location, PageLocation("kG", "Sandyish")))
            }

            it("finds a regular interwiki link that contains a namespace") {
                assertThat(createPage("[[fr:Yachters:Martite]]").getInterwikiLinks(interwikiMap("fr")))
                    .containsExactly(InterwikiLink(location, PageLocation("fr", "Yachters:Martite")))
            }

            it("finds a regular interwiki link that contains a two-letter namespace") {
                assertThat(createPage("[[yu:di:Nomina]]").getInterwikiLinks(interwikiMap("yu")))
                    .containsExactly(InterwikiLink(location, PageLocation("yu", "di:Nomina")))
            }

            it("finds an interwiki link even if the page name is empty") {
                assertThat(createPage("[[aa:]]").getInterwikiLinks(interwikiMap("aa")))
                    .containsExactly(InterwikiLink(location, PageLocation("aa", "")))
            }

            it("finds short interwiki identifiers") {
                assertThat(createPage("[[u:Unnigh]]").getInterwikiLinks(interwikiMap("u")))
                    .containsExactly(InterwikiLink(location, PageLocation("u", "Unnigh")))
            }

            it("finds long interwiki identifiers") {
                assertThat(createPage("[[hhknilzbtu:Venada]]").getInterwikiLinks(interwikiMap("hhknilzbtu")))
                    .containsExactly(InterwikiLink(location, PageLocation("hhknilzbtu", "Venada")))
            }

            it("finds multiple interwiki links even if not separated by newlines") {
                assertThat(createPage("[[lq:Cimicid]] [[ja:Triode]] [[yi:Taglet]]")
                    .getInterwikiLinks(interwikiMap("ja", "lq", "yi")))
                    .containsExactlyInAnyOrder(
                        InterwikiLink(location, PageLocation("lq", "Cimicid")),
                        InterwikiLink(location, PageLocation("ja", "Triode")),
                        InterwikiLink(location, PageLocation("yi", "Taglet"))
                    )
            }
        }
    }
})
