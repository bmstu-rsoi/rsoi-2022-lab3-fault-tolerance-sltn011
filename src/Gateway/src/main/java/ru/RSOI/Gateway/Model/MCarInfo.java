package ru.RSOI.Gateway.Model;

import java.util.UUID;

public class MCarInfo {

    public UUID carUid;
    public String brand;
    public String model;
    public String registrationNumber;
    public int power;
    public String type;
    public int price;
    public boolean available;


    public MCarInfo(UUID carUid, String brand, String model, String registrationNumber,
                    int power, String type, int price, boolean available) {
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
