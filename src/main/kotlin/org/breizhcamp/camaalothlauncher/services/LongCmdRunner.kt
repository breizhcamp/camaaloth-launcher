package org.breizhcamp.camaalothlauncher.services

import mu.KotlinLogging
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.io.InputStream
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Command runner, can write output to file or stomp topic
 */
class LongCmdRunner(private val appName: String, private val cmd: List<String>, private val runDir: Path,
                    private val logFile: Path? = null, private val msgTpl: SimpMessagingTemplate? = null,
                    private val stompDest: String? = null) : Thread("${appName}Runner") {

    private var stdCallback: ((String) -> Unit)? = null
    private var endCallback: (() -> Unit)? = null
    private var currentProcess: Process? = null

    fun stdCallback(callback: (String) -> Unit): LongCmdRunner {
        stdCallback = callback
        return this
    }

    fun endCallback(callback: () -> Unit): LongCmdRunner {
        endCallback = callback
        return this
    }

    fun exec(): LongCmdRunner {
        this.start()
        return this
    }

    override fun run() {
        logger.info { "Starting $appName with command : [$cmd]" }
        val process = ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .directory(runDir.toFile())
                .start()

        currentProcess = process
        ReadStream(process.inputStream, "${appName}StdoutReader").start()
        val waitFor = process.waitFor()
        currentProcess = null

        val exitLog = "$appName stopped and returned [$waitFor]"
        logger.info { exitLog }
        sendMsg(msgTpl, stompDest, exitLog)

        endCallback?.invoke()
    }

    fun end() {
        currentProcess?.let { ShortCmdRunner("exitObs", listOf("kill", "-SIGINT", it.pid().toString())).run() }
    }

    /** Read input stream and copy into Outputs */
    private inner class ReadStream(private val inputStream: InputStream, name: String) : Thread(name) {

        override fun run() {
            sendMsg(msgTpl, stompDest, "---- NEW STREAM ----")
            readInput()
            sendMsg(msgTpl, stompDest, "---- END STREAM ----")
        }

        private fun readInput() {
            val log = logFile?.toFile()?.bufferedWriter()

            inputStream.bufferedReader().forEachLine { line ->
                logger.debug { "$appName stdout : $line" }
                sendMsg(msgTpl, stompDest, line)
                log?.appendLine(line)
                log?.flush()
                stdCallback?.invoke(line)
            }
        }
    }

    private fun sendMsg(msgTpl: SimpMessagingTemplate?, stompDest: String?, msg: Any) {
        val tpl = msgTpl ?: return
        val dest = stompDest ?: return
        tpl.convertAndSend(dest, msg)
    }

}