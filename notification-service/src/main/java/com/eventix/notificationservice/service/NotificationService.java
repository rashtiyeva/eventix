package com.eventix.notificationservice.service;

import com.eventix.notificationservice.model.entity.Notification;
import com.eventix.notificationservice.model.enums.NotificationStatus;
import com.eventix.notificationservice.model.enums.NotificationType;
import com.eventix.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailService emailService;

    public void send(Long userId, String email, NotificationType type, String subject, String body) {

        Notification notification = Notification.builder()
                .userId(userId)
                .email(email)
                .type(type)
                .status(NotificationStatus.PENDING)
                .subject(subject)
                .body(body)
                .createdAt(LocalDateTime.now())
                .build();

        notification = repository.save(notification);

        try {
            emailService.sendEmail(email, subject, body);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

        } catch (Exception e) {

            log.error("Email sending failed userId={}", userId, e);

            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        repository.save(notification);
    }

    public void sendWelcome(String email, Long userId) {

        send(
                userId,
                email,
                NotificationType.USER_REGISTERED,
                "Welcome to Eventix",
                "Thanks for registering!"
        );
    }

    public void sendTicketConfirmed(Long userId, String email) {

        send(
                userId,
                email,
                NotificationType.TICKET_CONFIRMED,
                "Ticket confirmed",
                "Your ticket has been confirmed."
        );
    }
}