package com.galenospro.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String rol;
    private Long userId;
    private Long farmaciaId;
    private LocalDateTime expira;
}
