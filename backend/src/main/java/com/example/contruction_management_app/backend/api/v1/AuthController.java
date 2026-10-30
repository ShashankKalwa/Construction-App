package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.LoginRequest;
import com.example.contruction_management_app.backend.dto.TokenResponse;
import com.example.contruction_management_app.backend.dto.UserDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<com.example.contruction_management_app.backend.dto.UserMeDto> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        com.example.contruction_management_app.backend.dto.UserMeDto user = authService.getUserMe(principal);
        return ResponseEntity.ok(user);
    }
}
