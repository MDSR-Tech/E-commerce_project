package com.mdsrtech.backend.services;

import com.mdsrtech.backend.domain.dtos.customresponses.auth.*;
import com.mdsrtech.backend.domain.dtos.entities.UserDTO;

import java.util.Map;

public interface AuthService {

    // POST /api/auth/register
    AuthResponseDTO register(RegisterRequestDTO registerRequestDTO);
    // POST /api/auth/login
    AuthResponseDTO login(LoginRequestDTO loginRequestDTO);
    // POST /api/auth/refresh
    RefreshResponseDTO refresh(String refreshToken);
    // GET /api/auth/me
    UserDTO getCurrentUser(String email);
    // POST /api/auth/logout
    String logout();

    Map<String, String> forgotPassword(String email);
    VerifyResetDTO verifyResetToken(String token);
    Map<String, String> resetPassword(String token, String newPassword);

}
