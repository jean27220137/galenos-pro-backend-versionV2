package com.galenospro.farmacia.exception;

public class SolicitudDuplicadaException extends RuntimeException {
    public SolicitudDuplicadaException(Long farmaciaId) {
        super("Ya existe una solicitud activa para la farmacia: " + farmaciaId);
    }
}
