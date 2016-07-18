package io.cloudslang.pypi.transformers;

import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipEntry;
import org.python.apache.commons.compress.utils.IOUtils;
import org.python.google.common.collect.Sets;
import org.python.google.common.io.Files;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.Set;
import java.util.zip.ZipOutputStream;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class TarballPackageTransformer extends PackageTransformer {
    public static final String TAR_EXTENSION = ".tar";
    public static final String GZGZ_EXTENSION = ".gz";

    @Autowired
    private PyPiMetaDataHelper pyPiMetaDataHelper;

    @Override
    public String getMetaData(String absolutePath) {
        return pyPiMetaDataHelper.getWheelLikeMetaData(absolutePath);
    }

    @Override
    public boolean isSupportedFormat(String extension) {
        return GZGZ_EXTENSION.equalsIgnoreCase(extension);
    }

    @Override
    public String getSupportedFormat() {
        return GZGZ_EXTENSION;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void transform(String packagePath) {
        logger.info("Transforming format " + getSupportedFormat() + " to zip [" + packagePath + "]");

        File sourceFile = new File(packagePath);
//        File tempDir = Files.createTempDir();
//
//        TarGZipUnArchiver ua = new TarGZipUnArchiver();
//        ua.setSourceFile(sourceFile);
//        ua.setDestDirectory(tempDir);
//        ua.extract();
//
        sourceFile.delete();
//        String pkgInfoPath = getAllPkgFiles(tempDir);
//        if(pkgInfoPath != null) {
//            File destFile = new File(packagePath.substring(0, packagePath.length() - TAR_EXTENSION.length() - GZGZ_EXTENSION.length()).toLowerCase() + ZIP_EXTENSION);
//            try(ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destFile))) {
//                // move the whole folder from gzip to zip which contains endsWith.caseinsensitive(EGG_INFO) or endsWith.caseinsensitive(PKG_INFO)
//                addFolderToZip(new File(pkgInfoPath), zos, pkgInfoPath);
//            } catch (IOException e) {
//                logger.error("Failed to transform [" + sourceFile.getAbsolutePath() + "] to [" + destFile.getAbsolutePath() + "]", e);
//            }
//
//            if(!sourceFile.delete()) {
//                logger.error("Failed to delete [" + sourceFile + "]");
//            }
//            if(sourceFile.exists()) {
//                logger.error("Source file [" + sourceFile + "] was not deleted");
//            }
//            if(!destFile.exists()) {
//                logger.error("Destination file [" + sourceFile + "] was not created");
//            }
//
//            if(!sourceFile.delete()) {
//                logger.error("Failed to delete [" + sourceFile + "]");
//            }
//            if(sourceFile.exists()) {
//                logger.error("Source file [" + sourceFile + "] was not deleted");
//            }
//            if(!destFile.exists()) {
//                logger.error("Destination file [" + sourceFile + "] was not created");
//            }
//            destFile.renameTo(sourceFile);
//        }
    }

    private void addFolderToZip(File folder, ZipOutputStream zip, String baseName) throws IOException {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                addFolderToZip(file, zip, baseName);
            } else {
                String name = file.getAbsolutePath().substring(baseName.length());
                ZipEntry zipEntry = new ZipEntry(name);
                zip.putNextEntry(zipEntry);
                IOUtils.copy(new FileInputStream(file), zip);
                zip.closeEntry();
            }
        }
    }

    private String getAllPkgFiles(File unzipedPackagePath) {
        Set<String> allPkgFiles = Sets.newHashSet();
        getAllPkgFiles(unzipedPackagePath, allPkgFiles);

        String pkgFile = null;
        for (String allPkgFile : allPkgFiles) {
            if((pkgFile == null) || allPkgFile.length() < pkgFile.length()) {
                pkgFile = allPkgFile;
            }
        }

        if(pkgFile != null) {
            pkgFile = pkgFile.substring(0, pkgFile.lastIndexOf('/') + 1);
            if(pkgFile.endsWith(EGG_INFO + "/")) {
                pkgFile = pkgFile.substring(0, pkgFile.indexOf(EGG_INFO + "/"));
            }
        }

        return pkgFile;
    }

    private void getAllPkgFiles(File unzipedPackagePath, Set<String> allPkgFile) {
        File pkgFile = new File(unzipedPackagePath, PKG_INFO);
        if(pkgFile.exists() && pkgFile.isFile()) {
            allPkgFile.add(unzipedPackagePath.getAbsolutePath());
        }
        File [] folders = unzipedPackagePath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File folder : folders) {
            getAllPkgFiles(folder, allPkgFile);
        }
    }
}
