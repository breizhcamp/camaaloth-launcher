package org.breizhcamp.camaalothlauncher.services.obs

import net.twasi.obsremotejava.OBSRemoteController
import org.breizhcamp.camaalothlauncher.dto.*
import org.breizhcamp.camaalothlauncher.services.midi.MidiSrv
import org.breizhcamp.camaalothlauncher.services.recorder.ObsSrv
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Handle start/stop recording buttons
 *
 * Lights:
 *  - When not recording, the start button is blinking fast in purple
 *  - When recording is starting, the start button is blinking normal in red
 *  - During recording, the stop button is breathing normal in red
 *  - When recording is stopping, the stop button is blinking normal in red
 *
 * Buttons:
 *  - When recording is stopped, push on "start rec" starts the recording and push on "stop rec" closes OBS
 *  - When recording, push on "stop rec" stops the recording
 */
@Component
class ObsRecord(
    private val controller: OBSRemoteController,
    private val midiSrv: MidiSrv,
    private val publisher: ApplicationEventPublisher,
): ObsHandler {
    private enum class RecordingState { STOPPED, STARTING, RECORDING, STOPPING }
    private var state = RecordingState.STOPPED

    override fun setup() {
        controller.registerRecordingStartedCallback { updateState(RecordingState.RECORDING) }
        controller.registerRecordingStoppedCallback { updateState(RecordingState.STOPPED) }
    }

    override fun connected() {
        midiSrv.setValue(ColorPadValue("stop_rec", 127, 0, 0))
        displayLedState()
    }

    override fun onMidiReceived(msg: PadMsg) {
        if (msg.action != PadMsg.PadAction.PRESSED) return
        when (msg.buttonName) {
            "start_rec" -> startRec()
            "stop_rec" -> stopRec()
        }
    }

    private fun startRec() {
        if (state == RecordingState.STOPPED) updateState(RecordingState.STARTING)
        controller.startRecording {}
    }

    private fun stopRec() {
        if (state == RecordingState.STOPPED) {
            publisher.publishEvent(ObsStopMsg())
        } else {
            if (state == RecordingState.RECORDING) updateState(RecordingState.STOPPING)
            controller.stopRecording {}
        }
    }

    private fun updateState(newState: RecordingState) {
        this.state = newState
        displayLedState()
    }

    private fun displayLedState() {
        midiSrv.setValue(StatePadValue("start_rec", StateValue.OFF))
        midiSrv.setValue(StatePadValue("stop_rec", StateValue.OFF))

        when (state) {
            RecordingState.STOPPED -> {
                midiSrv.setValue(ColorPadValue("start_rec", 63, 0, 63))
                midiSrv.setValue(BlinkPadValue("start_rec", BlinkPattern.BLINK, 115))
                midiSrv.setValue(StatePadValue("start_rec", StateValue.ON))
            }
            RecordingState.STARTING -> {
                midiSrv.setValue(ColorPadValue("start_rec", 127, 0, 0))
                midiSrv.setValue(BlinkPadValue("start_rec", BlinkPattern.BLINK, 63))
                midiSrv.setValue(StatePadValue("start_rec", StateValue.ON))
            }
            RecordingState.RECORDING -> {
                midiSrv.setValue(BlinkPadValue("stop_rec", BlinkPattern.BREATH, 63))
                midiSrv.setValue(StatePadValue("stop_rec", StateValue.ON))
            }
            RecordingState.STOPPING -> {
                midiSrv.setValue(BlinkPadValue("stop_rec", BlinkPattern.BLINK, 63))
                midiSrv.setValue(StatePadValue("stop_rec", StateValue.ON))
            }
        }
    }
}