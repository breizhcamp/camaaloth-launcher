package org.breizhcamp.camaalothlauncher.services

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import mu.KotlinLogging
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * Adding systray when running
 */
@Service
class Systray(private val props: CamaalothProps): ApplicationListener<ServletWebServerInitializedEvent> {

    override fun onApplicationEvent(event: ServletWebServerInitializedEvent) {
        val url = "http://localhost:${event.source.port}"
        if (props.startBrowserOnStartup) startBrowser(url)

        if (!props.systray) return
        val systray = SystemTray.get() ?: return logger.warn("System is not handling systray")

        systray.setTooltip("Camaaloth Launcher")
        systray.setImage(createImage("/logo.png"))

        val mainMenu = systray.menu

        mainMenu.add(MenuItem("Ouvrir") { startBrowser(url) })
        mainMenu.add(MenuItem("Quitter") { exitProcess(0) })
    }

    /**
     * Create the systray icon based on a classpath image file
     * @param path Classpath relative path of the image
     * @return Image resource
     */
    private fun createImage(path: String): URL {
        return Systray::class.java.getResource(path) ?: throw RuntimeException("Unable to find image [$path] for systray.")
    }

    /**
     * Start the local browser
     * @param url URL to start with
     */
    private fun startBrowser(url: String) {
        val os = System.getProperty("os.name").lowercase()
        var commands: Array<String>? = null

        //start browser depending the operating system
        if (os.contains("win")) {
            commands = arrayOf("cmd", "/c", "start", url)

        } else if (os.contains("nux")) {
            commands = arrayOf("xdg-open", url)

        } else if (os.contains("mac")) {
            commands = arrayOf("open", url)
        }

        if (commands != null) {
            try {
                Runtime.getRuntime().exec(commands)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}