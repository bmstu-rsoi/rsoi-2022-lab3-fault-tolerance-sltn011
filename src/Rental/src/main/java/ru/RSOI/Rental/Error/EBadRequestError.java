package ru.RSOI.Rental.Error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class EBadRequestError extends RuntimeException {
    public String message;
    public List<EError400Description> errors;

    public EBadRequestError(String message, ArrayList<EError400Description> errors)
    {
        this.message = message;
        this.errors = errors;
    }
}
