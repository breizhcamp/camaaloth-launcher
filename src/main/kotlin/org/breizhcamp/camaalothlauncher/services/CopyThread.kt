package org.breizhcamp.camaalothlauncher.services

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.CopyCmd
import org.breizhcamp.camaalothlauncher.dto.CopyFile
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

    /**
     * Retrieve the list of all file in waiting queue
     */
    fun listWaiting() : List<CopyFile> = queue.map { CopyFile(it.dirFileName, it.fileSize) }

    private fun copyFile(copyCmd: CopyCmd) {
        val src = copyCmd.src
        if (src.fileName.toString() == "/nonexistant" || !Files.exists(src)) return

        val srcdir = src.parent.parent.toAbsolutePath().toString() //retrieving root directory of all videos
        val srcfile = copyCmd.dirFileName //extracting dirname and video file
        val dest = copyCmd.destDir.toString()

        val cmd = mutableListOf("/bin/bash", copyScript, srcdir, srcfile, dest)
        copyCmd.destServer?.let { cmd.add(it) }

        val logFile = copyCmd.logDir.resolve(logDateFormater.format(LocalDateTime.now()) + "_copy.log")
        val runDir = src.parent

        //we "run()" the class because we're already in a dedicated thread and want to copy file by file
        LongCmdRunner("copy", cmd, runDir, logFile, msgTpl, "/050-copy-out").run()
    }
}