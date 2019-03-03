package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.ConvertSrv
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.InputStream
import java.time.Duration

/**
 * Controller for 040-export
 */
@RestController @RequestMapping("/export")
class ExportCtrl(private val state: State, private val convertSrv: ConvertSrv) {

    @PostMapping("/start")
    fun start(): Duration {
        val recordingPath = state.recordingPath ?: return Duration.ZERO
        return convertSrv.startConvert(recordingPath, state.filesToExport)
    }

    /**
     * Retrieve progress sent by ffmpeg during conversion.
     */
    @PostMapping("/progress")
    fun progress(input: InputStream) {
        convertSrv.progress(input)
    }
}