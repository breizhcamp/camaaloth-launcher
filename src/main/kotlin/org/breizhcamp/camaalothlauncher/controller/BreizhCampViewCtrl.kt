package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.services.TalkConfSrv
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate
import java.time.LocalTime

/**
 * Display views in BreizhCamp flavour
 */
@Profile("breizhcamp")
@Controller
class BreizhCampViewCtrl(private val props: CamaalothProps, private val talkConfSrv: TalkConfSrv) {

    @GetMapping("/")
    fun home() : String {
        return "redirect:010-talk-choice"
    }

    @GetMapping("/010-talk-choice")
    fun talkChoix(model: Model) : String {
        val talks = talkConfSrv.listTalksInCurrentRoom()

        val overriddenDate = props.breizhcamp.overriddenDate
        val date = overriddenDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        val time = overriddenDate?.let { LocalTime.parse("12:50") } ?: LocalTime.now().minusHours(1)

        model.addAttribute("talks", talks.filter { it.date == date && time < it.startTime })
        model.addAttribute("curDate", date)
        model.addAttribute("room", props.breizhcamp.room)
        return "breizhcamp/010-talk-choice"
    }

}