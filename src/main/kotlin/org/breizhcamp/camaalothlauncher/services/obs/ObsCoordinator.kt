package org.breizhcamp.camaalothlauncher.services.obs

import mu.KotlinLogging
import net.twasi.obsremotejava.OBSRemoteController
import net.twasi.obsremotejava.events.models.SwitchScenesEvent
import net.twasi.obsremotejava.requests.GetVersion.GetVersionResponse
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.*
import org.breizhcamp.camaalothlauncher.services.midi.MidiSrv
import org.breizhcamp.camaalothlauncher.services.recorder.RecorderHook
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

private val logger = KotlinLogging.logger {}

/**
 * Coordinate OBS when running for handling midi events and configuration via OBS Websocket
 */
@Service
class ObsCoordinator(
    private val controller: OBSRemoteController,
    private val midiSrv: MidiSrv,
    private val obsHandlers: List<ObsHandler>,
): RecorderHook {

    private var connected = false

    @PostConstruct
    fun setup() {
        controller.registerConnectCallback(::onConnect)
        controller.registerDisconnectCallback(::onDisconnect)
        obsHandlers.forEach { it.setup() }
    }

    override fun postStart() {
        // waiting for OBS starting
        Thread.sleep(1000)
        controller.connect()
    }

    override fun postRecord(preview: Boolean) = controller.disconnect()


    @EventListener
    fun onMidiReceived(event: MidiReceivedMsg) {
        if (!connected) return
        obsHandlers.forEach { it.onMidiReceived(event.msg) }
    }

    private fun onConnect(res: GetVersionResponse) {
        logger.info { "[OBS] Connected to WebSocket - OBS version [${res.obsStudioVersion}]" }
        connected = true
        midiSrv.setValue(StatePadValue("all", StateValue.OFF))
        midiSrv.setValue(BrightnessPadValue("all", 100))
        obsHandlers.forEach { it.connected() }
    }

    private fun onDisconnect() {
        logger.info { "[OBS] Disconnected from WebSocket" }
        connected = false
        midiSrv.setValue(StatePadValue("all", StateValue.OFF))
    }
}