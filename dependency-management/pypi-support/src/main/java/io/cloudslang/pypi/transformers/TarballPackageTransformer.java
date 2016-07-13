package io.cloudslang.pypi.transformers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
@Component
public class TarballPackageTransformer implements PackageTransformer {
    private static final Logger logger = Logger.getLogger(PackageTransformer.class);

    @Override
    public String getSupportedFormat() {
        return "gz";
    }

    @Override
    public void transform(String packagePath) {
        logger.info("Transforming format " + getSupportedFormat() + " to zip [" + packagePath + "]");
    }
}
