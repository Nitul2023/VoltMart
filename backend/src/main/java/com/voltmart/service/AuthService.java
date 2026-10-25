package com.voltmart.service;

import com.voltmart.domain.entity.Role;
import com.voltmart.domain.entity.User;
import com.voltmart.dto.AuthDtos.*;
import com.voltmart.exception.ApiException;
import com.voltmart.repository.RoleRepository;
import com.voltmart.repository.UserRepository;
import com.voltmart.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("Email already registered");
        }
        Role customer = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> ApiException.badRequest("Customer role missing"));
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRoles(Set.of(customer));
        user = userRepository.save(user);
        notificationService.send(user.getEmail(), "Welcome to VoltMart",
                "Your account is ready. Explore the latest electronics.", "USER", user.getId().toString());
        return toAuth(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!user.isEnabled()) {
            throw ApiException.forbidden("Account disabled");
        }
        return toAuth(user);
    }

    public UserView toView(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();
        return new UserView(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), roles, user.isEmailVerified());
    }

    private AuthResponse toAuth(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String token = jwtService.generate(user.getId(), user.getEmail(), roles);
        return new AuthResponse(token, toView(user));
    }
}
