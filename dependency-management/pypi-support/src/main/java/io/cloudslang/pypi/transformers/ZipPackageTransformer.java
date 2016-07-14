package io.cloudslang.pypi.transformers;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
@Component
public class ZipPackageTransformer extends PackageTransformer {
    private static final String EGG_INFO = "egg-info";
    private static final String PKG_INFO = "pkg-info";
    public static final String TAR_EXTENSION = ".tar";
    public static final String GZGZ_EXTENSION = ".gz";

    @Override
    public String getSupportedFormat() {
        return GZGZ_EXTENSION;
    }

    @Override
    public void transform(String packagePath) {
        logger.info("Transforming format " + getSupportedFormat() + " to zip [" + packagePath + "]");
        File sourceFile = new File(packagePath);
        File destFile = new File(packagePath.substring(0, packagePath.length() - ZIP_EXTENSION.length()).toLowerCase() + "_temp" + ZIP_EXTENSION);
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile));
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destFile))) {
            // move the whole folder from gzip to zip which contains endsWith.caseinsensitive(EGG_INFO) or endsWith.caseinsensitive(PKG_INFO)
        } catch (IOException e) {
            logger.error("Failed to transform [" + sourceFile.getAbsolutePath() + "] to [" + destFile.getAbsolutePath() + "]", e);
        }

        if(!sourceFile.delete()) {
            logger.error("Failed to delete [" + sourceFile + "]");
        }
        if(sourceFile.exists()) {
            logger.error("Source file [" + sourceFile + "] was not deleted");
        }
        if(!destFile.exists()) {
            logger.error("Destination file [" + sourceFile + "] was not created");
        }
        sourceFile.delete();
        destFile.renameTo(sourceFile);
    }
}
