package com.example.airportInfo.api.exception;

public class InvalidIcaoException extends RuntimeException {
    public InvalidIcaoException(String icao) {
        super("Invalid or unsupported ICAO code: " + icao);
    }
}
