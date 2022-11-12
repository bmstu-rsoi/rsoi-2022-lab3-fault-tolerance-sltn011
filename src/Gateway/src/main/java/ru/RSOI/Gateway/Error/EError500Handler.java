package ru.RSOI.Gateway.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EError500Handler {

    @ExceptionHandler(EInternalServerError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public EError500 HandleInternalServerError(EInternalServerError error) {
        return new EError500(error);
    }

}
