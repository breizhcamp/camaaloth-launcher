package org.breizhcamp.camaalothlauncher.services.obs

import net.twasi.obsremotejava.OBSRemoteController
import org.breizhcamp.camaalothlauncher.dto.PadMsg

/** Implement this interface if you want to handle coordination between midi device and OBS */
interface ObsHandler {

    /** Called on startup when initializing controller, register callback in here */
    fun setup()

    /** Called when controller is connected */
    fun connected() { }

    /** Called when a new midi message is received */
    fun onMidiReceived(msg: PadMsg)
}