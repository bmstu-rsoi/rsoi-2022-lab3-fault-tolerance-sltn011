package ru.RSOI.Gateway.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EError503Handler {

    @ExceptionHandler(EServiceUnavailableError.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public EError503 HandleServiceUnavailableError(EServiceUnavailableError error) {
        return new EError503(error);
    }

}
