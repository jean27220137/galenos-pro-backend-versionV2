package com.galenospro.farmacia.service;

import com.galenospro.farmacia.dto.FarmaciaResponseDto;
import com.galenospro.farmacia.exception.FarmaciaNotFoundException;
import com.galenospro.farmacia.mapper.FarmaciaMapper;
import com.galenospro.farmacia.repository.FarmaciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FarmaciaServiceImpl implements FarmaciaService {

    private final FarmaciaRepository farmaciaRepository;
    private final FarmaciaMapper farmaciaMapper;

    @Override
    @Cacheable(value = "farmacias", key = "'catalogo'")
    public List<FarmaciaResponseDto> listar() {
        return farmaciaMapper.toDtoList(farmaciaRepository.findAllByActivo(1));
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
}
