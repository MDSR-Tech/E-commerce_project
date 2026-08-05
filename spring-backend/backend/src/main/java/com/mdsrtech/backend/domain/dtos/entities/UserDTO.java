package com.mdsrtech.backend.domain.dtos.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mdsrtech.backend.domain.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private UUID id;
    private String email;
    @JsonProperty("full_name")
    private String fullName;
    private Role role;
    @JsonProperty("oauth_provider")
    private String oauthProvider;

}
