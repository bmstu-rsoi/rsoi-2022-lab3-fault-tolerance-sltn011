package ru.RSOI.Gateway.Error;

public class EError503 {

    public String message;

    public EError503(EServiceUnavailableError error)
    {
        this.message = error.message;
    }
}
