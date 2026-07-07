package com.galenospro.auth.exception;

public class UsuarioInactivoException extends RuntimeException {
    public UsuarioInactivoException() {
        super("Usuario inactivo");
    }
}
