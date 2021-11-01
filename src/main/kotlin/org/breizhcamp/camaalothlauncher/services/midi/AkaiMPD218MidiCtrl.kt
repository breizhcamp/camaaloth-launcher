package org.breizhcamp.camaalothlauncher.services.midi

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.PadMsg
import org.breizhcamp.camaalothlauncher.dto.PadMsg.PadAction
import org.breizhcamp.camaalothlauncher.dto.PadMsg.PadType
import org.springframework.stereotype.Component
import javax.sound.midi.MidiDevice
import javax.sound.midi.ShortMessage

/**
 * Implementation for Akai MPD 218
 */
@Component
class AkaiMPD218MidiCtrl(private val props: CamaalothProps): MidiController {

    override fun handle(device: MidiDevice, info: MidiDevice.Info): Boolean {
        val name = props.akaiName ?: return false
        return info.name.contains(name) && device.maxReceivers >= 0
    }

    override fun event(status: Int, bank: Int, msg: ByteArray): PadMsg {
        //see http://www.somascape.org/midi/tech/spec.html for MIDI message description
        //status has the first byte of the message, then msg get all message bytes

        //for button and knob, the Akai controller always use the same midi bank

        val action = when(status) {
            ShortMessage.NOTE_ON -> PadAction.PRESSED
            ShortMessage.NOTE_OFF -> PadAction.RELEASED
            ShortMessage.CONTROL_CHANGE -> PadAction.VALUE
            else -> PadAction.UNKNOWN
        }

        val type = when(action) {
            PadAction.PRESSED, PadAction.RELEASED -> PadType.PAD
            PadAction.VALUE -> PadType.KNOB
            PadAction.UNKNOWN -> PadType.UNKNOWN
        }

        val button = if ((type == PadType.PAD || type == PadType.KNOB) && msg.size > 1) {
            getBtnName(msg[1].toInt())
        } else {
            PadMsg.UNKNOWN_NAME
        }

        val value = msg[msg.size - 1].toInt()

        return PadMsg(button, type, action, value)
    }

    private val buttons: HashMap<Int, String> = hashMapOf(
        0x30 to "start_rec", 0x31 to "stop_rec",
        0x2c to "cut", 0x2d to "anim", 0x2e to "fade",
        0x28 to "title", 0x29 to "ratio",
        0x24 to "pc", 0x25 to "cam1", 0x26 to "cam2", 0x27 to "pc_cam"
    )

    private fun getBtnName(note: Int): String = buttons[note] ?: PadMsg.UNKNOWN_NAME
}