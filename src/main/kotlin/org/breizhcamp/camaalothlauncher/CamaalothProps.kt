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

    class Nageru {
        /** bash start for nageru, params of bash script must be added to nageru params */
        var startScript = "src/test/resources/start-script.sh"

        /** location of nageru theme */
        var themeDir = "videos/theme"
    }

}