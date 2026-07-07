package com.galenospro.almacen.exception;

public class AlmacenNotFoundException extends RuntimeException {
    public AlmacenNotFoundException(Long id) {
        super("Almacén no encontrado: " + id);
    }
}
