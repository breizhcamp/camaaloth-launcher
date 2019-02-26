package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.dto.State.Step.CHOICE
import org.breizhcamp.camaalothlauncher.dto.State.Step.PREVIEW
import org.breizhcamp.camaalothlauncher.services.StateSrv
import org.breizhcamp.camaalothlauncher.services.TalkConfSrv
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate
import java.time.LocalTime

/**
 * Display views in BreizhCamp flavour
 */
@Profile("breizhcamp")
@Controller
class BreizhCampViewCtrl(private val props: CamaalothProps, private val talkConfSrv: TalkConfSrv,
                         private val state: State, private val stateSrv: StateSrv) {

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

}