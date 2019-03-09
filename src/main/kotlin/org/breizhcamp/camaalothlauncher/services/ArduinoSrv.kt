package org.breizhcamp.camaalothlauncher.services

import com.fazecast.jSerialComm.SerialPort
import mu.KotlinLogging
import org.breizhcamp.camaalothlauncher.CamaalothProps
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
class ArduinoSrv(private val props: CamaalothProps): NageruHook {

    private lateinit var port: SerialPort
    private lateinit var input: BufferedReader
    private lateinit var output: BufferedWriter

    @PostConstruct
    fun connectTo() {
        port = if (props.breizhcamp.arduinoPort == "auto") {
            val ports = SerialPort.getCommPorts()
            if (ports.isEmpty()) throw IllegalStateException("No serial ports detected")
            ports.first()
        } else {
            SerialPort.getCommPort(props.breizhcamp.arduinoPort)
        } ?: throw java.lang.IllegalStateException("Cannot find Arduino port")

        logger.info { "Connecting to Arduino on port [${port.systemPortName}]" }

        port.setComPortParameters(115200, 8, 1, 0)
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0)
        port.clearDTR()
        port.clearRTS()

        if (!port.openPort()) {
            logger.error { "Unable to connect to Arduino on port [${port.systemPortName}]" }
            return
        }

        logger.info { "Connected to Arduino on port [${port.systemPortName}]" }

        input = port.inputStream.bufferedReader()
        output = port.outputStream.bufferedWriter()
    }

    @PreDestroy
    fun close() {
        port.closePort()
    }

    override fun preNageru(preview: Boolean) {
        if (preview) return
        switchToSpeaker()
    }

    override fun postNageru(preview: Boolean) {
        if (preview) return
        switchToCamaaloth()
    }

    private fun switchToSpeaker() = sendKey("1")
    private fun switchToCamaaloth() = sendKey("2")

    private fun sendKey(key: String) {
        output.write("$key\n")
        output.flush()

        val line = input.readLine()

        if (line != "$key sent") {
            logger.error { "Arduino: $key sent but return is not expected : [$line]" }
        }
    }
}