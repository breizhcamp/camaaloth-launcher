package org.breizhcamp.camaalothlauncher.services.recorder

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.services.LongCmdRunner
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
                private val hooks: List<RecorderHook> = emptyList()) {

    private val logDateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun start(recordingDir: Path, stompDest: String, preview: Boolean = false) {
        val startScript = Paths.get(props.nageru.startScript).toAbsolutePath().toString()

        val cmd = listOf("/bin/bash", startScript, "-r", recordingDir.toAbsolutePath().toString())
        val logFile = recordingDir.resolve(logDateFormater.format(LocalDateTime.now()) + "_nageru.log")
        val runDir = Paths.get(props.nageru.themeDir)

        hooks.forEach { it.preRecord(preview) }

        LongCmdRunner("nageru", cmd, runDir, logFile, msgTpl, stompDest)
                .endCallback { hooks.forEach { it.postRecord(preview) } }
                .start()

        hooks.forEach { it.postStart() }
    }
}