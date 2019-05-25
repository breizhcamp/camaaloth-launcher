package org.breizhcamp.camaalothlauncher.dto

/**
 * Message sent when an event happens on the control pad
 */
data class PadMsg(
        val type: PadType,
        val action: PadAction,
        val button: String,
        val bank: String,
        val value: Int
) {
    enum class PadType {
        PAD, KNOB, UNKNOWN
    }

    enum class PadAction {
        PRESSED, RELEASED, VALUE, UNKNOWN
    }
}