package com.fwdekker.interwikichecker

import mu.KLogging
import java.util.LinkedList

/**
 * Collects interwiki-related information on pages.
 *
 * @property downloader the [PageDownloader] used to download pages with
 */
class InterwikiCollector(private val downloader: PageDownloader) {
    companion object : KLogging()

    /**
     * Builds a network of interwiki links between pages, starting from [location].
     *
     * This method works by recursively following interwiki links until no more pages are linked to. Data on the
     * relationships between pages is returned in the form of an [InterwikiNetwork].
     *
     * @param location the location from which to start following interwiki links
     * @return a network of interwiki links between pages, starting from [location]
     */
    fun buildInterwikiNetwork(location: PageLocation): InterwikiNetwork {
        logger.info { "Building interwiki network for `$location`." }

        val links = HashMap<PageLocation, List<InterwikiLink>>()

        val queue = LinkedList<PageLocation>()
        queue.add(location)

        while (queue.isNotEmpty()) {
            val nextPage = queue.pop() ?: continue
            if (nextPage in links) continue

            downloader.downloadPage(nextPage).getInterwikiLinks()
                .also {
                    links[nextPage] = it
                    queue.addAll(it.map { it.target })
                }
        }

        return InterwikiNetwork(links)
    }
}

/**
 * A network of interwiki links between pages.
 *
 * @property links a mapping from a page to the interwiki links found on that page
 */
data class InterwikiNetwork(val links: Map<PageLocation, List<InterwikiLink>>) {
    /**
     * A set of all languages contained in this [InterwikiNetwork].
     */
    val languages get() = links.keys.map { it.language }.toSet()

    /**
     * A list of all pages contained in this [InterwikiNetwork].
     */
    val pages get() = links.keys.toList()

    /**
     * Returns the [PageLocation]s of all pages with language [language].
     *
     * @param language the language for which to return the [PageLocation]s
     * @return the [PageLocation]s of all pages with language [language]
     */
    fun getPagesInLanguage(language: String) = links.keys.filter { it.language == language }

    /**
     * Returns all [InterwikiLink]s originating from [language].
     *
     * If there are multiple pages in the same language in this [InterwikiNetwork], the links are combined into a
     * single list.
     *
     * @param language the language from which the [InterwikiLink]s originate
     * @return all [InterwikiLink]s originating from [language]
     */
    fun getLinksFrom(language: String) =
        links
            .filter { it.key.language == language }
            .flatMap { it.value }

    /**
     * Returns all [InterwikiLink]s originating from [page].
     *
     * @param page the page from which the [InterwikiLink]s originate
     * @return all [InterwikiLink]s originating from [page]
     */
    fun getLinksFrom(page: PageLocation) = links[page] ?: emptyList()

    /**
     * Returns all [InterwikiLink]s that link to [targetLanguage].
     *
     * @param targetLanguage the language to which the [InterwikiLink]s link
     * @return all [InterwikiLink]s that link to [targetLanguage]
     */
    fun getLinksTo(targetLanguage: String) =
        links
            .flatMap { it.value }
            .filter { it.target.language == targetLanguage }

    /**
     * Returns all [InterwikiLink]s that link to [targetPage].
     *
     * @param targetPage the page to which the [InterwikiLink]s link
     * @return all [InterwikiLink]s that link to [targetPage]
     */
    fun getLinksTo(targetPage: PageLocation) =
        links
            .flatMap { it.value }
            .filter { it.target == targetPage }
}
