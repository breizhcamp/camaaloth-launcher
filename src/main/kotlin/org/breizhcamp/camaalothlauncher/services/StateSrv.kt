package org.breizhcamp.camaalothlauncher.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.breizhcamp.camaalothlauncher.dto.State
import org.springframework.stereotype.Service

/**
 * Update, Persist and Load State
 */
@Service
class StateSrv(private val mapper: ObjectMapper) {

    /**
     * Persist [state] on new [step]
     */
    fun save(step: State.Step, state: State) {
        state.step = step

        println(mapper.writeValueAsString(state))
    }

}