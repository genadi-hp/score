package io.cloudslang.pypi;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 14/07/2016.
 */
public interface Pip2MavenAdapter {
    boolean isPipConfigured();

    boolean isPipRequirement(String requirement);
    String getLibraryNameFromRequirement(String requirement);
    String getLibraryVersionFromRequirement(String requirement);

    void downloadDependencies(String libraryName, String libraryVersion, String downloadFolder, String mavenRepo);

    String pip2MavenArtifact(String libraryName, String libraryVersion);
}
