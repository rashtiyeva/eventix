package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.event.UserDeletedEvent;
import org.eventix.authservice.exception.UserNotFoundException;
import org.eventix.authservice.mapper.UserMapper;
import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {

        log.debug("Fetching user by id={}", id);

        User user = findActiveUser(id);

        log.debug("User found id={}", id);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        log.debug("Fetching all active users");

        List<UserResponse> users = userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(userMapper::toResponse)
                .toList();

        log.debug("Found {} active users", users.size());

        return users;
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {

        log.debug("Updating user id={}", id);

        User user = findActiveUser(id);

        userMapper.updateUser(request, user);

        UserResponse response = userMapper.toResponse(user);

        log.info("User updated successfully id={}", id);

        return response;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

        log.debug("Deleting user id={}", id);

        User user = findActiveUser(id);

        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        log.info("User marked as deleted id={}", id);

        eventPublisher.publishEvent(new UserDeletedEvent(id));

        log.info("UserDeletedEvent published for user id={}", id);
    }

    @Transactional
    @Override
    public int purgeDeletedUsers() {

        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        int deleted = userRepository.deletePermanently(cutoff);

        log.info("Permanently deleted {} users", deleted);

        return deleted;
    }

    private User findActiveUser(Long id) {

        log.debug("Looking up active user id={}", id);

        return userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Active user not found id={}", id);
                    return new UserNotFoundException(id);
                });
    }
}