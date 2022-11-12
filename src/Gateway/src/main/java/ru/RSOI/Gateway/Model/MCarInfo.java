package ru.RSOI.Gateway.Model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MCarInfo {

    public UUID carUid;
    public String brand;
    public String model;
    public String registrationNumber;
    public Integer power;
    public String type;
    public Integer price;
    public Boolean available;


    public MCarInfo(UUID carUid, String brand, String model, String registrationNumber,
                    Integer power, String type, Integer price, Boolean available) {
        this.carUid = carUid;
        this.brand = brand;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.power = power;
        this.type = type;
        this.price = price;
        this.available = available;
    }
}
