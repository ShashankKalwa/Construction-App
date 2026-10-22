package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.Permission;
import com.example.contruction_management_app.backend.domain.User;
import com.example.contruction_management_app.backend.dto.LoginRequest;
import com.example.contruction_management_app.backend.dto.RoleDto;
import com.example.contruction_management_app.backend.dto.TokenResponse;
import com.example.contruction_management_app.backend.dto.UserDto;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.exception.UnauthorizedException;
import com.example.contruction_management_app.backend.repository.UserRepository;
import com.example.contruction_management_app.backend.security.JwtTokenProvider;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Incorrect email or password"));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new UnauthorizedException("Incorrect email or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is deactivated");
        }

        String token = tokenProvider.generateToken(user.getId());

        List<String> permissions = user.getRole().getPermissions().stream()
            .map(Permission::getId)
            .collect(Collectors.toList());

        RoleDto roleDto = new RoleDto(
            user.getRole().getId(),
            user.getRole().getName(),
            user.getRole().getDescription(),
            permissions
        );

        UserDto userDto = new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getAvatarUrl(),
            user.getOrganization().getId(),
            user.getOrganization().getName(),
            roleDto
        );

        return new TokenResponse(token, "bearer", userDto);
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> permissions = user.getRole().getPermissions().stream()
            .map(Permission::getId)
            .collect(Collectors.toList());

        RoleDto roleDto = new RoleDto(
            user.getRole().getId(),
            user.getRole().getName(),
            user.getRole().getDescription(),
            permissions
        );

        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getAvatarUrl(),
            user.getOrganization().getId(),
            user.getOrganization().getName(),
            roleDto
        );
    }
}
