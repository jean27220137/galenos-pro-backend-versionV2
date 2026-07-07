package com.galenospro.almacen.repository;

import com.galenospro.almacen.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    List<Medicamento> findAllByActivo(Integer activo);
    Optional<Medicamento> findByCodigoSismedAndActivo(String codigoSismed, Integer activo);
}
