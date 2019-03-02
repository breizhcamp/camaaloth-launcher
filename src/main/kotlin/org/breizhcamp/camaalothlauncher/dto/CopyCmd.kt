package org.breizhcamp.camaalothlauncher.dto

import java.nio.file.Path

/**
 * DTO for copying file
 */
data class CopyCmd (

        /** Source file on local path */
        val src: Path,

        /** Destination path that will be 2nd parameter of copy script */
        val dest: Path,

        /** Logging directory for script output */
        val logDir: Path

        )