package org.breizhcamp.camaalothlauncher.services

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.annotation.PreDestroy

/**
 * Service for handling nageru (start/stop...)
 */
@Service
class NageruSrv(private val props: CamaalothProps, private val msgTpl: SimpMessagingTemplate,
                private val hooks: List<NageruHook> = emptyList()) {

    private val logDateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    /** Run post hooks at startup to init everything */
    @EventListener(ApplicationReadyEvent::class)
    fun runStartupHooks() {
        hooks.forEach { it.postNageru(false) }
    }

    /** Run pre hooks on shutdown to reset state */
    @PreDestroy
    fun runStopHooks() {
        hooks.forEach { it.preNageru(false) }
    }

    fun start(recordingDir: Path, stompDest: String, preview: Boolean = false) {
        val startScript = Paths.get(props.nageru.startScript).toAbsolutePath().toString()

        val cmd = listOf("/bin/bash", startScript, "-r", recordingDir.toAbsolutePath().toString())
        val logFile = recordingDir.resolve(logDateFormater.format(LocalDateTime.now()) + "_nageru.log")
        val runDir = Paths.get(props.nageru.themeDir)

        hooks.forEach { it.preNageru(preview) }

        LongCmdRunner("nageru", cmd, runDir, logFile, msgTpl, stompDest)
                .endCallback { hooks.forEach { it.postNageru(preview) } }
                .start()
    }
}