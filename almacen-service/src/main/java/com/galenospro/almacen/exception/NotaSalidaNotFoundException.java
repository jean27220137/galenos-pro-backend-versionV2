package com.galenospro.almacen.exception;

public class NotaSalidaNotFoundException extends RuntimeException {
    public NotaSalidaNotFoundException(Long id) {
        super("Nota de Salida no encontrada: " + id);
    }
}
