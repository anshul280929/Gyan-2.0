package com.gyan.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gyan.entity.User;
import com.gyan.repository.UserRepository;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        return userRepository.findByEmail(email).orElseThrow();
    }
}
