package ru.RSOI.Payment.Error;

public class EError400Description {
    public String field;
    public String error;

    public EError400Description(String field, String error)
    {
        this.field = field;
        this.error = error;
    }
}
