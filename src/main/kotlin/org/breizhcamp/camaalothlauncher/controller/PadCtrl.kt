package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.PadMsg
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

/**
 * Handle Pad message and send it to connected webpages
 */
@Controller
class PadCtrl(private val msgTpl: SimpMessagingTemplate) {

    fun send(msg: PadMsg) {
        msgTpl.convertAndSend("/pad", msg)
    }

}