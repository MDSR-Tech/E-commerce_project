package com.mdsrtech.backend.domain.dtos.customresponses.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSessionDetailsRequestDTO {

    private String sessionId;

}