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
    private final com.example.contruction_management_app.backend.repository.CompanyMembershipRepository companyMembershipRepository;
    private final com.example.contruction_management_app.backend.repository.ProjectMemberRepository projectMemberRepository;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider,
        com.example.contruction_management_app.backend.repository.CompanyMembershipRepository companyMembershipRepository,
        com.example.contruction_management_app.backend.repository.ProjectMemberRepository projectMemberRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.companyMembershipRepository = companyMembershipRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String rawInput = request.email() != null ? request.email().trim() : "";
        String lookupEmail = rawInput.contains("@") ? rawInput : rawInput + "@chaitanyainfratech.com";

        User user = userRepository.findByEmail(lookupEmail)
            .or(() -> userRepository.findByEmail(rawInput))
            .orElseThrow(() -> new UnauthorizedException("Incorrect email or password"));

        boolean matches = passwordEncoder.matches(request.password(), user.getHashedPassword());

        if (!matches) {
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

    @Transactional(readOnly = true)
    public com.example.contruction_management_app.backend.dto.UserMeDto getUserMe(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<String> rolePermissions = user.getRole().getPermissions() != null
            ? user.getRole().getPermissions().stream().map(Permission::getId).collect(Collectors.toList())
            : List.of();

        RoleDto roleDto = new RoleDto(
            user.getRole().getId(),
            user.getRole().getName(),
            user.getRole().getDescription(),
            rolePermissions
        );

        List<com.example.contruction_management_app.backend.dto.CompanyMembershipDto> companyMemberships =
            companyMembershipRepository.findByUserId(user.getId())
                .stream()
                .map(cm -> new com.example.contruction_management_app.backend.dto.CompanyMembershipDto(
                    cm.getId(),
                    cm.getOrganization().getId(),
                    cm.getOrganization().getName(),
                    cm.getRole().getId(),
                    cm.getRole().getName(),
                    cm.getStatus(),
                    cm.getJoinedAt() != null ? cm.getJoinedAt().toString() : ""
                ))
                .collect(Collectors.toList());

        if (companyMemberships.isEmpty()) {
            companyMemberships.add(new com.example.contruction_management_app.backend.dto.CompanyMembershipDto(
                "cm_" + user.getId(),
                user.getOrganization().getId(),
                user.getOrganization().getName(),
                user.getRole().getId(),
                user.getRole().getName(),
                "ACTIVE",
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
            ));
        }

        List<com.example.contruction_management_app.backend.dto.ProjectMembershipDto> projectMemberships =
            projectMemberRepository.findByUserId(user.getId())
                .stream()
                .map(pm -> new com.example.contruction_management_app.backend.dto.ProjectMembershipDto(
                    pm.getId(),
                    pm.getProject().getId(),
                    pm.getProject().getName(),
                    pm.getProject().getCode(),
                    pm.getAssignedRole(),
                    pm.getStatus() != null ? pm.getStatus() : "ACTIVE",
                    pm.getJoinedAt() != null ? pm.getJoinedAt().toString() : ""
                ))
                .collect(Collectors.toList());

        return new com.example.contruction_management_app.backend.dto.UserMeDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getAvatarUrl(),
            user.getOrganization().getId(),
            user.getOrganization().getName(),
            roleDto,
            companyMemberships,
            projectMemberships,
            rolePermissions
        );
    }
}

