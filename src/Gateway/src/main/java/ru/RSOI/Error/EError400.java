package ru.RSOI.Gateway.Error;

import java.util.HashMap;
import java.util.Map;

public class EError400 {
    public String message;
    public Map<String, String> errors = new HashMap<String, String>();

    EError400(EBadRequestError error)
    {
        this.message = error.message;
        for (int i = 0; i < error.errors.size(); i++)
        {
            addError(error.errors.get(i));
        }
    }

    public void addError(String error)
    {
        String key = new String("additionalProp") + String.format("%d", errors.size() + 1);
        errors.put(key, error);
    }
}
