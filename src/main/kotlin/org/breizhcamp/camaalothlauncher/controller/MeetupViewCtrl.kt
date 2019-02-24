package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.services.TalkSrv
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Display views in meetup flavour
 */
@Profile("!breizhcamp")
@Controller
class MeetupViewCtrl(private val talkSrv: TalkSrv, private val state: State) {

    @GetMapping("/")
    fun home() : String {
        return "meetup/000-home"
    }

    @GetMapping("/010-talk-choice")
    fun talkChoix() : String {
        return "meetup/010-talk-choice"
    }

    @GetMapping("/020-preview")
    fun preview(@RequestParam file: String?, model: Model) : String {
        file?.let { talkSrv.setCurrentTalkFromFile(it, state) }

        val talk = state.currentTalk ?: return "redirect:010-talk-choice"
        model.addAttribute("talk", talk)
        return "meetup/020-preview"
    }

    @GetMapping("/030-live")
    fun live(model: Model) : String {
        val talk = state.currentTalk ?: return "redirect:010-talk-choice"

        model.addAttribute("talk", talk)
        return "meetup/030-live"
    }

    @GetMapping("/040-export")
    fun export(model: Model) : String {
        val talk = state.currentTalk ?: return "redirect:010-talk-choice"

        model.addAttribute("talk", talk)
        return "meetup/040-export"
    }
}