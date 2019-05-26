package org.breizhcamp.camaalothlauncher.services.midi

import org.breizhcamp.camaalothlauncher.dto.PadMsg
import org.breizhcamp.camaalothlauncher.dto.PadMsg.PadAction
import org.breizhcamp.camaalothlauncher.dto.PadMsg.PadType
import org.springframework.stereotype.Component
import javax.sound.midi.MidiDevice

/**
 * Implementation for Akai MPD 128
 */
@Component
class AkaiMPD128MidiCtrl: MidiController {

    override fun handle(device: MidiDevice, info: MidiDevice.Info): Boolean {
        return info.name.contains("MPD218 [hw") && device.maxReceivers >= 0
    }

    override fun event(status: Int, bank: Int, msg: ByteArray): PadMsg {
        //see http://www.somascape.org/midi/tech/spec.html for MIDI message description
        //status has the first byte of the message, then msg get all message bytes

        //for button and knob, the Akai controller always use the same midi bank

        val action = when(status) {
            144 -> PadAction.PRESSED
            128 -> PadAction.RELEASED
            176 -> PadAction.VALUE
            else -> PadAction.UNKNOWN
        }

        val type = when(action) {
            PadAction.PRESSED, PadAction.RELEASED -> PadType.PAD
            PadAction.VALUE -> PadType.KNOB
            PadAction.UNKNOWN -> PadType.UNKNOWN
        }

        val (button, akaiBank) = if ((type == PadType.PAD || type == PadType.KNOB) && msg.size > 1) {
            getPadBankName(msg[1].toInt())
        } else {
            Pair("UNKNOWN", "UNKNOWN")
        }

        val value = msg[msg.size - 1].toInt()

        return PadMsg(type, action, button, akaiBank, value)
    }

    private val buttons: HashMap<Int, Btn> = hashMapOf(
            //knobs
             3 to Btn("1", "A"), 16 to Btn("1", "B"), 22 to Btn("1", "C"),
             9 to Btn("2", "A"), 17 to Btn("2", "B"), 23 to Btn("2", "C"),
            12 to Btn("3", "A"), 18 to Btn("3", "B"), 24 to Btn("3", "C"),
            13 to Btn("4", "A"), 19 to Btn("4", "B"), 25 to Btn("4", "C"),
            14 to Btn("5", "A"), 20 to Btn("5", "B"), 26 to Btn("5", "C"),
            15 to Btn("6", "A"), 21 to Btn("6", "B"), 27 to Btn("6", "C"),

            //buttons
            36 to Btn( "1", "A"), 52 to Btn( "1", "B"), 68 to Btn( "1", "C"),
            37 to Btn( "2", "A"), 53 to Btn( "2", "B"), 69 to Btn( "2", "C"),
            38 to Btn( "3", "A"), 54 to Btn( "3", "B"), 70 to Btn( "3", "C"),
            39 to Btn( "4", "A"), 55 to Btn( "4", "B"), 71 to Btn( "4", "C"),
            40 to Btn( "5", "A"), 56 to Btn( "5", "B"), 72 to Btn( "5", "C"),
            41 to Btn( "6", "A"), 57 to Btn( "6", "B"), 73 to Btn( "6", "C"),
            42 to Btn( "7", "A"), 58 to Btn( "7", "B"), 74 to Btn( "7", "C"),
            43 to Btn( "8", "A"), 59 to Btn( "8", "B"), 75 to Btn( "8", "C"),
            44 to Btn( "9", "A"), 60 to Btn( "9", "B"), 76 to Btn( "9", "C"),
            45 to Btn("10", "A"), 61 to Btn("10", "B"), 77 to Btn("10", "C"),
            46 to Btn("11", "A"), 62 to Btn("11", "B"), 78 to Btn("11", "C"),
            47 to Btn("12", "A"), 63 to Btn("12", "B"), 79 to Btn("12", "C"),
            48 to Btn("13", "A"), 64 to Btn("13", "B"), 80 to Btn("13", "C"),
            49 to Btn("14", "A"), 65 to Btn("14", "B"), 81 to Btn("14", "C"),
            50 to Btn("15", "A"), 66 to Btn("15", "B"), 82 to Btn("15", "C"),
            51 to Btn("16", "A"), 67 to Btn("16", "B"), 83 to Btn("16", "C")
    )

    private fun getPadBankName(note: Int): Pair<String, String> {
        val button = buttons[note] ?: return Pair("UNKNOWN", "UNKNOWN")
        return Pair(button.name, button.bank)
    }

    private data class Btn(val name: String, val bank: String)

}