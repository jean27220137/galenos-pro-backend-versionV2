package com.galenospro.almacen.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(Long medicamentoId) {
        super("Stock insuficiente para medicamento id=" + medicamentoId);
    }
}
