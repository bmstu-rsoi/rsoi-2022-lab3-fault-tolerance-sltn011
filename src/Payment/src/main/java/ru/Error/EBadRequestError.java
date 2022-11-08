package ru.RSOI.Payment.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EBadRequestError extends RuntimeException {
    public String message;
    public List<String> errors = new ArrayList<String>();

    public EBadRequestError(String message, ArrayList<String> errors)
    {
        this.message = message;
        this.errors = errors;
    }
}
