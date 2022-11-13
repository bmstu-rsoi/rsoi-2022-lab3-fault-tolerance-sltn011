package ru.RSOI.Gateway.Model;

public class MRentCarFilter {
    @Override
    public boolean equals(Object obj) {
        MRentCarInfo carInfo = (MRentCarInfo) obj;
        return carInfo.registrationNumber != null && carInfo.model != null && carInfo.brand != null;
    }
}
