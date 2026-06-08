package org.eventix.authservice.service;

import org.eventix.authservice.model.dto.request.UserUpdateRequest;
import org.eventix.authservice.model.dto.response.UserResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {

    UserResponse getUser(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    int purgeDeletedUsers();
}
