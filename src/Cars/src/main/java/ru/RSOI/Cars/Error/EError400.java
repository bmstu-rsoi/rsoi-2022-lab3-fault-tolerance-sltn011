package ru.RSOI.Cars.Error;


import java.util.ArrayList;
import java.util.List;

public class EError400 {

    public String message;
    public List<EError400Description> errors = new ArrayList<>();

    EError400(EBadRequestError error)
    {
        this.message = error.message;
        this.errors = error.errors;
    }
}
