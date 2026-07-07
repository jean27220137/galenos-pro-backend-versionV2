package com.galenospro.auth.service;

import com.galenospro.auth.dto.RegistrarUsuarioRequestDto;
import com.galenospro.auth.dto.UsuarioResponseDto;
import com.galenospro.auth.entity.Usuario;
import com.galenospro.auth.exception.EmailDuplicadoException;
import com.galenospro.auth.exception.UsuarioNotFoundException;
import com.galenospro.auth.mapper.UsuarioMapper;
import com.galenospro.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;

    @Override
    public UsuarioResponseDto registrar(RegistrarUsuarioRequestDto dto) {
        Long nuevoId = llamarPrRegistrarUsuario(dataSource, dto);
        log.info("Usuario registrado id={} rol={}", nuevoId, dto.getRol());
        return usuarioMapper.toDto(
                usuarioRepository.findById(nuevoId)
                        .orElseThrow(() -> new UsuarioNotFoundException(nuevoId))
        );
    }

    Long llamarPrRegistrarUsuario(DataSource ds, RegistrarUsuarioRequestDto dto) {
        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_AUTH")
                .withCatalogName("PKG_AUTH")
                .withProcedureName("PR_REGISTRAR_USUARIO");
        try {
            Map<String, Object> resultado = call.execute(Map.of(
                    "p_nombres",       dto.getNombres(),
                    "p_apellidos",     dto.getApellidos(),
                    "p_email",         dto.getEmail(),
                    "p_password_hash", passwordEncoder.encode(dto.getPassword()),
                    "p_cargo",         dto.getCargo() != null ? dto.getCargo() : "",
                    "p_rol",           dto.getRol(),
                    "p_farmacia_id",   dto.getFarmaciaId() != null ? dto.getFarmaciaId() : 0L
            ));
            return ((Number) resultado.get("p_id")).longValue();
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20002")) {
                throw new EmailDuplicadoException(dto.getEmail());
            }
            throw ex;
        }
    }

    @Override
    public List<UsuarioResponseDto> listar() {
        return usuarioMapper.toDtoList(usuarioRepository.findAllByActivo(1));
    }

    @Override
    public UsuarioResponseDto actualizar(Long id, RegistrarUsuarioRequestDto dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCargo(dto.getCargo());
        usuario.setRol(dto.getRol());
        usuario.setFarmaciaId(dto.getFarmaciaId());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return usuarioMapper.toDto(usuarioRepository.save(usuario));
    }

    void llamarPrDesactivarUsuario(DataSource ds, Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(ds)
                .withSchemaName("GP_AUTH")
                .withCatalogName("PKG_AUTH")
                .withProcedureName("PR_DESACTIVAR_USUARIO");
        try {
            call.execute(Map.of("p_id", id));
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("ORA-20003")) {
                throw new UsuarioNotFoundException(id);
            }
            throw ex;
        }
    }

    @Override
    public void desactivar(Long id) {
        llamarPrDesactivarUsuario(dataSource, id);
        log.info("Usuario desactivado id={}", id);
    }
}
