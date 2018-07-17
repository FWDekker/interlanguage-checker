package com.fwdekker.interwikichecker

import javafx.application.Application
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.controlsfx.control.spreadsheet.GridBase
import org.controlsfx.control.spreadsheet.SpreadsheetCellBase
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

        mainPane.center = pageSheet

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

        val dinges = InterwikiNetworkTable(network)
        pageSheet.grid = GridBase(network.pages.size, 4)
        pageSheet.grid.setRows(
            FXCollections.observableList(
                network.pages.sortedBy { it.toString() }.mapIndexed { index, page ->
                    FXCollections.observableList(listOf(
                        languageCells[page.language],
                        SpreadsheetCellBase(index, 1, 1, 1)
                            .apply { graphic = dinges.pageButtons[page] },
                        SpreadsheetCellType.STRING.createCell(index, 2, 1, 1, page.pageName),
                        SpreadsheetCellType.STRING.createCell(index, 2, 1, 1, "")
                            .apply {
                                dinges.selectedPages.addListener { _: Observable ->
                                    item = dinges.selectedPages
                                        .subtract(network.getLinksFrom(page).map { it.target })
                                        .subtract(listOf(page))
                                        .joinToString(", ")
                                }
                            }
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

class InterwikiNetworkTable(private val network: InterwikiNetwork) {
    val selectedPages = FXCollections.observableList(mutableListOf<PageLocation>())
    val languageGroups: Map<String, ToggleGroup>
    val pageButtons: Map<PageLocation, RadioButton>


    init {
        languageGroups =
            network.languages
                .map { language ->
                    Pair(language, ToggleGroup())
                }
                .toMap()

        pageButtons =
            network.pages
                .map { page ->
                    Pair(
                        page,
                        RadioButton()
                            .apply {
                                val group = languageGroups[page.language]!!

                                toggleGroup = group
                                selectedProperty().addListener { _ ->
                                    selectedPages.removeIf { it.language == page.language }
                                    selectedPages.add(page)
                                }

                                if (group.selectedToggle == null)
                                    isSelected = true
                            }
                    )
                }
                .toMap()
    }
}
