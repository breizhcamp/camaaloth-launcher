package org.breizhcamp.camaalothlauncher.services

/**
 * Hook for running before and after Nageru execution
 */
interface NageruHook {

    /**
     * Hook executed before running Nageru
     * @param preview true if Nageru is started on preview
     */
    fun preNageru(preview: Boolean) { }

    /**
     * Hook executed after Nageru exited
     * @param preview true if Nageru is started on preview
     */
    fun postNageru(preview: Boolean) { }
}