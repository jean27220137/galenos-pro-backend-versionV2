package com.galenospro.auth.service;

import com.galenospro.auth.dto.LoginRequestDto;
import com.galenospro.auth.dto.LoginResponseDto;
import com.galenospro.auth.dto.ValidateTokenResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto dto);
    void logout(String token);
    ValidateTokenResponseDto validateToken(String token);
}
