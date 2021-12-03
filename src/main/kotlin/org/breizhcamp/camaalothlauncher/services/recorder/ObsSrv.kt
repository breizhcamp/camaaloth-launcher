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
 * Service for handling OBS (start/stop...)
 */
@Service
class ObsSrv(private val props: CamaalothProps, private val msgTpl: SimpMessagingTemplate,
             private val hooks: List<RecorderHook> = emptyList()) {

    private val logDateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun start(recordingDir: Path, stompDest: String, preview: Boolean = false) {
        val cmd = props.obs.startCmd
        val logFile = recordingDir.resolve(logDateFormater.format(LocalDateTime.now()) + "_obs.log")

        hooks.forEach { it.preRecord(preview) }

        LongCmdRunner("obs", cmd, recordingDir, logFile, msgTpl, stompDest)
                .endCallback { hooks.forEach { it.postRecord(preview) } }
                .start()
    }
}