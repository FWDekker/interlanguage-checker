package com.fwdekker.interwikichecker

import mu.KLogging
import net.sourceforge.jwbf.core.actions.HttpActionClient
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

/**
 * Creates [MediaWikiBot]s to connect to interwikis with.
 *
 * @property urlBuilder given an interwiki abbreviation, returns the url such that appending "api.php" gives the URL of
 * that interwiki's endpoint
 */
class MediaWikiBotFactory(private val urlBuilder: (String) -> String) {
    /**
     * Creates a [MediaWikiBot] that connects to the wiki with the given language.
     *
     * @param language the language of the wiki to create a bot for
     * @return a [MediaWikiBot] that connects to the wiki with the given language
     */
    fun createMediaWikiBot(language: String): MediaWikiBot =
        MediaWikiBot(HttpActionClient.builder()
            .withUrl(urlBuilder(language))
            .withUserAgent("InterwikiChecker", "0.0.1")
            .build())
}

/**
 * Downloads pages in a given language.
 *
 * @property mediaWikiBotFactory a factory for [MediaWikiBot]s to connect to interwikis with
 */
class PageDownloader(private val mediaWikiBotFactory: MediaWikiBotFactory) {
    companion object : KLogging()

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
            .also { logger.info { "Downloading `$location`." } }

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
    private fun createBot(language: String) = mediaWikiBotFactory.createMediaWikiBot(language)
}
