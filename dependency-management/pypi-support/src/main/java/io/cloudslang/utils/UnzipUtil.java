package io.cloudslang.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 17/07/2016.
 */
public class UnzipUtil {
    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public static void unzipToFolder(String folderPath, InputStream source) {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        File mavenHome = new File(folderPath);
        if(!mavenHome.exists()) {
            try (ZipInputStream zio = new ZipInputStream(source)) {
                ZipEntry ze;
                while ((ze = zio.getNextEntry()) != null) {
                    File file = new File(mavenHome, ze.getName());
                    if(ze.isDirectory()) {
                        file.mkdirs();
                    } else {
                        file.getParentFile().mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            int len;
                            while ((len = zio.read(buffer)) > 0)
                            {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
