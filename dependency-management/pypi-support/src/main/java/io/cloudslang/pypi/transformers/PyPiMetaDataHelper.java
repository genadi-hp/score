package io.cloudslang.pypi.transformers;

import io.cloudslang.pypi.PypiLibrary;
import io.cloudslang.utils.PyPiRequireUtils;
import org.python.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 18/07/2016.
 */
public class PyPiMetaDataHelper {
    private static final String WHEEL_LIKE_METADATA = "Name: {0}\nVersion: {1}";

    @Autowired
    private List<PackageTransformer> packageTransformers;

    private final Map<String, PackageTransformer> packageTransformerMap = Maps.newHashMap();

    @PostConstruct
    private void init() {
        for (PackageTransformer packageTransformer : packageTransformers) {
            packageTransformerMap.put(packageTransformer.getSupportedFormat().toLowerCase(), packageTransformer);
        }
    }

    public boolean matchRequireVersion(String fileName, String library, String condition, String version) {
        if(!fileName.startsWith(library)) {
            return false;
        }
        if(condition == null || version == null) {
            return true;
        }

        String fileVersion = getVersionFromFileName(library, fileName);
        if(PyPiRequireUtils.REQUIRE_EQ.equals(condition)) {
            return version.equals(fileVersion);
        }

        return PyPiRequireUtils.REQUIRE_GT.equals(condition) ?
                fileVersion.compareTo(version) > 0 :
                fileVersion.compareTo(version) < 0;
    }

    public String getWheelLikeMetaData(String fileName) {
        String library = getLibraryFromFileName(fileName);
        String version = getVersionFromFileName(library, fileName);
        return MessageFormat.format(WHEEL_LIKE_METADATA, library, version);
    }

    public String getLibraryFromFileName(String fileName) {
        return fileName.substring(0, fileName.indexOf('-'));
    }

    public String getVersionFromFileName(String library, String fileName) {
        //expecting files with naming libraryname-libraryversion-whatever.extension
        String expression = fileName.substring(library.length());
        int index = expression.indexOf("-", 1);
        if(index != -1) {
            return expression.substring(1, index);
        }
        expression = expression.substring(0, expression.lastIndexOf("."));
        if(expression.endsWith(".tar")) {
            expression = expression.substring(0, expression.indexOf(".tar"));
        }
        return expression;
    }

    public int compareVersions(String v1, String v2) {
        return v1.compareTo(v2);
    }

    public String getLibraryNameFromRequirement(String requirement) {
        String processStr = requirement.trim();
        String condition = getRequirementCondition(processStr);
        if(condition == null) {
            return requirement.trim();
        }
        return processStr.substring(0, processStr.indexOf(condition)).trim();
    }

    public String getRequirementCondition(String requirement) {
        if(requirement.contains(PyPiRequireUtils.REQUIRE_EQ)) {
            return PyPiRequireUtils.REQUIRE_EQ;
        }
        if(requirement.contains(PyPiRequireUtils.REQUIRE_GT)) {
            return PyPiRequireUtils.REQUIRE_GT;
        }
        if(requirement.contains(PyPiRequireUtils.REQUIRE_LS)) {
            return PyPiRequireUtils.REQUIRE_LS;
        }
        return null;
    }

    public String getLibraryVersionFromRequirement(String requirement) {
        String processStr = requirement.trim();
        String condition = getRequirementCondition(processStr);
        if(condition == null) {
            return null;
        }
        return processStr.substring(processStr.indexOf(condition) + condition.length()).trim();
    }

    public PypiLibrary loadLibrary(String library, String condition, String version, File[] candidates) {
        File libraryFile = null;
        String libraryVersion = null;
        for (File candidate : candidates) {
            if(candidate.getName().startsWith(library)) {
                if(PyPiRequireUtils.REQUIRE_EQ.equals(condition)){
                    if(candidate.getName().contains(version)) {
                        return readMetaData(candidate);
                    }
                } else if(matchRequireVersion(candidate.getName(), library, condition, version)) {
                    String fileVersion = getVersionFromFileName(library, candidate.getName());
                    if((libraryFile == null) ||
                            (PyPiRequireUtils.REQUIRE_GT.equals(condition) && compareVersions(fileVersion, libraryVersion) > 0)  ||
                            (PyPiRequireUtils.REQUIRE_LS.equals(condition) && compareVersions(fileVersion, libraryVersion) < 0)) {
                        libraryFile = candidate;
                        libraryVersion = fileVersion;
                    }
                }
            }
        }
        return libraryFile != null ? readMetaData(libraryFile) : null ;
    }

    private PypiLibrary readMetaData(File libraryFile) {
        String absolutePath = libraryFile.getAbsolutePath();
        String extension = absolutePath.substring(absolutePath.lastIndexOf('.'));
        PackageTransformer packageTransformer = packageTransformerMap.get(extension.toLowerCase());
        String metaData = null;
        if(packageTransformer != null) {
            metaData = packageTransformer.getMetaData(absolutePath);
        }
        return metaData != null ? new PypiLibrary(metaData) : null;
    }

    public void transformLibrariesToZip(String downloadFolder) {
        File[] libraries = new File(downloadFolder).listFiles();
        if(libraries != null) {
            for(File library: libraries) {
                String absolutePath = library.getAbsolutePath();
                String extension = absolutePath.substring(absolutePath.lastIndexOf('.'));
                PackageTransformer packageTransformer = packageTransformerMap.get(extension.toLowerCase());
                if(packageTransformer != null) {
                    packageTransformer.transform(absolutePath);
                } else {
                    library.delete();
                }
            }
        }
    }

}
