package io.cloudslang.pypi;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 14/07/2016.
 */
public class Pip2MavenAdapterImpl implements Pip2MavenAdapter {
    @Autowired
    private Pip pip;

    @Autowired
    private Pip2MavenTransformer pip2MavenTransformer;

    @Override
    public boolean isPipConfigured() {
        return pip.isPipConfigured();
    }

    @Override
    public boolean isPipRequirement(String requirement) {
        return pip.isPipRequirement(requirement);
    }

    @Override
    public String getLibraryNameFromRequirement(String requirement) {
        return pip.getLibraryNameFromRequirement(requirement);
    }

    @Override
    public String getLibraryVersionFromRequirement(String requirement) {
        return pip.getLibraryVersionFromRequirement(requirement);
    }

    @Override
    public void downloadDependencies(String libraryName, String libraryVersion, String downloadFolder, String mavenRepo) {
        pip.download(libraryName, libraryVersion, downloadFolder);
        pip2MavenTransformer.pip2Maven(libraryName, libraryVersion, downloadFolder, mavenRepo);
    }

    @Override
    public String pip2MavenArtifact(String libraryName, String libraryVersion) {
        return pip2MavenTransformer.pip2MavenArtifact(libraryName, libraryVersion);
    }
}
