package ru.RSOI.Gateway.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ENotFoundError extends RuntimeException {
    public String message;

    public ENotFoundError(String message)
    {
        this.message = message;
    }
}
