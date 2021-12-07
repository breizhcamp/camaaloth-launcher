package org.breizhcamp.camaalothlauncher.services

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.breizhcamp.camaalothlauncher.dto.FFMpegProgress
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.io.InputStream
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * Convert output files from nageru to MP4
 */
@Service
class ConvertSrv(private val msgTpl: SimpMessagingTemplate, private val filesSrv: FilesSrv)
    : ApplicationListener<ServletWebServerInitializedEvent> {

    private val logDateFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    private var httpPort = 0

    override fun onApplicationEvent(event: ServletWebServerInitializedEvent) {
        httpPort = event.source.port
    }

    /**
     * Start conversion for selected files
     */
    fun startConvert(destPath: Path, srcPath: Path, filesToConvert: List<Path>) {
        if (filesToConvert.isEmpty()) return
        if (Files.notExists(destPath)) Files.createDirectories(destPath)

        val destFile = destPath.resolve("export.mp4")
        Files.deleteIfExists(destFile)

        val inputArgs = if (filesToConvert.size == 1) {
            listOf("-i", "file:${filesToConvert[0]}")
        } else {
            srcPath.resolve("concat.txt").toFile()
                    .writeText(filesToConvert.map { "file 'file:${it.fileName}'" }.joinToString("\n"))
            listOf("-f", "concat", "-safe", "0", "-i", "concat.txt")
        }

        val cmd = mutableListOf("ffmpeg", "-n", "-progress", "http://localhost:$httpPort/export/progress")
        cmd.addAll(inputArgs)
        cmd.addAll(listOf("-c:v", "copy", "-c:a", "aac", "-b:a", "384k", "-profile:a", "aac_low", destFile.toString()))

        val logFile = srcPath.resolve(logDateFormater.format(LocalDateTime.now()) + "_ffmpeg.log")
        LongCmdRunner("ffmpeg", cmd, destPath, logFile, msgTpl, "/030-ffmpeg-export-out").start()
    }

    /**
     * Process input from ffmpeg, sending "messages" each ended with progress=xxx, each line with key=value
     */
    fun progress(input: InputStream) {
        input.bufferedReader().use { r ->
            val curMsg = HashMap<String, String>()

            try {
                for (line in r.lines()) {
                    val (key, value) = line.split('=')
                    curMsg.put(key, value)

                    if (line.startsWith("progress=") && curMsg.size > 0) {
                        val progress = FFMpegProgress.build(curMsg)
                        msgTpl.convertAndSend("/030-ffmpeg-export-progress", progress)
                        curMsg.clear()
                    }
                }
            } catch (e: UncheckedIOException) {
                //ffmpeg cut the connection violently, we discard the exception
                logger.info { "FFMpeg has terminated the progress \"a l'arrache\"" }
            }
        }
    }
}