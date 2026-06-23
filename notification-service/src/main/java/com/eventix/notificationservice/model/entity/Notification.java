package com.eventix.notificationservice.model.entity;

import com.eventix.notificationservice.model.enums.NotificationStatus;
import com.eventix.notificationservice.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId; // idempotency

    private Long userId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String templateKey;

    @Column(length = 500)
    private String subject;

    @Column(length = 5000)
    private String body;

    private Integer retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @Column(length = 1000)
    private String errorMessage;
}
