package org.breizhcamp.camaalothlauncher.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

/**
 * DTO for schedule.json talk
 */
data class TalkConf (
        val id: Int,
        val name: String,
        val speakers: String,
        val venue: String,
        @JsonProperty("event_start")
        val eventStart: ZonedDateTime,
        @JsonProperty("event_end")
        val eventEnd: ZonedDateTime
)