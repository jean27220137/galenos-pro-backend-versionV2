package com.galenospro.almacen.service;

import com.galenospro.almacen.dto.MedicamentoRequestDto;
import com.galenospro.almacen.dto.MedicamentoResponseDto;
import com.galenospro.almacen.entity.Medicamento;
import com.galenospro.almacen.exception.MedicamentoNotFoundException;
import com.galenospro.almacen.mapper.MedicamentoMapper;
import com.galenospro.almacen.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    @Override
    @Caching(evict = {
        @CacheEvict(value = "medicamentos", key = "'catalogo'"),
        @CacheEvict(value = "medicamentos", allEntries = true)
    })
    public MedicamentoResponseDto crear(MedicamentoRequestDto dto) {
        Medicamento medicamento = medicamentoMapper.toEntity(dto);
        Medicamento guardado = medicamentoRepository.save(medicamento);
        log.info("Medicamento creado id={} codigo={}", guardado.getId(), guardado.getCodigoSismed());
        return medicamentoMapper.toDto(guardado);
    }

    @Override
    @Cacheable(value = "medicamentos", key = "'catalogo'")
    public List<MedicamentoResponseDto> listar() {
        return medicamentoMapper.toDtoList(medicamentoRepository.findAllByActivo(1));
    }

    @Override
    @Cacheable(value = "medicamentos", key = "#id")
    public MedicamentoResponseDto buscarPorId(Long id) {
        Medicamento med = medicamentoRepository.findById(id)
                .filter(m -> m.getActivo() == 1)
                .orElseThrow(() -> new MedicamentoNotFoundException(id));
        return medicamentoMapper.toDto(med);
    }
}
