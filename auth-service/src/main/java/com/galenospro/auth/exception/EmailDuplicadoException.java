package com.galenospro.auth.exception;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String email) {
        super("Email ya registrado: " + email);
    }
}
