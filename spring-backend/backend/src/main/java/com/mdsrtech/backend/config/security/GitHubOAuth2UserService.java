package com.mdsrtech.backend.config.security;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHubOAuth2UserService extends DefaultOAuth2UserService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        if (!"github".equals(userRequest.getClientRegistration().getRegistrationId())) {
            return oAuth2User;
        }

        if (oAuth2User.getAttribute("email") != null) {
            return oAuth2User;
        }

        String accessToken = userRequest.getAccessToken().getTokenValue();
        String primaryEmail = fetchPrimaryEmail(accessToken);

        if (primaryEmail == null) {
            return oAuth2User;
        }

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", primaryEmail);

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "id");
    }

    @SuppressWarnings("unchecked")
    private String fetchPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        List<Map<String, Object>> emails = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        ).getBody();

        if (emails == null || emails.isEmpty()) {
            return null;
        }

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElseGet(() -> (String) emails.getFirst().get("email"));
    }
}
