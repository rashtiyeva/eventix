package org.eventix.authservice.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.UserNotFoundException;
import org.eventix.authservice.mapper.UserMapper;
import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.RefreshTokenRepository;
import org.eventix.authservice.repository.SessionRepository;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.service.RefreshTokenService;
import org.eventix.authservice.service.SessionService;
import org.eventix.authservice.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;

    @Override
    public UserResponse getUser(Long id) {
        User user = userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(id));

        userMapper.updateUser(request, user);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(id));

        refreshTokenService.revokeAll(id);
        sessionService.revokeAll(id);

        user.setStatus(UserStatus.DELETED);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }
}