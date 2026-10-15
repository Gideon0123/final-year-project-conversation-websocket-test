package com.example.stomptestclient;

import java.util.concurrent.CompletableFuture;

import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import tools.jackson.databind.json.JsonMapper;

public class StompTestSessionHandler
        extends StompSessionHandlerAdapter {

    private static final String WEBSOCKET_URL =
            "ws://localhost:8080/conversation/ws";

    private final String clientName;
    private final String accessToken;

    private StompSession session;

    private final CountDownLatch messageReceivedLatch =
            new CountDownLatch(1);

    private final ThreadPoolTaskScheduler
            stompClientTaskScheduler =
            new ThreadPoolTaskScheduler();

    public StompTestSessionHandler(
            String clientName,
            String accessToken
    ) {

        this.clientName = clientName;
        this.accessToken = accessToken;

        /*
         * Scheduler used by WebSocketStompClient for
         * STOMP heartbeat processing.
         */
        stompClientTaskScheduler.setPoolSize(2);

        stompClientTaskScheduler.setThreadNamePrefix(
                "stomp-client-heartbeat-"
                        + clientName
                        + "-"
        );

        stompClientTaskScheduler.initialize();
    }

    public CompletableFuture<StompSession> connect() {

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "       CONNECTING " + clientName
        );
        System.out.println(
                "=========================================="
        );
        System.out.println();

        System.out.println(
                "WebSocket URL:"
        );

        System.out.println(
                WEBSOCKET_URL
        );

        System.out.println();

        StandardWebSocketClient webSocketClient =
                new StandardWebSocketClient();

        WebSocketStompClient stompClient =
                new WebSocketStompClient(
                        webSocketClient
                );

        /*
         * STOMP client heartbeat scheduler.
         */
        stompClient.setTaskScheduler(
                stompClientTaskScheduler
        );

        /*
         * Spring Framework 7 / Jackson 3.
         *
         * findAndAddModules() lets JsonMapper discover
         * available Jackson modules.
         */
        JsonMapper jsonMapper =
                JsonMapper.builder()
                        .findAndAddModules()
                        .build();

        stompClient.setMessageConverter(
                new JacksonJsonMessageConverter(
                        jsonMapper
                )
        );

        WebSocketHttpHeaders handshakeHeaders =
                new WebSocketHttpHeaders();

        handshakeHeaders.setBearerAuth(
                accessToken
        );

        StompHeaders connectHeaders =
                new StompHeaders();

        connectHeaders.setHeartbeat(
                new long[]{
                        10_000L,
                        20_000L
                }
        );

        CompletableFuture<StompSession> future =
                stompClient.connectAsync(
                        WEBSOCKET_URL,
                        handshakeHeaders,
                        connectHeaders,
                        this
                );

        /*
         * Only report connection failures here.
         *
         * Subscriptions belong exclusively in
         * afterConnected().
         */
        return future.whenComplete(
                (connectedSession, throwable) -> {

                    if (throwable != null) {

                        System.err.println();
                        System.err.println(
                                "=========================================="
                        );
                        System.err.println(
                                "       "
                                        + clientName
                                        + " CONNECTION FAILED"
                        );
                        System.err.println(
                                "=========================================="
                        );
                        System.err.println();

                        throwable.printStackTrace();
                    }
                }
        );
    }

    @Override
    public void afterConnected(
            StompSession session,
            StompHeaders connectedHeaders
    ) {

        this.session = session;

        System.out.println();
        System.out.println(
                "******************************************"
        );
        System.out.println(
                "   "
                        + clientName
                        + " STOMP CONNECTED"
        );
        System.out.println(
                "******************************************"
        );
        System.out.println();

        System.out.println(
                "Session ID: "
                        + session.getSessionId()
        );

        System.out.println();

        System.out.println(
                "Connected headers:"
        );

        connectedHeaders.forEach(
                (key, value) ->
                        System.out.println(
                                key + " = " + value
                        )
        );

        System.out.println();

        /*
         * ==================================================
         * 1. MESSAGES
         * ==================================================
         */

        System.out.println(
                "["
                        + clientName
                        + "] Subscribing to "
                        + "/user/queue/messages"
        );

        session.subscribe(
                "/user/queue/messages",
                new MessageStompHandler(
                        session,
                        clientName,
                        messageReceivedLatch
                )
        );

        System.out.println(
                "["
                        + clientName
                        + "] SUBSCRIBED "
                        + "/user/queue/messages ✅"
        );

        /*
         * ==================================================
         * 2. MESSAGE STATUS
         * ==================================================
         */

        session.subscribe(
                "/user/queue/message-status",
                new MessageStatusStompHandler(
                        clientName
                )
        );

        System.out.println(
                "["
                        + clientName
                        + "] SUBSCRIBED "
                        + "/user/queue/message-status ✅"
        );

        /*
         * ==================================================
         * 3. CONVERSATION READ
         * ==================================================
         */

        session.subscribe(
                "/user/queue/conversation-read",
                new ConversationReadStompHandler(
                        clientName
                )
        );

        System.out.println(
                "["
                        + clientName
                        + "] SUBSCRIBED "
                        + "/user/queue/conversation-read ✅"
        );

        /*
         * ==================================================
         * 4. PRESENCE
         * ==================================================
         */

        session.subscribe(
                "/user/queue/presence",
                new PresenceStompHandler(
                        clientName
                )
        );

        System.out.println(
                "["
                        + clientName
                        + "] SUBSCRIBED "
                        + "/user/queue/presence ✅"
        );

        /*
         * ==================================================
         * 5. ERRORS
         * ==================================================
         */

        session.subscribe(
                "/user/queue/errors",
                new ErrorStompHandler(
                        clientName
                )
        );

        System.out.println(
                "["
                        + clientName
                        + "] SUBSCRIBED "
                        + "/user/queue/errors ✅"
        );

        /*
         * ==================================================
         * 6. OFFLINE SYNCHRONIZATION
         * ==================================================
         *
         * Synchronization MUST happen after all
         * subscriptions are established.
         */

        session.send(
                "/app/conversations/sync",
                "{}"
        );

        System.out.println(
                "["
                        + clientName
                        + "] OFFLINE MESSAGE SYNC REQUEST SENT ✅"
        );
    }

    public void sendMessage(
            Long conversationId,
            String content
    ) {

        ensureConnected();

        SendMessageRequest request =
                new SendMessageRequest(
                        content
                );

        String destination =
                "/app/conversations/"
                        + conversationId
                        + "/messages";

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "["
                        + clientName
                        + "] SENDING MESSAGE"
        );
        System.out.println(
                "Destination: "
                        + destination
        );
        System.out.println(
                "Content: "
                        + content
        );
        System.out.println(
                "=========================================="
        );

        session.send(
                destination,
                request
        );

        System.out.println(
                "["
                        + clientName
                        + "] SEND FRAME DISPATCHED TO SERVER"
        );
    }

    public void markConversationRead(
            Long conversationId
    ) {

        ensureConnected();

        String destination =
                "/app/conversations/"
                        + conversationId
                        + "/read";

        session.send(
                destination,
                "{}"
        );

        System.out.println(
                "["
                        + clientName
                        + "] READ REQUEST SENT: "
                        + destination
        );
    }

    public boolean awaitMessage(
            long timeout,
            TimeUnit unit
    ) throws InterruptedException {

        return messageReceivedLatch.await(
                timeout,
                unit
        );
    }

    private void ensureConnected() {

        if (session == null
                || !session.isConnected()) {

            throw new IllegalStateException(
                    clientName
                            + " is not connected."
            );
        }
    }

    @Override
    public void handleFrame(
            StompHeaders headers,
            Object payload
    ) {

        /*
         * This callback is for an ERROR frame that is not
         * handled by one of our application destinations.
         *
         * DO NOT deserialize this as MessageResponse.
         */

        System.err.println();
        System.err.println(
                "["
                        + clientName
                        + "] ========== STOMP ERROR FRAME =========="
        );

        headers.forEach(
                (key, value) ->
                        System.err.println(
                                key + " = " + value
                        )
        );

        System.err.println(
                "Payload: " + payload
        );

        System.err.println();
    }

    @Override
    public void handleTransportError(
            StompSession session,
            Throwable exception
    ) {

        System.err.println();
        System.err.println(
                "["
                        + clientName
                        + "] ========== WEBSOCKET ERROR =========="
        );

        exception.printStackTrace();
    }

    public void disconnect() {

        try {

            if (session != null
                    && session.isConnected()) {

                System.out.println(
                        "["
                                + clientName
                                + "] Disconnecting..."
                );

                session.disconnect();

                System.out.println(
                        "["
                                + clientName
                                + "] Disconnected."
                );
            }

        } finally {

            stompClientTaskScheduler.shutdown();
        }
    }
}