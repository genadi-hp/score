package io.cloudslang.pypi.transformers;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public interface PackageTransformer {
    String getSupportedFormat();
    void transform(String packagePath);
}
