package com.example.stomptestclient;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;

public class PresenceStompHandler
        implements StompFrameHandler {

    private final String clientName;

    public PresenceStompHandler(
            String clientName
    ) {
        this.clientName = clientName;
    }

    @Override
    public Type getPayloadType(
            StompHeaders headers
    ) {
        return PresenceUpdate.class;
    }

    @Override
    public void handleFrame(
            StompHeaders headers,
            Object payload
    ) {

        PresenceUpdate update =
                (PresenceUpdate) payload;

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "["
                        + clientName
                        + "] PRESENCE UPDATE"
        );
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "User ID: "
                        + update.userId()
        );

        System.out.println(
                "Online: "
                        + update.online()
        );

        System.out.println(
                "Timestamp: "
                        + update.timestamp()
        );
    }
}