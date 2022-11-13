package ru.RSOI.Gateway.Model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MRentCarInfo
{
    public UUID carUid;
    public String brand;
    public String model;
    public String registrationNumber;

    public MRentCarInfo(UUID carUid, String brand, String model, String registrationNumber) {
        this.carUid = carUid;
        this.brand = brand;
        this.model = model;
        this.registrationNumber = registrationNumber;
    }
}
