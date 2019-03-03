package org.breizhcamp.camaalothlauncher.dto

import java.nio.file.Files
import java.nio.file.Path

/**
 * DTO for copying file
 */
data class CopyCmd (

        /** Source file on local path */
        val src: Path,

        /** Destination file that will be 2nd parameter of copy script */
        val destFile: Path,

        /** Logging directory for script output */
        val logDir: Path,

        /** Destination server, passed as third argument */
        val destServer: String? = null,

        /** Size of source file */
        val fileSize: Long = if (Files.exists(src)) Files.size(src) else 0L,

        /** Number of bytes already copied on destination */
        var copied: Long = 0L

)