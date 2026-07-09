package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.MedicamentoRequestDto;
import com.galenospro.almacen.dto.MedicamentoResponseDto;
import com.galenospro.almacen.entity.Medicamento;
import com.galenospro.almacen.exception.MedicamentoNotFoundException;
import com.galenospro.almacen.mapper.MedicamentoMapper;
import com.galenospro.almacen.repository.MedicamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicamentoServiceImplTest {

    @Mock MedicamentoRepository medicamentoRepository;
    @Mock MedicamentoMapper medicamentoMapper;

    @InjectMocks MedicamentoServiceImpl medicamentoService;

    private Medicamento medicamento;
    private MedicamentoResponseDto medicamentoDto;

    @BeforeEach
    void setUp() {
        medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setNombre("Paracetamol 500mg");
        medicamento.setCodigoSismed("SISMED-001");
        medicamento.setActivo(1);

        medicamentoDto = MedicamentoResponseDto.builder()
                .id(1L).nombre("Paracetamol 500mg").codigoSismed("SISMED-001").build();
    }

    @Test
    void listar_retorna_medicamentos_activos() {
        when(medicamentoRepository.findAllByActivo(1)).thenReturn(List.of(medicamento));
        when(medicamentoMapper.toDtoList(List.of(medicamento))).thenReturn(List.of(medicamentoDto));

        List<MedicamentoResponseDto> result = medicamentoService.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Paracetamol 500mg");
    }

    @Test
    void buscarPorId_existente_retorna_dto() {
        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(medicamento));
        when(medicamentoMapper.toDto(medicamento)).thenReturn(medicamentoDto);

        MedicamentoResponseDto result = medicamentoService.buscarPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_inexistente_lanza_excepcion() {
        when(medicamentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicamentoService.buscarPorId(99L))
                .isInstanceOf(MedicamentoNotFoundException.class);
    }

    @Test
    void crear_medicamento_retorna_dto() {
        MedicamentoRequestDto request = new MedicamentoRequestDto();
        request.setNombre("Amoxicilina 500mg");
        request.setCodigoSismed("SISMED-002");

        when(medicamentoMapper.toEntity(request)).thenReturn(medicamento);
        when(medicamentoRepository.save(any())).thenReturn(medicamento);
        when(medicamentoMapper.toDto(medicamento)).thenReturn(medicamentoDto);

        MedicamentoResponseDto result = medicamentoService.crear(request);

        assertThat(result).isNotNull();
    }

    @Test
    void listar_vacio_retorna_lista_vacia() {
        when(medicamentoRepository.findAllByActivo(1)).thenReturn(List.of());
        when(medicamentoMapper.toDtoList(List.of())).thenReturn(List.of());

        List<MedicamentoResponseDto> result = medicamentoService.listar();

        assertThat(result).isEmpty();
    }

    @Test
    void buscarPorId_medicamento_inactivo_lanza_excepcion() {
        medicamento.setActivo(0);
        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(medicamento));

        assertThatThrownBy(() -> medicamentoService.buscarPorId(1L))
                .isInstanceOf(MedicamentoNotFoundException.class);
    }
}
