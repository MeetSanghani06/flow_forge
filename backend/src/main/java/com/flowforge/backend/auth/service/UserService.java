package com.flowforge.backend.auth.service;

import com.flowforge.backend.auth.dto.RegisterRequest;
import com.flowforge.backend.auth.dto.RegisterResponse;
import com.flowforge.backend.auth.entity.Role;
import com.flowforge.backend.auth.entity.User;
import com.flowforge.backend.auth.repository.UserRepository;
import com.flowforge.backend.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                "A user with this email already exists"
            );
        }

        User user = new User();

        user.setEmail(email);
        user.setPasswordHash(
            passwordEncoder.encode(request.password())
        );
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName()
        );
    }
}
