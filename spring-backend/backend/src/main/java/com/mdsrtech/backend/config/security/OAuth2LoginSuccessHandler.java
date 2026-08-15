package com.mdsrtech.backend.config.security;

import com.mdsrtech.backend.domain.entities.Role;
import com.mdsrtech.backend.domain.entities.User;
import com.mdsrtech.backend.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // "google" or "github"

        String providerId;
        String email;
        String fullName;

        if (provider.equals("google")) {
            providerId = oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            fullName = oAuth2User.getAttribute("name");
        } else {
            Object githubId = oAuth2User.getAttributes().get("id");
            providerId = String.valueOf(githubId);
            email = oAuth2User.getAttribute("email");
            fullName = oAuth2User.getAttribute("name") != null
                    ? oAuth2User.getAttribute("name")
                    : oAuth2User.getAttribute("login");
        }

        if (email == null) {
            response.sendRedirect(frontendUrl + "/auth/callback?error=email_not_available");
            return;
        }
        String normalizedEmail = email.toLowerCase();

        User user = userRepository.findByOauthProviderAndOauthId(provider, providerId)
                .orElseGet(() -> userRepository.findByEmail(normalizedEmail)
                        .map(existing -> {
                            existing.setOauthProvider(provider);
                            existing.setOauthId(providerId);
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> userRepository.save(User.builder()
                                .email(normalizedEmail)
                                .fullName(fullName)
                                .oauthProvider(provider)
                                .oauthId(providerId)
                                .role(Role.customer)
                                .build())));


        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("full_name", user.getFullName());
        extraClaims.put("role", user.getRole().name());

        String accessToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        response.sendRedirect(frontendUrl + "/auth/callback?access_token=" + accessToken
                + "&refresh_token=" + refreshToken);
    }

}

