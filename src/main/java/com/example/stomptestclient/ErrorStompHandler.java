package com.example.stomptestclient;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;

public class ErrorStompHandler
        implements StompFrameHandler {

    private final String clientName;

    public ErrorStompHandler(
            String clientName
    ) {
        this.clientName = clientName;
    }

    @Override
    public Type getPayloadType(
            StompHeaders headers
    ) {
        return StompErrorResponse.class;
    }

    @Override
    public void handleFrame(
            StompHeaders headers,
            Object payload
    ) {

        StompErrorResponse error =
                (StompErrorResponse) payload;

        System.err.println();
        System.err.println(
                "=========================================="
        );
        System.err.println(
                "["
                        + clientName
                        + "] STOMP APPLICATION ERROR"
        );
        System.err.println(
                "=========================================="
        );

        System.err.println(
                "Code: "
                        + error.code()
        );

        System.err.println(
                "Message: "
                        + error.message()
        );

        System.err.println(
                "Timestamp: "
                        + error.timestamp()
        );
    }
}