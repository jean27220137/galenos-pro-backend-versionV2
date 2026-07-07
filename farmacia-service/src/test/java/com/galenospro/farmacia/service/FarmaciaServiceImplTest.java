package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.entity.Farmacia;
import com.galenospro.farmacia.exception.FarmaciaNotFoundException;
import com.galenospro.farmacia.mapper.FarmaciaMapper;
import com.galenospro.farmacia.repository.FarmaciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmaciaServiceImplTest {

    @Mock FarmaciaRepository farmaciaRepository;
    @Mock FarmaciaMapper farmaciaMapper;

    @InjectMocks FarmaciaServiceImpl farmaciaService;

    private Farmacia farmacia;
    private FarmaciaResponseDto farmaciaDto;

    @BeforeEach
    void setUp() {
        farmacia = Farmacia.builder()
                .id(1L).codigo("FAR-001").nombre("Farmacia Central")
                .area("Consulta Externa").tipo("CONSULTA_EXTERNA").activo(1).build();

        farmaciaDto = FarmaciaResponseDto.builder()
                .id(1L).codigo("FAR-001").nombre("Farmacia Central")
                .area("Consulta Externa").tipo("CONSULTA_EXTERNA").build();
    }

    @Test
    void listar_retorna_farmacias_activas() {
        when(farmaciaRepository.findAllByActivo(1)).thenReturn(List.of(farmacia));
        when(farmaciaMapper.toDtoList(List.of(farmacia))).thenReturn(List.of(farmaciaDto));

        List<FarmaciaResponseDto> result = farmaciaService.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo("FAR-001");
    }

    @Test
    void buscarPorId_existente_retorna_dto() {
        when(farmaciaRepository.findById(1L)).thenReturn(Optional.of(farmacia));
        when(farmaciaMapper.toDto(farmacia)).thenReturn(farmaciaDto);

        FarmaciaResponseDto result = farmaciaService.buscarPorId(1L);

        assertThat(result.getNombre()).isEqualTo("Farmacia Central");
    }

    @Test
    void buscarPorId_inexistente_lanza_excepcion() {
        when(farmaciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmaciaService.buscarPorId(99L))
                .isInstanceOf(FarmaciaNotFoundException.class);
    }
}
