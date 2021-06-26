package com.pttrn42.microprimer.servicechassispringboot;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.client.WireMockBuilder;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.containers.DockerComposeContainer.RemoveImages.LOCAL;

@Slf4j
class SystemTestEnvironment implements BeforeAllCallback {
    static final int SERVICE_NUM_INSTANCES = 2;

    private final String SERVICE_BASE_NAME = "service";
    private final int SERVICE_PORT = 8080;
    private static final String WIREMOCK_CONTAINER = "wiremock_1";
    private static final int WIREMOCK_PORT = 8080;

    private DockerComposeContainer environment;
    private boolean environmentStarted = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!environmentStarted) {
            environment = new DockerComposeContainer(getDockerComposeFile())
                    .withLocalCompose(true)
                    .withExposedService(WIREMOCK_CONTAINER, WIREMOCK_PORT, Wait.forListeningPort())
                    .withScaledService(SERVICE_BASE_NAME, SERVICE_NUM_INSTANCES)
                    .withRemoveImages(LOCAL);

            IntStream.rangeClosed(1, SERVICE_NUM_INSTANCES).forEach(i -> {
                String serviceInstanceName = getServiceInstanceName(i);
                environment.withLogConsumer(serviceInstanceName, new Slf4jLogConsumer(log).withPrefix(serviceInstanceName))
                        .withExposedService(serviceInstanceName, SERVICE_PORT, Wait.forListeningPort());
            });

            environment.start();
            environmentStarted = true;
        }

        givenZipkinSupport();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        Runtime.getRuntime().addShutdownHook(new Thread(environment::stop));
    }

    private File getDockerComposeFile() {
        ClassLoader classLoader = SystemTestEnvironment.class.getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource("docker-compose.yml")).getFile());
    }

    /**
     * Returns the service url to be used in tests.
     * In case of multiple instances the method will return url of the random instance. If you need to access the specific
     * instance use {@link #serviceUrl(int) }
     *
     * @return service url
     */
    String serviceUrl() {
        int instanceNumber = ThreadLocalRandom.current().nextInt(1, SERVICE_NUM_INSTANCES + 1);
        return serviceUrl(instanceNumber);
    }

    /**
     * Returns the service url of the specific instance to be used in tests. If you want to use a random instance or have
     * a single one then simply use {@link #serviceUrl()}.
     *
     * @param instanceNumber number of the instance (starting from 1) which the service url will be returned
     * @return service url
     */
    String serviceUrl(int instanceNumber) {
        assertTrue(instanceNumber >= 1, "Invalid instanceNumber. Must be at least one");
        assertTrue(instanceNumber <= SERVICE_NUM_INSTANCES, "Invalid instanceNumber. Must not exceed number of instances = " + SERVICE_NUM_INSTANCES);

        String serviceInstanceName = getServiceInstanceName(instanceNumber);
        return String.format("http://%s:%d",
                environment.getServiceHost(serviceInstanceName, SERVICE_PORT),
                environment.getServicePort(serviceInstanceName, SERVICE_PORT));
    }

    WireMock wireMock() {
        return new WireMockBuilder()
                .host(environment.getServiceHost(WIREMOCK_CONTAINER, WIREMOCK_PORT))
                .port(environment.getServicePort(WIREMOCK_CONTAINER, WIREMOCK_PORT))
                .build();
    }

    void givenZipkinSupport() {
        wireMock().register(WireMock.post("/api/v2/spans")
                .withHeader("Host", equalTo("zipkin:8080"))
                .willReturn(aResponse()
                        .withStatus(200)
                ));
    }

    private String getServiceInstanceName(int instanceNumber) {
        return SERVICE_BASE_NAME + "_" + instanceNumber;
    }
}
