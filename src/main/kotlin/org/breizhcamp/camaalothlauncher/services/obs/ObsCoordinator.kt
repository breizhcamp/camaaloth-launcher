package org.breizhcamp.camaalothlauncher.services.obs

import mu.KotlinLogging
import net.twasi.obsremotejava.OBSRemoteController
import net.twasi.obsremotejava.requests.GetVersion.GetVersionResponse
import org.breizhcamp.camaalothlauncher.dto.*
import org.breizhcamp.camaalothlauncher.services.midi.MidiSrv
import org.breizhcamp.camaalothlauncher.services.recorder.RecorderHook
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
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
    private var wsNeedsConnected = false
    private var runner: ObsCoordinatorRunner? = null

    @PostConstruct
    fun setup() {
        controller.registerConnectCallback(::onConnect)
        controller.registerConnectionFailedCallback { onFailedConnect(it) }
        controller.registerDisconnectCallback(::onDisconnect)
        obsHandlers.forEach { it.setup() }
    }

    override fun postStart() {
        wsNeedsConnected = true
        runner = ObsCoordinatorRunner(this)
        runner?.start()
        askConnect()
    }

    override fun postRecord(preview: Boolean) {
        wsNeedsConnected = false
        runner?.shutdown()
    }

    @EventListener
    fun onMidiReceived(event: MidiReceivedMsg) {
        if (!connected) return
        obsHandlers.forEach { it.onMidiReceived(event.msg) }
    }

    fun sleepConnect() {
        try {
            if (connected) return

            // waiting for OBS starting
            Thread.sleep(1000)
            if (!wsNeedsConnected) return

            logger.info { "[OBS] Connecting to WebSocket..." }
            controller.connect()
        } catch (e: InterruptedException) {
            logger.info { "[OBS] Coordinator shutdown, stop trying WebSocket connection" }
            wsNeedsConnected = false
        }
    }

    fun disconnect() {
        if (connected) controller.disconnect()
    }

    private fun onConnect(res: GetVersionResponse) {
        logger.info { "[OBS] Connected to WebSocket - OBS version [${res.obsStudioVersion}]" }
        connected = true
        midiSrv.setValue(StatePadValue("all", StateValue.OFF))
        midiSrv.setValue(BrightnessPadValue("all", 100))
        obsHandlers.forEach { it.connected() }
    }

    private fun onFailedConnect(msg: String) {
        logger.warn { "[OBS] Unable to connect to WebSocket, retrying..., error: $msg" }
        askConnect()
    }

    private fun onDisconnect() {
        logger.info { "[OBS] Disconnected from WebSocket" }
        connected = false
        midiSrv.setValue(StatePadValue("all", StateValue.OFF))

        if (wsNeedsConnected) {
            logger.warn { "[OBS] Websocket failed while running, trying to reconnect" }
            askConnect()
        }
    }

    private fun askConnect() = runner?.connect()

    private class ObsCoordinatorRunner(private val coordinator: ObsCoordinator): Thread("ObsCoordinatorRunner") {
        sealed class CoordinatorAction {
            object Connect: CoordinatorAction()
            object Disconnect: CoordinatorAction()
        }

        val waitingQueue: BlockingQueue<CoordinatorAction> = LinkedBlockingQueue()
        private var running = true


        override fun run() {
            while (running) {
                when (waitingQueue.take()) {
                    is CoordinatorAction.Connect -> coordinator.sleepConnect()
                    is CoordinatorAction.Disconnect -> coordinator.disconnect()
                }
            }
        }


        fun connect() = waitingQueue.put(CoordinatorAction.Connect)

        fun shutdown() {
            running = false
            waitingQueue.put(CoordinatorAction.Disconnect)
        }
    }
}