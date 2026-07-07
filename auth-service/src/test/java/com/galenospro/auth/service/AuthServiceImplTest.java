package com.galenospro.auth.service;

import com.galenospro.auth.config.JwtConfig;
import com.galenospro.auth.dto.LoginRequestDto;
import com.galenospro.auth.dto.LoginResponseDto;
import com.galenospro.auth.dto.ValidateTokenResponseDto;
import com.galenospro.auth.entity.Usuario;
import com.galenospro.auth.exception.CredencialesInvalidasException;
import com.galenospro.auth.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JwtConfig jwtConfig;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = Usuario.builder()
                .id(1L).nombres("Juan").apellidos("Perez")
                .email("juan@bernales.gob.pe").password("$2a$10$hash")
                .rol("FARMACEUTICO").farmaciaId(2L).activo(1)
                .build();
    }

    @Test
    void login_exitoso_retorna_jwt() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("juan@bernales.gob.pe");
        dto.setPassword("clave123");

        when(usuarioRepository.findByEmailAndActivo("juan@bernales.gob.pe", 1))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("clave123", "$2a$10$hash")).thenReturn(true);
        when(jwtConfig.generarToken(1L, "juan@bernales.gob.pe", "FARMACEUTICO", 2L))
                .thenReturn("jwt-token-generado");
        when(jwtConfig.getExpiracion()).thenReturn(LocalDateTime.now().plusMinutes(30));

        LoginResponseDto response = authService.login(dto);

        assertThat(response.getToken()).isEqualTo("jwt-token-generado");
        assertThat(response.getRol()).isEqualTo("FARMACEUTICO");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_usuario_no_encontrado_lanza_excepcion() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("noexiste@test.com");
        dto.setPassword("mal");

        when(usuarioRepository.findByEmailAndActivo("noexiste@test.com", 1))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void login_password_incorrecto_lanza_excepcion() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("juan@bernales.gob.pe");
        dto.setPassword("claveErronea");

        when(usuarioRepository.findByEmailAndActivo("juan@bernales.gob.pe", 1))
                .thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches("claveErronea", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void logout_agrega_token_a_blacklist_redis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtConfig.getExpirationMinutes()).thenReturn(30);

        authService.logout("token-valido");

        verify(valueOperations).set(eq("jwt:blacklist:token-valido"), eq("revocado"), any());
    }

    @Test
    void validateToken_en_blacklist_lanza_excepcion() {
        when(redisTemplate.hasKey("jwt:blacklist:token-revocado")).thenReturn(true);

        assertThatThrownBy(() -> authService.validateToken("token-revocado"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void validateToken_valido_retorna_claims() {
        Claims claims = mock(Claims.class);
        when(redisTemplate.hasKey("jwt:blacklist:token-ok")).thenReturn(false);
        when(jwtConfig.validarToken("token-ok")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.getSubject()).thenReturn("juan@bernales.gob.pe");
        when(claims.get("rol", String.class)).thenReturn("FARMACEUTICO");
        when(claims.get("farmaciaId", Long.class)).thenReturn(2L);

        ValidateTokenResponseDto response = authService.validateToken("token-ok");

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRol()).isEqualTo("FARMACEUTICO");
    }
}
