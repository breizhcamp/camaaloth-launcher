package org.breizhcamp.camaalothlauncher.services

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.springframework.stereotype.Service

/**
 * Start/stop scripts before and after recording
 */
@Service
class ScriptsHooks(private val props: CamaalothProps): NageruHook {

    override fun preNageru(preview: Boolean) {
        if (preview) return

        val script = props.preRecordScript ?: return
        ShortCmdRunner("preScript", listOf("/bin/bash", script), true).run()
    }

    override fun postNageru(preview: Boolean) {
        if (preview) return

        val script = props.postRecordScript ?: return
        ShortCmdRunner("postScript", listOf("/bin/bash", script), true).run()
    }
}