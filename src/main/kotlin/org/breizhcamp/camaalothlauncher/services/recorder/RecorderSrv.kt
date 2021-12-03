package org.breizhcamp.camaalothlauncher.services.recorder

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy

@Service
class RecorderSrv(private val hooks: List<RecorderHook> = emptyList()) {

    /** Run post hooks at startup to init everything */
    @EventListener(ApplicationReadyEvent::class)
    fun runStartupHooks() {
        hooks.forEach { it.postRecord(false) }
    }

    /** Run pre hooks on shutdown to reset state */
    @PreDestroy
    fun runStopHooks() {
        hooks.forEach { it.preRecord(false) }
    }

}