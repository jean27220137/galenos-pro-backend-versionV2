package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.EntradaStockRequestDto;
import com.galenospro.almacen.dto.StockResponseDto;
import com.galenospro.almacen.entity.Medicamento;
import com.galenospro.almacen.entity.Stock;
import com.galenospro.almacen.mapper.StockMapper;
import com.galenospro.almacen.repository.MedicamentoRepository;
import com.galenospro.almacen.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock StockRepository stockRepository;
    @Mock MedicamentoRepository medicamentoRepository;
    @Mock StockMapper stockMapper;
    @Mock DataSource dataSource;

    @InjectMocks StockServiceImpl stockService;

    private Stock stock;
    private StockResponseDto stockDto;
    private EntradaStockRequestDto entradaDto;

    @BeforeEach
    void setUp() {
        stock = new Stock();
        stock.setId(1L);
        stock.setMedicamentoId(10L);
        stock.setAlmacenId(1L);
        stock.setLote("LOTE-001");
        stock.setCantidad(100);

        stockDto = StockResponseDto.builder()
                .id(1L).medicamentoId(10L).almacenId(1L)
                .lote("LOTE-001").cantidad(100).build();

        entradaDto = new EntradaStockRequestDto();
        entradaDto.setMedicamentoId(10L);
        entradaDto.setAlmacenId(1L);
        entradaDto.setLote("LOTE-001");
        entradaDto.setCantidad(100);
        entradaDto.setFechaVencimiento(LocalDate.of(2026, 12, 31));
        entradaDto.setPrecioUnitario(new BigDecimal("5.50"));
    }

    @Test
    void registrarEntrada_exitoso_retorna_dto() {
        StockServiceImpl spyService = spy(stockService);
        doReturn(1L).when(spyService).llamarPrRegistrarEntrada(any(), any());
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(stockMapper.toDto(stock)).thenReturn(stockDto);
        when(medicamentoRepository.findById(10L)).thenReturn(Optional.empty());

        StockResponseDto result = spyService.registrarEntrada(entradaDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLote()).isEqualTo("LOTE-001");
    }

    @Test
    void consultarDisponible_retorna_suma() {
        when(stockRepository.sumCantidadDisponible(10L, 1L)).thenReturn(250);

        Integer result = stockService.consultarDisponible(10L, 1L);

        assertThat(result).isEqualTo(250);
    }

    @Test
    void consultarDisponible_sin_stock_retorna_cero() {
        when(stockRepository.sumCantidadDisponible(10L, 1L)).thenReturn(null);

        Integer result = stockService.consultarDisponible(10L, 1L);

        assertThat(result).isZero();
    }

    @Test
    void listarPorAlmacen_retorna_lista() {
        when(stockRepository.findByAlmacenId(1L)).thenReturn(List.of(stock));
        when(stockMapper.toDto(stock)).thenReturn(stockDto);
        when(medicamentoRepository.findById(10L)).thenReturn(Optional.empty());

        List<StockResponseDto> result = stockService.listarPorAlmacen(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarPorMedicamento_retorna_lista_ordenada() {
        when(stockRepository.findByMedicamentoIdAndAlmacenIdOrderByFechaVencimientoAsc(10L, 1L))
                .thenReturn(List.of(stock));
        when(stockMapper.toDto(stock)).thenReturn(stockDto);
        when(medicamentoRepository.findById(10L)).thenReturn(Optional.empty());

        List<StockResponseDto> result = stockService.listarPorMedicamento(10L, 1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void registrarEntrada_enriquece_con_nombre_medicamento() {
        Medicamento med = new Medicamento();
        med.setId(10L);
        med.setNombre("Paracetamol 500mg");
        med.setCodigoSismed("SISMED-001");

        StockServiceImpl spyService = spy(stockService);
        doReturn(1L).when(spyService).llamarPrRegistrarEntrada(any(), any());
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(stockMapper.toDto(stock)).thenReturn(stockDto);
        when(medicamentoRepository.findById(10L)).thenReturn(Optional.of(med));

        StockResponseDto result = spyService.registrarEntrada(entradaDto);

        assertThat(result.getNombreMedicamento()).isEqualTo("Paracetamol 500mg");
        assertThat(result.getCodigoSismed()).isEqualTo("SISMED-001");
    }
}
