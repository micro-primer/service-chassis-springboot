package com.pttrn42.microprimer.servicechassispringboot.infrastructure.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MetricsConfigurationTest {

    private static final String DUMMY_APP_NAME = "dummy-app-name";
    private static final String DUMMY_HOSTNAME = "dummy-hostname";
    private MetricsConfiguration sut = new MetricsConfiguration();

    @Test
    void shouldAddAppAndHostTagsToMeters() {
        //given
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.application.name", DUMMY_APP_NAME);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        MeterRegistryCustomizer<MeterRegistry> customizer = sut.meterRegistryCustomizer(environment);
        //when
        customizer.customize(meterRegistry);
        meterRegistry.gauge("test",1);
        //then
        Meter meter = meterRegistry.getMeters().get(0);
        assertTagValue(meter, "app", DUMMY_APP_NAME);
        assertTagValue(meter, "host", "localhost");

    }

    @Test
    void shouldUseHostFromJMXProperty() {
        //given
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.application.name", DUMMY_APP_NAME);
        environment.setProperty("JMX_HOSTNAME", DUMMY_HOSTNAME);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        MeterRegistryCustomizer<MeterRegistry> customizer = sut.meterRegistryCustomizer(environment);
        //when
        customizer.customize(meterRegistry);
        meterRegistry.gauge("test",1);
        //then
        Meter meter = meterRegistry.getMeters().get(0);
        assertTagValue(meter, "app", DUMMY_APP_NAME);
        assertTagValue(meter, "host", DUMMY_HOSTNAME);

    }

    private void assertTagValue(final Meter meter, final String tagName, final String expectTagValue) {
        String tagValue = meter.getId().getTag(tagName);
        assertThat(tagValue).isEqualTo(expectTagValue);
    }

    @Test
    public void shouldFailWhenNoApplicationName() {
        //given
        MockEnvironment environment = new MockEnvironment();
        //then
        assertThatThrownBy(() -> sut.meterRegistryCustomizer(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Required key 'spring.application.name' not found");
    }
}