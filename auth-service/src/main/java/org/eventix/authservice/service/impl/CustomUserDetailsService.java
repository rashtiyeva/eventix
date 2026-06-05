package org.eventix.authservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.UserNotFoundException;
import org.eventix.authservice.model.entity.User;
import org.eventix.authservice.model.enums.UserStatus;
import org.eventix.authservice.repository.UserRepository;
import org.eventix.authservice.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmailAndStatus(
                        email,
                        UserStatus.ACTIVE
                )
                .orElseThrow(() -> new UserNotFoundException(email));

        return new UserPrincipal(user);
    }

    public UserDetails loadUserById(Long id) {

        User user = userRepository.findByIdAndStatus(
                id,
                UserStatus.ACTIVE
        ).orElseThrow(() -> new UserNotFoundException(id));

        return new UserPrincipal(user);
    }
}
