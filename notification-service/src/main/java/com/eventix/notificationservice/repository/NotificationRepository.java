package com.eventix.notificationservice.repository;


import com.eventix.notificationservice.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}