package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.NageruSrv
import org.breizhcamp.camaalothlauncher.services.ViewVideoSrv
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import java.nio.file.Files

/**
 * Handle method for 020-preview
 */
@RestController @RequestMapping("/preview")
class PreviewCtrl(private val state: State, private val nageruSrv: NageruSrv, private val viewVideoSrv: ViewVideoSrv) {

    @PostMapping("/start") @ResponseStatus(NO_CONTENT)
    fun startNageru() {
        val preview = state.previewDir() ?: return
        clearPreviewDir()
        nageruSrv.start(preview, "/020-nageru-preview-out", true)
    }

    @PostMapping("/view") @ResponseStatus(NO_CONTENT)
    fun readVlc() {
        val preview = state.previewDir() ?: return

        Files.newDirectoryStream(preview, "*.nut")
            .use { it.firstOrNull() }
            ?.let { viewVideoSrv.start(it, preview) }
    }

    @DeleteMapping @ResponseStatus(NO_CONTENT)
    fun clearPreviewDir() {
        val preview = state.previewDir() ?: return
        preview.toFile().listFiles().forEach { it.deleteRecursively() }
    }

}