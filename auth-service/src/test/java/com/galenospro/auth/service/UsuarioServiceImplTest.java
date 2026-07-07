package com.galenospro.auth.service;

import com.galenospro.auth.dto.RegistrarUsuarioRequestDto;
import com.galenospro.auth.dto.UsuarioResponseDto;
import com.galenospro.auth.entity.Usuario;
import com.galenospro.auth.exception.EmailDuplicadoException;
import com.galenospro.auth.exception.UsuarioNotFoundException;
import com.galenospro.auth.mapper.UsuarioMapper;
import com.galenospro.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DataSource dataSource;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private UsuarioResponseDto usuarioDto;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L).nombres("Ana").apellidos("Torres")
                .email("ana@bernales.gob.pe").password("hash")
                .rol("JEFE_FARMACIA").farmaciaId(1L).activo(1)
                .build();

        usuarioDto = UsuarioResponseDto.builder()
                .id(1L).nombres("Ana").apellidos("Torres")
                .email("ana@bernales.gob.pe").rol("JEFE_FARMACIA")
                .farmaciaId(1L).activo(1)
                .build();
    }

    @Test
    void listar_retorna_solo_usuarios_activos() {
        when(usuarioRepository.findAllByActivo(1)).thenReturn(List.of(usuario));
        when(usuarioMapper.toDtoList(List.of(usuario))).thenReturn(List.of(usuarioDto));

        List<UsuarioResponseDto> resultado = usuarioService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEmail()).isEqualTo("ana@bernales.gob.pe");
    }

    @Test
    void actualizar_usuario_existente_retorna_dto_actualizado() {
        RegistrarUsuarioRequestDto dto = new RegistrarUsuarioRequestDto();
        dto.setNombres("Ana"); dto.setApellidos("Torres");
        dto.setEmail("ana@bernales.gob.pe"); dto.setRol("ADMIN");
        dto.setFarmaciaId(null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioMapper.toDto(usuario)).thenReturn(usuarioDto);

        UsuarioResponseDto resultado = usuarioService.actualizar(1L, dto);

        assertThat(resultado).isNotNull();
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizar_usuario_inexistente_lanza_excepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.actualizar(99L, new RegistrarUsuarioRequestDto()))
                .isInstanceOf(UsuarioNotFoundException.class);
    }

    @Test
    void desactivar_usuario_inexistente_lanza_excepcion() {
        UsuarioServiceImpl spyService = spy(usuarioService);
        doThrow(new UsuarioNotFoundException(99L))
                .when(spyService).llamarPrDesactivarUsuario(any(), anyLong());

        assertThatThrownBy(() -> spyService.desactivar(99L))
                .isInstanceOf(UsuarioNotFoundException.class);
    }

    @Test
    void desactivar_usuario_existente_exitoso() {
        UsuarioServiceImpl spyService = spy(usuarioService);
        doNothing().when(spyService).llamarPrDesactivarUsuario(any(), anyLong());

        spyService.desactivar(1L);

        verify(spyService).llamarPrDesactivarUsuario(dataSource, 1L);
    }

    @Test
    void registrar_usuario_nuevo_retorna_dto() {
        RegistrarUsuarioRequestDto dto = new RegistrarUsuarioRequestDto();
        dto.setNombres("Ana"); dto.setApellidos("Torres");
        dto.setEmail("ana@bernales.gob.pe"); dto.setPassword("clave");
        dto.setRol("JEFE_FARMACIA"); dto.setFarmaciaId(1L);

        UsuarioServiceImpl spyService = spy(usuarioService);
        doReturn(1L).when(spyService).llamarPrRegistrarUsuario(any(), any());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDto(usuario)).thenReturn(usuarioDto);

        UsuarioResponseDto resultado = spyService.registrar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("ana@bernales.gob.pe");
    }

    @Test
    void registrar_email_duplicado_lanza_excepcion() {
        RegistrarUsuarioRequestDto dto = new RegistrarUsuarioRequestDto();
        dto.setNombres("Ana"); dto.setApellidos("Torres");
        dto.setEmail("ana@bernales.gob.pe"); dto.setPassword("clave");
        dto.setRol("JEFE_FARMACIA");

        UsuarioServiceImpl spyService = spy(usuarioService);
        doThrow(new EmailDuplicadoException("ana@bernales.gob.pe"))
                .when(spyService).llamarPrRegistrarUsuario(any(), any());

        assertThatThrownBy(() -> spyService.registrar(dto))
                .isInstanceOf(EmailDuplicadoException.class);
    }
}
