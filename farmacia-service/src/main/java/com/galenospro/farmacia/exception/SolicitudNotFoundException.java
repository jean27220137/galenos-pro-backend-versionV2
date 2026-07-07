package com.galenospro.farmacia.exception;

public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(Long id) {
        super("Solicitud no encontrada: " + id);
    }
}
