package org.eventix.authservice.kafka;

import lombok.RequiredArgsConstructor;
import org.eventix.authservice.event.UserDeletedEvent;
import org.eventix.authservice.event.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(Long userId, String email, String role) {

        UserRegisteredEvent event = new UserRegisteredEvent(
                userId,
                email,
                role,
                Instant.now()
        );

        kafkaTemplate.send("USER_REGISTERED", event);
    }

    public void publishUserDeleted(Long userId) {

        UserDeletedEvent event = new UserDeletedEvent(
                userId,
                Instant.now()
        );

        kafkaTemplate.send("USER_DELETED", event);
    }
}
