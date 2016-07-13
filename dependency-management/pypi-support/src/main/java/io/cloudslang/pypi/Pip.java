package io.cloudslang.pypi;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public interface Pip {
    /**
     * Download specified library and all transitive dependencies to specified
     * folder
     * @param libraryReference library to download
     * @param downloadFolder folder library should be placed
     */
    void download(String libraryReference, String downloadFolder);

    boolean isPipConfigured();
}
