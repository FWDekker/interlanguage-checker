package com.fwdekker.interwikichecker

import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField

class SimpleTableGui(downloader: PageDownloader) {
    private val collector = InterwikiCollector(downloader)

    private val input = JTextField("Rolling pin (Fallout 3)")
    private val submitInput = JButton("Load")
    private var table = JTable()

    init {
        val frame = JFrame()
        val panel = JPanel()
        frame.add(panel)

        panel.add(input)
        panel.add(submitInput)
        frame.rootPane.defaultButton = submitInput

        submitInput.addActionListener {
            table.model = createTableFor(PageLocation("en", input.text)).model
        }

        table = JTable()
        val scrollPane = JScrollPane(table)

        panel.add(scrollPane)
        frame.pack()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }


    private fun createTableFor(page: PageLocation): JTable {
        val network = collector.buildInterwikiNetwork(page)
        return JTable(network.toTable(), network.toHeaders())
    }
}

fun InterwikiNetwork.toHeaders() = pages.sortedBy { it.toString() }.toTypedArray()

fun InterwikiNetwork.toTable() =
    pages.sortedBy(PageLocation::toString).map { target ->
        pages.sortedBy(PageLocation::toString).map { origin ->
            when {
                InterwikiLink(origin, target) in getLinksFrom(origin) -> target.toString()
                origin == target -> "X"
                else -> ""
            }
        }.toTypedArray()
    }.toTypedArray()


fun main(args: Array<String>) {
    SimpleTableGui(PageDownloader(HttpActionClientFactory { language ->
        if (language == "en") "https://fallout.wikia.com/"
        else "http://$language.fallout.wikia.com/"
    }))
}
