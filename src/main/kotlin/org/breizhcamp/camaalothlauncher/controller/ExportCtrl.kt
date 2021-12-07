package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.ExportInfos
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.ConvertSrv
import org.breizhcamp.camaalothlauncher.services.FilesSrv
import org.breizhcamp.camaalothlauncher.services.TalkSrv
import org.springframework.web.bind.annotation.*
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration

/**
 * Controller for 030-export
 */
@RestController @RequestMapping("/export")
class ExportCtrl(
    private val state: State,
    private val convertSrv: ConvertSrv,
    private val filesSrv: FilesSrv,
    private val talkSrv: TalkSrv,
) {

    @GetMapping("/infos")
    fun infos(): ExportInfos {
        return ExportInfos(filesSrv.videoFileLength(state.filesToExport), filesSrv.filesSize(state.filesToExport))
    }

    @PostMapping("/start")
    fun start(@RequestParam dir: String) {
        val recordingPath = state.recordingPath ?: return
        val destDir = Path.of(dir, "videos", recordingPath.fileName.toString())
        talkSrv.copyMetadataToDest(destDir, state)
        convertSrv.startConvert(destDir, recordingPath, state.filesToExport)
    }

    /**
     * Retrieve progress sent by ffmpeg during conversion.
     */
    @PostMapping("/progress")
    fun progress(input: InputStream) {
        convertSrv.progress(input)
    }
}