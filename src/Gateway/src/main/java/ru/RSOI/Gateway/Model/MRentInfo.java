package ru.RSOI.Gateway.Model;

import java.util.UUID;

public class MRentInfo {

    public UUID rentalUid;
    public String status;
    public String dateFrom;
    public String dateTo;
    public MRentCarInfo car;
    public MRentPaymentInfo payment;

    public MRentInfo(
            UUID rentalUid, String status, String dateFrom, String dateTo, MRentCarInfo car, MRentPaymentInfo payment) {
        this.rentalUid = rentalUid;
        this.status = status;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.car = car;
        this.payment = payment;
    }

}
