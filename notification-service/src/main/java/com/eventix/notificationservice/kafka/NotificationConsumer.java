package com.eventix.notificationservice.kafka;

import com.eventix.notificationservice.event.TicketConfirmedEvent;
import com.eventix.notificationservice.event.TicketRejectedEvent;
import com.eventix.notificationservice.event.TicketReservedEvent;
import com.eventix.notificationservice.event.UserRegisteredEvent;
import com.eventix.notificationservice.service.EmailService;
import com.eventix.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(topics = "USER_REGISTERED", groupId = "notification-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {

        log.info("USER_REGISTERED received userId={}", event.userId());

        notificationService.sendWelcome(
                event.email(),
                event.userId()
        );
    }

    @KafkaListener(topics = "TICKET_CONFIRMED", groupId = "notification-service-group")
    public void handleTicketConfirmed(TicketConfirmedEvent event) {

        log.info("TICKET_CONFIRMED received ticketId={}", event.ticketId());

        emailService.sendEmail(
                event.email(),
                "Ticket confirmed",
                "Your ticket is confirmed!"
        );
    }

    @KafkaListener(topics = "TICKET_REJECTED", groupId = "notification-service-group")
    public void handleTicketRejected(TicketRejectedEvent event) {

        log.info("TICKET_REJECTED received ticketId={}", event.ticketId());

        emailService.sendEmail(
                event.email(),
                "Ticket rejected",
                "Reason: " + event.reason()
        );
    }

    @KafkaListener(topics = "TICKET_RESERVED", groupId = "notification-service-group")
    public void handleTicketReserved(TicketReservedEvent event) {

        log.info("TICKET_RESERVED received ticketId={}", event.ticketId());

        emailService.sendEmail(
                event.email(),
                "Ticket reserved",
                "Your ticket is reserved!"
        );
    }
}