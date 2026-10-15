package com.example.stomptestclient;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

public class MessageStompHandler
        implements StompFrameHandler {

    private final StompSession session;
    private final String clientName;
    private final CountDownLatch messageReceivedLatch;

    public MessageStompHandler(
            StompSession session,
            String clientName,
            CountDownLatch messageReceivedLatch
    ) {

        this.session = session;
        this.clientName = clientName;
        this.messageReceivedLatch =
                messageReceivedLatch;
    }

    @Override
    public Type getPayloadType(
            StompHeaders headers
    ) {

        /*
         * Tell JacksonJsonMessageConverter exactly
         * what Java type this STOMP MESSAGE represents.
         */
        return MessageResponse.class;
    }

    @Override
    public void handleFrame(
            StompHeaders headers,
            Object payload
    ) {

        /*
         * JacksonJsonMessageConverter has already
         * deserialized the payload.
         *
         * payload is now MessageResponse,
         * NOT byte[] and NOT JSON String.
         */
        MessageResponse message =
                (MessageResponse) payload;

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "["
                        + clientName
                        + "] MESSAGE RECEIVED ✅"
        );
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Message ID: "
                        + message.id()
        );

        System.out.println(
                "Conversation ID: "
                        + message.conversationId()
        );

        System.out.println(
                "Sender ID: "
                        + message.senderId()
        );

        System.out.println(
                "Recipient ID: "
                        + message.recipientId()
        );

        System.out.println(
                "Content: "
                        + message.content()
        );

        System.out.println(
                "Created At: "
                        + message.createdAt()
        );

        System.out.println(
                "Delivered At: "
                        + message.deliveredAt()
        );

        System.out.println(
                "Read At: "
                        + message.readAt()
        );

        /*
         * ----------------------------------------------
         * BUSINESS DELIVERY ACK
         * ----------------------------------------------
         *
         * Receiving the WebSocket/STOMP message is now
         * acknowledged explicitly.
         */
        String destination =
                "/app/conversations/"
                        + message.conversationId()
                        + "/messages/"
                        + message.id()
                        + "/delivered";

        session.send(
                destination,
                "{}"
        );

        System.out.println(
                "["
                        + clientName
                        + "] DELIVERY ACK SENT ✅"
        );

        /*
         * Only count the message after successful
         * deserialization and processing.
         */
        messageReceivedLatch.countDown();
    }
}