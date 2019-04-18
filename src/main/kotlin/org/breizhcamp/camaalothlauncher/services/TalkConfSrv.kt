package org.breizhcamp.camaalothlauncher.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.Speaker
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.dto.TalkConf
import org.breizhcamp.camaalothlauncher.dto.TalkSession
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Manage TalkConf (read...)
 */
@Service
class TalkConfSrv(private val objectMapper: ObjectMapper, private val props: CamaalothProps, private val talkSrv: TalkSrv) {

    private val dayFormat = DateTimeFormatter.ofPattern("dd")
    private val timeFormat = DateTimeFormatter.ofPattern("HH-mm")

    /** @return All talks for the room specified in configuration */
    fun listTalksInCurrentRoom(): List<TalkSession> {
        return loadTalks()
                .filter { it.venue == props.breizhcamp.room }
                .sortedBy { it.eventStart }
                .map { talkConfToSession(it) }
    }

    /**
     * Define current talk from this id
     * TODO: check if we can do something better with [TalkSrv.setCurrentTalkFromFile]
     *
     * @param id Id of the talk to define
     * @param state State containing the current talk if found
     */
    fun setCurrentTalk(id: Int, state: State) {
        val t = getTalk(id) ?: return
        state.currentTalk = talkConfToSession(t)

        val recordingPath = buildVideoDirName(t)
        state.recordingPath = Paths.get(props.recordingDir, recordingPath).toAbsolutePath()

        val preview = state.previewDir() ?: return
        if (Files.notExists(preview)) {
            Files.createDirectories(preview)
        }
    }

    /** Load all talks for schedules files */
    private fun loadTalks(): List<TalkConf> {
        return props.breizhcamp.scheduleFiles.flatMap { file ->
            objectMapper.readValue<List<TalkConf>>(File(file))
        }
    }

    /** @return talk with [id] or null */
    private fun getTalk(id: Int): TalkConf? {
        if (id == 0) {
            return TalkConf(0, "Captation à la volée", "Captation sans association à un talk", props.breizhcamp.room,
                    ZonedDateTime.now(), ZonedDateTime.now().plusHours(1))
        }

        return loadTalks().firstOrNull { it.id == id }
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

    /**
     * @return the directory name for a specific talk at the same format of the video uploader
     */
    private fun buildVideoDirName(talk: TalkConf): String {
        val name = talkSrv.cleanForFilename(talk.name)
        val speakers = talkSrv.cleanForFilename(talk.speakers)

        return (dayFormat.format(talk.eventStart) + "." + talk.venue + "." + timeFormat.format(talk.eventStart)
                + " - " + name + " (" + speakers + ") - " + talk.id)
    }

}