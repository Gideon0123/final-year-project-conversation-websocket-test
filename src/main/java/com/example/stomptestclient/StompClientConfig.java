package com.example.stomptestclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class StompClientConfig {

    @Bean
    public ThreadPoolTaskScheduler stompClientTaskScheduler() {

        ThreadPoolTaskScheduler scheduler =
                new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix(
                "stomp-client-heartbeat-"
        );

        scheduler.initialize();

        return scheduler;
    }
}