package org.breizhcamp.camaalothlauncher

import org.breizhcamp.camaalothlauncher.services.recorder.RecorderType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Properties for camaalooth
 */
@ConfigurationProperties("camaaloth")
@ConstructorBinding
class CamaalothProps {

    /** directory containing recording, each in sub-directory */
    val recordingDir = "videos"

    /** Location of script called for copying file */
    val copyScript = "src/test/resources/copy-script.sh"

    /** Location of script called just before starting recording */
    val preRecordScript: String? = null

    /** Location of script called just after the end of recording */
    val postRecordScript: String? = null

    /** Do we show the systray or not */
    val systray = true

    /** Name of Akai in midi list */
    val akaiName: String? = null

    val recorder: RecorderType = RecorderType.OBS
    val nageru = Nageru()
    val obs = Obs()
    val breizhcamp = BreizhCamp()

    @ConstructorBinding
    class Nageru {
        /** bash start for nageru, params of bash script must be added to nageru params */
        val startScript = "src/test/resources/start-script.sh"
        /** location of nageru theme */
        val themeDir = "videos/theme"
    }

    @ConstructorBinding
    class Obs {
        val startCmd = listOf("obs")
        /** For now, we cannot change OBS record dir, so we create a symbolic link between this directory and the talk dir */
        val configuredRecordingDir: String = "videos/records"

        /** URL of websocket-obs */
        val wsUrl = "ws://localhost:4444"
        /** Password of websocket-obs */
        val wsPassword: String = "breizhcamp"
    }

    /** Configurations used when BreizhCamp flavour is on */
    @ConstructorBinding
    class BreizhCamp {
        /** Name of the room the launcher display talks in */
        val room = "Amphi C"

        /** Location of local shedule.json file */
        val scheduleFiles = listOf("src/test/resources/schedule.json")

        /** Destination directory of copied files, on remote server if specified */
        val copyDir = "/tmp"

        /** Server string (user@host) used to copy file */
        val copyServer: String? = null

        /**
         * Path to arduino port (ex: /dev/ttyUSB0).
         * If auto, tries to detect all serial port and take the first,
         * If no, disable entirely the Arduino connection
         */
        val arduinoPort: String = "auto"

        /** Number of ports (2 or 4) in portta switcher as they don't use the same infrared signals -_- */
        val nbPortsSwitcher: String = "2"

        /** For test purpose, set the date (YYYY-MM-DD) to filter talk instead of current date */
        val overriddenDate: String? = null

        /** For test purpose, set the time (HH:MM) to filter talk instead of current time */
        val overriddenTime: String? = null
    }
}
