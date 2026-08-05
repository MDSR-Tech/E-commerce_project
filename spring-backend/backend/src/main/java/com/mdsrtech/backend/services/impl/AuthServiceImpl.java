package com.mdsrtech.backend.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.mdsrtech.backend.config.security.JwtService;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.AuthResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.LoginRequestDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.RefreshResponseDTO;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.RegisterRequestDTO;
import com.mdsrtech.backend.domain.dtos.entities.UserDTO;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.repositories.UserRepository;
import com.mdsrtech.backend.services.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Mapper<User, UserDTO> userMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO registerRequestDTO) {

        String email = registerRequestDTO.getEmail().trim().toLowerCase();
        String password = registerRequestDTO.getPassword();
        String fullName = registerRequestDTO.getFullName().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("An account with that email already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .passwordHash(encodedPassword)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        UserDTO userDTO = userMapper.mapFromEntityToDTO(savedUser);

        return AuthResponseDTO.builder()
                .message("User registered successfully")
                .user(userDTO)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequestDTO) {

        String email = loginRequestDTO.getEmail().trim().toLowerCase();
        String password = loginRequestDTO.getPassword();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.getOauthProvider() != null && !StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("Error: This account uses " + user.getOauthProvider() + " sign in. Please use the " + user.getOauthProvider().substring(0, 1).toUpperCase() + user.getOauthProvider().substring(1) + "button to log in.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        UserDTO userDTO = userMapper.mapFromEntityToDTO(user);

        return AuthResponseDTO.builder()
                .message("Login successful")
                .user(userDTO)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    @Override
    public RefreshResponseDTO refresh(String refreshToken) {

        String email = jwtService.extractUsername(refreshToken);

        if (!"refresh".equals(jwtService.extractTokenType(refreshToken))) {
            throw new RuntimeException("Invalid token type: refresh token required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found or inactive"));

        Map<String, Object> additionalClaims = new HashMap<>();
        additionalClaims.put("email", user.getEmail());
        additionalClaims.put("full_name", user.getFullName());
        additionalClaims.put("role", user.getRole().name());

        String accessToken = jwtService.generateToken(additionalClaims, user);

        return RefreshResponseDTO.builder()
                .accessToken(accessToken)
                .build();

    }

    @Override
    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.mapFromEntityToDTO(user);
    }

    @Override
    public String logout() {
        return "Logout Successful";
    }
}