package org.eventix.authservice.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.UserNotFoundException;
import org.eventix.authservice.mapper.UserMapper;
import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserStatus;
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
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = findActiveUser(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findActiveUser(id);

        userMapper.updateUser(request, user);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {

        User user = findActiveUser(id);

        user.setStatus(UserStatus.DELETED);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        //TODO: async cleanup recommended
        refreshTokenService.revokeAll(id);
        sessionService.revokeAll(id);
    }

    private User findActiveUser(Long id) {
        return userRepository.findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}