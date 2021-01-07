package com.pttrn42.microprimer.servicechassispringboot.infrastructure.actuator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PingEndpointTest {

    @Test
    void shouldResponseWithPong() {
        //given
        PingEndpoint sut = new PingEndpoint();
        //when
        String response = sut.ping();
        //then
        assertThat(response).isEqualTo("pong");
    }
}