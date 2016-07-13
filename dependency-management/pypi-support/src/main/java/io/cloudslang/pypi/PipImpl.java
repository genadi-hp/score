package io.cloudslang.pypi;

import org.apache.log4j.Logger;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class PipImpl implements Pip {
    private static final Logger logger = Logger.getLogger(PipImpl.class);

    private static final String PYTHON_LIB = "python.path";

    private static final String NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING = "No PyPi support. '" + PYTHON_LIB + "' system property is missing!!!";

    @Value("#{ systemProperties['pypi.index.url']}")
    private String pypiUrl;

    @Value("#{systemProperties['" + PYTHON_LIB + "']}")
    private String pythonLib;

    private PythonInterpreter pipExecutor;

    @PostConstruct
    private void initPip() {
        if(pythonLib != null) {
            logger.info("Starting pip support configuration process");

            pipExecutor = new PythonInterpreter();
            logger.info("Pip package loaded to the python interpreter");
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
        if(isPipConfigured()) {
            logger.info("Downloading library [" + libraryReference + "] to folder [" + downloadFolder + "]");
            String downloadScript = pypiUrl == null ?
                    "import pip\npip.main(['download', '-d', '" + downloadFolder + "','" + libraryReference + "'])":
                    "import pip\npip.main(['download', '-i', '" + pypiUrl + "', '-d', '" + downloadFolder + "','" + libraryReference + "'])";
//            String downloadScript = "import pip\npip.main(['download', '-d', 'c:\\downloads\\pypi\\python-lib','aestools==0.1.1'])";
            pipExecutor.exec(downloadScript);
        } else {
            logger.error(NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING);
        }
    }

    @Override
    public boolean isPipConfigured() {
        return pipExecutor != null;
    }
}
