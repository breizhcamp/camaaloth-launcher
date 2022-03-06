package org.breizhcamp.camaalothlauncher.services.midi

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.*
import org.breizhcamp.camaalothlauncher.dto.PadMsg.PadAction
import org.breizhcamp.camaalothlauncher.dto.PadMsg.PadType
import org.springframework.stereotype.Service
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiMessage
import javax.sound.midi.ShortMessage

@Service
class CamaalothButtonsReceiver(private val props: CamaalothProps): MidiReceiveController, MidiSendController {
    override fun handle(device: MidiDevice, info: MidiDevice.Info): Boolean {
        return info.name.contains(props.midi.xiaoName)
    }

    override fun event(status: Int, bank: Int, msg: ByteArray): PadMsg {
        val note = msg[1].toInt()

        val action = when(status) {
            ShortMessage.NOTE_ON -> PadAction.PRESSED
            ShortMessage.NOTE_OFF -> PadAction.RELEASED
            ShortMessage.CONTROL_CHANGE -> PadAction.VALUE
            else -> PadAction.UNKNOWN
        }

        val type = buttons[note]?.type ?: PadType.UNKNOWN
        val btn = buttons[note]?.name ?: PadMsg.UNKNOWN_NAME

        val value = if (msg.size > 1) msg[2].toInt() else null

        return PadMsg(btn, type, action, value)
    }

    override fun getSendMsg(message: PadValue): List<MidiMessage> {
        val btn = names[message.buttonName] ?: return emptyList()
        return when (message) {
            is StatePadValue -> statePadMsg(message, btn)
            is ColorPadValue -> colorPadMsg(message, btn)
            is BrightnessPadValue -> listOf(msg(ShortMessage.CONTROL_CHANGE, btn, message.brightness))
            is BlinkPadValue -> blinkPadMsg(message, btn)
            is SimplePadValue -> emptyList()
        }
    }

    /** Mapping for LED ON / OFF */
    private fun statePadMsg(message: StatePadValue, btn: Int) = listOf(
        if (message.state == StateValue.ON) msg(ShortMessage.NOTE_ON, btn, 127) else msg(ShortMessage.NOTE_OFF, btn, 0)
    )

    /** Mapping for LED color */
    private fun colorPadMsg(message: ColorPadValue, btn: Int) = listOf(
        msg(ShortMessage.NOTE_ON, btn, message.red, 10),
        msg(ShortMessage.NOTE_ON, btn, message.green, 11),
        msg(ShortMessage.NOTE_ON, btn, message.blue, 12),
    )

    /** Mapping for blink */
    private fun blinkPadMsg(message: BlinkPadValue, btn: Int) = listOf(
        when (message.blinkPattern) {
            BlinkPattern.NONE -> msg(ShortMessage.CONTROL_CHANGE, btn, 0, 10)
            BlinkPattern.BLINK -> msg(ShortMessage.CONTROL_CHANGE, btn, message.speed, 10)
            BlinkPattern.BREATH -> msg(ShortMessage.CONTROL_CHANGE, btn, message.speed, 11)
        }
    )

    private fun msg(command: Int, button: Int, value: Int = 0, channel: Int = 9) = ShortMessage(command, channel, button, value)

    override fun close(): List<MidiMessage> {
        return getSendMsg(StatePadValue("all", StateValue.OFF))
    }

    private val buttons: HashMap<Int, Btn> = hashMapOf(
        1 to Btn("pc"), 2 to Btn("cam1"), 3 to Btn("cam2"),
        4 to Btn("pc_cam"), 5 to Btn("cam_pc"), 6 to Btn("title"),

        7 to Btn("cut"), 8 to Btn("fade"), 9 to Btn("anim"),
        10 to Btn("start_rec"), 11 to Btn("stop_rec"),

        15 to Btn("scroll", PadType.KNOB),

        21 to Btn("ratio"), 22 to Btn("sbs_cam1"), 23 to Btn("sbs_cam2"),
        24 to Btn("cam_left"), 25 to Btn("cam_right"),
    )

    private val names = buttons.entries.associate { (k, v) -> v.name to k } + mapOf("all" to 64)

    private data class Btn(val name: String, val type: PadType = PadType.PAD)
}