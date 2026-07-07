package com.galenospro.farmacia.repository;

import com.galenospro.farmacia.entity.SolicitudRequerimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<SolicitudRequerimiento, Long> {
    List<SolicitudRequerimiento> findByFarmaciaId(Long farmaciaId);
    List<SolicitudRequerimiento> findByEstado(String estado);
    Optional<SolicitudRequerimiento> findByNroSolicitud(String nroSolicitud);
}
