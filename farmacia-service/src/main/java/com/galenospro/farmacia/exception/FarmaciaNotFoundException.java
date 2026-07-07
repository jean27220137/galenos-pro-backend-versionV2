package com.galenospro.farmacia.exception;

public class FarmaciaNotFoundException extends RuntimeException {
    public FarmaciaNotFoundException(Long id) {
        super("Farmacia no encontrada: " + id);
    }
}
