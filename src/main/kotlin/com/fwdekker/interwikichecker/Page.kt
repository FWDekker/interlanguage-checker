package com.fwdekker.interwikichecker

import net.sourceforge.jwbf.core.contentRep.Article

/**
 * Represents a page on a wiki.
 *
 * @property location the location of this page
 * @property article the [Article]
 */
class Page(private val location: PageLocation, private val article: Article) {
    /**
     * The contents of the page.
     */
    val contents: String = article.text

    /**
     * Returns a map of all interwiki links on this page as a map from the language to the page.
     *
     * @return a map of all interwiki links on this page as a map from the language to the page
     */
    fun getInterwikiLinks() =
        extractInterwikiLinks(contents)
            .map { InterwikiLink(location, PageLocation(it.removeSurrounding("[[", "]]"))) }
            .toList()

    /**
     * Returns all interwiki links that occur in [contents].
     *
     * An interwiki link is identified as a link of which the namespace contains exactly two characters. For
     * example, `[[en:Page]]` and `[[uk:Help:Page]]` are interwiki links, but `[[Page]]` and `[[Category:Cat]]` are
     * not.
     *
     * @param contents the contents to find interwiki links in
     * @return all interwiki links that occur in [contents]
     */
    private fun extractInterwikiLinks(contents: String) =
        Regex("\\[\\[.{2}:.*]]").findAll(contents).map { it.value }
}

/**
 * The location of a page as its language and name.
 *
 * @param language the language of the page, such as "en" or "de"
 * @param pageName the full name of the page, such as "Page" or "User:Foo"
 */
data class PageLocation(val language: String, val pageName: String) {
    /**
     * Creates a description of the location of a page from the link to that page including the language specifier.
     *
     * @param fullLink the location to a page including the language specifier, such as "en:Page" or "de:User:Foo"
     */
    constructor(fullLink: String) : this(fullLink.substringBefore(":"), fullLink.substringAfter(":"))

    override fun toString(): String {
        return "$language:$pageName"
    }
}

/**
 * A link from one page to another.
 *
 * @param origin the page on which the link was found
 * @param target the page to which the link links
 */
data class InterwikiLink(val origin: PageLocation, val target: PageLocation)
