package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    List<User> findAllByStatus(UserStatus status);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByEmailAndStatus(String email, UserStatus status);
}