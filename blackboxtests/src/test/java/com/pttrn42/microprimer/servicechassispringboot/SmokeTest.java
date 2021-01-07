package com.pttrn42.microprimer.servicechassispringboot;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

@Slf4j
public class SmokeTest {
    private static final String TEST_NAME_PER_SERVICE_INSTANCE = RepeatedTest.DISPLAY_NAME_PLACEHOLDER + " service instance " + RepeatedTest.CURRENT_REPETITION_PLACEHOLDER;

    @RegisterExtension
    public static SystemTestEnvironment environment = new SystemTestEnvironment();

    @RepeatedTest(value = SystemTestEnvironment.SERVICE_NUM_INSTANCES, name = TEST_NAME_PER_SERVICE_INSTANCE)
    void pingPongs(RepetitionInfo repetitionInfo) {
        // @formatter:off
        given()
                .baseUri(environment.serviceUrl(repetitionInfo.getCurrentRepetition())) //example how to get url of the specific instance
        .when()
                .get("/actuator/ping")
        .then()
                .body(containsString("pong"))
                .statusCode(200);
        // @formatter:on
    }

    @Test
    void shouldBeHealthy() {
        // @formatter:off
        given()
                .baseUri(environment.serviceUrl()) //example how to get url of the random instance
        .when()
                .get("/actuator/health")
        .then()
                .body("status", equalTo("UP"))
                .statusCode(200);
        // @formatter:on
    }

}
