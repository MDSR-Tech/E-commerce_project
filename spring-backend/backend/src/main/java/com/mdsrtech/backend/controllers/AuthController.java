package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.domain.dtos.customresponses.auth.AuthResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.LoginRequestDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.RefreshResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.RegisterRequestDTO;
import com.mdsrtech.backend.domain.dtos.entities.UserDTO;
import com.mdsrtech.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequestDTO));
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(authService.login(loginRequestDTO));
    }

    @PostMapping(path = "/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @GetMapping(path = "/me")
    public ResponseEntity<Map<String, UserDTO>> getCurrentUser(Authentication authentication) {
        UserDTO userDTO = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(Map.of("user", userDTO));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok(authService.logout());
    }

}
