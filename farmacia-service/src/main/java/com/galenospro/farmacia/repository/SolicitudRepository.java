package com.galenospro.farmacia.repository;

import com.galenospro.farmacia.entity.SolicitudRequerimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolicitudRepository extends JpaRepository<SolicitudRequerimiento, Long> {

    @EntityGraph(attributePaths = {"detalles"})
    List<SolicitudRequerimiento> findByFarmaciaId(Long farmaciaId);

    @EntityGraph(attributePaths = {"detalles"})
    List<SolicitudRequerimiento> findByEstado(String estado);

    @EntityGraph(attributePaths = {"detalles"})
    List<SolicitudRequerimiento> findByEstadoIn(List<String> estados);

    Optional<SolicitudRequerimiento> findByNroSolicitud(String nroSolicitud);
}
