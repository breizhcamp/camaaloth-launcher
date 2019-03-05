package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.services.CopyThread
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Rest controller for copy operations
 */
@RestController @RequestMapping("/copy")
class CopyCtrl(private val copyThread: CopyThread) {

    @GetMapping("/waiting")
    fun listWaiting() = copyThread.listWaiting()

}
