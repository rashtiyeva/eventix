# 🎟️ Eventix

### Distributed Event Ticketing Platform

<br>

![Java](https://img.shields.io/badge/Java-21-C2185B?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-1A1A1A?style=for-the-badge)
![Kafka](https://img.shields.io/badge/Kafka-Event_Driven-C2185B?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-1A1A1A?style=for-the-badge)

---

### Overview

**Eventix** is a production-style distributed event ticketing platform designed to demonstrate modern backend engineering practices and microservices architecture.

Authentication → Event Management → Ticket Reservation → Saga Orchestration → Event-Driven Notifications

---

### Built With & Architecture

Spring Boot • Apache Kafka • PostgreSQL • Redis • Liquibase • JWT • OAuth2 • Two-Factor Authentication • Transactional Outbox • Saga Pattern

---

## ➤ What is Eventix?

Eventix is a secure ticketing platform where users can sign in (including Google login), and depending on their role — admin, organizer, or buyer — they can manage events, sell or purchase tickets, and receive real-time updates and notifications about everything happening in the system.

---

## ➤ Why it stands out

**Full event-driven flow:** Event creation → ticket reservation → confirmation → notification → all coordinated through a reliable asynchronous messaging system ensuring loose coupling and scalability.

**Concurrency control:** Optimistic locking and transactional boundaries ensure safe multi-user operations under high load without data corruption.

**Event-driven architecture:** Kafka-based communication between services ensures real-time processing and system-wide consistency.

**Saga pattern:** Distributed ticket reservation workflow guarantees eventual consistency across Event and Ticket services without global transactions.

**Data persistence:** Service-level PostgreSQL databases with Liquibase migrations ensure isolated ownership and controlled schema evolution per microservices.

**Security layer:** JWT authentication with refresh token rotation, OAuth2 login, and 2FA ensures secure identity management across all entry points.

**Rate limiting:** Redis-based throttling protects authentication flows and critical endpoints from abuse and brute-force attacks.

**Clean architecture:** Clear separation of layers (controller/service/repository), domain-driven boundaries, and strict service isolation.

**Reliability patterns:** Outbox pattern, idempotent consumers, and scheduled cleanup jobs ensure safe retries and consistent system state in distributed environments.

---

## 🔧 Services

| Service | Port | Responsibility |
|----------|------|----------------|
| **🔏 Auth Service** | 8087 | Identity management, JWT security, OAuth2 login, refresh token rotation, session management, RBAC, TOTP-based 2FA and security enforcement |
| **🪩 Event Service** | 8088 | Event lifecycle management, publishing workflow, reservation validation, seat allocation and event orchestration |
| **📩 Notification Service** | 8089 | Asynchronous notification processing, email delivery and communication audit trail |
| **🎫 Ticket Service** | 8090 | Ticket reservation, confirmation, cancellation, expiration handling and Saga participation |
| **⚙️ Infra Service** | 8091 | Infrastructure orchestration, containerized deployment, environment configuration and platform provisioning |
---

## 🩸 Service-Level Design Decisions

### 🔐 Authentication Service

The Authentication Service is designed as the platform’s security boundary, responsible for authentication, authorization, session management, and account protection.

**JWT + Refresh Token Rotation:** Secure and stateless authentication model with improved session security.  
**OAuth2 Integration:** Flexible support for external identity providers.  
**TOTP-Based 2FA:** Additional layer of account protection for sensitive operations.  
**Redis Rate Limiting:** Protection against brute-force and abuse of authentication endpoints.  
**Session Lifecycle Management:** Fine-grained control over session creation, validation, and revocation.  
**RBAC:** Centralized role-based access control for consistent permission management.

This results in a secure, scalable, and isolated authentication layer independent of business logic services.

---

### 🎟 Event & Ticket Services

Event and Ticket services are separated into independent bounded contexts and communicate through asynchronous messaging.

**Saga Pattern:** Ensures consistency across distributed workflows without global transactions.  
**Kafka Event Bus:** Enables loose coupling and asynchronous service communication.  
**Transactional Outbox Pattern:** Guarantees reliable event delivery in distributed environments.  
**Optimistic Locking:** Prevents race conditions under concurrent ticket reservations.  
**Eventual Consistency:** Allows independent service evolution while maintaining system correctness.

Together, these services implement a resilient and scalable reservation workflow capable of handling high concurrency.

---

### 📨 Notification Service

The Notification Service is isolated from core business logic and acts as an event-driven communication consumer.

**Event-Driven Notifications:** Eliminates direct dependencies between services.  
**Asynchronous Processing:** Improves responsiveness and system throughput.  
**Delivery Tracking:** Provides full visibility into notification status and history.  
**Failure Isolation:** Notification failures do not impact business operations.

This ensures reliable and scalable user communication without affecting transactional flows.

---

### ☁️ Infrastructure Layer

The Infrastructure layer manages deployment, configuration, and environment consistency across all services.



**Containerized Deployment:** Ensures consistent runtime across environments.  
**Centralized Configuration:** Simplifies service setup and management.  
**Infrastructure as Code:** Enables reproducible and version-controlled deployments.  
**Environment Isolation:** Guarantees predictable behavior across local and production setups.

This layer ensures that the entire system operates as a cohesive distributed platform.

---

## ➤ System Flows

### 🟥 Flow 1 — Regular User Journey (Buyer)

```text
User enters the platform
        ↓
Logs in via OAuth2 (Google / credentials)
        ↓
Gets access as a normal user (BUYER)
        ↓
Browses available events
        ↓
Selects event and buys ticket
        ↓
System processes reservation automatically
        ↓
Ticket is confirmed or rejected
        ↓
User receives notification about result
```
---

### 🟥 Flow 2 — Organizer Journey
```text
User enters the platform
        ↓
Logs in via OAuth2
        ↓
Has default role: USER
        ↓
Sends request to become ORGANIZER
        ↓
Admin approves request
        ↓
User role is upgraded to ORGANIZER
        ↓
User logs in again with new permissions
        ↓
Can now create and manage events
        ↓
Buyers can purchase tickets for these events
        ↓
Organizer receives updates about event activity
```
---

## 🚀 Quickstart (Docker)

```bash
docker compose up -d --build
```
---

## Author

[Shafiga Rashtiyeva](https://www.linkedin.com/in/shafiga-rashtiyeva-0aa427281)
