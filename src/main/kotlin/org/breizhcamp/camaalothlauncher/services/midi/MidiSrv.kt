package org.breizhcamp.camaalothlauncher.services.midi

import mu.KotlinLogging
import org.breizhcamp.camaalothlauncher.dto.MidiReceivedMsg
import org.breizhcamp.camaalothlauncher.dto.PadMsg
import org.breizhcamp.camaalothlauncher.services.NageruHook
import org.breizhcamp.camaalothlauncher.services.midi.MidiSrv.MidiWay.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiMessage
import javax.sound.midi.MidiSystem

private val logger = KotlinLogging.logger {}

/**
 * Class for Midi controller connection
 */
@Service
class MidiSrv(
    private val receiveCtrls: List<MidiReceiveController>,
    private val sendingCtrls: List<MidiSendController>,
    private val publisher: ApplicationEventPublisher,
): NageruHook {

    private val receivingDevices = ArrayList<MidiDevice>()
    private val transmittingDevices = ArrayList<Pair<MidiDevice, MidiSendController>>()

    override fun preNageru(preview: Boolean) {
        logger.info { "[MIDI] Disconnecting all ${receivingDevices.size} devices" }
        disconnect()
    }

    override fun postNageru(preview: Boolean) {
        logger.info { "[MIDI] Connecting devices" }
        connect()
    }

    @PreDestroy
    fun shutdown() {
        disconnect()
    }

    fun list(): String {
        val infos = MidiSystem.getMidiDeviceInfo()
        return infos.joinToString("\n") { info ->
            val ways = getMidiWays(MidiSystem.getMidiDevice(info)).joinToString { it.label }
            " - ${info.name}: $ways"
        }
    }

    fun connect() {
        val infos = MidiSystem.getMidiDeviceInfo()

        logger.info { "[MIDI] Devices list: " + infos.joinToString { it.name } }

        for (info in infos) {
            logger.debug { "[MIDI] Device ${info.name}" }
            val device = MidiSystem.getMidiDevice(info)
            val ways = getMidiWays(device)

            val receivers = receiveCtrls.filter { it.handle(device, info) }

            if (receivers.isNotEmpty() && ways.contains(RECEIVING)) {
                logger.info { "[MIDI] Opening [${info.name}] device for receiving" }
                device.open()
                receivers.forEach { device.transmitter.receiver = Receiver(it, publisher) }
                receivingDevices.add(device)
            }

            val senders = sendingCtrls.filter { it.handle(device, info) }
            if (senders.isNotEmpty() && ways.contains(TRANSMITTING)) {
                logger.info { "[MIDI] Opening [${info.name}] device for sending" }
                device.open()
                senders.forEach { transmittingDevices.add(device to it) }
            }
        }
    }

    fun setValue(message: PadMsg) {
        transmittingDevices.forEach { (device, ctrl) ->
            ctrl.getSendMsg(message)?.let {
                logger.debug { "Sending message to MIDI Controller: ${it.message}" }
                device.receiver.send(it, -1)
            }
        }
    }

    private fun getMidiWays(device: MidiDevice): List<MidiWay> {
        val res = ArrayList<MidiWay>()
        if (device.maxTransmitters != 0) res.add(RECEIVING)
        if (device.maxReceivers != 0) res.add(TRANSMITTING)
        if (res.isEmpty()) res.add(NONE)

        return res
    }

    private fun disconnect() {
        transmittingDevices.forEach { (device, ctrl) -> ctrl.close().forEach { device.receiver.send(it, -1) } }

        receivingDevices.filter(MidiDevice::isOpen).forEach(MidiDevice::close)
        receivingDevices.clear()
    }

    private class Receiver(
        private val midiReceiveCtrl: MidiReceiveController,
        private val publisher: ApplicationEventPublisher,
    ) : javax.sound.midi.Receiver {


        override fun send(message: MidiMessage, timeStamp: Long) {
            val op = message.status and 0xF0
            val bank = message.status and 0x0F

            logger.info {
                val msg = message.message.joinToString(" - ") { it.toString() }
                "Receiving [${message.length}] bytes with status [${message.status}], op [$op] and bank [$bank]: ${msg}"
            }

            val padMsg = midiReceiveCtrl.event(op, bank, message.message)
            logger.debug { padMsg }

            if (padMsg.action != PadMsg.PadAction.UNKNOWN) {
                publisher.publishEvent(MidiReceivedMsg(padMsg))
            }
        }

        override fun close() {
            logger.info { "Closing Midi Receiver" }
        }
    }

    private enum class MidiWay(val label: String) {
        RECEIVING("Receiving"), TRANSMITTING("Transmitting"), NONE("No receiving or transmitting")
    }
}