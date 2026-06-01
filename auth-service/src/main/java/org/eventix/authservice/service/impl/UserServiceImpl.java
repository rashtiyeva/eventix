package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.UserNotFoundException;
import org.eventix.authservice.mapper.UserMapper;
import org.eventix.authservice.model.dto.request.UserCreateRequest;
import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserRole;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

//    @Override
//    public UserResponse createUser(UserCreateRequest request) {
//        User user = userMapper.toEntity(request);
//
//        user.setEmail(request.email());
//        user.setPassword(passwordEncoder.encode(request.password()));
//        user.setRole(UserRole.BUYER);
//        user.setStatus(UserStatus.ACTIVE);
//        user.setCreatedAt(LocalDateTime.now());
//
//        return userMapper.toResponse(userRepository.save(user));
//    }

    @Override
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userMapper.updateUser(request, user);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
    }
}
