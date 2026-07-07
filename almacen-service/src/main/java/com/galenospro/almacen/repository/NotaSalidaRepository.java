package com.galenospro.almacen.repository;

import com.galenospro.almacen.entity.NotaSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotaSalidaRepository extends JpaRepository<NotaSalida, Long> {
    List<NotaSalida> findByAlmacenOrigenId(Long almacenId);
    Optional<NotaSalida> findBySolicitudId(Long solicitudId);
    List<NotaSalida> findByEstado(String estado);
}
