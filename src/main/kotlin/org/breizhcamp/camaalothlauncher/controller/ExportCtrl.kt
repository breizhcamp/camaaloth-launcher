package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.ExportStart
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.ConvertSrv
import org.breizhcamp.camaalothlauncher.services.FilesSrv
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.InputStream
import java.time.Duration

/**
 * Controller for 040-export
 */
@RestController @RequestMapping("/export")
class ExportCtrl(private val state: State, private val convertSrv: ConvertSrv, private val filesSrv: FilesSrv) {

    @PostMapping("/start")
    fun start(): ExportStart {
        val recordingPath = state.recordingPath ?: return ExportStart(Duration.ZERO, 0)

        val sizes = filesSrv.filesSize(state.filesToExport);
        val length = convertSrv.startConvert(recordingPath, state.filesToExport)

        return ExportStart(length, sizes)
    }

    /**
     * Retrieve progress sent by ffmpeg during conversion.
     */
    @PostMapping("/progress")
    fun progress(input: InputStream) {
        convertSrv.progress(input)
    }
}