package org.breizhcamp.camaalothlauncher.dto

import java.time.Duration

/**
 * Data sent when starting export
 */
data class ExportStart (
        /** Duration sum of video files to export */
        val filesLength: Duration,

        /** Size sum of files to export */
        val filesSize: Long
)