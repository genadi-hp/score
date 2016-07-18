package io.cloudslang.pypi;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 14/07/2016.
 */
public interface Pip2MavenTransformer {
    /**
     * Converts provided library with all its dependency libraries to maven artifacts
     * @param pypiLibrary pip library
     * @param mavenRepoFolder maven repo folder
     */
    void pip2Maven(PypiLibrary pypiLibrary, String mavenRepoFolder);

    String pip2MavenArtifact(String libraryName, String libraryVersion);
}
