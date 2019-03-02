package org.breizhcamp.camaalothlauncher.services

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for handling nageru (start/stop...)
 */
@Service
class NageruSrv(private val props: CamaalothProps, private val msgTpl: SimpMessagingTemplate) {
    private val logDateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun start(recordingDir: Path, stompDest: String) {
        val startScript = Paths.get(props.nageru.startScript).toAbsolutePath().toString()

        val cmd = listOf("/bin/bash", startScript, "-r", recordingDir.toAbsolutePath().toString())
        val logFile = recordingDir.resolve(logDateFormater.format(LocalDateTime.now()) + "_nageru.log")
        val runDir = Paths.get(props.nageru.themeDir)

        LongCmdRunner("nageru", cmd, runDir, logFile, msgTpl, stompDest).start()
    }
}