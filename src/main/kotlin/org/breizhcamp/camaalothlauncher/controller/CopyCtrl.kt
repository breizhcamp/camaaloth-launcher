package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.CopyProgress
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.CopyThread
import org.breizhcamp.camaalothlauncher.services.FilesSrv
import org.breizhcamp.camaalothlauncher.services.StateSrv
import org.springframework.http.HttpStatus
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.web.bind.annotation.*

/**
 * Rest controller for copy operations
 */
@RestController @RequestMapping("/copy")
class CopyCtrl(private val copyThread: CopyThread, private val state: State, private val stateSrv: StateSrv,
               private val filesSrv: FilesSrv) {

    @SubscribeMapping("/050-copy-progress")
    fun subscribe(): CopyProgress = copyThread.progress

    @GetMapping("/waiting")
    fun listWaiting() = copyThread.listWaiting()

    @PostMapping("/dir") @ResponseStatus(HttpStatus.NO_CONTENT)
    fun defineCopyDir(@RequestParam dir: String?) {
        state.copyingDir = dir
        stateSrv.save(State.Step.EXPORT, state)
    }

    @PostMapping("/shutdown") @ResponseStatus(HttpStatus.NO_CONTENT)
    fun shutdown() {
        filesSrv.shutdown()
    }
}
