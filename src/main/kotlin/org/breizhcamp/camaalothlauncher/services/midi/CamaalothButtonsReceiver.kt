package org.breizhcamp.camaalothlauncher.services.midi

import org.breizhcamp.camaalothlauncher.dto.PadMsg
import org.breizhcamp.camaalothlauncher.dto.PadMsg.*
import org.springframework.stereotype.Service
import javax.sound.midi.MidiDevice
import javax.sound.midi.ShortMessage

@Service
class CamaalothButtonsReceiver: MidiReceiveController {
    override fun handle(device: MidiDevice, info: MidiDevice.Info): Boolean {
        return info.name.contains("XIAO")
    }

    override fun event(status: Int, bank: Int, msg: ByteArray): PadMsg {
        val note = msg[1].toInt()

        val action = when(status) {
            ShortMessage.NOTE_ON -> PadAction.PRESSED
            ShortMessage.NOTE_OFF -> PadAction.RELEASED
            ShortMessage.CONTROL_CHANGE -> PadAction.VALUE
            else -> PadAction.UNKNOWN
        }

        val type = when {
            buttons.containsKey(note) -> PadType.PAD
            note == 15 || note == 16 -> PadType.KNOB
            else -> PadType.UNKNOWN
        }
        TODO()
    }

    private val buttons: HashMap<Int, String> = hashMapOf(
        1 to "pc", 2 to "cam1", 3 to "cam2", 4 to "pc_cam", 5 to "cam_pc", 6 to "title",
        7 to "cut", 8 to "fade", 9 to "anim",
        10 to "start_rec", 11 to "stop_rec",

        21 to "ratio", 22 to "sbs_cam1", 23 to "sbs_cam2", 24 to "cam_left", 25 to "cam_right",
    )
}