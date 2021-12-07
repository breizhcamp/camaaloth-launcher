package org.breizhcamp.camaalothlauncher.controller

import org.breizhcamp.camaalothlauncher.dto.FileMeta
import org.breizhcamp.camaalothlauncher.dto.Partition
import org.breizhcamp.camaalothlauncher.dto.State
import org.breizhcamp.camaalothlauncher.dto.TalkSession
import org.breizhcamp.camaalothlauncher.services.FilesSrv
import org.breizhcamp.camaalothlauncher.services.TalkSrv
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Handle partitions, files, etc...
 */
@RestController @RequestMapping("/files")
class FilesCtrl(private val filesSrv: FilesSrv, private val talkSrv: TalkSrv, private val state: State) {

    /**
     * @return List of all *.ug.zip files on removable devices
     */
    @GetMapping
    fun listAllUgFiles() : List<FileMeta> {
        val partitions = filesSrv.readPartitions()
        return filesSrv.getFilesFromPartitions(partitions, "*.ug.zip")
    }

    @GetMapping("/removable")
    fun listRemovableDisk(): List<Partition> {
        val partitions = filesSrv.readPartitions(true)
        val recordingDir = state.recordingPath ?: return partitions
        return partitions + listOf(filesSrv.getPartitionFromDir(recordingDir.parent.parent))
    }

    @GetMapping("/talk")
    fun readTalkSession(@RequestParam file: String) : TalkSession {
        return talkSrv.readTalkSession(file)
    }
}