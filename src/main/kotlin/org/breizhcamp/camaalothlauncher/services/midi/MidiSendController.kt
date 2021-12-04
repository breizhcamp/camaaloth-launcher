package org.breizhcamp.camaalothlauncher.services.midi

import org.breizhcamp.camaalothlauncher.dto.PadValue
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiMessage

/**
 * Interface to implement in order to handle sending message to a midi controller
 */
interface MidiSendController {

    fun handle(device: MidiDevice, info: MidiDevice.Info): Boolean

    /** Retrieve the message to send to the midi device with this PadMsg */
    fun getSendMsg(message: PadValue): List<MidiMessage>

    /** Called when the software is shutting down */
    fun close(): List<MidiMessage>
}