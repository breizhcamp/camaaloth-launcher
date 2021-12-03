package org.breizhcamp.camaalothlauncher

import mu.KotlinLogging
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage

private val logger = KotlinLogging.logger {}

fun main() {
    val infos = MidiSystem.getMidiDeviceInfo()

    logger.info { "[MIDI] Devices list: " + infos.joinToString { it.name } }



    for (info in infos) {
        logger.info { "[MIDI] Device ${info.name}" }
        val device = MidiSystem.getMidiDevice(info)

        if (info.name.contains("XIAO") && device.maxReceivers != 0) {
            logger.info { "Receiving device" }
            device.open()

            //RGB
            var btn = 4
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 10, btn, 127), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 11, btn, 0), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 12, btn, 0), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 9, btn, 127), -1)

            //BRIGHTNESS
//            device.receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE or 9, btn, 120), -1)

            btn = 8
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 10, btn, 0), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 11, btn, 127), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 12, btn, 0), -1)
            device.receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE or 9, btn, 30), -1)

            //BLINK
            device.receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE or 10, btn, 64), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 9, btn, 127), -1)


            btn = 10
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 10, btn, 42), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 11, btn, 0), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 12, btn, 127), -1)
            device.receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE or 9, btn, 64), -1)

            device.receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE or 11, btn, 32), -1)
            device.receiver.send(ShortMessage(ShortMessage.NOTE_ON or 9, btn, 127), -1)

            device.close()
        }
    }
}