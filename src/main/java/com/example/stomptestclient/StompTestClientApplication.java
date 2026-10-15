package com.example.stomptestclient;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StompTestClientApplication {

    public static void main(
            String[] args
    ) throws Exception {

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "       STOMP TWO-USER TEST CLIENT"
        );
        System.out.println(
                "=========================================="
        );
        System.out.println();

        String userAToken = System.getenv("USER_A_ACCESS_TOKEN");
        String userBToken = System.getenv("USER_B_ACCESS_TOKEN");

        if (userAToken == null || userAToken.isBlank()) {
            throw new IllegalStateException(
                    "USER_A_ACCESS_TOKEN environment variable "
                            + "is not set."
            );
        }

        if (userBToken == null || userBToken.isBlank()) {
            throw new IllegalStateException(
                    "USER_B_ACCESS_TOKEN environment variable "
                            + "is not set."
            );
        }

        StompTestSessionHandler userA =
                new StompTestSessionHandler(
                        "USER-A",
                        userAToken
                );

        StompTestSessionHandler userB =
                new StompTestSessionHandler(
                        "USER-B",
                        userBToken
                );

        userA.connect().join();
        userB.connect().join();

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "   BOTH USERS CONNECTED"
        );
        System.out.println(
                "=========================================="
        );
        System.out.println();

        /*
         * Give subscriptions a small amount of time
         * to become established before sending.
         */
        Thread.sleep(1500);

        /*
         * CHANGE THIS to an existing conversation
         * between User A and User B.
         */
        Long conversationId = 2L;

        System.out.println(
                "TEST CLIENT PROCESS ID: "
                        + ProcessHandle.current().pid()
        );

        System.out.println(
                "ENV CONVERSATION_ID: ["
                        + System.getenv("CONVERSATION_ID")
                        + "]"
        );

        System.out.println(
                "PARSED CONVERSATION_ID: "
                        + conversationId
        );

        userB.markConversationRead(
                conversationId
        );

        userA.sendMessage(
                conversationId,
                "Hello from User AAA!"
        );

        boolean userAReceived =
                userA.awaitMessage(
                        10,
                        TimeUnit.SECONDS
                );

        boolean userBReceived = userB.awaitMessage(
                10,
                TimeUnit.SECONDS
        );

        userB.markConversationRead(
                conversationId
        );

//        userB.markConversationRead(
//                conversationId
//        );

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "          MESSAGE TEST RESULT"
        );
        System.out.println(
                "=========================================="
        );

        System.out.println(
                "User A received message: "
                        + userAReceived
        );

        System.out.println(
                "User B received message: "
                        + userBReceived
        );

        System.out.println();

        userA.disconnect();
        userB.disconnect();

        Thread.sleep(1_000);
    }

}