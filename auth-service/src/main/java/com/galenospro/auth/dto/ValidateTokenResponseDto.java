package com.galenospro.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateTokenResponseDto {
    private Long userId;
    private String email;
    private String rol;
    private Long farmaciaId;
}
