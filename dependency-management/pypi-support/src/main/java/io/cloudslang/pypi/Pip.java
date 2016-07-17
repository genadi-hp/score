package io.cloudslang.pypi;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public interface Pip {
    String PYPI_REPOSITORY_PROPERTY = "cloudslang.pypi.index.url";
    String DEFAULT_PYPI_REPOSITORY = "https://pypi.python.org/simple";
    String PYTHON_LIB = "python.path";

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
