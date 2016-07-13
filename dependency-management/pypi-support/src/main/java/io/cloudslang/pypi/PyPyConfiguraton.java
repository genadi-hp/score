package io.cloudslang.pypi;

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
}
