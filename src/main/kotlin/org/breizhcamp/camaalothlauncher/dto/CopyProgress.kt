package org.breizhcamp.camaalothlauncher.dto

/**
 * Sent to update of current copy progress
 */
data class CopyProgress (
        /** Current coping file, null if no file is coping */
        var current: CopyFile?,

        /** Number of bytes copied on destination */
        var copied: Long = 0L,

        /** Transfer rate in byte/sec */
        var speed: Long = 0L,

        /** Total size of files waiting in queue */
        var waitingSize: Long = 0L
)