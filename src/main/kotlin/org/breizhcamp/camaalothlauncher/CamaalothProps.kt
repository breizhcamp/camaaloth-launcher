package org.breizhcamp.camaalothlauncher

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for camaalooth
 */
@ConfigurationProperties("camaaloth")
class CamaalothProps {

    /** directory containing recording, each in sub-directory */
    var recordingDir = "videos"

    /** Location of script called for copying file */
    var copyScript = "src/test/resources/copy-script.sh"

    val nageru = Nageru()
    val breizhcamp = BreizhCamp()

    class Nageru {
        /** bash start for nageru, params of bash script must be added to nageru params */
        var startScript = "src/test/resources/start-script.sh"
        /** location of nageru theme */
        var themeDir = "videos/theme"
    }

    /** Configurations used when BreizhCamp flavour is on */
    class BreizhCamp {
        /** Name of the room the launcher display talks in */
        var room = "Amphi C"

        /** Location of local shedule.json file */
        var scheduleFiles = listOf("src/test/resources/schedule.json")

        /** Destination directory of copied files, on remote server if specified */
        var copyDir = "/tmp"

        /** Server string (user@host) used to copy file */
        var copyServer: String? = null

        /** Path to arduino port (ex: /dev/ttyUSB0), if auto, tries to detect all serial port and take the first */
        var arduinoPort: String = "auto"

        /** For test purpose, set the date (YYYY-MM-DD) to filter talk instead of current date */
        var overriddenDate: String? = null

        /** For test purpose, set the time (HH:MM) to filter talk instead of current time */
        var overriddenTime: String? = null
    }

}
