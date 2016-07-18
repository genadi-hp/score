package io.cloudslang.pypi;

import io.cloudslang.pypi.transformers.PyPiRequire;
import org.python.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 18/07/2016.
 */
public class PypiLibrary {
    private static final Pattern NAME_PATTERN = Pattern.compile("\\nName:\\s*(\\S+)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("[\\n]Version:\\s*([\\S\\.]+)");
    private static final Pattern DEPENDENCIES_PATTERN = Pattern.compile("Requires-Dist:\\s+([^;\\n]+)");

    private final String name;
    private final String version;
    private final List<PyPiRequire> dependencies = Lists.newArrayList();

    private File libraryFile = null;
    private List<PypiLibrary> dependencyLibraries = Lists.newArrayList();

    public PypiLibrary(String source) {
        Matcher matcher = NAME_PATTERN.matcher(source);
        if(matcher.find()) {
            name = matcher.group(1).trim();
        } else {
            name = null;
        }
        matcher = VERSION_PATTERN.matcher(source);
        if(matcher.find()) {
            version = matcher.group(1).trim();
        } else {
            version = null;
        }
        Matcher dependenciesMatcher = DEPENDENCIES_PATTERN.matcher(source);
        if(dependenciesMatcher != null) {
            while (dependenciesMatcher.find()) {
                dependencies.add(new PyPiRequire(dependenciesMatcher.group(1)));
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public File getFile() {
        return libraryFile;
    }

    void setFile(File f) {
        libraryFile = f;
    }

    public List<PyPiRequire> getRequires() {
        return dependencies;
    }

    public List<PypiLibrary> getLibraryDependencies() {
        return dependencyLibraries;
    }

    public String getDescription() {
        return "[" + getName() + "]-[" + getVersion() + "]";
    }

    void addDependency(PypiLibrary library) {
        dependencyLibraries.add(library);
    }
}
