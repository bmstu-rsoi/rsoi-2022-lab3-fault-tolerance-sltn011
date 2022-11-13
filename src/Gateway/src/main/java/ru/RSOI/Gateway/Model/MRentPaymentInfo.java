package ru.RSOI.Gateway.Model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

public class MRentPaymentInfo
{
    public UUID paymentUid;
    public String status;
    public Integer price;

    public MRentPaymentInfo(UUID paymentUid, String status, Integer price) {
        this.paymentUid = paymentUid;
        this.status = status;
        this.price = price;
    }
}
