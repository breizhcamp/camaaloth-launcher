package org.breizhcamp.camaalothlauncher.config

import net.twasi.obsremotejava.OBSRemoteController
import org.breizhcamp.camaalothlauncher.CamaalothProps
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObsConfig(private val props: CamaalothProps) {

    @Bean
    fun obsController() = OBSRemoteController(props.obs.wsUrl, false, props.obs.wsPassword, false)

}