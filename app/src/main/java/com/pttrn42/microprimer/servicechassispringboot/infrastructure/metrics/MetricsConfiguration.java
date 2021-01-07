package com.pttrn42.microprimer.servicechassispringboot.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class MetricsConfiguration {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(Environment environment) {
        String appName = environment.getRequiredProperty("spring.application.name");
        String hostName = environment.getProperty("JMX_HOSTNAME");

        return registry -> registry.config().commonTags(
                "app", appName,
                "host", hostName != null ? hostName.split("\\.")[0] : "localhost"
        );
    }
}
