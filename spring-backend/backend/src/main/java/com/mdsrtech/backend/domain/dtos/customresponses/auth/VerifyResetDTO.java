package com.mdsrtech.backend.domain.dtos.customresponses.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyResetDTO {

    private Boolean valid;
    private String email;

}