package com.mdsrtech.backend.services;

import com.mdsrtech.backend.domain.dtos.customresponses.auth.AuthResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.LoginRequestDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.RefreshResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.RegisterRequestDTO;
import com.mdsrtech.backend.domain.dtos.entities.UserDTO;

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

}
