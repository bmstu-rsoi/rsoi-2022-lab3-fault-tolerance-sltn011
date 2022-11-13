package ru.RSOI.Gateway.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public class MRentInfo {

    public UUID rentalUid;
    public String status;
    public String dateFrom;
    public String dateTo;
    public MRentCarInfo car;
    public MRentPaymentInfo payment;

    @JsonIgnore
    public String paymentUid;
    @JsonIgnore
    public String carUid;

    public MRentInfo(
            UUID rentalUid, String status, String dateFrom, String dateTo, MRentCarInfo car, MRentPaymentInfo payment) {
        this.rentalUid = rentalUid;
        this.status = status;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.car = car;
        this.payment = payment;
    }

    public MRentInfo(
            UUID rentalUid, String status, String dateFrom, String dateTo, MRentCarInfo car, MRentPaymentInfo payment,
            String paymentUid, String carUid) {
        this.rentalUid = rentalUid;
        this.status = status;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.car = car;
        this.payment = payment;

        this.paymentUid = paymentUid;
        this.carUid = carUid;
    }

}
