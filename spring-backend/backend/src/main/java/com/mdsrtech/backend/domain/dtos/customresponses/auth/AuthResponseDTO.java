package com.mdsrtech.backend.domain.dtos.customresponses.auth;

import com.mdsrtech.backend.domain.dtos.entities.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String message;
    private UserDTO user;
    private String accessToken;
    private String refreshToken;

}