package org.breizhcamp.camaalothlauncher.services.midi

import org.breizhcamp.camaalothlauncher.dto.PadMsg
import javax.sound.midi.MidiDevice

/**
 * Interface to implement in order to handle receiving message from a midi controller
 */
interface MidiReceiveController {

    fun handle(device: MidiDevice, info: MidiDevice.Info): Boolean

    fun event(status: Int, bank: Int, msg: ByteArray): PadMsg
}