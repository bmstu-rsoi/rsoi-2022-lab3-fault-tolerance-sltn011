package ru.RSOI.Gateway.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class EServiceUnavailableError extends ECarsErrorBase {

    public String message;

    public EServiceUnavailableError(String message)
    {
        this.message = message;
    }

}
