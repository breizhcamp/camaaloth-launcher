package org.breizhcamp.camaalothlauncher.services.obs

import mu.KotlinLogging
import net.twasi.obsremotejava.OBSRemoteController
import net.twasi.obsremotejava.events.models.SwitchScenesEvent
import org.breizhcamp.camaalothlauncher.dto.*
import org.breizhcamp.camaalothlauncher.services.midi.MidiSrv
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/** Handle OBS scenes */
@Component
class ObsScenes(
    private val controller: OBSRemoteController,
    private val midiSrv: MidiSrv,
): ObsHandler {

    private var currentScene = ""

    override fun setup() {
        controller.registerSwitchScenesCallback(::onSceneChange)
    }

    override fun connected() {
        initScenesButtons()
    }

    override fun onMidiReceived(msg: PadMsg) {
        if (msg.action != PadMsg.PadAction.PRESSED) return
        setScene(msg.buttonName)
    }

    /** Define current scene in OBS */
    private fun setScene(btnName: String) {
        val newScene = btnScenes[btnName] ?: return
        //send new scene event if the same scene just in case
        controller.setCurrentScene(newScene) {}

        if (newScene != currentScene) {
            //don't blink current led for the same scene because onSceneChange is not triggered
            midiSrv.setValue(BlinkPadValue(btnName, BlinkPattern.BLINK, 64))
            midiSrv.setValue(StatePadValue(btnName, StateValue.ON))
        }
    }

    private fun onSceneChange(event: SwitchScenesEvent) {
        logger.info { "[OBS] Scene changed to [${event.sceneName}]" }
        currentScene = event.sceneName
        displayLedScene(event.sceneName)
    }


    private fun displayLedScene(sceneName: String) {
        val btn = scenesBtn[sceneName] ?: return
        scenesBtn.values.filter { it != sceneName }.forEach { midiSrv.setValue(StatePadValue(it, StateValue.OFF)) }
        midiSrv.setValue(BlinkPadValue(btn, BlinkPattern.NONE))
        midiSrv.setValue(StatePadValue(btn, StateValue.ON))
    }


    private fun initScenesButtons() {
        scenesBtn.values.forEach {
            midiSrv.setValue(ColorPadValue(it, 127, 0, 0))
        }
        controller.getCurrentScene {
            currentScene = it.name
            displayLedScene(it.name)
        }
    }

    private val btnScenes: Map<String, String> = hashMapOf(
        "pc" to "PC", "cam1" to "CAM1", "cam2" to "CAM2", "pc_cam" to "PC/CAM", "cam_pc" to "CAM/PC", "title" to "TITRE"
    )
    private val scenesBtn = btnScenes.entries.associate { (k, v) -> v to k }

}