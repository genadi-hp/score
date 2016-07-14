package io.cloudslang.pypi;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class PipManager {
    private static final String LIBRARY_KEY = "-l=";
    private static final String DOWNLOAD_FOLDER_KEY = "-d=";
    private static final String MAVEN_REPO_FOLDER_KEY = "-m=";

    public static void main(String[] args) {
        if(args.length < 2) {
            printUsage();
            return;
        }

        String mavenRepoFolder = null;
        String downloadFolder = null;
        String libraryRequirement = null;

        for (String arg : args) {
            if(arg.startsWith(LIBRARY_KEY)) {
                libraryRequirement = arg.substring(LIBRARY_KEY.length());
            } else if(arg.startsWith(DOWNLOAD_FOLDER_KEY)) {
                downloadFolder = arg.substring(DOWNLOAD_FOLDER_KEY.length());
            } else if(arg.startsWith(MAVEN_REPO_FOLDER_KEY)) {
                mavenRepoFolder = arg.substring(MAVEN_REPO_FOLDER_KEY.length());
            }
        }

        if(libraryRequirement == null) {
            System.out.println("Library reference was not provided, use -l flag");
            printUsage();
            return;
        }

        if(downloadFolder == null) {
            System.out.println("Download folder should be provided, use -d");
            printUsage();
            return;
        }

        if(mavenRepoFolder == null) {
            System.out.println("Maven repo folder is not provided, use -m. Skipping transformation to maven");
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PyPyConfiguraton.class);
        Pip pip = context.getBean(Pip.class);
        if(!pip.isPipConfigured()) {
            System.out.println("Pip is not configured ");
        } else {
            if(pip.isPipRequirement(libraryRequirement)) {
                String libraryName = pip.getLibraryNameFromRequirement(libraryRequirement);
                String libraryVersion = pip.getLibraryVersionFromRequirement(libraryRequirement);

                if(mavenRepoFolder != null) {
                    Pip2MavenAdapter pip2MavenAdapter = context.getBean(Pip2MavenAdapter.class);
                    pip2MavenAdapter.downloadDependencies(libraryName, libraryVersion, downloadFolder, mavenRepoFolder);
                } else {
                    pip.download(libraryName, libraryVersion, downloadFolder);
                }
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage:\n-l library_name==library_version\n-d /path/to/download/folder\n-m maven repo folder");
        System.out.println("Example:\n-l=jenkinsapi==0.2.22 -d=c:\\Temp\\download\\");
    }
}
