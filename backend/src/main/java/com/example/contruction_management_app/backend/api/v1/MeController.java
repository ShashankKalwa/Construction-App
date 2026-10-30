package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.UserMeDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AuthService authService;

    public MeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<UserMeDto> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserMeDto me = authService.getUserMe(principal);
        return ResponseEntity.ok(me);
    }
}
