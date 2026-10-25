package com.voltmart.util;

import com.voltmart.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class CurrentUser {
    private CurrentUser() {}

    public static Optional<UserPrincipal> principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UUID requireId() {
        return principal().map(UserPrincipal::getId)
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));
    }
}
