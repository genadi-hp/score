package io.cloudslang.pypi;

import io.cloudslang.pypi.transformers.EggPackageTransformer;
import io.cloudslang.pypi.transformers.PackageTransformer;
import io.cloudslang.pypi.transformers.TarballPackageTransformer;
import io.cloudslang.pypi.transformers.WheelPackageTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 13/07/2016.
 */
@Configuration
public class PyPyConfiguraton {
    @Bean
    public Pip getPip() {
        return new PipImpl();
    }

    @Bean
    public PackageTransformer wheelPackageTransformer() {
        return new WheelPackageTransformer();
    }

    @Bean
    public PackageTransformer eggPackageTransformer() {
        return new EggPackageTransformer();
    }

    @Bean
    public PackageTransformer tarballPackageTransformer() {
        return new TarballPackageTransformer();
    }

    @Bean
    public Pip2Maven pip2Maven() {
        return new Pip2MavenImpl();
    }
}
