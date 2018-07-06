package com.fwdekker.interwikichecker

import mu.KLogging
import net.sourceforge.jwbf.core.actions.HttpActionClient
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import java.net.URL

/**
 * Creates [MediaWikiBot]s to connect to interwikis with.
 */
class MediaWikiBotFactory {
    /**
     * Creates a [MediaWikiBot] that connects to the wiki at the given URL.
     *
     * @param url the URL of the wiki to create a bot for
     * @return a [MediaWikiBot] that connects to the wiki at the given URL
     */
    fun createMediaWikiBot(url: URL): MediaWikiBot =
        MediaWikiBot(HttpActionClient.builder()
            .withUrl(url)
            .withUserAgent("InterwikiChecker", "0.0.1")
            .build())
}

/**
 * Downloads pages in a given language.
 *
 * @property mediaWikiBotFactory a factory for [MediaWikiBot]s to connect to interwikis with
 * @property interwikiMap a map from interwiki identifiers to wiki URLs
 */
class PageDownloader(
    private val mediaWikiBotFactory: MediaWikiBotFactory,
    val interwikiMap: Map<String, String>
) {
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
    private fun createBot(language: String) = mediaWikiBotFactory.createMediaWikiBot(URL(interwikiMap[language]))
}
