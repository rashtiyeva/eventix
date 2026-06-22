//package com.eventix.notificationservice.kafka;
//
//import com.eventix.notificationservice.model.dto.TicketConfirmedEvent;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationConsumer {
//
//    private final ObjectMapper objectMapper;
//    private final NotificationService notificationService;
//
//    @KafkaListener(
//            topics = "TICKET_CONFIRMED",
//            groupId = "notification-service"
//    )
//    public void handleTicketConfirmed(String message) {
//
//        try {
//            TicketConfirmedEvent event =
//                    objectMapper.readValue(message, TicketConfirmedEvent.class);
//
//            notificationService.send(
//                    event.userId(),
//                    "Your ticket is confirmed!"
//            );
//
//        } catch (Exception e) {
//            log.error("Failed to send notification", e);
//        }
//    }
//}
