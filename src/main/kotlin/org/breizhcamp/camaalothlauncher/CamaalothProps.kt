package org.breizhcamp.camaalothlauncher

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties for camaalooth
 */
@ConfigurationProperties("camaaloth")
class CamaalothProps {

    /** directory containing recording, each in sub-directory */
    var recordingDir = "videos"
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
        var scheduleFile = "src/test/resources/schedule.json"

        /** For test purpose, set the date to filter talk instead of current date */
        var overriddenDate: String? = null
    }

}
