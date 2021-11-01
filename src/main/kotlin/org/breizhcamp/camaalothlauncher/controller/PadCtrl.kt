package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.MidiReceivedMsg
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

/**
 * Handle Pad message and send it to connected webpages
 */
@Controller
class PadCtrl(private val msgTpl: SimpMessagingTemplate) {

    @EventListener
    fun send(event: MidiReceivedMsg) {
        msgTpl.convertAndSend("/pad", event.msg)
    }

}