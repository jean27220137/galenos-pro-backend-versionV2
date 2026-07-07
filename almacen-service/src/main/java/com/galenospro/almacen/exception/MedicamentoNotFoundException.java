package com.galenospro.almacen.exception;

public class MedicamentoNotFoundException extends RuntimeException {
    public MedicamentoNotFoundException(Long id) {
        super("Medicamento no encontrado: " + id);
    }
    public MedicamentoNotFoundException(String codigo) {
        super("Medicamento no encontrado con código SISMED: " + codigo);
    }
}
