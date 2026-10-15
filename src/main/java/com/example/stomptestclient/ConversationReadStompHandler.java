package com.example.stomptestclient;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.lang.reflect.Type;

public class ConversationReadStompHandler implements StompFrameHandler {

    private final String clientName;

    public ConversationReadStompHandler(
            String clientName
    ) {
        this.clientName = clientName;
    }

    @Override
    public Type getPayloadType(
            StompHeaders headers
    ) {
        return ConversationReadUpdate.class;
    }

    @Override
    public void handleFrame(
            StompHeaders headers,
            Object payload
    ) {

        ConversationReadUpdate update =
                (ConversationReadUpdate) payload;

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "["
                        + clientName
                        + "] CONVERSATION READ UPDATE"
        );
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Conversation ID: "
                        + update.conversationId()
        );

        System.out.println(
                "Reader ID: "
                        + update.readerId()
        );

        System.out.println(
                "Messages read: "
                        + update.messagesRead()
        );

        System.out.println(
                "Message IDs: "
                        + update.messageIds()
        );

        System.out.println(
                "Read At: "
                        + update.readAt()
        );
    }
}