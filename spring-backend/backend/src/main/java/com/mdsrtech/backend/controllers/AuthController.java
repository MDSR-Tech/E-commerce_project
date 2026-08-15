package com.mdsrtech.backend.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.*;
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

    @PostMapping(path = "/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody EmailRequestDTO emailRequestDTO) {
        return ResponseEntity.ok(authService.forgotPassword(emailRequestDTO.getEmail()));
    }

    @PostMapping(path = "/verify-reset-token")
    public ResponseEntity<VerifyResetDTO> verifyResetToken(@RequestBody TokenRequestDTO tokenRequestDTO) {
        return ResponseEntity.ok(authService.verifyResetToken(tokenRequestDTO.getToken()));
    }

    @PostMapping(path = "/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        return ResponseEntity.ok(authService.resetPassword(resetPasswordRequestDTO.getToken(), resetPasswordRequestDTO.getPassword()));
    }

}
