package com.fwdekker.interwikichecker

import net.sourceforge.jwbf.core.actions.HttpActionClient
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

/**
 * Downloads pages in a given language.
 *
 * @property urlBuilder a function that turns a language's abbreviation into the URL of the wiki in that language
 */
class PageDownloader(private val urlBuilder: (String) -> String) {
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
        downloaders.getOrPut(language) { createBot(urlBuilder(language)) }

    /**
     * Creates a new [MediaWikiBot] that can download pages from the wiki at the specified URL.
     *
     * @param url the URL such that appending "api.php" gives the wiki's API endpoint
     * @return a new [MediaWikiBot] that can download pages from the wiki at the specified URL
     */
    private fun createBot(url: String) =
        MediaWikiBot(HttpActionClient.builder()
            .withUrl(url)
            .withUserAgent("InterwikiChecker", "0.0.1")
            .build())
}
