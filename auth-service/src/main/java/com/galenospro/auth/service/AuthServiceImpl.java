package com.galenospro.auth.service;

import com.galenospro.auth.config.JwtConfig;
import com.galenospro.auth.dto.LoginRequestDto;
import com.galenospro.auth.dto.LoginResponseDto;
import com.galenospro.auth.dto.ValidateTokenResponseDto;
import com.galenospro.auth.entity.Usuario;
import com.galenospro.auth.exception.CredencialesInvalidasException;
import com.galenospro.auth.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        Usuario usuario = usuarioRepository.findByEmailAndActivo(dto.getEmail(), 1)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtConfig.generarToken(
                usuario.getId(), usuario.getEmail(),
                usuario.getRol(), usuario.getFarmaciaId()
        );

        log.info("Login exitoso para usuario id={} rol={}", usuario.getId(), usuario.getRol());

        return LoginResponseDto.builder()
                .token(token)
                .rol(usuario.getRol())
                .userId(usuario.getId())
                .farmaciaId(usuario.getFarmaciaId())
                .expira(jwtConfig.getExpiracion())
                .build();
    }

    @Override
    public void logout(String token) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "revocado",
                Duration.ofMinutes(jwtConfig.getExpirationMinutes())
        );
        log.info("Token añadido a blacklist Redis");
    }

    @Override
    public ValidateTokenResponseDto validateToken(String token) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token))) {
            throw new CredencialesInvalidasException();
        }

        Claims claims = jwtConfig.validarToken(token);

        return ValidateTokenResponseDto.builder()
                .userId(claims.get("userId", Long.class))
                .email(claims.getSubject())
                .rol(claims.get("rol", String.class))
                .farmaciaId(claims.get("farmaciaId", Long.class))
                .build();
    }
}
