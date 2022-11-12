package ru.RSOI.Gateway.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EInternalServerError extends ECarsErrorBase {

    public String message;

    public EInternalServerError(String message)
    {
        this.message = message;
    }

}
