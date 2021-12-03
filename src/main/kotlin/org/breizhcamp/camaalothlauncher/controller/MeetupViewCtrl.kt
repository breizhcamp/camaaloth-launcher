package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.CopyCmd
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.dto.State.Step.*
import org.breizhcamp.camaalothlauncher.services.CopyThread
import org.breizhcamp.camaalothlauncher.services.StateSrv
import org.breizhcamp.camaalothlauncher.services.TalkSrv
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.nio.file.Paths

/**
 * Display views in meetup flavour
 */
@Profile("!breizhcamp")
@Controller
class MeetupViewCtrl(private val talkSrv: TalkSrv, private val state: State, private val stateSrv: StateSrv,
                     private val copyThread: CopyThread) {

    @GetMapping("/")
    fun home() : String {
        return "meetup/000-home"
    }

    @GetMapping("/010-talk-choice")
    fun talkChoix() : String {
        stateSrv.save(CHOICE, state)
        return "meetup/010-talk-choice"
    }

    @GetMapping("/020-record")
    fun live(@RequestParam file: String?, model: Model) : String {
        file?.let { talkSrv.setCurrentTalkFromFile(it, state) }
        val talk = state.currentTalk ?: return "redirect:010-talk-choice"

        model.addAttribute("talk", talk)
        model.addAttribute("forceExport", false)

        stateSrv.save(RECORD, state)
        return "common/020-record"
    }

    @GetMapping("/030-export")
    fun export(model: Model) : String {
        val talk = state.currentTalk ?: return "redirect:010-talk-choice"

        stateSrv.save(EXPORT, state)
        model.addAttribute("talk", talk)
        return "meetup/030-export"
    }

    @GetMapping("/050-copy")
    fun copy() : String {
        val src = state.recordingPath ?: return "redirect:010-talk-choice"
        val dest = state.copyingDir ?: return "redirect:040-export"
        state.filesToExport = emptyList()

        //let's copy exported file into selected destination
        val fullDest = Paths.get(dest, "videos")
        copyThread.addFileToCopy(CopyCmd(src.resolve("infos.ug.zip"), fullDest, src))
        copyThread.addFileToCopy(CopyCmd(src.resolve("export.mp4"), fullDest, src))


        return "common/050-copy"
    }
}