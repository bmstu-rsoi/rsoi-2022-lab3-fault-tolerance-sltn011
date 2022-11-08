package ru.RSOI.Gateway.Model;

import java.util.UUID;

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
