package org.eventix.authservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;

public class test {

    @KafkaListener(topics = "USER_REGISTERED", groupId = "test-group")
    public void listen(String msg) {
        System.out.println("🔥 RECEIVED: " + msg);
    }
}
