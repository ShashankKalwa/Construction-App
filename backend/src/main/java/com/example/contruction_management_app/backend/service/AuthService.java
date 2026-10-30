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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    private final Environment environment;
    private final com.example.contruction_management_app.backend.repository.CompanyMembershipRepository companyMembershipRepository;
    private final com.example.contruction_management_app.backend.repository.ProjectMemberRepository projectMemberRepository;

    @Value("${cms.auth.allow-demo-passwords:false}")
    private boolean allowDemoPasswords;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider,
        Environment environment,
        com.example.contruction_management_app.backend.repository.CompanyMembershipRepository companyMembershipRepository,
        com.example.contruction_management_app.backend.repository.ProjectMemberRepository projectMemberRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.environment = environment;
        this.companyMembershipRepository = companyMembershipRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @PostConstruct
    public void validateSecurityGuard() {
        boolean isDevOrTest = environment.matchesProfiles("dev") || environment.matchesProfiles("test");
        if (allowDemoPasswords && !isDevOrTest) {
            throw new IllegalStateException(
                "FATAL SECURITY VIOLATION: Universal demo passwords ('cms.auth.allow-demo-passwords') cannot be enabled outside dev/test profiles!"
            );
        }
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String rawInput = request.email() != null ? request.email().trim() : "";
        String lookupEmail = rawInput.contains("@") ? rawInput : rawInput + "@apexinfra.com";

        User user = userRepository.findByEmail(lookupEmail)
            .or(() -> userRepository.findByEmail(rawInput))
            .orElseThrow(() -> new UnauthorizedException("Incorrect email or password"));

        boolean isDevOrTest = (environment.matchesProfiles("dev") || environment.matchesProfiles("test")) && allowDemoPasswords;

        boolean matches = passwordEncoder.matches(request.password(), user.getHashedPassword())
            || (isDevOrTest && (
                "Apex@123".equals(request.password())
                || "password123".equals(request.password())
                || isRolePassword(user.getRole().getId(), request.password())
            ));

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

    private boolean isRolePassword(String roleId, String password) {
        if (password == null || roleId == null) return false;
        return switch (roleId.toLowerCase()) {
            case "super_admin" -> password.equals("Admin@123");
            case "company_admin" -> password.equals("Company@123");
            case "project_manager" -> password.equals("Manager@123");
            case "civil_engineer" -> password.equals("Engineer@123");
            case "site_supervisor" -> password.equals("Site@123");
            case "storekeeper" -> password.equals("Store@123");
            case "accountant" -> password.equals("Accounts@123");
            case "contractor" -> password.equals("Contract@123");
            case "client_owner" -> password.equals("Client@123");
            default -> false;
        };
    }
}

