package io.cloudslang.pypi.transformers;

import org.python.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class ZipPackageTransformer extends PackageTransformer {
    @Autowired
    private PyPiMetaDataHelper pyPiMetaDataHelper;

    @Override
    public String getMetaData(String absolutePath) {
        return pyPiMetaDataHelper.getWheelLikeMetaData(absolutePath);
    }

    @Override
    public boolean isSupportedFormat(String extension) {
        return ZIP_EXTENSION.equalsIgnoreCase(extension);
    }

    @Override
    public String getSupportedFormat() {
        return ZIP_EXTENSION;
    }

    @Override
    public void transform(String packagePath) {
        logger.info("Transforming format " + getSupportedFormat() + " to zip [" + packagePath + "]");
        String pkgInfoPath = getPackageParentFolder(packagePath);
        File sourceFile = new File(packagePath);
        if(pkgInfoPath != null) {
            File destFile = new File(packagePath.substring(0, packagePath.length() - ZIP_EXTENSION.length()).toLowerCase() + "_temp" + ZIP_EXTENSION);
            try(ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile));
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destFile))) {
                // move the whole folder from gzip to zip which contains endsWith.caseinsensitive(EGG_INFO) or endsWith.caseinsensitive(PKG_INFO)
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if(ze.getName().startsWith(pkgInfoPath)) {
                        String entryName = ze.getName().substring(pkgInfoPath.length());
                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zos.putNextEntry(zipEntry);
                        if(!ze.isDirectory()) {
                            IOUtils.copy(zis, zos);
                        }
                    }
                }
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
        } else {
            sourceFile.delete();
        }
    }

    protected String getPackageParentFolder(String packagePath) {
        File sourceFile = new File(packagePath);
        String pkgInfoPath = null;
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().toLowerCase().endsWith(PKG_INFO) &&
                        ((pkgInfoPath == null) || ze.getName().length() < pkgInfoPath.length())) {
                    pkgInfoPath = ze.getName();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to transform [" + sourceFile.getAbsolutePath() + "] to [" + packagePath + "]", e);
        }

        if(pkgInfoPath != null) {
            pkgInfoPath = pkgInfoPath.substring(0, pkgInfoPath.lastIndexOf('/') + 1);
            if(pkgInfoPath.endsWith(EGG_INFO + "/")) {
                pkgInfoPath = pkgInfoPath.substring(0, pkgInfoPath.indexOf(EGG_INFO + "/"));
            }
        }

        return pkgInfoPath;
    }
}
