package org.breizhcamp.camaalothlauncher.dto

import org.springframework.stereotype.Component
import java.nio.file.Path

/**
 * Stateful class containing current recording state
 */
@Component
class State {
    enum class Step {
        CHOICE, RECORD, EXPORT
    }

    var step = Step.CHOICE

    /** Selected user talk read from JSON file */
    var currentTalk: TalkSession? = null

    /** Path designing recording dir for [currentTalk] */
    var recordingPath: Path? = null

    /** After recording, list of file to convert into MP4 or to copy */
    var filesToExport: List<Path> = emptyList()

    /** Copy destination dir for ug */
    var copyingDir: String? = null

    fun previewDir() = recordingPath?.resolve("preview")
}