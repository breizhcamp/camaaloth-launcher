package org.breizhcamp.camaalothlauncher.services

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.CopyCmd
import org.breizhcamp.camaalothlauncher.dto.CopyFile
import org.breizhcamp.camaalothlauncher.dto.CopyProgress
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

    /** Current progress */
    val progress = CopyProgress()

    private val rsyncRegex = Regex("^\\s*([0-9,]+)\\s+[0-9]+%\\s+([0-9.]+)(.?B)/s.*$")

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

        progress.current = CopyFile(srcfile, copyCmd.fileSize)
        progress.copied = 0L
        sendProgress()

        //we "run()" the class because we're already in a dedicated thread and want to copy file by file
        LongCmdRunner("copy", cmd, runDir, logFile, msgTpl, "/050-copy-out", this::parseRsyncAndUpdateProgress).run()

        if (queue.isEmpty()) {
            progress.reset()
        }

        sendProgress()
    }

    /**
     * Parse an update rsync line, update all progress object and send update in stomp queue.
     * example of rsync line : 196,178 100%   77.92MB/s    0:00:00 (xfr#1, to-chk=0/2)
     */
    private fun parseRsyncAndUpdateProgress(line: String) {
        val res = rsyncRegex.find(line) ?: return
        val (bytes, speed, speedUnit) = res.destructured
        progress.copied = bytes.replace(",", "").toLong()

        val speedMultiplier = when(speedUnit) {
            "B" -> 1
            "kB" -> 1024
            "MB" -> 1024 * 1024
            "GB" -> 1024 * 1024 * 1024
            else -> throw IllegalStateException("rsync speed unit [$speedUnit] is unknown")
        }

        progress.speed = speed.toBigDecimal().multiply(speedMultiplier.toBigDecimal()).toLong()
        sendProgress()
    }

    private fun sendProgress() {
        progress.waitingSize = queue.map { it.fileSize }.sum()
        msgTpl.convertAndSend("/050-copy-progress", progress)
    }
}