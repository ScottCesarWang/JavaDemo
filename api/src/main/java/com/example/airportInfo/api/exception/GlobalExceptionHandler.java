package com.example.airportInfo.api.exception;

import com.example.airportInfo.infrastructure.exception.MetarFetchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidIcaoException.class)
    public ResponseEntity<ProblemDetail> handleInvalidIcao(InvalidIcaoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid ICAO Code");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MetarFetchException.class)
    public ResponseEntity<ProblemDetail> handleMetarFetch(MetarFetchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("METAR Service Unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        return ResponseEntity.internalServerError().body(problem);
    }
}
