package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.request.UserCreateRequest;
import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

//    UserResponse createUser(UserCreateRequest request);

    UserResponse getUser(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);
}
