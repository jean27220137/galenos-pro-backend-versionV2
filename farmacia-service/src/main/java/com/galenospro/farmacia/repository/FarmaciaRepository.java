package com.galenospro.farmacia.repository;

import com.galenospro.farmacia.entity.Farmacia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmaciaRepository extends JpaRepository<Farmacia, Long> {
    List<Farmacia> findAllByActivo(int activo);
}
