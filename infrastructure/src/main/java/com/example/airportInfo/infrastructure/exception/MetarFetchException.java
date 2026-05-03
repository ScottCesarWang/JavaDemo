package com.example.airportInfo.infrastructure.exception;

public class MetarFetchException extends RuntimeException {
    public MetarFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
