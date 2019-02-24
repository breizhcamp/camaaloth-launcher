package org.breizhcamp.camaalothlauncher.controller

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Display views in BreizhCamp flavour
 */
@Profile("breizhcamp")
@Controller
class BreizhCampViewCtrl {

    @GetMapping("/")
    fun home() : String {
        return "redirect:010-talk-choice"
    }

    @GetMapping("/010-talk-choice")
    fun talkChoix() : String {
        return "breizhcamp/010-talk-choice"
    }

}