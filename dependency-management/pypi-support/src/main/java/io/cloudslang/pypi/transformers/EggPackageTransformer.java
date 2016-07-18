package io.cloudslang.pypi.transformers;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
public class EggPackageTransformer extends PackageTransformer {
    private static final String EGG = ".egg";

    @Autowired
    private PyPiMetaDataHelper pyPiMetaDataHelper;

    @Override
    public String getMetaData(String absolutePath) {
        return pyPiMetaDataHelper.getWheelLikeMetaData(absolutePath);
    }

    @Override
    public boolean isSupportedFormat(String extension) {
        return EGG.equalsIgnoreCase(extension);
    }

    @Override
    public String getSupportedFormat() {
        return EGG;
    }
}
