package com.pttrn42.microprimer.servicechassispringboot.infrastructure.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "ping")
@Component
class PingEndpoint {

    @ReadOperation
    String ping() {
        return "pong";
    }
}
