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

    private static final String DEFAULT_PYPI_REPOSITORY = "https://pypi.python.org/simple";

    private static final String PYTHON_LIB = "python.path";

    private static final String NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING = "No PyPi support. '" + PYTHON_LIB + "' system property is missing!!!";

    @Value("#{ systemProperties['pypi.index.url']}")
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
                packageTransformerMap.put(packageTransformer.getSupportedFormat(), packageTransformer);
            }
        } else {
            logger.info(NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING);
        }
    }

    @Override
    public void download(String libraryReference, String downloadFolder) {
        downloadFolder = downloadFolder.replace("\\", File.separator).replace("/", File.separator);
        if(downloadFolder.endsWith(File.separator)) {
            downloadFolder = downloadFolder.substring(0, downloadFolder.length() - 1);
        }

        cleanDownloadFolder(downloadFolder);

        if(isPipConfigured()) {
            logger.info("Downloading library [" + libraryReference + "] to folder [" + downloadFolder + "]");
            String downloadScript = "import pip\npip.main(['download', '-i', '" + pypiUrl + "', '-d', '" + downloadFolder + "','" + libraryReference + "'])";
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
                String extension = absolutePath.substring(absolutePath.lastIndexOf(".") + 1);
                PackageTransformer packageTransformer = packageTransformerMap.get(extension);
                if(packageTransformer != null) {
                    packageTransformer.transform(absolutePath);
                }
            }
        }
    }

    private void cleanDownloadFolder(String downloadFolder) {
        File[] existingLibraries = new File(downloadFolder).listFiles();
        if(existingLibraries != null) {
            for (File library : existingLibraries) {
                if(!library.delete()) {
                    logger.error("Failed to delete [" + library.getAbsolutePath() + "] when cleaning folder");
                }
            }
        }
    }

    @Override
    public boolean isPipConfigured() {
        return pipExecutor != null;
    }
}
