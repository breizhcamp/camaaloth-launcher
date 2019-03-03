package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.CopyCmd
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.dto.State.Step.*
import org.breizhcamp.camaalothlauncher.services.CopyThread
import org.breizhcamp.camaalothlauncher.services.StateSrv
import org.breizhcamp.camaalothlauncher.services.TalkConfSrv
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalTime

/**
 * Display views in BreizhCamp flavour
 */
@Profile("breizhcamp")
@Controller
class BreizhCampViewCtrl(private val props: CamaalothProps, private val talkConfSrv: TalkConfSrv,
                         private val state: State, private val stateSrv: StateSrv, private val copyThread: CopyThread) {

    @GetMapping("/")
    fun home() : String {
        return "redirect:010-talk-choice"
    }

    @GetMapping("/010-talk-choice")
    fun talkChoix(model: Model) : String {
        val talks = talkConfSrv.listTalksInCurrentRoom()

        val overriddenDate = props.breizhcamp.overriddenDate
        val overriddenTime = props.breizhcamp.overriddenTime
        val date = overriddenDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        val time = overriddenTime?.let { LocalTime.parse(overriddenTime) } ?: LocalTime.now().minusHours(1)

        model.addAttribute("talks", talks.filter { it.date == date && time < it.startTime })
        model.addAttribute("curDate", date)
        model.addAttribute("room", props.breizhcamp.room)

        stateSrv.save(CHOICE, state)
        return "breizhcamp/010-talk-choice"
    }

    @GetMapping("/020-preview")
    fun preview(@RequestParam id: Int?, model: Model) : String {
        id?.let { talkConfSrv.setCurrentTalk(it, state) }

        val talk = state.currentTalk ?: return "redirect:010-talk-choice"
        model.addAttribute("talk", talk)

        stateSrv.save(PREVIEW, state)
        return "common/020-preview"
    }

    @GetMapping("/030-live")
    fun live(model: Model) : String {
        val talk = state.currentTalk ?: return "redirect:010-talk-choice"
        model.addAttribute("talk", talk)
        model.addAttribute("forceExport", true)

        stateSrv.save(LIVE, state)
        return "common/030-live"
    }

    @GetMapping("/040-export")
    fun export() : String {
        //no export in BreizhCamp configuration, go direct into copy step
        return "redirect:050-copy"
    }

    @GetMapping("/050-copy")
    fun copy() : String {
        val recordingPath = state.recordingPath ?: return "redirect:010-talk-choice"
        val dest = Paths.get(props.breizhcamp.copyDir)

        //add all exported file into copy queue
        state.filesToExport.forEach { f ->
            copyThread.addFileToCopy(CopyCmd(recordingPath.resolve(f), dest, recordingPath, props.breizhcamp.copyServer))
        }
        state.filesToExport = emptyList()

        return "common/050-copy"
    }

}