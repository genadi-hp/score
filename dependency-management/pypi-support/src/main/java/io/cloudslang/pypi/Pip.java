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
    void download(String libraryName, String libraryVersion, String downloadFolder);

    boolean isPipConfigured();

    boolean isPipRequirement(String requirement);

    String getLibraryNameFromRequirement(String requirement);
    String getLibraryVersionFromRequirement(String requirement);
}
