package com.fwdekker.interwikichecker

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.lang.Math.random

/**
 * Unit tests for [InterwikiNetwork].
 */
internal object InterwikiNetworkTest : Spek({
    fun randomLocation(language: String) = PageLocation(language, random().toString())

    // Test data
    val locations = listOf(
        randomLocation("lu"),
        randomLocation("en"),
        randomLocation("lu"),
        randomLocation("se"),
        randomLocation("ch"),
        randomLocation("se")
    )
    val links = mapOf(
        Pair(locations[0], listOf(InterwikiLink(locations[0], locations[1]))),
        Pair(locations[1], listOf(InterwikiLink(locations[1], locations[4]))),
        Pair(locations[2], listOf(InterwikiLink(locations[2], locations[3]))),
        Pair(locations[3], listOf(InterwikiLink(locations[3], locations[5]))),
        Pair(locations[4], listOf(InterwikiLink(locations[4], locations[2]))),
        Pair(locations[5], listOf(InterwikiLink(locations[5], locations[4])))
    )
    val network = InterwikiNetwork(links)


    describe("interwiki networks") {
        it("returns a list of all languages") {
            assertThat(network.languages)
                .containsExactlyInAnyOrder("lu", "en", "se", "ch")
        }

        it("returns a list of all pages") {
            assertThat(network.pages)
                .containsExactlyInAnyOrderElementsOf(locations)
        }

        it("returns a list of pages in a particular language") {
            assertThat(network.getPagesInLanguage("lu"))
                .containsExactlyInAnyOrder(locations[0], locations[2])
        }

        it("returns a list of all links originating from a given language") {
            assertThat(network.getLinksFrom("se"))
                .containsExactlyInAnyOrder(
                    InterwikiLink(locations[3], locations[5]),
                    InterwikiLink(locations[5], locations[4])
                )
        }

        it("returns a list of all links originating from a given page") {
            assertThat(network.getLinksFrom(locations[2]))
                .containsExactlyInAnyOrder(InterwikiLink(locations[2], locations[3]))
        }

        it("returns a list of all links going to a given language") {
            assertThat(network.getLinksTo("ch"))
                .containsExactlyInAnyOrder(
                    InterwikiLink(locations[1], locations[4]),
                    InterwikiLink(locations[5], locations[4])
                )
        }

        it("returns a list of all links going to a given page") {
            assertThat(network.getLinksTo(locations[1]))
                .containsExactlyInAnyOrder(InterwikiLink(locations[0], locations[1]))
        }
    }
})
