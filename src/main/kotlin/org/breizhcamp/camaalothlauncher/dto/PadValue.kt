package org.breizhcamp.camaalothlauncher.dto

sealed class PadValue(open val buttonName: String)

data class SimplePadValue(
    override val buttonName: String,
    val value: Int
): PadValue(buttonName)

data class ColorPadValue(
    override val buttonName: String,

    val red: Int,
    val green: Int,
    val blue: Int,
): PadValue(buttonName)

data class StatePadValue(
    override val buttonName: String,
    val state: StateValue
): PadValue(buttonName)

enum class StateValue { ON, OFF }

data class BrightnessPadValue(
    override val buttonName: String,
    val brightness: Int
): PadValue(buttonName)

data class BlinkPadValue(
    override val buttonName: String,
    val blinkPattern: BlinkPattern,
    val speed: Int = 0,
): PadValue(buttonName)

enum class BlinkPattern { NONE, BLINK, BREATH }