package io.cloudslang.pypi.transformers;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class WheelPackageTransformer extends PackageTransformer {
    private static final String WHL = ".whl";

    @Override
    public boolean isSupportedFormat(String extension) {
        return WHL.equalsIgnoreCase(extension);
    }

    @Override
    public String getSupportedFormat() {
        return WHL;
    }

    @Override
    public String getMetaData(String absolutePath) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(absolutePath))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().toLowerCase().endsWith("dist-info/metadata")) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    return baos.toString();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read metadata from [" + absolutePath + "]", e);
        }
        return null;
    }
}
