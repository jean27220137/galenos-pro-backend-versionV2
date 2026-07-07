package com.galenospro.auth.repository;

import com.galenospro.auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndActivo(String email, Integer activo);

    List<Usuario> findAllByActivo(Integer activo);
}
