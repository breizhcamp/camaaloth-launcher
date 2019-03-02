package org.breizhcamp.camaalothlauncher.services

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.CopyCmd
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.LinkedBlockingDeque
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Thread copying files
 */
@Service
class CopyThread(props: CamaalothProps, private val msgTpl: SimpMessagingTemplate) : Thread("CopyThread") {
    private val logDateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    private val queue = LinkedBlockingDeque<CopyCmd>()
    private var running = true
    private val copyScript = Paths.get(props.copyScript).toAbsolutePath().toString()

    override fun run() {
        while(running) {
            val copyCmd = queue.take()
            copyFile(copyCmd)
        }
    }

    /** add a [copyCmd] into copy list */
    fun addFileToCopy(copyCmd: CopyCmd) {
        queue.addLast(copyCmd)
    }

    @PostConstruct
    fun startup() {
        this.start()
    }

    @PreDestroy
    fun shutdown() {
        running = false
        val nonexistant = Paths.get("/nonexistant")
        queue.addLast(CopyCmd(nonexistant, nonexistant, nonexistant))
    }

    private fun copyFile(copyCmd: CopyCmd) {
        val src = copyCmd.src
        if (src.fileName.toString() == "/nonexistant" || !Files.exists(src)) return

        val dest = copyCmd.dest.toString()

        val cmd = listOf("/bin/bash", copyScript, src.toAbsolutePath().toString(), dest)
        val logFile = copyCmd.logDir.resolve(logDateFormater.format(LocalDateTime.now()) + "_copy.log")
        val runDir = src.parent

        LongCmdRunner("copy", cmd, runDir, logFile, msgTpl, "/050-copy-out").start()
    }
}