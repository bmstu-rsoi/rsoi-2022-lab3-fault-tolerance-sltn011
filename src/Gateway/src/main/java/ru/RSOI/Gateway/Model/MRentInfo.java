package ru.RSOI.Gateway.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class MRentInfo {

    public UUID rentalUid;
    public String status;
    public String dateFrom;
    public String dateTo;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = MRentCarFilter.class)
    public MRentCarInfo car;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = MRentPaymentFilter.class)
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
