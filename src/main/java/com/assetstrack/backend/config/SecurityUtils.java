package com.assetstrack.backend.config;

import com.assetstrack.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found in database"))
                .getId();
    }

    public void verifyOwnership(Long requestedId) {
        Long authenticatedId = getAuthenticatedUserId();
        if (!authenticatedId.equals(requestedId)) {
            throw new AccessDeniedException("Access denied: you can only access your own resources");
        }
    }
}
