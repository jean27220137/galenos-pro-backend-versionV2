package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.FarmaciaRequestDto;
import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.exception.FarmaciaNotFoundException;
import com.galenospro.farmacia.mapper.FarmaciaMapper;
import com.galenospro.farmacia.repository.FarmaciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmaciaServiceImpl implements FarmaciaService {

    private final FarmaciaRepository farmaciaRepository;
    private final FarmaciaMapper     farmaciaMapper;

    @Override
    @Cacheable(value = "farmacias", key = "'catalogo'")
    public List<FarmaciaResponseDto> listar() {
        return farmaciaMapper.toDtoList(farmaciaRepository.findAllByActivo(1));
    }

    @Override
    public List<FarmaciaResponseDto> listarTodas() {
        return farmaciaMapper.toDtoList(farmaciaRepository.findAll());
    }

    @Override
    @Cacheable(value = "farmacias", key = "#id")
    public FarmaciaResponseDto buscarPorId(Long id) {
        return farmaciaMapper.toDto(
                farmaciaRepository.findById(id)
                        .filter(f -> f.getActivo() == 1)
                        .orElseThrow(() -> new FarmaciaNotFoundException(id))
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "farmacias", allEntries = true)
    public FarmaciaResponseDto crear(FarmaciaRequestDto dto) {
        try {
            var farmacia = farmaciaMapper.toEntity(dto);
            farmacia.setActivo(1);
            log.info("Creando farmacia: {}", dto.getCodigo());
            return farmaciaMapper.toDto(farmaciaRepository.save(farmacia));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe una farmacia con el código " + dto.getCodigo());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "farmacias", allEntries = true)
    public FarmaciaResponseDto actualizar(Long id, FarmaciaRequestDto dto) {
        var farmacia = farmaciaRepository.findById(id)
                .orElseThrow(() -> new FarmaciaNotFoundException(id));
        try {
            farmaciaMapper.updateFromDto(dto, farmacia);
            log.info("Actualizando farmacia ID={}: {}", id, dto.getNombre());
            return farmaciaMapper.toDto(farmaciaRepository.save(farmacia));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe una farmacia con el código " + dto.getCodigo());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "farmacias", allEntries = true)
    public void desactivar(Long id) {
        var farmacia = farmaciaRepository.findById(id)
                .orElseThrow(() -> new FarmaciaNotFoundException(id));
        farmacia.setActivo(0);
        farmaciaRepository.save(farmacia);
        log.info("Farmacia ID={} desactivada", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "farmacias", allEntries = true)
    public void activar(Long id) {
        var farmacia = farmaciaRepository.findById(id)
                .orElseThrow(() -> new FarmaciaNotFoundException(id));
        farmacia.setActivo(1);
        farmaciaRepository.save(farmacia);
        log.info("Farmacia ID={} activada", id);
    }
}
