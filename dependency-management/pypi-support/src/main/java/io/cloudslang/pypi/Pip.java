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
     * @param libraryName library to download
     * @param libraryVersion library version to download
     * @param downloadFolder folder library should be placed
     */
    PypiLibrary download(String libraryName, String libraryVersion, String downloadFolder);

    /**
     * Download any (seems like latest) version and all transitive dependencies to specified
     * folder
     * @param downloadFolder folder library should be placed
     * @param libraryName
     * @param downloadFolder
     */
    PypiLibrary download(String libraryName, String downloadFolder);
    /**
     * Download library by condition (<=, >=, ==) and all transitive dependencies to specified
     * folder
     * @param libraryName library to download
     * @param condition condition for library version
     * @param libraryVersion library version to download
     * @param downloadFolder folder library should be placed
     */
    PypiLibrary download(String libraryName, String condition, String libraryVersion, String downloadFolder);

    boolean isPipConfigured();

    boolean isPipRequirement(String requirement);

    String getLibraryNameFromRequirement(String requirement);
    String getRequirementCondition(String requirement);
    String getLibraryVersionFromRequirement(String requirement);
}
