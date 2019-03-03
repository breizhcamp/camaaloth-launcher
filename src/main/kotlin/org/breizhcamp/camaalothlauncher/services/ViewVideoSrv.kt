package org.breizhcamp.camaalothlauncher.services

import org.springframework.stereotype.Service
import java.nio.file.Path

/**
 * Service used for playing video file
 */
@Service
class ViewVideoSrv {

    /**
     * Start video player to play [video] and define [rundir] as current dir
     */
    fun start(video: Path, rundir: Path) {
        val cmd = listOf("vlc", video.toAbsolutePath().toString())
        LongCmdRunner("vlc", cmd, rundir).start()
    }

}