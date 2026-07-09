package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.FarmaciaRequestDto;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmaciaServiceImplTest {

    @Mock FarmaciaRepository farmaciaRepository;
    @Mock FarmaciaMapper farmaciaMapper;

    @InjectMocks FarmaciaServiceImpl farmaciaService;

    private Farmacia farmacia;
    private FarmaciaResponseDto farmaciaDto;
    private FarmaciaRequestDto requestDto;

    @BeforeEach
    void setUp() {
        farmacia = Farmacia.builder()
                .id(1L).codigo("FAR-001").nombre("Farmacia Central")
                .area("Consulta Externa").tipo("CONSULTA_EXTERNA").activo(1).build();

        farmaciaDto = FarmaciaResponseDto.builder()
                .id(1L).codigo("FAR-001").nombre("Farmacia Central")
                .area("Consulta Externa").tipo("CONSULTA_EXTERNA").build();

        requestDto = FarmaciaRequestDto.builder()
                .codigo("FAR-001").nombre("Farmacia Central").tipo("CONSULTA_EXTERNA").build();
    }

    // ── listar ───────────────────────────────────────────────────────────────

    @Test
    void listar_retorna_farmacias_activas() {
        when(farmaciaRepository.findAllByActivo(1)).thenReturn(List.of(farmacia));
        when(farmaciaMapper.toDtoList(List.of(farmacia))).thenReturn(List.of(farmaciaDto));

        List<FarmaciaResponseDto> result = farmaciaService.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo("FAR-001");
    }

    @Test
    void listarTodas_retorna_todas_las_farmacias() {
        Farmacia farmaciaInactiva = Farmacia.builder()
                .id(2L).codigo("FAR-002").nombre("Farmacia Inactiva").activo(0).build();
        FarmaciaResponseDto dtoInactiva = FarmaciaResponseDto.builder()
                .id(2L).codigo("FAR-002").nombre("Farmacia Inactiva").build();

        when(farmaciaRepository.findAll()).thenReturn(List.of(farmacia, farmaciaInactiva));
        when(farmaciaMapper.toDtoList(anyList())).thenReturn(List.of(farmaciaDto, dtoInactiva));

        List<FarmaciaResponseDto> result = farmaciaService.listarTodas();

        assertThat(result).hasSize(2);
    }

    // ── buscarPorId ──────────────────────────────────────────────────────────

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

    @Test
    void buscarPorId_activo_cero_lanza_excepcion() {
        farmacia.setActivo(0);
        when(farmaciaRepository.findById(1L)).thenReturn(Optional.of(farmacia));

        assertThatThrownBy(() -> farmaciaService.buscarPorId(1L))
                .isInstanceOf(FarmaciaNotFoundException.class);
    }

    // ── crear ────────────────────────────────────────────────────────────────

    @Test
    void crear_exitoso_retorna_dto() {
        Farmacia nuevaFarmacia = Farmacia.builder()
                .codigo("FAR-001").nombre("Farmacia Central").tipo("CONSULTA_EXTERNA").build();

        when(farmaciaMapper.toEntity(requestDto)).thenReturn(nuevaFarmacia);
        when(farmaciaRepository.save(nuevaFarmacia)).thenReturn(farmacia);
        when(farmaciaMapper.toDto(farmacia)).thenReturn(farmaciaDto);

        FarmaciaResponseDto result = farmaciaService.crear(requestDto);

        assertThat(result.getCodigo()).isEqualTo("FAR-001");
        assertThat(nuevaFarmacia.getActivo()).isEqualTo(1);
        verify(farmaciaRepository).save(nuevaFarmacia);
    }

    @Test
    void crear_codigo_duplicado_lanza_excepcion() {
        Farmacia nuevaFarmacia = Farmacia.builder()
                .codigo("FAR-001").nombre("Farmacia Central").tipo("CONSULTA_EXTERNA").build();

        when(farmaciaMapper.toEntity(requestDto)).thenReturn(nuevaFarmacia);
        when(farmaciaRepository.save(nuevaFarmacia))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> farmaciaService.crear(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FAR-001");
    }

    // ── actualizar ───────────────────────────────────────────────────────────

    @Test
    void actualizar_exitoso_retorna_dto() {
        when(farmaciaRepository.findById(1L)).thenReturn(Optional.of(farmacia));
        doNothing().when(farmaciaMapper).updateFromDto(requestDto, farmacia);
        when(farmaciaRepository.save(farmacia)).thenReturn(farmacia);
        when(farmaciaMapper.toDto(farmacia)).thenReturn(farmaciaDto);

        FarmaciaResponseDto result = farmaciaService.actualizar(1L, requestDto);

        assertThat(result.getNombre()).isEqualTo("Farmacia Central");
        verify(farmaciaMapper).updateFromDto(requestDto, farmacia);
    }

    @Test
    void actualizar_inexistente_lanza_excepcion() {
        when(farmaciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmaciaService.actualizar(99L, requestDto))
                .isInstanceOf(FarmaciaNotFoundException.class);
    }

    @Test
    void actualizar_codigo_duplicado_lanza_excepcion() {
        when(farmaciaRepository.findById(1L)).thenReturn(Optional.of(farmacia));
        doNothing().when(farmaciaMapper).updateFromDto(requestDto, farmacia);
        when(farmaciaRepository.save(farmacia))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> farmaciaService.actualizar(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FAR-001");
    }

    // ── desactivar ───────────────────────────────────────────────────────────

    @Test
    void desactivar_exitoso() {
        when(farmaciaRepository.findById(1L)).thenReturn(Optional.of(farmacia));
        when(farmaciaRepository.save(farmacia)).thenReturn(farmacia);

        farmaciaService.desactivar(1L);

        assertThat(farmacia.getActivo()).isEqualTo(0);
        verify(farmaciaRepository).save(farmacia);
    }

    @Test
    void desactivar_inexistente_lanza_excepcion() {
        when(farmaciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmaciaService.desactivar(99L))
                .isInstanceOf(FarmaciaNotFoundException.class);
    }

    // ── activar ──────────────────────────────────────────────────────────────

    @Test
    void activar_exitoso() {
        farmacia.setActivo(0);
        when(farmaciaRepository.findById(1L)).thenReturn(Optional.of(farmacia));
        when(farmaciaRepository.save(farmacia)).thenReturn(farmacia);

        farmaciaService.activar(1L);

        assertThat(farmacia.getActivo()).isEqualTo(1);
        verify(farmaciaRepository).save(farmacia);
    }

    @Test
    void activar_inexistente_lanza_excepcion() {
        when(farmaciaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmaciaService.activar(99L))
                .isInstanceOf(FarmaciaNotFoundException.class);
    }
}
