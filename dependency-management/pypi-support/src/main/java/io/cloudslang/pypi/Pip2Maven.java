package io.cloudslang.pypi;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 14/07/2016.
 */
public interface Pip2Maven {
    /**
     * Converts provided library with all its dependency libraries to maven artifacts
     * @param libraryName pip library name
     * @param libraryVersion pip library version
     * @param pipLibraryFolder pip containing folder
     * @param mavenRepoFolder maven repo folder
     */
    void pip2Maven(String libraryName, String libraryVersion, String pipLibraryFolder, String mavenRepoFolder);
}
