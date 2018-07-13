package com.fwdekker.interwikichecker

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.controlsfx.control.spreadsheet.GridBase
import org.controlsfx.control.spreadsheet.SpreadsheetCellType
import org.controlsfx.control.spreadsheet.SpreadsheetView
import java.net.URL

class TableApplication : Application() {
    private val collector: InterwikiCollector

    private val pageSelection = TextField()
    private val pageSelectionSubmit = Button("Load")
    private val pageSheet = SpreadsheetView(GridBase(0, 0))
        .apply {
            isEditable = false
            showRowHeaderProperty().set(false)
        }


    init {
        val downloader = PageDownloader(MediaWikiBotFactory(), createFalloutInterwikiMap())
        collector = InterwikiCollector(downloader)
    }


    override fun start(stage: Stage) {
        val mainPane = BorderPane()

        HBox()
            .also { settingsBox ->
                mainPane.top = settingsBox

                settingsBox.children.add(pageSelection)
                settingsBox.children.add(pageSelectionSubmit)

                pageSelectionSubmit.setOnAction(::onPageSelectionSubmit)
            }

        ScrollPane()
            .also { centerPane ->
                mainPane.center = centerPane

                centerPane.content = pageSheet
            }

        stage.scene = Scene(mainPane, 1000.0, 500.0)
        stage.show()
    }

    private fun onPageSelectionSubmit(event: ActionEvent) {
        val network = collector.buildInterwikiNetwork(PageLocation("en", pageSelection.text))

        var pagesInLanguageSum = 0
        val languageCells = network.languages.map { language ->
            val pagesInLanguage = network.getPagesInLanguage(language).size
            val pair = Pair(language, SpreadsheetCellType.STRING.createCell(pagesInLanguageSum, 0, 1, 1, language))
            pagesInLanguageSum += pagesInLanguage
            pair
        }.toMap()

        pageSheet.grid = GridBase(network.pages.size, 3)
        pageSheet.grid.setRows(
            FXCollections.observableList(
                network.pages.sortedBy { it.toString() }.mapIndexed { index, page ->
                    FXCollections.observableList(listOf(
                        languageCells[page.language],
                        SpreadsheetCellType.STRING.createCell(index, 1, 1, 1, "button"),
                        SpreadsheetCellType.STRING.createCell(index, 2, 1, 1, page.pageName)
                    ))
                }
            )
        )
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(TableApplication::class.java)
        }

        fun createFalloutInterwikiMap() =
            MediaWikiBotFactory().createMediaWikiBot(URL("https://fallout.wikia.com/"))
                .siteinfo.interwikis
                .filter { it.value.contains("fallout.wikia.com") }
                .map { Pair(it.key, it.value.replace("/wiki", "")) }
                .toMap()
    }
}
