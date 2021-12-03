package org.breizhcamp.camaalothlauncher.services.recorder

/**
 * Hook for running before and after recording software execution
 */
interface RecorderHook {

    /**
     * Hook executed before running recorder
     * @param preview true if recorder is started on preview
     */
    fun preRecord(preview: Boolean) { }

    /**
     * Hook executed after recorder exited
     * @param preview true if recorder is started on preview
     */
    fun postRecord(preview: Boolean) { }
}