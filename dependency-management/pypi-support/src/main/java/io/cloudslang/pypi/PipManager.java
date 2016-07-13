package io.cloudslang.pypi;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class PipManager {
    private static final String LIBRARY_KEY = "-l=";
    private static final String DOWNLOAD_FOLDER_KEY = "-d=";
    private static final String IINDEX_URL_KEY = "-i=";

    public static void main(String[] args) {
        if((args.length < 2) || (args.length > 3)) {
            printUsage();
            return;
        }

        String downloadFolder = null;
        String libraryReference = null;
        String indexUrl = null;

        for (String arg : args) {
            if(arg.startsWith(LIBRARY_KEY)) {
                libraryReference = arg.substring(LIBRARY_KEY.length());
            } else if(arg.startsWith(DOWNLOAD_FOLDER_KEY)) {
                downloadFolder = arg.substring(DOWNLOAD_FOLDER_KEY.length());
            } else if(arg.startsWith(IINDEX_URL_KEY)) {
                downloadFolder = arg.substring(IINDEX_URL_KEY.length());
            }
        }

        if(libraryReference == null) {
            System.out.println("Library reference was not provided, use -l flag");
            printUsage();
            return;
        }

        if(downloadFolder == null) {
            System.out.println("Download folder was not provided, use -d flag");
            printUsage();
            return;
        }

        if(indexUrl == null) {
            System.out.println("Index url was not provided, use default https://pypi.python.org");
        }

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PyPyConfiguraton.class);
        Pip bean = context.getBean(Pip.class);
        if(!bean.isPipConfigured()) {
            System.out.println("Pip is not configured ");
        } else {
            bean.download(libraryReference, downloadFolder);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:\n-l library_name==library_version\n-d /path/to/download/folder\n-i index url");
        System.out.println("Example:\n-l=jenkinsapi==0.2.22 -d=c:\\Temp\\download\\");
    }
}
