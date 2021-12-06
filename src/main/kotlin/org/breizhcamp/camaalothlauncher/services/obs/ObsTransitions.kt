package org.breizhcamp.camaalothlauncher.services.obs

import mu.KotlinLogging
import net.twasi.obsremotejava.OBSRemoteController
import net.twasi.obsremotejava.events.models.SwitchTransitionEvent
import org.breizhcamp.camaalothlauncher.dto.*
import org.breizhcamp.camaalothlauncher.services.midi.MidiSrv
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Handle transitions selection on OBS
 *
 * Lights:
 *  - When a specific transition is selected, the corresponding button is light with light green
 *  - When no specific transition is selected (OBS Theme choose the transition with matrix plugin), no lights is displayed
 *
 * Buttons:
 *  - When a button is pressed, the corresponding transition is selected
 *  - When a button already selected (light up) is pressed, the current transition is deselected
 *      and the matrix plugin is used to determine the transition
 */
@Component
class ObsTransitions(
    private val controller: OBSRemoteController,
    private val midiSrv: MidiSrv,
): ObsHandler {
    private enum class Mode { MANUAL, AUTO }
    private var mode = Mode.MANUAL
    private var current: String = ""

    override fun setup() {
        controller.registerSwitchTransitionCallback(::onSwitch)
    }

    private fun onSwitch(event: SwitchTransitionEvent) {
        logger.info { "[OBS] Transition switched to [${event.transitionName}]" }
        current = event.transitionName
        displayLedState()
    }

    override fun connected() {
        initLedState()
        controller.getTransitionList {
            current = it.currentTransition
            displayLedState()
        }
    }

    override fun onMidiReceived(msg: PadMsg) {
        if (msg.action != PadMsg.PadAction.PRESSED) return
        setTransition(msg.buttonName)
    }

    private fun setTransition(btnName: String) {
        val transition = transitionsByBtn[btnName] ?: return
        if (transition != current) {
            mode = Mode.MANUAL
            controller.setCurrentTransition(transition) {}

        } else {
            if (mode == Mode.MANUAL) {
                mode = Mode.AUTO
                //TODO switch to matrix mode
            } else {
                mode = Mode.MANUAL
            }
            displayLedState()
        }
    }

    private fun initLedState() {
        switchAllLedOff()
        btnByTransitions.values.forEach {
            midiSrv.setValue(ColorPadValue(it, 0, 127, 0))
            midiSrv.setValue(BrightnessPadValue(it, 50))
        }
    }

    private fun displayLedState() {
        switchAllLedOff()
        if (mode == Mode.MANUAL) {
            val btn = btnByTransitions[current] ?: return
            midiSrv.setValue(StatePadValue(btn, StateValue.ON))
        }
    }

    private fun switchAllLedOff() {
        btnByTransitions.values.forEach { midiSrv.setValue(StatePadValue(it, StateValue.OFF)) }
    }

    private val transitionsByBtn = mapOf("cut" to "Coupure", "fade" to "Fondu")
    private val btnByTransitions = transitionsByBtn.entries.associate { (k, v) -> v to k }
}