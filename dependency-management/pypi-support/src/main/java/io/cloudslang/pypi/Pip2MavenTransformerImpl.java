package io.cloudslang.pypi;

import io.cloudslang.pypi.transformers.PackageTransformer;
import org.apache.log4j.Logger;
import org.python.google.common.collect.Sets;

import java.io.*;
import java.text.MessageFormat;
import java.util.Set;

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
    public void pip2Maven(String libraryName, String libraryVersion, String pipLibraryFolder, String mavenRepoFolder) {
        File pipLibraryFolderFile = new File(pipLibraryFolder);
        File [] libraries = pipLibraryFolderFile.listFiles(new FileFilter() {
            public boolean accept(File pathname) {return pathname.getAbsolutePath().toLowerCase().endsWith(PackageTransformer.ZIP_EXTENSION);}
        });
        File mainLibrary = null;
        Set<File> dependencies = Sets.newHashSet();
        for (File library : libraries) {
            if(library.getName().toLowerCase().startsWith(libraryName.toLowerCase())) {
                if(!library.getName().toLowerCase().contains(libraryVersion.toLowerCase())) {
                    throw new RuntimeException("Found wrong version:expected [" + libraryVersion + "] while got [" + library.getAbsolutePath() + "]");
                }
                if(mainLibrary != null) {
                    throw new RuntimeException("Failed to identify main library for [" +
                            libraryName + "]-[" + libraryVersion + "], got at least two: [" +
                            mainLibrary.getAbsolutePath() + "] and [" + library.getAbsolutePath() + "]");
                }
                mainLibrary = library;
            } else {
                dependencies.add(library);
            }
        }

        if(mainLibrary == null) {
            throw new RuntimeException("Failed to identify library for [" + libraryName + "]-[" + libraryVersion + "]");
        }

        deployLibrary(mavenRepoFolder, libraryName, libraryVersion, mainLibrary, dependencies);
    }

    @Override
    public String pip2MavenArtifact(String libraryName, String libraryVersion) {
        return PYTHON_GROUP + ":" + libraryName + ":" + libraryVersion + ":" + ZIP_TYPE;
    }

    private void deployLibrary(String mavenRepoFolder,
                               String libraryName, String libraryVersion, File mainLibrary,
                               Set<File> dependencies) {
        String libPomStr = MessageFormat.format(TEMPLATE_START_POM, libraryName, libraryVersion);
        StringBuilder libPom = new StringBuilder(libPomStr);
        if((dependencies != null) && !dependencies.isEmpty()) {
            libPom.append("    <dependencies>\n");
            for (File dependency : dependencies) {
                deployDependency(mavenRepoFolder, dependency);
                String [] av = extractAV(dependency);
                libPom.append(MessageFormat.format(TEMPLATE_DEPENDENCY, av[0], av[1]));
            }
            libPom.append("    </dependencies>\n");
        }
        libPom.append(TEMPLATE_END_POM);

        createArtifact(mavenRepoFolder, mainLibrary, new String[]{libraryName, libraryVersion}, libPom.toString());
    }

    private void deployDependency(String mavenRepoFolder, File lib) {
        String [] libAV = extractAV(lib);
        String libPomStr = MessageFormat.format(TEMPLATE_START_POM + TEMPLATE_END_POM, libAV[0], libAV[1]);

        createArtifact(mavenRepoFolder, lib, libAV, libPomStr);
    }

    private void createArtifact(String mavenRepoFolder, File lib, String[] libAV, String libPomStr) {
        logger.info("Creating artifact for [" + libAV[0] + "]-[" + libAV[1] + "]");
        String artifactName = libAV[0] + "-" + libAV[1];
        File pythonGroupFolder = new File(new File(mavenRepoFolder), PYTHON_GROUP);
        pythonGroupFolder.mkdirs();
        File artifactFolder = new File(new File(pythonGroupFolder, libAV[0]), libAV[1]);
        artifactFolder.mkdirs();

        File artifactFile = new File(artifactFolder, artifactName + PackageTransformer.ZIP_EXTENSION);
        logger.info("Moving [" + lib.getAbsolutePath() + "] to [" + artifactFile.getAbsolutePath() + "]");
        lib.renameTo(artifactFile);

        File pomFile = new File(artifactFolder, artifactName + POM_EXTENSION);

        logger.info("Creating pom for [" + libAV[0] + "]-[" + libAV[1] + "] --> [" + pomFile.getAbsolutePath() + "]");
        try (OutputStream os = new FileOutputStream(pomFile)) {
            os.write(libPomStr.getBytes());
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create for [" + artifactName + "] pom file [" + pomFile.getAbsolutePath() + "]", e);
        }
    }

    String [] extractAV(File lib) {
        String path = lib.getName();
        String dependencyStr = path.substring(0, path.length() - PackageTransformer.ZIP_EXTENSION.length());
        return new String[]{dependencyStr.substring(0, dependencyStr.indexOf('-')).toLowerCase(),
                dependencyStr.substring(dependencyStr.indexOf('-') + 1).toLowerCase()};
    }
}
