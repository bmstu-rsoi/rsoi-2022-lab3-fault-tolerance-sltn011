package ru.RSOI.Payment.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EError400Handler {

    @ExceptionHandler(EBadRequestError.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public EError400 HandleBadRequestError(EBadRequestError error) {
        return new EError400(error);
    }

}
