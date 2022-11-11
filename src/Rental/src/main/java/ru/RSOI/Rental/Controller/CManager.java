package ru.RSOI.Rental.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manage")
public class CManager {

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public void checkIsAvailable()
    {
        return;
    }
}
