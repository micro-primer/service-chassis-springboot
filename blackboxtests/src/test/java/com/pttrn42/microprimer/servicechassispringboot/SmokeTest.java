package com.pttrn42.microprimer.servicechassispringboot;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

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
                .get("/actuator/hawtio/jolokia")
        .then()
                .body("status", equalTo(200))
                .body("request.type", equalTo("version"))
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
                .accept(ContentType.TEXT)
        .when()
                .get("/actuator/prometheus")
        .then()
                .body(containsString("app=\"service-chassis-springboot\""))
                .statusCode(200);
    }

}
