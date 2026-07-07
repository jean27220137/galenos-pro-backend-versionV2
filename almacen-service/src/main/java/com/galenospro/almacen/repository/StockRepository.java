package com.galenospro.almacen.repository;

import com.galenospro.almacen.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {

    List<Stock> findByAlmacenId(Long almacenId);

    List<Stock> findByMedicamentoIdAndAlmacenIdOrderByFechaVencimientoAsc(
            Long medicamentoId, Long almacenId);

    @Query("SELECT COALESCE(SUM(s.cantidad), 0) FROM Stock s " +
           "WHERE s.medicamentoId = :medId AND s.almacenId = :almId " +
           "AND s.fechaVencimiento > CURRENT_DATE")
    Integer sumCantidadDisponible(@Param("medId") Long medId, @Param("almId") Long almId);
}
