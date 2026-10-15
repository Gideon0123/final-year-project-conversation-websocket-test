package com.example.stomptestclient;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;

public class MessageStatusStompHandler
        implements StompFrameHandler {

    private final String clientName;

    public MessageStatusStompHandler(
            String clientName
    ) {
        this.clientName = clientName;
    }

    @Override
    public Type getPayloadType(
            StompHeaders headers
    ) {
        return MessageStatusUpdate.class;
    }

    @Override
    public void handleFrame(
            StompHeaders headers,
            Object payload
    ) {

        MessageStatusUpdate update =
                (MessageStatusUpdate) payload;

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "["
                        + clientName
                        + "] MESSAGE STATUS UPDATE"
        );
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Message ID: "
                        + update.messageId()
        );

        System.out.println(
                "Conversation ID: "
                        + update.conversationId()
        );

        System.out.println(
                "Recipient ID: "
                        + update.recipientId()
        );

        System.out.println(
                "Delivered At: "
                        + update.deliveredAt()
        );

        System.out.println(
                "Read At: "
                        + update.readAt()
        );
    }
}