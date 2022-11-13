package ru.RSOI.Cars.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EError404Handler {

    @ExceptionHandler(ENotFoundError.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public EError404 HandleNotFoundError(ENotFoundError error) {
        return new EError404(error.message);
    }

}
