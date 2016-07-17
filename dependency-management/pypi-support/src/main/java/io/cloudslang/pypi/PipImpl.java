package io.cloudslang.pypi;

import io.cloudslang.pypi.transformers.PackageTransformer;
import org.apache.log4j.Logger;
import org.python.google.common.collect.Maps;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class PipImpl implements Pip {
    private static final Logger logger = Logger.getLogger(PipImpl.class);

    private static final String NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING = "No PyPi support. '" + PYTHON_LIB + "' system property is missing!!!";
    private static final String STRREQUIREMENT_EQ = "==";

    @Value("#{ systemProperties['" + PYPI_REPOSITORY_PROPERTY + "']}")
    private String pypiUrl;

    @Value("#{systemProperties['" + PYTHON_LIB + "']}")
    private String pythonLib;

    @Autowired
    private List<PackageTransformer> packageTransformers;

    private final Map<String, PackageTransformer> packageTransformerMap = Maps.newHashMap();

    private PythonInterpreter pipExecutor;

    @PostConstruct
    private void initPip() {
        if((pythonLib != null) && (pypiUrl != null)) {
            logger.info("Starting pip support configuration process");

            pipExecutor = new PythonInterpreter();
            logger.info("Pip package loaded to the python interpreter");

            for (PackageTransformer packageTransformer : packageTransformers) {
                packageTransformerMap.put(packageTransformer.getSupportedFormat().toLowerCase(), packageTransformer);
            }
        } else {
            logger.info(NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING);
        }
    }

    @Override
    public void download(String libraryName, String libraryVersion, String downloadFolder) {
        String libraryRequirement = libraryName + STRREQUIREMENT_EQ + libraryVersion;
        downloadFolder = downloadFolder.replace("\\", "/");
        if(downloadFolder.endsWith(File.separator)) {
            downloadFolder = downloadFolder.substring(0, downloadFolder.length() - 1);
        }

        if(isPipConfigured()) {
            logger.info("Downloading library [" + libraryRequirement + "] to folder [" + downloadFolder + "]");
            String downloadScript = "import pip\npip.main(['download', '-i', '" + pypiUrl + "', '-d', '" + downloadFolder + "','" + libraryRequirement + "'])";
            logger.info("Executing download script [" + downloadScript + "]");
            pipExecutor.exec(downloadScript);
        } else {
            logger.error(NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING);
        }

        transformLibrariesToZip(downloadFolder);
    }

    private void transformLibrariesToZip(String downloadFolder) {
        File[] libraries = new File(downloadFolder).listFiles();
        if(libraries != null) {
            for(File library: libraries) {
                String absolutePath = library.getAbsolutePath();
                String extension = absolutePath.substring(absolutePath.lastIndexOf('.'));
                PackageTransformer packageTransformer = packageTransformerMap.get(extension.toLowerCase());
                if(packageTransformer != null) {
                    packageTransformer.transform(absolutePath);
                }
            }
        }
    }

    @Override
    public boolean isPipConfigured() {
        return pipExecutor != null;
    }

    @Override
    public boolean isPipRequirement(String requirement) {
        return requirement.contains(STRREQUIREMENT_EQ);
    }

    @Override
    public String getLibraryNameFromRequirement(String requirement) {
        String processStr = requirement.trim();
        int index = processStr.indexOf(STRREQUIREMENT_EQ);
        if(index > 0) {
            return processStr.substring(0, index).trim();
        }
        return null;
    }

    @Override
    public String getLibraryVersionFromRequirement(String requirement) {
        String processStr = requirement.trim();
        int index = processStr.indexOf(STRREQUIREMENT_EQ);
        if((index > -1) && (processStr.length() > (index + STRREQUIREMENT_EQ.length()))) {
            return processStr.substring(index + STRREQUIREMENT_EQ.length()).trim();
        }
        return null;
    }
}
