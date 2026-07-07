package com.galenospro.almacen.repository;

import com.galenospro.almacen.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    List<Almacen> findAllByActivo(Integer activo);
}
