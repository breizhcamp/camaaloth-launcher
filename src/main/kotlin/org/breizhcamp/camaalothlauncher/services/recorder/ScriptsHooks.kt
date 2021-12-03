package org.breizhcamp.camaalothlauncher.services.recorder

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.services.ShortCmdRunner
import org.springframework.stereotype.Service

/**
 * Start/stop scripts before and after recording
 */
@Service
class ScriptsHooks(private val props: CamaalothProps): RecorderHook {

    override fun preRecord(preview: Boolean) {
        if (preview) return

        val script = props.preRecordScript ?: return
        ShortCmdRunner("preScript", listOf("/bin/bash", script), true).run()
    }

    override fun postRecord(preview: Boolean) {
        if (preview) return

        val script = props.postRecordScript ?: return
        ShortCmdRunner("postScript", listOf("/bin/bash", script), true).run()
    }
}