package com.galenospro.auth.mapper;

import com.galenospro.auth.dto.UsuarioResponseDto;
import com.galenospro.auth.entity.Usuario;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponseDto toDto(Usuario usuario);

    List<UsuarioResponseDto> toDtoList(List<Usuario> usuarios);
}
