package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.FileMeta
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.FilesSrv
import org.breizhcamp.camaalothlauncher.services.ViewVideoSrv
import org.breizhcamp.camaalothlauncher.services.recorder.NageruSrv
import org.breizhcamp.camaalothlauncher.services.recorder.ObsSrv
import org.breizhcamp.camaalothlauncher.services.recorder.RecorderType.NAGERU
import org.breizhcamp.camaalothlauncher.services.recorder.RecorderType.OBS
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import java.nio.file.Paths
import java.time.Duration

/**
 * Handle method for 030-live
 */
@RestController @RequestMapping("/record")
class RecordCtrl(
    private val state: State,
    private val props: CamaalothProps,
    private val nageruSrv: NageruSrv,
    private val obsSrv: ObsSrv,
    private val filesSrv: FilesSrv,
    private val viewVideoSrv: ViewVideoSrv,
) {

    @PostMapping("/start") @ResponseStatus(NO_CONTENT)
    fun startRecord() {
        val recordingDir = state.recordingPath ?: throw IllegalStateException("Current talk not set")
        when (props.recorder) {
            NAGERU -> nageruSrv.start(recordingDir, "/020-record-live-out")
            OBS -> obsSrv.start(recordingDir, "/020-record-live-out")
        }
    }

    @GetMapping("/files")
    fun listFiles() : List<FileMeta> {
        val recordingDir = state.recordingPath ?: return emptyList()
        return filesSrv.listFiles(recordingDir, "*.nut", null)
    }

    @GetMapping("/duration")
    fun duration(@RequestParam file: String) : Duration {
        return filesSrv.fileDuration(Paths.get(file))
    }

    @PostMapping("/view") @ResponseStatus(NO_CONTENT)
    fun view(@RequestParam file: String) {
        val recordingDir = state.recordingPath ?: throw IllegalStateException("Current talk not set")
        viewVideoSrv.start(recordingDir.resolve(file), recordingDir)
    }

    @PostMapping("/export") @ResponseStatus(NO_CONTENT)
    fun export(@RequestBody files: List<String>) {
        val recordingDir = state.recordingPath ?: throw IllegalStateException("Current talk not set")

        state.filesToExport = files.map { recordingDir.resolve(it) }
    }
}