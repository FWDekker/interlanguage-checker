package com.fwdekker.interwikichecker

import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import java.net.URL

class TableApplication : Application() {
    private val collector: InterwikiCollector

    private val pageSelection = TextField()
    private val pageSelectionSubmit = Button("Load")
    private val table = TableView<ObservableList<PageLocation>>()


    init {
        val downloader = PageDownloader(MediaWikiBotFactory(), createFalloutInterwikiMap())
        collector = InterwikiCollector(downloader)
    }


    override fun start(stage: Stage) {
        pageSelectionSubmit.setOnAction(::onPageSelectionSubmit)

        val pane = BorderPane()
        pane.top = pageSelection
        pane.left = pageSelectionSubmit
        pane.center = table

        stage.scene = Scene(pane, 300.0, 500.0)
        stage.show()
    }

    private fun onPageSelectionSubmit(event: ActionEvent) {
        val network = collector.buildInterwikiNetwork(PageLocation("en", pageSelection.text))

        table.items.setAll(network.toObservableTable())
        table.columns.setAll(
            network.pages
                .sortedBy { it.toString() }
                .mapIndexed { i, location ->
                    TableColumn<ObservableList<PageLocation>, PageLocation>(location.toString())
                        .apply {
                            setCellValueFactory { data -> SimpleObjectProperty<PageLocation>(data.value[i]) }
                        }
                }
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

private fun InterwikiNetwork.toObservableTable() =
    FXCollections.observableList(
        pages.sortedBy(PageLocation::toString).map<PageLocation, ObservableList<PageLocation>?> { target ->
            FXCollections.observableList(
                pages.sortedBy(PageLocation::toString).map { origin ->
                    when {
                        InterwikiLink(origin, target) in getLinksFrom(origin) -> target
                        else -> null
                    }
                })
        })
