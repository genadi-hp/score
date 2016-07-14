package io.cloudslang.pypi.transformers;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public abstract class PackageTransformer {
    protected static final Logger logger = Logger.getLogger(PackageTransformer.class);
    public static final String ZIP_EXTENSION = ".zip";

    public abstract String getSupportedFormat();
    public void transform(String packagePath) {
        logger.info("Transforming format " + getSupportedFormat() + " to zip [" + packagePath + "]");
        File source = new File(packagePath);
        File dest = new File(packagePath.substring(0, packagePath.lastIndexOf(".")).toLowerCase() + ZIP_EXTENSION);
        if(!source.renameTo(dest)) {
            logger.error("Failed to rename [" + source.getAbsolutePath() + "] to [" + dest.getAbsolutePath() + "]");
        }
        if(source.exists()) {
            logger.error("Source file [" + source.getAbsolutePath() + "] was not removed");
        }
        if(!dest.exists()) {
            logger.error("Destination file [" + dest.getAbsolutePath() + "] was not created");
        }
    }
}
