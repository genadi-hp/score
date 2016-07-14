package io.cloudslang.pypi.transformers;

import org.springframework.stereotype.Component;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
@Component
public class WheelPackageTransformer extends PackageTransformer {
    @Override
    public String getSupportedFormat() {
        return ".whl";
    }
}
