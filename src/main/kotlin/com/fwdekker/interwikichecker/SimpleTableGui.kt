package com.fwdekker.interwikichecker

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.Stage
import java.net.URL

class TableApplication : Application() {
    private val collector: InterwikiCollector

    private val pageSelection = TextField()
    private val pageSelectionSubmit = Button("Load")
    private val pageList = ListView<PageLocation>()


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

                centerPane.content = pageList
            }

        stage.scene = Scene(mainPane, 1000.0, 500.0)
        stage.show()
    }

    private fun onPageSelectionSubmit(event: ActionEvent) {
        val network = collector.buildInterwikiNetwork(PageLocation("en", pageSelection.text))
        pageList.items = FXCollections.observableList(network.pages)
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
