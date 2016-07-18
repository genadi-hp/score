package io.cloudslang.pypi.transformers;

import io.cloudslang.utils.PyPiRequireUtils;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 18/07/2016.
 */
public class PyPiRequire {
    private final String library;
    private final String condition;
    private final String version;

    public PyPiRequire(String require) {
        if(require.indexOf('(') != -1) {
            require = require.replace('(', ' ').replace(')', ' ').trim();
        }
        library = PyPiRequireUtils.getLibraryNameFromRequirement(require);
        condition = PyPiRequireUtils.getRequirementCondition(require);
        version = PyPiRequireUtils.getLibraryVersionFromRequirement(require);
    }

    public String getLibrary() {
        return library;
    }

    public String getCondition() {
        return condition;
    }

    public String getVersion() {
        return version;
    }
}
