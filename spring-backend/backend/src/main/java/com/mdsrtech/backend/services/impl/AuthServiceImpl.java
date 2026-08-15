package com.mdsrtech.backend.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.mdsrtech.backend.config.security.JwtService;
import com.mdsrtech.backend.domain.dtos.customresponses.auth.*;
import com.mdsrtech.backend.domain.dtos.entities.UserDTO;
import com.mdsrtech.backend.domain.entities.PasswordResetToken;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.mapper.Mapper;
import com.mdsrtech.backend.repositories.PasswordResetTokenRepository;
import com.mdsrtech.backend.repositories.UserRepository;
import com.mdsrtech.backend.services.AuthService;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender javaMailSender;
    @Value("${frontend.url}")
    private String frontendUrl;

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

    @Transactional
    @Override
    public Map<String, String> forgotPassword(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("No account found with that email"));

        if (user.getOauthProvider() != null &&  user.getPasswordHash() == null) {
            throw new RuntimeException("This account uses " + user.getOauthProvider() + " sign in. Please use the " + user.getOauthProvider() + " button to log in.");
        }

        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findAllByUserIdAndUsedFalse(user.getId());
        existingTokens.forEach(token -> token.setUsed(true));
        passwordResetTokenRepository.saveAll(existingTokens);

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS); // 1 hour expiration

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(user.getEmail());
            mimeMessageHelper.setSubject("Reset Your MDSRTech Password");
            mimeMessageHelper.setText(buildResetPasswordHtml(user, resetLink), true);
            javaMailSender.send(mimeMessage);
            return Map.of("message", "Password reset email sent successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email. Please try again later.");
        }

    }

    @Override
    public VerifyResetDTO verifyResetToken(String token) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Token is required");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (passwordResetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }

        return VerifyResetDTO.builder()
                .valid(true)
                .email(passwordResetToken.getUser().getEmail())
                .build();

    }

    @Transactional
    @Override
    public Map<String, String> resetPassword(String token, String newPassword) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Token is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required");
        }
        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters long");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        resetToken.setUsed(true);
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(user.getEmail());
            mimeMessageHelper.setSubject("Your MDSRTech Password Has Been Changed");
            mimeMessageHelper.setText(buildPasswordChangedHtml(user), true);
            javaMailSender.send(mimeMessage);
            System.out.println("Password changed confirmation email sent to " + user.getEmail());
        } catch (Exception e) {
            System.out.println("Confirmation email error: " + e.getMessage());
        }

        return Map.of("message", "Password reset successfully");

    }

    private String buildPasswordChangedHtml(User user) {
        return String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <h1 style="color: #2563eb; text-align: center;">MDSRTech</h1>
            <h2 style="color: #1f2937;">Password Changed Successfully</h2>
            <p style="color: #4b5563; font-size: 16px;">
                Hi %s,
            </p>
            <p style="color: #4b5563; font-size: 16px;">
                Your password has been successfully changed. You can now log in with your new password.
            </p>
            <p style="color: #4b5563; font-size: 14px;">
                If you did not make this change, please contact us immediately.
            </p>
            <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;" />
            <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                © 2025 MDSRTech. All rights reserved.
            </p>
        </div>
        """, user.getFullName());
    }

    private String buildResetPasswordHtml(User user, String resetLink) {
        return String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h1 style="color: #2563eb; text-align: center;">MDSRTech</h1>
                <h2 style="color: #1f2937;">Reset Your Password</h2>
                <p style="color: #4b5563; font-size: 16px;">
                    Hi %s,
                </p>
                <p style="color: #4b5563; font-size: 16px;">
                    We received a request to reset your password. Click the button below to create a new password:
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #2563eb; color: white; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: bold; display: inline-block;">
                        Reset Password
                    </a>
                </div>
                <p style="color: #4b5563; font-size: 14px;">
                    This link will expire in 1 hour for security reasons.
                </p>
                <p style="color: #4b5563; font-size: 14px;">
                    If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
                </p>
                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;" />
                <p style="color: #9ca3af; font-size: 12px; text-align: center;">
                    © 2025 MDSRTech. All rights reserved.
                </p>
            </div>
        """, user.getFullName(), resetLink);
    }

}