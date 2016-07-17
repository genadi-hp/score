package io.cloudslang.dependency.impl.services.utils;

import java.io.InputStream;

public class UnzipUtil {
    public static void unzipToFolder(String folderPath, InputStream source) {
        io.cloudslang.utils.UnzipUtil.unzipToFolder(folderPath, source);
    }
}
