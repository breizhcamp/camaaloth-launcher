package org.breizhcamp.camaalothlauncher.dto

/**
 * DTO sent to GUI
 */
data class CopyFile (
        /** Dirname and filename, ex: BreizhJUG-La JVM/xxx.nut */
        val name: String,

        /** Size of file */
        val size: Long
)