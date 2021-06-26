package com.pttrn42.microprimer.servicechassispringboot;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

@Slf4j
public class SmokeTest {
    private static final String TEST_NAME_PER_SERVICE_INSTANCE = RepeatedTest.DISPLAY_NAME_PLACEHOLDER + " service instance " + RepeatedTest.CURRENT_REPETITION_PLACEHOLDER;

    @RegisterExtension
    public static SystemTestEnvironment environment = new SystemTestEnvironment();

    @RepeatedTest(value = SystemTestEnvironment.SERVICE_NUM_INSTANCES, name = TEST_NAME_PER_SERVICE_INSTANCE)
    void pingPongs(RepetitionInfo repetitionInfo) {
        given()
                .baseUri(environment.serviceUrl(repetitionInfo.getCurrentRepetition())) //example how to get url of the specific instance
        .when()
                .get("/actuator/ping")
        .then()
                .body(containsString("pong"))
                .statusCode(200);
    }

    @Test
    void shouldBeHealthy() {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get("/actuator/health")
        .then()
                .body("status", equalTo("UP"))
                .statusCode(200);
    }

    @Test
    void shouldExposeJmxControls() {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
                .queryParam("mimeType", "application/json")
        .when()
                .get("/actuator/jolokia")
        .then()
                .body("status", equalTo(200))
                .body("request.type", equalTo("version"))
                .statusCode(200);
    }

    @Test
    void shouldExposeHawtioUi() {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get("/actuator/hawtio")
        .then()
                .body(containsString("<title>Hawtio</title>"))
                .statusCode(200);
    }

    @Test
    void shouldExposeThreadDump() {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get("/actuator/threaddump")
        .then()
                .body("threads", hasSize(greaterThan(0)))
                .statusCode(200);
    }

    @Test
    void shouldExposePrometheusMetrics() {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get("/actuator/prometheus")
        .then()
                .body(containsString("app=\"service-chassis-springboot\""))
                .statusCode(200);
    }

    @Test
    void shouldExposeSwaggerApi() {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get("/v3/api-docs")
        .then()
                .body("openapi", equalTo("3.0.3"))
                .statusCode(200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/swagger-ui/index.html", "/actuator/swagger-ui/"})
    void shouldExposeSwaggerUI(String url) {
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get(url)
        .then()
                .body(containsString("<title>Swagger UI</title>"))
                .statusCode(200);
    }

    @Test
    @AfterAll
    static void shouldRecordZipkinInteractions() {
        List<LoggedRequest> allRequests = environment.wireMock()
                .find(postRequestedFor(urlPathEqualTo("/api/v2/spans")));

        assertThat(allRequests, hasSize(greaterThan(0)));
    }

}
