package ru.RSOI.Gateway.Error;

public class EError500 {

    public String message;

    public EError500(EInternalServerError error)
    {
        this.message = error.message;
    }
}
