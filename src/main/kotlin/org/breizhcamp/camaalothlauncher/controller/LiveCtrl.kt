package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.FileMeta
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.FilesSrv
import org.breizhcamp.camaalothlauncher.services.NageruSrv
import org.breizhcamp.camaalothlauncher.services.ViewVideoSrv
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*
import java.nio.file.Paths
import java.time.Duration

/**
 * Handle method for 030-live
 */
@RestController @RequestMapping("/live")
class LiveCtrl(private val state: State, private val nageruSrv: NageruSrv, private val filesSrv: FilesSrv,
               private val viewVideoSrv: ViewVideoSrv) {

    @PostMapping("/start") @ResponseStatus(NO_CONTENT)
    fun startNageru() {
        val recordingDir = state.recordingPath ?: throw IllegalStateException("Current talk not set")
        nageruSrv.start(recordingDir, "/030-nageru-live-out")
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

        state.filesToConvert = files.map { recordingDir.resolve(it) }
    }
}