package org.breizhcamp.camaalothlauncher.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.Speaker
import org.breizhcamp.camaalothlauncher.dto.TalkConf
import org.breizhcamp.camaalothlauncher.dto.TalkSession
import org.springframework.stereotype.Service
import java.io.File

/**
 * Manage TalkConf (read...)
 */
@Service
class TalkConfSrv(private val objectMapper: ObjectMapper, private val props: CamaalothProps) {

    /** @return All talks for the room specified in configuration */
    fun listTalksInCurrentRoom(): List<TalkSession> {
        val talks: List<TalkConf> = objectMapper.readValue(File(props.breizhcamp.scheduleFile))

        return talks
                .filter { it.venue == props.breizhcamp.room }
                .sortedBy { it.eventStart }
                .map { talkConfToSession(it) }
    }

    private fun talkConfToSession(conf: TalkConf): TalkSession {
        val speakers = conf.speakers.split(",").map { Speaker(it.trimEnd()) }

        return TalkSession(
                id = conf.id.toString(),
                talk = conf.name,
                speakers = speakers,
                date = conf.eventStart.toLocalDate(),
                startTime = conf.eventStart.toLocalTime(),
                endTime = conf.eventEnd.toLocalTime(),
                name = "BreizhCamp"
        )
    }

}