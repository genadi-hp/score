package io.cloudslang.pypi;

import io.cloudslang.pypi.transformers.PackageTransformer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 14/07/2016.
 */
public class Pip2MavenTransformerImpl implements Pip2MavenTransformer {
    private static final Logger logger = Logger.getLogger(PackageTransformer.class);

    public static final String PYTHON_GROUP = "python";
    public static final String ZIP_TYPE = "zip";

    private static final String POM_EXTENSION = ".pom";

    private static final String TEMPLATE_START_POM = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "    <groupId>" + PYTHON_GROUP + "</groupId>\n" +
            "    <artifactId>{0}</artifactId>\n" +
            "    <version>{1}</version>\n";

    private static final String TEMPLATE_DEPENDENCY =
            "        <dependency>\n" +
            "            <groupId>" + PYTHON_GROUP + "</groupId>\n" +
            "            <artifactId>{0}</artifactId>\n" +
            "            <version>{1}</version>\n" +
            "            <type>" + ZIP_TYPE + "</type>\n" +
            "        </dependency>\n";

    private static final String TEMPLATE_END_POM = "</project>\n";

    @Override
    public String pip2MavenArtifact(String libraryName, String libraryVersion) {
        return PYTHON_GROUP + ":" + libraryName + ":" + libraryVersion + ":" + ZIP_TYPE;
    }

    @Override
    public void pip2Maven(PypiLibrary pypiLibrary, String mavenRepoFolder) {
        String libPomStr = MessageFormat.format(TEMPLATE_START_POM, pypiLibrary.getName(), pypiLibrary.getVersion());
        StringBuilder libPom = new StringBuilder(libPomStr);
        if(!pypiLibrary.getLibraryDependencies().isEmpty()) {
            libPom.append("    <dependencies>\n");
            for (PypiLibrary dependency : pypiLibrary.getLibraryDependencies()) {
                pip2Maven(dependency, mavenRepoFolder);
                libPom.append(MessageFormat.format(TEMPLATE_DEPENDENCY, dependency.getName(), dependency.getVersion()));
            }
            libPom.append("    </dependencies>\n");
        }
        libPom.append(TEMPLATE_END_POM);

        createArtifact(pypiLibrary, libPom.toString(), mavenRepoFolder);
    }

    private void createArtifact(PypiLibrary pypiLibrary, String libPomStr, String mavenRepoFolder) {
        logger.info("Creating artifact for " + pypiLibrary.getDescription());
        String artifactName = pypiLibrary.getName() + "-" + pypiLibrary.getVersion();
        File pythonGroupFolder = new File(new File(mavenRepoFolder), PYTHON_GROUP);
        pythonGroupFolder.mkdirs();
        File artifactFolder = new File(new File(pythonGroupFolder, pypiLibrary.getName()), pypiLibrary.getVersion());
        artifactFolder.mkdirs();

        File artifactFile = new File(artifactFolder, artifactName + PackageTransformer.ZIP_EXTENSION);
        logger.info("Moving [" + pypiLibrary.getFile().getAbsolutePath() + "] to [" + artifactFile.getAbsolutePath() + "]");
        pypiLibrary.getFile().renameTo(artifactFile);

        File pomFile = new File(artifactFolder, artifactName + POM_EXTENSION);

        logger.info("Creating pom for " + pypiLibrary.getDescription() + " --> [" + pomFile.getAbsolutePath() + "]");
        try (OutputStream os = new FileOutputStream(pomFile)) {
            os.write(libPomStr.getBytes());
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create for [" + artifactName + "] pom file [" + pomFile.getAbsolutePath() + "]", e);
        }
    }
}
