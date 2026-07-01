package com.fernando.veiculos.domain.exception;

public class CurrencyConversionException extends RuntimeException {

    public CurrencyConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
