package org.breizhcamp.camaalothlauncher.dto

import java.time.LocalDate
import java.time.LocalTime

/**
 * Infos for a talk session
 */
data class TalkSession (
        /** Session id, used for BreizhCamp for now */
        var id: String?,

        /** Talk name */
        var talk: String,
        var speakers: List<Speaker>,

        var date: LocalDate,
        var startTime: LocalTime?,
        var endTime: LocalTime?,

        /** Meetup name (ex: BreizhJUG) */
        var name: String,
        /** Meetup logo */
        var logo: ByteArray? = null

) {
        fun startDate() = date.atTime(startTime)
        fun endDate() = date.atTime(endTime)
}