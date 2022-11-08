package ru.RSOI.Gateway.Model;

import java.util.UUID;

public class MRentSuccess {

    public UUID rentalUid;
    public String status;
    public UUID carUid;
    public String dateFrom;
    public String dateTo;
    public MRentPaymentInfo payment;

    public MRentSuccess(UUID rentalUid, String status, UUID carUid, String dateFrom, String dateTo, MRentPaymentInfo payment) {
        this.rentalUid = rentalUid;
        this.status = status;
        this.carUid = carUid;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.payment = payment;
    }
}
