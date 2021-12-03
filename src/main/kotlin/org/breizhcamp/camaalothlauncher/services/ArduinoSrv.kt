package org.breizhcamp.camaalothlauncher.services

import com.fazecast.jSerialComm.SerialPort
import mu.KotlinLogging
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.services.recorder.RecorderHook
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.BufferedWriter
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

private val logger = KotlinLogging.logger {}

/**
 * Communication with Arduino
 */
@Profile("breizhcamp")
@Service
class ArduinoSrv(private val props: CamaalothProps): RecorderHook {

    private lateinit var port: SerialPort
    private lateinit var input: BufferedReader
    private lateinit var output: BufferedWriter

    /** Do we have to try to connect to the Arduino */
    private val enabled = props.breizhcamp.arduinoPort != "no"

    /** True if connected and messages can be send */
    private var connected = false

    @PostConstruct
    fun init() {
        if (!enabled) {
            logger.info { "Not trying to connect to Arduino as disabled in configuration" }
            return
        }

        connectTo()
    }

    /** Connect to the Arduino */
    fun connectTo() {
        if (!enabled) return

        port = if (props.breizhcamp.arduinoPort == "auto") {
            val ports = SerialPort.getCommPorts()

            if (ports.isEmpty()) {
                connected = false
                return
            }

            ports.first()
        } else {
            SerialPort.getCommPort(props.breizhcamp.arduinoPort)
        }

        logger.info { "Connecting to Arduino on port [${port.systemPortName}]" }

        port.setComPortParameters(115200, 8, 1, 0)
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 500, 0)
        port.clearDTR()
        port.clearRTS()

        if (!port.openPort()) {
            logger.error { "Unable to connect to Arduino on port [${port.systemPortName}]" }
            return
        }

        logger.info { "Connected to Arduino on port [${port.systemPortName}]" }
        connected = true

        input = port.inputStream.bufferedReader()
        output = port.outputStream.bufferedWriter()
    }

    @PreDestroy
    fun close() {
        connected = false
        port.closePort()
    }

    override fun preRecord(preview: Boolean) {
        if (preview) return
        switchToSpeaker()
    }

    override fun postRecord(preview: Boolean) {
        if (preview) return
        switchToCamaaloth()
    }

    private fun switchToCamaaloth() = sendKey(1)
    private fun switchToSpeaker() = sendKey(2)

    private fun sendKey(key: Int) {
        if (!connected) connectTo()

        val msg = buildMsg(key)

        if (!connected) {
            logger.warn { "Trying to send [$msg] to Arduino but it's not connected" }
            return
        }

        try {
            output.write("$msg\n")
            output.flush()
        } catch (e: Exception) {
            logger.error("Unable to send [$msg] to Arduino, port is probably disconnected", e)
            close()
            return
        }

        try {
            val line = input.readLine()

            if (line != "$msg sent") {
                logger.error { "Arduino: $msg sent but return is not expected : [$line]" }
            }
        } catch (e: Exception) {
            logger.error("Unable to read [$msg] from Arduino, port is probably disconnected or Arduino too slow to respond", e)
            close()
        }
    }

    private fun buildMsg(key: Int): String {
        if (props.breizhcamp.nbPortsSwitcher == "4") {
            //for 4x switcher, IR code in Arduino program is shifted from the 2x code that's first one
            return (key + 2).toString()
        }

        return "$key"
    }
}