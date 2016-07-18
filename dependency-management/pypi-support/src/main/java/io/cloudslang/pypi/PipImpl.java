package io.cloudslang.pypi;

import io.cloudslang.pypi.transformers.PyPiMetaDataHelper;
import io.cloudslang.pypi.transformers.PyPiRequire;
import io.cloudslang.utils.PyPiRequireUtils;
import org.apache.log4j.Logger;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class PipImpl implements Pip {
    private static final Logger logger = Logger.getLogger(PipImpl.class);

    private static final String NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING = "No PyPi support. '" + PYTHON_LIB + "' system property is missing!!!";
    public static final String UNIX_SEPARATOR = "/";

    @Value("#{ systemProperties['" + PYPI_REPOSITORY_PROPERTY + "']}")
    private String pypiUrl;

    @Value("#{systemProperties['" + PYTHON_LIB + "']}")
    private String pythonLib;

    @Autowired
    private PyPiMetaDataHelper pyPiMetaDataHelper;

    private PythonInterpreter pipExecutor;

    @PostConstruct
    private void initPip() {
        if((pythonLib != null) && (pypiUrl != null)) {
            logger.info("Starting pip support configuration process");

            pipExecutor = new PythonInterpreter();
            logger.info("Pip package loaded to the python interpreter");
        } else {
            logger.info(NO_PY_PI_SUPPORT_PIP_HOME_IS_MISSING);
        }
    }

    @Override
    public PypiLibrary download(String libraryName, String libraryVersion, String downloadFolder) {
        return download(libraryName, PyPiRequireUtils.REQUIRE_EQ, libraryVersion, downloadFolder);
    }

    @Override
    public PypiLibrary download(String libraryName, String downloadFolder) {
        return download(libraryName, null, null, downloadFolder);
    }

    @Override
    public PypiLibrary download(String libraryName, String condition, String libraryVersion, String downloadFolder) {
        PypiLibrary pypiLibrary = downloadLibraries(libraryName, condition, libraryVersion, downloadFolder);
        pyPiMetaDataHelper.transformLibrariesToZip(downloadFolder);
        return pypiLibrary;
    }


    private PypiLibrary downloadLibraries(String libraryName, String condition, String libraryVersion, String downloadFolder) {
        String libraryRequirement = libraryName + (condition != null ? (condition + libraryVersion) : "");
        downloadFolder = downloadFolder.replace("\\", UNIX_SEPARATOR);
        if(downloadFolder.endsWith(UNIX_SEPARATOR)) {
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
        return loadLibrary(libraryName, condition, libraryVersion, downloadFolder);
    }

    private PypiLibrary loadLibrary(String libraryName, String condition, String libraryVersion, String downloadFolder) {
        File pipLibraryFolderFile = new File(downloadFolder);
        File [] libraries = pipLibraryFolderFile.listFiles();

        PypiLibrary pypiLibrary = pyPiMetaDataHelper.loadLibrary(libraryName, condition, libraryVersion, libraries);
        if(pypiLibrary != null) {
            for(PyPiRequire require: pypiLibrary.getRequires()) {
                PypiLibrary dependencyLibrary =
                        loadLibrary(require.getLibrary(), require.getCondition(), require.getVersion(), downloadFolder);
                if(dependencyLibrary == null) {
                    dependencyLibrary =
                            downloadLibraries(require.getLibrary(), require.getCondition(), require.getVersion(), downloadFolder);
                }
                if(dependencyLibrary != null) {
                    pypiLibrary.addDependency(dependencyLibrary);
                }
            }
        }
        return pypiLibrary;
    }

    @Override
    public boolean isPipConfigured() {
        return pipExecutor != null;
    }

    @Override
    public boolean isPipRequirement(String requirement) {
        //we support only == for upper levels
        return requirement.contains(PyPiRequireUtils.REQUIRE_EQ);
    }

    @Override
    public String getLibraryNameFromRequirement(String requirement) {
        return pyPiMetaDataHelper.getLibraryNameFromRequirement(requirement);
    }

    @Override
    public String getRequirementCondition(String requirement) {
        return pyPiMetaDataHelper.getRequirementCondition(requirement);
    }

    @Override
    public String getLibraryVersionFromRequirement(String requirement) {
        return pyPiMetaDataHelper.getLibraryVersionFromRequirement(requirement);
    }
}
