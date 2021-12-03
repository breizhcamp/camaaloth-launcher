package org.breizhcamp.camaalothlauncher.dto

/**
 * Message sent when an event happens on the control pad
 */
data class PadMsg(
    val buttonName: String,
    val type: PadType,
    val action: PadAction,
    val value: Int?
) {
    companion object {
        const val UNKNOWN_NAME = "UNKNOWN"
    }

    enum class PadType {
        PAD, KNOB, UNKNOWN
    }

    enum class PadAction {
        PRESSED, RELEASED, VALUE, UNKNOWN
    }
}