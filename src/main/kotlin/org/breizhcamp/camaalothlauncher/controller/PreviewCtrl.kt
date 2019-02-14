package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.LongCmdRunner
import org.breizhcamp.camaalothlauncher.services.NageruSrv
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import java.nio.file.Files

/**
 * Handle method for 020-preview
 */
@RestController @RequestMapping("/preview")
class PreviewCtrl(private val state: State, private val nageruSrv: NageruSrv) {

    @PostMapping("/start") @ResponseStatus(NO_CONTENT)
    fun startNageru() {
        val preview = state.previewDir() ?: return
        clearPreviewDir()
        nageruSrv.start(preview, "/020-nageru-preview-out")
    }

    @PostMapping("/view") @ResponseStatus(NO_CONTENT)
    fun readVlc() {
        val preview = state.previewDir() ?: return

        Files.newDirectoryStream(preview, "*.nut")
            .use { it.firstOrNull() }
            ?.let { nutFile ->
                val cmd = listOf("vlc", nutFile.toAbsolutePath().toString())
                LongCmdRunner("vlc", cmd, preview).start()
            }
    }

    @DeleteMapping @ResponseStatus(NO_CONTENT)
    fun clearPreviewDir() {
        val preview = state.previewDir() ?: return
        preview.toFile().listFiles().forEach { it.deleteRecursively() }
    }

}