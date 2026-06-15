package org.eventix.authservice.repository;

import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    List<User> findAllByStatus(UserStatus status);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndStatus(String email, UserStatus status);

    @Modifying
    @Query("""
    delete from User u
    where u.status = org.eventix.authservice.model.enums.UserStatus.DELETED
      and u.deletedAt < :cutoff
""")
    int deletePermanently(@Param("cutoff") Instant cutoff);
}