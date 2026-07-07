package com.galenospro.auth.service;

import com.galenospro.auth.dto.RegistrarUsuarioRequestDto;
import com.galenospro.auth.dto.UsuarioResponseDto;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDto registrar(RegistrarUsuarioRequestDto dto);
    List<UsuarioResponseDto> listar();
    UsuarioResponseDto actualizar(Long id, RegistrarUsuarioRequestDto dto);
    void desactivar(Long id);
}
