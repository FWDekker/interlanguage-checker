package com.fwdekker.interwikichecker

import net.sourceforge.jwbf.core.actions.HttpActionClient
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

/**
 * Creates [HttpActionClient]s to connect to interwikis with.
 *
 * @property urlBuilder given an interwiki abbreviation, returns the url such that appending "api.php" gives the URL of
 * that interwiki's endpoint
 */
class HttpActionClientFactory(private val urlBuilder: (String) -> String) {
    /**
     * Creates an [HttpActionClient] that connects to the wiki with the given language.
     *
     * @param language the language of the wiki to create a bot for
     * @return an [HttpActionClient] that connects to the wiki with the given language
     */
    fun createHttpActionClient(language: String): HttpActionClient =
        HttpActionClient.builder()
            .withUrl(urlBuilder(language))
            .withUserAgent("InterwikiChecker", "0.0.1")
            .build()
}

/**
 * Downloads pages in a given language.
 *
 * @property httpActionClientFactory a factory for [HttpActionClient]s to connect to interwikis with
 */
class PageDownloader(private val httpActionClientFactory: HttpActionClientFactory) {
    /**
     * A mapping from a language to the downloader to use for downloading pages for that language.
     */
    private val downloaders = HashMap<String, MediaWikiBot>()

    /**
     * Downloads the contents of the indicated page.
     *
     * @param location a description of the page to download
     * @return the contents of the indicated page
     */
    fun downloadPage(location: PageLocation) =
        Page(location, getDownloader(location.language).getArticle(location.pageName))

    /**
     * Returns the previously created downloader for the wiki with language [language]; if no such downloader exists a
     * new one is created.
     *
     * @param language the language of the wiki to download from
     * @return the previously created downloader for the wiki with language [language]; if no such downloader exists a
     * new one is created
     */
    private fun getDownloader(language: String) =
        downloaders.getOrPut(language) { createBot(language) }

    /**
     * Creates a new [MediaWikiBot] that can download pages from the wiki at the specified URL.
     *
     * @param language the language of the wiki to create a bot for
     * @return a new [MediaWikiBot] that can download pages from the wiki at the specified URL
     */
    private fun createBot(language: String) =
        MediaWikiBot(httpActionClientFactory.createHttpActionClient(language))
}
