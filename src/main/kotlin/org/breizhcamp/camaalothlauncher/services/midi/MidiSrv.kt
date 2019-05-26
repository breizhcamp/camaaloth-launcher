package org.breizhcamp.camaalothlauncher.services.midi

import mu.KotlinLogging
import org.breizhcamp.camaalothlauncher.controller.PadCtrl
import org.breizhcamp.camaalothlauncher.dto.PadMsg
import org.breizhcamp.camaalothlauncher.services.NageruHook
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
class MidiSrv(private val controllers: List<MidiController>, private val padCtrl: PadCtrl): Thread("MidiThread"), NageruHook {

    /** Does the thread is running */
    private var running = true

    private var connectedDevices: MutableList<MidiDevice> = ArrayList()

    @PreDestroy
    fun shutdown() {
        running = false
        disconnect()
    }

    override fun preNageru(preview: Boolean) {
        logger.info { "[MIDI] Disconnecting all ${connectedDevices.size} devices" }
        disconnect()
    }

    override fun postNageru(preview: Boolean) {
        logger.info { "[MIDI] Connecting devices" }
        connect()
    }


    private fun connect() {
        val infos = MidiSystem.getMidiDeviceInfo()

        for (info in infos) {
            logger.debug { "[MIDI] Device ${info.name}" }
            val device = MidiSystem.getMidiDevice(info)

            val handlers = controllers.filter { it.handle(device, info) }

            if (handlers.isNotEmpty()) {
                logger.info { "[MIDI] Opening [${info.name}] device" }
                device.open()
                handlers.forEach { device.transmitter.receiver = Receiver(it, padCtrl) }
                connectedDevices.add(device)
            }
        }
    }

    private fun disconnect() {
        connectedDevices.filter(MidiDevice::isOpen).forEach(MidiDevice::close)
        connectedDevices.clear()
    }

    private class Receiver(val midiCtrl: MidiController, val padCtrl: PadCtrl) : javax.sound.midi.Receiver {


        override fun send(message: MidiMessage, timeStamp: Long) {
            val op = message.status and 0xF0
            val bank = message.status and 0x0F

            logger.debug {
                val msg = message.message.joinToString(" - ") { it.toString() }
                "Receving ${message.length} bytes (${message.status} - $op - $bank) : ${msg}"
            }

            val padMsg = midiCtrl.event(op, bank, message.message)
            logger.debug { padMsg }

            if (padMsg.action != PadMsg.PadAction.UNKNOWN) {
                padCtrl.send(padMsg)
            }
        }

        override fun close() {
            logger.info { "Closing Midi Receiver" }
        }
    }
}