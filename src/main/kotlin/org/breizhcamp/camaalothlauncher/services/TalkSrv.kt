package org.breizhcamp.camaalothlauncher.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.dto.TalkSession
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Manage Talk Data (zip...)
 */
@Service
class TalkSrv(private val objectMapper: ObjectMapper, private val props: CamaalothProps) {

    /**
     * @return the talk informations (and logo) read from the zip [zipFileName]
     */
    fun readTalkSession(zipFileName: String, withLogo: Boolean = true) : TalkSession {
        val zipFile = Paths.get(zipFileName)
        if (Files.notExists(zipFile)) throw FileNotFoundException("[$zipFileName] doesn't exists")

        ZipFile(zipFileName).use { zip ->
            val entries = zip.entries()

            var infos: TalkSession? = null
            var logo: ByteArray? = null

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()

                if (entry.name == "infos.json") {
                    infos = convertToTalk(zip, entry)
                }

                if (withLogo && entry.name == "logo.png") {
                    logo = zip.getInputStream(entry).use { it.readBytes() }
                }
            }

            if (infos == null) {
                throw FileNotFoundException("Cannot found [infos.json] in zip file [$zipFileName]")
            }

            infos.logo = logo
            return infos
        }
    }

    /** Define current talk session after reading zip [zipFile] */
    fun setCurrentTalkFromFile(zipFile: String, state: State) {
        val t = readTalkSession(zipFile, false)
        state.currentTalk = t

        val dirName = LocalDate.now().toString() + " - " + t.talk + " - " + t.speakers.joinToString(" -") { it.name }
        state.recordingPath = Paths.get(props.recordingDir, cleanForFilename(dirName)).toAbsolutePath()

        createCurrentTalkDirAndCopyInfos(zipFile, state)
        extractImagesToThemeDir(zipFile)
    }

    fun cleanForFilename(str: String) =
            StringUtils.stripAccents(str).replace(Regex("[\\\\/:*?\"<>|]"), "-").replace(Regex("[^A-Za-z0-9,\\-\\\\ ]"), "")

    /** Create directory for current dir */
    private fun createCurrentTalkDirAndCopyInfos(zipFile: String, state: State) {
        val recording = state.recordingPath ?: return
        val preview = state.previewDir() ?: return

        if (Files.notExists(preview)) {
            Files.createDirectories(preview)
        }

        val zip = Paths.get(zipFile)
        val dest = recording.resolve("infos.ug.zip")
        Files.copy(zip, dest, REPLACE_EXISTING, COPY_ATTRIBUTES)
    }

    /** Extract all png in [zipFile] into themes/images dir */
    private fun extractImagesToThemeDir(zipFile: String) {
        val imagesDir = Paths.get(props.nageru.themeDir, "images")
        val imgDirFile = imagesDir.toFile()

        if (Files.exists(imagesDir)) imgDirFile.deleteRecursively()
        Files.createDirectories(imagesDir)

        ZipFile(zipFile).use { zip ->
            val entries = zip.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()

                if (entry.name.endsWith(".png")) {
                    zip.getInputStream(entry).use { input ->
                        File(imgDirFile, entry.name).outputStream().use { out ->
                            input.copyTo(out)
                        }
                    }
                }
            }
        }
    }

    private fun convertToTalk(zip: ZipFile, entry: ZipEntry?): TalkSession {
        zip.getInputStream(entry).use {
            return objectMapper.readValue(it, TalkSession::class.java)
        }
    }

}